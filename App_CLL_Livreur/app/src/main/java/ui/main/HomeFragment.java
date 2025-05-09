package ui.main;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.app_cll_livreur.BuildConfig;
import com.example.app_cll_livreur.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import data.api.amazi.AmaziServices;
import data.controller.ColisController;
import data.model.Colis;
import ui.ColisAdapter;
import ui.ColisViewModel;
import ui.DeliveryIdHolder;
import ui.delivery.DeliveryActivity;
import ui.QrCodeFragment;
import ui.login.LoginActivity;
import utils.TokenHolder;

/**
 * Fragment that displays the home screen with the list of packages (Colis).
 * <p>
 * Shows a RecyclerView of current packages, allows swipe-to-delete with undo,
 * logout, navigation to map or delivery screens, and adding new packages via QR scan.
 * Observes the {@link ColisViewModel} for data changes.
 * </p>
 */
public class HomeFragment extends Fragment {

    /** RecyclerView displaying the list of Colis items. */
    private RecyclerView recyclerView;

    /** Adapter for binding Colis data to RecyclerView items. */
    private ColisAdapter adapter;

    /** List backing the adapter before observing LiveData updates. */
    private List<Colis> colisList;

    /** ViewModel holding the package data and delivery ID. */
    private ColisViewModel colisViewModel;

    /** Default empty constructor. */
    public HomeFragment() { }

    /**
     * Inflates the home fragment layout, initializes UI components,
     * sets up data observers and swipe-to-delete behavior, and configures
     * navigation actions (logout, map view, delivery view, and QR scan).
     *
     * @param inflater           the LayoutInflater to inflate views
     * @param container          the parent view that the fragment's UI should attach to
     * @param savedInstanceState if non-null, this fragment is being re-created
     * @return the root View for this fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Configure RecyclerView and its adapter
        recyclerView = view.findViewById(R.id.rv_colis);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        colisList = new ArrayList<>();
        adapter = new ColisAdapter(colisList);
        recyclerView.setAdapter(adapter);

        colisViewModel = new ViewModelProvider(requireActivity()).get(ColisViewModel.class);
        // colisViewModel.setLivraisonId();

        // Observe package list changes to update the adapter
        colisViewModel.getColis().observe(getViewLifecycleOwner(), new Observer<List<Colis>>() {
            @Override
            public void onChanged(List<Colis> colis) {
                adapter.setColisList(colis);
            }
        });

        // Enable swipe-to-delete with undo functionality
        enableSwipeToDeleteWithUndo();

        // Logout button navigates back to LoginActivity
        ImageView btnGoToLoginActivity = view.findViewById(R.id.btn_logout);
        btnGoToLoginActivity.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        // Button to navigate to the map fragment
        Button btnGoToMapFragment = view.findViewById(R.id.btn_itineraire);
        btnGoToMapFragment.setOnClickListener(v -> {
            Fragment newFragment = new MapFragment();
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Button to launch the delivery activity with the current package list
        Button btnGoToDeliveryFragment = view.findViewById(R.id.btn_delivery);
        btnGoToDeliveryFragment.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DeliveryActivity.class);
            ArrayList<Colis> colisList = new ArrayList<>(colisViewModel.getColis().getValue());
            intent.putParcelableArrayListExtra("colis_list", colisList);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        // Button to initiate QR code scan for adding a new package
        ImageView btnScanQR = view.findViewById(R.id.btn_add);
        btnScanQR.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new QrCodeFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Listen for QR scan results and process the scanned package
        getParentFragmentManager().setFragmentResultListener(
                "qr_scan",
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    int qrContent = Integer.parseInt(Objects.requireNonNull(bundle.getString("qr_result")));

                    ColisController controller = new ColisController(requireContext(), BuildConfig.MAPS_API_KEY);
                    controller.traiterColisDepuisQRCode(qrContent, new ColisController.ColisCallback() {
                        @Override
                        public void onColisReady(Colis colis) {
                            colisViewModel.addColis(colis);
                            updateDeliveryStatus(colis, 1);
                            Toast.makeText(getContext(), "Colis ajouté : Id " + colis.getNumber(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(getContext(), "Erreur : " + message, Toast.LENGTH_SHORT).show();
                            Log.d("Maps", "Erreur : " + message);
                        }
                    });

                });

        return view;
    }

    /**
     * Enables swipe-to-delete on the RecyclerView items with an undo Snackbar.
     * Swiping left deletes the item from the ViewModel, but offers an "Undo"
     * action to restore it.
     */
    private void enableSwipeToDeleteWithUndo() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                Colis deletedColis = adapter.getColisAt(position);
                if (deletedColis == null) {
                    adapter.notifyDataSetChanged();
                    return;
                }

                colisViewModel.removeColis(deletedColis);

                deleteColis(deletedColis.getNumber());

                Snackbar.make(recyclerView, "Colis supprimé", Snackbar.LENGTH_LONG)
                        .setAction("Annuler", v -> {
                            colisViewModel.addColis(deletedColis);
                            updateDeliveryStatus(deletedColis, 1);
                        })
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                if (dX >= 0) return;

                View itemView = viewHolder.itemView;

                Paint paint = new Paint();
                paint.setColor(Color.parseColor("#F44336"));
                paint.setAntiAlias(true);

                float cornerRadius = 32f;
                int marginStart = 20;

                RectF background = new RectF(
                        itemView.getRight() + dX + marginStart,
                        itemView.getTop(),
                        itemView.getRight(),
                        itemView.getBottom()
                );

                c.drawRoundRect(background, cornerRadius, cornerRadius, paint);

                Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);
                if (icon != null) {
                    int itemHeight = itemView.getHeight();
                    int desiredSize = (int) (itemHeight * 0.4);
                    int iconSize = Math.min(icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                    int finalSize = Math.min(iconSize, desiredSize);

                    int iconTop = itemView.getTop() + (itemHeight - finalSize) / 2;
                    int iconBottom = iconTop + finalSize;
                    int iconRight = itemView.getRight() - 32;
                    int iconLeft = iconRight - finalSize;

                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    icon.draw(c);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Calls the Amazi API to update the delivery status of a package,
     * then on success removes it and refreshes the route.
     *
     * @param colis  the {@link Colis} whose status is being updated
     * @param status the new status string to send ("LIVRE" or "ABSENT")
     */
    private void updateDeliveryStatus(Colis colis, int status) {
        new AmaziServices(TokenHolder.getToken()).updateDeliveryStatus(DeliveryIdHolder.getDeliveryId(), colis.getNumber(), "mail@mail.mail" /*TODO: Utiliser le vrai ID du livreur*/, status, new AmaziServices.StatutCallback() {
            @Override
            public void onSuccess() { }

            @Override
            public void onError(String message) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Erreur : " + message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void deleteColis(int colisId) {
        new AmaziServices(TokenHolder.getToken()).deleteColis(DeliveryIdHolder.getDeliveryId(), colisId, new AmaziServices.DeleteCallback() {
            @Override
            public void onSuccess() {
                // mettre à jour l’affichage, retirer le colis localement…
            }
            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), "Suppression impossible : " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
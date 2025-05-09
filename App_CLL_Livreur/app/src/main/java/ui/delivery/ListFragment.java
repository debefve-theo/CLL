package ui.delivery;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.app_cll_livreur.R;

import java.util.Arrays;
import java.util.List;

import data.model.Colis;
import ui.ColisViewModel;
import ui.ColisAdapter;
import ui.login.LoginActivity;
import ui.main.MainActivity;

/**
 * Fragment that displays a list of {@link Colis} with filtering options.
 * <p>
 * Provides controls to navigate back to the delivery view, end the session,
 * and filter the list by status (All, Ongoing, Delivered, Absent). Observes
 * the {@link ColisViewModel} for updates to the filtered list.
 * </p>
 */
public class ListFragment extends Fragment {

    /** ViewModel holding and filtering the list of Colis */
    private ColisViewModel colisViewModel;

    /** Adapter for presenting Colis items in the RecyclerView */
    private ColisAdapter adapter;

    private List<TextView> filterButtons;

    /**
     * Default constructor.
     */
    public ListFragment() {}

    /**
     * Called to do initial creation of the fragment.
     *
     * @param savedInstanceState if non-null, this fragment is being re-created
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        colisViewModel = new ViewModelProvider(requireActivity()).get(ColisViewModel.class);
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           LayoutInflater to inflate views
     * @param container          parent view that the fragment's UI should attach to
     * @param savedInstanceState if non-null, this fragment is being re-constructed
     * @return the root {@link View} for this fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // Navigate back to the DeliveryFragment
        ImageView btnGoToHomeFragment = view.findViewById(R.id.btn_back_fraglist);
        btnGoToHomeFragment.setOnClickListener(v -> {
            Fragment newFragment = new DeliveryFragment();
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        Button btnGoToMainActivity = view.findViewById(R.id.btn_end);
        btnGoToMainActivity.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Déconnexion")
                    .setMessage("Êtes-vous sûr de vouloir terminer la session ?\n" +
                            "Si votre tournée n’est pas finie, elle sera perdue.")
                    .setCancelable(false)
                    .setPositiveButton("Oui, déconnecter", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(requireActivity(), LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        }
                    })
                    .setNegativeButton("Annuler", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });

        // Set up RecyclerView and its adapter
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        adapter = new ColisAdapter(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Filter buttons
        TextView filterAll = view.findViewById(R.id.filter_all);
        TextView filterInProgress = view.findViewById(R.id.filter_in_progress);
        TextView filterDelivered = view.findViewById(R.id.filter_delivered);
        TextView filterAbsent = view.findViewById(R.id.filter_not_delivered);

        filterButtons = Arrays.asList(filterAll, filterInProgress, filterDelivered, filterAbsent);

        // Observe filtered list and submit updates to the adapter
        colisViewModel.getFilteredColis().observe(getViewLifecycleOwner(), (List<Colis> filteredList) -> {
            adapter.setColisList(filteredList);
        });

        // Création d’un listener commun
        View.OnClickListener filterClick = v -> {
            // réinitialise tous les backgrounds
            for (TextView tv : filterButtons) {
                tv.setBackgroundResource(0);
            }
            // applique le background au bouton cliqué
            v.setBackgroundResource(R.drawable.cmp_content_selector_selected);

            // applique le filtre correspondant
            if (v == filterAll) {
                colisViewModel.filterColis("All");
            } else if (v == filterInProgress) {
                colisViewModel.filterColis("Ongoing");
            } else if (v == filterDelivered) {
                colisViewModel.filterColis("Delivered");
            } else {
                colisViewModel.filterColis("Absent");
            }
        };

        // attache le listener à chaque bouton
        for (TextView tv : filterButtons) {
            tv.setOnClickListener(filterClick);
        }

        // initialisation par défaut
        filterAll.setBackgroundResource(R.drawable.cmp_content_selector_selected);
        colisViewModel.filterColis("All");

        return view;
    }
}
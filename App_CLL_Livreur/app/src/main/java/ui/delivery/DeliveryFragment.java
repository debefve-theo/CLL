package ui.delivery;

import static utils.MapUtils.addUserMarker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app_cll_livreur.BuildConfig;
import com.example.app_cll_livreur.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import data.api.maps.GoogleMapsServices;
import data.api.amazi.AmaziServices;
import data.model.Colis;
import data.model.DirectionsResponse;
import ui.ColisViewModel;
import ui.DeliveryIdHolder;
import ui.MapViewModel;
import ui.QrCodeFragment;
import ui.login.LoginActivity;
import utils.LocationUtils;
import utils.MapUtils;
import utils.TokenHolder;

/**
 * Fragment that displays the delivery map and handles navigation logic.
 * <p>
 * It observes a list of {@link Colis} from a shared {@link ColisViewModel},
 * requests route calculations from {@link MapViewModel}, and renders the
 * current navigation state on a {@link GoogleMap}. It also supports QR code
 * scanning to confirm package delivery and updates delivery status via the Amazi API.
 * </p>
 */
public class DeliveryFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapViewModel mapViewModel;
    private ColisViewModel colisViewModel;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng userLocation;
    private LatLng startingLocation;
    TextView tvStopsRemaining;
    TextView tvTimeRemaining;
    TextView tvDistanceRemaining;
    TextView tvDistance;
    TextView tvDuration;
    TextView tvAdress;
    TextView tvName;
    int currentWaypointIndex = 0;
    List<Integer> waypointOrder;
    TextView dateTextView;

    /**
     * Default empty constructor.
     */
    public DeliveryFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
    }

    /**
     * Inflates the layout, initializes Views and ViewModels, and sets up
     * listeners for UI controls and fragment results.
     *
     * @param inflater           the LayoutInflater object that can be used to inflate views
     * @param container          the parent view that the fragment's UI should attach to
     * @param savedInstanceState if non-null, this fragment is being re-constructed
     * @return the root View for this fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        colisViewModel = new ViewModelProvider(requireActivity()).get(ColisViewModel.class);

        // Observe package list and update waypoints in the MapViewModel
        colisViewModel.getColis().observe(getViewLifecycleOwner(), colisList -> {
            List<LatLng> waypoints = new ArrayList<>();
            for (Colis colis : colisList) {
                waypoints.add(new LatLng(colis.getLatitude(), colis.getLongitude()));
            }
            mapViewModel.setWaypoints(waypoints);
        });

        // Menu button navigates back to the list of packages
        ImageView btnGoToListFragment = view.findViewById(R.id.btn_menu);
        btnGoToListFragment.setOnClickListener(v -> navigateToListFragment());

        // Scan QR code to confirm a package
        ImageView btnScanQR = view.findViewById(R.id.btn_scan_qr);
        btnScanQR.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new QrCodeFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Start Google Maps navigation to the next waypoint
        Button btnGoToGoogleMap = view.findViewById(R.id.btn_start_guidance);
        btnGoToGoogleMap.setOnClickListener(v -> {
            if (mapViewModel.getWaypoints() != null && waypointOrder != null && !waypointOrder.isEmpty()) {
                lancerNavigationVers(mapViewModel.getWaypoints().get(waypointOrder.get(currentWaypointIndex)));
            } else {
                Toast.makeText(getContext(), "Plus de destination disponible", Toast.LENGTH_SHORT).show();
            }
        });

        // Listen for QR scan results to check the package ID
        getParentFragmentManager().setFragmentResultListener(
                "qr_scan",
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    String qrContent = bundle.getString("qr_result");
                    checkPackageScan(qrContent);
                }
        );

        // Bind UI components for route info and date display
        tvStopsRemaining = view.findViewById(R.id.tv_stops_remaining_value);
        tvTimeRemaining = view.findViewById(R.id.tv_time_remaining_value);
        tvDistanceRemaining = view.findViewById(R.id.tv_distance_remaining_value);
        tvDistance = view.findViewById(R.id.tv_distance);
        tvDuration = view.findViewById(R.id.tv_duration);
        tvAdress = view.findViewById(R.id.tv_address);
        tvName = view.findViewById(R.id.tv_name);
        dateTextView = view.findViewById(R.id.toolbar_date);

        setDate();
        setDefaultValue();

        return view;
    }

    /**
     * Called when the GoogleMap is ready to be used.
     * Obtains the user's current location and starts initial route calculation.
     *
     * @param googleMap the GoogleMap instance
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        getUserLocation();

        if (mapViewModel.getWaypoints() == null || mapViewModel.getWaypoints().isEmpty()) {
            Log.w("CLL-DeliveryFragment", "drawRoute: no waypoints to display.");
        }

        mapViewModel.getRouteLiveData().observe(getViewLifecycleOwner(), this::drawRoute);

        Log.d("CLL-DeliveryFragment", "Google Map is ready.");
    }

    /**
     * Navigates back to the package list fragment.
     */
    private void navigateToListFragment() {
        Fragment newFragment = new ListFragment();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Requests the user's current location, centers the map on it,
     * places a user marker, and triggers route calculation.
     */
    private void getUserLocation() {
        LocationUtils.fetchUserLocation(this, fusedLocationProviderClient,
                latLng -> {
                    userLocation = latLng;

                    if (startingLocation == null) {
                        startingLocation = latLng;
                    }

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                    addUserMarker(mMap, latLng, getResources(), R.drawable.ic_street_view);
                    mapViewModel.fetchDirectionsFromService(userLocation, startingLocation);
                },
                errorMsg -> Log.e("Delivery", errorMsg)
        );
    }

    /**
     * Draws the calculated route on the map and updates internal state.
     *
     * @param response the {@link DirectionsResponse} containing route data
     */
    private void drawRoute(DirectionsResponse response) {
        if (response == null || response.getRoutes() == null || response.getRoutes().isEmpty()) {
            Log.w("CLL-DeliveryFragment", "drawRoute called with an empty response.");
            return;
        }

        waypointOrder = response.getRoutes().get(0).getWaypointOrder();

        MapUtils.drawRouteOnMap(
                mMap,
                response,
                mapViewModel.getWaypoints(),
                getResources(),
                R.drawable.ic_street_view,
                userLocation
        );

        updateRouteInfo(response);
        updateNextRoute();
    }

    /**
     * Aggregates total distance and duration from all route legs
     * and displays them along with the remaining stop count.
     *
     * @param response the {@link DirectionsResponse} containing route legs
     */
    private void updateRouteInfo(DirectionsResponse response) {
        int totalDistance = 0;
        int totalDuration = 0;

        for (DirectionsResponse.Route route : response.getRoutes()) {
            for (DirectionsResponse.Leg leg : route.getLegs()) {
                totalDistance += leg.getDistance().getValue();
                totalDuration += leg.getDuration().getValue();
            }
        }

        tvStopsRemaining.setText(String.valueOf(colisViewModel.getColis().getValue().size()));
        tvDistanceRemaining.setText(String.format("%.1f km", totalDistance / 1000.0));

        int hours = totalDuration / 3600;
        int minutes = (totalDuration % 3600) / 60;
        String durationText = (hours > 0) ? String.format("%dh%02d", hours, minutes) : minutes + " min";

        tvTimeRemaining.setText(durationText);
    }

    /**
     * Updates the UI for the next delivery waypoint, fetching simple
     * distance and duration info via {@link GoogleMapsServices}.
     */
    public void updateNextRoute() {
        if (mapViewModel.getWaypoints() == null || waypointOrder == null || waypointOrder.isEmpty()) return;

        List<Colis> colisList = colisViewModel.getColis().getValue();

        if (colisList == null || colisList.isEmpty() || currentWaypointIndex >= waypointOrder.size()) {
            Log.i("Delivery", "Plus de colis à livrer");
            tvName.setText("Aucun colis");
            tvAdress.setText("Fin de tournée");
            tvDistance.setText("-");
            tvDuration.setText("-");
            tvStopsRemaining.setText("0");
            tvDistanceRemaining.setText("0 km");
            tvTimeRemaining.setText("0 min");
            showEndOfTourDialog();
            return;
        }

        int colisIndex = waypointOrder.get(currentWaypointIndex);

        if (colisIndex < colisList.size()) {
            Colis prochainColis = colisList.get(colisIndex);
            tvName.setText(prochainColis.getName());
            tvAdress.setText(prochainColis.getAdresse());
        } else {
            Log.e("Maps", "Index de colis invalide : " + colisIndex);
            return;
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                LatLng destination = mapViewModel.getWaypoints().get(waypointOrder.get(currentWaypointIndex));

                GoogleMapsServices mapsServices = new GoogleMapsServices(BuildConfig.MAPS_API_KEY);
                mapsServices.fetchSimpleDirection(userLocation, destination, new GoogleMapsServices.SimpleDirectionCallback() {
                    @Override
                    public void onDirectionFetched(int distanceInMeters, int durationInSeconds) {
                        requireActivity().runOnUiThread(() -> {
                            String distanceText = String.format("%.1f km", distanceInMeters / 1000.0);
                            tvDistance.setText(distanceText);

                            int hours = durationInSeconds / 3600;
                            int minutes = (durationInSeconds % 3600) / 60;

                            String durationText = (hours > 0)
                                    ? String.format("%dh%02d", hours, minutes)
                                    : minutes + " min";

                            tvDuration.setText(durationText);
                        });
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("Maps", "Erreur direction simple : " + message);
                    }
                });
            }
        });
    }


    /**
     * Launches Google Maps navigation intent to the specified destination coordinate.
     *
     * @param destination the {@link LatLng} of the navigation target
     */
    public void lancerNavigationVers(LatLng destination) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destination.latitude + "," + destination.longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(getContext(), "Google Maps n'est pas installé", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Validates the scanned QR content against the expected package number
     * and either shows an action dialog or an error toast.
     *
     * @param qrContent the scanned QR code content as a string
     */
    private void checkPackageScan(String qrContent) {
        List<Colis> colisList = colisViewModel.getColis().getValue();
        if (colisList == null || waypointOrder == null || currentWaypointIndex >= waypointOrder.size()) return;

        int indexColis = waypointOrder.get(currentWaypointIndex);
        Colis colisAttendu = colisList.get(indexColis);

        if (qrContent.equals(String.valueOf(colisAttendu.getNumber()))) {
            afficherPopupActionColis(colisAttendu);
        } else {
            Toast.makeText(requireContext(), "Ce n’est pas le bon colis", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows a confirmation dialog allowing the user to mark the package
     * as delivered or absent.
     *
     * @param colis the {@link Colis} being confirmed
     */
    private void afficherPopupActionColis(Colis colis) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmer la livraison")
                .setMessage("Que souhaitez-vous faire pour ce colis ?")
                .setNeutralButton("Client absent", (dialog, which) -> updateDeliveryStatus(colis, 3))
                .setPositiveButton("Colis livré", (dialog, which) -> updateDeliveryStatus(colis, 2))
                .show();
    }

    /**
     * Removes the delivered package from the list, refreshes waypoints and route,
     * and resets the current waypoint index.
     *
     * @param colis the {@link Colis} that was delivered or marked absent
     */
    private void retirerWaypointEtRaffraichir(Colis colis) {
        List<Colis> colisList = new ArrayList<>(colisViewModel.getColis().getValue());
        colisList.removeIf(c -> c.getNumber() == colis.getNumber());
        colisViewModel.setColis(colisList);

        currentWaypointIndex = 0;
        List<LatLng> newWaypoints = new ArrayList<>();
        for (Colis c : colisList) {
            newWaypoints.add(new LatLng(c.getLatitude(), c.getLongitude()));
        }
        mapViewModel.setWaypoints(newWaypoints);
        mapViewModel.fetchDirectionsFromService(userLocation, startingLocation);

        updateNextRoute();
    }

    /**
     * Calls the Amazi API to update the delivery status of a package,
     * then on success removes it and refreshes the route.
     *
     * @param colis  the {@link Colis} whose status is being updated
     * @param statut the new status string to send ("LIVRE" or "ABSENT")
     */
    private void updateDeliveryStatus(Colis colis, int statut) {
        new AmaziServices(TokenHolder.getToken()).updateDeliveryStatus(DeliveryIdHolder.getDeliveryId(), colis.getNumber(), "mail@mail.mail" /*TODO: Utiliser le vrai ID du livreur*/, statut, new AmaziServices.StatutCallback() {
            @Override
            public void onSuccess() {
                requireActivity().runOnUiThread(() -> retirerWaypointEtRaffraichir(colis));
            }

            @Override
            public void onError(String message) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Erreur : " + message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * Formats and displays the current date in the toolbar.
     * Uses the device locale and capitalizes the first letter.
     */
    private void setDate() {
        Locale currentLocale;
        currentLocale = getResources().getConfiguration().locale;


        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d MMMM", currentLocale);
        String formattedDate = dateFormat.format(new Date());

        // Maj pour la première lettre
        formattedDate = formattedDate.substring(0, 1).toUpperCase(currentLocale) + formattedDate.substring(1);

        dateTextView.setText(formattedDate);
    }

    private void showEndOfTourDialog() {
        // Utilise le contexte de l'Activity hôte pour construire la boîte de dialogue
        new AlertDialog.Builder(requireContext())
                .setTitle("Fin de tournée")
                .setMessage("Votre tournée est terminée.\n")
                .setPositiveButton("Déconnexion", (dialog, which) -> {
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Annuler", (dialog, which) -> {
                    // On ferme simplement la boîte de dialogue
                    dialog.dismiss();
                })
                .show();
    }

    private void setDefaultValue() {
        tvName.setText("Aucun colis");
        tvAdress.setText("Fin de tournée");
        tvDistance.setText("-");
        tvDuration.setText("-");
        tvStopsRemaining.setText("0");
        tvDistanceRemaining.setText("0 km");
        tvTimeRemaining.setText("0 min");
    }
}
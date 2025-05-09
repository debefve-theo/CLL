package ui.main;

import static utils.MapUtils.addUserMarker;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.app_cll_livreur.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import data.model.DirectionsResponse;
import data.model.Colis;
import ui.ColisViewModel;
import ui.MapViewModel;
import utils.LocationUtils;
import utils.MapUtils;

/**
 * Fragment that displays a Google Map showing the user's location and the delivery route.
 * <p>
 * Observes the list of {@link Colis} from {@link ColisViewModel}, translates them into map waypoints,
 * requests route directions via {@link MapViewModel}, and renders the route on the map.
 * Also displays the current parcel count and allows navigation back to the home screen.
 * </p>
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private MapViewModel mapViewModel;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng userLocation;
    TextView tvParcelCount;

    /** Default empty constructor required for fragment instantiation. */
    public MapFragment() {}

    /**
     * Called to do initial creation of the fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
    }

    /**
     * Inflates the fragment layout, initializes UI components, and sets up
     * observation of the parcel list to update waypoints and parcel count.
     *
     * @param inflater           LayoutInflater to inflate views
     * @param container          parent view that the fragment's UI should attach to
     * @param savedInstanceState if non-null, this fragment is being re-created
     * @return the root {@link View} for this fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_map);

        tvParcelCount = view.findViewById(R.id.toolbar_parcel_count);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ColisViewModel colisViewModel = new ViewModelProvider(requireActivity()).get(ColisViewModel.class);

        observeColisList(colisViewModel);

        ImageView btnGoToHomeFragment = view.findViewById(R.id.btn_back_home);
        btnGoToHomeFragment.setOnClickListener(v -> navigateToHomeFragment());

        return view;
    }

    /**
     * Called when the GoogleMap is ready to be used.
     * <p>
     * Requests the user's current location, observes route updates,
     * and logs map readiness.
     * </p>
     *
     * @param googleMap the {@link GoogleMap} instance
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        getUserLocation();

        if (mapViewModel.getWaypoints() == null || mapViewModel.getWaypoints().isEmpty()) {
            Log.w("CLL-MapFragment", "drawRoute: no waypoints to display.");
        }

        mapViewModel.getRouteLiveData().observe(getViewLifecycleOwner(), this::drawRoute);

        Log.d("CLL-MapFragment", "Google Map is ready.");
    }

    /**
     * Observes changes to the parcel list in the given {@link ColisViewModel},
     * updates map waypoints, clears any existing route, and updates the display.
     *
     * @param colisViewModel the {@link ColisViewModel} providing the parcel list
     */
    private void observeColisList(ColisViewModel colisViewModel) {
        colisViewModel.getColis().observe(getViewLifecycleOwner(), colisList -> {
            Log.d("CLL-MapFragment", "Colis list updated with " + colisList.size() + " items.");

            List<LatLng> waypoints = new ArrayList<>();
            for (Colis colis : colisList) {
                waypoints.add(new LatLng(colis.getLatitude(), colis.getLongitude()));
            }
            mapViewModel.setWaypoints(waypoints);
            mapViewModel.clearRoute();

            if (mMap != null) {
                getUserLocation();
            }

            int parcelNumber = colisList.size();
            String formatted = getString(R.string.parcel_count, parcelNumber);
            tvParcelCount.setText(formatted);
        });
    }

    /**
     * Replaces this fragment with the {@link HomeFragment}.
     */
    private void navigateToHomeFragment() {
        Fragment newFragment = new HomeFragment();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    /**
     * Retrieves the user's current location, centers the map on it,
     * adds a user marker, and requests initial route calculation.
     */
    private void getUserLocation() {
        LocationUtils.fetchUserLocation(this, fusedLocationProviderClient,
                latLng -> {
                    userLocation = latLng;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));
                    addUserMarker(mMap, userLocation, getResources(), R.drawable.ic_street_view);
                    mapViewModel.fetchDirectionsFromService(userLocation, userLocation);
                },
                errorMsg -> Log.e("CLL-MapFragment", errorMsg)
        );
    }

    /**
     * Draws the route on the map using the provided {@link DirectionsResponse}.
     * Logs if no routes are available or confirms the number of routes drawn.
     *
     * @param response the {@link DirectionsResponse} containing route data
     */
    private void drawRoute(DirectionsResponse response) {
        MapUtils.drawRouteOnMap(
                mMap,
                response,
                mapViewModel.getWaypoints(),
                getResources(),
                R.drawable.ic_street_view,
                userLocation
        );

        if (response == null || response.getRoutes().isEmpty()) {
            Log.w("CLL-MapFragment", "No routes received to draw.");
        } else {
            Log.d("CLL-MapFragment", "Drawing route with " + response.getRoutes().size() + " route(s).");
        }

    }
}
package ui;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.app_cll_livreur.BuildConfig;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import data.api.maps.GoogleMapsServices;
import data.model.DirectionsResponse;


/**
 * ViewModel responsible for managing map routing data.
 * <p>
 * Holds the list of waypoints for the route and exposes LiveData
 * for observing direction responses fetched from the Google Maps API.
 * </p>
 */
public class MapViewModel extends ViewModel {
    private final MutableLiveData<DirectionsResponse> routeLiveData = new MutableLiveData<>();
    private List<LatLng> waypoints = new ArrayList<>();

    /**
     * Returns a LiveData stream of the latest {@link DirectionsResponse}.
     * <p>
     * Observers are notified whenever a new set of directions is fetched
     * or when the route is cleared.
     * </p>
     *
     * @return LiveData containing the current {@link DirectionsResponse}, or null if none
     */
    public LiveData<DirectionsResponse> getRouteLiveData() {
        return routeLiveData;
    }

    /**
     * Returns the current list of waypoints used for routing.
     *
     * @return a {@link List} of {@link LatLng} representing intermediate stops
     */
    public List<LatLng> getWaypoints() {
        return waypoints;
    }

    /**
     * Updates the list of waypoints for the next routing request.
     *
     * @param waypoints a {@link List} of {@link LatLng} to use as route waypoints
     */
    public void setWaypoints(List<LatLng> waypoints) {
        this.waypoints = waypoints;
    }

    /**
     * Initiates an asynchronous request to fetch directions from the Google Maps API.
     * <p>
     * Uses the {@link GoogleMapsServices} to fetch a route starting from
     * {@code startingLocation}, passing through any set {@code waypoints}, and ending at {@code userLocation}.
     * On success, posts the {@link DirectionsResponse} to {@link #routeLiveData}.
     * On failure, logs an error message.
     * </p>
     *
     * @param userLocation     the final destination {@link LatLng} (typically user's current location)
     * @param startingLocation the origin {@link LatLng} for the route
     */
    public void fetchDirectionsFromService(LatLng userLocation, LatLng startingLocation) {
        GoogleMapsServices service = new GoogleMapsServices(BuildConfig.MAPS_API_KEY);

        service.fetchDirections(userLocation, startingLocation, waypoints, new GoogleMapsServices.DirectionsCallback() {
            @Override
            public void onDirectionsFetched(DirectionsResponse response) {
                routeLiveData.postValue(response);
            }

            @Override
            public void onError(String message) {
                Log.e("MapViewModel", "Error in fetchDirections : " + message);
            }
        });
    }

    /**
     * Clears the current route by setting the LiveData value to null.
     * <p>
     * Observers of {@link #getRouteLiveData()} will receive a null value.
     * </p>
     */
    public void clearRoute() {
        routeLiveData.setValue(null);
    }
}
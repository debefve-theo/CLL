package data.api.maps;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.app_cll_livreur.BuildConfig;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import data.model.DirectionsResponse;
import data.model.GeocodingResponse;
import data.model.SimpleDirectionResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * A service class to interact with the Google Maps API using Retrofit.
 * Provides utilities to fetch directions, coordinates, and basic distance/duration info.
 */
public class GoogleMapsServices {
    private static final String BASE_URL = "https://maps.googleapis.com/";
    private final IGoogleMapsAPI apiService;
    private final String apiKey;

    /**
     * Constructs a new GoogleMapsServices instance with the API key.
     *
     * @param apiKey    Google Maps API key (in local.properties file).
     */
    public GoogleMapsServices(String apiKey) {
        this.apiKey =  BuildConfig.MAPS_API_KEY;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.apiService = retrofit.create(IGoogleMapsAPI.class);
    }

    /**
     * Fetches optimized directions from the user's current location through a list of waypoints.
     *
     * @param userLocation          The origin and destination location.
     * @param destinationLocation   The destination location.
     * @param waypoints             The list of intermediate waypoints.
     * @param callback              Callback to handle the result or error.
     */
    public void fetchDirections(LatLng userLocation, LatLng destinationLocation, List<LatLng> waypoints, DirectionsCallback callback) {
        if (userLocation == null) {
            Log.e("Maps", "Position utilisateur non disponible");
            callback.onError("Position utilisateur indisponible.");
            return;
        }

        String origin = userLocation.latitude + "," + userLocation.longitude;
        String destination = destinationLocation.latitude + "," + destinationLocation.longitude;
        String waypointsStr = "optimize:true|" + formatWaypoints(waypoints);

        Call<DirectionsResponse> call = apiService.getDirections(origin, destination, waypointsStr, "driving", apiKey);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                if (response.body() != null && !response.body().getRoutes().isEmpty()) {
                    DirectionsResponse.Route route = response.body().getRoutes().get(0);
                    //DirectionsResponse.Leg leg = route.getLegs().get(0);
                    callback.onDirectionsFetched(response.body());
                } else {
                    Log.e("Maps", "Réponse API Directions vide ou incorrecte !");
                    callback.onError("Réponse API Directions vide ou incorrecte !");
                }
            }

            @Override
            public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable t) {
                Log.e("Maps", "Erreur API Directions : " + t.getMessage());
            }
        });
    }

    public interface DirectionsCallback {
        void onDirectionsFetched(DirectionsResponse response);
        void onError(String message);
    }

    /**
     * Fetches coordinates for a given address using Google Geocoding API.
     *
     * @param address   Textual address.
     * @param callback  Callback to handle coordinates or error.
     */
    public void fetchCoordinates(String address, CoordinateCallback callback) {
        if (address == null || address.isEmpty()) {
            Log.e("Class [GoogleMapsServices] : ", "Adresse invalide");
            callback.onError("Adresse invalide");
            return;
        }

        Call<GeocodingResponse> call = apiService.getCoordinates(address, apiKey);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GeocodingResponse> call, @NonNull Response<GeocodingResponse> response) {
                if (response.body() != null && !response.body().getResults().isEmpty()) {
                    double lat = response.body().getResults().get(0).getGeometry().getLocation().getLat();
                    double lng = response.body().getResults().get(0).getGeometry().getLocation().getLng();
                    Log.d("Class [GoogleMapsServices] : ", "Latitude: " + lat + ", Longitude: " + lng);
                    callback.onCoordinatesReceived(lat, lng);
                } else {
                    Log.e("Class [GoogleMapsServices] : ", "Réponse API Geocoding vide ou incorrecte !");
                    callback.onError("Réponse API vide");
                }
            }

            @Override
            public void onFailure(@NonNull Call<GeocodingResponse> call, @NonNull Throwable t) {
                Log.e("Class [GoogleMapsServices] : ", "Erreur API Geocoding : " + t.getMessage());
                callback.onError("Erreur API : " + t.getMessage());
            }
        });
    }

    public interface CoordinateCallback {
        void onCoordinatesReceived(double lat, double lng);
        void onError(String message);
    }

    /**
     * Fetches basic direction info (distance and duration) between two points.
     *
     * @param userLocation        The origin location.
     * @param destinationLocation The destination location.
     * @param callback            Callback to handle results or error.
     */
    public void fetchSimpleDirection(LatLng userLocation, LatLng destinationLocation, SimpleDirectionCallback callback) {
        if (userLocation == null || destinationLocation == null) {
            callback.onError("Position invalide");
            return;
        }

        String origin = userLocation.latitude + "," + userLocation.longitude;
        String destination = destinationLocation.latitude + "," + destinationLocation.longitude;
        String apiKey = BuildConfig.MAPS_API_KEY;

        Call<SimpleDirectionResponse> call = apiService.getSimpleDirection(origin, destination, "driving", apiKey);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SimpleDirectionResponse> call, @NonNull Response<SimpleDirectionResponse> response) {
                if (response.body() != null && response.body().getRoutes() != null && !response.body().getRoutes().isEmpty()) {
                    SimpleDirectionResponse.Route route = response.body().getRoutes().get(0);
                    SimpleDirectionResponse.Leg leg = route.getLegs().get(0);
                    callback.onDirectionFetched(leg.getDistance().getValue(), leg.getDuration().getValue());
                } else {
                    callback.onError("Réponse API Directions vide");
                }
            }

            @Override
            public void onFailure(@NonNull Call<SimpleDirectionResponse> call, @NonNull Throwable t) {
                callback.onError("Erreur API : " + t.getMessage());
            }
        });
    }

    public interface SimpleDirectionCallback {
        void onDirectionFetched(int distanceInMeters, int durationInSeconds);
        void onError(String message);
    }

    /**
     * Formats a list of LatLng into a URL-encoded string for waypoints.
     *
     * @param waypoints List of coordinates to format.
     * @return A string formatted as "lat,lng|lat,lng|..."
     */
    private String formatWaypoints(List<LatLng> waypoints) {
        StringBuilder waypointsStr = new StringBuilder();
        for (LatLng point : waypoints) {
            waypointsStr.append(point.latitude).append(",").append(point.longitude).append("|");
        }
        if (waypointsStr.length() > 0) {
            waypointsStr.setLength(waypointsStr.length() - 1);
        }
        return waypointsStr.toString();
    }
}
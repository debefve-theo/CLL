package data.api.maps;

import data.model.DirectionsResponse;
import data.model.GeocodingResponse;
import data.model.SimpleDirectionResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit interface for interacting with the Google Maps API.
 * Provides methods to fetch directions and geolocation data.
 */
public interface IGoogleMapsAPI {

    /**
     * Return a full route between an origin and a destination, with waypoints.
     *
     * @param origin      The starting point (e.g., "Rue peetermans 1, Seraing").
     * @param destination The destination point (e.g., "Rue peetermans 1, Seraing").
     * @param waypoints   Optional intermediate waypoints, separated by '|'. Can be null.
     * @param mode        Mode of transportation (e.g., "driving", "walking", "bicycling").
     * @param apiKey      Google Maps API key.
     * @return A Retrofit Call object returning a {@link DirectionsResponse} containing detailed route information.
     */
    @GET("maps/api/directions/json")
    Call<DirectionsResponse> getDirections(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("waypoints") String waypoints,
            @Query("mode") String mode,
            @Query("key") String apiKey
    );

    /**
     * Return a simplified route between an origin and a destination.
     *
     * @param origin        The starting point.
     * @param destination   The destination point.
     * @param mode          Mode of transportation.
     * @param apiKey        Google Maps API key.
     * @return A Retrofit Call object returning a {@link SimpleDirectionResponse} with basic route information.
     */
    @GET("maps/api/directions/json")
    Call<SimpleDirectionResponse> getSimpleDirection(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("mode") String mode,
            @Query("key") String apiKey
    );

    /**
     * Return geographical coordinates (latitude and longitude) for a given address.
     *
     * @param address   The address to geocode (e.g., "Rue peetermans 1, Seraing Belgique").
     * @param apiKey    Google Maps API key.
     * @return A Retrofit Call object returning a {@link GeocodingResponse} containing geolocation data.
     */
    @GET("maps/api/geocode/json")
    Call<GeocodingResponse> getCoordinates(
            @Query("address") String address,
            @Query("key") String apiKey
    );
}
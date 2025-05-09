package data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;


/**
 * Represents the response from the Google Maps Geocoding API,
 */
public class GeocodingResponse {
    @SerializedName("results")
    private List<Result> results;

    /**
     * Returns the list of geocoding results.
     *
     * @return a {@link List} of {@link Result} objects, each representing a possible match
     */
    public List<Result> getResults() {
        return results;
    }

    /**
     * Represents a single geocoding result entry,
     * including location geometry data.
     */
    public static class Result {
        @SerializedName("geometry")
        private Geometry geometry;

        /**
         * Returns the geometry information for this result.
         *
         * @return a {@link Geometry} object containing location details
         */
        public Geometry getGeometry() {
            return geometry;
        }
    }

    /**
     * Encapsulates the geometry details of a geocoding result,
     * including the precise latitude and longitude.
     */
    public static class Geometry {
        @SerializedName("location")
        private Location location;

        /**
         * Returns the geographic coordinates for this geometry.
         *
         * @return a {@link Location} object with latitude and longitude
         */
        public Location getLocation() {
            return location;
        }
    }

    /**
     * Represents a latitude/longitude pair for a geocoded location.
     */
    public static class Location {
        @SerializedName("lat")
        private double lat;

        @SerializedName("lng")
        private double lng;

        /**
         * Returns the latitude value of the location.
         *
         * @return the latitude in decimal degrees
         */
        public double getLat() {
            return lat;
        }


        /**
         * Returns the longitude value of the location.
         *
         * @return the longitude in decimal degrees
         */
        public double getLng() {
            return lng;
        }
    }
}
package data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Represents a simplified response from the Google Maps Directions API,
 * containing routes with only distance and duration information.
 */
public class SimpleDirectionResponse {

    @SerializedName("routes")
    private List<Route> routes;

    /**
     * Returns the list of routes in this response.
     *
     * @return a {@link List} of {@link Route} objects
     */
    public List<Route> getRoutes() {
        return routes;
    }

    /**
     * Represents a single route in the simple directions response.
     * A route consists of one or more legs.
     */
    public static class Route {
        @SerializedName("legs")
        private List<Leg> legs;

        /**
         * Returns the legs that make up this route.
         *
         * @return a {@link List} of {@link Leg} objects
         */
        public List<Leg> getLegs() {
            return legs;
        }
    }

    /**
     * Represents a single leg of a route, containing aggregate distance and duration.
     */
    public static class Leg {
        @SerializedName("distance")
        private Distance distance;

        @SerializedName("duration")
        private Duration duration;

        /**
         * Returns the total distance for this leg.
         *
         * @return a {@link Distance} object with text and numeric value
         */
        public Distance getDistance() {
            return distance;
        }

        /**
         * Returns the total duration for this leg.
         *
         * @return a {@link Duration} object with text and numeric value
         */
        public Duration getDuration() {
            return duration;
        }
    }

    /**
     * Contains human-readable and numeric distance information.
     * The numeric value is expressed in meters.
     */
    public static class Distance {
        @SerializedName("text")
        private String text;

        @SerializedName("value")
        private int value;

        /**
         * Returns the distance value in meters.
         *
         * @return the numeric distance in meters
         */
        public int getValue() {
            return value;
        }
    }


    /**
     * Contains duration value in seconds.
     */
    public static class Duration {
        @SerializedName("text")
        private String text;

        @SerializedName("value")
        private int value;

        /**
         * Returns the duration value in seconds.
         *
         * @return the numeric duration in seconds
         */
        public int getValue() {
            return value;
        }
    }
}
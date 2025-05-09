package data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents the response from the Google Maps Directions API,
 * containing one or more possible routes from origin to destination.
 */
public class DirectionsResponse {
    @SerializedName("routes")
    private List<Route> routes;

    /**
     * Returns the list of routes available for the given directions request.
     *
     * @return a {@link List} of {@link Route} objects
     */
    public List<Route> getRoutes() {
        return routes;
    }

    /**
     * Represents a single route in the directions response.
     * A route consists of one or more legs and an optional waypoint order.
     */
    public static class Route {
        @SerializedName("legs")
        private List<Leg> legs;

        @SerializedName("waypoint_order")
        private List<Integer> waypointOrder;

        /**
         * Returns the list of legs that make up this route.
         *
         * @return a {@link List} of {@link Leg} objects
         */
        public List<Leg> getLegs() {
            return legs;
        }


        /**
         * Returns the order in which waypoints should be visited for this route.
         * Each integer is an index into the original waypoint list.
         *
         * @return a {@link List} of waypoint indices
         */
        public List<Integer> getWaypointOrder() {
            return waypointOrder;
        }
    }

    /**
     * Represents a single leg of a route, corresponding to the section between two waypoints.
     * Contains detailed steps, total distance, and estimated duration.
     */
    public static class Leg {
        @SerializedName("steps")
        private List<Step> steps;

        @SerializedName("distance")
        private Distance distance;

        @SerializedName("duration")
        private Duration duration;

        /**
         * Returns the list of individual navigation steps in this leg.
         *
         * @return a {@link List} of {@link Step} objects
         */
        public List<Step> getSteps() {
            return steps;
        }

        /**
         * Returns the total distance of this leg.
         *
         * @return a {@link Distance} object with text and numeric value
         */
        public Distance getDistance() { return distance; }

        /**
         * Returns the total estimated travel time of this leg.
         *
         * @return a {@link Duration} object with text and numeric value
         */
        public Duration getDuration() { return duration; }
    }

    /**
     * Represents a single navigation step within a leg.
     * Each step contains an encoded polyline for the path segment.
     */
    public static class Step {
        @SerializedName("polyline")
        private Polyline polyline;

        /**
         * Returns the encoded polyline for this step.
         *
         * @return a {@link Polyline} object containing encoded points
         */
        public Polyline getPolyline() {
            return polyline;
        }
    }

    /**
     * Contains distance information in metters.
     */
    public static class Distance {
        @SerializedName("text")
        private String text;

        @SerializedName("value")
        private int value; // en mètres

        /**
         * Returns the distance value in meters.
         *
         * @return the distance value in meters
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * Contains duration information in seconds.
     */
    public static class Duration {
        @SerializedName("text")
        private String text;

        @SerializedName("value")
        private int value;

        /**
         * Returns the duration value in seconds.
         *
         * @return the duration value in seconds
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * Represents the encoded polyline for a step, containing a single string of points.
     */
    public static class Polyline {
        @SerializedName("points")
        private String points;

        /**
         * Returns the encoded polyline string for reconstructing the path.
         *
         * @return the encoded points string
         */
        public String getPoints() {
            return points;
        }
    }
}
package utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

import data.model.DirectionsResponse;

/**
 * Utility class providing helper methods to draw markers, routes, and waypoints
 * on a {@link GoogleMap}, as well as create and resize custom map icons.
 */
public class MapUtils {

    /**
     * Creates a numbered pin marker BitmapDescriptor, with a colored circle
     * and a stem pointing downward.
     *
     * @param number the number to display on the pin
     * @return a {@link BitmapDescriptor} representing the custom pin marker
     */
    public static BitmapDescriptor createPinMarker(int number) {
        int width = 100;
        int height = 200;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float tigeLargeur = 12;
        float tigeHauteur = 60;
        float pointeHauteur = 20;
        float tigeX = width / 2f - (tigeLargeur / 2);
        float tigeYTop = height - tigeHauteur - pointeHauteur;
        float tigeYBottom = height - pointeHauteur;

        Paint pinPaint = new Paint();
        pinPaint.setColor(Color.GRAY);
        pinPaint.setStyle(Paint.Style.FILL);
        pinPaint.setAntiAlias(true);
        canvas.drawRect(tigeX, tigeYTop, tigeX + tigeLargeur, tigeYBottom, pinPaint);

        Path pointePath = new Path();
        pointePath.moveTo(width / 2f, height);
        pointePath.lineTo(tigeX, tigeYBottom);
        pointePath.lineTo(tigeX + tigeLargeur, tigeYBottom);
        pointePath.close();
        canvas.drawPath(pointePath, pinPaint);

        Paint circlePaint = new Paint();
        circlePaint.setColor(Color.RED);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setAntiAlias(true);

        canvas.drawCircle(width / 2f, tigeYTop, width / 3f, circlePaint);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);

        float x = width / 2f;
        float y = tigeYTop - ((textPaint.ascent() + textPaint.descent()) / 2);
        canvas.drawText(String.valueOf(number), x, y, textPaint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Resizes a drawable resource into a {@link BitmapDescriptor} of the specified dimensions.
     *
     * @param res         the {@link Resources} to load the drawable from
     * @param drawableRes the resource ID of the drawable to resize
     * @param width       the target width in pixels
     * @param height      the target height in pixels
     * @return a {@link BitmapDescriptor} representing the resized icon
     */
    public static BitmapDescriptor resizeIcon(Resources res, int drawableRes, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(res, drawableRes);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }

    /**
     * Adds a user location marker to the map with a custom icon.
     *
     * @param map         the {@link GoogleMap} on which to place the marker
     * @param userLocation the {@link LatLng} coordinates of the user's location
     * @param res         the {@link Resources} to load the drawable from
     * @param drawableRes the resource ID of the icon drawable
     */
    public static void addUserMarker(GoogleMap map, LatLng userLocation, Resources res, int drawableRes) {
        map.addMarker(new MarkerOptions()
                .position(userLocation)
                .title("Votre position")
                .icon(resizeIcon(res, drawableRes, 100, 100))
        );
    }

    /**
     * Draws numbered waypoint markers on the map according to the given order.
     *
     * @param map           the {@link GoogleMap} on which to place the waypoints
     * @param waypoints     a {@link List} of {@link LatLng} representing all waypoint coordinates
     * @param waypointOrder a {@link List} of indices specifying the visit order of waypoints
     */
    public static void drawWaypoints(GoogleMap map, List<LatLng> waypoints, List<Integer> waypointOrder) {
        if (waypoints == null || waypointOrder == null) return;

        for (int i = 0; i < waypointOrder.size(); i++) {
            int waypointIndex = waypointOrder.get(i);
            LatLng waypoint = waypoints.get(waypointIndex);
            int number = i + 1;

            map.addMarker(new MarkerOptions()
                    .position(waypoint)
                    .title("Arrêt " + number)
                    .icon(createPinMarker(number))
            );
        }
    }

    /**
     * Clears the map and draws the route polyline, waypoints, and user marker.
     *
     * @param map            the {@link GoogleMap} on which to draw
     * @param response       the {@link DirectionsResponse} containing route data
     * @param waypoints      a {@link List} of {@link LatLng} for intermediate stops
     * @param res            the {@link Resources} to load drawables
     * @param userIconRes    the resource ID of the user icon drawable
     * @param userLocation   the {@link LatLng} of the user's location
     */
    public static void drawRouteOnMap(
            GoogleMap map,
            DirectionsResponse response,
            List<LatLng> waypoints,
            Resources res,
            int userIconRes,
            LatLng userLocation
    ) {
        if (response == null || response.getRoutes() == null || response.getRoutes().isEmpty()) {
            Log.w("MapUtils", "drawRouteOnMap : réponse vide ou nulle");
            return;
        }

        map.clear();

        List<LatLng> points = new ArrayList<>();
        List<Integer> waypointOrder = response.getRoutes().get(0).getWaypointOrder();

        for (DirectionsResponse.Route route : response.getRoutes()) {
            for (DirectionsResponse.Leg leg : route.getLegs()) {
                for (DirectionsResponse.Step step : leg.getSteps()) {
                    points.addAll(PolyUtil.decode(step.getPolyline().getPoints()));
                }
            }
        }

        if (!points.isEmpty()) {
            map.addPolyline(new PolylineOptions()
                    .addAll(points)
                    .width(10)
                    .color(Color.BLUE));
        } else {
            Log.e("MapUtils", "drawRouteOnMap : aucun point pour tracer la route !");
        }

        drawWaypoints(map, waypoints, waypointOrder);

        if (userLocation != null) {
            addUserMarker(map, userLocation, res, userIconRes);
        }
    }

}
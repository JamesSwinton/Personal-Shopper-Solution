package com.ses.zebra.pssdemo_2019.Utilities;

import android.graphics.PointF;
import android.util.Log;
import com.philips.indoormaps.map.AnchorPoint;
import com.philips.indoormaps.map.CoordinateConverter;
import com.philips.indoormaps.map.Location;
import com.ses.zebra.pssdemo_2019.POJOs.Geofencing.CenterPoint;
import com.ses.zebra.pssdemo_2019.POJOs.Geofencing.GeofenceData;
import com.ses.zebra.pssdemo_2019.POJOs.Geofencing.VertexPoint;
import java.util.ArrayList;
import java.util.List;

public class GeofenceHelper {

  // Debugging
  private static final String TAG = "GeofenceHelper";

  // Constants
  private static final int r = 6371; // Earth Radius (~KM)

  // Static Variables
  private static double mRegionSize = 4;
  private static double mAngle = 102;

  // Non-Static Variables

  public GeofenceHelper() { }

  public static GeofenceData createCircularGeofence(double centerLatitude, double centerLongitude, double radiusKilometers) {
    // Convert Radius
    double radiusMeters = radiusKilometers / 1000;

    // Convert Lat / Long to X / Y
    AnchorPoint centerAnchorPoint = new AnchorPoint(0, 0, centerLongitude, centerLatitude);
    AnchorPoint topAnchorPoint = new AnchorPoint(0, 1, centerLongitude, getTopLatitude(centerLatitude, radiusMeters));

    //
    CoordinateConverter coordinateConverter = new CoordinateConverter(centerAnchorPoint, topAnchorPoint);

    // Create Values
    List<Location> locations = new ArrayList<>();
    for (int i = 0; i < 360; i++) {

      PointF point = coordinateConverter.pointFromLongitudeAndLatitude(centerLongitude, centerLatitude);

      double x = point.x + (radiusMeters * Math.cos(i));
      double y = point.y + (radiusMeters * Math.sin(i));

      locations.add(new Location(x, y));
    }

    // Create Vertex Point
    List<VertexPoint> vertexPoints = new ArrayList<>();
    for (Location xyLocation : locations) {
      Location latLngLocation = coordinateConverter.locationFromXY(xyLocation.getLongitude(), xyLocation.getLatitude());

      VertexPoint vertexPoint = new VertexPoint();
      vertexPoint.setLongitude(latLngLocation.getLongitude());
      vertexPoint.setLatitude(latLngLocation.getLatitude());
      vertexPoints.add(vertexPoint);
    }

    // Set Geofence Data
    GeofenceData geofenceData = new GeofenceData();
    geofenceData.setCenterPoint(new CenterPoint(centerLatitude, centerLongitude));
    geofenceData.setVertexPoints(vertexPoints);
    geofenceData.setRegionSizeFeetSquared(radiusMeters);
    geofenceData.setFloor(0);

    // Print Values
    for (VertexPoint vertexPoint : vertexPoints) {
      Log.i(TAG, vertexPoint.getLongitude() + " " + vertexPoint.getLatitude() + " " + "circle1");
    }

    return geofenceData;
  }

  private static double getTopLatitude(double latitude, double radiusMeters) {
    return latitude  + (radiusMeters / r) * (180 / Math.PI);
  }

  public static GeofenceData getGeofenceData(double latitude, double longitude, double regionSize, double angle, int floor) {
    //
    createCircularGeofence(latitude, longitude, regionSize / 1000);

    // Set Region Size & Angle Holder Variables
    mRegionSize = regionSize; mAngle = angle;

    // Create Vertex & Geofence Holder
    GeofenceData geofenceData = new GeofenceData();
    List<VertexPoint> vertexPoints = new ArrayList<>();

    // Create Vertex Points
    VertexPoint p0 = new VertexPoint();
    VertexPoint p1 = new VertexPoint();
    VertexPoint p2 = new VertexPoint();
    VertexPoint p3 = new VertexPoint();

    // Set Vertex Values
    p0.setLongitude(convertToLatitude(latitude, getArbitraryY()));
    p0.setLatitude(convertToLongitude(getArbitraryX(), latitude, longitude));

    p1.setLongitude(convertToLatitude(latitude, getArbitraryX()));
    p1.setLatitude(convertToLongitude(-getArbitraryY(), latitude, longitude));

    p2.setLongitude(convertToLatitude(latitude, -getArbitraryY()));
    p2.setLatitude(convertToLongitude(-getArbitraryX(), latitude, longitude));

    p3.setLongitude(convertToLatitude(latitude, -getArbitraryX()));
    p3.setLatitude(convertToLongitude(getArbitraryY(), latitude, longitude));

    // Add Vertex's to List
    vertexPoints.add(p0);
    vertexPoints.add(p1);
    vertexPoints.add(p2);
    vertexPoints.add(p3);

    // Set Geofence Data
    geofenceData.setCenterPoint(new CenterPoint(latitude, longitude));
    geofenceData.setVertexPoints(vertexPoints);
    geofenceData.setRegionSizeFeetSquared(regionSize);
    geofenceData.setFloor(floor);

    return geofenceData;
  }

  private static double getArbitraryX() {
    return 0.5 * Math.sqrt(2.0) * mRegionSize * Math.cos(mAngle + 0.25 + Math.PI);
  }

  private static double getArbitraryY() {
    return 0.5 * Math.sqrt(2.0) * mRegionSize * Math.sin(mAngle + 0.25 + Math.PI);
  }

  private static double convertToLatitude(double latitude, double dy) {
    return latitude + degToRad(dy / r);
  }

  private static double convertToLongitude(double dx, double latitude, double longitude) {
    return longitude + degToRad(dx / r / Math.cos(latitude));
  }

  private static double degToRad(double degrees) {
    return (degrees * Math.PI) / 180;
  }

}

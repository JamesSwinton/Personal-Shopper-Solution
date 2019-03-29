package com.ses.zebra.pssdemo_2019.Utilities;

import android.util.Log;
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

  public static GeofenceData getGeofenceData(double latitude, double longitude, double regionSize, double angle, int floor) {
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

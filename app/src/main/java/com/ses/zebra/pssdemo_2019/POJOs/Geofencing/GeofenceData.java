package com.ses.zebra.pssdemo_2019.POJOs.Geofencing;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class GeofenceData implements Serializable {

  @SerializedName("center_point")
  @Expose
  private CenterPoint centerPoint;
  @SerializedName("vertex_points")
  @Expose
  private List<VertexPoint> vertexPoints = null;
  @SerializedName("floor_level")
  @Expose
  private int floor;
  @SerializedName("region_size_feet_squared")
  @Expose
  private double regionSizeFeetSquared;

  public CenterPoint getCenterPoint() {
    return centerPoint;
  }

  public void setCenterPoint(CenterPoint centerPoint) {
    this.centerPoint = centerPoint;
  }

  public List<VertexPoint> getVertexPoints() {
    return vertexPoints;
  }

  public void setVertexPoints(List<VertexPoint> vertexPoints) {
    this.vertexPoints = vertexPoints;
  }

  public double getRegionSizeFeetSquared() {
    return regionSizeFeetSquared;
  }

  public void setRegionSizeFeetSquared(double regionSizeFeetSquared) {
    this.regionSizeFeetSquared = regionSizeFeetSquared;
  }

  public int getFloor() {
    return floor;
  }

  public void setFloor(int floor) {
    this.floor = floor;
  }
}

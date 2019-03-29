package com.ses.zebra.pssdemo_2019.POJOs.Geofencing;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class VertexPoint implements Serializable {

  @SerializedName("latitude")
  @Expose
  private Double latitude;
  @SerializedName("longitude")
  @Expose
  private Double longitude;
  private final static long serialVersionUID = -6461519332784133659L;

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

}

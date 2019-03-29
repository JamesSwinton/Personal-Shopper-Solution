package com.ses.zebra.pssdemo_2019.POJOs.Geofencing;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PopUpRegion implements Serializable {

  @SerializedName("UUID")
  @Expose
  private int id;
  @SerializedName("geo_fence_data")
  @Expose
  private GeofenceData geoFenceData;
  @SerializedName("pop_up_data")
  @Expose
  private PopUpData popUpData;
  private final static long serialVersionUID = -9001138493825107824L;

  public PopUpRegion() {
    Date now = new Date();
    this.id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.UK).format(now));
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public GeofenceData getGeoFenceData() {
    return geoFenceData;
  }

  public void setGeoFenceData(GeofenceData geoFenceData) {
    this.geoFenceData = geoFenceData;
  }

  public PopUpData getPopUpData() {
    return popUpData;
  }

  public void setPopUpData(PopUpData popUpData) {
    this.popUpData = popUpData;
  }

}

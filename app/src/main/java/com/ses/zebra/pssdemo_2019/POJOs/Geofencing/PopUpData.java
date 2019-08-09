package com.ses.zebra.pssdemo_2019.POJOs.Geofencing;

import android.net.Uri;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class PopUpData implements Serializable {

  @SerializedName("barcode")
  @Expose
  private String barcode;
  @SerializedName("title")
  @Expose
  private String title;
  @SerializedName("product_name")
  @Expose
  private String productName;
  @SerializedName("display_message")
  @Expose
  private Boolean displayMessage;
  @SerializedName("display_image")
  @Expose
  private Boolean displayImage;
  @SerializedName("message")
  @Expose
  private String message;
  @SerializedName("image")
  @Expose
  private String image;
  @SerializedName("display_time_seconds")
  @Expose
  private Integer displayTimeSeconds;
  private final static long serialVersionUID = 1785453022497605386L;

  public String getBarcode() {
    return barcode;
  }

  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public Boolean getDisplayMessage() {
    return displayMessage;
  }

  public void setDisplayMessage(Boolean displayMessage) {
    this.displayMessage = displayMessage;
  }

  public Boolean getDisplayImage() {
    return displayImage;
  }

  public void setDisplayImage(Boolean displayImage) {
    this.displayImage = displayImage;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public Integer getDisplayTimeSeconds() {
    return displayTimeSeconds;
  }

  public void setDisplayTimeSeconds(Integer displayTimeSeconds) {
    this.displayTimeSeconds = displayTimeSeconds;
  }

}

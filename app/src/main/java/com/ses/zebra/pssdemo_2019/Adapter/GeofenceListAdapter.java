package com.ses.zebra.pssdemo_2019.Adapter;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.ses.zebra.pssdemo_2019.App;
import com.ses.zebra.pssdemo_2019.Interfaces.EditOrDeleteGeofenceCallback;
import com.ses.zebra.pssdemo_2019.POJOs.Geofencing.PopUpRegion;
import com.ses.zebra.pssdemo_2019.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeofenceListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder > {

  // Debugging
  private static final String TAG = "GeofenceListAdapter";

  // Constants
  private static final int EMPTY_GEOFENCE_LIST_VIEW_TYPE = 0;
  private static final int POPULATED_GEOFENCE_LIST_VIEW_TYPE = 1;

  // Variables
  private List<PopUpRegion> mPopUpRegions;
  private EditOrDeleteGeofenceCallback mEditOrDeleteGeofenceCallback;

  public GeofenceListAdapter(EditOrDeleteGeofenceCallback editOrDeleteGeofenceCallback) {
    mPopUpRegions = new ArrayList<>(Arrays.asList(App.mPopUpRegions));
    mEditOrDeleteGeofenceCallback = editOrDeleteGeofenceCallback;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    switch(viewType) {
      case EMPTY_GEOFENCE_LIST_VIEW_TYPE:
        return new EmptyGeofenceHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_geofence_list_empty, parent, false));
      case POPULATED_GEOFENCE_LIST_VIEW_TYPE:
        return new GeofenceHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_geofence_list, parent, false));
      default:
        return new GeofenceHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_geofence_list, parent, false));
    }
  }

  @SuppressLint("SetTextI18n")
  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
    switch(viewHolder.getItemViewType()) {
      case POPULATED_GEOFENCE_LIST_VIEW_TYPE:
        // Cast Holder
        GeofenceHolder geofenceHolder = (GeofenceHolder) viewHolder;

        PopUpRegion popUpRegion = mPopUpRegions.get(position);

        if (popUpRegion.getPopUpData().getImage() != null) {
          geofenceHolder.image.setVisibility(View.VISIBLE);
          Glide.with(geofenceHolder.image)
              .load(popUpRegion.getPopUpData().getImage())
              .into(geofenceHolder.image);
        } else {
          geofenceHolder.image.setVisibility(View.GONE);
        }

        if (popUpRegion.getPopUpData().getMessage() != null) {
          geofenceHolder.message.setVisibility(View.VISIBLE);
          geofenceHolder.message.setText(popUpRegion.getPopUpData().getMessage());
        } else {
          geofenceHolder.message.setVisibility(View.GONE);
        }

        geofenceHolder.title.setText(popUpRegion.getPopUpData().getTitle());
        geofenceHolder.centerPoint.setText(
            popUpRegion.getGeoFenceData().getCenterPoint().getLatitude()
                + ", " + popUpRegion.getGeoFenceData().getCenterPoint().getLongitude());

        // Set Click Listener with Callback
        geofenceHolder.layoutContainer.setOnLongClickListener(view -> {
          mEditOrDeleteGeofenceCallback.onLongClick(popUpRegion);
          return false;
        });

        break;
    }
  }

  @Override
  public int getItemCount() {
    return mPopUpRegions.size() == 0 ? 1 : mPopUpRegions.size();
  }

  @Override
  public int getItemViewType(int position) {
    return mPopUpRegions.size() == 0 ? EMPTY_GEOFENCE_LIST_VIEW_TYPE : POPULATED_GEOFENCE_LIST_VIEW_TYPE;
  }

  public void refreshList() {
    mPopUpRegions = App.mPopUpRegions == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(App.mPopUpRegions));
    notifyDataSetChanged();
  }

  static class GeofenceHolder extends RecyclerView.ViewHolder {

    // List Elements
    LinearLayout layoutContainer;
    ImageView image;
    TextView title;
    TextView message;
    TextView centerPoint;

    GeofenceHolder(View geofenceLayout) {
      super(geofenceLayout);
      layoutContainer = geofenceLayout.findViewById(R.id.layoutContainer);
      image = geofenceLayout.findViewById(R.id.image);
      title = geofenceLayout.findViewById(R.id.title);
      message = geofenceLayout.findViewById(R.id.message);
      centerPoint = geofenceLayout.findViewById(R.id.center_point);
    }
  }

  static class EmptyGeofenceHolder extends RecyclerView.ViewHolder {
    EmptyGeofenceHolder(View stockLayout) {
      super(stockLayout);
    }
  }

}

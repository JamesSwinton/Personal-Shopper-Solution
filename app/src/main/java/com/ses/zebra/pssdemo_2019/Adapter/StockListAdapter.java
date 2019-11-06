package com.ses.zebra.pssdemo_2019.Adapter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ses.zebra.pssdemo_2019.App;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.Interfaces.EditOrDeleteStockItemCallback;
import com.ses.zebra.pssdemo_2019.POJOs.Meta;
import com.ses.zebra.pssdemo_2019.POJOs.StockItem;
import com.ses.zebra.pssdemo_2019.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.ses.zebra.pssdemo_2019.Activities.BaseActivity.DEFAULT_CURRENCY;
import static com.ses.zebra.pssdemo_2019.Activities.BaseActivity.PREF_SELECT_CURRENCY;
import static com.ses.zebra.pssdemo_2019.Activities.BaseActivity.mSharedPreferences;

public class StockListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder > {

    // Debugging
    private static final String TAG = "StockListAdapter";

    // Constants
    private static final int EMPTY_STOCK_LIST_VIEW_TYPE = 0;
    private static final int POPULATED_STOCK_LIST_VIEW_TYPE = 1;

    // Variables
    private StockItem[] mStockItems;
    private EditOrDeleteStockItemCallback mEditOrDeleteStockItemCallback;

    public StockListAdapter(EditOrDeleteStockItemCallback editOrDeleteStockItemCallback) {
        mStockItems = App.mStockItems == null ? new StockItem[0] : App.mStockItems;
        mEditOrDeleteStockItemCallback = editOrDeleteStockItemCallback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch(viewType) {
            case EMPTY_STOCK_LIST_VIEW_TYPE:
                return new EmptyStockHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.adapter_stock_list_empty, parent, false));
            case POPULATED_STOCK_LIST_VIEW_TYPE:
                return new StockHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.adapter_stock_list, parent, false));
            default:
                return new StockHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.adapter_stock_list, parent, false));
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        switch(viewHolder.getItemViewType()) {
            case POPULATED_STOCK_LIST_VIEW_TYPE:
                // Cast Holder
                StockHolder stockHolder = (StockHolder) viewHolder;

                // Get Current Stock Item
                StockItem stockItem = mStockItems[position];

                // Display Current Stock Item
                stockHolder.stockDesc.setText(stockItem.getDescription());
                stockHolder.stockPrice.setText(mSharedPreferences.getString(PREF_SELECT_CURRENCY,
                        DEFAULT_CURRENCY) + String.valueOf(stockItem.getPrice()));

                //
                stockHolder.stockIcon.setImageDrawable(stockHolder.stockIcon.getContext()
                        .getResources().getDrawable(R.drawable.ic_offers));

                // Display Image
                if (App.mMeta != null && App.mMeta.getStockImages() != null) {
                    for (Meta.StockImage stockImage : App.mMeta.getStockImages()) {
                        if (stockImage.getBarcode().equalsIgnoreCase(stockItem.getBarcode())) {
                            File imageFile = new File(App.mStockImagesPath + stockImage.getImageTag());
                            if (imageFile.exists()) {
                                Bitmap stockImageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                                stockHolder.stockIcon.setImageBitmap(stockImageBitmap);
                            } else {
                                stockHolder.stockIcon.setImageDrawable(stockHolder.stockIcon.getContext()
                                        .getResources().getDrawable(R.drawable.ic_offers));
                            }
                        }
                    }
                }

                // Set Click Listener with Callback
                stockHolder.stockContainer.setOnLongClickListener(view -> {
                    mEditOrDeleteStockItemCallback.onLongClick(mStockItems[position]);
                    return false;
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mStockItems.length == 0 ? 1 : mStockItems.length;
    }

    @Override
    public int getItemViewType(int position) {
        return mStockItems.length == 0 ? EMPTY_STOCK_LIST_VIEW_TYPE : POPULATED_STOCK_LIST_VIEW_TYPE;
    }

    public void refreshList() {
        mStockItems = App.mStockItems == null ? new StockItem[0] : App.mStockItems;
        notifyDataSetChanged();
    }

    static class StockHolder extends RecyclerView.ViewHolder {

        // List Elements
        LinearLayout stockContainer;
        ImageView stockIcon;
        TextView stockPrice;
        TextView stockDesc;

        StockHolder(View stockLayout) {
            super(stockLayout);
            stockContainer = stockLayout.findViewById(R.id.layoutContainer);
            stockIcon = stockLayout.findViewById(R.id.image);
            stockPrice = stockLayout.findViewById(R.id.price);
            stockDesc = stockLayout.findViewById(R.id.description);
        }
    }

    static class EmptyStockHolder extends RecyclerView.ViewHolder {
        EmptyStockHolder(View stockLayout) {
            super(stockLayout);
        }
    }

}

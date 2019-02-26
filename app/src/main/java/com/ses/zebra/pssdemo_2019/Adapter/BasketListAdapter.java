package com.ses.zebra.pssdemo_2019.Adapter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.ses.zebra.pssdemo_2019.Interfaces.UpdateBasketCallback;
import com.ses.zebra.pssdemo_2019.POJOs.BasketItem;
import com.ses.zebra.pssdemo_2019.POJOs.Meta;
import com.ses.zebra.pssdemo_2019.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.ses.zebra.pssdemo_2019.Activities.BaseActivity.DEFAULT_CURRENCY;
import static com.ses.zebra.pssdemo_2019.Activities.BaseActivity.PREF_SELECT_CURRENCY;
import static com.ses.zebra.pssdemo_2019.Activities.BaseActivity.mSharedPreferences;

public class BasketListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Debugging
    private static final String TAG = "BasketListAdapter";

    // Constants
    private static final int EMPTY_BASKET_VIEW_TYPE = 0;
    private static final int POPULATED_BASKET_VIEW_TYPE = 1;

    // Variables
    private List<BasketItem> mBasketItems;
    private UpdateBasketCallback mCallback;

    public BasketListAdapter(List<BasketItem> basketItems, UpdateBasketCallback callback) {
        this.mBasketItems = basketItems;
        this.mCallback = callback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case EMPTY_BASKET_VIEW_TYPE:
                return new EmptyViewHolder(LayoutInflater.from(
                        parent.getContext()).inflate(R.layout.adapter_basket_empty, parent, false));
            case POPULATED_BASKET_VIEW_TYPE:
                return new PopulatedViewHolder(LayoutInflater.from(
                        parent.getContext()).inflate(R.layout.adapter_basket_populated, parent, false));
            default:
                return new EmptyViewHolder(null);
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        switch(viewHolder.getItemViewType()) {
            case EMPTY_BASKET_VIEW_TYPE:
                Logger.i(TAG, "Showing Empty ViewHolder");
                break;
            case POPULATED_BASKET_VIEW_TYPE:
                Logger.i(TAG, "Showing Populated ViewHolder");
                // Cast ViewHolder to PopulatedViewHolder
                PopulatedViewHolder vh = (PopulatedViewHolder) viewHolder;
                // Get Current BasketItem
                BasketItem basketItem = mBasketItems.get(position);
                // Update Values
                vh.mDescription.setText(basketItem.getDescription());
                vh.mPrice.setText(mSharedPreferences.getString(PREF_SELECT_CURRENCY, DEFAULT_CURRENCY)
                        + String.format("%.2f", basketItem.getPrice()));
                vh.mQuantity.setText(String.valueOf(basketItem.getQuantity()));
                // Set Image if Exists
                for (Meta.StockImage stockImage : App.mMeta.getStockImages()) {
                    if (stockImage.getBarcode().equalsIgnoreCase(mBasketItems.get(position).getBarcode())) {
                        File stockImageFile = new File(App.mStockImagesPath + stockImage.getImageTag());
                        if (stockImageFile.exists()) {
                            vh.mBasketIcon.setImageBitmap(
                                    BitmapFactory.decodeFile(stockImageFile.getAbsolutePath()));
                        }
                    }
                }
                // Set Quantity Change Listeners
                vh.mAddQuantity.setOnClickListener(view -> mCallback.addQuantity(basketItem));
                vh.mMinusQuantity.setOnClickListener(view -> mCallback.minusQuantity(basketItem));
                vh.mProductLayout.setOnClickListener(view -> mCallback.viewProduct(basketItem));
                break;
        }
    }

    @Override
    public int getItemCount() {
        // Only 1 item = empty list = show empty view holder
        return mBasketItems.isEmpty() ? 1 : mBasketItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mBasketItems.size() == 0 ? EMPTY_BASKET_VIEW_TYPE : POPULATED_BASKET_VIEW_TYPE;
    }

    public static class PopulatedViewHolder extends RecyclerView.ViewHolder {

        // List Elements
        LinearLayout mProductLayout;
        ImageView mBasketIcon, mAddQuantity, mMinusQuantity;
        TextView mQuantity, mDescription, mPrice;

        PopulatedViewHolder(View view) {
            super(view);
            mProductLayout = view.findViewById(R.id.productLayout);
            mBasketIcon = view.findViewById(R.id.basketIcon);
            mAddQuantity = view.findViewById(R.id.addQuantity);
            mMinusQuantity = view.findViewById(R.id.minusQuantity);
            mQuantity = view.findViewById(R.id.quantity);
            mDescription = view.findViewById(R.id.description);
            mPrice = view.findViewById(R.id.price);
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(View view) {
            super(view);
        }
    }

}

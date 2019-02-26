package com.ses.zebra.pssdemo_2019.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
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
import com.ses.zebra.pssdemo_2019.Interfaces.ViewOfferProductCallback;
import com.ses.zebra.pssdemo_2019.POJOs.Meta;
import com.ses.zebra.pssdemo_2019.POJOs.StockItem;
import com.ses.zebra.pssdemo_2019.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static com.ses.zebra.pssdemo_2019.Activities.BaseActivity.DEFAULT_CURRENCY;
import static com.ses.zebra.pssdemo_2019.Activities.BaseActivity.PREF_SELECT_CURRENCY;
import static com.ses.zebra.pssdemo_2019.Activities.BaseActivity.mSharedPreferences;

public class OffersListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Debugging
    private static final String TAG = "OffersListAdapter";

    // Constants


    // Variables
    private Context mCx;
    private List<Meta.Offer> mOffersList;
    private ViewOfferProductCallback mCallack;

    public OffersListAdapter(Context cx, ViewOfferProductCallback callback) {
        this.mCx = cx;
        this.mCallack = callback;
        this.mOffersList = App.mMeta.getOffers();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new ViewHolder(LayoutInflater.from(
                parent.getContext()).inflate(R.layout.adapter_offers_list_v2, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        // Cast ViewHolder to PopulatedViewHolder
        ViewHolder vh = (ViewHolder) viewHolder;
        // Get Current BasketItem
        Meta.Offer offer = mOffersList.get(position);

        // Update Values
        vh.mDiscountText.setText(offer.getOffer());
        vh.mProductDescription.setText(offer.getDescription());

        // Get Stock Image if Available
//        for (Meta.StockImage stockImage : App.mMeta.getStockImages()) {
//            if (stockImage.getBarcode().equalsIgnoreCase(offer.getBarcode())) {
//                File stockImageFile = new File(App.mStockImagesPath + stockImage.getImageTag());
//                if (stockImageFile.exists()) {
//                    vh.mProductImage.setImageBitmap(
//                            BitmapFactory.decodeFile(stockImageFile.getAbsolutePath()));
//                } else {
//                    vh.mProductImage.setImageDrawable(mCx.getDrawable(R.drawable.ic_offers));
//                    Toast.makeText(mCx, "Image Missing!", Toast.LENGTH_LONG).show();
//                }
//            }
//        }

        for (Meta.StockImage stockImage : App.mMeta.getStockImages()) {
            if (stockImage.getBarcode().equalsIgnoreCase(offer.getBarcode())) {
                try {
                    String imageFilePath = App.mStockImagesPath + stockImage.getImageTag();
                    InputStream imageInputStream = mCx.getAssets().open("images/" + stockImage.getImageTag());
                    File stockImageFile = createFileFromAsset(imageInputStream, imageFilePath);
                    if (stockImageFile != null && stockImageFile.exists()) {
                        Bitmap stockImageBitmap = BitmapFactory.decodeFile(stockImageFile.getAbsolutePath());
                        vh.mProductImage.setImageBitmap(stockImageBitmap);
                    } else {
                        vh.mProductImage.setImageDrawable(mCx.getDrawable(R.drawable.ic_offers));
                        Toast.makeText(mCx, "Image Missing!", Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    Logger.e(TAG, "IOException: " + e.getMessage(), e);
                }
            }
        }

        for (Meta.StockImage stockImage : App.mMeta.getStockImages()) {
            if (stockImage.getBarcode().equalsIgnoreCase(offer.getBarcode())) {
                File stockImageFile = new File(App.mStockImagesPath + stockImage.getImageTag());
                if (stockImageFile.exists()) {
                    Bitmap stockImageBitmap = BitmapFactory.decodeFile(stockImageFile.getAbsolutePath());
                    vh.mProductImage.setImageBitmap(stockImageBitmap);
                } else {
                    vh.mProductImage.setImageDrawable(mCx.getDrawable(R.drawable.ic_offers));
                    Toast.makeText(mCx, "Image Missing!", Toast.LENGTH_LONG).show();
                }
            }
        }

        // Set Product Old & New Price
        for (StockItem stockItem : App.mStockItems) {
            if (stockItem.getBarcode().equalsIgnoreCase(offer.getBarcode())) {
                vh.mProudctOldPrice.setText(mCx.getResources().getString(R.string.offer_was,
                        mSharedPreferences.getString(PREF_SELECT_CURRENCY, DEFAULT_CURRENCY),
                        String.valueOf(stockItem.getPrice())));
                vh.mProductNewPrice.setText(mCx.getResources().getString(R.string.offer_now,
                        mSharedPreferences.getString(PREF_SELECT_CURRENCY, DEFAULT_CURRENCY),
                        calculateDiscount(offer.getDiscount(), stockItem.getPrice())));
            }
        }

        // Set Click Listener
        vh.mCardViewContainer.setOnClickListener(view ->
                mCallack.viewOfferProduct(mOffersList.get(position)));
    }

    private File createFileFromAsset(InputStream assetInputStream, String filePath) {
        try {
            File assetFile = new File(filePath);
            OutputStream outputStream = new FileOutputStream(assetFile);

            int length;
            byte buffer[] = new byte[1024];
            while((length = assetInputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }

            outputStream.close();
            assetInputStream.close();

            return assetFile;
        } catch (IOException e) {
            Logger.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    /*
     * If the discount is < 0 then it's just that amount off the price
     * If the discount is 0 to 1 then its a percentage
     * if its 241... then its two for one
     * */
    private static String calculateDiscount(double discount, double price) {
        // Amount off price
        if (discount < 0) {
            return String.format("%.2f", price + discount);
        }

        // Percentage off price
        if (0 < discount && discount < 1) {
            return String.format("%.2f", price - (price * discount));
        }

        // Unhandled cases (shouldn't be any)
        return String.valueOf(price);
    }

    @Override
    public int getItemCount() {
        return mOffersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // List Elements
        CardView mCardViewContainer;
        ImageView mProductImage, mDiscountImage;
        TextView mDiscountText;
        TextView mProductDescription, mProudctOldPrice, mProductNewPrice;


        ViewHolder(View view) {
            super(view);
            mCardViewContainer = view.findViewById(R.id.cardViewLayout);
            mProductImage = view.findViewById(R.id.productImage);
            mDiscountImage = view.findViewById(R.id.discountImage);
            mDiscountText = view.findViewById(R.id.discountText);
            mProductDescription = view.findViewById(R.id.productDescription);
            mProudctOldPrice = view.findViewById(R.id.oldPrice);
            mProductNewPrice = view.findViewById(R.id.updatedPrice);
        }
    }

}

package com.ses.zebra.pssdemo_2019.Activities.SubActivities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.BasketActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.OffersListActivity;
import com.ses.zebra.pssdemo_2019.Adapter.ProductListAdapter;
import com.ses.zebra.pssdemo_2019.App;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.POJOs.BasketItem;
import com.ses.zebra.pssdemo_2019.POJOs.Meta;
import com.ses.zebra.pssdemo_2019.POJOs.Sub.AllergenAdvice;
import com.ses.zebra.pssdemo_2019.POJOs.Sub.NutritionalInfoValues;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.databinding.ActivityProductBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

public class ProductActivity extends BaseActivity {

    // Debugging
    private static final String TAG = "ProductActivity";

    // Constants
    private static final int NO_PRODUCT_FOUND = 0;
    private static final int SHOW_BASKET_ITEM = 1;
    private static final int SHOW_OFFER_ITEM = 2;

    private static final String FROM = "from";
    private static final String BASKET_ACTIVITY = "basket-activity";
    private static final String OFFER_ACTIVITY = "offer-activity";
    private static final String BASKET_ITEM = "Basket-Item";
    private static final String OFFER_ITEM = "Offer-Item";

    // Variables
    private ActivityProductBinding mDataBinding;
    private ProductListAdapter mProductListAdapter;
    private static BasketItem mBasketItem;
    private static Meta.Offer mOfferItem;
    private static String mPreviousActivity;

    private List<String> mIngredients;
    private AllergenAdvice mAllergenAdvice;
    private NutritionalInfoValues mNutritionalInfo;

    @Override
    protected String getInheritedTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        // Init DataBinding
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_product);

        // Init Title & Icon & Back Navigation
        mDataBinding.headerText.setText("Product Info");
        mDataBinding.headerIcon.setImageResource(R.drawable.ic_back);
        mDataBinding.headerIcon.setOnClickListener(view -> {
            switch(mPreviousActivity) {
                case BASKET_ACTIVITY:
                    displayActivity(BasketActivity.class);
                    break;
                case OFFER_ACTIVITY:
                    displayActivity(OffersListActivity.class);
                    break;
            }
        });

        // Get Product From Intent
        switch (getProduct()) {
            case SHOW_BASKET_ITEM:
                displayBasketProductDetails();
                break;
            case SHOW_OFFER_ITEM:
                displayOfferProductDetails();
                break;
            case NO_PRODUCT_FOUND:
                Toast.makeText(this,
                        "Error locating product details", Toast.LENGTH_LONG).show();
                break;
        }

        // Init Dietary Info RecyclerView
        mProductListAdapter = new ProductListAdapter(this, mIngredients, mAllergenAdvice,
                mNutritionalInfo);
        mDataBinding.dietaryInfoExpandableView.setAdapter(mProductListAdapter);
    }

    private void displayBasketProductDetails() {
        // Set Product Details
        mDataBinding.price.setText(String.valueOf(mSharedPreferences.getString(PREF_SELECT_CURRENCY, DEFAULT_CURRENCY)
                + String.format(Locale.UK, "%.2f", mBasketItem.getPrice())));
        mDataBinding.productDescription.setText(mBasketItem.getDescription());
        mDataBinding.size.setText(String.valueOf(mBasketItem.getSize()) + "g");
        mDataBinding.discount.setText(calculateDiscount(mBasketItem.getDiscount(),
                mBasketItem.getPrice()));

        for (Meta.StockImage stockImage : App.mMeta.getStockImages()) {
            if (stockImage.getBarcode().equalsIgnoreCase(mBasketItem.getBarcode())) {
                File stockImageFile = new File(App.mStockImagesPath + stockImage.getImageTag());
                if (stockImageFile.exists()) {
                    Bitmap stockImageBitmap = BitmapFactory.decodeFile(stockImageFile.getAbsolutePath());
                    mDataBinding.productImage.setImageBitmap(stockImageBitmap);
                }
            }
        }

        // Update Dietary Info Variables
        mIngredients = mBasketItem.getIngredients();
        mAllergenAdvice = mBasketItem.getAllergenAdvice();
        mNutritionalInfo = mBasketItem.getNutritionalInfoValues();
    }

    private void displayOfferProductDetails() {
        for (int i = 0; i < App.mStockItems.length; i++) {
            if (App.mStockItems[i].getBarcode().equals(mOfferItem.getBarcode())) {
                mDataBinding.price.setText(String.valueOf(mSharedPreferences.getString(PREF_SELECT_CURRENCY, DEFAULT_CURRENCY)
                        + String.format(Locale.UK, "%.2f", App.mStockItems[i].getPrice())));
                mDataBinding.productDescription.setText(mOfferItem.getDescription());
                mDataBinding.size.setText(String.format(Locale.UK, "%.0f", App.mStockItems[i].getSize()) + "g");
                mDataBinding.discount.setText(calculateDiscount(mOfferItem.getDiscount(),
                        App.mStockItems[i].getPrice()));

                // Set Image if Exists
                for (Meta.StockImage stockImage : App.mMeta.getStockImages()) {
                    if (stockImage.getBarcode().equalsIgnoreCase(App.mStockItems[i].getBarcode())) {
                        File stockImageFile = new File(App.mStockImagesPath + stockImage.getImageTag());
                        if (stockImageFile.exists()) {
                            Bitmap stockImageBitmap = BitmapFactory.decodeFile(stockImageFile.getAbsolutePath());
                            mDataBinding.productImage.setImageBitmap(stockImageBitmap);
                        }
                    }
                }

                // Update Dietary Info Variables
                mIngredients = App.mStockItems[i].getIngredients();
                mAllergenAdvice = App.mStockItems[i].getAllergenAdvice();
                mNutritionalInfo = App.mStockItems[i].getNutritionalInfoValues();
            }
        }
    }

    private int getProduct() {
        Intent productIntent = getIntent();
        if (productIntent.getExtras() != null) {
            mPreviousActivity = productIntent.getStringExtra(FROM);
            if (productIntent.getSerializableExtra(BASKET_ITEM) != null) {
                mBasketItem = (BasketItem) productIntent.getSerializableExtra(BASKET_ITEM);
                return SHOW_BASKET_ITEM;
            } else if (productIntent.getSerializableExtra(OFFER_ITEM) != null) {
                mOfferItem = (Meta.Offer) productIntent.getSerializableExtra(OFFER_ITEM);
                return SHOW_OFFER_ITEM;
            }
        }
        return NO_PRODUCT_FOUND;
    }

    /*
     * If the discount is < 0 then it's just that amount off the price
     * If the discount is 0 to 1 then its a percentage
     * if its 241... then its two for one
     * */
    private static String calculateDiscount(double discount, double price) {
        // No Discount
        if (discount == 0) {
            return "Was " + String.valueOf(price);
        }

        // Amount off price
        if (discount < 0) {
            return mSharedPreferences.getString(PREF_SELECT_CURRENCY, DEFAULT_CURRENCY)
                    + String.valueOf(discount * -1) + " Off this purchase!";
        }

        // Percentage off price
        if (0 < discount && discount < 1) {
            return String.format(Locale.UK, "%.0f", (discount) * 100) + "% Off this purchase!";
        }

        // 2 for 1 Deal
        if (discount == 241) {
            return "Buy one get one free";
        }

        // Unhandled cases (shouldn't be any)
        return "";
    }
}

package com.ses.zebra.pssdemo_2019.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ses.zebra.pssdemo_2019.App;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.POJOs.Meta;
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

public class ShoppingListAdapter extends BaseExpandableListAdapter {

    // Debugging
    private static final String TAG = "ShoppingListAdapter";

    // Constants
    private static final String TEST = "";

    // Variables
    private Context mCx;
    private String mCurrency;
    private List<List<Meta.ShoppingList>> mShoppingLists;

    public ShoppingListAdapter(Context cx) {
        this.mCx = cx;
        this.mShoppingLists = App.mMeta.getShoppingLists();
        this.mCurrency = mSharedPreferences.getString(PREF_SELECT_CURRENCY, DEFAULT_CURRENCY);
    }

    @Override
    public int getGroupCount() {
        return mShoppingLists.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mShoppingLists.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mShoppingLists.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mCx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.adapter_shopping_list_header, parent, false);
        }

        TextView shoppingListHeader = convertView.findViewById(R.id.shoppingListHeader);
        switch (groupPosition) {
            case 0:
                shoppingListHeader.setText("List 1");
                break;
            case 1:
                shoppingListHeader.setText("List 2");
                break;
            case 2:
                shoppingListHeader.setText("List 3");
                break;
            default:
                shoppingListHeader.setText("Misc");
                break;
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mCx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.adapter_shopping_list_data, parent, false);
        }

        // Get Elements
        ImageView image = convertView.findViewById(R.id.image);
        TextView description = convertView.findViewById(R.id.description);
        TextView price = convertView.findViewById(R.id.price);

        // Create Shopping List Item
        Meta.ShoppingList shoppingList = (Meta.ShoppingList) getChild(groupPosition, childPosition);

        // Display Info
        description.setText(shoppingList.getDescription());
        description.setPadding(20, 0, 0, 0);
        description.setTextColor(Color.BLACK);
        description.setTextSize(12);
        price.setText(mCurrency + String.valueOf(shoppingList.getPrice()));
        price.setPadding(20, 0, 0, 0);
        price.setTextColor(Color.BLACK);
        price.setTextSize(12);

        for (Meta.StockImage stockImage : App.mMeta.getStockImages()) {
            if (stockImage.getBarcode().equalsIgnoreCase(shoppingList.getBarcode())) {
                File stockImageFile = new File(App.mStockImagesPath + stockImage.getImageTag());
                if (stockImageFile.exists()) {
                    Bitmap stockImageBitmap = BitmapFactory.decodeFile(stockImageFile.getAbsolutePath());
                    image.setImageBitmap(stockImageBitmap);
                } else {
                    image.setImageDrawable(mCx.getResources().getDrawable(R.drawable.ic_label));
                }
            }
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}

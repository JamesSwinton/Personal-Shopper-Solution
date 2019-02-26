package com.ses.zebra.pssdemo_2019.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.ses.zebra.pssdemo_2019.POJOs.Sub.AllergenAdvice;
import com.ses.zebra.pssdemo_2019.POJOs.Sub.CalcNutrient;
import com.ses.zebra.pssdemo_2019.POJOs.Sub.NutritionalInfoValues;
import com.ses.zebra.pssdemo_2019.R;

import java.util.ArrayList;
import java.util.List;

public class ProductListAdapter extends BaseExpandableListAdapter {

    // Debugging
    private static final String TAG = "ProductListAdapter";

    // Constants
    private static final String INGREDIENTS_HEADER = "Ingredients";
    private static final String ALLERGEN_HEADER = "Allergen Advice";

    // Variables
    private Context mCx;
    private static List<String> mHeaders;
    private static List<String> mIngredients;
    private static AllergenAdvice mAllergenAdvice;
    private static NutritionalInfoValues mNutritionalInfoValues;

    public ProductListAdapter(Context cx, List<String> ingredients, AllergenAdvice allergenAdvice,
                       NutritionalInfoValues nutritionalInfoValues) {
        mCx = cx;
        mHeaders = new ArrayList<>();
        mIngredients = ingredients;
        mAllergenAdvice = allergenAdvice;
        mNutritionalInfoValues = nutritionalInfoValues;

        if (mIngredients != null && mIngredients.size() > 0) {
            mHeaders.add(INGREDIENTS_HEADER);
        }

        if (mAllergenAdvice != null && mAllergenAdvice.getAllergens() != null
                && mAllergenAdvice.getAllergens().size() > 0) {
            mHeaders.add(ALLERGEN_HEADER);
        }

        if (mNutritionalInfoValues != null && mNutritionalInfoValues.getCalcNutrients() != null
                && mNutritionalInfoValues.getCalcNutrients().size() > 0) {
            mHeaders.add(mNutritionalInfoValues.getPerServingHeader());
        }
    }

    @Override
    public int getGroupCount() {
        return mHeaders.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String header = mHeaders.get(groupPosition);

        if (header.equals(INGREDIENTS_HEADER)) {
            return mIngredients.size();
        }

        if (header.equals(ALLERGEN_HEADER)) {
            return mAllergenAdvice.getAllergens().size();
        }

        if (header.equals(mNutritionalInfoValues.getPerServingHeader())) {
            return mNutritionalInfoValues.getCalcNutrients().size();
        }

        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String header = mHeaders.get(groupPosition);

        if (header.equals(INGREDIENTS_HEADER)) {
            return Html.fromHtml(mIngredients.get(childPosition));
        }

        if (header.equals(ALLERGEN_HEADER)) {
            StringBuilder allergens = new StringBuilder();
            allergens.append(mAllergenAdvice.getAllergens().get(childPosition).getAllergenName());
            allergens.append(": ");
            for (String allergen : mAllergenAdvice.getAllergens().get(childPosition).getAllergenValues()) {
                allergens.append(allergen + ", ");
            } return allergens;
        }

        if (header.equals(mNutritionalInfoValues.getPerServingHeader())) {
            CalcNutrient calcNutrientPerServing
                    = mNutritionalInfoValues.getCalcNutrients().get(childPosition);
            return calcNutrientPerServing.getName()
                    + ": " + calcNutrientPerServing.getValuePerServing();
        }

        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        TextView GroupName = new TextView(mCx);
        GroupName.setText("     " + mHeaders.get(groupPosition));
        GroupName.setPadding(20, 0, 0, 0);
        GroupName.setTextColor(mCx.getResources().getColor(R.color.zebraBlue));
        GroupName.setTextSize(16);
        return GroupName;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TextView itemName = new TextView(mCx);
        itemName.setText(getChild(groupPosition, childPosition).toString());
        itemName.setPadding(20, 0, 0, 0);
        itemName.setTextColor(Color.BLACK);
        itemName.setTextSize(12);
        return itemName;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}

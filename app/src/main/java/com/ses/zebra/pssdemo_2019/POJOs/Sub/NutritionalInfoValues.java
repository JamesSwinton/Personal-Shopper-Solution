package com.ses.zebra.pssdemo_2019.POJOs.Sub;

import java.io.Serializable;
import java.util.List;

public class NutritionalInfoValues implements Serializable {

    private String perServingHeader;
    private List<CalcNutrient> calcNutrients;

    public String getPerServingHeader() {
        return perServingHeader;
    }

    public void setPerServingHeader(String perServingHeader) {
        this.perServingHeader = perServingHeader;
    }

    public List<CalcNutrient> getCalcNutrients() {
        return calcNutrients;
    }

    public void setCalcNutrients(List<CalcNutrient> calcNutrients) {
        this.calcNutrients = calcNutrients;
    }

}
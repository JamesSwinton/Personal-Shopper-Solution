package com.ses.zebra.pssdemo_2019.POJOs.Sub;

import java.io.Serializable;

public class CalcNutrient implements Serializable {

    private String name, valuePerServing;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValuePerServing() {
        return valuePerServing;
    }

    public void setValuePerServing(String valuePerServing) {
        this.valuePerServing = valuePerServing;
    }

}

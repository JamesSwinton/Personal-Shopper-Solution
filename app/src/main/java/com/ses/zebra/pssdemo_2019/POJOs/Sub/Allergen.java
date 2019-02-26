package com.ses.zebra.pssdemo_2019.POJOs.Sub;

import java.io.Serializable;
import java.util.List;

public class Allergen implements Serializable {

    private String allergenName;
    private List<String> allergenValues = null;

    public String getAllergenName() {
        return allergenName;
    }

    public void setAllergenName(String allergenName) {
        this.allergenName = allergenName;
    }

    public List<String> getAllergenValues() {
        return allergenValues;
    }

    public void setAllergenValues(List<String> allergenValues) {
        this.allergenValues = allergenValues;
    }
}

package com.ses.zebra.pssdemo_2019.POJOs.Sub;

import java.io.Serializable;
import java.util.List;

public class AllergenAdvice implements Serializable {

    private List<Allergen> allergens = null;
    private String allergenText;

    public List<Allergen> getAllergens() {
        return allergens;
    }

    public void setAllergens(List<Allergen> allergens) {
        this.allergens = allergens;
    }

    public String getAllergenText() {
        return allergenText;
    }

    public void setAllergenText(String allergenText) {
        this.allergenText = allergenText;
    }

}

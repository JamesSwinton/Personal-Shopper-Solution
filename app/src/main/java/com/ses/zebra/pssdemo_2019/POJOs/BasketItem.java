package com.ses.zebra.pssdemo_2019.POJOs;

import com.ses.zebra.pssdemo_2019.POJOs.Sub.AllergenAdvice;
import com.ses.zebra.pssdemo_2019.POJOs.Sub.NutritionalInfoValues;

import java.io.Serializable;
import java.util.List;

public class BasketItem implements Serializable {

    private double price, size, discount;
    private String barcode, description;
    private int quantity;

    private List<String> ingredients;
    private AllergenAdvice allergenAdvice;
    private NutritionalInfoValues nutritionalInfoValues;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public AllergenAdvice getAllergenAdvice() {
        return allergenAdvice;
    }

    public void setAllergenAdvice(AllergenAdvice allergenAdvice) {
        this.allergenAdvice = allergenAdvice;
    }

    public NutritionalInfoValues getNutritionalInfoValues() {
        return nutritionalInfoValues;
    }

    public void setNutritionalInfoValues(NutritionalInfoValues nutritionalInfoValues) {
        this.nutritionalInfoValues = nutritionalInfoValues;
    }
}

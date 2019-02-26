package com.ses.zebra.pssdemo_2019.POJOs;

import com.ses.zebra.pssdemo_2019.POJOs.Sub.AllergenAdvice;
import com.ses.zebra.pssdemo_2019.POJOs.Sub.NutritionalInfoValues;

import java.io.Serializable;
import java.util.List;

public class StockItem implements Serializable {

    private String barcode, description;
    private double size, discount, price;
    private List<String> ingredients;
    private AllergenAdvice allergenAdvice;
    private NutritionalInfoValues nutritionalInfoValues;

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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
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

    public String[] getIngredientsList() {
        return ingredientsList;
    }

    private static final String[] ingredientsList = {
            "Acesulfame K",
            "Almonds",
            "Ammonium Bicarbonate",
            "Apple Juice Concentrate",
            "Artificial Color",
            "Ascorbic Acid (Vitamin C)",
            "Aspartame",
            "Baking Powder",
            "Beans",
            "Benzoic Acid",
            "Black Bean Flakes",
            "Black Bean Soup",
            "Black Beans",
            "Brown Sugar",
            "Butter Oil",
            "Buttermilk",
            "Caffeine",
            "Calcium Carbonate",
            "Calcium Pantothenate",
            "Canola Oil",
            "Caramel Color",
            "Carbonated Water",
            "Carnauba Wax",
            "Carrageenan",
            "Cayenne Pepper",
            "Cider Vinegar",
            "Citric Acid",
            "Colors",
            "Corn Flour",
            "Corn Meal",
            "Corn Oil",
            "Corn Starch",
            "Corn Syrup",
            "Cream",
            "Cultured Wheat",
            "Dextrose",
            "Diced Peaches",
            "Diced Pears",
            "Disodium Guanylate",
            "Disodium Inosinate",
            "Enriched Flour (Wheat Flour",
            "Erythritol",
            "Extract of Paprike",
            "Folic Acid",
            "Fructose",
            "Fruit and Vegetable Juice",
            "Garlic Powder",
            "Garlic",
            "Glucuronolactone",
            "Glycerin",
            "Grapes",
            "Guar Gum",
            "Guarana Extract",
            "Halved Cherries",
            "Herb Extract",
            "High Oleic Canola Oil",
            "Honey",
            "Hydrolyzed Soy Protein",
            "Inositol",
            "Inulin",
            "Invert Sugar",
            "Jalapeno Chilli Powder",
            "Jalapeno Pepper",
            "Lactic Acid",
            "L-Carnitine",
            "Liquid Sugar",
            "Locust Bean Gum",
            "L-Tartrate",
            "Malic Acid",
            "Malt Extract",
            "Maltodextrin",
            "Manchego Cheese",
            "Milk",
            "Milkfat",
            "Mixed Tocopherols",
            "Modified Corn Starch",
            "Modified Cornflour",
            "Molasses Powder",
            "Mono & Diglycerides",
            "Monosodium Glutamate",
            "Monosodium Glutamine",
            "Mozzarella Cheese",
            "Natural & Artificial Flavors",
            "Natural and artificial flavourings",
            "Natural Cane Sugar",
            "Natural Cherry Juice Concentrate",
            "Natural Flavourings",
            "Niacin",
            "Niacinamide",
            "Non-fat Dry Milk",
            "Oleoresin Paprika",
            "Onions",
            "Oregano",
            "Organic, unrefined Coconut Oil",
            "Palm Oil Shortening",
            "Panax Ginseng Extract",
            "Paprika",
            "Pear Juice Concentrate",
            "Pepper Jack Cheese",
            "Pineapple Segments",
            "Poblano Peppers",
            "Prepared Navy Beans",
            "Purified Carbonated Water",
            "Purple Carrot Juice Concentrate",
            "Pyridoxine HCL",
            "Pyrophosphate",
            "Rainsins",
            "Red Bell Peppers",
            "Red Wine Vinegar",
            "Reduced Iron",
            "Riboflavin",
            "Salsa",
            "Salt",
            "Scallions",
            "Sea Salt",
            "Skim Milk",
            "Sobric Acid",
            "Sodium Acid",
            "Sodium Bicarbonate",
            "Sodium Citrate",
            "Sodium Diacetate",
            "Soy Sauce",
            "Spices",
            "Spirit Vinegar",
            "Sucralose",
            "Sugar",
            "Taurine",
            "Thiamin Mononitrate",
            "Tomato Paste",
            "Tomato Powder",
            "Tomatoes",
            "Tortilla",
            "Torula Yeast",
            "Unsalted Butter",
            "Vegetable Oil",
            "Vinegar",
            "Vitamin B12",
            "Water",
            "Wheat Gluten",
            "Wheat Starch",
            "Whey",
            "White Mineral Oil",
            "Whole Grain Oat Flour",
            "Whole Grain Rolled Oats",
            "Whole Grain Wheat Flakes",
            "Worchester Sauce",
            "Xanthan Gum",
            "Yeast",
            "Yellow 5 Lake",
            "Yellow 6 Lake",
    };
}


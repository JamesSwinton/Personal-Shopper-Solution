package com.ses.zebra.pssdemo_2019.POJOs;

import java.io.Serializable;
import java.util.List;

public class Meta {

    private List<StockImage> stockImages = null;
    private List<Offer> offers = null;
    private List<List<ShoppingList>> shoppingLists = null;

    public List<StockImage> getStockImages() {
        return stockImages;
    }

    public void setStockImages(List<StockImage> stockImages) {
        this.stockImages = stockImages;
    }

    public List<Offer> getOffers() {
        return offers;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }

    public List<List<ShoppingList>> getShoppingLists() {
        return shoppingLists;
    }

    public void setShoppingLists(List<List<ShoppingList>> shoppingLists) {
        this.shoppingLists = shoppingLists;
    }

    public static class Offer implements Serializable {

        private String barcode;
        private String description;
        private String offer;
        private double discount;
        private String imageTag;

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

        public String getOffer() {
            return offer;
        }

        public void setOffer(String offer) {
            this.offer = offer;
        }

        public double getDiscount() {
            return discount;
        }

        public void setDiscount(double discount) {
            this.discount = discount;
        }

        public String getImageTag() {
            return imageTag;
        }

        public void setImageTag(String imageTag) {
            this.imageTag = imageTag;
        }

    }

    public static class ShoppingList {

        private String barcode;
        private String description;
        private double price;

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

    }

    public static class StockImage {

        private String barcode;
        private String imageTag;

        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        public String getImageTag() {
            return imageTag;
        }

        public void setImageTag(String imageTag) {
            this.imageTag = imageTag;
        }

    }

}

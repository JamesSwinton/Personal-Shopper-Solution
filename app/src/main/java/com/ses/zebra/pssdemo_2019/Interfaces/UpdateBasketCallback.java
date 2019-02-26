package com.ses.zebra.pssdemo_2019.Interfaces;

import com.ses.zebra.pssdemo_2019.POJOs.BasketItem;

public interface UpdateBasketCallback {
    void minusQuantity(BasketItem basketItem);
    void addQuantity(BasketItem basketItem);
    void viewProduct(BasketItem basketItem);
}

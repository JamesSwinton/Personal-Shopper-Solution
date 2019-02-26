package com.ses.zebra.pssdemo_2019.Interfaces;

import com.ses.zebra.pssdemo_2019.POJOs.Meta;
import com.ses.zebra.pssdemo_2019.POJOs.StockItem;

public interface EditOrDeleteStockItemCallback {
    void onLongClick(StockItem stockItem);
}

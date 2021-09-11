package com.mamunsproject.food_delevery.CallBack;

import com.mamunsproject.food_delevery.Model.Order;

import java.util.List;

public interface ILoadOrderCallBackListener {
    void onLoadOrderSuccess(List<Order>orderList);
    void onLoadOrderFailed(String message);

}

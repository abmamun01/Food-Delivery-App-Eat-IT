package com.mamunsproject.food_delevery.ui.View_Orders;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mamunsproject.food_delevery.Model.Order;

import java.util.List;

public class ViewOrderViewModel extends ViewModel {

    private MutableLiveData<List<Order>> mutableLiveDataOrderList;

    public ViewOrderViewModel(){
        mutableLiveDataOrderList=new MutableLiveData<>();

    }


    public MutableLiveData<List<Order>> getMutableLiveDataOrderList(){
        return mutableLiveDataOrderList;
    }


    public void setMutableLiveDataOrderList(List<Order>orderList){

        mutableLiveDataOrderList.setValue(orderList);
    }
}

package com.mamunsproject.food_delevery.ui.FoodList;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mamunsproject.food_delevery.Common.Common;
import com.mamunsproject.food_delevery.Model.FoodModel;

import java.util.List;

public class FoodlistViewModel extends ViewModel {


    private MutableLiveData<List<FoodModel>> mutableLiveDataFoodList;

    public FoodlistViewModel() {

    }

    public MutableLiveData<List<FoodModel>> getMutableLiveDataFoodList() {
        if (mutableLiveDataFoodList == null) {
            mutableLiveDataFoodList = new MutableLiveData<>();
        }
        mutableLiveDataFoodList.setValue(Common.categorySelected.getFoods());
        return mutableLiveDataFoodList;
    }
}

package com.mamunsproject.food_delevery.CallBack;

import com.mamunsproject.food_delevery.Model.CategoryModel;

import java.util.List;

public interface ICategoryCallBackListener {

    void onCategoryLoadSuccess(List<CategoryModel>categoryModelList);
    void onCategoryLoadFailed(String message);

}

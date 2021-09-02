package com.mamunsproject.food_delevery.CallBack;

import com.mamunsproject.food_delevery.Model.PopularCategoryModel;

import java.util.List;

public interface IPopularCallBackLisetener {

    void onPopularLoadSuccess(List<PopularCategoryModel> popularCategoryModels);
    void onPopularLoadFailed(String message);

}

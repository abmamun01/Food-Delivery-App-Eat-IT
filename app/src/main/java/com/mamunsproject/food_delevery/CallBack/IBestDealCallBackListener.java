package com.mamunsproject.food_delevery.CallBack;

import com.mamunsproject.food_delevery.Model.BestDealsModel;
import com.mamunsproject.food_delevery.Model.PopularCategoryModel;

import java.util.List;

public interface IBestDealCallBackListener {
    void onBestDealLoadSuccess(List<BestDealsModel> bestDealsModels);
    void onBestDealLoadFailed(String message);
}

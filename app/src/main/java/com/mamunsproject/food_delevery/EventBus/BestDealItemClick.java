package com.mamunsproject.food_delevery.EventBus;

import com.mamunsproject.food_delevery.Model.BestDealsModel;

public class BestDealItemClick {

    private BestDealsModel bestDealsModel;

    public BestDealItemClick(BestDealsModel bestDealsModel) {
        this.bestDealsModel = bestDealsModel;
    }


    public BestDealsModel getBestDealsModel() {
        return bestDealsModel;
    }

    public void setBestDealsModel(BestDealsModel bestDealsModel) {
        this.bestDealsModel = bestDealsModel;
    }
}

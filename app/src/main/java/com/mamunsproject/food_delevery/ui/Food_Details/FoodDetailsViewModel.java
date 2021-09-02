package com.mamunsproject.food_delevery.ui.Food_Details;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mamunsproject.food_delevery.Common.Common;
import com.mamunsproject.food_delevery.Model.Comment_Model;
import com.mamunsproject.food_delevery.Model.FoodModel;

public class FoodDetailsViewModel extends ViewModel {

    private MutableLiveData<FoodModel> mutableLiveDataFood;
    private final MutableLiveData<Comment_Model> mutableLiveDataComment;

    public void setCommentModel(Comment_Model comment_model) {
        if (mutableLiveDataComment != null) {
            mutableLiveDataComment.setValue(comment_model);
        }
    }



    public MutableLiveData<Comment_Model> getMutableLiveDataComment() {
        return mutableLiveDataComment;
    }

    public FoodDetailsViewModel() {

        mutableLiveDataComment = new MutableLiveData<>();
    }

    public MutableLiveData<FoodModel> getMutableLiveDataFood() {
        if (mutableLiveDataFood == null) {
            mutableLiveDataFood = new MutableLiveData<>();
        }
        mutableLiveDataFood.setValue(Common.selectedFood);
        return mutableLiveDataFood;
    }

    public void setFoodModel(FoodModel foodModel) {
        if (mutableLiveDataFood != null) {

            mutableLiveDataFood.setValue(foodModel);
        }
    }
}
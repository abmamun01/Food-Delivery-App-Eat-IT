package com.mamunsproject.food_delevery.ui.Comments;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mamunsproject.food_delevery.Model.Comment_Model;
import com.mamunsproject.food_delevery.Model.FoodModel;

import java.util.List;

public class CommentViewModel extends ViewModel {
    private MutableLiveData<List<Comment_Model>> mutableLiveDataFoddList;
    public CommentViewModel(){
        mutableLiveDataFoddList=new MutableLiveData<>();
    }

    public MutableLiveData<List<Comment_Model>> getMutableLiveDataFoddList(){
        return mutableLiveDataFoddList;
    }

    public void setCommentList(List<Comment_Model> commentList){
        mutableLiveDataFoddList.setValue(commentList);
    }
}

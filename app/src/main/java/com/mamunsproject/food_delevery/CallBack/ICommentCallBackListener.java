package com.mamunsproject.food_delevery.CallBack;

import com.mamunsproject.food_delevery.Model.Comment_Model;

import java.util.List;

public interface ICommentCallBackListener {
    void onCommentLoadSuccess(List<Comment_Model> commentModels);
    void onCommentLoadFailed(String message);

}

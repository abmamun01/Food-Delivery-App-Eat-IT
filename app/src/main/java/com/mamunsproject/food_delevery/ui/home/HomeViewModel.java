package com.mamunsproject.food_delevery.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mamunsproject.food_delevery.CallBack.IBestDealCallBackListener;
import com.mamunsproject.food_delevery.CallBack.IPopularCallBackLisetener;
import com.mamunsproject.food_delevery.Common.Common;
import com.mamunsproject.food_delevery.Model.BestDealsModel;
import com.mamunsproject.food_delevery.Model.PopularCategoryModel;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel implements IPopularCallBackLisetener, IBestDealCallBackListener {


    private MutableLiveData<List<PopularCategoryModel>> popularList;
    private MutableLiveData<List<BestDealsModel>> bestDealList;
    private MutableLiveData<String> messegeError;
    private IPopularCallBackLisetener popularCallBackLisetener;
    private IBestDealCallBackListener bestDealCallBackListener;

    public HomeViewModel(){
        popularCallBackLisetener=this;
        bestDealCallBackListener=this;

    }

    public MutableLiveData<List<PopularCategoryModel>> getPopularList() {

        if (popularList==null){
            popularList=new MutableLiveData<>();
            messegeError=new MutableLiveData<>();
            LoadPopularList();
        }

        return popularList;
    }

    private void LoadPopularList() {
        List<PopularCategoryModel> tempList=new ArrayList<>();
        DatabaseReference popularRef = FirebaseDatabase.getInstance().getReference(Common.POPULAR_CATEGORY_REF);

        popularRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot itemSnapshot:snapshot.getChildren()){
                    PopularCategoryModel model=itemSnapshot.getValue(PopularCategoryModel.class);
                    tempList.add(model);
                }
                popularCallBackLisetener.onPopularLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                popularCallBackLisetener.onPopularLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<List<BestDealsModel>> getBestDealList() {
        if (bestDealList==null  ){
            bestDealList=new MutableLiveData<>();
            messegeError=new MutableLiveData<>();
            loadBestDeal();
        }
        return bestDealList;
    }

    private void loadBestDeal() {

        List<BestDealsModel> tempList=new ArrayList<>();
        DatabaseReference bestDealRef=FirebaseDatabase.getInstance().getReference(Common.BEST_DEALS_REF);
        bestDealRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                for (DataSnapshot itemSnapshot:snapshot.getChildren()){
                    BestDealsModel model=itemSnapshot.getValue(BestDealsModel.class);
                    tempList.add(model);
                }
                bestDealCallBackListener.onBestDealLoadSuccess(tempList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                bestDealCallBackListener.onBestDealLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessegeError() {
        return messegeError;
    }

    @Override
    public void onPopularLoadSuccess(List<PopularCategoryModel> popularCategoryModels) {

        popularList.setValue(popularCategoryModels);
    }

    @Override
    public void onPopularLoadFailed(String message) {

        messegeError.setValue(message);
    }

    @Override
    public void onBestDealLoadSuccess(List<BestDealsModel> bestDealsModels) {

        bestDealList.setValue(bestDealsModels);
    }

    @Override
    public void onBestDealLoadFailed(String message) {

        messegeError.setValue(message);
    }
}
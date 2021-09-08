package com.mamunsproject.food_delevery.ui.Cart;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mamunsproject.food_delevery.Common.Common;
import com.mamunsproject.food_delevery.Database.CartDataSources;
import com.mamunsproject.food_delevery.Database.CartDatabase;
import com.mamunsproject.food_delevery.Database.CartItem;
import com.mamunsproject.food_delevery.Database.LocalCartDataSource;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CartViewModel extends ViewModel {

    private CompositeDisposable compositeDisposable;
    private CartDataSources cartDataSources;
    private MutableLiveData<List<CartItem>> mutableLiveDataInCartItem;

    public CartViewModel(){
        compositeDisposable=new CompositeDisposable();
    }

    public void initCartDataSource(Context context){
        cartDataSources=new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
    }

    public void onStop(){
        compositeDisposable.clear();
    }
    public MutableLiveData<List<CartItem>> getMutableLiveDataInCartItem() {
        if (mutableLiveDataInCartItem==null)
            mutableLiveDataInCartItem=new MutableLiveData<>();
        
        getAllCartItems();
        return mutableLiveDataInCartItem;
    }

    private void getAllCartItems() {

        compositeDisposable.add(cartDataSources.getAllCart(Common.currentUser.getUid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(cartItems -> {

            mutableLiveDataInCartItem.setValue(cartItems);

        }, throwable -> {

            mutableLiveDataInCartItem.setValue(null);
        }));
    }
}

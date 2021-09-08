package com.mamunsproject.food_delevery.Database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class LocalCartDataSource implements CartDataSources {

    private CartDAOS cartDAOS;

    public LocalCartDataSource(CartDAOS cartDAOS) {
        this.cartDAOS = cartDAOS;
    }



    @Override
    public Flowable<List<CartItem>> getAllCart(String uid) {
        return cartDAOS.getAllCart(uid);
    }

    @Override
    public Single<Integer> countItemInCart(String uid) {
        return cartDAOS.countItemInCart(uid);
    }


    @Override
    public Single<Double> sumPriceInCart(String uid) {
        return cartDAOS.sumPriceInCart(uid);
    }


    @Override
    public Single<CartItem> getItemInCart(String foodId, String uid) {
        return cartDAOS.getItemInCart(foodId,uid);
    }


    @Override
    public Completable insertOrReplaceAll(CartItem... cartItems) {
        return cartDAOS.insertOrReplaceAll(cartItems);
    }


    @Override
    public Single<Integer> updateCartItems(CartItem cartItem) {
        return cartDAOS.updateCartItems(cartItem);
    }


    @Override
    public Single<Integer> deleteCartItem(CartItem cartItem) {
        return cartDAOS.deleteCartItem(cartItem);
    }


    @Override
    public Single<Integer> cleanCart(String uid) {
        return cartDAOS.cleanCart(uid);
    }

    @Override
    public Single<CartItem> getItemWithAllOptionsInCart(String uid, String foodId, String foodSize, String foodAddon) {
        return cartDAOS.getItemWithAllOptionsInCart(uid, foodId, foodSize, foodAddon);
    }

}

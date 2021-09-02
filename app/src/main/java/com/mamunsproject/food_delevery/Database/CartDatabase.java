package com.mamunsproject.food_delevery.Database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(version = 2, entities = CartItem.class, exportSchema = true)
public abstract class CartDatabase extends RoomDatabase {


    public abstract CartDAOS cartDAO();
    private static CartDatabase instance;

    public static CartDatabase getInstance(Context context) {
        if (instance == null)
            instance = Room.databaseBuilder(context, CartDatabase.class, "FoodDeliveryV1").build();

        return instance;
    }

}



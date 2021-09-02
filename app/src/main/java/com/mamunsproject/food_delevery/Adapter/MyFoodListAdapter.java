package com.mamunsproject.food_delevery.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mamunsproject.food_delevery.CallBack.IRecyclerClickListener;
import com.mamunsproject.food_delevery.Common.Common;
import com.mamunsproject.food_delevery.Database.CartDataSources;
import com.mamunsproject.food_delevery.Database.CartDatabase;
import com.mamunsproject.food_delevery.Database.CartItem;
import com.mamunsproject.food_delevery.Database.LocalCartDataSource;
import com.mamunsproject.food_delevery.EventBus.CounterCartEvent;
import com.mamunsproject.food_delevery.EventBus.FoodItemClick;
import com.mamunsproject.food_delevery.Model.FoodModel;
import com.mamunsproject.food_delevery.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyFoodListAdapter extends RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder> {

    private Context context;
    private List<FoodModel> foodModelsList;
    private CompositeDisposable compositeDisposable;
    private CartDataSources cartDataSources;

    public MyFoodListAdapter(Context context, List<FoodModel> foodModelsList) {
        this.context = context;
        this.foodModelsList = foodModelsList;
        this.compositeDisposable = new CompositeDisposable();
        this.cartDataSources = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());

    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_food_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Glide.with(context)
                .load(foodModelsList.get(position).getImage()).into(holder.img_food_image);

        holder.txt_food_price.setText("৳" + foodModelsList.get(position).getPrice());
        holder.txt_food_name.setText(foodModelsList.get(position).getName());

        //Event
        holder.setListener((view, pos) -> {
            Common.selectedFood = foodModelsList.get(pos);
            Common.selectedFood.setKey(String.valueOf(pos));
            EventBus.getDefault().postSticky(new FoodItemClick(true, foodModelsList.get(pos)));
        });

        holder.img_cart.setOnClickListener(v -> {

            CartItem cartItem = new CartItem();
            cartItem.setUid(Common.currentUser.getUid());
            cartItem.setUserPhone(Common.currentUser.getPhone());

            cartItem.setFoodId(foodModelsList.get(position).getId());
            cartItem.setFoodName(foodModelsList.get(position).getName());
            cartItem.setFoodImage(foodModelsList.get(position).getImage());
            cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModelsList.get(position).getPrice())));
            cartItem.setFoodQuantitiy(1);
            cartItem.setFoodExtraPrice(0.0);//Because default we not choose size + addon so extra prise is 0
            cartItem.setFoodAddon("Default");
            cartItem.setFoodSize("Default");


            cartDataSources.getItemWithAllOptionsInCart(Common.currentUser.getUid(),
                    cartItem.getFoodId(),
                    cartItem.getFoodSize(),
                    cartItem.getFoodAddon())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<CartItem>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull CartItem cartItemFromDB) {

                            if (cartItemFromDB.equals(cartItem)) {
                                //ALREADY IN DATABASE , JUST UPDATE
                                cartItemFromDB.setFoodExtraPrice(cartItem.getFoodExtraPrice());
                                cartItemFromDB.setFoodAddon(cartItem.getFoodAddon());
                                cartItemFromDB.setFoodSize(cartItem.getFoodSize());
                                cartItemFromDB.setFoodQuantitiy(cartItemFromDB.getFoodQuantitiy() + cartItemFromDB.getFoodQuantitiy());

                                cartDataSources.updateCartItems(cartItemFromDB)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new SingleObserver<Integer>() {
                                            @Override
                                            public void onSubscribe(@NonNull Disposable d) {

                                            }

                                            @Override
                                            public void onSuccess(@NonNull Integer integer) {
                                                Toast.makeText(context, "Update Cart Successful!", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                            }

                                            @Override
                                            public void onError(@NonNull Throwable e) {

                                                Toast.makeText(context, "[UPDATE CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });


                            } else {
                                //Item not available in cart before,inser new
                                compositeDisposable.add(cartDataSources.insertOrReplaceAll(cartItem).subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(() -> {
                                                    Toast.makeText(context, "Add to Cart Success!", Toast.LENGTH_SHORT).show();
                                                    EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                                }, throwable -> {
                                                    Toast.makeText(context, "[CART ERROR]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                }

                                        ));
                            }

                        }

                        @Override
                        public void onError(@NonNull Throwable e) {

                            if (e.getMessage().contains("empty")) {

                                //Default,if cart is empty, this code will be fired
                                compositeDisposable.add(cartDataSources.insertOrReplaceAll(cartItem).subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(() -> {
                                                    Toast.makeText(context, "Add to Cart Success!", Toast.LENGTH_SHORT).show();

                                                    EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                                }, throwable -> {
                                                    Toast.makeText(context, "[CART ERROR]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                }

                                        ));

                            } else {

                            }
                            Toast.makeText(context, "GET CART" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        });
    }

    @Override
    public int getItemCount() {
        return foodModelsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Unbinder unbinder;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.text_food_price)
        TextView txt_food_price;
        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @BindView(R.id.img_fav)
        ImageView img_fav;
        @BindView(R.id.img_quick_cart)
        ImageView img_cart;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v, getAdapterPosition());
        }
    }
}

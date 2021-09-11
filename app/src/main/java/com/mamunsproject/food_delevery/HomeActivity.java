package com.mamunsproject.food_delevery;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mamunsproject.food_delevery.Common.Common;
import com.mamunsproject.food_delevery.Database.CartDataSources;
import com.mamunsproject.food_delevery.Database.CartDatabase;
import com.mamunsproject.food_delevery.Database.LocalCartDataSource;
import com.mamunsproject.food_delevery.EventBus.BestDealItemClick;
import com.mamunsproject.food_delevery.EventBus.CategorClick;
import com.mamunsproject.food_delevery.EventBus.CounterCartEvent;
import com.mamunsproject.food_delevery.EventBus.FoodItemClick;
import com.mamunsproject.food_delevery.EventBus.MenuItemBack;
import com.mamunsproject.food_delevery.EventBus.PopularCategoryClick;
import com.mamunsproject.food_delevery.Model.CategoryModel;
import com.mamunsproject.food_delevery.Model.FoodModel;
import com.mamunsproject.food_delevery.databinding.ActivityHomeBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;
    NavController navController, navController2;

    private CartDataSources cartDataSources;
    android.app.AlertDialog dialog;

    int menuClickId = -1;


    @BindView(R.id.fab)
    CounterFab fab;

    @Override
    protected void onResume() {
        super.onResume();

        counCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarHome.toolbar);

        ButterKnife.bind(this);
        cartDataSources = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_menu, R.id.nav_food_details, R.id.nav_cart, R.id.nav_view_orders, R.id.nav_food_list)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        fab.setOnClickListener(view -> {
            navController.navigate(R.id.nav_cart);

        });

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user);


        Common.setSpanString("Hey ", Common.currentUser.getName(), txt_user);
        counCartItem();


        Log.d("COMMMONNNN", "onCreate: " + Common.currentUser.getName() + " ID  \n " + Common.currentUser.getUid() + "\n  Phone " + Common.currentUser.getPhone() + " \n Add " + Common.currentUser.getAddress());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();

    }


    //EventBUs


    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCartCounter(CounterCartEvent event) {
        if (event.isSuccess()) {


            counCartItem();
            //   Toast.makeText(this, "Click No "+event.getCategoryModel().getName(), Toast.LENGTH_SHORT).show();
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPopularItemClick(PopularCategoryClick event) {
        if (event.getPopularCategoryModel() != null) {

            navController2 = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);


            dialog.show();

            FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child(event.getPopularCategoryModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {


                                Common.categorySelected = snapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(snapshot.getKey());

                                //Load Food
                                FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child(event.getPopularCategoryModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getPopularCategoryModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {

                                                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                                                        Common.selectedFood = itemSnapshot.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(itemSnapshot.getKey());

                                                    }

                                                    navController2.navigate(R.id.nav_food_details);

                                                } else {

                                                    Toast.makeText(getApplicationContext(), "Item Doesn't Exist!", Toast.LENGTH_SHORT).show();

                                                }

                                                dialog.dismiss();


                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dialog.dismiss();
                                                Toast.makeText(getApplicationContext(), "Error!" + error, Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            } else {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Item Doesn't Exist!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBestDealItemClick(BestDealItemClick event) {
        if (event.getBestDealsModel() != null) {

            navController2 = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);

            dialog.show();

            FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child(event.getBestDealsModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {

                                Common.categorySelected = snapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(snapshot.getKey());

                                //Load Food
                                FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child(event.getBestDealsModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getBestDealsModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {

                                                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                                                        Common.selectedFood = itemSnapshot.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(itemSnapshot.getKey());

                                                    }

                                                    navController2.navigate(R.id.nav_food_details);

                                                } else {

                                                    Toast.makeText(getApplicationContext(), "Item Doesn't Exist!", Toast.LENGTH_SHORT).show();

                                                }

                                                dialog.dismiss();


                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dialog.dismiss();
                                                Toast.makeText(getApplicationContext(), "Error!" + error, Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            } else {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Item Doesn't Exist!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }


    private void counCartItem() {

        cartDataSources.countItemInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull Integer integer) {

                        fab.setCount(integer);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                        if (!e.getMessage().contains("Query returned empty!"))
                            Toast.makeText(getApplicationContext(), "Error!" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        else fab.setCount(0);
                    }
                });
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);

        navController2 = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);

        switch (item.getItemId()) {
            case R.id.nav_home:
                if (item.getItemId() != menuClickId)
                    navController2.navigate(R.id.nav_home);
                break;

            case R.id.nav_menu:
                if (item.getItemId() != menuClickId)

                    navController2.navigate(R.id.nav_menu);
                break;

            case R.id.nav_cart:
                if (item.getItemId() != menuClickId)

                    navController2.navigate(R.id.nav_cart);
                break;

            case R.id.nav_view_orders:
                if (item.getItemId() != menuClickId)

                    navController2.navigate(R.id.nav_view_orders);
                break;

            case R.id.nav_sign_out:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Sign Out")
                        .setMessage("Do You Really want to Sign Out ?")
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                dialogInterface.dismiss();
                            }
                        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Common.selectedFood = null;
                        Common.categorySelected = null;
                        Common.currentUser = null;
                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        finish();


                    }
                });

                break;


        }

        menuClickId = item.getItemId();

        return true;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategorClick event) {
        if (event.isSuccess()) {

            navController2 = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
            navController2.navigate(R.id.nav_food_list);

            Toast.makeText(this, "Click No " + event.getCategoryModel().getName(), Toast.LENGTH_SHORT).show();
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFoodItemClick(FoodItemClick event) {
        if (event.isSuccess()) {

            navController2 = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
            navController2.navigate(R.id.nav_food_details);

            Toast.makeText(this, "Click No " + event.getFoodModel().getName(), Toast.LENGTH_SHORT).show();
        }
    }


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onMenuItemBack(MenuItemBack event){

        menuClickId=-1;

        if (getSupportFragmentManager().getBackStackEntryCount()>0)
            getSupportFragmentManager().popBackStack();

    }
}
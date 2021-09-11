package com.mamunsproject.food_delevery.ui.FoodList;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.mamunsproject.food_delevery.Adapter.MyFoodListAdapter;
import com.mamunsproject.food_delevery.Common.Common;
import com.mamunsproject.food_delevery.EventBus.MenuItemBack;
import com.mamunsproject.food_delevery.MainActivity;
import com.mamunsproject.food_delevery.Model.FoodModel;
import com.mamunsproject.food_delevery.R;
import com.mamunsproject.food_delevery.ui.home.HomeViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class FoodListFragment extends Fragment {

    private FoodlistViewModel sendViewModel;

    Unbinder unbinder;
    @BindView(R.id.recycler_food_list)
    RecyclerView recyclerFoodList;

    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sendViewModel= new ViewModelProvider(this).get(FoodlistViewModel.class);
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_food_list, container, false);

        unbinder= ButterKnife.bind(this,view);
        initView();
        sendViewModel.getMutableLiveDataFoodList().observe(getViewLifecycleOwner(), foodModels -> {

            adapter=new MyFoodListAdapter(getContext(),foodModels);
            recyclerFoodList.setAdapter(adapter);
            recyclerFoodList.setLayoutAnimation(layoutAnimationController);
        });


        return view;

    }


    private void initView() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Common.categorySelected.getName());
        recyclerFoodList.setHasFixedSize(true);
        recyclerFoodList.setLayoutManager(new LinearLayoutManager(getContext()));
        layoutAnimationController= AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
    }


    @Override
    public void onDestroy() {

        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}
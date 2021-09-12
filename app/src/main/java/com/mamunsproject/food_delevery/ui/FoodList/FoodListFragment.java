package com.mamunsproject.food_delevery.ui.FoodList;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;

import com.mamunsproject.food_delevery.Adapter.MyFoodListAdapter;
import com.mamunsproject.food_delevery.Common.Common;
import com.mamunsproject.food_delevery.EventBus.MenuItemBack;
import com.mamunsproject.food_delevery.MainActivity;
import com.mamunsproject.food_delevery.Model.CategoryModel;
import com.mamunsproject.food_delevery.Model.FoodModel;
import com.mamunsproject.food_delevery.R;
import com.mamunsproject.food_delevery.ui.home.HomeViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class FoodListFragment extends Fragment {

    private FoodlistViewModel foodlistViewModel;

    Unbinder unbinder;
    @BindView(R.id.recycler_food_list)
    RecyclerView recyclerFoodList;

    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        foodlistViewModel = new ViewModelProvider(this).get(FoodlistViewModel.class);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_food_list, container, false);

        unbinder = ButterKnife.bind(this, view);
        initView();
        foodlistViewModel.getMutableLiveDataFoodList().observe(getViewLifecycleOwner(), foodModels -> {

            if (foodModels != null) {

                adapter = new MyFoodListAdapter(getContext(), foodModels);
                recyclerFoodList.setAdapter(adapter);
                recyclerFoodList.setLayoutAnimation(layoutAnimationController);

            }
        });


        return view;

    }


    private void initView() {
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Common.categorySelected.getName());
        recyclerFoodList.setHasFixedSize(true);
        recyclerFoodList.setLayoutManager(new LinearLayoutManager(getContext()));
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.search_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));


        //Event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                startSearch(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });


        // Clear Text when click to clear button on search View

        ImageView closeButton = searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(view -> {

            EditText ed = searchView.findViewById(R.id.search_src_text);
            //Clear text
            ed.setText("");
            //Clear query
            searchView.setQuery("", false);
            //Collapse the action view
            searchView.onActionViewCollapsed();
            //Collapse the search wiedget
            menuItem.collapseActionView();
            //Restore result to original
            foodlistViewModel.getMutableLiveDataFoodList();
        });

    }

    private void startSearch(String s) {

        List<FoodModel> resultList = new ArrayList<>();
        for (int i = 0; i < Common.categorySelected.getFoods().size(); i++) {

            FoodModel foodModel = Common.categorySelected.getFoods().get(i);
            if (foodModel.getName().toLowerCase().contains(s))
                resultList.add(foodModel);

        }
        foodlistViewModel.getMutableLiveDataFoodList().setValue(resultList);
    }

    @Override
    public void onDestroy() {

        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}
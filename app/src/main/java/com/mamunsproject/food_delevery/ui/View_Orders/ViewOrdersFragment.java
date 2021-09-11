package com.mamunsproject.food_delevery.ui.View_Orders;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mamunsproject.food_delevery.Adapter.MyOrderAdapter;
import com.mamunsproject.food_delevery.CallBack.ILoadOrderCallBackListener;
import com.mamunsproject.food_delevery.Common.Common;
import com.mamunsproject.food_delevery.Model.Order;
import com.mamunsproject.food_delevery.R;
import com.mamunsproject.food_delevery.ui.Food_Details.FoodDetailsViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;


public class ViewOrdersFragment extends Fragment implements ILoadOrderCallBackListener {

    @BindView(R.id.recycler_orders)
    RecyclerView recycler_orders;
    AlertDialog alertDialog;

    private Unbinder unbinder;
    private ViewOrderViewModel viewOrderViewModel;

    private ILoadOrderCallBackListener listener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewOrderViewModel = new ViewModelProvider(this).get(ViewOrderViewModel.class);

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_view_orders, container, false);
        unbinder = ButterKnife.bind(this, root);

        initView(root);
        loadOrderFromFirebase();
        viewOrderViewModel.getMutableLiveDataOrderList().observe(getViewLifecycleOwner(),
                orderList -> {
                    MyOrderAdapter adapter=new MyOrderAdapter(getContext(),orderList);
                    recycler_orders.setAdapter(adapter);
                });

        return root;
    }

    private void loadOrderFromFirebase() {
        List<Order> orderList=new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .orderByChild("userId")
                .equalTo(Common.currentUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot orderSnapshot: snapshot.getChildren()){
                            Order order=orderSnapshot.getValue(Order.class);
                            order.setOrderNumber(orderSnapshot.getKey()); //Remember set it
                            orderList.add(order);

                        }

                        listener.onLoadOrderSuccess(orderList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
listener.onLoadOrderFailed(error.getMessage());
                    }
                });
    }

    private void initView(View root) {

        listener=this;
        alertDialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();

        recycler_orders.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_orders.setLayoutManager(layoutManager);
        recycler_orders.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
    }

    @Override
    public void onLoadOrderSuccess(List<Order> orderList) {
        alertDialog.dismiss();;
        viewOrderViewModel.setMutableLiveDataOrderList(orderList);
    }

    @Override
    public void onLoadOrderFailed(String message) {

        alertDialog.dismiss();
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
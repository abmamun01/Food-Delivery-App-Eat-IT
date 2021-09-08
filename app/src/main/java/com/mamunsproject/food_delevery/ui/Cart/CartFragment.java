package com.mamunsproject.food_delevery.ui.Cart;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.mamunsproject.food_delevery.Adapter.MyCartAdapter;
import com.mamunsproject.food_delevery.Common.Common;
import com.mamunsproject.food_delevery.Common.MySwiperHelper;
import com.mamunsproject.food_delevery.Database.CartDataSources;
import com.mamunsproject.food_delevery.Database.CartDatabase;
import com.mamunsproject.food_delevery.Database.CartItem;
import com.mamunsproject.food_delevery.Database.LocalCartDataSource;
import com.mamunsproject.food_delevery.EventBus.CounterCartEvent;
import com.mamunsproject.food_delevery.EventBus.UpdateItemInCart;
import com.mamunsproject.food_delevery.Model.Order;
import com.mamunsproject.food_delevery.R;
import com.mamunsproject.food_delevery.databinding.FragmentCartBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment {

    private Parcelable recyclerViewState;
    private CartDataSources cartDataSources;
    private CompositeDisposable compositeDisposable=new CompositeDisposable();


    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;


    @BindView(R.id.recycler_cart)
    RecyclerView recyclerViewCart;

    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.txt_empty_catt)
    TextView txt_empty_cart;

    @BindView(R.id.group_place_holder)
    CardView group_place_holder;

    @OnClick(R.id.btn_place_order)
    void onPlaceOrderClick() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("One More Step!");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order, null);

        EditText edt_address = (EditText) view.findViewById(R.id.edt_address);
        EditText edt_comment = (EditText) view.findViewById(R.id.edt_comment);
        TextView txt_address = view.findViewById(R.id.txt_address_details);


        RadioButton rdi_home = view.findViewById(R.id.rdi_home_address);
        RadioButton rdi_other_address = view.findViewById(R.id.rdi_other_address);
        RadioButton rdi_ship_this_address = view.findViewById(R.id.rdi_ship_this_address);
        RadioButton rdi_cod = view.findViewById(R.id.rdi_cod);
        RadioButton rdi_braintree = view.findViewById(R.id.rdi_braintree);

        //Data
        edt_address.setText(Common.currentUser.getAddress()); // By default we select home address, so user's address will display

        //Event
        rdi_home.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                edt_address.setText(Common.currentUser.getAddress());
                txt_address.setVisibility(View.GONE);

            }
        });
        rdi_other_address.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                edt_address.setText("");
                edt_address.setHint("Enter Your Address");
                txt_address.setVisibility(View.GONE);


            }
        });
        rdi_ship_this_address.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {

                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationProviderClient.getLastLocation()
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                txt_address.setVisibility(View.GONE);
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        String coordinates = new StringBuilder()
                                .append(task.getResult().getLatitude())
                                .append("/")
                                .append(task.getResult().getLongitude()).toString();

                        Single<String> singleAdress=Single.just(getAdressFromLatLong(task.getResult().getLatitude(),
                                task.getResult().getLongitude()));

                        Disposable disposable=singleAdress.subscribeWith(new DisposableSingleObserver<String>(){
                            @Override
                            public void onSuccess(@NonNull String s) {

                                edt_address.setText(coordinates);
                                txt_address.setText(s);
                                txt_address.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                edt_address.setText(coordinates);
                                txt_address.setText(e.getMessage());
                                txt_address.setVisibility(View.VISIBLE);
                            }
                        });

                    }
                });
            }
        });


        builder.setView(view);
        builder.setNegativeButton("No", ((dialogInterface, i) -> {
            dialogInterface.dismiss();
        })).setPositiveButton("Yes", (dialogInterface, i) -> {

            //Toast.makeText(getContext(), "Implement Late!", Toast.LENGTH_SHORT).show();

            if (rdi_cod.isChecked()){
                paymentCOD(edt_address.getText().toString(),edt_comment.getText().toString());

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void paymentCOD(String address, String comment) {

        compositeDisposable.add(cartDataSources.getAllCart(Common.currentUser.getUid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(cartItems -> {
            //when we have all cartItems,we will get total price
            cartDataSources.sumPriceInCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Double>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull Double totalPrice) {

                            double finalPrice=totalPrice; // we will modify this formula for discount late
                            Order order=new Order();
                            order.setUserId(Common.currentUser.getUid());
                            order.setUserName(Common.currentUser.getName());
                            order.setUserPhone(Common.currentUser.getPhone());
                            order.setShippingAddress(address);
                            order.setComment(comment);

                            if (currentLocation!=null){
                                order.setLat(currentLocation.getLatitude());
                                order.setLng(currentLocation.getLongitude());
                            }else {
                                order.setLat(-0.1f);
                                order.setLng(-0.1f);
                            }
                            order.setCartItemList(cartItems);
                            order.setTotalPayment(totalPrice);
                            order.setDiscount(0);
                            order.setFinalPayment(finalPrice);
                            order.setCod(true);
                            order.setTransactionId("Cansh On Delivery");

                            //Submit this order  object to Firebase
                            writeToFirebase(order);
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }, throwable -> {

            Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }));
    }

    private void writeToFirebase(Order order) {

        FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_REF)
        .child(Common.createOrderNumber())//Create order with only digit
        .setValue(order)
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                cartDataSources.cleanCart(Common.currentUser.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Integer>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onSuccess(@NonNull Integer integer) {
                                //Clean Success
                                Toast.makeText(getContext(), "Order Placed Succssfully!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                
                                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
            }
        });
    }

    private String getAdressFromLatLong(double latitude, double longitude) {

        Geocoder geocoder=new Geocoder(getContext(), Locale.getDefault());
        String result="";

        try {
            List<Address> addressList=geocoder.getFromLocation(latitude,longitude,1);
            if (addressList!=null && addressList.size()>0 ){
                Address address=addressList.get(0); //Always get first item
                StringBuilder sb=new StringBuilder(address.getAddressLine(0));
                result=sb.toString();

            }else {
                result="Address not found";
            }
        }catch (Exception e){

            e.printStackTrace();
            result=e.getMessage();
        }
        return result;
    }

    private MyCartAdapter adapter;


    private Unbinder unbinder;

    private CartViewModel cartViewModel;
    FragmentCartBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        cartViewModel =
                new ViewModelProvider(this).get(CartViewModel.class);
        binding = FragmentCartBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        unbinder = ButterKnife.bind(this, root);
        initViews();
        initLocation();


        cartViewModel.initCartDataSource(getContext());
        cartViewModel.getMutableLiveDataInCartItem().observe(getViewLifecycleOwner(), new Observer<List<CartItem>>() {
            @Override
            public void onChanged(List<CartItem> cartItems) {


                Log.d("SIEID", "onChanged: " + cartItems.size());
                if (cartItems == null || cartItems.isEmpty()) {
                    recyclerViewCart.setVisibility(View.GONE);
                    group_place_holder.setVisibility(View.GONE);
                    txt_empty_cart.setVisibility(View.VISIBLE);
                } else {

                    recyclerViewCart.setVisibility(View.VISIBLE);
                    group_place_holder.setVisibility(View.VISIBLE);
                    txt_empty_cart.setVisibility(View.GONE);

                    adapter = new MyCartAdapter(getContext(), cartItems);
                    recyclerViewCart.setAdapter(adapter);
                }
            }
        });

        return root;
    }

    private void initLocation() {
        buildLocationRequest();
        buildLocationCallBack();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());


    }


    private void buildLocationCallBack() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();

            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);

    }

    private void initViews() {
        setHasOptionsMenu(true);
        cartDataSources = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        recyclerViewCart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewCart.setLayoutManager(layoutManager);
        recyclerViewCart.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));


        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recyclerViewCart, 200) {
            @Override
            public void instantitaMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {

                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#ff3c30"),
                        pos -> {

                            CartItem cartItem = adapter.getItemPosition(pos);
                            cartDataSources.deleteCartItem(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(@NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(@NonNull Integer integer) {

                                            adapter.notifyItemRemoved(pos);
                                            sumAllItemInCart();//Update total price
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true)); //Update Fab
                                            Toast.makeText(getContext(), "Delet item from Cart Successful!", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(@NonNull Throwable e) {
                                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }));
            }
        };
        sumAllItemInCart();

    }

    private void sumAllItemInCart() {

        cartDataSources.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull Double aDouble) {

                        txt_total_price.setText(new StringBuilder("Total: $ ").append(aDouble));
                        Log.d("KDKDKDK", "onSuccess: " + txt_total_price);
                        Log.d("KDKDKDK", "onSuccess: " + aDouble);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                        if (!e.getMessage().contains("Query returned empty"))
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false);// Hide Home menu already inflate
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cart) {
            cartDataSources.cleanCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull Integer integer) {

                            Toast.makeText(getContext(), "Clear Cart Success", Toast.LENGTH_SHORT).show();

                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {

                            Toast.makeText(getContext(), " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (fusedLocationProviderClient != null) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCart(UpdateItemInCart event) {
        if (event.getCartItem() != null) {
            //First , save state of RecyclerView

            recyclerViewState = recyclerViewCart.getLayoutManager().onSaveInstanceState();

            cartDataSources.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull Integer integer) {

                            calculateTotalPrice();
                            recyclerViewCart.getLayoutManager().onRestoreInstanceState(recyclerViewState); //Fix error refresh recyclerView after update
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {

                            Toast.makeText(getContext(), "[UPDATE CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void calculateTotalPrice() {
        cartDataSources.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull Double aDouble) {

                        txt_total_price.setText(new StringBuilder("Total: $")
                                .append(Common.formatPrice(aDouble)));
                        Log.d("KDKKLLL", "onSuccess: " + txt_total_price);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                        Toast.makeText(getContext(), "[SUM CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }
}
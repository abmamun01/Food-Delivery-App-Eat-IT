package com.mamunsproject.food_delevery.ui.Food_Details;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.mamunsproject.food_delevery.Common.Common;
import com.mamunsproject.food_delevery.Database.CartDataSources;
import com.mamunsproject.food_delevery.Database.CartDatabase;
import com.mamunsproject.food_delevery.Database.CartItem;
import com.mamunsproject.food_delevery.Database.LocalCartDataSource;
import com.mamunsproject.food_delevery.EventBus.CounterCartEvent;
import com.mamunsproject.food_delevery.EventBus.MenuItemBack;
import com.mamunsproject.food_delevery.Model.AddonModel;
import com.mamunsproject.food_delevery.Model.Comment_Model;
import com.mamunsproject.food_delevery.Model.FoodModel;
import com.mamunsproject.food_delevery.Model.SizeModel;
import com.mamunsproject.food_delevery.R;
import com.mamunsproject.food_delevery.databinding.FragmentFoodDetailsBinding;
import com.mamunsproject.food_delevery.ui.Comments.CommentsFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class FoodDetails_Fragment extends Fragment implements TextWatcher {

    private CartDataSources cartDataSources;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private FoodDetailsViewModel foodDetailsViewModel;
    private FragmentFoodDetailsBinding binding;
    private Unbinder unbinder;
    Context context;
    private BottomSheetDialog addonBottomsheetDialog;

    //View need inflate
    ChipGroup chipGroup_addon;
    EditText edt_search;


    AlertDialog waitingDialog;

    @BindView(R.id.img_foodss)
    ImageView img_food;
    @BindView(R.id.btnCart)
    CounterFab btnCart;
    @BindView(R.id.btn_rating)
    FloatingActionButton btn_rating;
    @BindView(R.id.food_name)
    TextView food_name;
    @BindView(R.id.food_description)
    TextView food_description;
    @BindView(R.id.food_price)
    TextView food_price;
    @BindView(R.id.number_button)
    ElegantNumberButton numberButton;
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;
    @BindView(R.id.btn_show_Comment)
    Button btn_showComment;

    @BindView(R.id.rdi_group_size)
    RadioGroup radioGroup_size;
    @BindView(R.id.img_addon)
    ImageView img_addon;
    @BindView(R.id.chip_group_user_selected_addon)
    ChipGroup chipGroup_user_selected_addon;


    @OnClick(R.id.img_addon)
    void onAddonClick() {
        if (Common.selectedFood.getAddon() != null) {
            displayAddonList();
            addonBottomsheetDialog.show();
        }
    }

    @OnClick(R.id.btnCart)
    void onCartItemAdd() {

        Toast.makeText(context, "HEllo Clicked~!", Toast.LENGTH_SHORT).show();
        CartItem cartItem = new CartItem();
        cartItem.setUid(Common.currentUser.getUid());
        cartItem.setUserPhone(Common.currentUser.getPhone());


        cartItem.setFoodId(Common.selectedFood.getId());
        cartItem.setFoodName(Common.selectedFood.getName());
        cartItem.setFoodImage(Common.selectedFood.getImage());
        cartItem.setFoodPrice(Double.valueOf(String.valueOf(Common.selectedFood.getPrice())));
        cartItem.setFoodQuantitiy(Integer.valueOf(numberButton.getNumber()));
        cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectedFood.getUserSelectedSize(), Common.selectedFood.getUserSelectedAddon()));//Because default we not choose size + addon so extra prise is 0

        if (Common.selectedFood.getUserSelectedAddon() != null) {
            cartItem.setFoodAddon(new Gson().toJson(Common.selectedFood.getUserSelectedAddon()));
        } else {

            cartItem.setFoodAddon("Default");
        }
        if (Common.selectedFood.getUserSelectedSize() != null) {
            cartItem.setFoodSize(new Gson().toJson(Common.selectedFood.getUserSelectedSize()));
        } else {

            cartItem.setFoodSize("Default");

        }
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


    }

    private void displayAddonList() {
        if (Common.selectedFood.getAddon().size() > 0) {
            chipGroup_addon.clearCheck();//Clear check all views
            chipGroup_addon.removeAllViews();

            edt_search.addTextChangedListener(this);

            //Add all view
            for (AddonModel addonModel : Common.selectedFood.getAddon()) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));

                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {

                    if (isChecked) {
                        if (Common.selectedFood.getUserSelectedAddon() == null) {
                            Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                        }
                        Common.selectedFood.getUserSelectedAddon().add(addonModel);

                    }
                });
                chipGroup_addon.addView(chip);

            }
        }


    }

    @OnClick(R.id.btn_rating)
    void onRatingButtonClick() {
        showDialogRating();
    }

    @OnClick(R.id.btn_show_Comment)
    void onShowCommentBUttonClick() {
        CommentsFragment commentsFragment = CommentsFragment.getInstance();
        commentsFragment.show(getActivity().getSupportFragmentManager(), "CommentFragment");
    }

    private void showDialogRating() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Register");
        builder.setMessage("Please fill information!");


        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_rating, null);
        RatingBar ratingBar = itemView.findViewById(R.id.rating_bar);
        EditText edit_comment = itemView.findViewById(R.id.edit_comment);

        builder.setView(itemView);
        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.setPositiveButton("OK", (dialog, which) -> {

            Comment_Model comment_model = new Comment_Model();
            comment_model.setName(Common.currentUser.getName());
            comment_model.setUid(Common.currentUser.getUid());
            comment_model.setComment(edit_comment.getText().toString());
            comment_model.setRatingValue(ratingBar.getRating());
            Map<String, Object> serverTimeStamp = new HashMap<>();
            serverTimeStamp.put("timeStamp", ServerValue.TIMESTAMP);
            comment_model.setCommentTimeStamp(serverTimeStamp);


            foodDetailsViewModel.setCommentModel(comment_model);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodDetailsViewModel =
                new ViewModelProvider(this).get(FoodDetailsViewModel.class);

        binding = FragmentFoodDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        unbinder = ButterKnife.bind(this, root);

        initView();

        foodDetailsViewModel.getMutableLiveDataFood().observe(getViewLifecycleOwner(), foodModel -> {

            displayInfo(foodModel);
            Log.d("foodModel", "onCreateView: ");

        });


        foodDetailsViewModel.getMutableLiveDataComment().observe(getViewLifecycleOwner(), comment_model -> {
            submitRatingToFirebase(comment_model);
            Log.d("foodDetailsViewModel", "onCreateView: ");
        });

        return root;
    }

    private void initView() {

        cartDataSources = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());

        context = getContext();
        waitingDialog = new SpotsDialog.Builder().setCancelable(false).setContext(context).build();
        addonBottomsheetDialog = new BottomSheetDialog(context, R.style.DialogStyle);
        View layout_addon_display = getLayoutInflater().inflate(R.layout.layout_addon_display, null);

        chipGroup_addon = (ChipGroup) layout_addon_display.findViewById(R.id.chip_group_addon);
        edt_search = (EditText) layout_addon_display.findViewById(R.id.edt_search);
        addonBottomsheetDialog.setContentView(layout_addon_display);

        addonBottomsheetDialog.setOnDismissListener(dialog -> {
            displayUserSelectedAddon();
        });


        numberButton.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                calculateTotalPrice();
            }
        });


    }

    private void displayUserSelectedAddon() {
        if (Common.selectedFood.getUserSelectedAddon() != null && Common.selectedFood.getUserSelectedAddon().size() > 0) {
            chipGroup_user_selected_addon.removeAllViews();//Clear all view already added
            for (AddonModel addonModel : Common.selectedFood.getUserSelectedAddon()) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon, null);

                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));
                chip.setClickable(false);
                chip.setOnCloseIconClickListener(v -> {

                    chipGroup_user_selected_addon.removeView(v);
                    Common.selectedFood.getUserSelectedAddon().remove(addonModel);
                    calculateTotalPrice();
                });
                chipGroup_user_selected_addon.addView(chip);

            }
        } else  {
            chipGroup_user_selected_addon.removeAllViews();
        }

        calculateTotalPrice();

    }

    private void submitRatingToFirebase(Comment_Model comment_model) {
        waitingDialog.show();
        Log.d("submit", "submitRatingToFirebase: " + "CalllE");

        //First,we will submit to curents ref
        FirebaseDatabase.getInstance()
                .getReference(Common.COMMENT_REF)
                .child(Common.selectedFood.getId())
                .push()
                .setValue(comment_model)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        //After Submit to CommentRef,we will update value 
                        addRatingToFodd(comment_model.getRatingValue());
                    }
                    waitingDialog.dismiss();
                });
    }

    private void addRatingToFodd(float ratingValue) {

        Log.d("Calll", "addRatingToFodd: " + "Calll");
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .child("foods")//selectArryList 'foods' of this category
                .child(Common.selectedFood.getKey())//Because food item is array list so key is index of arrayLIst
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            FoodModel foodModel = snapshot.getValue(FoodModel.class);
                            foodModel.setKey(Common.selectedFood.getKey());

                            //Apply Rating
                            if (foodModel.getRatingValue() == null)
                                foodModel.setRatingValue(0d);
                            if (foodModel.getRatingCount() == null)
                                foodModel.setRatingCount(0l);

                            double sumRating = foodModel.getRatingValue() + ratingValue;
                            long ratingCount = foodModel.getRatingCount() + 1;


                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("ratingValue", sumRating);
                            updateData.put("ratingCount", ratingCount);
                            //Update data in variable

                            foodModel.setRatingValue(sumRating);
                            foodModel.setRatingCount(ratingCount);

                            snapshot.getRef()
                                    .updateChildren(updateData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                Toast.makeText(context, "Thank You!", Toast.LENGTH_SHORT).show();

                                                Common.selectedFood = foodModel;
                                                foodDetailsViewModel.setFoodModel(foodModel);// Call refresh
                                            }
                                            waitingDialog.dismiss();
                                        }
                                    });
                        } else {
                            waitingDialog.dismiss();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        waitingDialog.dismiss();
                        Toast.makeText(context, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayInfo(FoodModel foodModel) {

        Glide.with(context).load(foodModel.getImage()).into(img_food);
        food_name.setText(foodModel.getName());
        food_description.setText(foodModel.getDescription());
        food_price.setText(foodModel.getPrice().toString());


        if (foodModel.getRatingValue() != null) {
            ratingBar.setRating(foodModel.getRatingValue().floatValue() / foodModel.getRatingCount());
        }

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Common.selectedFood.getName());

        //Size
        for (SizeModel sizeModel : Common.selectedFood.getSize()) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked)
                    Common.selectedFood.setUserSelectedSize(sizeModel);
                calculateTotalPrice();//Update Price
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
            radioButton.setLayoutParams(params);
            radioButton.setText(sizeModel.getName());
            radioButton.setTag(sizeModel.getPrice());
            radioGroup_size.addView(radioButton);


        }

        if (radioGroup_size.getChildCount() > 0) {
            RadioButton radioButton = (RadioButton) radioGroup_size.getChildAt(0);
            radioButton.setChecked(true);

        }

        calculateTotalPrice();
    }


    private void calculateTotalPrice() {
        double totalPrice = Double.parseDouble(Common.selectedFood.getPrice().toString()), displayPrice = 0.0;

        //Addon
        if (Common.selectedFood.getUserSelectedAddon() != null && Common.selectedFood.getUserSelectedAddon().size() > 0) {

            for (AddonModel addonModel : Common.selectedFood.getUserSelectedAddon())
                totalPrice += Double.parseDouble(addonModel.getPrice().toString());
        }
        try {
            //Size
            if (Common.selectedFood.getUserSelectedSize() != null)
                totalPrice += Double.parseDouble(Common.selectedFood.getUserSelectedSize().getPrice().toString());

            displayPrice = totalPrice * (Integer.parseInt(numberButton.getNumber()));
            displayPrice = Math.round(displayPrice * 100.0 / 100.0);
            food_price.setText(new StringBuilder("").append(Common.formatPrice(displayPrice)).toString());


        } catch (Exception e) {
            Toast.makeText(context, "Total Price Null" + totalPrice + "NullLL" + e, Toast.LENGTH_SHORT).show();
        }

        Log.d("EMPTYDD", "getUserSelectedSize: " + Common.selectedFood.getUserSelectedSize());
        Log.d("EMPTYDD", "getId: " + Common.selectedFood.getId());
        Log.d("EMPTYDD", "getUserSelectedAddon: " + Common.selectedFood.getUserSelectedAddon());
        Log.d("EMPTYDD", "getUid: " + Common.currentUser.getUid());
        Log.d("EMPTYDD", "getName: " + Common.currentUser.getName().toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //Noting
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        chipGroup_addon.clearCheck();
        chipGroup_addon.removeAllViews();

        for (AddonModel addonModel : Common.selectedFood.getAddon()) {
            if (addonModel.getName().toLowerCase().contains(s.toString())) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));

                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {

                    if (isChecked) {
                        if (Common.selectedFood.getUserSelectedAddon() == null) {
                            Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                        }
                        Common.selectedFood.getUserSelectedAddon().add(addonModel);

                    }
                });
                chipGroup_addon.addView(chip);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {

        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}
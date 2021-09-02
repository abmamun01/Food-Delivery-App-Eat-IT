package com.mamunsproject.food_delevery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mamunsproject.food_delevery.Common.Common;
import com.mamunsproject.food_delevery.Model.UserModel;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {

    private static int APP_REQUEST_CODE = 7171;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private DatabaseReference userRef;
    //For Register
    private List<AuthUI.IdpConfig> providers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){


        //--------========-------------- For Register --------========--------------

        providers= Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());

        userRef= FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCES);
        firebaseAuth=FirebaseAuth.getInstance();
        dialog=new SpotsDialog.Builder().setCancelable(false).setContext(this).build();

        listener= firebaseAuth -> {
            FirebaseUser user=firebaseAuth.getCurrentUser();
            if (user!=null){
               // Toast.makeText(getApplicationContext(), "Already Login!", Toast.LENGTH_SHORT).show();
                checkUserFromFirebase(user);
            }else {

                phoneLogin();
            }
        };


        //--------========-------------- For Register --------========--------------




    }








    //--------========-------------- For Register --------========--------------



    private void checkUserFromFirebase(FirebaseUser user) {
        dialog.show();
        userRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()){

                            Toast.makeText(MainActivity.this, "You Already Registered!", Toast.LENGTH_SHORT).show();
                            UserModel userModel=snapshot.getValue(UserModel.class);

                        }else {

                            showRegisterDialog(user);
                        }

                        dialog.hide();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialog.hide();

                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void showRegisterDialog(FirebaseUser user){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage("Please fill information!");



        View itemView=LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        EditText editName,editAddress,editPhone;
        editName =itemView.findViewById(R.id.edit_name);
        editAddress=itemView.findViewById(R.id.edit_address);
        editPhone=itemView.findViewById(R.id.edit_phone);

        //Set Data
        editPhone.setText(user.getPhoneNumber());


        UserModel userModel=new UserModel();
        userModel.setUid(user.getUid());
        userModel.setName(editName.getText().toString());
        userModel.setAddress(editAddress.getText().toString());
        userModel.setPhone(editPhone.getText().toString());

        userRef.child(user.getUid()).setValue(userModel)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        dialog.hide();
                        Toast.makeText(MainActivity.this, "Congratulation! Succes", Toast.LENGTH_SHORT).show();
                        gotoHomeActivity(userModel);
                    }
                });

        builder.setView(itemView);
        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.setPositiveButton("REGISTERED", (dialog, which) -> {

            if (TextUtils.isEmpty(editName.getText().toString())){
                Toast.makeText(MainActivity.this, "Please enter your name!", Toast.LENGTH_SHORT).show();
            }else if (TextUtils.isEmpty(editAddress.getText().toString())){
                Toast.makeText(MainActivity.this, "Enter Your Address!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(itemView);
        AlertDialog dialog=builder.create();
        dialog.show();



    }


    private void gotoHomeActivity(UserModel userModel) {
        //--------========-------------- For Register --------========--------------

        Common.currentUser=userModel;// Importan,you need always assign value for it!
        Log.d("KDJDDDD", "gotoHomeActivity: ");
        //--------========-------------- For Register --------========--------------


        startActivity(new Intent(MainActivity.this,HomeActivity.class));
        finish();
    }

    private void  phoneLogin() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers).build(),APP_REQUEST_CODE);
    }


    //--------========-------------- For Register --------========--------------






    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //--------========-------------- For Register --------========--------------

        if (requestCode==APP_REQUEST_CODE){

            IdpResponse response=IdpResponse.fromResultIntent(data);
            if (resultCode==RESULT_OK){
                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
            }else {
                Toast.makeText(getApplicationContext(), "Fialed to sing in!", Toast.LENGTH_SHORT).show();
                Log.d("KDJED", "Failed To Login : "+requestCode+" "+resultCode+" "+data +"   "+RESULT_OK);
            }
        }
        //--------========-------------- For Register --------========--------------


    }

    @Override
    protected void onStart() {
        super.onStart();
        //--------========-------------- For Register --------========--------------

        firebaseAuth.addAuthStateListener(listener);

        //--------========-------------- For Register --------========--------------

    }

    @Override
    protected void onStop() {


        //--------========-------------- For Register --------========--------------
        if (listener != null) {
            firebaseAuth.removeAuthStateListener(listener);
            compositeDisposable.clear();
            super.onStop();
        }
        //--------========-------------- For Register --------========--------------

    }


    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
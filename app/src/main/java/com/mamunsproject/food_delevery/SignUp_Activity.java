package com.mamunsproject.food_delevery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.mamunsproject.food_delevery.Common.Common;
import com.mamunsproject.food_delevery.Model.UserModel;

public class SignUp_Activity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword, etName, etPhone, etAddress;
    private DatabaseReference userRef;
    FirebaseUser user;
    private Button signup_button;
    String email, password, phone, address, name;
    FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child(Common.USER_REFERENCES);


        etEmail = findViewById(R.id.etEmail_Signup);
        etPassword = findViewById(R.id.etPassword_signup);
        signup_button = findViewById(R.id.signupButton);
        etName = findViewById(R.id.et_name_signup);
        etPhone = findViewById(R.id.edit_phone_Number);
        etAddress = findViewById(R.id.edit_address);


        signup_button.setOnClickListener(view ->

                mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("TAGSSS", "createUserWithEmail:success");

                                    FirebaseUser user = mAuth.getCurrentUser();

                                    email = etEmail.getText().toString();
                                    password = etPassword.getText().toString();
                                    phone = etPhone.getText().toString();
                                    address = etAddress.getText().toString();
                                    name = etName.getText().toString();


                                    UserModel userModel = new UserModel();

                                    userModel.setUid(user.getUid());
                                    userModel.setName(name);
                                    userModel.setAddress(address);
                                    userModel.setPhone(phone);


                                    userRef.child(user.getUid()).setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "Congratulation! Succes", Toast.LENGTH_SHORT).show();

                                                gotoHomeActivity(userModel,Common.currentToken);
                                            }
                                        }
                                    });

                                    //goto home

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(SignUp_Activity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        }));


    }
/*    private void gotoHomeActivity(UserModel userModel) {
        //--------========-------------- For Register --------========--------------


        Common.currentUser = userModel;// Importan,you need always assign value for it!
        startActivity(new Intent(SignUp_Activity.this, HomeActivity.class));
        finish();

            }*/




    private void gotoHomeActivity(UserModel userModel, String token) {
        //--------========-------------- For Register --------========--------------

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                Common.currentToken = token;
                Common.currentUser = userModel;// Importan,you need always assign value for it!
                startActivity(new Intent(SignUp_Activity.this, HomeActivity.class));
                Common.updateToken(SignUp_Activity.this,task.getResult());
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Common.currentToken = token;
                Common.currentUser = userModel;// Importan,you need always assign value for it!
                startActivity(new Intent(SignUp_Activity.this, HomeActivity.class));
                finish();
                Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }



    @Override
    protected void onStart() {
        super.onStart();


        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        if (user != null) {

                            userRef.child(user.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            if (snapshot.exists()) {

                                                Toast.makeText(SignUp_Activity.this, "You Already Registered!", Toast.LENGTH_SHORT).show();
                                                UserModel userModel = snapshot.getValue(UserModel.class);
                                                gotoHomeActivity(userModel,Common.currentToken);

                                            } else {

                                                //  showRegisterDialog(user);
                                            }
                                            // dialog.hide();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            //  dialog.hide();
                                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                        Toast.makeText(SignUp_Activity.this, "You must Permission to use app", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();


    }
}
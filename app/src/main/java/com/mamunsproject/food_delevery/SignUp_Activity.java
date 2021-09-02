package com.mamunsproject.food_delevery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mamunsproject.food_delevery.Common.Common;
import com.mamunsproject.food_delevery.Model.UserModel;

public class SignUp_Activity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword, etName;
    private DatabaseReference userRef;
    FirebaseUser user;
    private Button signup_button;
    String email, password;
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


        email = etEmail.getText().toString();
        password = etPassword.getText().toString();


    }

    private void gotoHomeActivity(UserModel userModel) {
        //--------========-------------- For Register --------========--------------

        Common.currentUser = userModel;// Importan,you need always assign value for it!
        Log.d("KDJDDDD", "gotoHomeActivity: ");
        //--------========-------------- For Register --------========--------------


        startActivity(new Intent(SignUp_Activity.this, HomeActivity.class));
        finish();
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.


        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("TAGSSS", "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    UserModel userModel = new UserModel();


                                    userModel.setUid(user.getUid());
                                    userModel.setName(etName.getText().toString());
                                    userModel.setAddress(etEmail.getText().toString());
                                    userModel.setPhone("04404");


                                    userRef.child(user.getUid()).setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "Congratulation! Succes", Toast.LENGTH_SHORT).show();

                                                gotoHomeActivity(userModel);
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
                        });

            }
        });


        if (user != null) {
            UserModel userModel = new UserModel();
            userModel.setUid(user.getUid());
            userModel.setName(etName.getText().toString());
            userModel.setAddress(etEmail.getText().toString());
            userModel.setPhone("04404");


            gotoHomeActivity(userModel);
        }
    }


}
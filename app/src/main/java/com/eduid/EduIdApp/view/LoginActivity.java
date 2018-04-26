package com.eduid.EduIdApp.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.eduid.EduIdApp.controller.ActivitiesManager;
import com.eduid.EduIdApp.R;
import com.eduid.EduIdApp.controller.Profile.*;
import com.eduid.EduIdApp.model.EduIdDB;

/**
 * Created by usi on 14.04.16.
 */
public class LoginActivity extends Activity {

    private LoginManagement loginManagement;

    private EditText editTextEmail;
    private EditText editTextPass;
    private Button showPassButton;
    private ImageView barEmail, barPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginManagement = new LoginManagement(this.getApplicationContext());

        editTextEmail = findViewById(R.id.emailEditText);
        editTextPass = findViewById(R.id.passwordEditText);

        showPassButton = findViewById(R.id.hideShowPassword);
        showPassButton.setTag(R.drawable.ico_eye_hidden);

        barEmail = findViewById(R.id.inputBarEmail);
        barPassword = findViewById(R.id.inputBarPass);

        editTextEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    barEmail.setBackgroundResource(R.color.eduIdBlue);
                }else{
                    barEmail.setBackgroundResource(R.color.eduIdGray);
                }
            }
        });

        editTextPass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    barPassword.setBackgroundResource(R.color.eduIdBlue);
                }else {
                    barPassword.setBackgroundResource(R.color.eduIdGray);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * Login button click
         */
        Button loginButton = (Button) this.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                /**
                 * Get data of user
                 */
                final String username = ((EditText) LoginActivity.this.findViewById(R.id.emailEditText)).getText().toString();
                final String password = ((EditText) LoginActivity.this.findViewById(R.id.passwordEditText)).getText().toString();

                /**
                 * Authenticate
                 */
                new Authentication(LoginActivity.this.getApplicationContext()).authenticate(username, password, new AuthenticationCallback() {
                    @Override
                    public void onAuthenticationFinish(boolean authenticateSuccessful) {

                        Context ctx = LoginActivity.this.getApplicationContext();

                        if(authenticateSuccessful){

                            /**
                             * Login successful -> Verify profile -> Open home activity
                             */
//                            new UserProfileVerification(LoginActivity.this.getApplicationContext()).checkUserProfile(new UserProfileVerification.UserProfileVerificationCallback() {
//                                @Override
//                                public void onUserProfileVerificationFinish(boolean verified) {
//                                    if(verified){
//                                        ActivitiesManager.startHomeActivity(LoginActivity.this.getApplicationContext());
//                                        LoginActivity.this.finish();
//
////                                        String name = new EduIdDB(LoginActivity.this.getApplicationContext()).getUserParam("name");
////                                        Toast.makeText(LoginActivity.this.getApplicationContext(), "Profile checked success! Welcome " + name, Toast.LENGTH_LONG).show();
//                                    }
//                                    else{
//                                        Toast.makeText(LoginActivity.this.getApplicationContext(), "Error on check profile, logout!", Toast.LENGTH_LONG).show();
//                                        ActivitiesManager.startLoginActivity(LoginActivity.this.getApplicationContext());
//                                    }
//                                }
//                            });

                            ActivitiesManager.startHomeActivity(LoginActivity.this.getApplicationContext());
                            LoginActivity.this.finish();

                        }
                        else{
                            /**
                             * Login error
                             */
                            Toast.makeText(ctx, ctx.getString(R.string.loginError), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        /**
         * If the user is logged, go on home page
         */
        if(loginManagement.isLogged()){
            ActivitiesManager.startHomeActivity(this.getApplicationContext());
        }

    }

    @Override
    public void onBackPressed() {
        /**
         * Disable back button on activity_login view
         */
    }

    public void hideShowPass(View v){
        Button btn = (Button) v;
        Log.d("EDUID", "button background..." + btn.getBackground().toString());
        if( (Integer) btn.getTag() == R.drawable.ico_eye_hidden ){
            //Toast.makeText(this, "show the pass", Toast.LENGTH_SHORT).show();
            editTextPass.setInputType(InputType.TYPE_CLASS_TEXT);
            btn.setBackgroundResource(R.drawable.ico_eye);
            btn.setScaleX(0.75f);
            btn.setScaleY(0.55f);
            btn.setTag(R.drawable.ico_eye);

        }else if ( (Integer)btn.getTag() == R.drawable.ico_eye ){

            //Toast.makeText(this, "hide the pass", Toast.LENGTH_SHORT).show();
            editTextPass.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btn.setBackgroundResource(R.drawable.ico_eye_hidden);
            btn.setScaleY(0.8f);
            btn.setScaleX(0.8f);
            btn.setTag(R.drawable.ico_eye_hidden);
        }
    }

    public void forgotPassword(View v){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"));
        startActivity(browserIntent);
    }


}

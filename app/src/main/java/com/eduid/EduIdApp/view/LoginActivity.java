package com.eduid.EduIdApp.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginManagement = new LoginManagement(this.getApplicationContext());
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
}

package com.eduid.EduIdApp.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.eduid.EduIdApp.controller.ActivitiesManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        ActivitiesManager.startLoginActivity(this);
    }

}

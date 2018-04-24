package com.eduid.EduIdApp.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.eduid.EduIdApp.R;
import com.eduid.EduIdApp.controller.ActivitiesManager;

public class MainActivity extends AppCompatActivity {

    ImageView splashImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);

        Animation loadingAnimation = new ScaleAnimation(1f, 1.2f, 1f, 1.2f,Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF, 0.5f);
        loadingAnimation.setRepeatMode(Animation.REVERSE);
        loadingAnimation.setRepeatCount(Animation.INFINITE);
        loadingAnimation.setDuration(1200);

         splashImage = findViewById(R.id.splashIcon);
         splashImage.setAnimation(loadingAnimation);
         splashImage.animate();

         final Handler handler = new Handler();
         handler.postDelayed(new Runnable() {
             @Override
             public void run() {
                 splashImage.clearAnimation();
                 ActivitiesManager.startLoginActivity(MainActivity.this);
             }
         }, 3000);


        //ActivitiesManager.startLoginActivity(this);
    }

}

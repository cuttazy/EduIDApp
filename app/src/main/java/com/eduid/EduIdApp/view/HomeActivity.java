package com.eduid.EduIdApp.view;

import android.app.Activity;
import android.content.Intent;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.eduid.EduIdApp.R;
import com.eduid.EduIdApp.controller.ActivitiesManager;
import com.eduid.EduIdApp.controller.Config;
import com.eduid.EduIdApp.model.EduIdDB;
import com.eduid.EduIdApp.model.dataobjects.InfoEntry;
import com.eduid.EduIdApp.model.dataobjects.UserInfo;
import com.eduid.EduIdApp.model.dataobjects.UserInfoAdapter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by usi on 26.08.16.
 */
public class HomeActivity extends Activity {

    private EduIdDB eduIdDB = null;
    private UserInfo userInfo = null;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * Initialize DB and get user data
         */
        userInfo = UserInfo.getUserInfo(this.getApplicationContext());
        if(userInfo == null){
            new EduIdDB(HomeActivity.this).logout();
            ActivitiesManager.startLoginActivity(HomeActivity.this);
            HomeActivity.this.finish();
        }

        TextView fullname = (TextView) findViewById(R.id.userFullName);
        fullname.setText(userInfo.getFullName());
        //TextView userEmailAddress = (TextView) findViewById(R.id.userEmailAddress);
        //userEmailAddress.setText(userInfo.getEmail());

        mListView = findViewById(R.id.userListView);
        UserInfoAdapter adapter = new UserInfoAdapter(this, generateEntriesFromUserInfo());
        mListView.setAdapter(adapter);

        /**
         * Logout
         */
        Button logoutButton = (Button) findViewById(R.id.logoutButton2);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EduIdDB(HomeActivity.this).logout();
                ActivitiesManager.startLoginActivity(HomeActivity.this);
                HomeActivity.this.finish();
            }
        });


        /**
         * Virtual 3rd party app button: to authenticate a service
         */
//        Button virtual3rdButton = (Button) findViewById(R.id.virtual3rdButton);
//        virtual3rdButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent intent = new Intent(getApplicationContext(), SelectServiceActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                getApplicationContext().startActivity(intent);
//
//
//            }
//        });




    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Config.debug(11111 + "-----------------------------------------------------------------------------");
    }

    @Override
    public void onBackPressed() {
        // Nothing
    }


    private EduIdDB getEduIdDB(){
        if(this.eduIdDB == null) {
            this.eduIdDB = new EduIdDB(this.getApplicationContext());
        }
        return this.eduIdDB;
    }

    private ArrayList<InfoEntry> generateEntriesFromUserInfo(){

        ArrayList<InfoEntry> infoEntries = new ArrayList<InfoEntry>(
            Arrays.asList(new InfoEntry("email" , userInfo.getEmail()), new InfoEntry("iss", userInfo.getServer_url()) )
        );
        return  infoEntries;

    }


}

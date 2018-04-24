package com.eduid.EduIdApp.controller;

import android.content.Context;
import android.content.Intent;

import com.eduid.EduIdApp.view.HomeActivity;
import com.eduid.EduIdApp.view.LoginActivity;

/**
 * Created by Yann Cuttaz on 14.04.16.
 */
public class ActivitiesManager {

    private static Context initialContext = null;

    /**
     * Start the activity_login
     * @param context
     */
    public static void startLoginActivity(Context context){
        setInitialContext(context);
        startActivity(LoginActivity.class, -1);
    }

    /**
     * Start the home activity
     * @param context
     */
    public static void startHomeActivity(Context context){
        setInitialContext(context);
        startActivity(HomeActivity.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }


    /**
     * Set initial context
     * @param ctx
     */
    private static void setInitialContext(Context ctx){
        if(initialContext == null){
            initialContext = ctx;
        }
    }

    /**
     * Start an activity
     * @param flags: default -1
     * @param cls
     */
    private static void startActivity(Class<?> cls, int flags){
        Intent intent = new Intent(initialContext, cls);
        if(flags != -1){
            intent.addFlags(flags);
        }
        initialContext.startActivity(intent);
    }

}

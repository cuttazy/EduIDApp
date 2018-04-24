package com.eduid.EduIdApp.controller.Profile;

import android.content.Context;

import com.eduid.EduIdApp.model.EduIdDB;

/**
 * Created by usi on 09.05.16.
 */
public class LoginManagement {

    private EduIdDB eduIdDB = null;

    public LoginManagement(Context ctx){
        if(this.eduIdDB == null)
            this.eduIdDB = new EduIdDB(ctx);
    }

    /**
     * Return if the user is logged in edu-id App
     * @return
     */
    public boolean isLogged(){
        return eduIdDB.isLogged();
    }

    public boolean saveLoginData(String username, String access_token, String token_type, String id_token, int expires_in){
        return eduIdDB.saveLoginData(username, access_token, token_type, id_token, expires_in);
    }

}

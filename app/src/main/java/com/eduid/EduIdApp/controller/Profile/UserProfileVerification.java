package com.eduid.EduIdApp.controller.Profile;

import android.content.Context;

import com.eduid.EduIdApp.model.EduIdDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by usi on 23.06.16.
 */
public class UserProfileVerification {


    private EduIdDB eduIdDB = null;
    private Context currentContext;
    private UserProfileVerificationTask task;
    private UserProfileVerificationCallback callback;

    public UserProfileVerification(Context ctx){
        eduIdDB = new EduIdDB(ctx);
        this.currentContext = ctx;
    }

    public void checkUserProfile(UserProfileVerificationCallback callback){

        this.callback = callback;


        task = new UserProfileVerificationTask();
        task.currentContext = this.currentContext;
        task.callback = new UserProfileVerificationTask.UserProfileVerificationTaskCallback() {
            @Override
            public void onUserProfileVerificationTaskFinish(String json) {

                UserProfileVerification thisObj = UserProfileVerification.this;


//                Config.debug("Result: " + json);

                try {
                    JSONArray responseArray = new JSONArray(json);
                    JSONObject user = responseArray.getJSONObject(0);

                    String userid = user.getString("userid");
                    String mailaddress = user.getString("mailaddress");
                    JSONObject extra = user.getJSONObject("extra");
                    String given_name = extra.getString("given_name");
                    String family_name = extra.getString("family_name");
                    String name = extra.getString("name");

//                    Config.debug(name);

                    /**
                     * Save on local DB
                     */
                    boolean saveSuccess = eduIdDB.saveUserData(userid, mailaddress, given_name, family_name, name);

                    /**
                     * Callback
                     */
                    thisObj.callback.onUserProfileVerificationFinish(true);


                } catch (JSONException e) {
                    thisObj.eduIdDB.logout();
                    thisObj.callback.onUserProfileVerificationFinish(false);
                }

            }
        };


        /**
         * Get params from authentication response
         */
        String access_token = eduIdDB.getAuthenticationParam("access_token");
        String token_type = eduIdDB.getAuthenticationParam("token_type");
        String kid = eduIdDB.getAuthenticationParam("kid");
        String mac_key = eduIdDB.getAuthenticationParam("mac_key");
        String mac_algorithm = eduIdDB.getAuthenticationParam("mac_algorithm");


        task.execute(access_token, token_type, kid, mac_key, mac_algorithm);



    }


    public interface UserProfileVerificationCallback{
        void onUserProfileVerificationFinish(boolean verified);
    }

}

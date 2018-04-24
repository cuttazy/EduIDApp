package com.eduid.EduIdApp.controller.Profile;

import android.content.Context;

import com.eduid.EduIdApp.controller.AppAccessManager.CertificatesManager;
import com.eduid.EduIdApp.controller.Config;
import com.eduid.EduIdApp.controller.ServiceManagement.OpenidConfigurationManager;
import com.eduid.EduIdApp.model.EduIdDB;
import com.nimbusds.jose.jwk.JWK;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.util.List;

/**
 * Created by usi on 03.03.16.
 */
public class Authentication {

    private EduIdDB eduIdDB = null;
    private Context currentContext;
    private AuthenticationTask authenticationTask;
    private String username;
    private String password;
    private AuthenticationCallback callback;

    public Authentication(Context ctx){
        eduIdDB = new EduIdDB(ctx);
        this.currentContext = ctx;
    }

    /**
     * Log the user
     * @param username
     * @param password
     * @param authenticationCallback
     */
    public void authenticate(final String username, final String password, final AuthenticationCallback authenticationCallback){

        OpenidConfigurationManager.loadOIDCConfiguration(new OpenidConfigurationManager.OpenidConfigurationCallback() {
            @Override
            public void onTaskFinish() {


                CertificatesManager.loadCerts(currentContext, new CertificatesManager.CertificatesManagerCallback() {
                    @Override
                    public void onCertificatesLoaded(List<JWK> keys) {
                        callServiceAuth(username, password, keys, authenticationCallback);
                    }

                    @Override
                    public void onCertificatesError() {
                        Config.debug("Error loading certs keys");
                        authenticationCallback.onAuthenticationFinish(false);
                    }
                });


            }

            @Override
            public void onTaskError() {
                authenticationCallback.onAuthenticationFinish(false);
            }
        });


    }

    /**
     * Call authentication service
     * @param username
     * @param password
     * @param authenticationCallback
     */
    private void callServiceAuth(String username, String password, List<JWK> keys, AuthenticationCallback authenticationCallback){
        this.username = username;
        this.password = password;
        authenticationTask = new AuthenticationTask();
        authenticationTask.setCurrentContext(currentContext);
        authenticationTask.certs = keys;
        /**
         * Set callback
         */
        authenticationTask.setAuthenticationCallback(new AuthenticationTask.AuthenticationTaskCallback() {
            @Override
            public void onAuthenticationTaskFinish(String json) {

                final Authentication authentication = Authentication.this;

                /**
                 * Check if json is valid
                 */
                if(json.contains("access_token")){
                    try {
                        JSONObject responseObject = new JSONObject(json);
                        String access_token = responseObject.getString("access_token");
                        int expires_in = responseObject.getInt("expires_in");
                        String token_type = responseObject.getString("token_type");
                        String id_token = responseObject.getString("id_token");

                        /**
                         * Save on local DB
                         */
                        boolean saveSuccess = eduIdDB.saveLoginData(authentication.username, access_token, token_type, id_token, expires_in);

                        /**
                         * Callback
                         */
                        authentication.callback.onAuthenticationFinish(saveSuccess);


                    } catch (JSONException e) {
                        authentication.callback.onAuthenticationFinish(false);
                    }
                }
                else{
                    authentication.callback.onAuthenticationFinish(false);
                }
            }
        });

        String access_token = eduIdDB.getDeviceParam("access_token");
        String token_type = eduIdDB.getDeviceParam("token_type");
        String kid = eduIdDB.getDeviceParam("kid");
        String mac_key = eduIdDB.getDeviceParam("mac_key");
        String mac_algorithm = eduIdDB.getDeviceParam("mac_algorithm");

        this.callback = authenticationCallback;

        /**
         * If params are null, the device is not registred.
         */
//        if(access_token == null || token_type == null || kid == null || mac_key == null){
//
//            this.username = username;
//            this.password = password;
//
//            DeviceRegistration.registerClient(this.currentContext, new DeviceRegistration.DeviceRegistrationCallback() {
//                @Override
//                public void onDeviceRegistrationFinish(boolean success) {
//                    if(success) {
//                        String access_token = eduIdDB.getDeviceParam("access_token");
//                        String token_type = eduIdDB.getDeviceParam("token_type");
//                        String kid = eduIdDB.getDeviceParam("kid");
//                        String mac_key = eduIdDB.getDeviceParam("mac_key");
//                        String mac_algorithm = eduIdDB.getDeviceParam("mac_algorithm");
//                        Authentication.this.authenticationTask.execute(Authentication.this.username, Authentication.this.password, access_token, token_type, kid, mac_key, mac_algorithm);
//                    }
//                    else{
//                        Authentication.this.callback.onAuthenticationFinish(false);
//                    }
//                }
//            });
//
//        }

//        else
            authenticationTask.execute(username, password, access_token, token_type, kid, mac_key, mac_algorithm);
    }

}

package com.eduid.EduIdApp.controller.ServiceManagement;

import com.eduid.EduIdApp.controller.ApiConfig;

/**
 * Created by Yann Cuttaz on 27.07.17.
 */
public class OpenidConfigurationManager {

    private static OpenidConfigurationCallback callback;

    /**
     * Load OIDC configuration into ApiConfig file
     * @param callback
     */
    public static void loadOIDCConfiguration(OpenidConfigurationCallback callback){
        OpenidConfigurationManager.callback = callback;
        OpenidConfigurationTask task = new OpenidConfigurationTask();
        task.callback = new OpenidConfigurationTask.OpenidConfigurationTaskCallback() {
            @Override
            public void onTaskFinish(String token_endpoint, String grant_type, String jwks_endpoint) {
                ApiConfig.TOKEN_ENDPOINT_URL = token_endpoint;
                ApiConfig.GRANT_TYPE = grant_type;
                ApiConfig.JWKS_ENDPOINT_URL = jwks_endpoint;
                OpenidConfigurationManager.callback.onTaskFinish();
            }

            @Override
            public void onTaskError() {
                OpenidConfigurationManager.callback.onTaskError();
            }
        };
        task.execute();
    }

    public interface OpenidConfigurationCallback{
        void onTaskFinish();
        void onTaskError();
    }

}

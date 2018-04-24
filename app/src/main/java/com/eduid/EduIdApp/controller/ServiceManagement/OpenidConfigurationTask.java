package com.eduid.EduIdApp.controller.ServiceManagement;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;

import com.eduid.EduIdApp.controller.ApiConfig;
import com.eduid.EduIdApp.controller.RequestBuilder;
import com.eduid.EduIdApp.model.dataobjects.EduIDService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Yann Cuttaz on 27.07.17.
 */
public class OpenidConfigurationTask extends AsyncTask<String, String, String> {

    OpenidConfigurationTaskCallback callback;

    /**
     *
     * @param params [0]: platform_home_page_url
     * @return
     */
    @Override
    protected String doInBackground(String... params) {
        try{

            String charset = "UTF-8";

            /**
             * Params query
             */
            JSONObject query = new JSONObject();

            /**
             * Create connection
             */
            HttpsURLConnection connection = (HttpsURLConnection) new URL(ApiConfig.OIDC_CONFIGURATION_URL).openConnection();

            /**
             * Read response
             */
            InputStream in = (connection.getResponseCode() >= 400) ? connection.getErrorStream() : connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder result = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                result.append(line);
            }


            return result.toString();

        } catch(Exception e){
            return "";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);


        try{
            JSONObject json = new JSONObject(s);
            String token_endpoint = json.getString("token_endpoint");
            String jwks_endpoint = json.getString("jwks_uri");
            String grant_type = json.getJSONArray("grant_types_supported").getString(json.getJSONArray("grant_types_supported").length()-1);
            callback.onTaskFinish(token_endpoint, grant_type, jwks_endpoint);
        } catch (JSONException e) {
            callback.onTaskError();
        }

//        callback.onTaskFinish(service, "TOKENTOKENTOKEN");

    }

    public interface OpenidConfigurationTaskCallback{
        void onTaskFinish(String token_endpoint, String grant_type, String jwks_endpoint);
        void onTaskError();
    }

}

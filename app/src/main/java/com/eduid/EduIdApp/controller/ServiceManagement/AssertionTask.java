package com.eduid.EduIdApp.controller.ServiceManagement;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;

import com.eduid.EduIdApp.controller.ActivitiesManager;
import com.eduid.EduIdApp.controller.ApiConfig;
import com.eduid.EduIdApp.controller.Config;
import com.eduid.EduIdApp.controller.RequestBuilder;
import com.eduid.EduIdApp.model.EduIdDB;
import com.eduid.EduIdApp.model.dataobjects.EduIDService;
import com.eduid.EduIdApp.model.dataobjects.UserInfo;
import com.eduid.EduIdApp.view.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by usi on 12.05.16.
 */
public class AssertionTask extends AsyncTask<String, String, String> {

    AssertionTaskCallback callback;
    Context context;
    String federationServiceURL;
    String homePageLink;
    EduIDService service;

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

            String deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);


            /**
             * Build authorization
             */
            UserInfo userInfo = UserInfo.getUserInfo(context);
            String assertion = RequestBuilder.buildAssertionRPJWS(userInfo.getId(), deviceID, userInfo.getServer_url(), this.federationServiceURL, context );


            Config.debug("moodle assertion: " + assertion);

            String parameters = "?assertion=" + assertion + "&grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer";

            Config.debug("icorsi mobile service url: " + this.federationServiceURL + parameters);

            /**
             * Create connection
             */
            HttpsURLConnection connection = (HttpsURLConnection) new URL(this.federationServiceURL + parameters).openConnection();
//            connection.setRequestProperty("Authorization", "Bearer " + "{}");
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);
            connection.setDoOutput(false);
            connection.setDoInput(true);

//            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
//            wr.write(query.toString());
            //
//            wr.flush();

            Config.debug("Retrieve assertion code: " + connection.getResponseCode());
            if(connection.getResponseCode() == 403){ // Forbidden, regenerate keys pair and login again
                callback.onTaskError(this.service, true);
            }

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

        Config.debug("assertion: " + s);


        /**
         *
         * {
             "access_token":"N2NhNGRiMWMtOGEzYi00NWI4LThhOGMtZDA2MGI5OGIyYzdizIVkCNEwnGcpCffqyPp85ElPPA0mvxYuU2z04NeCwZry3Mn34yE_CF3P9hvUpZ-gxIyOqxzefOwtkQ9506XHtw",
             "expires_in":3600,
             "token_type":"Bearer",
             "api_key":null
         }
         */



        try{
            JSONObject json = new JSONObject(s);
//            callback.onTaskFinish(this.service, json.getString("access_token"), json.getInt("expires_in"), json.getString("token_type"), json.getString("api_key"));
            callback.onTaskFinish(this.service, s);
        } catch (JSONException e) {
            if(s.length() > 5)
                callback.onTaskError(this.service, false);

        }


    }

    public interface AssertionTaskCallback{
//        void onTaskFinish(EduIDService service, String token, int expires_in, String token_type, String api_key);
        void onTaskFinish(EduIDService service, String jsonResponse);
        void onTaskError(EduIDService service, boolean logout);
    }

}

package com.eduid.EduIdApp.controller.Profile;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;

import com.eduid.EduIdApp.controller.ApiConfig;
import com.eduid.EduIdApp.controller.Config;
import com.eduid.EduIdApp.controller.RequestBuilder;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by usi on 23.06.16.
 */
public class UserProfileVerificationTask extends AsyncTask<String, String, String> {

    public UserProfileVerificationTaskCallback callback;
    public Context currentContext;

    /**
     * Call authentication service
     * @param params: [0]: access_token, [1]: token_type, [2]: kid, [3]: mac_key, [4]: mac_algorithm
     * @return
     */
    @Override
    protected String doInBackground(String... params) {


        try{

            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            } };

            // Install the all-trusting trust manager
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);


            String access_token = params[0];
            String token_type = params[1];
            String kid = params[2];
            String mac_key = params[3];
            String mac_algorithm = params[4];


            String deviceID = Settings.Secure.getString(this.currentContext.getContentResolver(), Settings.Secure.ANDROID_ID);


            String charset = "UTF-8";

            /**
             * Params query
             */
    //            String query = String.format("username=%s&password=%s",
    //                    URLEncoder.encode(params[0], charset),
    //                    URLEncoder.encode(params[1], charset));


            /**
             * DO NOT DELETE !!!!!!!!!!!!!!!!!!!
             */
    //            String passwordParam = SHA1Manager.SHA1(mac_key + "\n" + username + "\n" + SHA1Manager.SHA1(password) + "\n");
    //            String challenge = SHA1Manager.SHA1(clientSecret + mac_key);




            JSONObject authQuery = new JSONObject();
//            authQuery.put("grant_type", "password");


    //            String basicAuth = Base64.encodeToString(new String(clientID + ":" + clientSecret).getBytes(), Base64.NO_WRAP);

            String authorizationAuthentication = RequestBuilder.buildAuthorizationProfileVerification(deviceID, ApiConfig.USER_PROFILE_VERIFICATION_ENDPOINT_URL, kid, mac_key, mac_algorithm);


//                Config.debug(authorizationAuthentication);



            /**
             * Create connection
             */
            CookieHandler.setDefault( new CookieManager( null, CookiePolicy.ACCEPT_ALL ) );
            HttpsURLConnection connection = (HttpsURLConnection) new URL(ApiConfig.USER_PROFILE_VERIFICATION_ENDPOINT_URL).openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + authorizationAuthentication);
            connection.setDoOutput(false);

//            Config.debug(connection.getResponseCode() + connection.getResponseMessage());



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
            Config.debug(e.toString());
            return "error";
        }


    }


    /**
     * Read json response
     * @param result
     */
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        callback.onUserProfileVerificationTaskFinish(result);

    }

    public interface UserProfileVerificationTaskCallback{
        void onUserProfileVerificationTaskFinish(String json);
    }

}

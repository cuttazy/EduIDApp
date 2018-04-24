package com.eduid.EduIdApp.controller.Profile;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.system.Os;
import android.util.Base64;
import android.util.Pair;


import com.eduid.EduIdApp.R;
import com.eduid.EduIdApp.controller.ApiConfig;
import com.eduid.EduIdApp.controller.AppAccessManager.DeviceRegistration;
import com.eduid.EduIdApp.controller.Config;
import com.eduid.EduIdApp.controller.RequestBuilder;
import com.eduid.EduIdApp.controller.SHA1Manager;
import com.eduid.EduIdApp.model.EduIdDB;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyConverter;
import com.nimbusds.jose.jwk.OctetSequenceKey;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.*;

import javax.crypto.SecretKey;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * Created by usi on 09.05.16.
 */
public class AuthenticationTask extends AsyncTask<String, String, String> {

    public AuthenticationTaskCallback authenticationTaskCallback = null;
    private Context currentContext = null;
    public List<JWK> certs;


    public void setAuthenticationCallback(AuthenticationTaskCallback authenticationTaskCallback){
        this.authenticationTaskCallback = authenticationTaskCallback;
    }

    public void setCurrentContext(Context ctx){
        this.currentContext = ctx;
    }


    /**
     * Call authentication service
     * @param params: [0]: username, [1]: password, [2]: access_token, [3]: token_type, [4]: kid, [5]: mac_key, [6]: mac_algorithm
     * @return
     */
    @Override
    protected String doInBackground(String... params){
        try{




            /**
             * TODO: delete trust connections
             */
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




            String username = params[0];
            String password = params[1];
            String access_token = params[2];
            String token_type = params[3];
            String kid = params[4];
            String mac_key = params[5];
            String mac_algorithm = params[6];

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



            // Get a key
            RSAPublicKey rsaPublicKey = (RSAPublicKey) KeyConverter.toJavaKeys(certs).get(0);




            String basicAuth = Base64.encodeToString(new String(RequestBuilder.clientID + ":" + RequestBuilder.clientSecret).getBytes(), Base64.NO_WRAP);

            String assertion = RequestBuilder.buildAssertionAuthenticationJWS(username, "android dev", ApiConfig.TOKEN_ENDPOINT_URL, deviceID,  password, currentContext, rsaPublicKey);

            Config.debug("auth assertion: " + assertion);

            /**
             * Create connection
             */
            HttpsURLConnection connection = (HttpsURLConnection) new URL(ApiConfig.TOKEN_ENDPOINT_URL).openConnection();
            connection.setRequestProperty("Authorization", "Basic " + basicAuth);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);

//            JSONObject authQuery = new JSONObject();
//            authQuery.put("assertion", assertion);

            // Request parameters and other properties.
            HashMap<String, String> postParams = new HashMap<String, String>();
            postParams.put("assertion", assertion);
            postParams.put("grant_type", ApiConfig.GRANT_TYPE);
            postParams.put("scope", "openid profile email");

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            Config.debug(RequestBuilder.getPostDataString(postParams));
            writer.write(RequestBuilder.getPostDataString(postParams));

            writer.flush();
            writer.close();
            os.close();



//            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
//            wr.write(getQuery);
//            wr.flush();

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
    protected void onPostExecute(String result){

        super.onPostExecute(result);

        Config.debug("Auth result: " + result);
        authenticationTaskCallback.onAuthenticationTaskFinish(result);

    }


    public interface AuthenticationTaskCallback{
        void onAuthenticationTaskFinish(String json);
    }



}

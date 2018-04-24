package com.eduid.EduIdApp.controller.AppAccessManager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;

import com.eduid.EduIdApp.R;
import com.eduid.EduIdApp.controller.ApiConfig;
import com.eduid.EduIdApp.controller.Config;
import com.eduid.EduIdApp.controller.Profile.AuthenticationCallback;
import com.eduid.EduIdApp.controller.RequestBuilder;
import com.eduid.EduIdApp.model.EduIdDB;
import com.nimbusds.jose.jwk.JWK;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by usi on 19.05.16.
 */
public class DeviceRegistrationTask extends AsyncTask<String, String, String> {


    public Context context;
    public List<JWK> certs;

    /**
     * Call authentication service
     * @param params: [0]: device_id, [1]: client_name
     * @return
     */
    @Override
    protected String doInBackground(String... params){
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


            String assertion = RequestBuilder.buildAssertionDeviceRegistration(params[0], params[1], ApiConfig.TOKEN_ENDPOINT_URL, context, null);



//            String urlParameters  = "param1=data1&param2=data2&param3=data3";
            String urlParameters  = "grant_type=" + ApiConfig.GRANT_TYPE + "&assertion=" + assertion + "&scope=openid";
            Config.debug("URL: " + urlParameters);
            Config.debug(ApiConfig.GRANT_TYPE);
            byte[] postData = urlParameters.getBytes();
            int postDataLength = postData.length;


            /**
             * Create connection
             */
            HttpsURLConnection connection = (HttpsURLConnection) new URL(ApiConfig.TOKEN_ENDPOINT_URL).openConnection();
            String userCredentials = RequestBuilder.clientID + ":" + RequestBuilder.clientSecret;
            String basicAuth = "Basic " + new String(android.util.Base64.encode(userCredentials.getBytes(), Base64.DEFAULT));
            connection.setRequestProperty("Authorization", basicAuth);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);


            try {
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.write( postData );
            }catch(Exception exc){
                Config.debug("device registration: error writing output on request");
            }

            Config.debug(connection.getResponseCode());

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

        } catch(IOException ioexc){
            Config.debug(ioexc.getCause());
            return "";
        }
        catch(Exception e){
            Config.debug(e.getMessage());
            return "";
        }
    }


    /**
     * Read json response
     * @param result
     */
    @Override
    protected void onPostExecute(String result){

        super.onPostExecute(result);
        Config.debug("device registration result: " + result);

        try {
            JSONObject json = new JSONObject(result);
            String access_token = json.getString("access_token");
            String token_type = json.getString("token_type");
            String kid = json.getString("kid");
            String mac_key = json.getString("mac_key");
            String mac_algorithm = json.getString("mac_algorithm");

            Config.debug("device registration OK");

            DeviceRegistration.callback.onDeviceRegistrationFinish(new EduIdDB(DeviceRegistration.currentContext).registerDevice(access_token, token_type, kid, mac_key, mac_algorithm));

        } catch (JSONException e) {
            Config.debug("Device registration failed: " + DeviceRegistration.currentContext.getString(R.string.errorOnReadJSON));
            DeviceRegistration.callback.onDeviceRegistrationFinish(false);
        }


    }


}

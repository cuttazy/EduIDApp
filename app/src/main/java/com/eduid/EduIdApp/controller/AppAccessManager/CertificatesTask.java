package com.eduid.EduIdApp.controller.AppAccessManager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;

import com.eduid.EduIdApp.R;
import com.eduid.EduIdApp.controller.ApiConfig;
import com.eduid.EduIdApp.controller.Config;
import com.eduid.EduIdApp.controller.RequestBuilder;
import com.eduid.EduIdApp.model.EduIdDB;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyConverter;
import com.nimbusds.jose.jwk.OctetSequenceKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.text.ParseException;

import javax.crypto.SecretKey;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by usi on 19.05.16.
 */
public class CertificatesTask extends AsyncTask<String, String, String> {


    public Context context;
    public CertificatesManager.CertificatesManagerCallback callback;

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
                public X509Certificate[] getAcceptedIssuers() {
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





            String charset = "UTF-8";


            String urlParameters = "";
            Config.debug(ApiConfig.JWKS_ENDPOINT_URL);
            byte[] postData = urlParameters.getBytes();
            int postDataLength = postData.length;


            /**
             * Create connection
             */
            HttpsURLConnection connection = (HttpsURLConnection) new URL(ApiConfig.JWKS_ENDPOINT_URL).openConnection();


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
        Config.debug("JWKS result: " + result);

        try {


            JWKSet localKeys = JWKSet.parse(result);
            this.callback.onCertificatesLoaded(localKeys.getKeys());
        } catch (ParseException e) {
            Config.debug("JWKS error: " + this.context.getString(R.string.errorOnReadJSON));
            this.callback.onCertificatesError();
        }


    }


}

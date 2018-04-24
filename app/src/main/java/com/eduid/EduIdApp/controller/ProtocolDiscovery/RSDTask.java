package com.eduid.EduIdApp.controller.ProtocolDiscovery;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;

import com.eduid.EduIdApp.controller.ApiConfig;
import com.eduid.EduIdApp.controller.Config;
import com.eduid.EduIdApp.controller.RequestBuilder;
import com.eduid.EduIdApp.model.EduIdDB;
import com.eduid.EduIdApp.model.dataobjects.EduIDApi;
import com.eduid.EduIdApp.model.dataobjects.EduIDService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by usi on 11.05.16.
 */
public class RSDTask extends AsyncTask<String, String, String> {

    public GetRSDCallback callback;
    public Context context;
    public String[] protocolList;

    /**
     * Get RSD list
     * @param params:
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





            String charset = "UTF-8";

            /**
             * Params query
             * Protocols are requested by Third Party App
             */
            JSONArray query = new JSONArray();
            for (String protocol :
                    this.protocolList) {
                query.put(protocol);
            }


            /**
             * Create connection
             */

            HttpsURLConnection connection = (HttpsURLConnection) new URL(ApiConfig.RSD_LIST_URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);


            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(query.toString());
            wr.flush();


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

        } catch(Exception e){
            Config.debug(e.getCause());
            return "error";
        }
    }

    /**
     * Send to callback JSON response
     * @param s
     */
    @Override
    protected void onPostExecute(String s) {
        s = s.replace("\n", "");
        super.onPostExecute(s);
        Config.debug("RSD list: " + s);

        try{

            JSONArray jsonArray = new JSONArray(s);
            ArrayList<EduIDService> services = new ArrayList<>();

            /**
             * Get all services from json
             */
            for(int i = 0 ; i < jsonArray.length() ; i++) {

                EduIDService service = new EduIDService();

                JSONObject serviceJSON = (JSONObject) jsonArray.get(i);
                service.setId(i+1);
                service.setEngineName((serviceJSON.has("engineName")) ? serviceJSON.getString("engineName") : "");
                service.setEngineLink((serviceJSON.has("engineLink")) ? serviceJSON.getString("engineLink") : "");
                service.setEngineId((serviceJSON.has("engineId")) ? serviceJSON.getString("engineId") : "");
                service.setHomePageLink((serviceJSON.has("homePageLink")) ? serviceJSON.getString("homePageLink") : "");
                service.setHomePageIcon((serviceJSON.has("homePageIcon")) ? serviceJSON.getString("homePageIcon") : "");
                service.setAuthorization((serviceJSON.has("authorization")) ? serviceJSON.getString("authorization") : "");
                service.setCode((serviceJSON.has("code")) ? serviceJSON.getString("code") : "");
                service.setRedirect_uri((serviceJSON.has("redirect_uri")) ? serviceJSON.getString("redirect_uri") : "");

                /**
                 * Apis
                 */
                JSONObject apis = serviceJSON.getJSONObject("apis");
                Iterator<String> keys = apis.keys();

                do{
                    String key = keys.next(); // Service shortname
                    JSONObject apiJSON = apis.getJSONObject(key);

                    EduIDApi api = new EduIDApi(key, apiJSON.toString(), service.getId());
                    service.addApi(api);

                } while(keys.hasNext());



                services.add(service);

            }

            callback.onGetRSDFinish(services);

        } catch (JSONException e) {
            Config.debug(e.getMessage());
            callback.onGetRSDError("Error on read JSON data.");
        }

    }
}

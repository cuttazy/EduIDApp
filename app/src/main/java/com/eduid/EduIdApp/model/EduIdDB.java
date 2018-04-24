package com.eduid.EduIdApp.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.eduid.EduIdApp.controller.Config;
import com.eduid.EduIdApp.model.dataobjects.EduIDService;
import com.eduid.EduIdApp.model.dataobjects.EduIDApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by usi on 19.04.16.
 */
public class EduIdDB extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "EduIdDB";
    private Context currentContext = null;

    /**
     * Settings table
     */
    private static final String DICTIONARY_TABLE_SETTINGS_NAME = "settings";
    private static final String DICTIONARY_TABLE_SETTINGS_CREATE =
            "CREATE TABLE IF NOT EXISTS " + DICTIONARY_TABLE_SETTINGS_NAME + "(" +
                    "key_name TEXT PRIMARY KEY NOT NULL," +
                    "data_value TEXT);";

    /**
     * Platforms table
     */
    private static final String SERVICES_TABLE_NAME = "services";
    private static final String SERVICES_CREATE_SQL =
            "CREATE TABLE IF NOT EXISTS " + SERVICES_TABLE_NAME + "(" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "engineName TEXT NOT NULL," +
                    "engineLink TEXT," +
                    "engineId TEXT," +
                    "homePageLink TEXT," +
                    "homePageIcon TEXT," +
                    "authorization TEXT," +
                    "redirect_uri TEXT," +
                    "code TEXT," +
                    "forward_assertion_access_token TEXT," +
                    "forward_assertion_refresh_token TEXT," +
                    "forward_assertion_kid TEXT," +
                    "forward_assertion_sign_key TEXT," +
                    "forward_assertion_algorithm TEXT," +
                    "forward_assertion_token_type TEXT);";
    /**
     * Services table
     */
    private static final String APIS_TABLE_NAME = "apis";
    private static final String APIS_CREATE_SQL =
            "CREATE TABLE IF NOT EXISTS " + APIS_TABLE_NAME + "(" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "apiVersion TEXT," +
                    "apiLink TEXT," +
                    "engineId TEXT," +
                    "docs TEXT," +
                    "preferred TEXT," +
                    "description TEXT," +
                    "notes TEXT," +
                    "settings TEXT," +
                    "transport TEXT," +
                    "id_platform INTEGER NOT NULL," +
                    "token_type TEXT," +
                    "authorization TEXT);";

    public EduIdDB(Context ctx){
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        this.currentContext = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.beginTransaction();

            db.execSQL(DICTIONARY_TABLE_SETTINGS_CREATE);
            Config.debug("Settings table created");
            db.execSQL(SERVICES_CREATE_SQL);
            Config.debug("Services table created");
            db.execSQL(APIS_CREATE_SQL);
            Config.debug("Apis table created");

            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Update DB
    }
    /**
     * Save the result json of the forward assertion
     * @param platformID
     * @param json
     * @return
     */
    public boolean saveForwardAssertion(int platformID, String json){
        SQLiteDatabase db = getWritableDatabase();
        boolean ret = false;
        try {

            db.beginTransaction();

            JSONObject obj = new JSONObject(json);
            String access_token = obj.getString("access_token");
            String refresh_token = obj.getString("refresh_token");
            String kid = obj.getString("kid");
            String sign_key = obj.getString("sign_key");
            String algorithm = obj.getString("algorithm");
            String token_type = obj.getString("token_type");

            String query = "UPDATE " + SERVICES_TABLE_NAME + " SET " +
                    "forward_assertion_access_token = '" + access_token + "', " +
                    "forward_assertion_refresh_token = '" + refresh_token + "', " +
                    "forward_assertion_kid = '" + kid + "', " +
                    "forward_assertion_sign_key = '" + sign_key + "', " +
                    "forward_assertion_algorithm = '" + algorithm + "', " +
                    "forward_assertion_token_type = '" + token_type + "' " +
                    "WHERE ID = '" + platformID + "'";

            db.execSQL(query);

            db.setTransactionSuccessful();

            ret = true;

        } catch (JSONException e) {
            // Nothing -> Return false
        } finally {
            db.endTransaction();
            db.close();
        }

        return ret;
    }

    /**
     * Save the result json of the retrieve assertion
     * @param federationServiceURL
     * @param json
     * @return
     */
    public boolean saveRetrieveAssertion(String federationServiceURL, String json){

        SQLiteDatabase db = getWritableDatabase();
        boolean ret = false;
        try {

            db.beginTransaction();

            JSONObject obj = new JSONObject(json);
            String code = obj.getString("code");
            String redirect_uri = obj.getString("redirect_uri").replace("\\", "");

            String query = "UPDATE " + SERVICES_TABLE_NAME + " SET redirect_uri = '" + redirect_uri + "', code = '" + code + "' WHERE homePageLink = '" + federationServiceURL + "'";

            db.execSQL(query);

            db.setTransactionSuccessful();

            ret = true;

        } catch (JSONException e) {
            // Nothing -> Return false
        } finally {
            db.endTransaction();
            db.close();
        }

        return ret;

    }



    /**
     * Delete all user data, tokens and logged from local DB
     * @return logout success
     */
    public boolean logout(){
        SQLiteDatabase db = getWritableDatabase();
        boolean ret = false;
        try {
            db.beginTransaction();

            /**
             * Delete old login data
             */
            String query = "DELETE FROM " + DICTIONARY_TABLE_SETTINGS_NAME + " " +
                    "WHERE key_name = 'username' " +
                    "OR key_name LIKE 'authentication_%' " +
                    "OR key_name LIKE 'device_%' " +
                    "OR key_name LIKE 'user_%' OR key_name = 'logged';";
            db.execSQL(query);

            db.setTransactionSuccessful();

            ret = true;
        } finally {
            db.endTransaction();
        }

        return ret;
    }


    /**
     * Save User data when User Profile is verified
     * @param userid
     * @param mailaddress
     * @param given_name
     * @param family_name
     * @param name
     * @return
     */
    public boolean saveUserData(String userid, String mailaddress, String given_name, String family_name, String name){
        SQLiteDatabase db = getWritableDatabase();
        boolean ret = false;
        try {
            db.beginTransaction();

            /**
             * Delete old login data
             */
            String query = "DELETE FROM " + DICTIONARY_TABLE_SETTINGS_NAME + " " +
                    "WHERE key_name LIKE 'user_%';";
            db.execSQL(query);

            /**
             * Insert new records
             */
            ContentValues cv1 = new ContentValues();
            cv1.put("key_name", "user_userid");
            cv1.put("data_value", userid);
            db.insert(DICTIONARY_TABLE_SETTINGS_NAME, null, cv1);

            cv1 = new ContentValues();
            cv1.put("key_name", "user_mailaddress");
            cv1.put("data_value", mailaddress);
            db.insert(DICTIONARY_TABLE_SETTINGS_NAME, null, cv1);

            cv1 = new ContentValues();
            cv1.put("key_name", "user_given_name");
            cv1.put("data_value", given_name);
            db.insert(DICTIONARY_TABLE_SETTINGS_NAME, null, cv1);

            cv1 = new ContentValues();
            cv1.put("key_name", "user_family_name");
            cv1.put("data_value", family_name);
            db.insert(DICTIONARY_TABLE_SETTINGS_NAME, null, cv1);

            cv1 = new ContentValues();
            cv1.put("key_name", "user_name");
            cv1.put("data_value", name);
            db.insert(DICTIONARY_TABLE_SETTINGS_NAME, null, cv1);

            db.setTransactionSuccessful();

            ret = true;
        } finally {
            db.endTransaction();
        }

        return ret;
    }

    /**
     * Save login data
     * @return
     */
    public boolean saveLoginData(String username, String access_token, String token_type, String id_token, int expires_in){
        SQLiteDatabase db = getWritableDatabase();
        boolean ret = false;
        try {
            db.beginTransaction();

            /**
             * Delete old login data
             */
            String query = "DELETE FROM " + DICTIONARY_TABLE_SETTINGS_NAME + " " +
                    "WHERE key_name = 'username' " +
                    "OR key_name LIKE 'authentication_%' " +
                    "OR key_name = 'logged';";
            db.execSQL(query);

            /**
             * Insert new records
             */
            ContentValues cv1 = new ContentValues();
            cv1.put("key_name", "username");
            cv1.put("data_value", username);
            db.insert(DICTIONARY_TABLE_SETTINGS_NAME, null, cv1);

            ContentValues cv3 = new ContentValues();
            cv3.put("key_name", "authentication_access_token");
            cv3.put("data_value", access_token);
            db.insert(DICTIONARY_TABLE_SETTINGS_NAME, null, cv3);

            ContentValues cv4 = new ContentValues();
            cv4.put("key_name", "authentication_token_type");
            cv4.put("data_value", token_type);
            db.insert(DICTIONARY_TABLE_SETTINGS_NAME, null, cv4);

            cv4 = new ContentValues();
            cv4.put("key_name", "authentication_id_token");
            cv4.put("data_value", id_token);
            db.insert(DICTIONARY_TABLE_SETTINGS_NAME, null, cv4);

            cv4 = new ContentValues();
            cv4.put("key_name", "authentication_expires_in");
            cv4.put("data_value", expires_in);
            db.insert(DICTIONARY_TABLE_SETTINGS_NAME, null, cv4);

            cv4 = new ContentValues();
            cv4.put("key_name", "logged");
            cv4.put("data_value", "true");
            db.insert(DICTIONARY_TABLE_SETTINGS_NAME, null, cv4);

            db.setTransactionSuccessful();

            ret = true;
        } finally {
            db.endTransaction();
        }

        return ret;
    }

    /**
     * Get Service Discovery Authorization Token
     * @return: the token or null if some data is not correct
     */
//    public String getServiceDiscoveryAuthToken(){
//
//        String kid = selectFromSettingsTable("kid");
//        String macKey = selectFromSettingsTable("macKey");
//
//        if(kid == null || macKey == null){
//            return null;
//        }else{
//            return "MAC kid=" + kid + ",mac=" + macKey;
//        }
//    }

    /**
     * Get Authentication param
     */
    public String getAuthenticationParam(String param){
        return selectFromSettingsTable("authentication_" + param);
    }

    public String getForwardAssertionParam(int platformID, String param){
        return getPlatformParam(platformID, "forward_assertion_" + param);
    }

    public String getPlatformParam(int platformID, String param){
        return selectFromServicesTable(platformID, param);
    }


    /**
     * Return if the user is logged in EduIdApp
     * @return
     */
    public boolean isLogged(){
        String logged = selectFromSettingsTable("logged");
        return (logged == null) ? false : Boolean.valueOf(logged);
    }

    public EduIDService getEduIDServiceByID(int eduIDServiceID, boolean selectApis){

        EduIDService service = null;


        String selectQuery = "SELECT * FROM " + SERVICES_TABLE_NAME + " WHERE ID=" + eduIDServiceID;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                service = new EduIDService();

                service.setId(cursor.getInt(cursor.getColumnIndex("ID")));
                service.setEngineName(cursor.getString(cursor.getColumnIndex("engineName")));
                service.setEngineId(cursor.getString(cursor.getColumnIndex("engineId")));
                service.setEngineLink(cursor.getString(cursor.getColumnIndex("engineLink")));
                service.setHomePageLink(cursor.getString(cursor.getColumnIndex("homePageLink")));
                service.setHomePageIcon(cursor.getString(cursor.getColumnIndex("homePageIcon")));
                service.setAuthorization(cursor.getString(cursor.getColumnIndex("authorization")));
                service.setCode(cursor.getString(cursor.getColumnIndex("code")));
                service.setRedirect_uri(cursor.getString(cursor.getColumnIndex("redirect_uri")));

                /**
                 * Get apis
                 */
                if (selectApis) {
                    String selectServices = "SELECT * FROM " + APIS_TABLE_NAME + " WHERE id_platform = '" + service.getId() + "'";
                    Cursor servicesCursor = database.rawQuery(selectServices, null);
                    if (servicesCursor.moveToFirst()) {
                        do {

                            EduIDApi api = new EduIDApi();
                            api.setId(servicesCursor.getInt(servicesCursor.getColumnIndex("ID")));
                            api.setName(servicesCursor.getString(servicesCursor.getColumnIndex("name")));
                            api.setApiVersion(servicesCursor.getString(servicesCursor.getColumnIndex("apiVersion")));
                            api.setApiLink(servicesCursor.getString(servicesCursor.getColumnIndex("apiLink")));
                            api.setEngineId(servicesCursor.getString(servicesCursor.getColumnIndex("engineId")));
                            api.setDocs(servicesCursor.getString(servicesCursor.getColumnIndex("docs")));
                            api.setPreferred(Boolean.valueOf(servicesCursor.getString(servicesCursor.getColumnIndex("preferred"))));
                            api.setDescription(servicesCursor.getString(servicesCursor.getColumnIndex("description")));
                            api.setNotes(servicesCursor.getString(servicesCursor.getColumnIndex("notes")));
                            api.setSettings(servicesCursor.getString(servicesCursor.getColumnIndex("settings")));
                            api.setTransport(servicesCursor.getString(servicesCursor.getColumnIndex("transport")));
                            api.setId_platform(servicesCursor.getInt(servicesCursor.getColumnIndex("id_platform")));
                            api.setToken_type(servicesCursor.getString(servicesCursor.getColumnIndex("token_type")));
                            api.setAuthorization(servicesCursor.getString(servicesCursor.getColumnIndex("authorization")));

                            service.addApi(api);

                        } while (servicesCursor.moveToNext());
                    }
                }
            }
            while (cursor.moveToNext()) ;
        }



        return service;
    }

    public ArrayList<EduIDService> getServices(){
        ArrayList<EduIDService> platforms = new ArrayList<EduIDService>();



        String selectQuery = "SELECT * FROM " + SERVICES_TABLE_NAME;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                EduIDService service = new EduIDService();
                service.setId(cursor.getInt(cursor.getColumnIndex("ID")));
                service.setEngineName(cursor.getString(cursor.getColumnIndex("engineName")));
                service.setEngineId(cursor.getString(cursor.getColumnIndex("engineId")));
                service.setEngineLink(cursor.getString(cursor.getColumnIndex("engineLink")));
                service.setHomePageLink(cursor.getString(cursor.getColumnIndex("homePageLink")));
                service.setHomePageIcon(cursor.getString(cursor.getColumnIndex("homePageIcon")));
                service.setAuthorization(cursor.getString(cursor.getColumnIndex("authorization")));
                service.setCode(cursor.getString(cursor.getColumnIndex("code")));
                service.setRedirect_uri(cursor.getString(cursor.getColumnIndex("redirect_uri")));

                /**
                 * Get apis for each platform
                 */
                String selectServices = "SELECT * FROM " + APIS_TABLE_NAME + " WHERE id_platform = '" + service.getId() + "'";
                Cursor servicesCursor = database.rawQuery(selectServices, null);
                if(servicesCursor.moveToFirst()){
                    do{

                        EduIDApi api = new EduIDApi();
                        api.setId(servicesCursor.getInt(servicesCursor.getColumnIndex("ID")));
                        api.setName(servicesCursor.getString(servicesCursor.getColumnIndex("name")));
                        api.setApiVersion(servicesCursor.getString(servicesCursor.getColumnIndex("apiVersion")));
                        api.setApiLink(servicesCursor.getString(servicesCursor.getColumnIndex("apiLink")));
                        api.setEngineId(servicesCursor.getString(servicesCursor.getColumnIndex("engineId")));
                        api.setDocs(servicesCursor.getString(servicesCursor.getColumnIndex("docs")));
                        api.setPreferred(Boolean.valueOf(servicesCursor.getString(servicesCursor.getColumnIndex("preferred"))));
                        api.setDescription(servicesCursor.getString(servicesCursor.getColumnIndex("description")));
                        api.setNotes(servicesCursor.getString(servicesCursor.getColumnIndex("notes")));
                        api.setSettings(servicesCursor.getString(servicesCursor.getColumnIndex("settings")));
                        api.setTransport(servicesCursor.getString(servicesCursor.getColumnIndex("transport")));
                        api.setId_platform(servicesCursor.getInt(servicesCursor.getColumnIndex("id_platform")));
                        api.setToken_type(servicesCursor.getString(servicesCursor.getColumnIndex("token_type")));
                        api.setAuthorization(servicesCursor.getString(servicesCursor.getColumnIndex("authorization")));

                        service.addApi(api);

                    } while (servicesCursor.moveToNext());
                }

                platforms.add(service);


            } while (cursor.moveToNext());
        }



        return platforms;
    }

    public ArrayList<EduIDApi> getApisFromServiceID(int platformID){

        SQLiteDatabase database = this.getWritableDatabase();
        ArrayList<EduIDApi> services = new ArrayList<>();

        /**
         * Get services for each platform
         */
        String selectServices = "SELECT * FROM " + APIS_TABLE_NAME + " WHERE id_platform = '" + platformID + "'";
        Cursor servicesCursor = database.rawQuery(selectServices, null);
        if(servicesCursor.moveToFirst()){
            do{

                EduIDApi api = new EduIDApi();
                api.setId(servicesCursor.getInt(servicesCursor.getColumnIndex("ID")));
                api.setName(servicesCursor.getString(servicesCursor.getColumnIndex("name")));
                api.setApiVersion(servicesCursor.getString(servicesCursor.getColumnIndex("apiVersion")));
                api.setApiLink(servicesCursor.getString(servicesCursor.getColumnIndex("apiLink")));
                api.setEngineId(servicesCursor.getString(servicesCursor.getColumnIndex("engineId")));
                api.setDocs(servicesCursor.getString(servicesCursor.getColumnIndex("docs")));
                api.setPreferred(Boolean.valueOf(servicesCursor.getString(servicesCursor.getColumnIndex("preferred"))));
                api.setDescription(servicesCursor.getString(servicesCursor.getColumnIndex("description")));
                api.setNotes(servicesCursor.getString(servicesCursor.getColumnIndex("notes")));
                api.setSettings(servicesCursor.getString(servicesCursor.getColumnIndex("settings")));
                api.setTransport(servicesCursor.getString(servicesCursor.getColumnIndex("transport")));
                api.setId_platform(servicesCursor.getInt(servicesCursor.getColumnIndex("id_platform")));
                api.setToken_type(servicesCursor.getString(servicesCursor.getColumnIndex("token_type")));
                api.setAuthorization(servicesCursor.getString(servicesCursor.getColumnIndex("authorization")));

                services.add(api);

            } while (servicesCursor.moveToNext());
        }

        return services;
    }

    /**
     * Save RSD json in local DB
     * @param json
     * @return
     */
    public boolean saveRSD(String json){

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        boolean ret = false;

        try {

            /**
             * First, try to get json response
             */
            JSONArray jsonArray = new JSONArray(json);

            /**
             * Delete old data
             */
            String query = "DELETE FROM " + SERVICES_TABLE_NAME + ";";
            db.execSQL(query);
            query = "DELETE FROM " + APIS_TABLE_NAME + ";";
            db.execSQL(query);

            /**
             * Reset index in autoincrement fields
             */
            query = "DELETE FROM sqlite_sequence where name='" + SERVICES_TABLE_NAME + "';";
            db.execSQL(query);
            query = "DELETE FROM sqlite_sequence where name='" + APIS_TABLE_NAME + "';";
            db.execSQL(query);

            for(int i = 0 ; i < jsonArray.length() ; i++){

                JSONObject serviceJSON = (JSONObject) jsonArray.get(i);


                /**
                 * Insert new records
                 */
                ContentValues cv = new ContentValues();
                cv.put("id", (i+1));
                cv.put("engineName", (serviceJSON.has("engineName")) ? serviceJSON.getString("engineName") : "");
                cv.put("engineLink", serviceJSON.getString("engineLink"));
                cv.put("engineId",  (serviceJSON.has("engineId")) ? serviceJSON.getString("engineId") : "");
                cv.put("homePageLink", (serviceJSON.has("homePageLink")) ? serviceJSON.getString("homePageLink").replace("\\", "") : "");
                cv.put("homePageIcon", (serviceJSON.has("homePageIcon")) ? serviceJSON.getString("homePageIcon").replace("\\", "") : "");
                cv.put("access_token", (serviceJSON.has("access_token")) ? serviceJSON.getString("access_token") : "");
                cv.put("refresh_token", (serviceJSON.has("refresh_token")) ? serviceJSON.getString("refresh_token") : "");
                cv.put("code", (serviceJSON.has("code")) ? serviceJSON.getString("code") : "");
                cv.put("redirect_uri", (serviceJSON.has("redirect_uri")) ? serviceJSON.getString("redirect_uri") : "");
                db.insert(SERVICES_TABLE_NAME, null, cv);

                /**
                 * Apis
                 */
                JSONObject apis = serviceJSON.getJSONObject("apis");
                Iterator<String> keys = apis.keys();

                do{
                    String key = keys.next(); // Service shortname
                    JSONObject api = apis.getJSONObject(key);

                    /**
                     * Save api
                     */
                    cv = new ContentValues();
                    cv.put("name", key);
                    cv.put("apiVersion", api.has("apiVersion") ? api.getString("apiVersion") : "");
                    cv.put("apiLink", api.has("apiLink") ? api.getString("apiLink") : "");
                    cv.put("engineId", api.has("engineId") ? api.getString("engineId") : "");
                    cv.put("docs", api.has("docs") ? api.getString("docs") : "");
                    cv.put("preferred", api.has("preferred") ? api.getString("preferred") : "");
                    cv.put("description", api.has("description") ? api.getString("description") : "");
                    cv.put("notes", api.has("notes") ? api.getString("notes") : "");
                    cv.put("settings", api.has("settings") ? api.getString("settings") : "");
                    cv.put("transport", api.has("transport") ? api.getString("transport") : "");
                    cv.put("token_type", api.has("token_type") ? api.getString("token_type") : "");
                    cv.put("authorization", api.has("authorization") ? api.getString("authorization") : "");
                    cv.put("id_platform", (i+1));
                    db.insert(APIS_TABLE_NAME, null, cv);



                } while(keys.hasNext());




            }

            db.setTransactionSuccessful();

            ret = true;

        } catch (JSONException e) {
            Config.debug(e.getMessage());
//            Config.debug(currentContext.getString(R.string.errorOnReadJSON));
        } catch(Exception exc){
            Config.debug(exc.getMessage());
        }finally {
            db.endTransaction();
            db.close();
        }

        return ret;

    }



    /**
     * Register current device on edu-id Service
     * @return
     */
    public boolean registerDevice(String access_token, String token_type, String kid, String mac_key, String mac_algorithm){
        SQLiteDatabase db = getWritableDatabase();
        boolean ret = false;
        try {
            db.beginTransaction();

            /**
             * Delete old login data
             */
            String query = "DELETE FROM " + DICTIONARY_TABLE_SETTINGS_NAME + " " +
                    "WHERE key_name LIKE 'device_%';";
            db.execSQL(query);

            /**
             * Insert new records
             */
            ContentValues cv1 = new ContentValues();
            cv1.put("key_name", "device_access_token");
            cv1.put("data_value", access_token);
            db.insert(DICTIONARY_TABLE_SETTINGS_NAME, null, cv1);

            cv1 = new ContentValues();
            cv1.put("key_name", "device_token_type");
            cv1.put("data_value", token_type);
            db.insert(DICTIONARY_TABLE_SETTINGS_NAME, null, cv1);

            cv1 = new ContentValues();
            cv1.put("key_name", "device_kid");
            cv1.put("data_value", kid);
            db.insert(DICTIONARY_TABLE_SETTINGS_NAME, null, cv1);

            cv1 = new ContentValues();
            cv1.put("key_name", "device_mac_key");
            cv1.put("data_value", mac_key);
            db.insert(DICTIONARY_TABLE_SETTINGS_NAME, null, cv1);

            cv1 = new ContentValues();
            cv1.put("key_name", "device_mac_algorithm");
            cv1.put("data_value", mac_algorithm);
            db.insert(DICTIONARY_TABLE_SETTINGS_NAME, null, cv1);

            db.setTransactionSuccessful();

            ret = true;
        } finally {
            db.endTransaction();
        }

        return ret;
    }


    /**
     * Get a param from the result of device registration request
     * @param param
     * @return
     */
    public String getDeviceParam(String param){
        return selectFromSettingsTable("device_" + param);
    }

    /**
     * Get a param from the result of profile verification request
     * @param param
     * @return
     */
    public String getUserParam(String param){
        return selectFromSettingsTable("user_" + param);
    }

    /**
     * Select from settings table
     * @param keyName
     * @return
     */
    public String selectFromSettingsTable(String keyName){
        String ret = null;

        String selectQuery = "SELECT * FROM " + DICTIONARY_TABLE_SETTINGS_NAME + " WHERE key_name = '" + keyName + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                ret = cursor.getString(cursor.getColumnIndex("data_value"));
            } while (cursor.moveToNext());
        }

        return ret;
    }

    /**
     * Select from platforms table
     * @return
     */
    public String selectFromServicesTable(int platformID, String fieldName){
        String ret = null;

        String selectQuery = "SELECT " + fieldName + " FROM " + SERVICES_TABLE_NAME + " WHERE ID = '" + platformID + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                ret = cursor.getString(0);
            } while (cursor.moveToNext());
        }

        return ret;
    }


    /**
     * Get platform id form the home page url
     * @param homePageURL
     * @return
     */
    public int getServiceIDFromHomePageURL(String homePageURL){
        int ret = 0;

        String selectQuery = "SELECT ID FROM " + SERVICES_TABLE_NAME + " WHERE homePageLink = '" + homePageURL + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                ret = cursor.getInt(0);
            } while (cursor.moveToNext());
        }

        return ret;
    }

    /**
     * Get platform id from protocol
     * @param protocol
     * @return
     */
    public int[] getServicesIDFromProtocol(String protocol){
        int[] ret = null;

        String selectQuery = "SELECT id_platform FROM " + APIS_TABLE_NAME + " WHERE name = '" + protocol + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        ret = new int[cursor.getCount()];
        if (cursor.moveToFirst()) {
            do {
                ret[cursor.getPosition()] = cursor.getInt(0);
            } while (cursor.moveToNext());
        }

        return ret;
    }


}

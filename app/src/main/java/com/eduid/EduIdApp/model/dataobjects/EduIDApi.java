package com.eduid.EduIdApp.model.dataobjects;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by usi on 19.07.16.
 */
public class EduIDApi {

    private static int indexID = 0;

    private int id;
    private String name;
    private String apiVersion;
    private String apiLink;
    private String engineId;
    private String docs;
    private boolean preferred;
    private String description;
    private String notes;
    private Object settings;
    private String transport;
    private int id_platform;
    private String token_type;
    private String authorization;



    public EduIDApi(){}
    public EduIDApi(String key, String json, int id_platform) throws JSONException {
        indexID++;
        JSONObject jsonObject = new JSONObject(json);
        this.setId((jsonObject.has("id")) ? jsonObject.getInt("id") : indexID);
        this.setName(key);
//        this.setName((jsonObject.has("name")) ? jsonObject.getString("name") : "");
        this.setApiVersion((jsonObject.has("apiVersion")) ? jsonObject.getString("apiVersion") : "");
        this.setApiLink((jsonObject.has("apiLink")) ? jsonObject.getString("apiLink") : "");
        this.setEngineId((jsonObject.has("engineId")) ? jsonObject.getString("engineId") : "");
        this.setDocs((jsonObject.has("docs")) ? jsonObject.getString("docs") : "");
        this.setPreferred((jsonObject.has("preferred")) ? jsonObject.getBoolean("preferred") : false);
        this.setDescription((jsonObject.has("description")) ? jsonObject.getString("description") : "");
        this.setNotes((jsonObject.has("notes")) ? jsonObject.getString("notes") : "");
        this.setSettings((jsonObject.has("settings")) ? jsonObject.getString("settings") : "");
        this.setTransport((jsonObject.has("transport")) ? jsonObject.getString("transport") : "");
        this.setId_platform(id_platform);
        this.setToken_type((jsonObject.has("token_type")) ? jsonObject.getString("token_type") : "");
        this.setAuthorization((jsonObject.has("authorization")) ? jsonObject.getString("authorization") : "");

    }

    public JSONObject toJSON(){

        JSONObject ret = new JSONObject();
        try {

            ret.put("id", this.getId());
            ret.put("name", this.getName());
            ret.put("apiVersion", this.getApiVersion());
            ret.put("apiLink", this.getApiLink());
            ret.put("engineId", this.getEngineId());
            ret.put("docs", this.getDocs());
            ret.put("preferred", this.isPreferred());
            ret.put("description", this.getDescription());
            ret.put("notes", this.getNotes());
            ret.put("settings", this.getSettings());
            ret.put("transport", this.getTransport());
            ret.put("id_platform", this.getId_platform());
            ret.put("token_type", this.getToken_type());
            ret.put("authorization", this.getAuthorization());

            return ret;

        } catch (JSONException e) {
            return ret;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiLink() {
        return apiLink;
    }

    public void setApiLink(String apiLink) {
        this.apiLink = apiLink;
    }

    public String getEngineId() {
        return engineId;
    }

    public void setEngineId(String engineId) {
        this.engineId = engineId;
    }

    public String getDocs() {
        return docs;
    }

    public void setDocs(String docs) {
        this.docs = docs;
    }

    public boolean isPreferred() {
        return preferred;
    }

    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Object getSettings() {
        return settings;
    }

    public void setSettings(Object settings) {
        this.settings = settings;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public int getId_platform() {
        return id_platform;
    }

    public void setId_platform(int id_platform) {
        this.id_platform = id_platform;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }
}

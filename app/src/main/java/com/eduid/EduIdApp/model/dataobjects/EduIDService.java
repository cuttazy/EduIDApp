package com.eduid.EduIdApp.model.dataobjects;

import com.eduid.EduIdApp.controller.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by usi on 19.07.16.
 */
public class EduIDService {

    private int id;
    private String engineName;
    private String engineLink;
    private String engineId;
    private String homePageLink;
    private String homePageIcon;
    private String authorization;
    private String code;
    private String redirect_uri;
    private ArrayList<EduIDApi> apis;

    public EduIDService(){
        this.setApis(new ArrayList<EduIDApi>());
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();
        try {
            json.put("id", this.getId());
            json.put("engineName", this.getEngineName());
            json.put("engineLink", this.getEngineLink());
            json.put("engineId", this.getEngineId());
            json.put("homePageLink", this.getHomePageLink());
            json.put("homePageIcon", this.getHomePageIcon());
            json.put("authorization", this.getAuthorization());
            json.put("redirect_uri", this.getRedirect_uri());
            json.put("code", this.getCode());

            JSONArray apisJSON = new JSONArray();

            for (int i = 0 ; i < this.getApis().size() ; i++) {
                apisJSON.put(i, this.getApis().get(i).toJSON());
            }

            json.put("apis", apisJSON);

        } catch (JSONException e) {
            Config.debug("Error on build json object.");
        }
        return json;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public String getEngineLink() {
        return engineLink;
    }

    public void setEngineLink(String engineLink) {
        this.engineLink = engineLink;
    }

    public String getEngineId() {
        return engineId;
    }

    public void setEngineId(String engineId) {
        this.engineId = engineId;
    }

    public String getHomePageLink() {
        return homePageLink;
    }

    public void setHomePageLink(String homePageLink) {
        this.homePageLink = homePageLink;
    }

    public String getHomePageIcon() {
        return homePageIcon;
    }

    public void setHomePageIcon(String homePageIcon) {
        this.homePageIcon = homePageIcon;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public ArrayList<EduIDApi> getApis() {
        return apis;
    }

    public void setApis(ArrayList<EduIDApi> apis) {
        this.apis = apis;
    }

    public void addApi(EduIDApi api){
        this.apis.add(api);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRedirect_uri() {
        return redirect_uri;
    }

    public void setRedirect_uri(String redirect_uri) {
        this.redirect_uri = redirect_uri;
    }

    @Override
    public String toString() {
        return this.getEngineName();
    }

}

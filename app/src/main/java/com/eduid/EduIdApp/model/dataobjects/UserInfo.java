package com.eduid.EduIdApp.model.dataobjects;

import android.content.Context;

import com.eduid.EduIdApp.controller.Config;
import com.eduid.EduIdApp.model.EduIdDB;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by usi on 26.08.16.
 */
public class UserInfo {

    private String id;
    private String name;
    private String surname;
    private String email;
    private String server_url;

    public UserInfo(){}

    public UserInfo(String id, String name, String surname, String email, String server_url){
        this.setId(id);
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.setServer_url(server_url);
    }

    public static UserInfo getUserInfo(Context ctx){

        String jwt = new EduIdDB(ctx).getAuthenticationParam("id_token");

        try {

            String payloadString = jwt.split("\\.")[1];
            JSONObject payload = new JSONObject( new String(android.util.Base64.decode(payloadString, android.util.Base64.DEFAULT)));

//            Config.debug("id_token: " + payload.toString());

            return new UserInfo(
                    payload.getString("preferred_username"),
                    payload.getString("given_name"),
                    payload.getString("family_name"),
                    payload.getString("email"),
                    payload.getString("iss")
            );
        }catch (JSONException e) {
            Config.debug("Error parsing user info: " + e.getMessage());
            return null;
        }
    }

    public String getFullName(){
        return this.getName() + " " + this.getSurname();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServer_url() {
        return server_url;
    }

    public void setServer_url(String server_url) {
        this.server_url = server_url;
    }
}

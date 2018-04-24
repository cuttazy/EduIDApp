package com.eduid.EduIdApp.controller;

/**
 * Created by Yann Cuttaz on 11.05.16.
 */
public class ApiConfig {



    private final static String ENDPOINT_URL = "https://eduid.htwchur.ch/eduid/eduid.php";


    public final static String USER_PROFILE_VERIFICATION_ENDPOINT_URL = ENDPOINT_URL + "/user-profile";
    public final static String RSD_LIST_URL = "https://eduid.htwchur.ch/rsd/";

    //    public final static String OIDC_SERVICE_URL = "https://eduid.htwchur.ch/oidc/";
    public static String TOKEN_ENDPOINT_URL = null;
    public static String JWKS_ENDPOINT_URL = null;
    public static String GRANT_TYPE = null;
    public final static String OIDC_CONFIGURATION_URL = "https://eduid.htwchur.ch/oidc/.well-known/openid-configuration";


}

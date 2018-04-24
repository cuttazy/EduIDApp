package com.eduid.EduIdApp.controller.Profile;

/**
 * Created by usi on 09.05.16.
 */
public interface AuthenticationCallback{
    /**
     * Callback of authentication action
     * @param authenticateSuccessful: true if the user is authenticate
     */
    void onAuthenticationFinish(boolean authenticateSuccessful);
}
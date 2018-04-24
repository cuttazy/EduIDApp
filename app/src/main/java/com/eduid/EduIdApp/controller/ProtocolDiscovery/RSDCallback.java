package com.eduid.EduIdApp.controller.ProtocolDiscovery;

import com.eduid.EduIdApp.model.dataobjects.EduIDService;

import java.util.ArrayList;

/**
 * Created by usi on 11.05.16.
 */
public interface RSDCallback {
    void rsdAuthenticationFinished(ArrayList<EduIDService> services);
    void rsdAuthenticationError(String errorMessage);
}

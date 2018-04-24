package com.eduid.EduIdApp.controller.ProtocolDiscovery;

import com.eduid.EduIdApp.model.dataobjects.EduIDService;

import java.util.ArrayList;

/**
 * Created by usi on 11.05.16.
 */
public interface GetRSDCallback {

    void onGetRSDFinish(ArrayList<EduIDService> services);
    void onGetRSDError(String errorMessage);

}

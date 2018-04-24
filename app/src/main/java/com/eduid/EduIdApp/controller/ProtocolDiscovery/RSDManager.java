package com.eduid.EduIdApp.controller.ProtocolDiscovery;

import android.content.Context;

import com.eduid.EduIdApp.model.dataobjects.EduIDService;

import java.util.ArrayList;

/**
 * Created by usi on 11.05.16.
 */
public class RSDManager {

    private Context currentContext;
    private RSDCallback callback;
    private String[] protocolList;

    public RSDManager(Context ctx){
        this.currentContext = ctx;
    }

    /**
     * Authenticate services
     * @param callbackToCall
     */
    public void getRSD(String[] protocolList, final RSDCallback callbackToCall){
        this.protocolList = protocolList;
        this.callback = callbackToCall;
        getRSDList(new GetRSDCallback() {
            @Override
            public void onGetRSDFinish(ArrayList<EduIDService> services) {
                callbackToCall.rsdAuthenticationFinished(services);

            }

            @Override
            public void onGetRSDError(String errorMessage) {
                callbackToCall.rsdAuthenticationError("Get RSD List Error.");
            }
        });
    }


    private void getRSDList(GetRSDCallback rsdCallback){
        RSDTask task = new RSDTask();
        task.context = currentContext;
        task.protocolList = this.protocolList;
        task.callback = rsdCallback;
        task.execute();
    }

}

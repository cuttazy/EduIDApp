package com.eduid.EduIdApp.controller.AppAccessManager;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;

import com.nimbusds.jose.jwk.JWK;

import java.util.List;

/**
 * Created by usi on 19.05.16.
 */
public class DeviceRegistration {

    protected static DeviceRegistrationCallback callback;
    public static Context currentContext;

    /**
     * Register a client on eduid service
     * @param context
     * @param callback
     */
    public static void registerClient(Context context, DeviceRegistrationCallback callback){
        DeviceRegistration.callback = callback;
        DeviceRegistration.currentContext = context;

        /**
         * Load keys from oidc service
         */
        CertificatesManager.loadCerts(context, new CertificatesManager.CertificatesManagerCallback() {
            @Override
            public void onCertificatesLoaded(List<JWK> keys) {
                /**
                 * Register device
                 */
                String deviceID = Secure.getString(DeviceRegistration.currentContext.getContentResolver(), Secure.ANDROID_ID);
                String clientName = Build.MODEL;
                DeviceRegistrationTask task = new DeviceRegistrationTask();
                task.context = DeviceRegistration.currentContext;
                task.certs = keys;
                task.execute(deviceID, clientName);
            }

            @Override
            public void onCertificatesError() {
                DeviceRegistration.callback.onDeviceRegistrationFinish(false);
            }
        });



    }

    /**
     * Callback structure
     */
    public interface DeviceRegistrationCallback{
        void onDeviceRegistrationFinish(boolean success);
    }

}

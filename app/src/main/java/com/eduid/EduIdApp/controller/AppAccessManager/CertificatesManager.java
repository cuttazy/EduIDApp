package com.eduid.EduIdApp.controller.AppAccessManager;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.nimbusds.jose.jwk.JWK;

import java.security.Key;
import java.util.List;

public class CertificatesManager {

    public static void loadCerts(Context context, CertificatesManagerCallback callback){

        CertificatesTask task = new CertificatesTask();
        task.callback = callback;
        task.context = context;
        task.execute();
    }


    public interface CertificatesManagerCallback{

        void onCertificatesLoaded(List<JWK> keys);
        void onCertificatesError();

    }

}

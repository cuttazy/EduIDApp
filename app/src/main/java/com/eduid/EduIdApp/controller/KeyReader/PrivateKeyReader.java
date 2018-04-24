package com.eduid.EduIdApp.controller.KeyReader;


import android.content.Context;

import java.io.*;
import java.nio.*;
import java.security.*;
import java.security.spec.*;

/**
 * Created by Yann Cuttaz on 09.08.17.
 */
public class PrivateKeyReader {

    public static PrivateKey get(String filename, Context context)
            throws Exception {

//        byte[] keyBytes = java.nio.file.Files.readAllBytes(Paths.get(filename));


        InputStream is = context.getAssets().open(filename);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        byte[] keyBytes = buffer.toByteArray();


        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}
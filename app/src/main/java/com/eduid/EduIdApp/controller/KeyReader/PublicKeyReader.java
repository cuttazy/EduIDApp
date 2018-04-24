package com.eduid.EduIdApp.controller.KeyReader;

import android.content.Context;

import java.io.*;
import java.nio.*;
import java.security.*;
import java.security.spec.*;

public class PublicKeyReader {

    public static PublicKey get(String filename, Context context)
            throws Exception {

//        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));


        InputStream is = context.getAssets().open(filename);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        byte[] keyBytes = buffer.toByteArray();



        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
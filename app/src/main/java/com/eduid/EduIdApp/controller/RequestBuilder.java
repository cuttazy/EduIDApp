package com.eduid.EduIdApp.controller;

import android.content.Context;
import android.content.res.AssetManager;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.chilkatsoft.CkCrypt2;
import com.chilkatsoft.CkJsonObject;
import com.eduid.EduIdApp.controller.KeyReader.PrivateKeyReader;
import com.eduid.EduIdApp.controller.KeyReader.PublicKeyReader;
import com.eduid.EduIdApp.model.EduIdDB;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyConverter;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.util.IOUtils;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.JwtSigner;
import io.jsonwebtoken.impl.crypto.MacProvider;

import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Timestamp;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * Created by usi on 13.06.16.
 */
public class RequestBuilder {

    public static String publicKey = "{\"keys\":[{\"kty\":\"RSA\",\"kid\":\"I8aLj8PyA0-qRXK3ua_J-_HxtRhGew6lB4Gz5NIlliI\",\"e\":\"AQAB\",\"n\":\"j_ax9SoDICsLOInyskslOxWVVZK8hBUaUOlPyA7dWT-QIzb5BEDHGUBYEpUE1rHUnu3Y-CrQmTVQozCPXPotbMFAjvJwvIlTpk0gGsIbRhPW-dah8ZnC0v1d1DQJUa9E4T4rKu-a4iKSqyV632IHds7PGGs5W1QLMBGp_hLwgHbZGEArhkLI-7aiMhZEqxGXUDApgiCjlZioDm4SvJnpMO7_87kxhIaX452uFQYOK7kYWPlkysuO1qMVR4J2yslHBSL1nynYG51T1r_FibeGIe3HSVt18Caj2tk0gfwcfIt4nlA0ZKqGfKY4MDgMTDtwWh_amDrv_rh6PEBNPcJh7w\"}]}\n";
    public static String privateKey = "{\"keys\":[{\"kty\":\"RSA\",\"kid\":\"I8aLj8PyA0-qRXK3ua_J-_HxtRhGew6lB4Gz5NIlliI\",\"e\":\"AQAB\",\"n\":\"j_ax9SoDICsLOInyskslOxWVVZK8hBUaUOlPyA7dWT-QIzb5BEDHGUBYEpUE1rHUnu3Y-CrQmTVQozCPXPotbMFAjvJwvIlTpk0gGsIbRhPW-dah8ZnC0v1d1DQJUa9E4T4rKu-a4iKSqyV632IHds7PGGs5W1QLMBGp_hLwgHbZGEArhkLI-7aiMhZEqxGXUDApgiCjlZioDm4SvJnpMO7_87kxhIaX452uFQYOK7kYWPlkysuO1qMVR4J2yslHBSL1nynYG51T1r_FibeGIe3HSVt18Caj2tk0gfwcfIt4nlA0ZKqGfKY4MDgMTDtwWh_amDrv_rh6PEBNPcJh7w\",\"d\":\"QeebnDJ5b9aEsoNkWX7RMnU1AhHEv2qxu2yzm1BCwMK6h4R5BxtoigTMzOTvbCHIxmD4PNPM3vZrTenDF7tn5CusFESkm4r6gl04X0eRGMmoVoONa38Kk3Bt7eFMitqD2-GL_YHnE7LkFfl4gJ7t79PJNtKPVWHqJT1Q5wFgnE4bEcjOxUegIKvYUixKmI6hObq_tl0YSt8xzdO2nObvS1TNVeAvb5dwXaI1UP-L6ly3uJWBx1V4QbSV_M7izfOlqbmdPNePjp1mOoReSVGQDLih4wKDkgDr3oMpW8ICP5qZGMP81nfuGP5B0Ze8iIPSPaRUWV9Mg2j4mN4lNXkzUQ\",\"p\":\"2KXG0_ZhMGo8acxUeLNpQ0DYB8AC-7QZkQ-SJEOdIUNEkVhIRR8tz-sGMp5FxM1GURT2LEGy1MEoeuPZt0yOCwkHwC6ISc21NH3Ru1Yc09KpxtbJyP9fMETwvZ2mcG7am3vbn637g6H4hYV_bQjLLJPdkUq4HDX479QxxTnLanM\",\"q\":\"qh0UQhfFShB2VoSbNHyNrIHsJV3dtKVpwIzHuOGwNM3fMWeIJ82KnON1rzaciyhG5vZanD4DwcDcX8Y9QWxB7WLl7AUpogwyP3cl1leUZ5yD9Uor6Kpcz2LJuwJ6T8zv4X3iHGFzq2KwECczcRCdRvQGGKbIqRAnn41SUJ_An5U\",\"dp\":\"KvsoqSC2Q26wMaUyFQgzF_6jXVZAwUMH13mpWrx3TgQUdGl5XQ1Ef313K0-vM84t5yQrerhGDGON7mOJ7A2qGJJgEUaqD0paauGTXIFHEtc2i5ZNTi0r612iDVxbiu7TPAmkltdjkMfvkD3d-_nEpUFoscyLHj2u95W3khNXQfU\",\"dq\":\"mhEqFs3mjbSpKKSC2J9xxGoK0LDZqdEiHRSjkgs9BiXqXZlR1BKw1nSfUifY0aORy34VBGqaBnHAZTFqqwT8FJ48dG0cjroTDC3B5AV_z_MOBCq_58HbuqvS3n9Y_UszensDPQb5tp_zqz2FtoIAn5FEsDDljVCnQjbA9WoqD50\",\"qi\":\"RHpB2Ya5bZUKpxVBHS3GoSqeI6tjoYZ_YpBV7cXvD0GMwtWYJcRoByUpR5LSRTzUYnfb4nS9viPJ9IHe1O8cME7FEdQq-BvtukIhbAnHwkR0IDTaJXVLlvgRVXcEfU1gCeyOzTjndr3CjvMvc2wd1NGm-kJGaQq7uUkHkkMQ-bo\"}]}\n";
    public static String generatedKeys = "{ \"keys\" : [ { \"p\"   : \"x9oLGkDsfHGBL1-RrtULjGRlZAQqD3TuDcmIcfz0yATPPeKyyxF-ZFymbVhw4Fl5A7dizQaLyAc8FsNay0hL0azufG2dlUyaX4w2R3rVqoIe7gOW3vS8zozR0QPKBlI2rpUjx__SLfMWuO7f4itI1A0yMdaKOi6P8Ieq6czG8pU\",\n" +
            "               \"kty\" : \"RSA\",\n" +
            "               \"q\"   : \"whPedr_zBmRrAPAyRKflgr_uyXEbEbzOx3BGXiFnenf9wwnXNoS4mVcoORNzbDbi4y8bJj8FDsDkkesWBaUgeyVnamjd17KCgw3jS5M95Lvt8fWt0bQkG3yxp7BJpF7h5MJb2liz8yagjReqt2xdgERA4mLlqmU2yJRhK9xsIcM\",\n" +
            "               \"d\"   : \"ECXuTtXzySUMgPmpwmOddORqwgTmVlBAxl9Xa7QQuRAoZd1XqzVY3qlzkS-ovWXabdcYJoo-wWCKhv_kgV0j2B5q224htJEwfqAf9RiIV1F9TVSnDNqXzWt4R7bAizfqz3-K2LLIOSLVBrThSJDMdAA1mX_yVD_q9C8f3fV73QEDDCufZEk5jBaJmVibujyYfond6Isa1_kpfqQhHnHkKAapeeYA6m-4N6W1cKiYZvWTApfkvaQMDigGq2icb3YnvrgK59WgPbCnoLatz9h-CHkNonteoaoXCUeIHtrYTo1ZUFmXYwfuR3XEV9JzB__W_EG4nFDaUw5q1s-LIY1raQ\",\n" +
            "               \"e\"   : \"AQAB\",\n" +
            "               \"use\" : \"sig\",\n" +
            "               \"kid\" : \"IyIA\",\n" +
            "               \"qi\"  : \"A2HTSBoT9QLOtq6TlNvR6hLlqLHMa8SoEFn_0V14TBs-W4InIEl7J4B21egGq5--og_Kuy0f9j5Dl6gjqxuAGATzYIbo-lLr-GmVHyFOs6m7tNzxV2ioyzhpZNgFhHiPa6qotrXGzIP99YHzEdwZDwCNopv39yXFltJnID5m5x0\",\n" +
            "               \"dp\"  : \"agFD5lcNmSxzg3dDxOKIT51pElYLIUu6gWGmmB--juHOM1stzvymeAGU3WcN6FJNGCWLqIiSWDVOMBzK6gyc-sipbyKNdYW-VHQAzoVfFCQU19bbjcNu6nhTSnSiEhxFmPDOm-UbzPel084QvtyKsr-VIk5T32yXONQdWgoedMk\",\n" +
            "               \"dq\"  : \"ErsCLGnIFaTZYuTLpS6jCuRlMlHVL_gN6NegIvOMXhh4iDtJ5vwWKM5tllyXNilx6kUglKdeYJcjOCX9IRxXryrj-TQXvPqAeA9-EUwrnAi7JS_1z5hYJ8L4DlwIL3Hs0Twky0TmUSA4PIW9NyihOYDc1jx_N9u-1srjOe1zDis\",\n" +
            "               \"n\"   : \"l4K_RIIhUYDikiqKdymqVLrm6SeO8VMy2z6NzOQgAAhDXQt0T36OIP8qWqTGiPItNv5_PDr7CdVDPGC83d2qptcZBr5BQzZRSGgIsbJtk_MfOBwDuF9150Vw0LkEug1XdkhJTwpVGSTW0z84h9PpKOOU5ezcyV0r8nFAETOiHuW5LYZNYYFHJPFrDqVjQ8ePwGXJMuW-dk9XCGPSqyIeRjjrZPOpRsftz0rABD862Hw54cwZqfE3nlAdPoWgqX7jobF56Nz9oP3tdr5x-rMhCN_WBkd6yRVsMdluKZNJsaIU0Kjt1LSmQ5vHYoZ0AYbYs4z8VHcQJpYodePqA6v8fw\" }, \n" +
            "             { \"kty\" : \"EC\",\n" +
            "               \"d\"   : \"tiOZ4iSyNYWa0zKTTqev_WA-OyzwNgIEJxBdQG8kRcM\",\n" +
            "               \"use\" : \"sig\",\n" +
            "               \"crv\" : \"P-256\",\n" +
            "               \"kid\" : \"t9uc\",\n" +
            "               \"x\"   : \"N1ba8w7nk3EcjTF4ylxFqeuqGZ22io36sGzqq1_5g10\",\n" +
            "               \"y\"   : \"sEgjt7UuU0RMzX-YJuMa8CQ8B_vJi8sHMEFjj3azfA4\" }, \n" +
            "             { \"kty\" : \"EC\",\n" +
            "               \"d\"   : \"I6bUR45HjtMnqyXiN5NCepuW14dSOw8VXrd_6Ioa6M_g7Ui9LXAwIIvT5UKkW43-\",\n" +
            "               \"use\" : \"sig\",\n" +
            "               \"crv\" : \"P-384\",\n" +
            "               \"kid\" : \"likY\",\n" +
            "               \"x\"   : \"YoLakoUs5OR_2KqdQh7PNET7slGKJStWf1SSqzSX8X0UIdEs22DB27S1wHNRqtXb\",\n" +
            "               \"y\"   : \"K5GwjCdZm-5zkLX6D3KegY1U0jQ6sJLw4c06i82Wy49icIqp9DNJcISPQhgNt4Nf\" }, \n" +
            "             { \"kty\" : \"EC\",\n" +
            "               \"d\"   : \"AI-lnAUObGstRhqA8IqD7cmCr8xW4TrWcchT0CSF_bUoBLqKqOkrzIgI6uqWsFK35-BsZcwE1PK4zYtAePcxP97c\",\n" +
            "               \"use\" : \"sig\",\n" +
            "               \"crv\" : \"P-521\",\n" +
            "               \"kid\" : \"oUBG\",\n" +
            "               \"x\"   : \"ASnqXjVQc_XAGOqCl6wsKVcyocxK2L-mqUgwmjJr1jWdnpTpw5hXSxw1G8kp0sh8xaMBftK0wT7vgvBPl0fEe0nn\",\n" +
            "               \"y\"   : \"Abv-r0UFViFHCwuQDtxyHTf41hUWIK_81OMqHMkMcEYb_I1I_uV1x3votZ3B81vVbWxAcm9y_nNFsta4yLZwh6uI\" }, \n" +
            "             { \"kty\" : \"oct\",\n" +
            "               \"use\" : \"enc\",\n" +
            "               \"kid\" : \"ti9u\",\n" +
            "               \"k\"   : \"wzoYGeFnADzfhBsWU5DiTQ\" }, \n" +
            "             { \"kty\" : \"oct\",\n" +
            "               \"use\" : \"sig\",\n" +
            "               \"kid\" : \"hmac\",\n" +
            "               \"k\"   : \"bmJ0awXr4PNJ9BO4j2jBhzrOk0VOoEa5dJBFBxgG4iQ\" }, \n" +
            "             { \"kty\" : \"oct\",\n" +
            "               \"use\" : \"enc\",\n" +
            "               \"kid\" : \"subject-encrypt\",\n" +
            "               \"k\"   : \"93pv8sBjdRiUVlQV7bkOyaFa7qSlRawy6dQas_8JhPM\" } ] }\n";


//    public static String clientID = "ch.htwchur.eduid.android.0"; vecchio
    public static String clientID = "android dev";
    public static String clientSecret = "JiGowImviAbEgphottEpLoos6blead";




    public static List<Key> getPublicKeys(Context context)  {

        try {


            JWKSet localKeys = JWKSet.parse(publicKey);

            return KeyConverter.toJavaKeys(localKeys.getKeys());
        }catch (ParseException e) {
            Config.debug("error: " + e.getMessage());
        }
        return null;

    }

    public static String getPublicKid(Context context)  {

        try {


            JWKSet localKeys = JWKSet.parse(publicKey);

            return localKeys.getKeys().get(0).getKeyID();
        }catch (ParseException e) {
            Config.debug("error getting public kid: " + e.getMessage());
        }
        return null;

    }

    public static String getPrivateKid(Context context)  {

        try {


            JWKSet localKeys = JWKSet.parse(privateKey);

            return localKeys.getKeys().get(0).getKeyID();
        }catch (ParseException e) {
            Config.debug("error getting private kid: " + e.getMessage());
        }
        return null;

    }


    public static List<Key> getPrivateKeys(Context context)  {


        try {
            JWKSet jwkSet = JWKSet.parse(privateKey);


            return KeyConverter.toJavaKeys(jwkSet.getKeys());
        }catch (ParseException e) {
            Config.debug("error: " + e.getMessage());
        }
        return null;

    }

    public static String buildAssertionAuthenticationJWS(String username, String registeredClient, String requestURL, String deviceID, String password, Context context, RSAPublicKey serverKey ) {

        net.minidev.json.JSONObject cnf = new net.minidev.json.JSONObject();


        try {
            JSONObject publicKeyJSONObject = new JSONObject(publicKey);

            JWK jwk = JWKSet.parse(publicKeyJSONObject.toString()).getKeys().get(0);

            cnf.put("jwk", jwk.toJSONObject());

        } catch (ParseException e) {
            Config.debug("Error parsing JWK object");
        } catch (JSONException e) {
            Config.debug("Error on creating JSON object");
        }

        // Prepare JWT with claims set
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .issuer(registeredClient)
                .expirationTime(new Date(new Date().getTime() + 60 * 24 * 1000))
//                .issueTime(new Date())
                .audience(requestURL)
                .claim("azp", deviceID)
                .claim("x_crd", password)
                .claim("cnf", cnf)
                .build();


        Config.debug(claimsSet.toJSONObject());

        // Create RSA-signer with the private key
        List<Key> keys = getPrivateKeys(context);
        RSAPrivateKey key = (RSAPrivateKey) keys.get(1);
        JWSSigner signer = new RSASSASigner(key);

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(getPrivateKid(context)).build();


        SignedJWT signedJWT = new SignedJWT(header, claimsSet);

        // Compute the RSA signature
        try {
            signedJWT.sign(signer);
        } catch (JOSEException e) {
            Config.debug("Error on sign JWT");
        }

        String jwsSerialized = signedJWT.serialize();
        Config.debug(jwsSerialized);







        return jwsSerialized;

/*

        JwtBuilder builder = Jwts.builder();
        builder.setIssuer(registeredClient);
        builder.setAudience(requestURL);
        builder.setSubject(username);
        builder.claim("azp", deviceName);
        builder.claim("x_crd", password);
        builder.setIssuedAt(new Date());
        builder.setExpiration(new Date(new Date().getTime() + 60 * 1000));



        builder.setHeaderParam("kid", getPublicKid(context));

        List<Key> keys = getPrivateKeys(context);
//        Config.debug("Num: " + keys.size());
//        Config.debug(keys.get(1).getClass().getName());
        RSAPrivateKey key = (RSAPrivateKey) keys.get(1);

        try {
            JSONObject cnf = new JSONObject();

            JWK jwk = JWKSet.parse(publicKey).getKeys().get(0);

//            cnf.put("jwk", jwk.toJSONObject());

//            builder.claim("cnf", cnf);
            builder.claim("cnf", jwk.toJSONObject());
        } catch (ParseException e) {
            Config.debug("Error parsing JWK object");
        }
        builder = builder.signWith(SignatureAlgorithm.RS256, key);
        return builder.compact(); // Bug nimbus, crash
        */
    }

    public static String getMD5(String input){
        try {
            byte[] bytesOfMessage = input.getBytes("UTF-8");

            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(bytesOfMessage).toString();
        } catch (Exception e) {
            Config.debug("MD5 error: " + e.getMessage());
            return "";
        }
    }

    public static String getMD5(Long input){
        return getMD5(input.toString());
    }

    public static String buildAssertionRPJWS(String userid, String deviceID, String requestURLEduidServer, String serviceURL, Context context ) {

        String kid = getPrivateKid(context);

        net.minidev.json.JSONObject cnf = new net.minidev.json.JSONObject();
        cnf.put("kid", kid);

        Config.debug("issuer: " + deviceID);

        // Prepare JWT with claims set
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userid)
                .issuer(deviceID)
                .expirationTime(new Date(new Date().getTime() + 60 * 60 * 24 * 1000))
//                .issueTime(new Date())
                .audience(requestURLEduidServer)
                .claim("jti", getMD5(new Date().getTime()))
//                .claim("nbf", new Date())
                .claim("cnf", cnf)
                .claim("azp", serviceURL)
                .build();



        Config.debug(claimsSet.toJSONObject());

        // Create RSA-signer with the private key
        List<Key> keys = getPrivateKeys(context);
        RSAPrivateKey key = (RSAPrivateKey) keys.get(1);
        JWSSigner signer = new RSASSASigner(key);



        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(kid).build();

        SignedJWT signedJWT = new SignedJWT(header, claimsSet);

        // Compute the RSA signature
        try {
            signedJWT.sign(signer);
        } catch (JOSEException e) {
            Config.debug("Error on sign JWT");
        }

        String jwsSerialized = signedJWT.serialize();
        Config.debug(jwsSerialized);







        return jwsSerialized;

    }

    public static String buildAssertionAuthorizationJWS(String netidID, String deviceInstance, String requestURL, String redirect_uri, String password, Context context ){
        JwtBuilder builder = Jwts.builder();
        builder.setIssuer(deviceInstance);
        builder.setAudience(requestURL);
        builder.setSubject(netidID);
        builder.claim("azp", redirect_uri);
        builder.claim("x_crd", password);
        builder.setIssuedAt(new Date());
        builder.setExpiration(new Date(2018, 12, 12));

        List<Key> keys = getPrivateKeys(context);
        RSAPrivateKey key = (RSAPrivateKey) keys.get(1);

        builder.claim("cnf", key.toString());
        return builder.signWith(SignatureAlgorithm.RS256, key).compact();
    }


    public static String buildAssertionAuthentication(String username, String registeredClient, String requestURL, String deviceName, String password, Context context ) throws Exception {



        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        builder.issuer(registeredClient);
        builder.audience(requestURL);
        builder.subject(username);
        builder.claim("azp", deviceName);
        builder.claim("x_crd", password);
        builder.issueTime(new Date());
        builder.expirationTime(new Date(2018, 12, 12));

        List<Key> keys = getPublicKeys(context);
        Config.debug("Num: " + keys.size());
        RSAPublicKey key = (RSAPublicKey) keys.get(0);

        builder.claim("cnf", key);

        // Request JWT encrypted with RSA-OAEP-256 and 128-bit AES/GCM
        JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256CBC_HS512);

        // Create the encrypted JWT object
        EncryptedJWT jwt = new EncryptedJWT(header, builder.build());

        // Create an encrypter with the specified public RSA key
        RSAEncrypter encrypter = new RSAEncrypter(key);



        // Do the actual encryption
        jwt.encrypt(encrypter);

        // Serialise to JWT compact form
        String jwtString = jwt.serialize();

        Config.debug(jwtString);

        return jwtString;


//        return builder.signWith(SignatureAlgorithm.RS256, key).compact();
    }


    /**
     * Return the authorization string for Device registration request
     * @param deviceId
     * @param deviceName
     * @param requestURL
     * @return
     */
    public static String buildAssertionDeviceRegistration(String deviceId, String deviceName, String requestURL, Context context, List<Key> keys) throws Exception {


        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        builder.issuer(clientID);
        builder.audience(requestURL);
        builder.subject(clientID);
        builder.jwtID(clientSecret);
        builder.issueTime(new Date());
        builder.expirationTime(new Date(2018, 12, 12));
        builder.claim("name", deviceName);

//        List<Key> keys = getPublicKeys(context);
        Config.debug("Num: " + keys.size());
        RSAPublicKey key = (RSAPublicKey) keys.get(0);

        builder.claim("cnf", key);

        // Request JWT encrypted with RSA-OAEP-256 and 128-bit AES/GCM
        JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256CBC_HS512);

        // Create the encrypted JWT object
        EncryptedJWT jwt = new EncryptedJWT(header, builder.build());

        // Create an encrypter with the specified public RSA key
        RSAEncrypter encrypter = new RSAEncrypter(key);



        // Do the actual encryption
        jwt.encrypt(encrypter);

        // Serialise to JWT compact form
        String jwtString = jwt.serialize();

        Config.debug(jwtString);

        return jwtString;


//        return builder.signWith(SignatureAlgorithm.RS256, key).compact();
    }

    /**
     * Return the authorization string for the authentication request
     * @return
     */
    public static String buildAuthorizationAuthentication(String deviceId, String requestURL, String kid, String mac_key, String mac_algorithm){

        JwtBuilder builder = Jwts.builder();
        builder.setHeaderParam("typ", "JWT");
        builder.setIssuer(deviceId);
        builder.setAudience(requestURL);
        builder.setSubject(deviceId);
        builder.setHeaderParam("kid", kid);

        String key = Base64.encodeToString(mac_key.getBytes(), Base64.NO_WRAP);

        return builder.signWith(SignatureAlgorithm.valueOf(mac_algorithm), key).compact();
    }

    /**
     * Return the authorization string for the profile verification request
     * @return
     */
    public static String buildAuthorizationProfileVerification(String deviceId, String requestURL, String kid, String mac_key, String mac_algorithm){
        JwtBuilder builder = Jwts.builder();
        builder.setHeaderParam("typ", "JWT");
        builder.setIssuer(deviceId);
        builder.setAudience(requestURL);
        builder.setSubject(deviceId);
        builder.setHeaderParam("kid", kid);

        String key = Base64.encodeToString(mac_key.getBytes(), Base64.NO_WRAP);

        return builder.signWith(SignatureAlgorithm.valueOf(mac_algorithm), key).compact();
    }


    /**
     * Generic authorization for service-discovery request
     * @param context
     * @param requestedURL
     * @return
     */
    public static String buildGenericAuthorization(Context context, String requestedURL){
        EduIdDB eduIdDB = new EduIdDB(context);
        String kid = eduIdDB.getAuthenticationParam("kid");
        String mac_key = eduIdDB.getAuthenticationParam("mac_key");
        String mac_algorithm = eduIdDB.getAuthenticationParam("mac_algorithm");
        String deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        JwtBuilder builder = Jwts.builder();
        builder.setHeaderParam("typ", "JWT");
        builder.setIssuer(deviceID);
        builder.setAudience(requestedURL);
        builder.setSubject(deviceID);
        builder.setHeaderParam("kid", kid);

        String key = Base64.encodeToString(mac_key.getBytes(), Base64.NO_WRAP);
        return builder.signWith(SignatureAlgorithm.valueOf(mac_algorithm), key).compact();
    }

    public static String buildAppAuthorizationToServiceJWT(Context context, String thirdPartyAppID, int platformID, String requestedURL){
        EduIdDB eduIdDB = new EduIdDB(context);
        String kid = eduIdDB.getForwardAssertionParam(platformID, "kid");
        String mac_key = eduIdDB.getForwardAssertionParam(platformID, "sign_key");
        String mac_algorithm = eduIdDB.getForwardAssertionParam(platformID, "algorithm");
        String deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        JwtBuilder builder = Jwts.builder();
        builder.setHeaderParam("typ", "JWT");
        builder.setIssuer(deviceID);
        builder.setAudience(requestedURL);
        builder.setSubject(thirdPartyAppID);
//        builder.setSubject(deviceID);
        builder.setHeaderParam("kid", kid);

//        Config.debug(thirdPartyAppID);

        String key = Base64.encodeToString(mac_key.getBytes(), Base64.NO_WRAP);
        return builder.signWith(SignatureAlgorithm.valueOf(mac_algorithm), key).compact();
    }


    public static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }


}

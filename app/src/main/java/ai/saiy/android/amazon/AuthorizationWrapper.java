package ai.saiy.android.amazon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager;
import com.amazon.identity.auth.device.authorization.api.AuthorizationListener;
import com.amazon.identity.auth.device.authorization.api.AuthzConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import ai.saiy.android.amazon.listener.IAlexaToken;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class AuthorizationWrapper implements AuthorizationListener {

    private static final boolean DEBUG = MyLog.DEBUG;

    private static final String CLS_NAME = AuthorizationWrapper.class.getSimpleName();
    private static final String[] SCOPES = {"alexa:all"};
    private AmazonAuthorizationManager authorizationManager;
    private final Context context;
    private ai.saiy.android.amazon.listener.AuthorizationListener listener;

    public AuthorizationWrapper(Context context) {
        this.context = context;
        try {
            this.authorizationManager = new AmazonAuthorizationManager(context, Bundle.EMPTY);
        } catch (IllegalArgumentException e) {
            MyLog.e(CLS_NAME, "API Key error");
            if (DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private String encode(byte[] bytes) {
        return new String(Base64.encode(bytes, Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE)).split("=")[0].replace('+', '-').replace('/', '_');
    }

    private byte[] hash(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.reset();
            return messageDigest.digest(str.getBytes());
        } catch (NoSuchAlgorithmException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "hash: NoSuchAlgorithmException");
                e.printStackTrace();
            }
            return null;
        }
    }

    private String getCodeVerifier() {
        String codeVerifier = ai.saiy.android.utils.SPH.setCodeVerifier(this.context);
        if (!UtilsString.notNaked(codeVerifier)) {
            codeVerifier = verifier();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "verifier: " + codeVerifier);
            }
            ai.saiy.android.utils.SPH.getCodeVerifier(this.context, codeVerifier);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "getCodeVerifier: " + codeVerifier);
        }
        return codeVerifier;
    }

    private String getCodeChallenge() {
        return encode(hash(getCodeVerifier()));
    }

    private String verifier() {
        char[] charArray = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 128; i++) {
            sb.append(charArray[random.nextInt(charArray.length)]);
        }
        return sb.toString();
    }

    public AmazonAuthorizationManager getAuthorizationManager() {
        return this.authorizationManager;
    }

    @SuppressLint({"HardwareIds"})
    public void authoriseUser(ai.saiy.android.amazon.listener.AuthorizationListener listener) {
        this.listener = listener;
        Bundle bundle = new Bundle();
        JSONObject jSONObject = new JSONObject();
        JSONObject jSONProduct = new JSONObject();
        JSONObject jSONDevice = new JSONObject();
        try {
            String deviceID = Global.getGlobalID();
            if (!UtilsString.notNaked(deviceID)) {
                deviceID = Settings.Secure.getString(this.context.getContentResolver(), "android_id");
            }
            jSONDevice.put("deviceSerialNumber", deviceID);
            jSONProduct.put("productID", Constants.SAIY);
            jSONProduct.put("productInstanceAttributes", jSONDevice);
            jSONObject.put(SCOPES[0], jSONProduct);
            bundle.putString(AuthzConstants.BUNDLE_KEY.SCOPE_DATA.val, jSONObject.toString());
            bundle.putBoolean(AuthzConstants.BUNDLE_KEY.GET_AUTH_CODE.val, true);
            bundle.putString(AuthzConstants.BUNDLE_KEY.CODE_CHALLENGE.val, getCodeChallenge());
            bundle.putString(AuthzConstants.BUNDLE_KEY.CODE_CHALLENGE_METHOD.val, "S256");
            this.authorizationManager.authorize(SCOPES, bundle, this);
        } catch (JSONException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "authoriseUser: JSONException");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSuccess(Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "AuthorizationListener: onSuccess");
        }
        TokenHelper.getPrimaryAccessToken(this.context, bundle.getString(AuthzConstants.BUNDLE_KEY.AUTHORIZATION_CODE.val, ""), getCodeVerifier(), this.authorizationManager, new IAlexaToken() {
            @Override
            public void onSuccess(AmazonCredentials credentials) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "IAlexaToken: onSuccess");
                }
                AuthorizationWrapper.this.listener.onSuccess();
                TokenHelper.saveToken(AuthorizationWrapper.this.context, credentials);
            }

            @Override
            public void onFailure(Exception exc) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "IAlexaToken: onFailure");
                }
                AuthorizationWrapper.this.listener.onError(exc);
            }
        });
    }

    @Override
    public void onError(AuthError authError) {
        if (DEBUG) {
            MyLog.e(CLS_NAME, "AuthorizationListener: onError");
        }
        this.listener.onError(authError);
    }

    @Override
    public void onCancel(Bundle bundle) {
        if (DEBUG) {
            MyLog.e(CLS_NAME, "AuthorizationListener: onCancel");
        }
        this.listener.onCancel();
    }
}

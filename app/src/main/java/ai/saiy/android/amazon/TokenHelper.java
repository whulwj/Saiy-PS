package ai.saiy.android.amazon;

import android.content.Context;

import androidx.annotation.NonNull;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import ai.saiy.android.amazon.listener.IAlexaToken;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class TokenHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = TokenHelper.class.getSimpleName();
    private static final String AUTH_URL = "https://api.amazon.com/auth/O2/token";
    private static final String GRANT_TYPE = "grant_type";
    private static final String GRANT_REFRESH_TOKEN = "refresh_token";
    private static final String CLIENT_ID = "client_id";

    private static long refreshTimestamp;

    public static void refreshTokenIfRequired(final Context context) {
        if (!ai.saiy.android.utils.Conditions.Network.isNetworkAvailable(context)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "refreshTokenIfRequired: no network");
                return;
            }
            return;
        }
        if (isTokenValid(context)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "refreshTokenIfRequired: valid");
            }
        } else if (!hasToken(context)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "refreshTokenIfRequired: no longer authorised");
            }
        } else if (System.currentTimeMillis() <= refreshTimestamp + 5000) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "refreshTokenIfRequired: invalid: delay too short");
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "refreshTokenIfRequired: invalid: delay ok");
            }
            refreshTimestamp = System.currentTimeMillis();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    refreshToken(context);
                }
            }).start();
        }
    }

    public static void getAccessToken(Context context, IAlexaToken tokenListener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAccessToken");
        }
        if (!hasToken(context)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getAccessToken: no tokens");
            }
            tokenListener.onFailure(new Exception("No tokens"));
            return;
        }
        String accessToken = SPH.getAlexaAccessToken(context);
        String refreshToken = SPH.getAlexaRefreshToken(context);
        if (isTokenValid(context)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getAccessToken: valid");
            }
            tokenListener.onSuccess(new AmazonCredentials(accessToken, refreshToken, SPH.getAlexaAccessTokenExpiry(context), null));
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getAccessToken: refreshing");
            }
            refreshToken(context, tokenListener, refreshToken);
        }
    }

    private static void refreshToken(final Context context, final IAlexaToken tokenListener, String refreshToken) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "refreshToken");
        }
        FormBody.Builder builder = new FormBody.Builder().add(GRANT_TYPE, GRANT_REFRESH_TOKEN).add(AmazonCredentials.REFRESH_TOKEN, refreshToken);
        builder.add(CLIENT_ID, UtilsNetwork.getAuthorizationManager(context).getClientId());
        UtilsNetwork.getOkHttpClient().newCall(new Request.Builder().url(AUTH_URL).post(builder.build()).build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "refreshToken: onFailure");
                    e.printStackTrace();
                }
                tokenListener.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String g = response.body().string();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "refreshToken: onResponse: responseString: " + g);
                }
                AmazonCredentials amazonCredentials = new com.google.gson.Gson().fromJson(g, new TypeToken<AmazonCredentials>() {
                }.getType());
                TokenHelper.saveToken(context, amazonCredentials);
                tokenListener.onSuccess(amazonCredentials);
            }
        });
    }

    public static void saveToken(Context context, AmazonCredentials amazonCredentials) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "saveToken: getRefreshToken: " + amazonCredentials.getRefreshToken());
            MyLog.i(CLS_NAME, "saveToken: getAccessToken: " + amazonCredentials.getAccessToken());
            MyLog.i(CLS_NAME, "saveToken: getExpires: " + amazonCredentials.getExpiryTime());
        }
        SPH.setAlexaRefreshToken(context, amazonCredentials.getRefreshToken());
        SPH.setAlexaAccessToken(context, amazonCredentials.getAccessToken());
        SPH.setAlexaAccessTokenExpiry(context, System.currentTimeMillis() + (amazonCredentials.getExpiryTime() * 1000));
    }

    public static void getPrimaryAccessToken(final Context context, String code, String codeVerifier, AmazonAuthorizationManager authorizationManager, final IAlexaToken tokenListener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getPrimaryAccessToken");
        }
        FormBody.Builder builder = new FormBody.Builder().add(GRANT_TYPE, "authorization_code").add("code", code);
        try {
            builder.add("redirect_uri", authorizationManager.getRedirectUri());
            builder.add(CLIENT_ID, authorizationManager.getClientId());
            builder.add("code_verifier", codeVerifier);
            UtilsNetwork.getOkHttpClient().newCall(new Request.Builder().url(AUTH_URL).post(builder.build()).build()).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "getPrimaryAccessToken: onFailure");
                        e.printStackTrace();
                    }
                    tokenListener.onFailure(e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String g = response.body().string();
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getPrimaryAccessToken: responseString: " + g);
                    }
                    AmazonCredentials amazonCredentials = new com.google.gson.Gson().fromJson(g, new TypeToken<AmazonCredentials>() {
                    }.getType());
                    TokenHelper.saveToken(context, amazonCredentials);
                    tokenListener.onSuccess(amazonCredentials);
                }
            });
        } catch (AuthError e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "getPrimaryAccessToken: authError");
                e.printStackTrace();
            }
            tokenListener.onFailure(e);
        }
    }

    public static boolean hasToken(Context context) {
        return UtilsString.notNaked(SPH.getAlexaAccessToken(context)) && UtilsString.notNaked(SPH.getAlexaRefreshToken(context)) && SPH.getAlexaAccessTokenExpiry(context) > 0;
    }

    private static boolean isTokenValid(Context context) {
        return hasToken(context) && SPH.getAlexaAccessTokenExpiry(context) > System.currentTimeMillis();
    }

    private static void refreshToken(final Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "refreshToken");
        }
        FormBody.Builder builder = new FormBody.Builder().add(GRANT_TYPE, GRANT_REFRESH_TOKEN).add(AmazonCredentials.REFRESH_TOKEN, SPH.getAlexaRefreshToken(context));
        builder.add(CLIENT_ID, UtilsNetwork.getAuthorizationManager(context).getClientId());
        UtilsNetwork.getOkHttpClient().newCall(new Request.Builder().url(AUTH_URL).post(builder.build()).build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "refreshToken: onFailure");
                    e.printStackTrace();
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String g = response.body().string();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "refreshToken: onResponse: responseString: " + g);
                }
                TokenHelper.saveToken(context, new com.google.gson.Gson().fromJson(g, new TypeToken<AmazonCredentials>() {
                }.getType()));
            }
        });
    }
}

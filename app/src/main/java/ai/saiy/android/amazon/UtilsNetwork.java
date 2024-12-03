package ai.saiy.android.amazon;

import android.content.Context;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.UtilsString;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

public class UtilsNetwork {
    public static final String JSON_UTF_8 = "application/json; charset=UTF-8";
    public static final String OCTET_STREAM = "application/octet-stream";
    public static final String CONTENT_TYPE = "Content-Type";

    public static final int ALEXA_REGION_EUROPE = 0;
    public static final int ALEXA_NORTH_AMERICA = 1;
    public static final int ALEXA_ASIA = 2;
    private static OkHttpClient okHttpClient;

    public static String getUrl(Context context) {
        switch (ai.saiy.android.utils.SPH.getAlexaRegion(context, ALEXA_NORTH_AMERICA)) {
            case ALEXA_REGION_EUROPE:
                return "https://avs-alexa-eu.amazon.com/v20160207/events";
            case ALEXA_ASIA:
                return "https://avs-alexa-fe.amazon.com/v20160207/events";
            case ALEXA_NORTH_AMERICA:
            default:
                return "https://avs-alexa-na.amazon.com/v20160207/events";
        }
    }

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder().connectTimeout(0L, TimeUnit.MILLISECONDS).readTimeout(0L, TimeUnit.MILLISECONDS).connectionPool(new ConnectionPool(5, 3600000L, TimeUnit.MILLISECONDS)).build();
        }
        return okHttpClient;
    }

    public static com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager getAuthorizationManager(Context context) {
        return new AuthorizationWrapper(context).getAuthorizationManager();
    }

    public static String getMessageID() {
        String messageID = Global.getGlobalID();
        return (UtilsString.notNaked(messageID) ? messageID + "." : "") + UUID.randomUUID().toString();
    }
}

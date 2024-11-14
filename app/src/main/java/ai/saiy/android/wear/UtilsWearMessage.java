package ai.saiy.android.wear;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Locale;

import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.service.wear.WearListenerService;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsBundle;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;
import ai.saiy.android.wear.containers.WearMessageEvent;

public class UtilsWearMessage {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = UtilsWearMessage.class.getSimpleName();

    private static void sendMessage(Context context, String nodeId, @NonNull WearMessageEvent wearMessageEvent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sendMessage");
        }
        final Intent intent = new Intent(context, WearListenerService.class);
        intent.setAction(context.getApplicationContext().getPackageName());
        intent.putExtra(LocalRequest.EXTRA_WEAR, nodeId);
        intent.putExtra(WearListenerService.EXTRA_WEAR_MESSAGE, wearMessageEvent);
        context.getApplicationContext().startService(intent);
    }

    public static void sendMessage(Context context, String eventMessage, String nodeId, Locale eventLocale, @WearMessageEvent.Type byte eventType, @Nullable Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "prepareMessage");
        }
        if (!UtilsBundle.notNaked(bundle)) {
            sendMessage(context, nodeId, new WearMessageEvent(eventType, UtilsString.convertProperCase(eventMessage, eventLocale), UtilsLocale.convertToIETF(eventLocale), null));
            return;
        }

        if (bundle.containsKey(LocalRequest.EXTRA_OBJECT)) {
            final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            String gsonString;
            switch (bundle.getInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_NONE)) {
                case Condition.CONDITION_CONTACT:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "prepareMessage: Condition.CONDITION_CONTACT");
                    }
                    gsonString = gson.toJson(bundle.getParcelable(LocalRequest.EXTRA_OBJECT));
                    bundle.remove(LocalRequest.EXTRA_OBJECT);
                    bundle.putString(LocalRequest.EXTRA_OBJECT, gsonString);
                    break;
                case Condition.CONDITION_NOTE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "prepareMessage: Condition.CONDITION_NOTE");
                    }
                    gsonString = gson.toJson(bundle.getParcelable(LocalRequest.EXTRA_OBJECT));
                    bundle.remove(LocalRequest.EXTRA_OBJECT);
                    bundle.putString(LocalRequest.EXTRA_OBJECT, gsonString);
                    break;
                case Condition.CONDITION_FACEBOOK:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "prepareMessage: Condition.CONDITION_FACEBOOK");
                    }
                    gsonString = gson.toJson(bundle.getParcelable(LocalRequest.EXTRA_OBJECT));
                    bundle.remove(LocalRequest.EXTRA_OBJECT);
                    bundle.putString(LocalRequest.EXTRA_OBJECT, gsonString);
                    break;
                case Condition.CONDITION_TWITTER:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "prepareMessage: Condition.CONDITION_TWITTER");
                    }
                    gsonString = gson.toJson(bundle.getParcelable(LocalRequest.EXTRA_OBJECT));
                    bundle.remove(LocalRequest.EXTRA_OBJECT);
                    bundle.putString(LocalRequest.EXTRA_OBJECT, gsonString);
                    break;
                default:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "prepareMessage: Condition.CONDITION_NONE");
                    }
                    break;
            }
        }
        final Intent intent = new Intent();
        intent.putExtras(bundle);
        sendMessage(context, nodeId, new WearMessageEvent(eventType, UtilsString.convertProperCase(eventMessage, eventLocale), UtilsLocale.convertToIETF(eventLocale), intent.toUri(Intent.URI_INTENT_SCHEME)));
    }
}

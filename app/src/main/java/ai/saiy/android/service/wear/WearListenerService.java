package ai.saiy.android.service.wear;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.GsonBuilder;

import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import ai.saiy.android.wear.containers.WearMessageEvent;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class WearListenerService extends WearableListenerService {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = WearListenerService.class.getSimpleName();
    private static final String MESSAGE_PATH = "/saiy_request";
    public static final String EXTRA_WEAR_MESSAGE = "wear_message";

    private GoogleApiClient googleApiClient;
    private long then;

    private void sendMessage(final String nodeId, final WearMessageEvent wearMessageEvent, final Pair<Boolean, Integer> pair) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sendMessage: nodeId: " + nodeId);
        }
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                final String gsonString = new GsonBuilder().disableHtmlEscaping().create().toJson(wearMessageEvent);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "sendMessage: gsonString: " + gsonString);
                }
                if (!googleApiClient.isConnected()) {
                    if (!googleApiClient.isConnecting()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "sendMessage: isConnecting: false");
                        }
                        googleApiClient.connect();
                    }
                    for (int i = 1; !googleApiClient.isConnected() && i < 15; i++) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "sendMessage: sleeping " + i);
                        }
                        try {
                            Thread.sleep(200L);
                        } catch (InterruptedException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "sendMessage: InterruptedException");
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (googleApiClient.isConnected()) {
                    final MessageApi.SendMessageResult sendMessageResult = Wearable.MessageApi.sendMessage(googleApiClient, nodeId, MESSAGE_PATH, gsonString.getBytes()).await(2L, TimeUnit.SECONDS);
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "sendMessage: result: " + sendMessageResult.getStatus().getStatusMessage());
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "sendMessage: googleApiClient: can't connect");
                    }
                }
                googleApiClient.disconnect();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "sendMessage: stopSelf: " + pair.first);
                }
                if (pair.first) {
                    stopSelf(pair.second);
                }
            }
        });
    }

    @Override
    public void onMessageReceived(@NonNull com.google.android.gms.wearable.MessageEvent messageEvent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onMessageReceived: " + messageEvent);
        }
        if (!TextUtils.equals(messageEvent.getPath(), MESSAGE_PATH)) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onMessageReceived: incorrect path");
            }
            return;
        }

        final com.google.gson.Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        final String message = new String(messageEvent.getData());
        WearMessageEvent event = gson.fromJson(message, WearMessageEvent.class);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onMessageReceived: getEventType: " + event.getEventType());
            MyLog.i(CLS_NAME, "onMessageReceived: getEventMessage: " + event.getEventMessage());
            MyLog.i(CLS_NAME, "onMessageReceived: getEventLocale: " + event.getEventLocale());
            MyLog.i(CLS_NAME, "onMessageReceived: getEventUri: " + event.getEventUri());
        }
        final java.util.Locale vrLocale = ai.saiy.android.utils.SPH.getVRLocale(getApplicationContext());
        switch (event.getEventType()) {
            case WearMessageEvent.EVENT_SPEECH:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onMessageReceived: EVENT_SPEECH");
                }
                if (UtilsString.notNaked(event.getEventMessage())) {
                    final java.util.ArrayList<String> resultsRecognition = new java.util.ArrayList<>(1);
                    resultsRecognition.add(event.getEventMessage());
                    final float[] confidence = new float[] { 0.9f };
                    if (UtilsString.notNaked(event.getEventUri())) {
                        Intent intent = null;
                        try {
                            intent = Intent.parseUri(event.getEventUri(), Intent.URI_INTENT_SCHEME);
                        } catch (URISyntaxException e) {
                            if (DEBUG) {
                                MyLog.e(CLS_NAME, "onMessageReceived: URISyntaxException");
                            }
                        }
                        Bundle bundle;
                        if (intent != null) {
                            bundle = intent.getExtras();
                            int condition = Condition.CONDITION_NONE;
                            if (bundle != null && bundle.containsKey(LocalRequest.EXTRA_OBJECT)) {
                                condition = bundle.getInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_NONE);
                            }
                            switch (condition) {
                                case Condition.CONDITION_CONTACT:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "prepareMessage: Condition.CONDITION_CONTACT");
                                    }
                                    final ai.saiy.android.command.contact.CommandContactValues commandContactValues = gson.fromJson(bundle.getString(LocalRequest.EXTRA_OBJECT), ai.saiy.android.command.contact.CommandContactValues.class);
                                    bundle.remove(LocalRequest.EXTRA_OBJECT);
                                    bundle.putParcelable(LocalRequest.EXTRA_OBJECT, commandContactValues);
                                    break;
                                case Condition.CONDITION_NOTE:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "prepareMessage: Condition.CONDITION_NOTE");
                                    }
                                    final ai.saiy.android.command.note.NoteValues noteValues = gson.fromJson(bundle.getString(LocalRequest.EXTRA_OBJECT), ai.saiy.android.command.note.NoteValues.class);
                                    bundle.remove(LocalRequest.EXTRA_OBJECT);
                                    bundle.putParcelable(LocalRequest.EXTRA_OBJECT, noteValues);
                                    break;
                                case Condition.CONDITION_FACEBOOK:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "prepareMessage: Condition.CONDITION_FACEBOOK");
                                    }
                                    final ai.saiy.android.command.facebook.CommandFacebookValues commandFacebookValues = gson.fromJson(bundle.getString(LocalRequest.EXTRA_OBJECT), ai.saiy.android.command.facebook.CommandFacebookValues.class);
                                    bundle.remove(LocalRequest.EXTRA_OBJECT);
                                    bundle.putParcelable(LocalRequest.EXTRA_OBJECT, commandFacebookValues);
                                    break;
                                case Condition.CONDITION_TWITTER:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "prepareMessage: Condition.CONDITION_TWITTER");
                                    }
                                    final ai.saiy.android.command.twitter.CommandTwitterValues commandTwitterValues = gson.fromJson(bundle.getString(LocalRequest.EXTRA_OBJECT), ai.saiy.android.command.twitter.CommandTwitterValues.class);
                                    bundle.remove(LocalRequest.EXTRA_OBJECT);
                                    bundle.putParcelable(LocalRequest.EXTRA_OBJECT, commandTwitterValues);
                                    break;
                                default:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "prepareMessage: Condition.CONDITION_NONE");
                                    }
                                    if (bundle == null) {
                                        bundle = new Bundle();
                                    }
                                    break;
                            }
                        } else {
                            bundle = new Bundle();
                        }

                        bundle.putBoolean(LocalRequest.EXTRA_RESOLVED, true);
                        bundle.putString(LocalRequest.EXTRA_WEAR, messageEvent.getSourceNodeId());
                        bundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, resultsRecognition);
                        bundle.putFloatArray(SpeechRecognizer.CONFIDENCE_SCORES, confidence);
                        if (bundle.containsKey(LocalRequest.EXTRA_ACTION)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "prepareMessage: actionBundle: auto adding EXTRA_ACTION");
                            }
                        } else {
                            bundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_ONLY);
                        }
                        if (bundle.containsKey(LocalRequest.EXTRA_RECOGNITION_LANGUAGE)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "prepareMessage: actionBundle: auto adding EXTRA_RECOGNITION_LANGUAGE");
                            }
                        } else {
                            bundle.putString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE, vrLocale.toString());
                        }
                        if (bundle.containsKey(LocalRequest.EXTRA_TTS_LANGUAGE)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "prepareMessage: actionBundle: auto adding EXTRA_TTS_LANGUAGE");
                            }
                        } else {
                            bundle.putString(LocalRequest.EXTRA_TTS_LANGUAGE, ai.saiy.android.utils.SPH.getTTSLocale(getApplicationContext()).toString());
                        }
                        if (bundle.containsKey(LocalRequest.EXTRA_SUPPORTED_LANGUAGE)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "prepareMessage: actionBundle: auto adding EXTRA_SUPPORTED_LANGUAGE");
                            }
                        } else {
                            bundle.putSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE, ai.saiy.android.localisation.SupportedLanguage.getSupportedLanguage(vrLocale));
                        }
                        if (DEBUG) {
                            ai.saiy.android.utils.UtilsBundle.examineBundle(bundle);
                        }

                        final LocalRequest localRequest = new LocalRequest(getApplicationContext(), bundle);
                        localRequest.execute();
                        final WearMessageEvent wearMessageEvent = new WearMessageEvent(WearMessageEvent.EVENT_NONE, "speech received", vrLocale.toString(), null);
                        sendMessage(messageEvent.getSourceNodeId(), wearMessageEvent, new Pair<>(false, 0));
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onMessageReceived: message naked");
                    }
                }
                break;
            case WearMessageEvent.EVENT_DISPLAY:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onMessageReceived: EVENT_DISPLAY");
                }
                break;
            case WearMessageEvent.EVENT_UPDATE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onMessageReceived: EVENT_UPDATE");
                }
                final WearMessageEvent wearMessageEvent = new WearMessageEvent(WearMessageEvent.EVENT_UPDATE, "sending locale", ai.saiy.android.utils.UtilsLocale.convertToIETF(vrLocale), null);
                sendMessage(messageEvent.getSourceNodeId(), wearMessageEvent, new Pair<>(false, 0));
                break;
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onMessageReceived: EVENT_NONE");
                }
                break;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        this.then = System.nanoTime();
        this.googleApiClient = new GoogleApiClient.Builder(getApplicationContext()).addApi(Wearable.API).build();
        googleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
            MyLog.getElapsed(CLS_NAME, then);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onStartCommand");
        }
        if (intent != null) {
            final String action = intent.getAction();
            if (UtilsString.notNaked(action)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onStartCommand: " + action);
                }
                if (action.matches(getPackageName())) {
                    sendMessage(intent.getStringExtra(LocalRequest.EXTRA_WEAR), intent.getParcelableExtra(EXTRA_WEAR_MESSAGE), new Pair<>(true, startId));
                }
            } else if (DEBUG) {
                MyLog.i(CLS_NAME, "onStartCommand: naked");
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "onStartCommand: intent null");
        }
        return Service.START_STICKY;
    }
}

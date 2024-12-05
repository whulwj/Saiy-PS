package ai.saiy.android.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientLifecycleObserver;
import androidx.wear.ambient.AmbientLifecycleObserverKt;
import androidx.wear.widget.BoxInsetLayout;

import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.saiy.android.R;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SelfAwareVerbose;
import ai.saiy.android.wear.containers.WearMessageEvent;

public final class ActivityWearHome extends FragmentActivity implements ActivityCompat.OnRequestPermissionsResultCallback,
        MessageClient.OnMessageReceivedListener,
        AmbientLifecycleObserver.AmbientLifecycleCallback {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityWearHome.class.getSimpleName();

    private static final int REQ_AUDIO_PERMISSION = 76;
    private static final int REQ_SPEECH = 77;
    private static final String INTENT_STRING = "intent_string";
    private static final String MESSAGE_REQUEST_LOCALE = "requesting locale";
    private static final class SingleHolder {
        private static final ExecutorService DEFAULT = Executors.newSingleThreadExecutor();
    }

    private static final String MESSAGE_PATH = "/saiy_request";
    private BoxInsetLayout boxInsetLayout;
    private ImageButton buttonLogo;
    private ProgressBar progressBar;
    private TextView tvMessage;
    private Node node;
    private String locale;
    private boolean isFirstShow = true;
    private final AmbientLifecycleObserver ambientObserver = AmbientLifecycleObserverKt.AmbientLifecycleObserver(this, this);

    private void setMessage(final String text) {
        if (isActive()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMessage.setText(text);
                }
            });
        }
    }

    /**
     * Utility method to toast making sure it's on the main thread
     *
     * @param text   to toast
     * @param duration one of {@link Toast#LENGTH_SHORT} {@link Toast#LENGTH_LONG}
     */
    private void toast(@NonNull final String text, final int duration) {
        if (isActive()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), text, duration).show();
                }
            });
        }
    }

    private void startSpeechRequest(@Nullable String message, String eventUri) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startSpeechRequest: " + locale);
        }
        showProgress(true);
        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, (message != null) ? message : getString(R.string.app_name));
        if (eventUri != null) {
            intent.putExtra(INTENT_STRING, eventUri);
        }
        if (locale == null || locale.isEmpty()) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH.toString());
        } else {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale);
        }
        try {
            startActivityForResult(intent, REQ_SPEECH);
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "startSpeechRequest: ActivityNotFoundException");
            }
            toast(getString(R.string.error_speech_provider), Toast.LENGTH_SHORT);
        }
    }

    private boolean checkPermissionsResult(@NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void getConnectedNodes() {
        try {
            final List<Node> list = Tasks.await(Wearable.getNodeClient(getApplicationContext()).getConnectedNodes());
            if (list == null || list.isEmpty()) {
                return;
            }
            this.node = list.get(0);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "node: " + node.toString());
            }
            sendMessage(MESSAGE_REQUEST_LOCALE, WearMessageEvent.EVENT_UPDATE, null);
        } catch (InterruptedException e) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "Interrupt occurred");
            }
        } catch (ExecutionException e) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "Task failed");
            }
        }
    }

    private void updateDisplay() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "updateDisplay");
        }
        if (isActive()) {
            if (ambientObserver.isAmbient()) {
                boxInsetLayout.setBackgroundColor(Color.BLACK);
            } else {
                boxInsetLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorSaiyPurpleDark));
            }
        }
    }

    /**
     * Utility method to provide haptic feedback
     */
    private void vibrate() {
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(40L);
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onMessageReceived: " + messageEvent);
        }
        if (messageEvent.getPath().equals(MESSAGE_PATH)) {
            final WearMessageEvent wearMessageEvent = new GsonBuilder().disableHtmlEscaping().create().fromJson(new String(messageEvent.getData()), WearMessageEvent.class);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onMessageReceived: getEventType: " + wearMessageEvent.getEventType());
                MyLog.i(CLS_NAME, "onMessageReceived: getEventMessage: " + wearMessageEvent.getEventMessage());
                MyLog.i(CLS_NAME, "onMessageReceived: getEventLocale: " + wearMessageEvent.getEventLocale());
                MyLog.i(CLS_NAME, "onMessageReceived: getEventUri: " + wearMessageEvent.getEventUri());
            }
            this.locale = wearMessageEvent.getEventLocale();
            switch (wearMessageEvent.getEventType()) {
                case WearMessageEvent.EVENT_SPEECH:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onMessageReceived: EVENT_SPEECH");
                    }
                    vibrate();
                    setMessage(getString(R.string.title_waiting_google));
                    startSpeechRequest(wearMessageEvent.getEventMessage(), wearMessageEvent.getEventUri());
                    break;
                case WearMessageEvent.EVENT_DISPLAY:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onMessageReceived: EVENT_DISPLAY");
                    }
                    setMessage(wearMessageEvent.getEventMessage());
                    break;
                case WearMessageEvent.EVENT_UPDATE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onMessageReceived: EVENT_UPDATE");
                    }
                    break;
                default:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onMessageReceived: EVENT_NONE");
                    }
                    break;
            }
        }
    }

    public void sendMessage(String eventMessage, byte eventType, String eventUri) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sendMessage");
        }
        if (node == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "sendMessage: failed");
            }
        } else {
            final String gsonString = new GsonBuilder().disableHtmlEscaping().create().toJson(new WearMessageEvent(eventType, eventMessage, "", eventUri));
            if (DEBUG) {
                MyLog.i(CLS_NAME, "sendMessage: gsonString: " + gsonString);
            }
            Wearable.getMessageClient(this).sendMessage(node.getId(), MESSAGE_PATH, gsonString.getBytes());
        }
    }

    /**
     * Utility method to show or hide the progress bar
     *
     * @param visible true to show, false to hide
     */
    private void showProgress(final boolean visible) {
        if (isActive()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
                }
            });
        }
    }

    @Override
    public void onEnterAmbient(@NonNull AmbientLifecycleObserver.AmbientDetails ambientDetails) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onEnterAmbient");
        }
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onExitAmbient");
        }
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onUpdateAmbient");
        }
        updateDisplay();
    }

    private boolean isActive() {
        return !isDestroyed() && !isFinishing();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onActivityResult");
        }
        if (requestCode != REQ_SPEECH) {
            return;
        }
        SingleHolder.DEFAULT.execute(new Runnable() {
            @Override
            public void run() {
                if (resultCode == Activity.RESULT_OK && intent != null) {
                    final ArrayList<String> recognitionResults = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (DEBUG) {
                        SelfAwareVerbose.logSpeechResults(intent.getExtras());
                    }
                    if (recognitionResults != null && !recognitionResults.isEmpty()) {
                        final String recognitionResult = recognitionResults.get(0);
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onActivityResult: " + recognitionResult);
                        }
                        if (recognitionResult != null && !recognitionResult.isEmpty()) {
                            sendMessage(recognitionResult, WearMessageEvent.EVENT_SPEECH, intent.getStringExtra(INTENT_STRING));
                            setMessage(recognitionResult);
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "onActivityResult: speech naked");
                            }
                            setMessage(getString(R.string.self_aware));
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onActivityResult: missing result");
                        }
                        setMessage(getString(R.string.self_aware));
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: failed result");
                    }
                    setMessage(getString(R.string.self_aware));
                }
                showProgress(false);
            }
        });
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.layout_activity_wear_home);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
            SelfAwareVerbose.examineBundle(bundle);
        }
        this.progressBar = findViewById(R.id.pbSpeech);
        this.boxInsetLayout = findViewById(R.id.container);
        this.buttonLogo = findViewById(R.id.ibSaiyLogo);
        this.tvMessage = findViewById(R.id.tvMessage);
        this.tvMessage.setSelected(true);
        getLifecycle().addObserver(ambientObserver);
        boxInsetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrate();
                setMessage(getString(R.string.title_waiting_google));
                buttonLogo.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake));
                startSpeechRequest(null, null);
            }
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "PERMISSION_DENIED");
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQ_AUDIO_PERMISSION);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "PERMISSION_GRANTED");
        }
        SingleHolder.DEFAULT.execute(new Runnable() {
            @Override
            public void run() {
                getConnectedNodes();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
        getLifecycle().removeObserver(ambientObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPause");
        }
        Wearable.getMessageClient(this).removeListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_AUDIO_PERMISSION) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onRequestPermissionsResult: REQ_AUDIO_PERMISSION");
            }
            if (checkPermissionsResult(grantResults)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onRequestPermissionsResult: REQ_AUDIO_PERMISSION: PERMISSION_GRANTED");
                }
                sendMessage(MESSAGE_REQUEST_LOCALE, WearMessageEvent.EVENT_UPDATE, null);
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onRequestPermissionsResult: REQ_AUDIO_PERMISSION: PERMISSION_DENIED");
                }
                toast(":(", Toast.LENGTH_LONG);
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onResume");
        }
        Wearable.getMessageClient(this).addListener(this);
        buttonLogo.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake));
        if (this.isFirstShow) {
            this.isFirstShow = false;
        } else {
            vibrate();
        }
    }

    @Override
    protected void onStart() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onStart");
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onStop");
        }
        super.onStop();
    }
}

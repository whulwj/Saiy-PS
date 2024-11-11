/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.service;

import android.app.Application;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.core.app.ServiceCompat;

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;
import com.google.gson.GsonBuilder;
import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import ai.saiy.android.R;
import ai.saiy.android.amazon.TokenHelper;
import ai.saiy.android.amazon.directives.DirectiveType;
import ai.saiy.android.api.DeclinedBinder;
import ai.saiy.android.api.SaiyDefaults;
import ai.saiy.android.api.helper.CallbackType;
import ai.saiy.android.api.remote.Request;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.audio.AudioParameters;
import ai.saiy.android.audio.RecognitionMic;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.BeyondVerbal;
import ai.saiy.android.cognitive.identity.provider.microsoft.SpeakerEnrollment;
import ai.saiy.android.cognitive.identity.provider.microsoft.SpeakerIdentification;
import ai.saiy.android.cognitive.motion.provider.google.MotionRecognition;
import ai.saiy.android.command.driving.DrivingProfileHelper;
import ai.saiy.android.command.translate.provider.TranslationProvider;
import ai.saiy.android.command.translate.provider.bing.BingCredentials;
import ai.saiy.android.configuration.MicrosoftConfiguration;
import ai.saiy.android.contacts.ContactHelper;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.lib.ProcessStateOwner;
import ai.saiy.android.nlu.apiai.ApiRequest;
import ai.saiy.android.nlu.apiai.ResolveAPIAI;
import ai.saiy.android.nlu.local.InitStrings;
import ai.saiy.android.partial.PartialHelper;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.recognition.RecognitionAction;
import ai.saiy.android.recognition.SaiyHotwordListener;
import ai.saiy.android.recognition.SaiyRecognitionListener;
import ai.saiy.android.recognition.helper.GoogleNowMonitor;
import ai.saiy.android.recognition.provider.amazon.AlexaTTS;
import ai.saiy.android.recognition.provider.amazon.RecognitionAmazon;
import ai.saiy.android.recognition.provider.bluemix.RecognitionBluemix;
import ai.saiy.android.recognition.provider.google.chromium.RecognitionGoogleChromium;
import ai.saiy.android.recognition.provider.google.cloud.RecognitionGoogleCloud;
import ai.saiy.android.recognition.provider.microsoft.RecognitionMicrosoft;
import ai.saiy.android.recognition.provider.nuance.RecognitionNuance;
import ai.saiy.android.recognition.provider.remote.RecognitionRemote;
import ai.saiy.android.recognition.provider.sphinx.RecognitionSphinx;
import ai.saiy.android.recognition.provider.wit.RecognitionWit;
import ai.saiy.android.recognition.provider.wit.RecognitionWitHybrid;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.service.helper.SelfAwareCache;
import ai.saiy.android.service.helper.SelfAwareConditions;
import ai.saiy.android.service.helper.SelfAwareParameters;
import ai.saiy.android.service.helper.SelfAwareVerbose;
import ai.saiy.android.sound.VolumeHelper;
import ai.saiy.android.tts.SaiyProgressListener;
import ai.saiy.android.tts.SaiyTextToSpeech;
import ai.saiy.android.tts.TTS;
import ai.saiy.android.tts.engine.EngineNuance;
import ai.saiy.android.tts.helper.PendingTTS;
import ai.saiy.android.tts.helper.SpeechPriority;
import ai.saiy.android.ui.notification.NotificationHelper;
import ai.saiy.android.utils.BluetoothConstants;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsBundle;
import ai.saiy.android.utils.UtilsString;
import ai.saiy.android.wear.UtilsWearMessage;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * This foreground service class will remain running unless the user deactivates it
 * from the Saiy application settings, or the device becomes extremely low on memory. It is started
 * at boot. A permanent notification is required in order for Android to keep the service alive.
 * <p/>
 * Despite its name, as much as possible (always), this service should be stateless in terms of
 * performing requests, consider it a RESTful service. Requests that relate to or depend upon previous
 * actions should not be handled directly here. Instead, parameters should be provided with each request,
 * most often in the form of a {@link Bundle} that is subsequently actioned and then forgotten.
 * <p/>
 * This class does however manage the Text to Speech and Voice Recognition objects, as its lifecycle dictates
 * how and when these are required. With the management of these objects comes the responsibility of
 * handling bugs and idiosyncratic conditions....
 * <p>
 * Ideally, this service would run in its own process, but the requirement to behave according to the global
 * application settings, would make it less than trivial.
 * <p>
 * The service is exposed to remote connections, for applications that wish to utilise features of
 * Saiy. This class manages their connection and execution and the conditions that surround them.
 * <p>
 * To keep such an important class as readable and concise as possible, tasks are resolved in the
 * helper classes {@link SelfAwareConditions} {@link SelfAwareParameters} {@link SelfAwareCache}
 * wherever possible.
 * <p>
 * Created by benrandall76@gmail.com on 06/02/2016.
 */
public class SelfAware extends Service {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SelfAware.class.getSimpleName();

    private static final int MAX_INIT_ATTEMPTS = 4;
    private static final long SPEECH_ELAPSED_WARNING = 150;
    private static final long WARM_UP_SLEEP = 500L;
    private static final int WARM_UP_LOOP = 4;

    private static final long OKAY_GOOGLE_DELAY = 500L;

    private static final long MONITOR_ENGINE = 10000L;

    private volatile SaiyTextToSpeech tts;

    private volatile EngineNuance en;

    private volatile RecognitionNuance recogNuance;
    private volatile RecognitionGoogleCloud recogGoogleCloud;
    private volatile RecognitionGoogleChromium recogGoogleChromium;
    private volatile RecognitionMicrosoft recogOxford;
    private volatile RecognitionWit recogWit;
    private volatile RecognitionWitHybrid recogWitHybrid;
    private volatile RecognitionBluemix recogIBM;
    private volatile RecognitionRemote recogRemote;
    private volatile RecognitionSphinx recogSphinx;
    private volatile RecognitionAmazon recogAmazon;
    private volatile RecognitionMic recogMic;
    private volatile SpeechRecognizer recogNative;

    private volatile SelfAwareConditions conditions;
    private volatile SelfAwareParameters params;
    private volatile SelfAwareCache cache;

    private volatile PartialHelper partialHelper;
    private volatile PendingTTS pendingTTS;
    private final AtomicInteger initCount = new AtomicInteger();
    private volatile Disposable disposable;

    private final MotionRecognition motionRecognition = new MotionRecognition();

    private final AtomicBoolean initPending = new AtomicBoolean();

    private final IBinder localBinder = new BoundSA();
    private static SelfAware instance = null;

    private TelephonyManager telephonyManager;

    /**
     * Exposed instance for local binding.
     */
    public class BoundSA extends Binder {
        public SelfAware getService() {
            return SelfAware.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }

        setInstance();
        startForeground(NotificationHelper.NOTIFICATION_SELF_AWARE);
        conditions = new SelfAwareConditions(getApplicationContext(), getTelephonyManager());
        params = new SelfAwareParameters(getApplicationContext());
        cache = new SelfAwareCache(getApplicationContext());

        if (SPH.getOverrideSecureDriving(getApplicationContext()) || SPH.getHotwordStartDriving(getApplicationContext()) || SPH.getHotwordStopDriving(getApplicationContext()) || DrivingProfileHelper.isAutomatic(getApplicationContext())) {
            motionRecognition.prepare(getApplicationContext());
            motionRecognition.connect();
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate: motion activity recognition not required");
        }
        this.conditions.handleReinstallation();
    }

    /**
     * We check here to see if the Text to Speech Engine should be warmed up, to avoid any future
     * initialisation delays. A 'race condition' of attempting to instantiate multiple TTS objects
     * is avoided by using the {@link #initSaiyTTS()} and following {@link #initTTS()} helper methods.
     */
    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (!UtilsBundle.isSuspicious(intent)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onStartCommand: " + startId);
                MyLog.d(CLS_NAME, "onStartCommand: package: " + conditions.getPackage(intent));
            }

            if (conditions.shouldWarmUp(tts, intent)) {
                initTTS();
                restartStatusMonitor();
            }

            new InitStrings(getApplicationContext()).init();

            switch (conditions.checkNotificationInstruction(intent)) {
                case Condition.CONDITION_TUTORIAL:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onStartCommand: CONDITION_TUTORIAL");
                    }
                    startForeground(NotificationHelper.NOTIFICATION_TUTORIAL);
                    break;
                case Condition.CONDITION_SELF_AWARE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onStartCommand: CONDITION_SELF_AWARE");
                    }
                    startForeground(NotificationHelper.NOTIFICATION_SELF_AWARE);
                    break;
                case Condition.CONDITION_NONE:
                default:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onStartCommand: CONDITION_NONE");
                    }
                    break;
            }

        } else {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "onStartCommand: intent suspicious: stopping: " + startId);
            }

            this.stopSelf(startId);
        }

        return Service.START_STICKY;

    }


    @Override
    public IBinder onBind(final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBind");
        }

        final Pair<Boolean, Boolean> shouldBind = conditions.shouldBind(intent);

        if (shouldBind.first) {
            restartStatusMonitor();

            new InitStrings(getApplicationContext()).init();

            if (shouldBind.second) {
                return localBinder;
            } else {
                return remoteBinder;
            }
        }

        return new DeclinedBinder();
    }

    @Override
    public void onRebind(final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onRebind");
        }

        if (conditions.shouldRebind(intent)) {
            restartStatusMonitor();
            super.onRebind(intent);
        }
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onUnbind");
        }
        return conditions.shouldUnbind(intent);
    }

    /**
     * Remote access for external bound clients
     */
    private final ISaiy.Stub remoteBinder = new ISaiy.Stub() {

        @Override
        public boolean isReal() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "remoteBinder: isReal");
                MyLog.v(CLS_NAME, "remoteBinder: calling app: "
                        + getPackageManager().getNameForUid(Binder.getCallingUid()));
            }
            return true;
        }

        @Override
        public void speakListen(final ISaiyListener rl, final Bundle bundle) throws RemoteException {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "remoteBinder: speakListen");
                MyLog.v(CLS_NAME, "remoteBinder: calling app: "
                        + getPackageManager().getNameForUid(Binder.getCallingUid()));
            }

            if (conditions.checkSaiyRemotePermission()) {
                if (conditions.shouldAction(rl, bundle)) {
                    ((SelfAware.BoundSA) localBinder).getService().
                            speakListen(bundle);
                }
            } else {
                throw new RemoteException(getString(ai.saiy.android.R.string.error_missing_permissions));
            }
        }

        @Override
        public void speakOnly(final ISaiyListener rl, final Bundle bundle) throws RemoteException {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "remoteBinder: speakOnly");
                MyLog.v(CLS_NAME, "remoteBinder: calling app: "
                        + getPackageManager().getNameForUid(Binder.getCallingUid()));
            }

            if (conditions.checkSaiyRemotePermission()) {
                if (conditions.shouldAction(rl, bundle)) {
                    ((SelfAware.BoundSA) localBinder).getService().
                            speakOnly(bundle);
                }
            } else {
                throw new RemoteException(getString(ai.saiy.android.R.string.error_missing_permissions));
            }
        }
    };

    /**
     * Check if the hotword detection is currently active
     *
     * @return {@link Pair} with the first parameter denoting if the hotword is active, and the second if
     * it is set to restart
     */
    protected Pair<Boolean, Boolean> isHotwordActive() {
        return new Pair<>(conditions.isHotwordActive(recogSphinx), conditions.restartHotword());
    }

    /**
     * Check if the recognition is currently in use
     *
     * @return true if the state is {@link Recognition.State#LISTENING}
     * or {@link Recognition.State#PROCESSING}. Note, active hotword detection is not considered here.
     */
    protected boolean isListening() {
        return !conditions.isHotwordActive(recogSphinx) && conditions.isListening();
    }

    /**
     * Check if the tts engine is currently in use
     *
     * @return a {@link Pair}, which #first field will be true if the engine is speaking
     */
    protected Pair<Boolean, Integer> isSpeaking() {
        return conditions.isSpeaking(tts);
    }

    /**
     * Called to start the hotword detection.
     *
     * @param bundle of instructions
     */
    @SuppressWarnings("UnusedParameters")
    protected void startHotwordDetection(@NonNull final Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startHotwordDetection");
            MyLog.v(CLS_NAME, "startHotwordDetection: calling app: "
                    + getPackageManager().getNameForUid(Binder.getCallingUid()));
            MyLog.i(CLS_NAME, "startHotwordDetection: isMain thread: " + (Looper.myLooper() == Looper.getMainLooper()));
            MyLog.i(CLS_NAME, "startHotwordDetection: threadTid: " + android.os.Process.getThreadPriority(android.os.Process.myTid()));
        }

        Schedulers.computation().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                recogSphinx = conditions.getSphinxRecognition(hotwordListener);
                recogSphinx.startListening();
            }
        });

        conditions.onVRComplete();
    }

    /**
     * Called when voice recognition should begin once this utterance has completed
     *
     * @param bundle of instructions
     */
    protected void speakListen(@NonNull final Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "speakListen");
            MyLog.v(CLS_NAME, "speakListen: calling app: "
                    + getPackageManager().getNameForUid(Binder.getCallingUid()));
            MyLog.i(CLS_NAME, "speakListen: isMain thread: " + (Looper.myLooper() == Looper.getMainLooper()));
            MyLog.i(CLS_NAME, "speakListen: threadTid: " + android.os.Process.getThreadPriority(android.os.Process.myTid()));
        }

        speak(true, bundle);
    }

    /**
     * Called when only speech is required with no voice recognition on the completion
     * of the utterance
     *
     * @param bundle of instructions
     */
    protected void speakOnly(@NonNull final Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "speakOnly");
            MyLog.v(CLS_NAME, "speakOnly: calling app: "
                    + getPackageManager().getNameForUid(Binder.getCallingUid()));
            MyLog.i(CLS_NAME, "speakOnly: isMain thread: " + (Looper.myLooper() == Looper.getMainLooper()));
            MyLog.i(CLS_NAME, "speakOnly: threadTid: " + android.os.Process.getThreadPriority(android.os.Process.myTid()));
        }

        speak(false, bundle);
    }

    /**
     * Called to initiate speech
     *
     * @param bundle        of instructions
     * @param isSpeakListen true if the recognition should begin once the utterance has completed
     */
    private void speak(final boolean isSpeakListen, @NonNull final Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "speak");
        }

        NotificationHelper.cancelComputingNotification(getApplicationContext());

        if (conditions.checkSaiyPermission(Binder.getCallingUid())) {
            conditions.checkReinstallation(bundle);
            if (conditions.isHotwordActive(recogSphinx)) {
                stopListening(false);
            }

            final Pair<Pair<Boolean, Boolean>, Pair<Boolean, Boolean>> priorityPair = conditions.proceedPriority(tts, bundle);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "speak: priorityPair: " + priorityPair.first.first + " ~ " + priorityPair.first.second);
            }

            if (priorityPair.first.first) {

                if (!priorityPair.second.second) {

                    if (priorityPair.first.second) {
                        stopSpeech(true);
                    }

                    if (priorityPair.first.second || !priorityPair.second.first || conditions.isQueueAdd(bundle)) {
                        if (!conditions.servingRemote() && bundle.containsKey(LocalRequest.EXTRA_RESOLVED)) {
                            bundle.remove(LocalRequest.EXTRA_RESOLVED);
                            new ai.saiy.android.recognition.RecognitionAction(getApplicationContext(), conditions.getVRLocale(false), conditions.getTTSLocale(false), conditions.getSupportedLanguage(false), bundle);
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "speak: isResolved: true");
                            }
                            return;
                        }
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "speak: isResolved: false");
                        }
                        switch (conditions.getDefaultTTS()) {
                            case LOCAL:
                                conditions.setInitialisingCountdown();
                                break;
                            case NETWORK_NUANCE:
                                conditions.setFetchingCountdown();
                                break;
                        }
                        if (bundle.containsKey(LocalRequest.EXTRA_RECOGNITION_PROVIDER)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "speak: EXTRA_RECOGNITION_PROVIDER: true");
                            }
                            SPH.setDefaultRecognition(getApplicationContext(), SaiyDefaults.VR.ALEXA);
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "speak: EXTRA_RECOGNITION_PROVIDER: NATIVE");
                            }
                            SPH.setDefaultRecognition(getApplicationContext(), SaiyDefaults.VR.NATIVE);
                        }

                        if (isSpeakListen) {
                            switch (conditions.getDefaultRecognition()) {
                                case GOOGLE_CLOUD:
                                    switch (Recognition.getState()) {
                                        case IDLE:
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "GOOGLE_CLOUD: IDLE");
                                            }
                                            Schedulers.computation().scheduleDirect(new Runnable() {
                                                @Override
                                                public void run() {
                                                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "speakListen: GOOGLE_CLOUD: warming up");
                                                    }

                                                    recogMic = conditions.getMicRecognition(null,
                                                            AudioParameters.getDefaultMicrosoft(),
                                                            false, 0, true, false);
                                                    recogGoogleCloud = conditions.getGoogleCloudRecognition(recogMic, recognitionListener);
                                                }
                                            });
                                            break;
                                    }
                                    break;
                                case IBM:
                                    switch (Recognition.getState()) {
                                        case IDLE:
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "IBM: IDLE");
                                            }
                                            Schedulers.computation().scheduleDirect(new Runnable() {
                                                @Override
                                                public void run() {
                                                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "speakListen: IBM: warming up");
                                                    }

                                                    recogMic = conditions.getMicRecognition(null,
                                                            AudioParameters.getDefaultMicrosoft(),
                                                            false, 0, true, false);
                                                    recogIBM = conditions.getIBMRecognition(recogMic, recognitionListener);
                                                }
                                            });
                                            break;
                                    }
                                    break;
                                case ALEXA:
                                    switch (Recognition.getState()) {
                                        case IDLE:
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "AMAZON: IDLE");
                                            }
                                            Schedulers.computation().scheduleDirect(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "speakListen: AMAZON: warming up");
                                                    }
                                                    SelfAware.this.recogAmazon = SelfAware.this.conditions.getAmazonRecognition(SelfAware.this.recognitionListener);
                                                }
                                            });
                                            break;
                                    }
                                    break;
                                case WIT_HYBRID:
                                    switch (Recognition.getState()) {
                                        case IDLE:
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "WIT_HYBRID: IDLE");
                                            }
                                            Schedulers.computation().scheduleDirect(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "speakListen: WIT_HYBRID: warming up");
                                                    }
                                                    SelfAware.this.recogWitHybrid = SelfAware.this.conditions.getWitHybridRecognition(SelfAware.this.recognitionListener);
                                                }
                                            });
                                            break;
                                    }
                                case MICROSOFT:
                                    switch (Recognition.getState()) {
                                        case IDLE:
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "MICROSOFT: IDLE");
                                            }
                                            Schedulers.computation().scheduleDirect(new Runnable() {
                                                @Override
                                                public void run() {
                                                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "speakListen: MICROSOFT: warming up");
                                                    }
                                                    recogOxford = conditions.getMicrosoftRecognition(recognitionListener);
                                                }
                                            });
                                            break;
                                    }
                                    break;
                            }
                        }

                        restartEngineMonitor();
                        params.setParams(isSpeakListen, conditions, bundle);

                        /*
                         * Bundle is replace here
                         */
                        if (conditions.checkConditions(tts, conditions.getDefaultTTS(), bundle)) {

                            final String utterance = conditions.getUtterance();

                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "speak: " + utterance);
                            }
                            switch (SPH.getHeadsetSystem(getApplicationContext())) {
                                case BluetoothConstants.SYSTEM_ONE:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "slRun: BluetoothConstants.SYSTEM_ONE");
                                    }
                                    if (isSpeakListen) {
                                        conditions.startBluetoothAudio();
                                    }
                                    break;
                                case BluetoothConstants.SYSTEM_TWO:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "slRun: BluetoothConstants.SYSTEM_TWO");
                                    }
                                    break;
                                case BluetoothConstants.SYSTEM_THREE:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "slRun: BluetoothConstants.SYSTEM_THREE");
                                    }
                                    conditions.startBluetoothAudio();
                                    break;
                            }

                            if (!conditions.isSilentUtterance()) {
                                if (conditions.isWearRequest()) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "speak: isWearRequest");
                                    }
                                    UtilsWearMessage.sendMessage(getApplicationContext(), conditions.getUtterance(), conditions.getWearContent(), conditions.getVRLocale(false), ai.saiy.android.wear.containers.WearMessageEvent.EVENT_DISPLAY, null);
                                }

                                switch (conditions.getDefaultTTS()) {

                                    case LOCAL:
                                        conditions.setVoice(tts, params);
                                        doSpeech(tts.speak(utterance, conditions.getQueueType(),
                                                params, params.getUtteranceId()),
                                                conditions.getQueueType(), utterance);
                                        break;
                                    case NETWORK_NUANCE:

                                        if (conditions.servingRemote()) {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "speak: setting MainLooper");
                                            }

                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    en = conditions.getEngineNuance(params, progressListener);
                                                    en.startSpeech(utterance);
                                                }
                                            });
                                        } else {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "speak: not setting MainLooper");
                                            }

                                            en = conditions.getEngineNuance(params, progressListener);
                                            en.startSpeech(utterance);
                                        }

                                        break;
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "speak: processing silence");
                                }
                                conditions.removeRunnableCallback(engineMonitor);
                                progressListener.onDone(params.getUtteranceId());
                            }
                        } else {
                            speechRequestError(conditions.getQueueType(), conditions.getUtterance());
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "speak: isSpeaking: not queue add");
                        }
                        stopSpeech(false);
                        conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "speak: isListening");
                    }
                    stopListening(false);
                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "speak: priority lower, ignoring");
                }
                conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
            }
        } else {
            MyLog.e(CLS_NAME, getString(ai.saiy.android.R.string.error_missing_permissions));
            conditions.manageCallback(CallbackType.CB_ERROR_DEVELOPER, null);
        }
    }

    protected void alexaTTS(Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "alexaTTS");
            MyLog.v(CLS_NAME, "alexaTTS: calling app: " + getPackageManager().getNameForUid(Binder.getCallingUid()));
            MyLog.i(CLS_NAME, "alexaTTS: isMain thread: " + (Looper.myLooper() == Looper.getMainLooper()));
            MyLog.i(CLS_NAME, "alexaTTS: threadTid: " + Process.getThreadPriority(Process.myTid()));
        }
        new AlexaTTS(getApplicationContext(), recognitionListener, conditions.getTTSLocale(false), bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0), tts.getInitialisedEngine(), tts.getEngines());
        bundle.putInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_ALEXA_TTS);
        new RecognitionAction(getApplicationContext(), conditions.getVRLocale(false), conditions.getTTSLocale(false), conditions.getSupportedLanguage(false), bundle);
    }

    /**
     * Called to stop speech
     */
    protected void stopSpeech(final boolean preventRecognition) {
        conditions.stopSpeech(tts, params, en, preventRecognition);
        resetPendingConditions();
    }

    protected void toggleDrivingProfile(Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "toggleDrivingProfile");
            MyLog.v(CLS_NAME, "toggleDrivingProfile: calling app: " + getPackageManager().getNameForUid(Binder.getCallingUid()));
            MyLog.i(CLS_NAME, "toggleDrivingProfile: isMain thread: " + (Looper.myLooper() == Looper.getMainLooper()));
            MyLog.i(CLS_NAME, "toggleDrivingProfile: threadTid: " + Process.getThreadPriority(Process.myTid()));
        }
        if (DrivingProfileHelper.isEnabled(getApplicationContext())) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "toggleDrivingProfile: disabling");
            }
            DrivingProfileHelper.disable(getApplicationContext());
            Pair<Boolean, Boolean> isHotwordActive = isHotwordActive();
            if (isHotwordActive.first || isHotwordActive.second) {
                stopListening(true);
            } else {
                startForeground(NotificationHelper.NOTIFICATION_SELF_AWARE);
            }
            return;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "toggleDrivingProfile: enabling");
        }
        DrivingProfileHelper.enable(getApplicationContext());
        if (!DrivingProfileHelper.isStartHotwordEnabled(getApplicationContext())) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "toggleDrivingProfile: enabling: no hotword");
            }
            startForeground(NotificationHelper.NOTIFICATION_DRIVING_PROFILE);
            return;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "toggleDrivingProfile: enabling: need hotword");
        }
        Pair<Boolean, Boolean> isHotwordActive = isHotwordActive();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "toggleDrivingProfile: enabling hotwordPair.first: " + isHotwordActive.first);
            MyLog.i(CLS_NAME, "toggleDrivingProfile: enabling hotwordPair.second: " + isHotwordActive.second);
        }
        if (isHotwordActive.first || isHotwordActive.second) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "toggleDrivingProfile: enabling foreground");
            }
            startForeground(NotificationHelper.NOTIFICATION_DRIVING_PROFILE);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "toggleDrivingProfile: enabling hotword");
            }
            startHotwordDetection(bundle);
        }
    }

    /**
     * Stop the recognition currently in use
     *
     * @param shutdown true if the hotword detection should be permanently shutdown
     */
    protected void stopListening(final boolean shutdown) {
        conditions.setHotwordShutdown(recogSphinx, shutdown);
        conditions.stopListening(recogNuance, recogGoogleCloud, recogGoogleChromium, recogOxford,
                recogWit, recogIBM, recogRemote, recogMic, recogNative, recogSphinx, recogAmazon, recogWitHybrid);
    }

    /**
     * Although we can't rely on this reporting correctly, due to weird and wonderful conditions,
     * we use it as a safety net by exposing a static instance.
     */
    public static boolean checkInstance() {
        return SelfAware.instance != null;
    }

    /**
     * Runnable for speech only commands
     */
    private final Runnable soRun = new Runnable() {

        @Override
        public void run() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "soRun");
            }

            conditions.removeRunnableCallback(this);

            switch (conditions.getCondition()) {

                case Condition.CONDITION_TRANSLATION:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "soRun: Condition.CONDITION_TRANSLATION");
                    }

                    if (!TranslationProvider.shouldAction(getApplicationContext(),
                            conditions.getSupportedLanguage(conditions.servingRemote()))) {

                        if (conditions.restartHotword()) {
                            startHotwordDetection(conditions.getBundle());
                        }
                    }
                    break;
                case Condition.CONDITION_TUTORIAL:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "soRun: Condition.CONDITION_TUTORIAL");
                    }
                    new ai.saiy.android.tutorial.Tutorial(getApplicationContext(), conditions.getVRLocale(false), conditions.getTTSLocale(false), conditions.getSupportedLanguage(false), conditions.getBundle()).execute();
                    break;
                case Condition.CONDITION_EASTER_EGG:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "soRun: Condition.CONDITION_EASTER_EGG");
                    }
                    if (ai.saiy.android.command.easter_egg.EasterEggHunter.STAGE_4 == SPH.getEasterEggState(getApplicationContext())) {
                        SPH.setEasterEggState(getApplicationContext(), ai.saiy.android.command.easter_egg.EasterEggHunter.STAGE_5);
                        ai.saiy.android.command.easter_egg.EasterEggLocal.shareIntent(getApplicationContext());
                        break;
                    }
                case Condition.CONDITION_NONE:
                default:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "soRun: Condition.NONE");
                    }

                    System.gc();

                    if (conditions.restartHotword()) {
                        startHotwordDetection(conditions.getBundle());
                    }

                    break;
            }
            switch (SPH.getHeadsetSystem(getApplicationContext())) {
                case BluetoothConstants.SYSTEM_ONE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "soRun: BluetoothConstants.SYSTEM_ONE");
                    }
                    break;
                case BluetoothConstants.SYSTEM_TWO:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "soRun: BluetoothConstants.SYSTEM_TWO");
                    }
                    break;
                case BluetoothConstants.SYSTEM_THREE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "soRun: BluetoothConstants.SYSTEM_THREE");
                    }
                    conditions.stopBluetoothAudio();
                    break;
            }

            SPH.setLastUsed(getApplicationContext());
            conditions.doPeriodic();
        }
    };

    /**
     * Runnable for speech then recognition commands
     */
    private final Runnable slRun = new Runnable() {

        @Override
        public void run() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "slRun");
            }

            if (conditions.checkAudioPermission()) {

                if (conditions.isNetworkAvailable() ||
                        conditions.getDefaultRecognition() == SaiyDefaults.VR.NATIVE) {
                    switch (SPH.getHeadsetSystem(getApplicationContext())) {
                        case BluetoothConstants.SYSTEM_ONE:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "slRun: BluetoothConstants.SYSTEM_ONE");
                            }
                            break;
                        case BluetoothConstants.SYSTEM_TWO:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "slRun: BluetoothConstants.SYSTEM_TWO");
                            }
                            conditions.startBluetoothAudio();
                            break;
                        case BluetoothConstants.SYSTEM_THREE:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "slRun: BluetoothConstants.SYSTEM_THREE");
                            }
                            break;
                    }

                    switch (conditions.getDefaultRecognition()) {

                        case NUANCE:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "NUANCE/NUANCE_NLU");
                            }

                            switch (Recognition.getState()) {

                                case IDLE:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "NUANCE: IDLE");
                                    }

                                    recognitionListener.resetBugVariables();
                                    recogNuance = conditions.getNuanceRecognition(recognitionListener);
                                    recogNuance.startListening();

                                    break;
                                case PROCESSING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "NUANCE: PROCESSING");
                                    }

                                    recogNuance.cancelListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                                case LISTENING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "NUANCE: LISTENING");
                                    }

                                    recogNuance.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                            }

                            break;

                        case GOOGLE_CLOUD:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "GOOGLE_CLOUD");
                            }

                            switch (Recognition.getState()) {

                                case IDLE:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "GOOGLE_CLOUD: IDLE");
                                    }

                                    recognitionListener.resetBugVariables();

                                    Schedulers.computation().scheduleDirect(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!waitGoogleCloud()) {
                                                if (DEBUG) {
                                                    MyLog.w(CLS_NAME, "GOOGLE_CLOUD: IDLE: failed warm up loop");
                                                }

                                                recogMic = conditions.getMicRecognition(null,
                                                        AudioParameters.getDefaultMicrosoft(), false, 0, true, false);
                                                recogGoogleCloud = conditions.getGoogleCloudRecognition(recogMic,
                                                        recognitionListener);
                                            }

                                            recogGoogleCloud.startListening();
                                        }
                                    });

                                    break;
                                case PROCESSING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "GOOGLE_CLOUD: PROCESSING");
                                    }

                                    recogGoogleCloud.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                                case LISTENING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "GOOGLE_CLOUD: LISTENING");
                                    }

                                    recogGoogleCloud.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                            }

                            break;

                        case GOOGLE_CHROMIUM:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "GOOGLE_CHROMIUM");
                            }

                            switch (Recognition.getState()) {

                                case IDLE:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "GOOGLE_CHROMIUM: IDLE");
                                    }

                                    recognitionListener.resetBugVariables();

                                    Schedulers.computation().scheduleDirect(new Runnable() {
                                        @Override
                                        public void run() {
                                            recogGoogleChromium = conditions.getGoogleChromiumRecognition(recognitionListener);
                                            recogGoogleChromium.startListening();
                                        }
                                    });

                                    break;
                                case PROCESSING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "GOOGLE_CHROMIUM: PROCESSING");
                                    }

                                    recogGoogleChromium.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                                case LISTENING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "GOOGLE_CHROMIUM: LISTENING");
                                    }

                                    recogGoogleChromium.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                            }

                            break;

                        case MICROSOFT:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "MICROSOFT");
                            }

                            switch (Recognition.getState()) {

                                case IDLE:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "MICROSOFT: IDLE");
                                    }

                                    conditions.setFetchingCountdown();
                                    recognitionListener.resetBugVariables();

                                    Schedulers.computation().scheduleDirect(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!waitMicrosoft()) {
                                                if (DEBUG) {
                                                    MyLog.w(CLS_NAME, "MICROSOFT: IDLE: failed warm up loop");
                                                }
                                                recogOxford = conditions.getMicrosoftRecognition(recognitionListener);
                                            }
                                            recogOxford.startListening();
                                        }
                                    });
                                    break;
                                case PROCESSING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "MICROSOFT: PROCESSING");
                                    }

                                    recogOxford.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                                case LISTENING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "MICROSOFT: LISTENING");
                                    }

                                    recogOxford.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                            }

                            break;
                        case WIT:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "WIT");
                            }

                            switch (Recognition.getState()) {

                                case IDLE:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "WIT: IDLE");
                                    }

                                    conditions.setFetchingCountdown();
                                    recognitionListener.resetBugVariables();
                                    Schedulers.io().scheduleDirect(new Runnable() {
                                        @Override
                                        public void run() {
                                            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                                            recogWit = conditions.getWitRecognition(recognitionListener);
                                            recogWit.startListening();
                                        }
                                    });
                                    break;
                                case PROCESSING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "WIT: PROCESSING");
                                    }

                                    recogWit.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                                case LISTENING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "WIT: LISTENING");
                                    }

                                    recogWit.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                            }
                            break;
                        case ALEXA:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "AMAZON");
                            }
                            switch (Recognition.getState()) {
                                case IDLE:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "AMAZON: IDLE");
                                    }
                                    conditions.setFetchingCountdown();
                                    recognitionListener.resetBugVariables();
                                    Schedulers.io().scheduleDirect(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!SelfAware.this.waitAlexa()) {
                                                if (DEBUG) {
                                                    MyLog.w(CLS_NAME, "AMAZON: IDLE: failed warm up loop");
                                                }
                                                recogAmazon = conditions.getAmazonRecognition(recognitionListener);
                                            }
                                            recogAmazon.startListening();
                                        }
                                    });
                                    break;
                                case PROCESSING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "AMAZON: PROCESSING");
                                    }
                                    recogAmazon.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                                case LISTENING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "AMAZON: LISTENING");
                                    }
                                    recogAmazon.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                            }
                            break;
                        case WEAR:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "WEAR");
                            }
                            UtilsWearMessage.sendMessage(getApplicationContext(), conditions.getUtterance(), conditions.getWearContent(), conditions.getVRLocale(false), ai.saiy.android.wear.containers.WearMessageEvent.EVENT_SPEECH, conditions.getBundle());
                            break;
                        case WIT_HYBRID:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "WIT_HYBRID");
                            }
                            switch (Recognition.getState()) {
                                case IDLE:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "WIT_HYBRID: IDLE");
                                    }
                                    conditions.setFetchingCountdown();
                                    recognitionListener.resetBugVariables();
                                    recogWitHybrid.startListening();
                                    break;
                                case PROCESSING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "WIT_HYBRID: PROCESSING");
                                    }
                                    recogWitHybrid.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                                case LISTENING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "WIT_HYBRID: LISTENING");
                                    }
                                    recogWitHybrid.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                            }
                            break;
                        case IBM:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "IBM");
                            }

                            switch (Recognition.getState()) {

                                case IDLE:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "IBM: IDLE");
                                    }

                                    conditions.setFetchingCountdown();
                                    Schedulers.io().scheduleDirect(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!waitIBM()) {
                                                if (DEBUG) {
                                                    MyLog.w(CLS_NAME, "IBM: IDLE: failed warm up loop");
                                                }

                                                recogMic = conditions.getMicRecognition(null,
                                                        AudioParameters.getDefaultMicrosoft(), false, 0, true, false);
                                                recogIBM = conditions.getIBMRecognition(recogMic, recognitionListener);
                                            }
                                            recogIBM.startListening();
                                        }
                                    });
                                    break;
                                case PROCESSING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "IBM: PROCESSING");
                                    }

                                    recogIBM.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                                case LISTENING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "WIT: LISTENING");
                                    }

                                    recogIBM.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                            }

                            break;
                        case NATIVE:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "NATIVE");
                            }

                            switch (Recognition.getState()) {

                                case IDLE:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "NATIVE: IDLE");
                                    }

                                    if (SpeechRecognizer.isRecognitionAvailable(getApplicationContext())) {

                                        recognitionListener.resetBugVariables();

                                        if (SPH.getRecogniserBusyFix(getApplicationContext())) {
                                            releaseRecognition();
                                        }

                                        if (recogNative == null) {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "NATIVE: creating");
                                            }

                                            final Pair<SpeechRecognizer, Intent> nativePair =
                                                    conditions.getNativeRecognition(recognitionListener);

                                            if (nativePair != null) {
                                                recogNative = nativePair.first;
                                                recogNative.startListening(nativePair.second);
                                            } else {
                                                if (DEBUG) {
                                                    MyLog.w(CLS_NAME, "NATIVE: Pair null: Unavailable");
                                                }
                                                conditions.issueNoVRProvider();
                                                conditions.manageCallback(CallbackType.CB_ERROR_SAIY, null);
                                            }
                                        } else {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "NATIVE: exists");
                                            }
                                            recogNative.startListening(conditions.getNativeIntent());
                                        }
                                    } else {
                                        if (DEBUG) {
                                            MyLog.w(CLS_NAME, "NATIVE: Unavailable");
                                        }
                                        conditions.issueNoVRProvider();
                                        conditions.manageCallback(CallbackType.CB_ERROR_SAIY, null);
                                    }

                                    break;
                                case PROCESSING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "NATIVE: PROCESSING");
                                    }

                                    if (recogNative != null) {
                                        recogNative.stopListening();
                                    }

                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                                case LISTENING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "NATIVE: LISTENING");
                                    }

                                    if (recogNative != null) {
                                        recogNative.stopListening();
                                    }

                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                            }

                            break;
                        case REMOTE:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "REMOTE");
                            }

                            switch (Recognition.getState()) {

                                case IDLE:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "REMOTE: IDLE");
                                    }

                                    recognitionListener.resetBugVariables();
                                    recogRemote = conditions.getRemoteRecognition(recognitionListener);
                                    recogRemote.startListening();
                                    break;
                                case PROCESSING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "REMOTE: PROCESSING");
                                    }

                                    recogRemote.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                                case LISTENING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "REMOTE: LISTENING");
                                    }
                                    recogRemote.stopListening();
                                    conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                                    break;
                            }
                            break;
                        case MIC:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "MIC");
                            }

                            switch (Recognition.getState()) {

                                case IDLE:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "MIC: IDLE");
                                    }

                                    switch (conditions.getBundle().getInt(LocalRequest.EXTRA_CONDITION,
                                            Condition.CONDITION_NONE)) {

                                        case Condition.CONDITION_EMOTION:
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "MIC: CONDITION_EMOTION");
                                            }

                                            conditions.setFetchingCountdown();
                                            recogMic = conditions.getMicRecognition(recognitionListener,
                                                    AudioParameters.getDefaultBeyondVerbal(), true,
                                                    BeyondVerbal.MINIMUM_AUDIO_TIME, false, false);

                                            Schedulers.io().scheduleDirect(new Runnable() {
                                                @Override
                                                public void run() {
                                                    new BeyondVerbal(getApplicationContext(),
                                                            recogMic, conditions.getSupportedLanguage(false)).stream();
                                                }
                                            });
                                            break;
                                        case Condition.CONDITION_IDENTITY:
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "MIC: CONDITION_IDENTITY");
                                            }

                                            conditions.setFetchingCountdown();
                                            recogMic = conditions.getMicRecognition(recognitionListener,
                                                    AudioParameters.getDefaultMicrosoft(), true,
                                                    SpeakerEnrollment.MINIMUM_AUDIO_TIME, false, true);

                                            Schedulers.io().scheduleDirect(new Runnable() {
                                                @Override
                                                public void run() {

                                                    new SpeakerEnrollment(recogMic,
                                                            conditions.getSupportedLanguage(false),
                                                            MicrosoftConfiguration.OCP_APIM_KEY_1,
                                                            conditions.getIdentityProfile(), true).record();

                                                }
                                            });

                                            break;
                                        case Condition.CONDITION_IDENTIFY:
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "MIC: CONDITION_IDENTIFY");
                                            }

                                            conditions.setFetchingCountdown();
                                            recogMic = conditions.getMicRecognition(recognitionListener,
                                                    AudioParameters.getDefaultMicrosoft(), true,
                                                    SpeakerIdentification.MINIMUM_AUDIO_TIME, false, true);

                                            Schedulers.io().scheduleDirect(new Runnable() {
                                                @Override
                                                public void run() {

                                                    new SpeakerIdentification(recogMic,
                                                            conditions.getSupportedLanguage(false),
                                                            MicrosoftConfiguration.OCP_APIM_KEY_1,
                                                            conditions.getIdentityProfile(), true).record();
                                                }
                                            });
                                            break;
                                    }

                                    break;
                                case PROCESSING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "MIC: PROCESSING");
                                    }
                                    recogMic.stopRecording();
                                    break;
                                case LISTENING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "MIC: LISTENING");
                                    }
                                    recogMic.stopRecording();
                                    break;
                            }

                            break;
                        default:
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "UNKNOWN");
                            }
                            conditions.manageCallback(CallbackType.CB_ERROR_SAIY, null);
                            break;
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "No network connection");
                    }

                    if (conditions.servingRemote()) {
                        conditions.manageCallback(CallbackType.CB_ERROR_NETWORK, null);
                    } else {
                        conditions.showToast(getString(ai.saiy.android.R.string.error_network), Toast.LENGTH_SHORT);
                    }
                }

            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "Requesting permission");
                }
                conditions.manageCallback(CallbackType.CB_ERROR_SAIY, null);
            }

            conditions.removeRunnableCallback(this);
        }

    };

    /**
     * Due to misbehaving voice engines, it's often necessary to restart them even if {@link #onInitListener}
     * has returned successfully. This method handles both eventualities and sends errors on to a
     * restarting loop.
     */
    private void doSpeech(final int speechResult, final int queueType, @NonNull final String utterance) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "doSpeech");
        }

        switch (speechResult) {

            case SaiyTextToSpeech.SUCCESS:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "doSpeech: SUCCESS");
                }
                resetPendingConditions();
                break;
            case SaiyTextToSpeech.ERROR:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "doSpeech: ERROR");
                }
                speechRequestError(queueType, utterance);
                break;
        }
    }


    /**
     * A voice engine may have failed to initialise correctly. Use the {@link PendingTTS} class
     * to store the temporary data we need on restart.
     */
    private void speechRequestError(final int queueType, @NonNull final String utterance) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "speechRequestError");
        }

        pendingTTS = new PendingTTS();
        pendingTTS.setPendingUtterance(utterance);
        pendingTTS.setPendingQueueType(queueType);

        initTTS();
    }

    /**
     * The TTS engine has failed to initialise correctly or has falsely reported that it has. Here we
     * give it a chance to recover, up to a certain amount of times, before toasting an error to the user.
     */
    private void initTTS() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "initTTS: pending: " + initPending.get() + " ~ count: " + (initCount.get() + 1));
        }

        initCount.incrementAndGet();

        if (initPending.get()) {
            return;
        }

        if (initCount.intValue() <= MAX_INIT_ATTEMPTS) {
            initPending.set(true);
            initSaiyTTS();
        } else {
            engineError();
        }
    }

    /**
     * In order to combat lag, initialise the Engine on a background thread. In isolation, the
     * performance of doing this should make no difference, however:
     * <p/>
     * <a href="http://stackoverflow.com/q/36013611/1256219">Initialising the TextToSpeech object on a worker thread/a>
     */
    private void initSaiyTTS() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "initSaiyTTS");
        }

        Schedulers.computation().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                final ai.saiy.android.service.SelfAware selfAware = ai.saiy.android.service.SelfAware.this;
                selfAware.tts = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)?
                        new ai.saiy.android.tts.TextToSpeech21(selfAware, selfAware.onInitListener)
                        : new ai.saiy.android.tts.TextToSpeechLegacy(selfAware, selfAware.onInitListener);
            }
        });
    }

    /**
     * The TTS engine has failed to initialise.
     */
    private void engineError() {
        if (DEBUG) {
            MyLog.w(CLS_NAME, "engineError");
        }

        conditions.showToast(getApplicationContext().getString(ai.saiy.android.R.string.error_tts_initialisation),
                Toast.LENGTH_LONG);
        conditions.removeRunnableCallback(engineMonitor);
        releaseVoiceEngine();
    }

    /**
     * The TTS engine recovered from a failed initialisation and we
     * can reset the {@link PendingTTS} parameters
     */
    private void resetPendingConditions() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "resetPendingConditions");
        }

        pendingTTS = null;
        initPending.set(false);
        initCount.set(0);
    }

    /**
     * Make sure Android knows we are important.
     */
    private void startForeground(final int notificationConstant) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startForeground");
        }

        final Notification not = NotificationHelper.getForegroundNotification(SelfAware.this, notificationConstant);

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || isApplicationForeground()) {
                ServiceCompat.startForeground(this, NotificationService.NOTIFICATION_FOREGROUND, not,
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ? ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST : 0);
            } else {
                MyLog.w(CLS_NAME, "Failed to start (foreground launch restriction)");
            }
        } catch (IllegalStateException e) {
            // The process is running in the background, and is not allowed to start a foreground
            // service due to foreground service launch restrictions
            // (https://developer.android.com/about/versions/12/foreground-services).
            MyLog.e(CLS_NAME, "Not start (foreground launch restriction)");
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "beginForeground failure");
                e.printStackTrace();
            }
        }
    }

    private boolean isApplicationForeground() {
        final Application application = getApplication();
        if (application instanceof ProcessStateOwner) {
            return ((ProcessStateOwner) application).isForeground();
        }
        return false;
    }

    /**
     * Our {@link OnInitListener} to monitor the tts engine. Can't abstract without
     * unnecessarily messing with handlers.
     */
    private final OnInitListener onInitListener = new OnInitListener() {

        @SuppressWarnings("deprecation")
        @Override
        public void onInit(final int status) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onInit");
            }

            switch (status) {

                case SaiyTextToSpeech.SUCCESS:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onInit: SUCCESS");
                    }

                    if (tts == null) {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "onInit: SUCCESS: tts null. Threading error");
                        }
                        return;
                    }

                    tts.initialised();
                    tts.setOnUtteranceProgressListener(progressListener);
                    conditions.setVoice(tts, params);

                    if (pendingTTS != null) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onInit: pending speech true");
                        }

                        if (!conditions.isSilentUtterance()) {
                            doSpeech(tts.speak(pendingTTS.getPendingUtterance(),
                                    pendingTTS.getPendingQueueType(), params, params.getUtteranceId()),
                                    pendingTTS.getPendingQueueType(), pendingTTS.getPendingUtterance());
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onInit: pending speech true: silence");
                            }
                            conditions.removeRunnableCallback(engineMonitor);
                            progressListener.onDone(params.getUtteranceId());
                        }

                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onInit: pending speech false");
                        }

                        resetPendingConditions();
                    }

                    break;
                case SaiyTextToSpeech.ERROR:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onInit: ERROR");
                    }
                    initTTS();
                    break;
            }
        }
    };

    /**
     * Our {@link SaiyHotwordListener} to monitor the hotword detection.
     */
    private final SaiyHotwordListener hotwordListener = new SaiyHotwordListener() {

        @Override
        public void onHotwordStarted() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hotwordListener: onHotwordStarted");
            }
            conditions.acquireWakeLock();
            conditions.getSaiySoundPool().play(conditions.getSaiySoundPool().getBeepStart());
            if (DrivingProfileHelper.isEnabled(getApplicationContext())) {
                startForeground(NotificationHelper.NOTIFICATION_DRIVING_PROFILE);
            } else {
                startForeground(NotificationHelper.NOTIFICATION_HOTWORD);
            }
        }

        @Override
        public void onHotwordDetected(@NonNull final String hotword) {
            conditions.vibrate(SelfAwareConditions.VIBRATE_MIN);

            switch (hotword) {

                case SaiyHotwordListener.OKAY_GOOGLE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "hotwordListener: onHotwordDetected: OKAY_GOOGLE");
                    }

                    stopListening(false);

                    if (SPH.getOkayGoogleFix(getApplicationContext())) {
                        releaseRecognition();
                    }

                    conditions.acquireDisplayWakeLock();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ExecuteIntent.googleNowListen(getApplicationContext(), conditions.isSecure());
                            new GoogleNowMonitor().start(getApplicationContext());
                        }
                    }, OKAY_GOOGLE_DELAY);

                    break;
                case SaiyHotwordListener.WAKEUP_SAIY:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "hotwordListener: onHotwordDetected: WAKEUP_SAIY");
                    }

                    stopListening(false);
                    conditions.acquireDisplayWakeLock();
                    final LocalRequest lr = new LocalRequest(getApplicationContext());
                    lr.prepareIntro();
                    lr.setSecure(conditions.isSecure());
                    lr.execute();

                    break;
                case SaiyHotwordListener.STOP_LISTENING:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "hotwordListener: onHotwordDetected: STOP_LISTENING");
                    }
                    stopListening(true);
                    conditions.getSaiySoundPool().play(conditions.getSaiySoundPool().getBeepStop());
                    break;
            }
        }

        @Override
        public void onHotwordError(final int errorCode) {

            switch (errorCode) {

                case SaiyHotwordListener.ERROR_NULL:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "hotwordListener: onHotwordError: ERROR_NULL");
                    }
                    break;
                case SaiyHotwordListener.ERROR_INITIALISE:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "hotwordListener: onHotwordError: ERROR_INITIALISE");
                    }
                    break;
                case SaiyHotwordListener.ERROR_PERMISSIONS:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "hotwordListener: onHotwordError: ERROR_PERMISSIONS");
                    }
                    break;
            }

            conditions.showToast(getApplicationContext().getString(R.string.error_hotword),
                    Toast.LENGTH_LONG);
        }

        @Override
        public void onHotwordShutdown() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hotwordListener: onHotwordShutdown");
            }

            conditions.releaseWakeLock();
            startForeground(NotificationHelper.NOTIFICATION_SELF_AWARE);
        }
    };

    /**
     * Our {@link SaiyRecognitionListener} to monitor the voice recognition.
     */
    private final SaiyRecognitionListener recognitionListener = new SaiyRecognitionListener() {

        @Override
        public void onReadyForSpeech(final Bundle params) {
            super.onReadyForSpeech(params);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "recognitionListener: onReadyForSpeech");
            }

            conditions.onVRStarted();
        }

        @Override
        public void onBeginningOfRecognition() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "recognitionListener: onBeginningOfRecognition");
            }
            SPH.resetRecognizerBusyIncrement(getApplicationContext());
        }

        @Override
        public void onRmsChanged(final float rmsdB) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "recognitionListener: onRmsChanged");
            }
        }

        @Override
        public void onEvent(final int eventType, final Bundle params) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "recognitionListener: onEvent: " + eventType);
                MyLog.i(CLS_NAME, "recognitionListener: onEvent bundle: " + (params != null && !params.isEmpty()));
            }
        }

        @Override
        public void onBufferReceived(final byte[] buffer) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "recognitionListener: onBufferReceived");
            }
        }

        @Override
        public void onEndOfRecognition() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "recognitionListener: onEndOfRecognition");
            }

            conditions.onVREnded();
        }

        @Override
        public void onRecognitionError(final int error) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "recognitionListener: onRecognitionError: " + error);
            }

            conditions.onVRError();

            switch (conditions.getDefaultRecognition()) {

                case GOOGLE_CLOUD:
                    recogGoogleCloud = null;
                    recogMic = null;
                    break;
                case IBM:
                    recogIBM = null;
                    recogMic = null;
                    break;
                case MICROSOFT:
                    recogOxford = null;
                    break;
            }

            if (conditions.isUserInterrupted()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "recognitionListener: onError: isUserInterrupted: true");
                }
                conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "recognitionListener: onError: isUserInterrupted: false");
                }

                switch (error) {
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "recognitionListener: onError: ERROR_NETWORK_TIMEOUT");
                        }
                        conditions.manageCallback(CallbackType.CB_ERROR_NETWORK, null);
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "recognitionListener: onError: ERROR_NETWORK");
                        }
                        conditions.manageCallback(CallbackType.CB_ERROR_NETWORK, null);
                        break;
                    case SpeechRecognizer.ERROR_AUDIO:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "recognitionListener: onError: ERROR_AUDIO");
                        }
                        conditions.manageCallback(CallbackType.CB_ERROR_SAIY, null);
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "recognitionListener: onError: ERROR_SERVER");
                        }
                        if (!SPH.isOfflineInstallationShown(getApplicationContext())) {
                            ExecuteIntent.showInstallOfflineVoiceFiles(getApplicationContext());
                            conditions.showToast(getApplicationContext().getString(R.string.error_offline), Toast.LENGTH_LONG);
                        }
                        conditions.manageCallback(CallbackType.CB_ERROR_NETWORK, null);
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "recognitionListener: onError: ERROR_CLIENT");
                        }
                        conditions.manageCallback(CallbackType.CB_ERROR_SAIY, null);
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "recognitionListener: onError: ERROR_SPEECH_TIMEOUT");
                        }
                        conditions.manageCallback(CallbackType.CB_ERROR_NO_MATCH, null);
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "recognitionListener: onError: ERROR_NO_MATCH");
                        }
                        conditions.manageCallback(CallbackType.CB_ERROR_NO_MATCH, null);
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "recognitionListener: onError: ERROR_RECOGNIZER_BUSY");
                        }
                        conditions.manageCallback(CallbackType.CB_ERROR_BUSY, null);
                        if (SaiyDefaults.VR.NATIVE == conditions.getDefaultRecognition()) {
                            SPH.recognizerBusyAutoIncrease(getApplicationContext());
                            long count = SPH.getRecognizerBusyIncrement(getApplicationContext());
                            if (count < 3) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "recognitionListener: ERROR_RECOGNIZER_BUSY count: " + count);
                                }
                            } else {
                                ExecuteIntent.openApplicationSpecificSettings(getApplicationContext(), Installed.PACKAGE_NAME_GOOGLE_NOW);
                                final boolean servingRemote = conditions.servingRemote();
                                LocalRequest localRequest = new LocalRequest(getApplicationContext());
                                localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, conditions.getSupportedLanguage(servingRemote), conditions.getVRLocale(servingRemote), conditions.getTTSLocale(servingRemote), getString(R.string.error_recogniser_busy));
                                localRequest.setSpeechPriority(SpeechPriority.PRIORITY_MAX);
                                localRequest.execute();
                                SPH.setRecogniserBusyFix(getApplicationContext(), true);
                            }
                        }
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "recognitionListener: onError: ERROR_INSUFFICIENT_PERMISSIONS");
                        }
                        conditions.manageCallback(CallbackType.CB_ERROR_SAIY, null);
                        break;
                    default:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "recognitionListener: onError: " + SaiyRecognitionListener.getDescription(error));
                        }
                        conditions.manageCallback(CallbackType.CB_ERROR_SAIY, null);
                        break;
                }

                conditions.handleRecognitionError(error, conditions.getBundle(), conditions.getDefaultRecognition());
            }

            releaseRecognition();

            System.gc();

            if (conditions.restartHotword()) {
                startHotwordDetection(conditions.getBundle());
            }
        }

        @Override
        public void onPartialResults(final Bundle partialResults) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "recognitionListener: onPartialResults");
            }

            if (Recognition.getState() == Recognition.State.LISTENING) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onPartialResults: state == LISTENING");
                    SelfAwareVerbose.logSpeechResults(partialResults);
                }

                final boolean servingRemote = conditions.servingRemote();

                if (!conditions.isCancelled()) {
                    if (partialHelper == null || partialHelper.isShutdown()) {
                        partialHelper = new PartialHelper(getApplicationContext(),
                                conditions.getSupportedLanguage(servingRemote), this);
                    }

                    partialHelper.isPartial(partialResults);
                }

            }
        }

        @Override
        public void onCancelDetected() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onCancelDetected");
            }

            conditions.setCancelled();
            new Handler(Looper.getMainLooper()).post(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (Recognition.getState() == Recognition.State.LISTENING) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onCancelDetected: stopping recognition: true");
                                }
                                stopListening(false);
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onCancelDetected: stopping recognition: false");
                                }
                            }
                        }
                    }
            );
        }

        @Override
        public void onTranslateDetected() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onTranslateDetected");
            }
            BingCredentials.refreshTokenIfRequired(getApplicationContext());
        }

        @Override
        public void onAlexaDetected() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAlexaDetected");
            }
            TokenHelper.refreshTokenIfRequired(getApplicationContext());
        }

        @Override
        public void onResults(final Bundle results) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "recognitionListener: onResults");
                SelfAwareVerbose.logSpeechResults(results);
            }

            conditions.onVRComplete();

            switch (conditions.getDefaultRecognition()) {

                case GOOGLE_CLOUD:
                    recogGoogleCloud = null;
                    recogMic = null;
                    break;
                case IBM:
                    recogIBM = null;
                    recogMic = null;
                    break;
                case ALEXA:
                    recogAmazon = null;
                    if (results.containsKey(SaiyRecognitionListener.ALEXA_DIRECTIVE)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onResults: containsKey: ALEXA_DIRECTIVE");
                        }
                        LocalRequest localRequest = new LocalRequest(getApplicationContext());
                        DirectiveType directiveType = results.getParcelable(SaiyRecognitionListener.ALEXA_DIRECTIVE);
                        if (directiveType != null) {
                            switch (directiveType) {
                                case DIRECTIVE_MEDIA:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "onResults: DIRECTIVE_MEDIA");
                                    }
                                    localRequest.prepareDefault(results.getInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_ONLY), conditions.getSupportedLanguage(false), conditions.getVRLocale(false), conditions.getTTSLocale(false), ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(getApplicationContext(), conditions.getSupportedLanguage(false), R.string.alexa_media_response));
                                    localRequest.execute();
                                    return;
                                case DIRECTIVE_VOLUME:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "onResults: DIRECTIVE_VOLUME");
                                    }
                                    localRequest.prepareDefault(results.getInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_ONLY), conditions.getSupportedLanguage(false), conditions.getVRLocale(false), conditions.getTTSLocale(false), ai.saiy.android.personality.PersonalityResponse.getAlexaVolumeResponse(getApplicationContext(), conditions.getSupportedLanguage(false)));
                                    localRequest.execute();
                                    return;
                                case DIRECTIVE_CANCEL:
                                    localRequest.prepareCancelled(conditions.getSupportedLanguage(false), conditions.getVRLocale(false), conditions.getTTSLocale(false));
                                    localRequest.execute();
                                    return;
                                default:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "onResults: DIRECTIVE_NONE");
                                        break;
                                    }
                                    break;
                            }
                        } else if (DEBUG) {
                            MyLog.w(CLS_NAME, "onResults: DirectiveType null");
                        }
                    }
                    String string = results.getString(SaiyRecognitionListener.ALEX_FILE);
                    if (!UtilsString.notNaked(string)) {
                        LocalRequest localRequest = new LocalRequest(SelfAware.this.getApplicationContext());
                        localRequest.prepareDefault(results.getInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_ONLY), conditions.getSupportedLanguage(false), conditions.getVRLocale(false), conditions.getTTSLocale(false), ai.saiy.android.personality.PersonalityResponse.getAlexaUnreachable(SelfAware.this.getApplicationContext(), conditions.getSupportedLanguage(false)));
                        localRequest.execute();
                        return;
                    }
                    final int addEarCon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? tts.addEarcon(SaiyTextToSpeech.ALEXA_EAR_CON, new File(string)) : tts.addEarcon(SaiyTextToSpeech.ALEXA_EAR_CON, string);
                    LocalRequest localRequest = new LocalRequest(SelfAware.this.getApplicationContext());
                    switch (addEarCon) {
                        case android.speech.tts.TextToSpeech.ERROR:
                            localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, conditions.getSupportedLanguage(false), conditions.getVRLocale(false), conditions.getTTSLocale(false), ai.saiy.android.personality.PersonalityResponse.getAlexaUnreachable(SelfAware.this.getApplicationContext(), conditions.getSupportedLanguage(false)));
                            localRequest.execute();
                            return;
                        case android.speech.tts.TextToSpeech.SUCCESS:
                            int action = results.getInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_ONLY);
                            if (action == LocalRequest.ACTION_SPEAK_LISTEN) {
                                localRequest.setRecognitionProvider(SaiyDefaults.VR.ALEXA);
                            }
                            localRequest.prepareDefault(action, conditions.getSupportedLanguage(false), conditions.getVRLocale(false), conditions.getTTSLocale(false), SaiyRecognitionListener.ALEX_SPEECH);
                            localRequest.setAlexaFilePath(string);
                            localRequest.execute();
                            return;
                        default:
                            return;
                    }
                case WIT_HYBRID:
                    recogWitHybrid = null;
                    break;
                case MICROSOFT:
                    recogOxford = null;
                    break;
            }

            final boolean servingRemote = conditions.servingRemote();
            conditions.putResults(results);

            switch (conditions.getDefaultLanguageModel(servingRemote)) {

                case API_AI:

                    Schedulers.io().scheduleDirect(new Runnable() {

                        @SuppressWarnings("ConstantConditions")
                        @Override
                        public void run() {

                            final Pair<Boolean, DetectIntentResponse> remoteAPIPair = conditions.getAPIAIRemote(results);

                            if (remoteAPIPair.first) {

                                if (servingRemote) {
                                    final com.google.cloud.dialogflow.v2beta1.QueryResult queryResult = remoteAPIPair.second.getQueryResult();
                                    final String gsonString = new GsonBuilder().disableHtmlEscaping().create().toJson(queryResult);
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "gsonString: " + ApiRequest.handleResults(queryResult));
                                    }

                                    results.putString(Request.RESULTS_NLU, gsonString);
                                    conditions.manageCallback(CallbackType.CB_RESULTS_RECOGNITION, results);
                                } else {
                                    new ResolveAPIAI(getApplicationContext(),
                                            conditions.getSupportedLanguage(servingRemote),
                                            conditions.getVRLocale(servingRemote),
                                            conditions.getTTSLocale(servingRemote),
                                            results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES),
                                            results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION))
                                            .unpack(remoteAPIPair.second);
                                    //TODO handle result
                                }
                            } else {
                                if (servingRemote) {
                                    conditions.manageCallback(CallbackType.CB_ERROR_NETWORK, null);
                                } else {

                                    if (!conditions.isCancelled()) {
                                        new RecognitionAction(getApplicationContext(), conditions.getVRLocale(false),
                                                conditions.getTTSLocale(false), conditions.getSupportedLanguage(false),
                                                conditions.getBundle());
                                    } else {
                                        final LocalRequest lr = new LocalRequest(getApplicationContext());
                                        lr.prepareCancelled(conditions.getSupportedLanguage(false), conditions.getVRLocale(false),
                                                conditions.getTTSLocale(false));
                                        lr.execute();
                                    }
                                }
                            }
                        }
                    });

                    break;
                default:

                    if (servingRemote) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onResults: serving remote");
                        }
                        conditions.manageCallback(CallbackType.CB_RESULTS_RECOGNITION, results);
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onResults: handle internal");
                        }

                        if (!conditions.isCancelled()) {
                            new RecognitionAction(getApplicationContext(), conditions.getVRLocale(false),
                                    conditions.getTTSLocale(false), conditions.getSupportedLanguage(false),
                                    conditions.getBundle());
                        } else {
                            final LocalRequest lr = new LocalRequest(getApplicationContext());
                            lr.prepareCancelled(conditions.getSupportedLanguage(false), conditions.getVRLocale(false),
                                    conditions.getTTSLocale(false));
                            lr.execute();
                        }
                    }

                    break;
            }
        }

        @Override
        public void onComplete() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "recognitionListener: onComplete");
            }
            conditions.onVRComplete();
        }
    };

    /**
     * Our {@link SaiyProgressListener} to monitor the speech. Can't abstract without
     * unnecessarily messing with handlers and remote callbacks.
     */
    private final SaiyProgressListener progressListener = new SaiyProgressListener() {

        private long then;

        @Override
        public void onError(final String utteranceId, final int errorCode) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "progressListener: onError: errorCode" + errorCode);
            }
            super.onError(utteranceId, errorCode);
        }

        @Override
        public void onStop(final String utteranceId, final boolean interrupted) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "progressListener: onStop: interrupted " + interrupted);
            }

            if (interrupted) {
                onDone(utteranceId);
            }

            super.onStop(utteranceId, interrupted);
        }

        @Override
        public void onStart(final String utteranceId) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "progressListener: onStart: " + utteranceId);
            }

            then = System.nanoTime();

            if (params.validateOnStart(utteranceId)) {
                conditions.onTTSStarted();
                conditions.removeRunnableCallback(engineMonitor);
            } else {
                restartStatusMonitor();
            }
        }

        @Override
        public void onError(final String utteranceId) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "progressListener: onError");
            }

            if (conditions.isUserInterrupted()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "progressListener: onError: isUserInterrupted");
                }
                onDone(utteranceId);
            } else {

                conditions.onTTSError();
                conditions.handleTTSError(conditions.getDefaultTTS(), conditions.getBundle());

                switch (conditions.getDefaultTTS()) {

                    case LOCAL:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "progressListener: onError: SpeechDefault.LOCAL");
                        }
                        conditions.manageCallback(CallbackType.CB_ERROR_SAIY, null);
                        break;
                    case NETWORK_NUANCE:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "progressListener: onError: SpeechDefault.NETWORK_NUANCE");
                        }
                        conditions.manageCallback(CallbackType.CB_ERROR_NETWORK, null);
                        break;
                    default:
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "progressListener: onError: SpeechDefault.default");
                        }
                        conditions.manageCallback(CallbackType.CB_ERROR_SAIY, null);
                        break;
                }
            }

            releaseVoiceEngine();
        }

        @Override
        public void onDone(final String utteranceId) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "progressListener: onDone");
                MyLog.getElapsed("onDone", then);
            }

            if (conditions.getElapsed(then) < SPEECH_ELAPSED_WARNING
                    && !conditions.getUtterance().matches(SaiyRequestParams.SILENCE)) {
                conditions.handleTTSError(conditions.getDefaultTTS(), conditions.getBundle());
            }

            final Pair<Boolean, String> onDonePair = params.validateOnDone(utteranceId);

            if (onDonePair.first) {

                conditions.onTTSEnded(cache, tts, params);

                switch (Integer.parseInt(onDonePair.second)) {

                    case LocalRequest.ACTION_SPEAK_ONLY:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "progressListener: ACTION_SPEAK_ONLY");
                        }

                        conditions.getHandler().post(soRun);
                        break;
                    case LocalRequest.ACTION_SPEAK_LISTEN:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "progressListener: ACTION_SPEAK_LISTEN");
                        }

                        conditions.getHandler().post(slRun);
                        break;
                }

                conditions.manageCallback(CallbackType.CB_UTTERANCE_COMPLETED, null);
            }
        }
    };

    /**
     * Our {@link PhoneStateListener} to monitor the device radio and handle situations where the
     * speech or recognition would need to be stopped.
     */
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(final int state, final String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "PhoneStateListener: TelephonyManager.CALL_STATE_OFFHOOK");
                    }
                    interrupt();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "PhoneStateListener: TelephonyManager.CALL_STATE_RINGING: " + incomingNumber);
                    }
                    interrupt();
                    if (!SPH.announceCallerStats(getApplicationContext()) || !ai.saiy.android.quiet.QuietTimeHelper.canProceed(getApplicationContext())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PhoneStateListener: not announcing calls or within quiet times");
                        }
                    } else if (!UtilsString.notNaked(incomingNumber) || !ai.saiy.android.permissions.PermissionHelper.checkAnnounceCallerPermissionsNR(SelfAware.this.getApplicationContext())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PhoneStateListener: incoming number null or permission denied");
                        }
                    } else {
                        if (disposable == null) {
                            disposable = Schedulers.computation().scheduleDirect(new Runnable() {
                                @Override
                                public void run() {
                                    if (getTelephonyManager().getCallState() != TelephonyManager.CALL_STATE_RINGING) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "PhoneStateListener: no longer ringing");
                                        }
                                        return;
                                    }
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "PhoneStateListener: still ringing");
                                    }
                                    String userName = new ContactHelper().getNameFromNumber(getApplicationContext(), incomingNumber);
                                    if (!UtilsString.notNaked(userName)) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "PhoneStateListener: unable to resolve caller name");
                                        }
                                        userName = SelfAware.this.getString(R.string.an_unknown_caller);
                                    } else if (DEBUG) {
                                        MyLog.i(CLS_NAME, "PhoneStateListener: have caller name: " + userName);
                                    }
                                    VolumeHelper.muteRinger(getApplicationContext(), true);
                                    Schedulers.computation().scheduleDirect(new Runnable() {
                                        @Override
                                        public void run() {
                                            VolumeHelper.muteRinger(SelfAware.this.getApplicationContext(), false);
                                        }
                                    }, 5000L, TimeUnit.MILLISECONDS);
                                    LocalRequest localRequest = new LocalRequest(SelfAware.this.getApplicationContext());
                                    localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_LISTEN, conditions.getSupportedLanguage(false), conditions.getVRLocale(false), conditions.getTTSLocale(false), getString(R.string.incoming_call_from) + XMLResultsHandler.SEP_SPACE + userName + ", " + getString(R.string.would_you_like_to_answer_it));
                                    localRequest.setCondition(Condition.CONDITION_ANNOUNCE_CALLER);
                                    localRequest.setSpeechPriority(SpeechPriority.PRIORITY_MAX);
                                    localRequest.execute();
                                    Schedulers.computation().scheduleDirect(new Runnable() {
                                        @Override
                                        public void run() {
                                            disposable = null;
                                        }
                                    }, 14000L, TimeUnit.MILLISECONDS);
                                }
                            }, 5000L, TimeUnit.MILLISECONDS);
                        }
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "PhoneStateListener: TelephonyManager.CALL_STATE_IDLE");
                    }

                    if (SPH.getResetSpeaker(getApplicationContext())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "PhoneStateListener: TelephonyManager.CALL_STATE_IDLE: resetting");
                        }
                        SPH.setResetSpeaker(getApplicationContext(), false);
                        Schedulers.computation().scheduleDirect(new Runnable() {
                            @Override
                            public void run() {
                                SelfAware.this.disposable = null;
                                final android.media.AudioManager audioManager = (android.media.AudioManager) SelfAware.this.getSystemService(Context.AUDIO_SERVICE);
                                audioManager.setMode(android.media.AudioManager.MODE_NORMAL);
                                audioManager.setSpeakerphoneOn(false);
                                if (audioManager.isSpeakerphoneOn() || audioManager.getMode() != android.media.AudioManager.MODE_NORMAL) {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "PhoneStateListener: TelephonyManager.CALL_STATE_IDLE: forcing");
                                    }
                                    ai.saiy.android.sound.VolumeHelper.forceBehaviour();
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "PhoneStateListener: post forced getMode: " + audioManager.getMode());
                                        MyLog.w(CLS_NAME, "PhoneStateListener: post forced isSpeakerphoneOn: " + audioManager.isSpeakerphoneOn());
                                    }
                                }
                                if (conditions.restartHotword()) {
                                    startHotwordDetection(conditions.getBundle());
                                }
                            }
                        }, 3500L, TimeUnit.MILLISECONDS);
                    } else {
                        if (conditions.restartHotword()) {
                            startHotwordDetection(conditions.getBundle());
                        }
                    }

                    conditions.removeInterrupted(params);
                    break;
            }
        }
    };

    @WorkerThread
    private boolean waitGoogleCloud() {
        if (DEBUG) {
            MyLog.v(CLS_NAME, "waitGoogleCloud");
        }

        if (recogMic == null || recogGoogleCloud == null) {

            int sleepCount = 0;

            while (sleepCount < WARM_UP_LOOP) {
                try {
                    Thread.sleep(WARM_UP_SLEEP);
                } catch (final InterruptedException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "InterruptedException");
                        e.printStackTrace();
                    }
                }

                if (recogMic == null || recogGoogleCloud == null) {
                    if (DEBUG) {
                        MyLog.v(CLS_NAME, "recogMic/Google null: " + sleepCount);
                    }
                    sleepCount++;
                } else {
                    break;
                }
            }
        }

        return recogGoogleCloud != null && recogMic != null;

    }

    @WorkerThread
    private boolean waitIBM() {
        if (DEBUG) {
            MyLog.v(CLS_NAME, "waitIBM");
        }

        if (recogMic == null || recogIBM == null) {

            int sleepCount = 0;

            while (sleepCount < WARM_UP_LOOP) {
                try {
                    Thread.sleep(WARM_UP_SLEEP);
                } catch (final InterruptedException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "InterruptedException");
                        e.printStackTrace();
                    }
                }

                if (recogMic == null || recogIBM == null) {
                    if (DEBUG) {
                        MyLog.v(CLS_NAME, "recogIBM null: " + sleepCount);
                    }
                    sleepCount++;
                } else {
                    break;
                }
            }
        }

        return recogMic == null && recogIBM != null;
    }

    private boolean waitAlexa() {
        if (DEBUG) {
            MyLog.v(CLS_NAME, "waitAlexa");
        }
        if (recogAmazon == null) {
            for (int i = 0; i < 4; i++) {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "InterruptedException");
                        e.printStackTrace();
                    }
                }
                if (recogAmazon != null) {
                    break;
                }
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "recogAmazon null: " + i);
                }
            }
        }
        return recogAmazon != null;
    }

    @WorkerThread
    private boolean waitMicrosoft() {
        if (DEBUG) {
            MyLog.v(CLS_NAME, "waitMicrosoft");
        }

        if (recogOxford == null) {

            int sleepCount = 0;

            while (sleepCount < WARM_UP_LOOP) {
                try {
                    Thread.sleep(WARM_UP_SLEEP);
                } catch (final InterruptedException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "InterruptedException");
                        e.printStackTrace();
                    }
                }

                if (recogOxford == null) {
                    if (DEBUG) {
                        MyLog.v(CLS_NAME, "recogOxford null: " + sleepCount);
                    }
                    sleepCount++;
                } else {
                    break;
                }
            }
        }

        return recogOxford != null;
    }


    /**
     * The TTS playback and/or voice recognition needs to be cancelled.
     */
    private void interrupt() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "interrupt");
        }

        stopListening(false);
        stopSpeech(true);
        Global.setVoiceTutorialState(getApplicationContext(), false);
    }


    /**
     * We don't need the voice engine any more. The surrounding try catch blocks may seem like
     * noob overkill, but based on crash reports they safely handle misbehaving voice engines.
     */
    private void releaseVoiceEngine() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "releaseVoiceEngine");
        }

        VolumeHelper.abandonAudioMedia(getApplicationContext());
        NotificationHelper.cancelSpeakingNotification(getApplicationContext());
        NotificationHelper.cancelFetchingNotification(getApplicationContext());
        NotificationHelper.cancelInitialisingNotification(getApplicationContext());

        if (tts != null) {

            try {
                tts.stop();
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "releaseVoiceEngine: TTS stop Exception");
                    e.getStackTrace();
                }
            }

            try {
                tts.shutdown();
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "releaseVoiceEngine: TTS shutdown Exception");
                    e.getStackTrace();
                }
            }

            try {
                tts = null;
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "releaseVoiceEngine: TTS to null Exception!!??!!");
                    e.printStackTrace();
                }
            }

            resetPendingConditions();
        }

        // hint
        System.gc();
    }

    /**
     * Release the native voice recognition service
     */
    private void releaseRecognition() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "releaseRecognition");
            MyLog.i(CLS_NAME, "releaseRecognition: isMain thread: " + (Looper.myLooper() == Looper.getMainLooper()));
            MyLog.i(CLS_NAME, "releaseRecognition: threadTid: " + android.os.Process.getThreadPriority(android.os.Process.myTid()));
        }

        if (recogNative != null) {

            try {
                recogNative.destroy();
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "releaseRecognition: Exception");
                    e.printStackTrace();
                }
            } finally {
                recogNative = null;
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "releaseRecognition: null");
            }
        }
    }

    /**
     * Runnable to monitor the text to speech object so we can check for weird circumstances, such
     * as it reporting that it is attempting to speak, but doing nothing. After {@link #MONITOR_ENGINE}
     * amount of time, we'll shut it down and interact with the user.
     */
    private final Runnable engineMonitor = new Runnable() {
        @Override
        public void run() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "engineMonitor: notifying error");
            }

            conditions.showToast(getApplicationContext().getString(ai.saiy.android.R.string.error_tts_initialisation),
                    Toast.LENGTH_LONG);
            TTS.setState(TTS.State.IDLE);
            resetPendingConditions();
            releaseVoiceEngine();
            conditions.manageCallback(CallbackType.CB_ERROR_SAIY, null);

            // hint
            System.gc();
        }
    };

    /**
     * Restart the engine monitor for {@link SelfAwareConditions#getHandler()}
     */
    private void restartEngineMonitor() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "restartEngineMonitor");
        }
        conditions.removeRunnableCallback(engineMonitor);
        conditions.getHandler().postDelayed(engineMonitor, MONITOR_ENGINE);
    }

    /**
     * Runnable to reset all global status that could prevent the application from functioning.
     */
    private final Runnable statusMonitor = new Runnable() {
        @Override
        public void run() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "statusMonitor: resetting conditions");
            }

            TTS.setState(TTS.State.IDLE);
            Recognition.setState(Recognition.State.IDLE);
            resetPendingConditions();
            releaseVoiceEngine();
            releaseRecognition();
            conditions.removeRunnableCallback(null);
            releasePartialHelper();
            System.gc();
        }
    };


    /**
     * Restart the status monitor for {@link SelfAwareConditions#getHandler()}
     */
    private void restartStatusMonitor() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "restartStatusMonitor");
        }
        conditions.removeRunnableCallback(statusMonitor);
        conditions.getHandler().postDelayed(statusMonitor, SPH.getInactivityTimeout(getApplicationContext()));
    }

    /**
     * Shutdown the partial executor service
     */
    private void releasePartialHelper() {
        if (partialHelper != null && !partialHelper.isShutdown()) {
            partialHelper.shutdown();
        }
    }

    /**
     * Instantiate a {@link TelephonyManager} instance whilst setting the Class listener
     *
     * @return a {@link TelephonyManager} instance
     */
    private TelephonyManager getTelephonyManager() {

        if (telephonyManager == null) {
            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        return telephonyManager;
    }

    /**
     * Set the instance to help ascertain if this service is running
     */
    private void setInstance() {
        SelfAware.instance = this;
    }

    /**
     * Destroy the static instance. Called from {@link #tidyUp()}
     */
    private void destroyInstance() {
        SelfAware.instance = null;
    }

    /**
     * Housekeeping called from {@link #onDestroy()}
     */
    private void tidyUp() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "tidyUp");
        }

        VolumeHelper.abandonAudioMedia(getApplicationContext());
        stopListening(true);
        stopSpeech(true);
        releaseVoiceEngine();
        resetPendingConditions();
        conditions.killCallbacks();
        conditions.removeRunnableCallback(null);
        conditions.getSaiySoundPool().release();
        conditions.unregisterBluetoothController();
        TTS.setState(TTS.State.IDLE);
        Recognition.setState(Recognition.State.IDLE);
        releasePartialHelper();
        motionRecognition.destroy();
        conditions.releaseWakeLock();

        if (telephonyManager != null) {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        destroyInstance();
    }

    /**
     * This hint is generally seen on devices with hardware limitations and so is currently ignored.
     * <p>
     * Checking and releasing resources could potentially compound the situation, as such resources
     * will need to be restarted and the devices that call this regularly take a longer time to initialise,
     * reducing the user experience.
     * <p>
     * The native voice recognition and Text to Speech objects do take up a substantial amount of
     * memory, but they are already released after a period of {@link SPH#getInactivityTimeout(Context)}.
     * Changing the behaviour here for the sake of lower-end or poorly performing devices (for a myriad of
     * unidentifiable reasons) is not currently the way forward.
     *
     * @param level a hint to the amount of trimming the application may like to perform.
     */
    @Override
    public void onTrimMemory(final int level) {
        super.onTrimMemory(level);
        if (DEBUG) {
            SelfAwareVerbose.memoryVerbose(level);
        }
    }

    /**
     * See above.
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (DEBUG) {
            MyLog.w(CLS_NAME, "onLowMemory");
        }
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
        tidyUp();
        super.onDestroy();
        this.stopSelf();
    }
}
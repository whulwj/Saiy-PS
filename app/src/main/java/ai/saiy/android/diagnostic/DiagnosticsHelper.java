package ai.saiy.android.diagnostic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import ai.saiy.android.R;
import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.device.DeviceInfo;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DiagnosticsHelper {
    private static final int SAIY_VR_REQUEST_CODE = 1234;
    private static final String UTTERANCE_ID = "Saiy";
    private static final int SET_MODE = 1;
    private static final int APPEND_MODE = 2;

    private final Handler handler;
    private volatile TextToSpeech tts;
    private Disposable disposable;
    private final DiagnosticInfoListener diagnosticInfoListener;
    private final Context mContext;
    private ArrayList<VoiceEngine> containerVoiceEngine;
    private final PackageManager packageManager;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = DiagnosticsHelper.class.getSimpleName();

    private final AtomicBoolean isBroadcastReceived = new AtomicBoolean();
    private final AtomicBoolean isCancelled = new AtomicBoolean();
    private final AtomicBoolean isCompleted = new AtomicBoolean();
    private final AtomicBoolean isDoInBackground = new AtomicBoolean();
    private final HashMap<String, String> params = new HashMap<>();
    private final AtomicInteger diagnosticIndex = new AtomicInteger();
    private final Object lock = new Object();
    private final TextToSpeech.OnInitListener initListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "TextToSpeech thread onInit");
            }
            if (isCancelled.get()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onInitListener cancelled");
                }
                destroyTTS();
                return;
            }
            switch (status) {
                case TextToSpeech.ERROR:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "TextToSpeech.ERROR");
                    }
                    diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_init_failed));
                    diagnosticsInfo.setErrorCount(diagnosticsInfo.getErrorCount() + 1);
                    diagnosticInfoListener.setErrorCount(String.valueOf(diagnosticsInfo.getErrorCount()));
                    releaseTTS();
                    break;
                case TextToSpeech.SUCCESS:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "TextToSpeech.SUCCESS");
                    }
                    if (diagnosticIndex.get() == 0) {
                        diagnosticsInfo.setDefaultTTSPackage(tts.getDefaultEngine());
                    }
                    disposable = Schedulers.io().scheduleDirect(new Runnable() {
                        @Override
                        public void run() {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "TextToSpeech inner thread running");
                            }
                            restartEngineMonitor();
                            try {
                                diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_init_success));
                                diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_testing_speech));
                                diagnosticsInfo.setPassedCount(diagnosticsInfo.getPassedCount() + 1);
                                diagnosticInfoListener.setPassedCount(String.valueOf(diagnosticsInfo.getPassedCount()));
                                tts.setOnUtteranceProgressListener(progressListener);
                                tts.speak(containerVoiceEngine.get(diagnosticIndex.get()).getApplicationName(), TextToSpeech.QUEUE_FLUSH, params);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    Set<Locale> availableLanguages = tts.getAvailableLanguages();
                                    if (availableLanguages == null || availableLanguages.isEmpty()) {
                                        diagnosticsInfo.getVoiceEngineInfos().get(diagnosticIndex.get()).setLocaleArray(Collections.emptyList());
                                    } else {
                                        diagnosticsInfo.getVoiceEngineInfos().get(diagnosticIndex.get()).setLocaleArray(new ArrayList<>(availableLanguages));
                                    }
                                } else {
                                    ArrayList<Locale> arrayList = new ArrayList<>();
                                    Locale[] availableLocales = Locale.getAvailableLocales();
                                    for (int i = 0; i < availableLocales.length && !isCancelled.get(); ++i) {
                                        Locale locale = availableLocales[i];
                                        try {
                                            locale.getISO3Country();
                                            switch (tts.isLanguageAvailable(locale)) {
                                                case TextToSpeech.LANG_AVAILABLE:
                                                case TextToSpeech.LANG_COUNTRY_AVAILABLE:
                                                case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
                                                    arrayList.add(locale);
                                                    break;
                                            }
                                        } catch (IllegalArgumentException e) {
                                            if (DEBUG) {
                                                MyLog.w(CLS_NAME, "isLanguageAvailable: IllegalArgumentException");
                                            }
                                        } catch (MissingResourceException e) {
                                            if (DEBUG) {
                                                MyLog.w(CLS_NAME, "isLanguageAvailable: MissingResourceException");
                                            }
                                        }
                                    }
                                    diagnosticsInfo.getVoiceEngineInfos().get(diagnosticIndex.get()).setLocaleArray(arrayList);
                                }
                                List<Locale> localeArray = diagnosticsInfo.getVoiceEngineInfos().get(diagnosticIndex.get()).getLocaleArray();
                                for (int i = 0; i < localeArray.size() && !isCancelled.get(); i++) {
                                    diagnosticInfoListener.appendDiagnosticInfo(localeArray.get(i).toString());
                                    try { // Temporarily add the delay back
                                        Thread.sleep(7L);
                                    } catch (InterruptedException e) {
                                        if (DEBUG) {
                                            MyLog.e(CLS_NAME, "ttsLoc InterruptedException");
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } catch (NullPointerException e) {
                                if (DEBUG) {
                                    MyLog.e(CLS_NAME, "ttsLoc NullPointerException");
                                    e.printStackTrace();
                                }
                                releaseTTS();
                            } catch (Exception e) {
                                if (DEBUG) {
                                    MyLog.e(CLS_NAME, "ttsLoc Exception");
                                    e.printStackTrace();
                                }
                                releaseTTS();
                            }
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    };
    private final Runnable engineMonitor = new Runnable() {
        @Override
        public void run() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "engineMonitor: notifying error");
            }
            System.gc();
            progressListener.onError(UTTERANCE_ID);
            try {
                if (!disposable.isDisposed()) {
                    disposable.dispose();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "engineMonitor: Exception");
                    e.printStackTrace();
                }
            } finally {
                disposable = null;
            }
        }
    };
    private final ai.saiy.android.tts.SaiyProgressListener progressListener = new ai.saiy.android.tts.SaiyProgressListener() {
        private long startTime;

        @Override
        public void onDone(String utteranceId) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "progressListener: onDone");
                MyLog.getElapsed("onDone", startTime);
            }
            diagnosticsInfo.getVoiceEngineInfos().get(diagnosticIndex.get()).setTextToSpeechDone(true);
            diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_speech_complete));
            diagnosticsInfo.setPassedCount(diagnosticsInfo.getPassedCount() + 1);
            diagnosticInfoListener.setPassedCount(String.valueOf(diagnosticsInfo.getPassedCount()));
            releaseTTS();
            removeRunnableCallback(engineMonitor);
        }

        @Override
        public void onError(String utteranceId) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "progressListener: onResetError");
            }
            diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_speech_error));
            diagnosticsInfo.setErrorCount(diagnosticsInfo.getErrorCount() + 1);
            diagnosticInfoListener.setErrorCount(String.valueOf(diagnosticsInfo.getErrorCount()));
            releaseTTS();
            removeRunnableCallback(engineMonitor);
        }

        @Override
        public void onError(String utteranceId, int errorCode) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "progressListener: onResetError: errorCode" + errorCode);
            }
            super.onError(utteranceId, errorCode);
        }

        @Override
        public void onStart(String utteranceId) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "progressListener: onStart: " + utteranceId);
            }
            startTime = System.nanoTime();
            diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_speech_start));
            diagnosticsInfo.setPassedCount(diagnosticsInfo.getPassedCount() + 1);
            diagnosticInfoListener.setPassedCount(String.valueOf(diagnosticsInfo.getPassedCount()));
            removeRunnableCallback(engineMonitor);
            restartEngineMonitor();
        }

        @Override
        public void onStop(String utteranceId, boolean interrupted) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "progressListener: onStop: interrupted " + interrupted);
            }
            if (interrupted) {
                onDone(utteranceId);
            }
            super.onStop(utteranceId, interrupted);
        }
    };
    private final DiagnosticsInfo diagnosticsInfo = new DiagnosticsInfo();

    public DiagnosticsHelper(Context context, DiagnosticInfoListener listener) {
        mContext = context;
        diagnosticInfoListener = listener;
        packageManager = mContext.getPackageManager();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
        handler = new Handler(Looper.getMainLooper());
    }

    private Locale stringToLocale(String str) {
        Locale locale;
        try {
            if (str.contains(XMLResultsHandler.SEP_HYPHEN)) {
                StringTokenizer stringTokenizer = new StringTokenizer(str, XMLResultsHandler.SEP_HYPHEN);
                switch (stringTokenizer.countTokens()) {
                    case 1:
                        locale = new Locale(stringTokenizer.nextToken());
                        break;
                    case 2:
                        locale = new Locale(stringTokenizer.nextToken(), stringTokenizer.nextToken());
                        break;
                    case 3:
                        locale = new Locale(stringTokenizer.nextToken(), stringTokenizer.nextToken(), stringTokenizer.nextToken());
                        break;
                    default:
                        locale = new Locale(str);
                        break;
                }
            } else if (str.contains("_")) {
                StringTokenizer stringTokenizer = new StringTokenizer(str, "_");
                switch (stringTokenizer.countTokens()) {
                    case 1:
                        locale = new Locale(stringTokenizer.nextToken());
                        break;
                    case 2:
                        locale = new Locale(stringTokenizer.nextToken(), stringTokenizer.nextToken());
                        break;
                    case 3:
                        locale = new Locale(stringTokenizer.nextToken(), stringTokenizer.nextToken(), stringTokenizer.nextToken());
                        break;
                    default:
                        locale = new Locale(str);
                        break;
                }
            } else {
                locale = new Locale(str);
            }
            return locale;
        } catch (IllegalArgumentException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stringToLocale: IllegalArgumentException");
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stringToLocale: NullPointerException");
            }
        } catch (MissingResourceException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stringToLocale: MissingResourceException");
            }
        } catch (NoSuchElementException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stringToLocale: NoSuchElementException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stringToLocale: Exception");
            }
        }
        return new Locale(str);
    }

    private void examineBundle(Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "examineBundle");
        }
        if (bundle == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "examineBundle: bundle null");
            }
        } else {
            for (String str : bundle.keySet()) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "examineBundle: " + str + " ~ " + bundle.get(str));
                }
            }
        }
    }

    private void removeRunnableCallback(Runnable runnable) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "removeRunnableCallback");
        }
        try {
            handler.removeCallbacks(runnable);
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "removeRunnableCallback: NullPointerException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "removeRunnableCallback: Exception");
                e.printStackTrace();
            }
        }
    }

    private void flashCursor(String str, int mode) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "flashCursor");
        }
        final long currentTimeMillis = System.currentTimeMillis();
        int i = 0;
        while (!isCancelled.get() && System.currentTimeMillis() - currentTimeMillis < 3100) {
            if (i == 0) {
                switch (mode) {
                    case SET_MODE:
                        diagnosticInfoListener.setDiagnosticInfo(str + ".");
                        break;
                    case APPEND_MODE:
                        diagnosticInfoListener.appendDiagnosticInfo("\n" + str + ".");
                        break;
                }
            } else {
                if (i % 3 == 0) {
                    switch (mode) {
                        case SET_MODE:
                            diagnosticInfoListener.setDiagnosticInfo(str + ".");
                            break;
                        case APPEND_MODE:
                            diagnosticInfoListener.replaceDiagnosticInfo(str + ".");
                            break;
                    }
                } else if (i % 3 == 1) {
                    switch (mode) {
                        case SET_MODE:
                            diagnosticInfoListener.setDiagnosticInfo(str + "..");
                            break;
                        case APPEND_MODE:
                            diagnosticInfoListener.replaceDiagnosticInfo(str + "..");
                            break;
                    }
                } else {
                    switch (mode) {
                        case SET_MODE:
                            diagnosticInfoListener.setDiagnosticInfo(str + "...");
                            break;
                        case APPEND_MODE:
                            diagnosticInfoListener.replaceDiagnosticInfo(str + "...");
                            break;
                    }
                }
            }
            i++;
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        switch (mode) {
            case SET_MODE:
                populateASRCount();
                break;
            case APPEND_MODE:
                populateTTSCount();
                break;
            default:
                break;
        }
    }

    private void onComplete(boolean forceStop) {
        if (isCompleted.get()) {
            return;
        }
        isCompleted.set(true);
        diagnosticInfoListener.onComplete(diagnosticsInfo, forceStop);
        isDoInBackground.set(false);
    }

    private void setVRLocales(ArrayList<String> arrayList) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setVRLocales");
        }
        if (arrayList == null || arrayList.isEmpty() || isCancelled.get()) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "setVRLocales: availableVoices null or empty");
            }
            diagnosticsInfo.setErrorCount(diagnosticsInfo.getErrorCount() + 1);
            diagnosticInfoListener.setErrorCount(String.valueOf(diagnosticsInfo.getErrorCount()));
            flashCursor(mContext.getString(R.string.diagnostics_checking_tts), APPEND_MODE);
            return;
        }
        for (int i = 0; i < arrayList.size() && !isCancelled.get(); ++i) {
            String str = arrayList.get(i);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "loc: " + str);
            }
            try {
                Thread.sleep(7L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            diagnosticsInfo.getSupportedLocales().add(stringToLocale(str));
            diagnosticInfoListener.appendDiagnosticInfo(str);
        }
        diagnosticsInfo.setPassedCount(diagnosticsInfo.getPassedCount() + 1);
        diagnosticInfoListener.setPassedCount(String.valueOf(diagnosticsInfo.getPassedCount()));
        flashCursor(mContext.getString(R.string.diagnostics_checking_tts), APPEND_MODE);
    }

    private void populateTTSCount() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "populateTTSCount");
        }
        List<ResolveInfo> providers = getTTSProviders();
        int size = providers.size();
        if (DEBUG) {
            MyLog.d(CLS_NAME, "populateTTSCount: providers: " + size);
        }
        if (size == 0) {
            diagnosticsInfo.setErrorCount(diagnosticsInfo.getErrorCount() + 1);
            diagnosticInfoListener.setErrorCount(String.valueOf(diagnosticsInfo.getErrorCount()));
        } else {
            for (int i = 0; i < size && !isCancelled.get(); i++) {
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                diagnosticInfoListener.appendDiagnosticInfo("\n" + providers.get(i).loadLabel(packageManager).toString());
                diagnosticInfoListener.appendDiagnosticInfo(UtilsApplication.getPackageName(providers.get(i)));
                diagnosticInfoListener.setTTSCount(String.valueOf(i + 1));
                diagnosticsInfo.setTTSCount(i + 1);
            }
        }
        getEngines();
    }

    private void getEngines() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getEngines");
        }
        if (isCancelled.get()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getEngines cancelled");
            }
            return;
        }
        if (containerVoiceEngine.size() == 1) {
            diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_init_prepare) + XMLResultsHandler.SEP_SPACE + containerVoiceEngine.size() + XMLResultsHandler.SEP_SPACE + mContext.getString(R.string.engine));
        } else {
            diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_init_prepare) + XMLResultsHandler.SEP_SPACE + containerVoiceEngine.size() + XMLResultsHandler.SEP_SPACE + mContext.getString(R.string.engines));
        }
        try {
            Thread.sleep(2500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < containerVoiceEngine.size() && !isCancelled.get(); i++) {
            diagnosticInfoListener.appendDiagnosticInfo(containerVoiceEngine.get(i).getApplicationName());
            diagnosticInfoListener.appendDiagnosticInfo(containerVoiceEngine.get(i).getPackageName());
            if (containerVoiceEngine.get(i).getAvailableLocales() == null) {
                containerVoiceEngine.get(i).setAvailableLocales(Collections.emptyList());
            }
            for (int j = 0; j < containerVoiceEngine.get(i).getAvailableLocales().size() && !isCancelled.get(); j++) {
                diagnosticInfoListener.appendDiagnosticInfo(containerVoiceEngine.get(i).getAvailableLocales().get(j));
                try {
                    Thread.sleep(20L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (containerVoiceEngine.get(i).getDataRoot() != null) {
                diagnosticInfoListener.appendDiagnosticInfo(mContext.getString(R.string.data_root_) + containerVoiceEngine.get(i).getDataRoot());
            }
            if (DEBUG) {
                MyLog.v(CLS_NAME, "cve: " + containerVoiceEngine.get(i).getApplicationName() + " - " + containerVoiceEngine.get(i).getAvailableLocales().size() + " - " + containerVoiceEngine.get(i).getAvailableLocales().toString());
            }
            try {
                Thread.sleep(20L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        initEngines();
    }

    private void initEngines() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "initEngines");
        }
        if (diagnosticIndex.get() >= containerVoiceEngine.size() || isCancelled.get()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initEngines complete");
            }
            destroyTTS();
            return;
        }
        diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_initialising) + XMLResultsHandler.SEP_SPACE + containerVoiceEngine.get(diagnosticIndex.get()).getApplicationName());
        VoiceEngineInfo voiceEngineInfo = new VoiceEngineInfo();
        voiceEngineInfo.setApplicationName(containerVoiceEngine.get(diagnosticIndex.get()).getApplicationName());
        voiceEngineInfo.setPackageName(containerVoiceEngine.get(diagnosticIndex.get()).getPackageName());
        diagnosticsInfo.getVoiceEngineInfos().add(voiceEngineInfo);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "TextToSpeech thread running");
                }
                if (diagnosticIndex.get() >= containerVoiceEngine.size() || isCancelled.get()) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "TextToSpeech thread engineCount changed");
                    }
                    destroyTTS();
                    return;
                }
                try {
                    tts = new TextToSpeech(mContext, initListener, containerVoiceEngine.get(diagnosticIndex.get()).getPackageName());
                } catch (IndexOutOfBoundsException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "TextToSpeech thread IndexOutOfBoundsException");
                        e.printStackTrace();
                    }
                    destroyTTS();
                }
            }
        });
        if (diagnosticIndex.get() == 0) {
            waitForEngines();
        }
    }

    private void waitForEngines() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "waitForEngines");
        }
        synchronized (lock) {
            try {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "waitForEngines waiting");
                }
                lock.wait();
            } catch (InterruptedException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "waitForEngines InterruptedException");
                    e.printStackTrace();
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "waitForEngines: released");
        }
        onComplete(isCancelled.get());
    }

    private void releaseTTS() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "releaseTTS");
        }
        diagnosticIndex.incrementAndGet();
        try {
            if (tts != null) {
                tts.shutdown();
                tts = null;
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "releaseTTS NullPointerException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "releaseTTS Exception");
                e.printStackTrace();
            }
        }
        initEngines();
    }

    private List<ResolveInfo> getTTSProviders() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getTTSProviders");
        }
        List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA), PackageManager.GET_META_DATA);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getTTSProviders: " + queryIntentActivities.size());
        }
        return queryIntentActivities;
    }

    private void populateASRCount() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "populateASRCount");
        }
        if (!haveASRProviders()) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "populateASRCount: none");
            }
            diagnosticsInfo.setASRCount(0);
            diagnosticInfoListener.setASRCount(String.valueOf(0));
            flashCursor(mContext.getString(R.string.diagnostics_checking_tts), APPEND_MODE);
            return;
        }
        diagnosticInfoListener.setPassedCount(String.valueOf(diagnosticsInfo.getPassedCount() + 1));
        final List<ResolveInfo> asrProviders = getASRProviders();
        int size = asrProviders.size();
        if (DEBUG) {
            MyLog.d(CLS_NAME, "populateASRCount: providers: " + size);
        }
        if (size == 0) {
            diagnosticsInfo.setErrorCount(diagnosticsInfo.getErrorCount() + 1);
            diagnosticInfoListener.setErrorCount(String.valueOf(diagnosticsInfo.getErrorCount()));
            return;
        }

        String string = Settings.Secure.getString(mContext.getContentResolver(), "voice_recognition_service"/*Settings.Secure.VOICE_RECOGNITION_SERVICE*/);
        String recognizerPackageName;
        if (string != null) {
            String[] split = string.split("/", 2);
            recognizerPackageName = split.length > 1 ? split[0] : string;
        } else {
            recognizerPackageName = null;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "populateASRCount: recognizerPackageName: " + recognizerPackageName);
        }
        for (int i = 0; i < size && !isCancelled.get(); i++) {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String packageName = UtilsApplication.getPackageName(asrProviders.get(i));
            String applicationName = asrProviders.get(i).loadLabel(packageManager).toString();
            if (DEBUG) {
                MyLog.d(CLS_NAME, "populateASRCount: packageName: " + packageName);
            }
            diagnosticsInfo.setPackageName(packageName);
            if (recognizerPackageName != null && recognizerPackageName.matches(packageName)) {
                diagnosticsInfo.setApplicationName(applicationName);
            } else if (!UtilsString.notNaked(diagnosticsInfo.getApplicationName())) {
                diagnosticsInfo.setApplicationName(string);
            }
            diagnosticInfoListener.appendDiagnosticInfo("\n" + applicationName);
            diagnosticInfoListener.appendDiagnosticInfo(packageName);
            diagnosticsInfo.setASRCount(i + 1);
            diagnosticInfoListener.setASRCount(String.valueOf(i + 1));
        }
        diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_awaiting_locales));
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                if (isBroadcastReceived.get() || isCancelled.get()) {
                    return;
                }
                diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_wait_1));
                try {
                    Thread.sleep(7 * 1000L);
                } catch (InterruptedException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "populateASRCount: InterruptedException");
                        e.printStackTrace();
                    }
                }
                if (isBroadcastReceived.get() || isCancelled.get()) {
                    return;
                }
                diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_wait_2));
                try {
                    Thread.sleep(7 * 1000L);
                } catch (InterruptedException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "populateASRCount: InterruptedException");
                        e.printStackTrace();
                    }
                }
                if (isBroadcastReceived.get() || isCancelled.get()) {
                    return;
                }
                diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_wait_3));
                try {
                    Thread.sleep(5 * 1000L);
                } catch (InterruptedException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "populateASRCount: InterruptedException");
                        e.printStackTrace();
                    }
                }
                if (isBroadcastReceived.get() || isCancelled.get()) {
                    return;
                }
                diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_wait_4));
                try {
                    Thread.sleep(7 * 1000L);
                } catch (InterruptedException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "populateASRCount: InterruptedException");
                        e.printStackTrace();
                    }
                }
                if (isBroadcastReceived.get() || isCancelled.get()) {
                    return;
                }
                diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_wait_5));
                try {
                    Thread.sleep(7 * 1000L);
                } catch (InterruptedException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "populateASRCount: InterruptedException");
                        e.printStackTrace();
                    }
                }
                if (isBroadcastReceived.get() || isCancelled.get()) {
                    return;
                }
                diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_wait_6));
                try {
                    Thread.sleep(10 * 1000L);
                } catch (InterruptedException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "populateASRCount: InterruptedException");
                        e.printStackTrace();
                    }
                }
                if (isBroadcastReceived.get() || isCancelled.get()) {
                    return;
                }
                diagnosticInfoListener.appendDiagnosticInfo("\n" + mContext.getString(R.string.diagnostics_wait_7));
            }
        }, 5, TimeUnit.SECONDS);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "populateASRCount: waiting...");
        }
        long currentTimeMillis = System.currentTimeMillis();
        while (!isBroadcastReceived.get() && !isCancelled.get() && System.currentTimeMillis() - currentTimeMillis < 60 * 1000) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "populateASRCount: InterruptedException");
                    e.printStackTrace();
                }
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "populateASRCount: waiting...");
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "populateASRCount: received broadcast: " + isBroadcastReceived.get());
        }
        setVRLocales(diagnosticsInfo.getSupportedLanguages());
    }

    private List<ResolveInfo> getASRProviders() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getASRProviders");
        }
        List<ResolveInfo> queryIntentServices = packageManager.queryIntentServices(new Intent(RecognitionService.SERVICE_INTERFACE), 0);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getASRProviders: " + queryIntentServices.size());
        }
        return queryIntentServices;
    }

    private boolean haveASRProviders() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "haveASRProviders: " + SpeechRecognizer.isRecognitionAvailable(mContext));
        }
        return SpeechRecognizer.isRecognitionAvailable(mContext);
    }

    /**
     * collects language details
     */
    private void checkSpeechRecognizer() {
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        if (!packageManager.queryIntentActivities(recognizerIntent, PackageManager.GET_META_DATA).isEmpty() && !isCancelled.get()) {
            Intent detailsIntent = RecognizerIntent.getVoiceDetailsIntent(mContext);
            if (detailsIntent == null) {
                final Intent voiceSearchIntent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
                final ResolveInfo resolveInfo = mContext.getPackageManager().resolveActivity(
                        voiceSearchIntent, 0);
                detailsIntent = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
                if (resolveInfo != null) {
                    detailsIntent.setPackage(UtilsApplication.getPackageName(resolveInfo));
                } else {
                    detailsIntent.setPackage(DeviceInfo.getDefaultVRProvider(mContext));
                }
            }
            mContext.sendOrderedBroadcast(detailsIntent, null, new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onReceive: ResultCode " + getResultCode());
                        MyLog.i(CLS_NAME, "onReceive: String data: " + (getResultData() != null));
                    }
                    if (isCancelled.get()) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onReceive: cancelled");
                        }
                        return;
                    }
                    isBroadcastReceived.set(true);
                    if (getResultCode() != SAIY_VR_REQUEST_CODE) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onReceive: getResultCode() != SAIY_VR_REQUEST_CODE");
                        }
                        diagnosticsInfo.setErrorCount(diagnosticsInfo.getErrorCount() + 1);
                        diagnosticInfoListener.setErrorCount(String.valueOf(diagnosticsInfo.getErrorCount()));
                        return;
                    }
                    if (intent == null) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onReceive: Intent null");
                        }
                        diagnosticsInfo.setErrorCount(diagnosticsInfo.getErrorCount() + 1);
                        diagnosticInfoListener.setErrorCount(String.valueOf(diagnosticsInfo.getErrorCount()));
                        return;
                    }
                    Bundle resultExtras = getResultExtras(true);
                    examineBundle(resultExtras);
                    if (resultExtras == null) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onReceive: Bundle null");
                        }
                        diagnosticsInfo.setErrorCount(diagnosticsInfo.getErrorCount() + 1);
                        diagnosticInfoListener.setErrorCount(String.valueOf(diagnosticsInfo.getErrorCount()));
                        return;
                    }
                    if (!resultExtras.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onReceive: EXTRA_SUPPORTED_LANGUAGES missing");
                        }
                        diagnosticsInfo.setErrorCount(diagnosticsInfo.getErrorCount() + 1);
                        diagnosticInfoListener.setErrorCount(String.valueOf(diagnosticsInfo.getErrorCount()));
                        return;
                    }
                    ArrayList<String> supportedLanguages = resultExtras.getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);
                    if (supportedLanguages != null && !supportedLanguages.isEmpty()) {
                        diagnosticsInfo.setSupportedLanguages(supportedLanguages);
                        return;
                    }
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onReceive: vr locales naked");
                    }
                    diagnosticsInfo.setErrorCount(diagnosticsInfo.getErrorCount() + 1);
                    diagnosticInfoListener.setErrorCount(String.valueOf(diagnosticsInfo.getErrorCount()));
                }
            }, null, SAIY_VR_REQUEST_CODE, null, null);
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "The user has no Voice Recognition Service available");
            }
            diagnosticsInfo.setErrorCount(diagnosticsInfo.getErrorCount() + 1);
            diagnosticInfoListener.setErrorCount(String.valueOf(diagnosticsInfo.getErrorCount()));
        }
    }

    private void destroyTTS() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "destroyTTS");
        }
        removeRunnableCallback(engineMonitor);
        try {
            if (tts != null) {
                tts.shutdown();
                tts = null;
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "destroyTTS NullPointerException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "destroyTTS Exception");
                e.printStackTrace();
            }
        }
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    private void restartEngineMonitor() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "restartEngineMonitor");
        }
        removeRunnableCallback(engineMonitor);
        handler.postDelayed(engineMonitor, 15 * 1000L);
    }

    public void cancel() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "cancel");
        }
        if (isCancelled()) {
            return;
        }
        isCancelled.set(true);
        destroyTTS();
        onComplete(isCancelled.get());
    }

    public void doInBackground(ArrayList<VoiceEngine> arrayList) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "doInBackground");
        }
        isDoInBackground.set(true);
        containerVoiceEngine = arrayList;
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                checkSpeechRecognizer();
                flashCursor(mContext.getString(R.string.diagnostics_checking_vr), SET_MODE);
            }
        });
    }

    public boolean isCancelled() {
        return isCancelled.get();
    }

    public boolean isCompleted() {
        return isCompleted.get();
    }

    public boolean isDiagnosing() {
        return !isCompleted.get() && !isCancelled.get() && isDoInBackground.get();
    }
}

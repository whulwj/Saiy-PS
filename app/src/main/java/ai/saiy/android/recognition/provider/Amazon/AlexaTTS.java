package ai.saiy.android.recognition.provider.Amazon;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.amazon.UtilsNetwork;
import ai.saiy.android.amazon.listener.IAlexaToken;
import ai.saiy.android.amazon.resolve.DirectiveList;
import ai.saiy.android.amazon.resolve.ResolveAmazon;
import ai.saiy.android.recognition.SaiyRecognitionListener;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.tts.SaiyTextToSpeech;
import ai.saiy.android.tts.helper.TTSDefaults;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsFile;
import ai.saiy.android.utils.UtilsString;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class AlexaTTS extends ai.saiy.android.tts.SaiyProgressListener implements IAlexaToken, TextToSpeech.OnInitListener {
    private final SaiyRecognitionListener listener;
    private final Locale ttsLocale;
    private String alexaAccessToken;
    private volatile boolean isWorking;
    private final Context mContext;
    private volatile TextToSpeech tts;
    private volatile File audioCacheFile;
    private long responseTime;
    private long requestTime;
    private final String recognitionResult;
    private String initEngine;
    private volatile byte[] fileBytes;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = AlexaTTS.class.getSimpleName();

    private RequestBody requestBody = new RequestBody() {
        @Override
        public MediaType contentType() {
            return MediaType.get(UtilsNetwork.OCTET_STREAM);
        }

        @Override
        public void writeTo(@NonNull BufferedSink sink) throws IOException {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "writeTo: fileBytes: " + AlexaTTS.this.fileBytes.length);
            }
            long nanoTime = System.nanoTime();
            int i = 44; //TODO check
            while (i < AlexaTTS.this.fileBytes.length) {
                int min = Math.min(320, AlexaTTS.this.fileBytes.length - i);
                sink.write(AlexaTTS.this.fileBytes, i, min);
                i += min;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "processing complete. Queue empty");
                MyLog.getElapsed(CLS_NAME, "writeTo", nanoTime);
            }
            AlexaTTS.this.requestTime = System.nanoTime();
        }
    };

    public AlexaTTS(final Context context, SaiyRecognitionListener listener, Locale locale, String recognitionResult, final String initEngine, List<TextToSpeech.EngineInfo> list) {
        this.mContext = context;
        this.listener = listener;
        this.ttsLocale = locale;
        this.recognitionResult = recognitionResult;
        this.initEngine = initEngine;
        Global.setAlexDirectiveBundle(null);
        this.isWorking = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
                ai.saiy.android.amazon.TokenHelper.getAccessToken(context, AlexaTTS.this);
            }
        }).start();
        ArrayList<String> arrayList = new ArrayList<>();
        if (initEngine.startsWith(TTSDefaults.TTS_PKG_NAME_CEREPROC)) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "enginePackage: TTS_PKG_NAME_CEREPROC");
                for (TextToSpeech.EngineInfo engineInfo : list) {
                    MyLog.v(CLS_NAME, "label: " + engineInfo.label);
                    MyLog.v(CLS_NAME, "name: " + engineInfo.name);
                }
            }
            for (TextToSpeech.EngineInfo engineInfo : list) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "label: " + engineInfo.label);
                    MyLog.v(CLS_NAME, "name: " + engineInfo.name);
                }
                if (UtilsString.notNaked(engineInfo.name) && !engineInfo.name.startsWith(TTSDefaults.TTS_PKG_NAME_CEREPROC)) {
                    if (engineInfo.name.matches(TTSDefaults.TTS_PKG_NAME_GOOGLE)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "engineName: found TTS_PKG_NAME_GOOGLE");
                        }
                        this.initEngine = engineInfo.name;
                        arrayList.clear();
                    } else {
                        arrayList.add(engineInfo.name);
                    }
                }
            }
        }
        if (!arrayList.isEmpty()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "availableEngines: " + arrayList.size());
                MyLog.i(CLS_NAME, "availableEngines: contains Pico: " + arrayList.contains(TTSDefaults.TTS_PKG_NAME_PICO));
            }
            if (arrayList.size() > 2 && arrayList.contains(TTSDefaults.TTS_PKG_NAME_PICO)) {
                arrayList.remove(TTSDefaults.TTS_PKG_NAME_PICO);
                arrayList.add(TTSDefaults.TTS_PKG_NAME_PICO);
                if (DEBUG) {
                    for (TextToSpeech.EngineInfo engineInfo2 : list) {
                        MyLog.v(CLS_NAME, "reordered label: " + engineInfo2.label);
                        MyLog.v(CLS_NAME, "reordered name: " + engineInfo2.name);
                    }
                }
            }
            for (String engine : arrayList) {
                if (!engine.startsWith(TTSDefaults.TTS_PKG_NAME_CEREPROC)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "availableEngines: selecting: " + engine);
                    }
                    this.initEngine = engine;
                }
            }
        } else if (this.initEngine.startsWith(TTSDefaults.TTS_PKG_NAME_CEREPROC)) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "chosenEngine: TTS_PKG_NAME_CEREPROC: warning");
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    ai.saiy.android.utils.UtilsToast.showToast(context, context.getString(R.string.title_error_alexa_cereproc), Toast.LENGTH_LONG);
                }
            });
        }

        if (UtilsString.notNaked(this.initEngine)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "final chosenEngine: " + this.initEngine);
            }
            this.tts = new TextToSpeech(context, this, this.initEngine);
        } else {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "final chosenEngine naked");
            }
            this.tts = new TextToSpeech(context, this);
        }
    }

    private void sendAudio() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new ai.saiy.android.amazon.SpeechRecognizer(AlexaTTS.this.requestBody).stream(UtilsNetwork.getUrl(AlexaTTS.this.mContext), AlexaTTS.this.alexaAccessToken, new ai.saiy.android.amazon.listener.StreamListener() {
                        @Override
                        public void onError(Exception e) {
                            if (DEBUG) {
                                MyLog.e(CLS_NAME, "onError: Exception");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onSuccess(okhttp3.Call call) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onSuccess");
                            }
                            try {
                                Response response = call.execute();
                                if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "onSuccess: Silence");
                                    }
                                    response.body().close();
                                    return;
                                }
                                ResolveAmazon resolveAmazon = new ResolveAmazon(response.body().byteStream(), response, ai.saiy.android.utils.UtilsFile.getTempAudioFile(AlexaTTS.this.mContext));
                                DirectiveList directiveList = resolveAmazon.parse();
                                response.body().close();
                                AlexaTTS.this.isWorking = false;
                                AlexaTTS.this.saveResults(directiveList);
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onSuccess: end");
                                }
                            } catch (IOException e) {
                                if (DEBUG) {
                                    MyLog.e(CLS_NAME, "onSuccess: IOException");
                                    e.printStackTrace();
                                }
                            } catch (JSONException e) {
                                if (DEBUG) {
                                    MyLog.e(CLS_NAME, "onSuccess: JSONException");
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "sendAudio: finally");
                    }
                    if (AlexaTTS.this.isWorking) {
                        AlexaTTS.this.shutdownTTS();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "sendAudio: Exception");
                        e.printStackTrace();
                    }
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "sendAudio: finally");
                    }
                    if (AlexaTTS.this.isWorking) {
                        AlexaTTS.this.shutdownTTS();
                    }
                } catch (Throwable th) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "sendAudio: finally");
                    }
                    if (AlexaTTS.this.isWorking) {
                        AlexaTTS.this.shutdownTTS();
                    }
                }
            }
        }).start();
    }

    private void saveResults(DirectiveList directiveList) {
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, "saveResults start", this.requestTime);
        }
        if (directiveList.getErrorCode() != 0) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "saveResults: error: " + directiveList.getErrorCode());
            }
            shutdownTTS();
            Global.setAlexDirectiveBundle(null);
            return;
        }
        Bundle bundle = new Bundle();
        if (directiveList.getFile() != null) {
            bundle.putString(SaiyRecognitionListener.ALEX_FILE, directiveList.getFile().getPath());
        }
        if (directiveList.hasDirectiveType()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "saveResults: hasDirectiveType: true: " + directiveList.getDirectiveType().name());
            }
            bundle.putSerializable(SaiyRecognitionListener.ALEXA_DIRECTIVE, directiveList.getDirectiveType());
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "saveResults: hasDirectiveType: false");
        }
        bundle.putInt(LocalRequest.EXTRA_ACTION, directiveList.getAction());
        Global.setAlexDirectiveBundle(bundle);
    }

    private void shutdownTTS() {
        if (DEBUG) {
            MyLog.w(CLS_NAME, "shutdownTTS");
        }
        if (this.isWorking) {
            Global.setAlexDirectiveBundle(null);
        }
        if (this.tts != null) {
            try {
                this.tts.shutdown();
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "shutdownTTS: Exception");
                    e.printStackTrace();
                }
            }
        }
        if (this.audioCacheFile == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "shutdownTTS: tempFile null");
            }
        } else {
            boolean delete = this.audioCacheFile.delete();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "shutdownTTS: tempFile deleted: " + delete);
            }
        }
    }

    @Override
    public void onSuccess(ai.saiy.android.amazon.AmazonCredentials credentials) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "IAlexaToken: onSuccess");
        }
        this.alexaAccessToken = credentials.getAccessToken();
    }

    @Override
    public void onFailure(Exception e) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "IAlexaToken: onError");
            e.printStackTrace();
        }
        shutdownTTS();
    }

    @Override
    public void onAudioAvailable(String utteranceId, byte[] audio) {
    }

    @Override
    public void onBeginSynthesis(String utteranceId, int sampleRateInHz, int audioFormat, int channelCount) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBeginSynthesis");
            MyLog.i(CLS_NAME, "onBeginSynthesis: sampleRateInHz: " + sampleRateInHz);
            MyLog.i(CLS_NAME, "onBeginSynthesis: audioFormat: " + audioFormat);
            MyLog.i(CLS_NAME, "onBeginSynthesis: channelCount: " + channelCount);
            MyLog.getElapsed(CLS_NAME, "onBeginSynthesis", this.responseTime);
        }
    }

    @Override
    public void onDone(String utteranceId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDone");
            MyLog.getElapsed(CLS_NAME, "onDone", this.responseTime);
        }
        try {
            this.fileBytes = org.apache.commons.io.FileUtils.readFileToByteArray(this.audioCacheFile);
        } catch (IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onDone: IOException");
                e.printStackTrace();
            }
        } catch (NullPointerException e2) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onDone: NullPointerException");
                e2.printStackTrace();
            }
        } catch (Exception e3) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onDone: Exception");
                e3.printStackTrace();
            }
        } finally {
            shutdownTTS();
        }
        if (UtilsString.notNaked(this.alexaAccessToken)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onDone: have accessToken");
            }
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            sendAudio();
            return;
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDone: accessToken naked");
        }
        for (int i = 1; !UtilsString.notNaked(this.alexaAccessToken) && i < 7; i++) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onDone: sleeping: " + i);
            }
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e4) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onDone: sleeping : InterruptedException");
                    e4.printStackTrace();
                }
            }
        }
        if (UtilsString.notNaked(this.alexaAccessToken)) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onDone: retry have accessToken");
            }
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            sendAudio();
        } else if (DEBUG) {
            MyLog.e(CLS_NAME, "onDone: retry accessToken null");
        }
    }

    @Override
    public void onError(String utteranceId) {
        if (DEBUG) {
            MyLog.w(CLS_NAME, "onResetError");
        }
        shutdownTTS();
    }

    @Override
    public void onInit(int status) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onInit");
        }
        switch (status) {
            case SaiyTextToSpeech.ERROR:
                shutdownTTS();
                return;
            case SaiyTextToSpeech.SUCCESS:
                try {
                    this.tts.setOnUtteranceProgressListener(this);
                    this.audioCacheFile = UtilsFile.getTempAudioFile(this.mContext);
                    if (this.audioCacheFile == null) {
                        shutdownTTS();
                        return;
                    }
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "doAudioCache: " + this.audioCacheFile.getAbsolutePath());
                    }
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "alexa_utt_id");
                    hashMap.put(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS, String.valueOf(false));
                    this.tts.setLanguage(Locale.ENGLISH);
                    this.responseTime = System.nanoTime();
                    this.tts.synthesizeToFile(this.recognitionResult, hashMap, this.audioCacheFile.getPath());
                    return;
                } catch (NullPointerException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "synthesizeToFile: NullPointerException");
                        e.printStackTrace();
                    }
                    shutdownTTS();
                    return;
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "synthesizeToFile: IOException");
                        e.printStackTrace();
                    }
                    shutdownTTS();
                    return;
                }
            default:
                break;
        }
    }

    @Override
    public void onStart(String utteranceId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onStart");
            MyLog.getElapsed(CLS_NAME, "onStart", this.responseTime);
        }
    }
}

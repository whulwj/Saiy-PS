package ai.saiy.android.recognition.provider.wit;

import android.content.Context;
import android.media.AudioRecord;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;

import androidx.annotation.NonNull;

import com.google.gson.GsonBuilder;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;

import ai.saiy.android.amazon.AmazonCredentials;
import ai.saiy.android.amazon.UtilsNetwork;
import ai.saiy.android.amazon.SpeechRecognizer;
import ai.saiy.android.amazon.TokenHelper;
import ai.saiy.android.amazon.listener.StreamListener;
import ai.saiy.android.amazon.resolve.DirectiveList;
import ai.saiy.android.amazon.resolve.ResolveAmazon;
import ai.saiy.android.amazon.listener.IAlexaToken;
import ai.saiy.android.api.SaiyDefaults;
import ai.saiy.android.api.language.vr.VRLanguageWit;
import ai.saiy.android.api.remote.Request;
import ai.saiy.android.audio.AudioParameters;
import ai.saiy.android.audio.SaiyRecorder;
import ai.saiy.android.audio.SaiySoundPool;
import ai.saiy.android.audio.pause.PauseDetector;
import ai.saiy.android.audio.pause.PauseListener;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.permissions.PermissionHelper;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.recognition.SaiyRecognitionListener;
import ai.saiy.android.recognition.provider.Amazon.VRLanguageAmazon;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class RecognitionWitHybrid implements IAlexaToken, PauseListener {
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_ = "Bearer ";
    private static final String JSON_HEADER_ACCEPT = "accept";
    private static final String HEADER_CONTENT_TYPE = "audio/raw;encoding=signed-integer;bits=16;rate=16000;endian=little";
    private static final String N_HEADER = "n";
    private static final String TRANSFER_ENCODING = "Transfer-Encoding";
    private static final String CHUNKED = "chunked";

    private final SaiyRecognitionListener listener;
    private final SaiyDefaults.LanguageModel languageModel;
    private final Locale ttsLocale;
    private final VRLanguageAmazon vRLanguageAmazon;
    private final VRLanguageWit vrLocale;
    private final SupportedLanguage sl;
    private String alexaAccessToken;
    private volatile boolean error;
    private final SaiySoundPool ssp;
    private PauseDetector pauseDetector;
    private final boolean servingRemote;
    private volatile SaiyRecorder saiyRecorder;
    private final Context mContext;
    private HttpsURLConnection urlConnection;
    private OutputStream outputStream;
    private InputStream inputStream;
    private final String apiKey;

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = RecognitionWitHybrid.class.getSimpleName();

    private final AtomicBoolean isRecording = new AtomicBoolean();
    private final Object lock = new Object();
    private final Object errorLock = new Object();
    private boolean isGoogleTTS = false;
    private RequestBody requestBody = new RequestBody() {
        @Override
        public MediaType contentType() {
            return MediaType.get(UtilsNetwork.OCTET_STREAM);
        }

        @Override
        public void writeTo(@NonNull BufferedSink sink) {
            try {
                int bufferSize = RecognitionWitHybrid.this.saiyRecorder.getBufferSize();
                if (RecognitionWitHybrid.this.DEBUG) {
                    MyLog.i(CLS_NAME, "writeTo: bufferSize: " + bufferSize);
                }
                RecognitionWitHybrid.this.urlConnection = (HttpsURLConnection) new URL("https://api.wit.ai/speech?v=20160526").openConnection();
                RecognitionWitHybrid.this.urlConnection.setAllowUserInteraction(false);
                RecognitionWitHybrid.this.urlConnection.setInstanceFollowRedirects(true);
                RecognitionWitHybrid.this.urlConnection.setRequestMethod(Constants.HTTP_POST);
                RecognitionWitHybrid.this.urlConnection.setRequestProperty(UtilsNetwork.CONTENT_TYPE, HEADER_CONTENT_TYPE);
                RecognitionWitHybrid.this.urlConnection.setRequestProperty(AUTHORIZATION, BEARER_ + RecognitionWitHybrid.this.apiKey);
                RecognitionWitHybrid.this.urlConnection.setRequestProperty(JSON_HEADER_ACCEPT, "application/vnd.wit.20160526");
                RecognitionWitHybrid.this.urlConnection.setRequestProperty(N_HEADER, "5");
                RecognitionWitHybrid.this.urlConnection.setUseCaches(false);
                RecognitionWitHybrid.this.urlConnection.setDoOutput(true);
                RecognitionWitHybrid.this.urlConnection.setRequestProperty(TRANSFER_ENCODING, CHUNKED);
                RecognitionWitHybrid.this.urlConnection.setChunkedStreamingMode(0);
                RecognitionWitHybrid.this.urlConnection.connect();
                RecognitionWitHybrid.this.outputStream = RecognitionWitHybrid.this.urlConnection.getOutputStream();
                byte[] bytes = new byte[bufferSize];
                while (RecognitionWitHybrid.this.isRecording.get() && RecognitionWitHybrid.this.saiyRecorder != null && RecognitionWitHybrid.this.saiyRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    if (RecognitionWitHybrid.this.saiyRecorder != null) {
                        int count = RecognitionWitHybrid.this.saiyRecorder.read(bytes);
                        RecognitionWitHybrid.this.listener.onBufferReceived(bytes);
                        if (RecognitionWitHybrid.this.servingRemote && !RecognitionWitHybrid.this.pauseDetector.hasDetected()) {
                            RecognitionWitHybrid.this.pauseDetector.addLength(bytes, count);
                            RecognitionWitHybrid.this.pauseDetector.monitor();
                        }
                        sink.write(bytes);
                        for (int i = 0; i < count; i++) {
                            RecognitionWitHybrid.this.outputStream.write(bytes[i]);
                        }
                    }
                }
                long nanoTime = System.nanoTime();
                RecognitionWitHybrid.this.audioShutdown();
                int responseCode = RecognitionWitHybrid.this.urlConnection.getResponseCode();
                if (RecognitionWitHybrid.this.DEBUG) {
                    MyLog.d(CLS_NAME, "responseCode: " + responseCode);
                    MyLog.d(CLS_NAME, "WitResponseTime", nanoTime);
                }
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    if (RecognitionWitHybrid.this.DEBUG) {
                        MyLog.e(CLS_NAME, "audioThread ErrorStream: " + UtilsString.streamToString(RecognitionWitHybrid.this.urlConnection.getErrorStream()));
                    }
                    RecognitionWitHybrid.this.listener.onError(android.speech.SpeechRecognizer.ERROR_NETWORK);
                } else {
                    RecognitionWitHybrid.this.inputStream = RecognitionWitHybrid.this.urlConnection.getInputStream();
                    ai.saiy.android.nlu.wit.NLUWit nluWit = new GsonBuilder().disableHtmlEscaping().create().fromJson(UtilsString.streamToString(RecognitionWitHybrid.this.inputStream), ai.saiy.android.nlu.wit.NLUWit.class);
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(nluWit.getText());
                    float[] confidenceArray = {nluWit.getConfidence()};
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(Request.RESULTS_RECOGNITION, arrayList);
                    bundle.putFloatArray(Request.CONFIDENCE_SCORES, confidenceArray);
                    RecognitionWitHybrid.this.listener.onResults(bundle);
                }
            } catch (NullPointerException e) {
                if (RecognitionWitHybrid.this.DEBUG) {
                    MyLog.e(CLS_NAME, "audioThread NullPointerException");
                    e.printStackTrace();
                }
                RecognitionWitHybrid.this.audioShutdown();
                RecognitionWitHybrid.this.handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
            } catch (MalformedURLException e) {
                if (RecognitionWitHybrid.this.DEBUG) {
                    MyLog.e(CLS_NAME, "MalformedURLException");
                    e.printStackTrace();
                }
                RecognitionWitHybrid.this.audioShutdown();
                RecognitionWitHybrid.this.handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
            } catch (UnknownHostException e) {
                if (RecognitionWitHybrid.this.DEBUG) {
                    MyLog.w(CLS_NAME, "UnknownHostException");
                    e.printStackTrace();
                }
                RecognitionWitHybrid.this.audioShutdown();
                RecognitionWitHybrid.this.handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
            } catch (IOException e) {
                if (RecognitionWitHybrid.this.DEBUG) {
                    MyLog.e(CLS_NAME, "audioThread IOException");
                    e.printStackTrace();
                }
                RecognitionWitHybrid.this.audioShutdown();
                RecognitionWitHybrid.this.handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
            } catch (IllegalStateException e) {
                if (RecognitionWitHybrid.this.DEBUG) {
                    MyLog.e(CLS_NAME, "audioThread IllegalStateException");
                    e.printStackTrace();
                }
                RecognitionWitHybrid.this.audioShutdown();
                RecognitionWitHybrid.this.handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
            } catch (ParseException e) {
                if (RecognitionWitHybrid.this.DEBUG) {
                    MyLog.w(CLS_NAME, "ParseException");
                    e.printStackTrace();
                }
                RecognitionWitHybrid.this.audioShutdown();
                RecognitionWitHybrid.this.handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
            } catch (Exception e) {
                if (RecognitionWitHybrid.this.DEBUG) {
                    MyLog.e(CLS_NAME, "audioThread Exception");
                    e.printStackTrace();
                }
                RecognitionWitHybrid.this.audioShutdown();
                RecognitionWitHybrid.this.handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
            } finally {
                RecognitionWitHybrid.this.dispose();
            }
            RecognitionWitHybrid.this.audioShutdown();
        }
    };

    public RecognitionWitHybrid(final Context context, SaiyRecognitionListener listener, SaiyDefaults.LanguageModel languageModel, Locale locale, VRLanguageAmazon vRLanguageAmazon, VRLanguageWit vRLanguageWit, SupportedLanguage supportedLanguage, SaiySoundPool ssp, boolean servingRemote, String apiKey) {
        this.mContext = context;
        this.listener = listener;
        this.languageModel = languageModel;
        this.ttsLocale = locale;
        this.vRLanguageAmazon = vRLanguageAmazon;
        this.vrLocale = vRLanguageWit;
        this.sl = supportedLanguage;
        this.servingRemote = servingRemote;
        this.ssp = ssp;
        this.apiKey = apiKey;
        Global.setAlexDirectiveBundle(null);
        AudioParameters audioParameters = AudioParameters.getDefault();
        if (this.servingRemote) {
            this.pauseDetector = new PauseDetector(this, audioParameters.getSampleRateInHz(), audioParameters.getnChannels(), 3250L);
        }
        this.saiyRecorder = new SaiyRecorder(audioParameters.getAudioSource(),
                audioParameters.getSampleRateInHz(), audioParameters.getChannelConfig(),
                audioParameters.getAudioFormat(), true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
                TokenHelper.getAccessToken(context, RecognitionWitHybrid.this);
            }
        }).start();
    }

    private void handleError(int error) {
        if (this.DEBUG) {
            MyLog.v(CLS_NAME, "in handleError");
        }
        synchronized (this.errorLock) {
            if (!this.error) {
                Global.setAlexDirectiveBundle(null);
                Recognition.setState(Recognition.State.IDLE);
                this.error = true;
                this.listener.onError(error);
            } else if (this.DEBUG) {
                MyLog.v(CLS_NAME, "handleError: thrown already: " + this.error);
            }
        }
    }

    private void sendResults(DirectiveList directiveList) {
        if (directiveList.getErrorCode() != 0) {
            if (this.DEBUG) {
                MyLog.w(CLS_NAME, "sendResults: error: " + directiveList.getErrorCode());
            }
            Global.setAlexDirectiveBundle(null);
            return;
        }
        Bundle bundle = new Bundle();
        if (directiveList.getFile() != null) {
            bundle.putString("alex_file", directiveList.getFile().getPath());
        }
        if (directiveList.hasDirectiveType()) {
            if (this.DEBUG) {
                MyLog.i(CLS_NAME, "sendResults: hasDirectiveType: true" + directiveList.getDirectiveType().name());
            }
            bundle.putSerializable("alex_directive", directiveList.getDirectiveType());
        } else if (this.DEBUG) {
            MyLog.i(CLS_NAME, "sendResults: hasDirectiveType: false");
        }
        bundle.putInt("extra_action", directiveList.getAction());
        Global.setAlexDirectiveBundle(bundle);
    }

    private void startRecording() {
        this.isRecording.set(true);
        this.error = false;
        if (this.servingRemote) {
            this.pauseDetector.begin();
        }
        this.ssp.play(this.ssp.getBeepStart());
        Recognition.setState(Recognition.State.LISTENING);
        this.listener.onReadyForSpeech(null);
        switch (this.saiyRecorder.initialise()) {
            case AudioRecord.STATE_UNINITIALIZED:
                if (this.DEBUG) {
                    MyLog.w(CLS_NAME, "AudioRecord.STATE_UNINITIALIZED");
                }
                handleError(android.speech.SpeechRecognizer.ERROR_AUDIO);
                return;
            case AudioRecord.STATE_INITIALIZED:
                switch (this.saiyRecorder.startRecording()) {
                    case AudioRecord.ERROR:
                        if (this.DEBUG) {
                            MyLog.w(CLS_NAME, "startRecording: != AudioRecord.RECORDSTATE_RECORDING");
                        }
                        handleError(android.speech.SpeechRecognizer.ERROR_AUDIO);
                        return;
                    case AudioRecord.RECORDSTATE_RECORDING:
                        if (this.DEBUG) {
                            MyLog.i(CLS_NAME, "startRecording: AudioRecord.RECORDSTATE_RECORDING");
                        }
                        sendAudio();
                        return;
                    default:
                        return;
                }
            default:
                break;
        }
    }

    private void sendAudio() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new SpeechRecognizer(RecognitionWitHybrid.this.requestBody).stream(UtilsNetwork.getUrl(RecognitionWitHybrid.this.mContext), RecognitionWitHybrid.this.alexaAccessToken, new StreamListener() {
                        @Override
                        public void onError(Exception e) {
                            if (RecognitionWitHybrid.this.DEBUG) {
                                MyLog.e(CLS_NAME, "onError: Exception");
                                e.printStackTrace();
                            }
                            RecognitionWitHybrid.this.handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
                        }

                        @Override
                        public void onSuccess(Call call) {
                            if (RecognitionWitHybrid.this.DEBUG) {
                                MyLog.i(CLS_NAME, "onSuccess");
                            }
                            try {
                                Response response = call.execute();
                                if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                                    if (RecognitionWitHybrid.this.DEBUG) {
                                        MyLog.w(CLS_NAME, "onSuccess: Silence");
                                    }
                                    response.body().close();
                                    RecognitionWitHybrid.this.handleError(android.speech.SpeechRecognizer.ERROR_NO_MATCH);
                                    return;
                                }
                                final DirectiveList directiveList = new ResolveAmazon(response.body().byteStream(), response, ai.saiy.android.utils.UtilsFile.getTempAudioFile(RecognitionWitHybrid.this.mContext)).parse(RecognitionWitHybrid.this.isGoogleTTS);
                                response.body().close();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        RecognitionWitHybrid.this.sendResults(directiveList);
                                    }
                                }).start();
                                if (RecognitionWitHybrid.this.DEBUG) {
                                    MyLog.i(CLS_NAME, "onSuccess: end");
                                }
                            } catch (IOException e) {
                                if (RecognitionWitHybrid.this.DEBUG) {
                                    MyLog.e(CLS_NAME, "onSuccess: IOException");
                                    e.printStackTrace();
                                }
                                RecognitionWitHybrid.this.handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
                            } catch (JSONException e2) {
                                if (RecognitionWitHybrid.this.DEBUG) {
                                    MyLog.e(CLS_NAME, "onSuccess: JSONException");
                                    e2.printStackTrace();
                                }
                                RecognitionWitHybrid.this.handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
                            }
                        }
                    });
                } catch (Exception e) {
                    if (RecognitionWitHybrid.this.DEBUG) {
                        MyLog.e(CLS_NAME, "sendAudio: Exception");
                        e.printStackTrace();
                    }
                    RecognitionWitHybrid.this.handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
                }
            }
        }).start();
    }

    public void audioShutdown() {
        if (this.DEBUG) {
            MyLog.v(CLS_NAME, "in audioShutdown");
        }
        synchronized (this.lock) {
            if (this.saiyRecorder != null) {
                this.ssp.play(this.ssp.getBeepStop());
                Recognition.setState(Recognition.State.IDLE);
                this.listener.onEndOfSpeech();
                this.saiyRecorder.shutdown(this.CLS_NAME);
                if (this.DEBUG) {
                    MyLog.d(CLS_NAME, "audioShutdown: audioSession set to NULL");
                }
                this.saiyRecorder = null;
            } else if (this.DEBUG) {
                MyLog.v(CLS_NAME, "audioShutdown: NULL");
            }
            if (this.DEBUG) {
                MyLog.v(CLS_NAME, "audioShutdown: finished synchronisation");
            }
        }
    }

    public void dispose() {
        if (this.outputStream != null) {
            try {
                this.outputStream.close();
            } catch (Exception e) {
                if (this.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            } catch (Exception e) {
                if (this.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        if (this.urlConnection != null) {
            try {
                this.urlConnection.disconnect();
            } catch (Exception e) {
                if (this.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stopListening() {
        if (this.DEBUG) {
            MyLog.i(CLS_NAME, "called stopListening");
        }
        this.isRecording.set(false);
        Recognition.setState(Recognition.State.IDLE);
    }

    @Override
    public void onSuccess(AmazonCredentials credentials) {
        if (this.DEBUG) {
            MyLog.i(CLS_NAME, "IAlexaToken: onSuccess");
        }
        this.alexaAccessToken = credentials.getAccessToken();
    }

    @Override
    public void onFailure(Exception exc) {
        if (this.DEBUG) {
            MyLog.i(CLS_NAME, "IAlexaToken: onFailure");
            exc.printStackTrace();
        }
        handleError(android.speech.SpeechRecognizer.ERROR_NETWORK);
    }

    public void startListening(boolean z) {
        if (this.DEBUG) {
            MyLog.i(CLS_NAME, "startListening");
        }
        this.isGoogleTTS = z && PermissionHelper.checkFilePermissions(this.mContext);
        if (this.alexaAccessToken == null) {
            if (this.DEBUG) {
                MyLog.i(CLS_NAME, "startListening: alexaAccessToken null");
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (RecognitionWitHybrid.this.alexaAccessToken == null) {
                        if (RecognitionWitHybrid.this.DEBUG) {
                            MyLog.e(CLS_NAME, "startListening: retry alexaAccessToken null");
                        }
                        RecognitionWitHybrid.this.handleError(android.speech.SpeechRecognizer.ERROR_NETWORK);
                    } else {
                        if (RecognitionWitHybrid.this.DEBUG) {
                            MyLog.e(CLS_NAME, "startListening: retry have alexaAccessToken");
                        }
                        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                        RecognitionWitHybrid.this.startRecording();
                    }
                }
            }, 3000L);
        } else {
            if (this.DEBUG) {
                MyLog.i(CLS_NAME, "startListening: have alexaAccessToken");
            }
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            startRecording();
        }
    }

    @Override
    public void onPauseDetected() {
        if (this.DEBUG) {
            MyLog.i(CLS_NAME, "onPauseDetected");
        }
        if (this.isRecording.get()) {
            stopListening();
        }
    }
}

package ai.saiy.android.recognition.provider.wit;

import android.content.Context;
import android.media.AudioRecord;
import android.net.ParseException;
import android.os.Bundle;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;

import ai.saiy.android.amazon.AmazonCredentials;
import ai.saiy.android.amazon.SpeechRecognizer;
import ai.saiy.android.amazon.TokenHelper;
import ai.saiy.android.amazon.UtilsNetwork;
import ai.saiy.android.amazon.listener.IAlexaToken;
import ai.saiy.android.amazon.listener.StreamListener;
import ai.saiy.android.amazon.resolve.DirectiveList;
import ai.saiy.android.amazon.resolve.ResolveAmazon;
import ai.saiy.android.api.SaiyDefaults;
import ai.saiy.android.api.language.vr.VRLanguageWit;
import ai.saiy.android.api.remote.Request;
import ai.saiy.android.audio.AudioParameters;
import ai.saiy.android.audio.SaiyRecorder;
import ai.saiy.android.audio.SaiySoundPool;
import ai.saiy.android.audio.pause.PauseDetector;
import ai.saiy.android.audio.pause.PauseListener;
import ai.saiy.android.configuration.WitConfiguration;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.recognition.SaiyRecognitionListener;
import ai.saiy.android.recognition.provider.amazon.VRLanguageAmazon;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import io.reactivex.rxjava3.schedulers.Schedulers;
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
    private volatile boolean alreadyThrownError;
    private final SaiySoundPool ssp;
    private PauseDetector pauseDetector;
    private final boolean servingRemote;
    private volatile SaiyRecorder saiyRecorder;
    private final Context mContext;
    private HttpsURLConnection urlConnection;
    private OutputStream outputStream;
    private InputStream inputStream;
    private final String apiKey;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = RecognitionWitHybrid.class.getSimpleName();

    private final AtomicBoolean isRecording = new AtomicBoolean();
    private final Object lock = new Object();
    private final Object errorLock = new Object();
    private RequestBody requestBody = new RequestBody() {
        @Override
        public MediaType contentType() {
            return MediaType.get(UtilsNetwork.OCTET_STREAM);
        }

        @Override
        public void writeTo(@NonNull BufferedSink sink) {
            try {
                final int bufferSize = saiyRecorder.getBufferSize();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "writeTo: bufferSize: " + bufferSize);
                }
                urlConnection = (HttpsURLConnection) new URL(WitConfiguration.WIT_SPEECH_URL).openConnection();
                urlConnection.setAllowUserInteraction(false);
                urlConnection.setInstanceFollowRedirects(true);
                urlConnection.setRequestMethod(Constants.HTTP_POST);
                urlConnection.setRequestProperty(UtilsNetwork.CONTENT_TYPE, HEADER_CONTENT_TYPE);
                urlConnection.setRequestProperty(AUTHORIZATION, BEARER_ + apiKey);
                urlConnection.setRequestProperty(JSON_HEADER_ACCEPT, "application/vnd.wit." + WitConfiguration.WIT_SPEECH_VERSION);
                urlConnection.setRequestProperty(N_HEADER, "5");
                urlConnection.setUseCaches(false);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty(TRANSFER_ENCODING, CHUNKED);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.connect();
                outputStream = urlConnection.getOutputStream();
                byte[] bytes = new byte[bufferSize];
                while (isRecording.get() && saiyRecorder != null && saiyRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    if (saiyRecorder != null) {
                        int count = saiyRecorder.read(bytes);
                        listener.onBufferReceived(bytes);
                        if (servingRemote && !pauseDetector.hasDetected()) {
                            pauseDetector.addLength(bytes, count);
                            pauseDetector.monitor();
                        }
                        sink.write(bytes);
                        for (int i = 0; i < count; i++) {
                            outputStream.write(bytes[i]);
                        }
                    }
                }
                final long witResponseTime = System.nanoTime();
                audioShutdown();
                int responseCode = urlConnection.getResponseCode();
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "responseCode: " + responseCode);
                    MyLog.getElapsed(CLS_NAME, "WitResponseTime", witResponseTime);
                }
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "audioThread ErrorStream: " + UtilsString.streamToString(urlConnection.getErrorStream()));
                    }
                    listener.onError(android.speech.SpeechRecognizer.ERROR_NETWORK);
                } else {
                    inputStream = urlConnection.getInputStream();
                    final ai.saiy.android.nlu.wit.NLUWit nluWit = new GsonBuilder().disableHtmlEscaping().create().fromJson(UtilsString.streamToString(inputStream), ai.saiy.android.nlu.wit.NLUWit.class);
                    final ArrayList<String> resultsArray = new ArrayList<>(1);
                    resultsArray.add(nluWit.getText());
                    final float[] confidenceArray = {nluWit.getConfidence()};
                    final Bundle results = new Bundle();
                    results.putStringArrayList(Request.RESULTS_RECOGNITION, resultsArray);
                    results.putFloatArray(Request.CONFIDENCE_SCORES, confidenceArray);
                    listener.onResults(results);
                }
            } catch (NullPointerException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "audioThread NullPointerException");
                    e.printStackTrace();
                }
                audioShutdown();
                handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
            } catch (MalformedURLException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "MalformedURLException");
                    e.printStackTrace();
                }
                audioShutdown();
                handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
            } catch (UnknownHostException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "UnknownHostException");
                    e.printStackTrace();
                }
                audioShutdown();
                handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
            } catch (IOException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "audioThread IOException");
                    e.printStackTrace();
                }
                audioShutdown();
                handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "audioThread IllegalStateException");
                    e.printStackTrace();
                }
                audioShutdown();
                handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
            } catch (ParseException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "ParseException");
                    e.printStackTrace();
                }
                audioShutdown();
                handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "audioThread Exception");
                    e.printStackTrace();
                }
                audioShutdown();
                handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
            } finally {
                closeConnection();
            }
            audioShutdown();
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
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
                TokenHelper.getAccessToken(context, RecognitionWitHybrid.this);
            }
        });
    }

    private void handleError(int error) {
        if (DEBUG) {
            MyLog.v(CLS_NAME, "in handleError");
        }
        synchronized (this.errorLock) {
            if (!this.alreadyThrownError) {
                Global.setAlexDirectiveBundle(null);
                Recognition.setState(Recognition.State.IDLE);
                this.alreadyThrownError = true;
                this.listener.onError(error);
            } else if (DEBUG) {
                MyLog.v(CLS_NAME, "handleError: thrown already: " + this.alreadyThrownError);
            }
        }
    }

    private void sendResults(DirectiveList directiveList) {
        if (directiveList.getErrorCode() != 0) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "sendResults: error: " + directiveList.getErrorCode());
            }
            Global.setAlexDirectiveBundle(null);
            return;
        }
        final Bundle results = new Bundle();
        if (directiveList.getFile() != null) {
            results.putString(SaiyRecognitionListener.ALEX_FILE, directiveList.getFile().getPath());
        }
        if (directiveList.hasDirectiveType()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "sendResults: hasDirectiveType: true" + directiveList.getDirectiveType().name());
            }
            results.putParcelable(SaiyRecognitionListener.ALEXA_DIRECTIVE, directiveList.getDirectiveType());
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "sendResults: hasDirectiveType: false");
        }
        results.putInt(LocalRequest.EXTRA_ACTION, directiveList.getAction());
        Global.setAlexDirectiveBundle(results);
    }

    private void startRecording() {
        this.isRecording.set(true);
        this.alreadyThrownError = false;
        if (this.servingRemote) {
            this.pauseDetector.begin();
        }
        this.ssp.play(this.ssp.getBeepStart());
        Recognition.setState(Recognition.State.LISTENING);
        this.listener.onReadyForSpeech(null);
        switch (saiyRecorder.initialise(mContext)) {
            case AudioRecord.STATE_UNINITIALIZED:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "AudioRecord.STATE_UNINITIALIZED");
                }
                handleError(android.speech.SpeechRecognizer.ERROR_AUDIO);
                return;
            case AudioRecord.STATE_INITIALIZED:
                switch (this.saiyRecorder.startRecording()) {
                    case AudioRecord.ERROR:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "startRecording: != AudioRecord.RECORDSTATE_RECORDING");
                        }
                        handleError(android.speech.SpeechRecognizer.ERROR_AUDIO);
                        break;
                    case AudioRecord.RECORDSTATE_RECORDING:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "startRecording: AudioRecord.RECORDSTATE_RECORDING");
                        }
                        sendAudio();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    private void sendAudio() {
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                try {
                    new SpeechRecognizer(requestBody).stream(UtilsNetwork.getUrl(mContext), alexaAccessToken, new StreamListener() {
                        @Override
                        public void onError(Exception e) {
                            if (DEBUG) {
                                MyLog.e(CLS_NAME, "onError: Exception");
                                e.printStackTrace();
                            }
                            handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
                        }

                        @Override
                        public void onSuccess(Call call) {
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
                                    handleError(android.speech.SpeechRecognizer.ERROR_NO_MATCH);
                                    return;
                                }
                                final DirectiveList directiveList = new ResolveAmazon(response.body().byteStream(), response, ai.saiy.android.utils.UtilsFile.getTempMp3File(mContext)).parse();
                                response.body().close();
                                Schedulers.io().scheduleDirect(new Runnable() {
                                    @Override
                                    public void run() {
                                        sendResults(directiveList);
                                    }
                                });
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onSuccess: end");
                                }
                            } catch (IOException e) {
                                if (DEBUG) {
                                    MyLog.e(CLS_NAME, "onSuccess: IOException");
                                    e.printStackTrace();
                                }
                                handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
                            } catch (JSONException e) {
                                if (DEBUG) {
                                    MyLog.e(CLS_NAME, "onSuccess: JSONException");
                                    e.printStackTrace();
                                }
                                handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
                            }
                        }
                    });
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "sendAudio: Exception");
                        e.printStackTrace();
                    }
                    handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
                }
            }
        });
    }

    public void audioShutdown() {
        if (DEBUG) {
            MyLog.v(CLS_NAME, "in audioShutdown");
        }
        synchronized (this.lock) {
            if (this.saiyRecorder != null) {
                this.ssp.play(this.ssp.getBeepStop());
                Recognition.setState(Recognition.State.IDLE);
                this.listener.onEndOfSpeech();
                this.saiyRecorder.shutdown(CLS_NAME);
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "audioShutdown: audioSession set to NULL");
                }
                this.saiyRecorder = null;
            } else if (DEBUG) {
                MyLog.v(CLS_NAME, "audioShutdown: NULL");
            }
            if (DEBUG) {
                MyLog.v(CLS_NAME, "audioShutdown: finished synchronisation");
            }
        }
    }

    public void closeConnection() {
        if (this.outputStream != null) {
            try {
                this.outputStream.close();
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        if (this.urlConnection != null) {
            try {
                this.urlConnection.disconnect();
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stopListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "called stopListening");
        }
        this.isRecording.set(false);
        Recognition.setState(Recognition.State.IDLE);
    }

    @Override
    public void onSuccess(AmazonCredentials credentials) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "IAlexaToken: onSuccess");
        }
        this.alexaAccessToken = credentials.getAccessToken();
    }

    @Override
    public void onFailure(Exception exc) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "IAlexaToken: onFailure");
            exc.printStackTrace();
        }
        handleError(android.speech.SpeechRecognizer.ERROR_NETWORK);
    }

    public void startListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startListening");
        }
        if (this.alexaAccessToken == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "startListening: alexaAccessToken null");
            }
            Schedulers.trampoline().scheduleDirect(new Runnable() {
                @Override
                public void run() {
                    if (alexaAccessToken == null) {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "startListening: retry alexaAccessToken null");
                        }
                        handleError(android.speech.SpeechRecognizer.ERROR_NETWORK);
                    } else {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "startListening: retry have alexaAccessToken");
                        }
                        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                        startRecording();
                    }
                }
            }, 3000L, TimeUnit.MILLISECONDS);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "startListening: have alexaAccessToken");
            }
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            startRecording();
        }
    }

    @Override
    public void onPauseDetected() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPauseDetected");
        }
        if (this.isRecording.get()) {
            stopListening();
        }
    }
}

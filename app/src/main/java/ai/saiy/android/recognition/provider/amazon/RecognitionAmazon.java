package ai.saiy.android.recognition.provider.amazon;

import android.content.Context;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Process;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.saiy.android.amazon.UtilsNetwork;
import ai.saiy.android.amazon.listener.IAlexaToken;
import ai.saiy.android.amazon.resolve.DirectiveList;
import ai.saiy.android.amazon.resolve.ResolveAmazon;
import ai.saiy.android.api.SaiyDefaults;
import ai.saiy.android.audio.SaiyRecorder;
import ai.saiy.android.audio.SaiySoundPool;
import ai.saiy.android.audio.pause.PauseDetector;
import ai.saiy.android.audio.pause.PauseListener;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.recognition.SaiyRecognitionListener;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class RecognitionAmazon implements IAlexaToken, PauseListener {
    private final SaiyRecognitionListener listener;
    private final SaiyDefaults.LanguageModel languageModel;
    private final Locale ttsLocale;
    private final VRLanguageAmazon vRLanguageAmazon;
    private final SupportedLanguage sl;
    private String alexaAccessToken;
    private volatile boolean alreadyThrownError;
    private final SaiySoundPool ssp;
    private PauseDetector pauseDetector;
    private final boolean servingRemote;
    private volatile SaiyRecorder saiyRecorder;
    private final Context mContext;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = RecognitionAmazon.class.getSimpleName();

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
                final byte[] bytes = new byte[bufferSize];
                boolean isRecordingStarted = false;
                while (isRecording.get() && saiyRecorder != null && saiyRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    if (!isRecordingStarted) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Recording Started");
                        }
                        ssp.play(ssp.getBeepStart());
                        Recognition.setState(Recognition.State.LISTENING);
                        listener.onReadyForSpeech(null);
                        isRecordingStarted = true;
                    }
                    if (saiyRecorder != null) {
                        final int count = saiyRecorder.read(bytes);
                        listener.onBufferReceived(bytes);
                        if (servingRemote && !pauseDetector.hasDetected()) {
                            pauseDetector.addLength(bytes, count);
                            pauseDetector.monitor();
                        }
                        sink.write(bytes);
                    }
                }
                audioShutdown();
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
            } catch (NullPointerException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "audioThread NullPointerException");
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
            }
        }
    };

    public RecognitionAmazon(final Context context, SaiyRecognitionListener listener, SaiyDefaults.LanguageModel languageModel, Locale locale, VRLanguageAmazon vRLanguageAmazon, SupportedLanguage supportedLanguage, SaiySoundPool ssp, boolean servingRemote) {
        this.mContext = context;
        this.listener = listener;
        this.languageModel = languageModel;
        this.ttsLocale = locale;
        this.vRLanguageAmazon = vRLanguageAmazon;
        this.sl = supportedLanguage;
        this.servingRemote = servingRemote;
        this.ssp = ssp;
        final ai.saiy.android.audio.AudioParameters audioParameters = ai.saiy.android.audio.AudioParameters.getDefault();
        if (servingRemote) {
            this.pauseDetector = new PauseDetector(this, audioParameters.getSampleRateInHz(), audioParameters.getnChannels(), 3250L);
        }
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
                ai.saiy.android.amazon.TokenHelper.getAccessToken(context, RecognitionAmazon.this);
            }
        });
        this.saiyRecorder = new SaiyRecorder(audioParameters.getAudioSource(),
                audioParameters.getSampleRateInHz(), audioParameters.getChannelConfig(),
                audioParameters.getAudioFormat(), true);
    }

    private void handleError(int error) {
        if (DEBUG) {
           MyLog.v(CLS_NAME, "in handleError");
        }
        synchronized (errorLock) {
            if (!alreadyThrownError) {
                Recognition.setState(Recognition.State.IDLE);
                this.alreadyThrownError = true;
                listener.onError(error);
            } else if (DEBUG) {
               MyLog.v(CLS_NAME, "handleError: thrown already: " + alreadyThrownError);
            }
        }
    }

    private void sendResults(@NonNull DirectiveList directiveList) {
        if (directiveList.getErrorCode() != 0) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "sendResults: error: " + directiveList.getErrorCode());
            }
            handleError(directiveList.getErrorCode());
            return;
        }
        Bundle bundle = new Bundle();
        if (directiveList.getFile() != null) {
            bundle.putString(SaiyRecognitionListener.ALEX_FILE, directiveList.getFile().getPath());
        }
        if (directiveList.hasDirectiveType()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "sendResults: hasDirectiveType: true" + directiveList.getDirectiveType().name());
            }
            bundle.putParcelable(SaiyRecognitionListener.ALEXA_DIRECTIVE, directiveList.getDirectiveType());
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "sendResults: hasDirectiveType: false");
        }
        bundle.putInt(LocalRequest.EXTRA_ACTION, directiveList.getAction());
        bundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, new ArrayList<>());
        bundle.putFloatArray(SpeechRecognizer.CONFIDENCE_SCORES, new float[1]);
        listener.onResults(bundle);
    }

    private void startRecording() {
        isRecording.set(true);
        this.alreadyThrownError = false;
        if (servingRemote) {
            pauseDetector.begin();
        }
        switch (saiyRecorder.initialise(mContext)) {
            case AudioRecord.STATE_UNINITIALIZED:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "AudioRecord.STATE_UNINITIALIZED");
                }
                handleError(android.speech.SpeechRecognizer.ERROR_AUDIO);
                break;
            case AudioRecord.STATE_INITIALIZED:
                switch (saiyRecorder.startRecording()) {
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
            default:
                break;
        }
    }

    private void sendAudio() {
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                try {
                    new ai.saiy.android.amazon.SpeechRecognizer(requestBody).stream(UtilsNetwork.getUrl(mContext), alexaAccessToken, new ai.saiy.android.amazon.listener.StreamListener() {
                        @Override
                        public void onError(Exception e) {
                            if (DEBUG) {
                                MyLog.e(CLS_NAME, "onError: Exception");
                                e.printStackTrace();
                            }
                            handleError(android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT);
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
                                    handleError(android.speech.SpeechRecognizer.ERROR_NO_MATCH);
                                    return;
                                }
                                DirectiveList directiveList = new ResolveAmazon(response.body().byteStream(), response, ai.saiy.android.utils.UtilsFile.getTempMp3File(mContext)).parse();
                                response.body().close();
                                sendResults(directiveList);
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

    private void audioShutdown() {
        if (DEBUG) {
           MyLog.v(CLS_NAME, "in audioShutdown");
        }
        synchronized (lock) {
            if (saiyRecorder != null) {
                ssp.play(ssp.getBeepStop());
                Recognition.setState(Recognition.State.IDLE);
                listener.onEndOfSpeech();
                saiyRecorder.shutdown(CLS_NAME);
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

    public void stopListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "called stopListening");
        }
        isRecording.set(false);
        Recognition.setState(Recognition.State.IDLE);
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
        handleError(android.speech.SpeechRecognizer.ERROR_NETWORK);
    }

    public void startListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startListening");
        }
        if (UtilsString.notNaked(alexaAccessToken)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "startListening: have accessToken");
            }
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            startRecording();
            return;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startListening: accessToken naked");
        }
        for (int i = 1; !UtilsString.notNaked(alexaAccessToken) && i < 7; i++) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "startListening: sleeping: " + i);
            }
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "startListening: sleeping : InterruptedException");
                    e.printStackTrace();
                }
            }
        }
        if (!UtilsString.notNaked(alexaAccessToken)) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "startListening: retry accessToken null");
            }
            handleError(android.speech.SpeechRecognizer.ERROR_NETWORK);
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "startListening: retry have accessToken");
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
        if (isRecording.get()) {
            stopListening();
        }
    }
}

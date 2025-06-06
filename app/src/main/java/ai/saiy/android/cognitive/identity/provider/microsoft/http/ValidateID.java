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

package ai.saiy.android.cognitive.identity.provider.microsoft.http;

import android.net.ParseException;
import android.os.Process;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import ai.saiy.android.audio.IMic;
import ai.saiy.android.audio.RecognitionMic;
import ai.saiy.android.cognitive.identity.provider.microsoft.Speaker;
import ai.saiy.android.cognitive.identity.provider.microsoft.containers.OperationStatus;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.ui.notification.NotificationHelper;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Created by benrandall76@gmail.com on 15/09/2016.
 */

public class ValidateID implements IMic {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ValidateID.class.getSimpleName();

    private static final String IDENTIFICATION_URL = "https://westus.api.cognitive.microsoft.com/spid/v1.0/identify?identificationProfileIds=";
    private static final String IDENTIFICATION_URL_EXTRA = "&shortAudio=";

    private static final String OCP_SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_AUDIO_PARAMS = "audio/l16; rate=16000";
    private static final int HTTP_ACCEPTED = 202;
    private static final String OPERATION_LOCATION = "Operation-Location";

    private static final long FETCH_DELAY = 6000;
    private static final long FETCH_DELAY_EXTENDED = 12000;

    private volatile HttpsURLConnection urlConnection;
    private volatile OutputStream outputStream;
    private FileInputStream fileInputStream;

    private volatile boolean retry = true;

    private final String apiKey;
    private final String profileId;
    private final RecognitionMic mic;
    private final SupportedLanguage sl;
    private final boolean shortAudio;
    private final File file;

    /**
     * Constructor
     *
     * @param mic       the initialised {@link RecognitionMic} object
     * @param sl        the {@link SupportedLanguage} object
     * @param apiKey    the api key
     * @param profileId of the user.
     */
    public ValidateID(@NonNull final RecognitionMic mic, @NonNull final SupportedLanguage sl,
                      @NonNull final String apiKey,
                      @NonNull final String profileId, final boolean shortAudio,
                      @NonNull final File file) {
        this.mic = mic;
        this.apiKey = apiKey;
        this.profileId = profileId;
        this.sl = sl;
        this.shortAudio = shortAudio;
        this.file = file;
    }

    /**
     * Start streaming the Microsoft enrollment servers
     */
    public void stream() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stream");
        }

        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

                try {

                    final String url = IDENTIFICATION_URL + URLEncoder.encode(profileId, Constants.ENCODING_UTF8)
                            + IDENTIFICATION_URL_EXTRA + shortAudio;

                    urlConnection = (HttpsURLConnection) new URL(url).openConnection();
                    urlConnection.setRequestMethod(Constants.HTTP_POST);
                    urlConnection.setRequestProperty(OCP_SUBSCRIPTION_KEY_HEADER, apiKey);
                    urlConnection.setRequestProperty(CONTENT_TYPE, CONTENT_TYPE_AUDIO_PARAMS);
                    urlConnection.setUseCaches(false);
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();

                    outputStream = urlConnection.getOutputStream();
                    fileInputStream = new FileInputStream(file);

                    final byte[] buffer = new byte[1024];

                    int len;
                    while ((len = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "requesting response");
                    }

                    final int responseCode = urlConnection.getResponseCode();

                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "responseCode: " + responseCode);
                    }

                    if (responseCode != HTTP_ACCEPTED) {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "ErrorStream: "
                                    + UtilsString.streamToString(urlConnection.getErrorStream()));
                        }

                        mic.getMicListener().onError(Speaker.ERROR_NETWORK);
                    } else {
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "response: HTTP_ACCEPTED");
                        }

                        final String statusUrl = urlConnection.getHeaderField(OPERATION_LOCATION);

                        if (UtilsString.notNaked(statusUrl)) {
                            if (DEBUG) {
                                MyLog.d(CLS_NAME, "response statusUrl: " + statusUrl);
                            }

                            Schedulers.computation().scheduleDirect(new Runnable() {
                                @Override
                                public void run() {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "timerTask: fetching operation status");
                                    }
                                    checkResult(new FetchIDOperation(mic.getContext(), apiKey, statusUrl)
                                            .getStatus(), statusUrl);
                                }
                            }, FETCH_DELAY, TimeUnit.MILLISECONDS);

                        } else {
                            mic.getMicListener().onError(Speaker.ERROR_NETWORK);
                        }
                    }
                } catch (final MalformedURLException e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "MalformedURLException");
                        e.printStackTrace();
                    }
                    mic.getMicListener().onError(Speaker.ERROR_NETWORK);
                } catch (final UnsupportedEncodingException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "UnsupportedEncodingException");
                        e.printStackTrace();
                    }
                    mic.getMicListener().onError(Speaker.ERROR_NETWORK);
                } catch (final ParseException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "ParseException");
                        e.printStackTrace();
                    }
                    mic.getMicListener().onError(Speaker.ERROR_NETWORK);
                } catch (final UnknownHostException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "UnknownHostException");
                        e.printStackTrace();
                    }
                    mic.getMicListener().onError(Speaker.ERROR_NETWORK);
                } catch (final IOException e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "IOException");
                        e.printStackTrace();
                    }
                    mic.getMicListener().onError(Speaker.ERROR_NETWORK);
                } catch (final IllegalStateException e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "IllegalStateException");
                        e.printStackTrace();
                    }
                    mic.getMicListener().onError(Speaker.ERROR_NETWORK);
                } catch (final NullPointerException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "NullPointerException");
                        e.printStackTrace();
                    }
                    mic.getMicListener().onError(Speaker.ERROR_NETWORK);
                } catch (final Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "Exception");
                        e.printStackTrace();
                    }
                    mic.getMicListener().onError(Speaker.ERROR_NETWORK);
                } finally {
                    closeConnection();
                }
            }
        });
    }

    /**
     * Check the result of the validation request
     *
     * @param statusPair the {@link Pair} with the first parameter denoting success and the
     *                   second the {@link OperationStatus} object
     * @param statusUrl  url to recheck the status if required
     */
    private void checkResult(@NonNull final Pair<Boolean, OperationStatus> statusPair,
                             @NonNull final String statusUrl) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkResult");
        }

        if (statusPair.first && statusPair.second != null && statusPair.second.getProcessingResult() != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "checkResult: have data");
            }

            switch (Speaker.Status.getStatus(statusPair.second.getStatus())) {

                case SUCCEEDED:
                    NotificationHelper.createIdentificationNotification(mic.getContext(),
                            Condition.CONDITION_IDENTIFY, true, Speaker.Confidence.getConfidence(
                                    statusPair.second.getProcessingResult().getConfidence()));
                    break;
                case FAILED:
                case UNDEFINED:
                    NotificationHelper.createIdentificationNotification(mic.getContext(),
                            Condition.CONDITION_IDENTIFY, true, Speaker.Confidence.ERROR);
                    break;
                case NOTSTARTED:
                case RUNNING:

                    if (retry) {
                        retry = false;

                        Schedulers.computation().scheduleDirect(new Runnable() {
                            @Override
                            public void run() {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "timerTask: fetching operation status");
                                }
                                checkResult(new FetchIDOperation(mic.getContext(), apiKey, statusUrl)
                                        .getStatus(), statusUrl);
                            }
                        }, FETCH_DELAY_EXTENDED, TimeUnit.MILLISECONDS);

                    } else {
                        NotificationHelper.createIdentificationNotification(mic.getContext(),
                                Condition.CONDITION_IDENTIFY, true, Speaker.Confidence.ERROR);
                    }

                    break;
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "statusPair false");
            }
            NotificationHelper.createIdentificationNotification(mic.getContext(),
                    Condition.CONDITION_IDENTIFY, true, Speaker.Confidence.ERROR);
        }
    }

    @Override
    public void onBufferReceived(final int bufferReadResult, final byte[] buffer) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBufferReceived");
        }
    }

    @Override
    public void onError(final int error) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onError");
        }
    }

    @Override
    public void onPauseDetected() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPauseDetected");
        }
    }

    @Override
    public void onRecordingStarted() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onRecordingStarted");
        }
    }

    @Override
    public void onRecordingEnded() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onRecordingEnded");
        }
    }

    @Override
    public void onFileWriteComplete(final boolean success) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onFileWriteComplete: " + success);
        }
    }

    private void closeConnection() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "closeConnection");
        }

        if (fileInputStream != null) {

            try {
                fileInputStream.close();
            } catch (final Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        if (outputStream != null) {

            try {
                outputStream.close();
            } catch (final Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        if (urlConnection != null) {

            try {
                urlConnection.disconnect();
            } catch (final Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }
}

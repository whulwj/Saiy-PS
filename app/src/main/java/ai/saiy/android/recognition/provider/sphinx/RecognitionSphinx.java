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

package ai.saiy.android.recognition.provider.sphinx;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.io.File;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.recognition.SaiyHotwordListener;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsFile;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.SpeechRecognizer;

/**
 * Created by benrandall76@gmail.com on 04/09/2016.
 */

public class RecognitionSphinx {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = RecognitionSphinx.class.getSimpleName();

    private static final String HOTWORD_SEARCH = "hotwords";
    private static final String HOTWORD_FILE = "hotwords.txt";
    private static final String ACOUSTIC_MODEL_EN = "en-us-ptm";
    private static final String DICTIONARY_FILE = "basic.dic";
    private static final String CONTEXT_INDEPENDENT = "-allphone_ci";
    private static final String VOICE_ACTIVATION_THRESHOLD = "-vad_threshold";

    private static final float VAD_THRESHOLD = 3.0f;

    private final Context mContext;
    private final SaiyHotwordListener listener;

    private final String dictionary;

    private volatile SpeechRecognizer recognizer;

    private static String dirPath = null;

    /**
     * @param mContext the application context
     * @param listener the {@link SaiyHotwordListener}
     * @param sl       the {@link SupportedLanguage}
     */
    @WorkerThread
    public RecognitionSphinx(@NonNull final Context mContext, @NonNull final SaiyHotwordListener listener,
                             @NonNull final SupportedLanguage sl) {
        this.mContext = mContext;
        this.listener = listener;

        // TODO - multiple language support

        switch (sl) {

            case ENGLISH:
            case ENGLISH_US:
                dictionary = DICTIONARY_FILE;
                break;
            default:
                dictionary = DICTIONARY_FILE;
                break;
        }

        setUp();
    }

    /**
     * Set up the recognizer. This is resource intensive.
     */
    private void setUp() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setUp");
        }

        final File destDirectory;
        if (dirPath == null) {
            destDirectory = UtilsFile.getPrivateDir(mContext);
            dirPath = (destDirectory == null)? null : destDirectory.getAbsolutePath();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "setUp: have path: " + dirPath);
            }
            destDirectory = new File(dirPath);
        }
        if (dirPath == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "setUp dirPath null");
            }
            onError(SaiyHotwordListener.ERROR_PERMISSIONS);
            return;
        }

        try {
            final File assetsDir = new Assets(mContext, destDirectory).syncAssets();

            recognizer = defaultSetup()
                    .setAcousticModel(new File(assetsDir, ACOUSTIC_MODEL_EN))
                    .setDictionary(new File(assetsDir, dictionary))
                    .setBoolean(CONTEXT_INDEPENDENT, true)
                    .setFloat(VOICE_ACTIVATION_THRESHOLD, VAD_THRESHOLD)
                    .getRecognizer();

            recognizer.addKeywordSearch(HOTWORD_SEARCH, new File(assetsDir, HOTWORD_FILE));
            recognizer.addListener(listener);

            listener.onHotwordInitialised(SPH.getHotwordOkayGoogle(mContext));

        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "setUp NullPointerException");
                e.printStackTrace();
            }
            onError(SaiyHotwordListener.ERROR_INITIALISE);
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "setUp Exception");
                e.printStackTrace();
            }
            onError(SaiyHotwordListener.ERROR_INITIALISE);
        }
    }

    /**
     * Start the recognition
     */
    public void startListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startListening");
        }

        if (recognizer != null) {
            Recognition.setState(Recognition.State.LISTENING);
            try {
                recognizer.startListening(HOTWORD_SEARCH);
                listener.onHotwordStarted();
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "startListening NullPointerException");
                    e.printStackTrace();
                }
                onError(SaiyHotwordListener.ERROR_NULL);
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "startListening Exception");
                    e.printStackTrace();
                }
                onError(SaiyHotwordListener.ERROR_NULL);
            }
        } else {
            onError(SaiyHotwordListener.ERROR_NULL);
        }
    }

    /**
     * Stop the recognition
     */
    public void stopListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stopListening");
        }

        Recognition.setState(Recognition.State.IDLE);

        if (recognizer != null) {

            try {
                recognizer.cancel();
                recognizer.shutdown();
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "stopListening Exception");
                    e.printStackTrace();
                }
            } finally {
                listener.onHotwordShutdown();
                recognizer = null;
            }
        }
    }

    /**
     * Report an error to the {@link SaiyHotwordListener}
     *
     * @param errorCode of the problem
     */
    private void onError(final int errorCode) {
        if (DEBUG) {
            MyLog.w(CLS_NAME, "onError");
        }

        listener.onHotwordError(errorCode);
        stopListening();
    }

    /**
     * Check if the hotword detection is currently active
     *
     * @return true if it's active, false otherwise
     */
    public boolean isListening() {
        return recognizer != null;
    }
}

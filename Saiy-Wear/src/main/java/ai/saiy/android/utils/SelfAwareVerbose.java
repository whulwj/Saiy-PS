package ai.saiy.android.utils;

import android.os.Bundle;
import android.speech.SpeechRecognizer;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * Helper class purely for logging verbose information. This will not be used in production, unless
 * logging is enabled when attempting to resolve a specific issue with a user.
 */
public final class SelfAwareVerbose {
    private static final String CLS_NAME = SelfAwareVerbose.class.getSimpleName();

    /**
     * Iterate through the recognition results and their associated confidence scores.
     *
     * @param bundle of recognition data
     */
    public static void logSpeechResults(final Bundle bundle) {
        if (!MyLog.DEBUG) {
            return;
        }
        MyLog.i(CLS_NAME, "logSpeechResults");

        examineBundle(bundle);

        final ArrayList<String> heardVoice = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        final float[] confidence = bundle.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

        if (heardVoice != null) {
            MyLog.d(CLS_NAME, "heardVoice: " + heardVoice.size());
        }
        if (confidence != null) {
            MyLog.d(CLS_NAME, "confidence: " + confidence.length);
        }

        /* handles empty string bug */
        if (heardVoice != null) {
            heardVoice.removeAll(Collections.singleton(""));
        }

        if (confidence != null && heardVoice != null && confidence.length == heardVoice.size()) {
            for (int i = 0; i < heardVoice.size(); i++) {
                MyLog.i(CLS_NAME, "Results: " + heardVoice.get(i) + " ~ " + confidence[i]);
            }
        } else if (heardVoice != null) {
            for (int i = 0; i < heardVoice.size(); i++) {
                MyLog.i(CLS_NAME, "Results: " + heardVoice.get(i));
            }
        } else {
            MyLog.w(CLS_NAME, "Results: values error");
        }
    }

    /**
     * For debugging the intent extras
     *
     * @param bundle containing potential extras
     */
    public static void examineBundle(@Nullable final Bundle bundle) {
        if (!MyLog.DEBUG) {
            return;
        }
        MyLog.i(CLS_NAME, "examineBundle");

        if (bundle != null) {
            final Set<String> keys = bundle.keySet();
            for (final String key : keys) {
                MyLog.v(CLS_NAME, "examineBundle: " + key + " ~ " + bundle.get(key));
            }
        }
    }
}

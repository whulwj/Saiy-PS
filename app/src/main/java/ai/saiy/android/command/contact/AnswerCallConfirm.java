package ai.saiy.android.command.contact;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.command.call.CallHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.local.PositiveNegative;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;

public class AnswerCallConfirm {
    private static Pattern pSpeaker;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = AnswerCallConfirm.class.getSimpleName();

    private final Context mContext;
    private final Bundle bundle;
    private final ConfirmType confirmType;
    private final Locale vrLocale;
    private final Locale ttsLocale;
    private final SupportedLanguage sl;
    private final ArrayList<String> resultsRecognition;

    public enum ConfirmType {
        ANSWER
    }

    public AnswerCallConfirm(Context context, Bundle bundle, ConfirmType confirmType, Locale vrLocale, Locale ttsLocale) {
        this.mContext = context;
        this.bundle = bundle;
        this.confirmType = confirmType;
        this.vrLocale = vrLocale;
        this.ttsLocale = ttsLocale;
        this.sl = (SupportedLanguage) bundle.getSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE);
        this.resultsRecognition = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        pSpeaker = Pattern.compile("\\b" + ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, this.sl, R.string.speaker) + "\\b");
    }

    private void toast(final String text) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    ai.saiy.android.utils.UtilsToast.showToast(mContext, text, Toast.LENGTH_LONG);
                } catch (RuntimeException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "toast: RuntimeException");
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void answer() {
        if (Objects.requireNonNull(confirmType) == ConfirmType.ANSWER) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "ANSWER");
            }
            final PositiveNegative positiveNegative = new PositiveNegative();
            final PositiveNegative.Result result = positiveNegative.resolve(mContext, resultsRecognition, sl);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "result: " + result.name());
                MyLog.i(CLS_NAME, "confidence: " + positiveNegative.getConfidence());
            }
            switch (result) {
                case POSITIVE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "POSITIVE");
                    }
                    final Locale locale = sl.getLocale();
                    boolean onSpeaker = false;
                    for (String s : resultsRecognition) {
                        if (pSpeaker.matcher(s.toLowerCase(locale)).find()) {
                            onSpeaker = true;
                        }
                    }
                    if (CallHelper.answerCall(mContext, onSpeaker)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "answerCall: success");
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "answerCall: failed");
                        }
                        toast(mContext.getString(R.string.error_answer_call));
                    }
                    break;
                case NEGATIVE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "NEGATIVE");
                    }
                    if (CallHelper.rejectCall(mContext)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "rejectCall: success");
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "rejectCall: failed");
                        }
                    }
                    break;
                case UNRESOLVED:
                case CANCEL:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "CANCEL/UNRESOLVED");
                    }
                    break;
                default:
                    break;
            }
        }
    }
}

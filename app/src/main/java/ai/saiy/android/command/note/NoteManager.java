package ai.saiy.android.command.note;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.defaults.notes.NoteProvider;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class NoteManager {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = NoteManager.class.getSimpleName();

    private final Context mContext;
    private final Bundle bundle;
    private final ContentType contentType;
    private final Locale vrLocale;
    private final Locale ttsLocale;
    private final SupportedLanguage sl;
    private final ArrayList<String> resultsRecognition;

    public enum ContentType implements Parcelable {
        NOTE_CONTENT,
        NOTE_SUBJECT;

        public static final Creator<ContentType> CREATOR = new Creator<ContentType>() {
            @Override
            public ContentType createFromParcel(Parcel in) {
                final byte index = in.readByte();
                for (NoteManager.ContentType contentType: NoteManager.ContentType.values()) {
                    if (index == contentType.ordinal()) {
                        return contentType;
                    }
                }
                return ContentType.NOTE_CONTENT;
            }

            @Override
            public ContentType[] newArray(int size) {
                return new ContentType[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeByte((byte) ordinal());
        }
    }

    public NoteManager(Context context, Bundle bundle, ContentType contentType, Locale vrLocale, Locale ttsLocale) {
        this.mContext = context;
        this.bundle = bundle;
        this.contentType = contentType;
        this.vrLocale = vrLocale;
        this.ttsLocale = ttsLocale;
        this.sl = (SupportedLanguage) bundle.getSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE);
        this.resultsRecognition = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
    }

    public void execute() {
        LocalRequest localRequest;
        NoteValues noteValues;
        switch (contentType) {
            case NOTE_CONTENT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "NOTE_CONTENT");
                }
                if (new ai.saiy.android.command.cancel.Cancel(sl, new ai.saiy.android.localisation.SaiyResources(mContext, sl)).detectCancel(resultsRecognition)) {
                    localRequest = new LocalRequest(mContext);
                    localRequest.prepareCancelled(sl, vrLocale, ttsLocale);
                    localRequest.execute();
                    break;
                }
                noteValues = bundle.getParcelable(LocalRequest.EXTRA_OBJECT);
                if (noteValues != null) {
                    noteValues.setContentType(ContentType.NOTE_SUBJECT);
                    noteValues.setNoteBody(UtilsString.convertProperCase(resultsRecognition.get(0), sl.getLocale()));
                }
                localRequest = new LocalRequest(mContext, bundle);
                localRequest.setParcelableObject(noteValues);
                localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_LISTEN, sl, vrLocale, ttsLocale, mContext.getString(R.string.note_title_request));
                localRequest.setCondition(Condition.CONDITION_NOTE);
                localRequest.execute();
                break;
            case NOTE_SUBJECT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "NOTE_SUBJECT");
                }
                if (new ai.saiy.android.command.cancel.Cancel(sl, new ai.saiy.android.localisation.SaiyResources(mContext, sl)).detectCancel(resultsRecognition)) {
                    localRequest = new LocalRequest(mContext);
                    localRequest.prepareCancelled(sl, vrLocale, ttsLocale);
                    localRequest.execute();
                    break;
                }
                localRequest = new LocalRequest(mContext, bundle);
                noteValues = bundle.getParcelable(LocalRequest.EXTRA_OBJECT);
                if (noteValues != null) {
                    noteValues.setNoteTitle(UtilsString.convertProperCase(resultsRecognition.get(0), sl.getLocale()));
                }
                String utterance;
                if (NoteProvider.publishNote(mContext, noteValues)) {
                    if (ai.saiy.android.utils.SPH.getNoteProviderVerbose(mContext)) {
                        utterance = mContext.getString(R.string.note_success_response);
                    } else {
                        ai.saiy.android.utils.SPH.markNoteProviderVerbose(mContext);
                        utterance = mContext.getString(R.string.note_success_response) + ". " + mContext.getString(R.string.content_note_provider_verbose);
                    }
                } else {
                    utterance = mContext.getString(R.string.note_error_response);
                }
                localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, utterance);
                localRequest.execute();
                break;
            default:
                break;
        }
    }
}

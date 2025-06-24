package ai.saiy.android.command.note;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.applications.Install;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.defaults.notes.NoteProvider;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;

public class CommandNote {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandNote.class.getSimpleName();
    private final Intent intent = new Intent();
    private long then;

    /**
     * A single point of return to check the elapsed time for debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private Outcome returnOutcome(Outcome outcome) {
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }

    private boolean setProvider(Context context, NoteProvider noteProvider) {
        switch (noteProvider) {
            case EVERNOTE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setProvider: " + NoteProvider.EVERNOTE.name());
                }
                final String string = context.getString(R.string.app_name_created);
                final ArrayList<String> arrayList = new ArrayList<>(1);
                arrayList.add(string);
                intent.setAction(NoteProvider.EVERNOTE_ACTION_NEW_VOICE_NOTE);
                intent.putExtra(Intent.EXTRA_TITLE, context.getString(R.string.title));
                intent.putExtra(Intent.EXTRA_TEXT, string);
                intent.putExtra(NoteProvider.EVERNOTE_EXTRA_TAGS, arrayList);
                intent.putExtra(NoteProvider.EVERNOTE_EXTRA_SOURCE_APP, context.getString(R.string.app_name));
                return true;
            case UNKNOWN:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setProvider: " + NoteProvider.UNKNOWN.name());
                }
                return false;
            default:
                return true;
        }
    }

    public @NonNull Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        outcome.setOutcome(Outcome.SUCCESS);
        outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        CommandNoteValues commandNoteValues;
        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
            commandNoteValues = (CommandNoteValues) cr.getVariableData();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            commandNoteValues = new Note(supportedLanguage).sort(context, voiceData);
        }
        if (commandNoteValues == null) {
            commandNoteValues = new CommandNoteValues();
            commandNoteValues.setType(CommandNoteValues.Type.NOTE);
        }
        if (commandNoteValues.getType() == CommandNoteValues.Type.VOICE_NOTE) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            final Pair<Boolean, Integer> providersCount = Installed.getVoiceNoteProvidersCount(context);
            if (providersCount.first) {
                intent.setAction(NoteProvider.VOICE_NOTE);
                if (providersCount.second > 0) {
                    final Pair<String, String> voiceNoteProviders = Installed.getVoiceNoteProviders(context);
                    if (!ExecuteIntent.executeIntent(context, intent)) {
                        outcome.setOutcome(Outcome.FAILURE);
                        final String applicationName;
                        if (voiceNoteProviders == null) {
                            final SaiyResources sr = new SaiyResources(context, supportedLanguage);
                            applicationName = sr.getString(R.string.note) + Constants.SEP_SPACE + sr.getString(R.string.voice);
                        } else {
                            applicationName = voiceNoteProviders.second;
                        }
                        outcome.setUtterance(PersonalityResponse.getDefaultAppFailed(context, supportedLanguage, applicationName));
                    } else {
                        outcome.setUtterance(SaiyRequestParams.SILENCE);
                    }
                } else {
                    outcome.setOutcome(Outcome.SUCCESS);
                    outcome.setUtterance(SaiyRequestParams.SILENCE);
                    ExecuteIntent.executeIntent(context, intent);
                }
            } else if (DEBUG) {
                MyLog.i(CLS_NAME, "no native providers");
            }
            final ArrayList<NoteProvider> evernote = Installed.getEvernote(context);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getVoiceNoteProviders: " + evernote.size());
            }
            if (evernote.isEmpty()) {
                outcome.setOutcome(Outcome.SUCCESS);
                outcome.setUtterance(PersonalityResponse.getSearchSuggestInstall(context, supportedLanguage, context.getString(R.string.evernote)));
                Install.showInstallLink(context, Installed.PACKAGE_EVERNOTE);
            } else {
                final NoteProvider noteProvider = evernote.get(0);
                setProvider(context, noteProvider);
                if (!ExecuteIntent.executeIntent(context, intent)) {
                    final String applicationName = NoteProvider.getApplicationName(context, supportedLanguage, noteProvider);
                    outcome.setOutcome(Outcome.FAILURE);
                    outcome.setUtterance(PersonalityResponse.getDefaultAppFailed(context, supportedLanguage, applicationName));
                } else {
                    outcome.setOutcome(Outcome.SUCCESS);
                    outcome.setUtterance(SaiyRequestParams.SILENCE);
                }
            }
            return returnOutcome(outcome);
        }

        if (NoteProvider.haveProviders(context)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "SELF_NOTE: have providers installed");
            }
            final NoteValues noteValues = new NoteValues();
            noteValues.setContentType(NoteManager.ContentType.NOTE_CONTENT);
            outcome.setUtterance(context.getString(R.string.note_content_request));
            outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
            outcome.setCondition(Condition.CONDITION_NOTE);
            outcome.setExtra(noteValues);
            outcome.setOutcome(Outcome.SUCCESS);
        } else {
            outcome.setOutcome(Outcome.FAILURE);
            outcome.setUtterance(PersonalityResponse.getSearchSuggestInstall(context, supportedLanguage, context.getString(R.string.evernote)));
            Install.showInstallLink(context, Installed.PACKAGE_EVERNOTE);
        }
        return returnOutcome(outcome);
    }
}

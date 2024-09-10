package ai.saiy.android.command.music;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class CommandMusic {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandMusic.class.getSimpleName();

    public @NonNull Outcome getResponse(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull SupportedLanguage supportedLanguage, @NonNull ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        final long then = System.nanoTime();
        final Outcome outcome = new Outcome();
        if (!cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            final CommandMusicValues commandMusicValues = new Music(supportedLanguage).sortMusic(context, voiceData);
            if (commandMusicValues == null || !UtilsString.notNaked(commandMusicValues.getQuery())) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "music data empty");
                }
                outcome.setUtterance(PersonalityResponse.getMusicUnknownError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
            } else if (UtilsMediaStore.playMusicFromSearch(context, commandMusicValues.getType(), commandMusicValues.getQuery())) {
                if (commandMusicValues.getType() == CommandMusicValues.Type.PLAYLIST && ai.saiy.android.utils.SPH.getPlaylistVerbose(context) <= 1) {
                    ai.saiy.android.utils.SPH.setPlaylistVerbose(context);
                    outcome.setUtterance(ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.playlist_verbose));
                } else if (commandMusicValues.getType() != CommandMusicValues.Type.RADIO || ai.saiy.android.utils.SPH.getRadioVerbose(context) > 1) {
                    outcome.setUtterance(SaiyRequestParams.SILENCE);
                } else {
                    ai.saiy.android.utils.SPH.setRadioVerbose(context);
                    outcome.setUtterance(ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.radio_verbose));
                }
                outcome.setOutcome(Outcome.SUCCESS);
            } else {
                outcome.setUtterance(PersonalityResponse.getNoAppError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "isResolved: true");
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }
}

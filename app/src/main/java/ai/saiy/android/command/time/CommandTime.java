package ai.saiy.android.command.time;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.command.time.online.WeatherOnlineTimeResponse;
import ai.saiy.android.command.time.online.model.TimeResponse;
import ai.saiy.android.command.weather.provider.online.WeatherOnlineHelper;
import ai.saiy.android.firebase.database.reference.WeatherOnlineReference;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

public class CommandTime {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandTime.class.getSimpleName();

    private long then;

    /**
     * A single point of return to check the elapsed time for debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private Outcome returnOutcome(@NonNull Outcome outcome) {
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }

    private String getUrl(@NonNull Context context, @NonNull String query) {
        final Pair<Boolean, String> authPair = new WeatherOnlineReference().getAPIKey(context);
        if (authPair.first) {
            return "https://api.worldweatheronline.com/premium/v1/tz.ashx?key=" + authPair.second + "&q=" + query.trim().replaceAll("\\s", "%20") + "&format=json";
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "authPair error");
        }
        return "https://api.worldweatheronline.com/premium/v1/tz.ashx?key=&q=" + query.trim().replaceAll("\\s", "%20") + "&format=json";
    }

    public @NonNull Outcome getResponse(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull SupportedLanguage supportedLanguage, @NonNull ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        final ArrayList<String> queries = new ArrayList<>();
        final ArrayList<String> urls = new ArrayList<>();
        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
            final CommandTimeValues commandTimeValues = (CommandTimeValues) cr.getVariableData();
            if (UtilsString.notNaked(commandTimeValues.getQuery())) {
                queries.add(commandTimeValues.getQuery());
                for (String place : queries) {
                    urls.add(getUrl(context, place));
                }
            } else {
                outcome.setOutcome(Outcome.SUCCESS);
                outcome.setUtterance(new TimeHelper().getTime(context));
                return returnOutcome(outcome);
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            queries.addAll(new Time(supportedLanguage).sort(context, voiceData));
            if (UtilsList.notNaked(queries)) {
                for (String place : queries) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "place: place");
                    }
                    urls.add(getUrl(context, place));
                }
            } else {
                outcome.setOutcome(Outcome.SUCCESS);
                outcome.setUtterance(new TimeHelper().getTime(context));
                return returnOutcome(outcome);
            }
        }
        if (UtilsList.notNaked(urls)) {
            final Pair<Boolean, TimeResponse> timeResponsePair = new WeatherOnlineHelper().getTimeResponse(urls);
            final WeatherOnlineTimeResponse onlineTimeResponse = timeResponsePair.first? WeatherOnlineTimeResponse.getResponse(timeResponsePair.second) : null;
            if (onlineTimeResponse != null) {
                final Pair<String, String> formattedSpokenTime = new TimeHelper().formatSpokenTimeIn(context, onlineTimeResponse.getTime());
                outcome.setUtterance(context.getString(R.string.in) + Constants.SEP_SPACE + onlineTimeResponse.getLocation() + ", " + context.getString(R.string.it_s) + Constants.SEP_SPACE + formattedSpokenTime.first + Constants.SEP_SPACE + context.getString(R.string.at) + Constants.SEP_SPACE + formattedSpokenTime.second);
                outcome.setOutcome(Outcome.SUCCESS);
                return returnOutcome(outcome);
            }
        }

        outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getTimeInError(context, supportedLanguage));
        outcome.setOutcome(Outcome.FAILURE);
        return returnOutcome(outcome);
    }
}

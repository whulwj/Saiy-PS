package ai.saiy.android.command.foursquare;

import android.content.Context;
import android.location.Location;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.List;

import ai.saiy.android.R;
import ai.saiy.android.algorithms.Algorithm;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.command.location.helper.FusedLocationHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.ActivityFoursquareOAuth;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

public class CommandFoursquare {
    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandFoursquare.class.getSimpleName();

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

    public Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        outcome.setCondition(Condition.CONDITION_NONE);
        outcome.setOutcome(Outcome.SUCCESS);
        outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        final String token = ai.saiy.android.utils.SPH.getFoursquareToken(context);
        if (!UtilsString.notNaked(token)) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "token naked");
            }
            ai.saiy.android.intent.ExecuteIntent.saiyActivity(context, ActivityFoursquareOAuth.class, null, true);
            outcome.setUtterance(ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.twitter_auth_request));
            outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        if (!ai.saiy.android.permissions.PermissionHelper.checkLocationPermissions(context, cr.getBundle())) {
            outcome.setUtterance(SaiyRequestParams.SILENCE);
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        ArrayList<String> voiceDataTrimmed = null;
        if (!cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            voiceDataTrimmed = new Foursquare(supportedLanguage).sort(context, voiceData);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "isResolved: true");
        }
        if (!UtilsList.notNaked(voiceDataTrimmed)) {
            FoursquareHelper.fourSquareCheckInPage(context);
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getLocationAccessError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }

        final FusedLocationHelper fusedLocationHelper = new FusedLocationHelper();
        fusedLocationHelper.prepare(context);
        fusedLocationHelper.connect();
        final Location location = fusedLocationHelper.getLastLocation();
        fusedLocationHelper.destroy();
        if (location == null) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getLocationAccessError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        final Pair<Boolean, VenuesResponse> venuesResponsePair = new FoursquareHelper().searchVenues(location, token);
        if (!venuesResponsePair.first || venuesResponsePair.second == null) {
            FoursquareHelper.fourSquareCheckInPage(context);
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getLocationAccessError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        final List<Venue> venues = venuesResponsePair.second.getResponse().getVenues();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "got check in venues");
            for (Venue venue : venues) {
                MyLog.d(CLS_NAME, "venue: " + venue.getName() + " ~ " + venue.getId());
            }
        }
        ArrayList<String> venueNames = new ArrayList<>(venues.size());
        for (Venue venue : venues) {
            venueNames.add(venue.getName());
        }
        final ai.saiy.android.nlu.local.AlgorithmicContainer algorithmicContainer = new ai.saiy.android.nlu.local.AlgorithmicResolver(context, new Algorithm[]{Algorithm.JARO_WINKLER, Algorithm.SOUNDEX, Algorithm.METAPHONE, Algorithm.DOUBLE_METAPHONE}, supportedLanguage.getLocale(), voiceDataTrimmed, venueNames, 500L, false).resolve();
        if (algorithmicContainer == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "failed to find a match");
            }
            FoursquareHelper.fourSquareCheckInPage(context);
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getFoursquareNearbyError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        final boolean isExactMatch = algorithmicContainer.isExactMatch();
        if (DEBUG) {
            MyLog.d(CLS_NAME, "container exactMatch: " + isExactMatch);
            MyLog.d(CLS_NAME, "container getInput: " + algorithmicContainer.getInput());
            MyLog.d(CLS_NAME, "container getGenericMatch: " + algorithmicContainer.getGenericMatch());
            MyLog.d(CLS_NAME, "container getAlgorithm: " + algorithmicContainer.getAlgorithm().name());
            MyLog.d(CLS_NAME, "container getScore: " + algorithmicContainer.getScore());
            MyLog.d(CLS_NAME, "container getParentPosition: " + algorithmicContainer.getParentPosition());
            MyLog.d(CLS_NAME, "container getVariableData: " + algorithmicContainer.getVariableData());
        }
        Venue venue = null;
        try {
            venue = venues.get(algorithmicContainer.getParentPosition());
        } catch (IndexOutOfBoundsException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "venueList IndexOutOfBoundsException");
                e.printStackTrace();
            }
        }
        if (venue == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "index out of bounds");
            }
            FoursquareHelper.fourSquareCheckInPage(context);
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getFoursquareNearbyError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "foursquareVenue name: " + venue.getName());
            MyLog.d(CLS_NAME, "foursquareVenue id: " + venue.getId());
        }
        final String distanceStr = isExactMatch ? "" : XMLResultsHandler.SEP_SPACE + ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.closest_match_extra);
        FoursquareHelper.fourSquareCheckIn(context, venue.getId());
        outcome.setUtterance(ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.i_found) + XMLResultsHandler.SEP_SPACE + distanceStr + XMLResultsHandler.SEP_SPACE + venue.getName() + XMLResultsHandler.SEP_SPACE + ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.nearby));
        outcome.setOutcome(Outcome.SUCCESS);
        return returnOutcome(outcome);
    }
}

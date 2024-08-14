package ai.saiy.android.command.search;

import android.content.Context;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.command.search.provider.AppSearchHelper;
import ai.saiy.android.command.search.provider.BingHelper;
import ai.saiy.android.command.search.provider.GoogleHelper;
import ai.saiy.android.command.search.provider.IMDbHelper;
import ai.saiy.android.command.search.provider.NetflixHelper;
import ai.saiy.android.command.search.provider.TwitterHelper;
import ai.saiy.android.command.search.provider.YahooHelper;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.localisation.SaiyWebHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class CommandSearch {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandSearch.class.getSimpleName();
    private static final String IMDB_QUERY_URL = "http://www.imdb.com/find?s=tt&q=";
    private static final String YOUTUBE_QUERY_URL = "http://www.youtube.com/results?search_query=";
    private static final String TWITTER_QUERY_URL = "http://twitter.com/search?q=";
    private static final String FACEBOOK_QUERY_URL = "https://www.facebook.com/search/top/?init=quick&q=";
    private static final String WOLFRAM_ALPHA_QUERY_URL = "https://www.wolframalpha.com/input/?i=";
    private static final String SPACE = "\\s"; //Regular expression of the space
    private static final String ESCAPED_SPACE = "%20";

    private long then;

    /**
     * A single point of return to check the elapsed time in debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private @NonNull Outcome returnOutcome(@NonNull Outcome outcome) {
        if (DEBUG) {
            MyLog.getElapsed(CommandSearch.class.getSimpleName(), then);
        }
        return outcome;
    }

    public Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        CommandSearchValues commandSearchValues;
        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
            commandSearchValues = (CommandSearchValues) cr.getVariableData();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            commandSearchValues = new Search(supportedLanguage).sortSearch(context, voiceData);
        }
        if (commandSearchValues == null || commandSearchValues.getType() == null || !UtilsString.notNaked(commandSearchValues.getQuery())) {
            outcome.setUtterance(PersonalityResponse.getSearchUnknownError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }

        String url;
        String description;
        switch (commandSearchValues.getType()) {
            case IMAGE:
            case GOOGLE:
            case WEB:
                if (ExecuteIntent.googleNow(context, commandSearchValues.getQuery().trim())) {
                    outcome.setUtterance(SaiyRequestParams.SILENCE);
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                String searchType = "";
                if (CommandSearchValues.Type.IMAGE == commandSearchValues.getType() || GoogleHelper.IMAGE == commandSearchValues.getGoogleType()) {
                    searchType = "&tbm=isch";
                    description = context.getString(R.string.images) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.of) + XMLResultsHandler.SEP_SPACE;
                } else if (GoogleHelper.VIDEO == commandSearchValues.getGoogleType()) {
                    searchType = "&tbm=vid";
                    description = context.getString(R.string.videos) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.of) + XMLResultsHandler.SEP_SPACE;
                } else {
                    description = "";
                }
                if (ExecuteIntent.webSearch(context, SaiyWebHelper.GOOGLE_URL_PREFIX + SaiyWebHelper.extension(SaiyWebHelper.GOOGLE, supportedLanguage) + "/search?q=" + commandSearchValues.getQuery().trim().replaceAll(SPACE, ESCAPED_SPACE) + searchType)) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.google), description + commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            case APPLICATION:
                if (!Installed.isPackageInstalled(context, Installed.PACKAGE_GOOGLE_STORE)) {
                    outcome.setUtterance(PersonalityResponse.getSearchRequireInstall(context, supportedLanguage, context.getString(R.string.google_play_store)));
                    outcome.setOutcome(Outcome.FAILURE);
                    return returnOutcome(outcome);
                }
                if (AppSearchHelper.search(context, commandSearchValues)) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.google_play_store), commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            case FILM:
                if (Installed.isPackageInstalled(context, Installed.PACKAGE_NETFLIX)) {
                    if (NetflixHelper.search(context, commandSearchValues)) {
                        outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.netflix), commandSearchValues.getQuery()));
                        outcome.setOutcome(Outcome.SUCCESS);
                        return returnOutcome(outcome);
                    }
                    ExecuteIntent.webSearch(context, IMDB_QUERY_URL + commandSearchValues.getQuery());
                    outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                    outcome.setOutcome(Outcome.FAILURE);
                    return returnOutcome(outcome);
                }
                if (!Installed.isPackageInstalled(context, Installed.PACKAGE_IMDB)) {
                    ExecuteIntent.webSearch(context, IMDB_QUERY_URL + commandSearchValues.getQuery());
                    outcome.setUtterance(PersonalityResponse.getSearchSuggestInstall(context, supportedLanguage, context.getString(R.string.netflix) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.or) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.imdb)));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                if (IMDbHelper.search(context, commandSearchValues)) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.imdb), commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                ExecuteIntent.webSearch(context, IMDB_QUERY_URL + commandSearchValues.getQuery());
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            case VIDEO:
            case YOUTUBE:
                if (!Installed.isPackageInstalled(context, Installed.PACKAGE_GOOGLE_YOUTUBE)) {
                    ExecuteIntent.webSearch(context, YOUTUBE_QUERY_URL + commandSearchValues.getQuery());
                    outcome.setUtterance(PersonalityResponse.getSearchRequireInstall(context, supportedLanguage, context.getString(R.string.youtube)));
                    outcome.setOutcome(Outcome.FAILURE);
                    return returnOutcome(outcome);
                }
                if (ExecuteIntent.searchYouTube(context, commandSearchValues)) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.youtube), commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                ExecuteIntent.webSearch(context, YOUTUBE_QUERY_URL + commandSearchValues.getQuery());
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            case SKY:
                if (!Installed.isPackageInstalled(context, Installed.PACKAGE_GOOGLE_SKY)) {
                    ExecuteIntent.playStoreSearch(context, Installed.PACKAGE_GOOGLE_SKY);
                    outcome.setUtterance(PersonalityResponse.getSearchRequireInstall(context, supportedLanguage, context.getString(R.string.google_sky_map)));
                    outcome.setOutcome(Outcome.FAILURE);
                    return returnOutcome(outcome);
                }
                if (ExecuteIntent.searchSky(context, commandSearchValues)) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.the) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.sky), commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            case EARTH:
                if (!Installed.isPackageInstalled(context, Installed.PACKAGE_GOOGLE_EARTH)) {
                    ExecuteIntent.playStoreSearch(context, Installed.PACKAGE_GOOGLE_EARTH);
                    outcome.setUtterance(PersonalityResponse.getSearchRequireInstall(context, supportedLanguage, context.getString(R.string.google_earth)));
                    outcome.setOutcome(Outcome.FAILURE);
                    return returnOutcome(outcome);
                }
                if (ExecuteIntent.searchEarth(context, commandSearchValues)) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.the) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.earth), commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            case NETFLIX:
                if (!Installed.isPackageInstalled(context, Installed.PACKAGE_NETFLIX)) {
                    ExecuteIntent.playStoreSearch(context, Installed.PACKAGE_NETFLIX);
                    outcome.setUtterance(PersonalityResponse.getSearchRequireInstall(context, supportedLanguage, context.getString(R.string.netflix)));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                if (NetflixHelper.search(context, commandSearchValues)) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.netflix), commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            case TWITTER:
                if (commandSearchValues.getTwitterType() == TwitterHelper.HASHTAG) {
                    commandSearchValues.setQuery("#" + commandSearchValues.getQuery());
                }
                if (!Installed.isPackageInstalled(context, Installed.PACKAGE_TWITTER)) {
                    ExecuteIntent.webSearch(context, TWITTER_QUERY_URL + commandSearchValues.getQuery().trim().replaceAll(SPACE, ESCAPED_SPACE));
                    outcome.setUtterance(PersonalityResponse.getSearchSuggestInstall(context, supportedLanguage, context.getString(R.string.twitter)));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                if (ExecuteIntent.searchTwitter(context, commandSearchValues)) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.twitter), commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                ExecuteIntent.webSearch(context, TWITTER_QUERY_URL + commandSearchValues.getQuery().trim().replaceAll(SPACE, ESCAPED_SPACE));
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            case FACEBOOK:
                if (!Installed.isPackageInstalled(context, Installed.PACKAGE_FACEBOOK)) {
                    ExecuteIntent.webSearch(context, FACEBOOK_QUERY_URL + commandSearchValues.getQuery().trim().replaceAll(SPACE, ESCAPED_SPACE));
                    outcome.setUtterance(PersonalityResponse.getSearchSuggestInstall(context, supportedLanguage, context.getString(R.string.facebook)));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                if (ExecuteIntent.searchFacebook(context, commandSearchValues)) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.facebook), commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                ExecuteIntent.webSearch(context, FACEBOOK_QUERY_URL + commandSearchValues.getQuery().trim().replaceAll(SPACE, ESCAPED_SPACE));
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            case FOURSQUARE:
                if (!Installed.isPackageInstalled(context, Installed.PACKAGE_FOUR_SQUARED)) {
                    ExecuteIntent.playStoreSearch(context, Installed.PACKAGE_FOUR_SQUARED);
                    outcome.setUtterance(PersonalityResponse.getSearchRequireInstall(context, supportedLanguage, context.getString(R.string.foursquare)));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                if (ExecuteIntent.searchFoursquare(context, commandSearchValues)) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.foursquare), commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            case IMDB:
                if (!Installed.isPackageInstalled(context, Installed.PACKAGE_IMDB)) {
                    ExecuteIntent.webSearch(context, IMDB_QUERY_URL + commandSearchValues.getQuery());
                    outcome.setUtterance(PersonalityResponse.getSearchSuggestInstall(context, supportedLanguage, context.getString(R.string.imdb)));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                if (IMDbHelper.search(context, commandSearchValues)) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.imdb), commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                ExecuteIntent.webSearch(context, IMDB_QUERY_URL + commandSearchValues.getQuery());
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            case WOLFRAM_ALPHA:
                if (!Installed.isPackageInstalled(context, Installed.PACKAGE_WOLFRAM_ALPHA)) {
                    ExecuteIntent.webSearch(context, WOLFRAM_ALPHA_QUERY_URL + commandSearchValues.getQuery().replaceAll(SPACE, ESCAPED_SPACE));
                    outcome.setUtterance(PersonalityResponse.getSearchSuggestInstall(context, supportedLanguage, context.getString(R.string.wolfram_alpha)));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                if (ExecuteIntent.wolframAlpha(context, commandSearchValues.getQuery())) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.wolfram_alpha), commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                ExecuteIntent.webSearch(context, WOLFRAM_ALPHA_QUERY_URL + commandSearchValues.getQuery().replaceAll(SPACE, ESCAPED_SPACE));
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            case EBAY:
                if (!Installed.isPackageInstalled(context, Installed.PACKAGE_EBAY)) {
                    ExecuteIntent.playStoreSearch(context, Installed.PACKAGE_EBAY);
                    outcome.setUtterance(PersonalityResponse.getSearchRequireInstall(context, supportedLanguage, context.getString(R.string.ebay)));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                if (ExecuteIntent.searchEbay(context, commandSearchValues)) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.ebay), commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            case YAHOO:
                switch (commandSearchValues.getYahooType()) {
                    case YahooHelper.IMAGE:
                        url = SaiyWebHelper.HTTP_PROTOCOL + SaiyWebHelper.yahooImage(supportedLanguage) + commandSearchValues.getQuery().trim().replaceAll(SPACE, ESCAPED_SPACE);
                        description = context.getString(R.string.images) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.of) + XMLResultsHandler.SEP_SPACE;
                        break;
                    case YahooHelper.VIDEO:
                        url = SaiyWebHelper.HTTP_PROTOCOL + SaiyWebHelper.yahooVideo(supportedLanguage) + commandSearchValues.getQuery().trim().replaceAll(SPACE, ESCAPED_SPACE);
                        description = context.getString(R.string.videos) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.of) + XMLResultsHandler.SEP_SPACE;
                        break;
                    default:
                        url = SaiyWebHelper.HTTP_PROTOCOL + SaiyWebHelper.extension(SaiyWebHelper.YAHOO, supportedLanguage) + "/search?q=" + commandSearchValues.getQuery().trim().replaceAll(SPACE, ESCAPED_SPACE);
                        description = "";
                        break;
                }
                if (ExecuteIntent.webSearch(context, url)) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.yahoo), description + commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            case BING:
                switch (commandSearchValues.getBingType()) {
                    case BingHelper.IMAGE:
                        url = SaiyWebHelper.HTTP_PROTOCOL + "bing.com/images/search?q=" + commandSearchValues.getQuery().trim().replaceAll(SPACE, ESCAPED_SPACE) + SaiyWebHelper.extension(SaiyWebHelper.BING, supportedLanguage);
                        description = context.getString(R.string.images) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.of) + XMLResultsHandler.SEP_SPACE;
                        break;
                    case BingHelper.VIDEO:
                        url = SaiyWebHelper.HTTP_PROTOCOL + "bing.com/videos/search?q=" + commandSearchValues.getQuery().trim().replaceAll(SPACE, ESCAPED_SPACE) + SaiyWebHelper.extension(SaiyWebHelper.BING, supportedLanguage);
                        description = context.getString(R.string.videos) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.of) + XMLResultsHandler.SEP_SPACE;
                        break;
                    default:
                        url = SaiyWebHelper.HTTP_PROTOCOL + "bing.com/search?q=" + commandSearchValues.getQuery().trim().replaceAll(SPACE, ESCAPED_SPACE) + SaiyWebHelper.extension(SaiyWebHelper.BING, supportedLanguage);
                        description = "";
                        break;
                }
                if (ExecuteIntent.webSearch(context, url)) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.bing), description + commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            case AMAZON:
                if (!Installed.amazonInstalled(context)) {
                    ExecuteIntent.playStoreSearch(context, Installed.PACKAGE_AMAZON);
                    outcome.setUtterance(PersonalityResponse.getSearchRequireInstall(context, supportedLanguage, context.getString(R.string.amazon)));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                if (ExecuteIntent.searchAmazon(context, commandSearchValues)) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.amazon), commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                outcome.setUtterance(ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.error_amazon_search));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            case YELP:
                if (!Installed.isPackageInstalled(context, Installed.PACKAGE_YELP)) {
                    ExecuteIntent.playStoreSearch(context, Installed.PACKAGE_YELP);
                    outcome.setUtterance(PersonalityResponse.getSearchRequireInstall(context, supportedLanguage, context.getString(R.string.yelp)));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                if (ExecuteIntent.searchYelp(context, commandSearchValues)) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.yelp), commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            default:
                if (ExecuteIntent.webSearch(context, SaiyWebHelper.GOOGLE_URL_PREFIX + SaiyWebHelper.extension(SaiyWebHelper.GOOGLE, supportedLanguage) + "/search?q=" + commandSearchValues.getQuery().trim().replaceAll(SPACE, ESCAPED_SPACE))) {
                    outcome.setUtterance(PersonalityResponse.getSearchConfirm(context, supportedLanguage, context.getString(R.string.google), commandSearchValues.getQuery()));
                    outcome.setOutcome(Outcome.SUCCESS);
                    return returnOutcome(outcome);
                }
                outcome.setUtterance(PersonalityResponse.getSearchError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
        }
    }
}

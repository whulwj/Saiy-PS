package ai.saiy.android.command.search;

import ai.saiy.android.command.search.provider.AppSearchHelper;
import ai.saiy.android.command.search.provider.BingHelper;
import ai.saiy.android.command.search.provider.GoogleHelper;
import ai.saiy.android.command.search.provider.IMDbHelper;
import ai.saiy.android.command.search.provider.NetflixHelper;
import ai.saiy.android.command.search.provider.TwitterHelper;
import ai.saiy.android.command.search.provider.YahooHelper;

public class CommandSearchValues {
    private static final int MASK_NETFLIX_TYPE = 0b11;
    private static final int MASK_TWITTER_TYPE = 0b100;
    private static final int MASK_IMDb_TYPE = 0b11000;
    private static final int MASK_GOOGLE_TYPE = 0b1100000;
    private static final int MASK_YAHOO_TYPE = 0b110000000;
    private static final int MASK_BING_TYPE = 0b11000000000;
    private static final int MASK_APP_SEARCH_TYPE = 0b111100000000000;

    private Type type;
    private String query;
    private int specificType;

    public enum Type {
        UNKNOWN,
        APPLICATION,
        FILM,
        VIDEO,
        IMAGE,
        SKY,
        EARTH,
        WEB,
        NETFLIX,
        TWITTER,
        FACEBOOK,
        FOURSQUARE,
        YOUTUBE,
        EBAY,
        GOOGLE,
        YAHOO,
        BING,
        AMAZON,
        YELP,
        IMDB,
        WOLFRAM_ALPHA
    }

    public String getQuery() {
        return this.query;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setAppSearchType(@AppSearchHelper.Type int type) {
        this.specificType |= (type & MASK_APP_SEARCH_TYPE);
    }

    public void setBingType(@BingHelper.Type int type) {
        this.specificType |= (type & MASK_BING_TYPE);
    }

    public void setGoogleType(@GoogleHelper.Type int type) {
        this.specificType |= (type & MASK_GOOGLE_TYPE);
    }

    public void setIMDbType(@IMDbHelper.Type int type) {
        this.specificType |= (type & MASK_IMDb_TYPE);
    }

    public void setNetflixType(@NetflixHelper.Type int type) {
        this.specificType |= (type & MASK_NETFLIX_TYPE);
    }

    public void setTwitterType(@TwitterHelper.Type int type) {
        this.specificType |= (type & MASK_TWITTER_TYPE);
    }

    public void setYahooType(@YahooHelper.Type int type) {
        this.specificType |= (type & MASK_YAHOO_TYPE);
    }

    public void setQuery(String str) {
        this.query = str;
    }

    public Type getType() {
        return this.type;
    }

    public @TwitterHelper.Type int getTwitterType() {
        if (TwitterHelper.HASHTAG == (specificType & MASK_TWITTER_TYPE)) {
            return TwitterHelper.HASHTAG;
        }
        return TwitterHelper.GENERIC;
    }

    public @GoogleHelper.Type int getGoogleType() {
        switch ((specificType & MASK_GOOGLE_TYPE)) {
            case GoogleHelper.VIDEO:
                return GoogleHelper.VIDEO;
            case GoogleHelper.IMAGE:
                return GoogleHelper.IMAGE;
            default:
                return GoogleHelper.GENERIC;
        }
    }

    public @BingHelper.Type int getBingType() {
        switch ((specificType & MASK_BING_TYPE)) {
            case BingHelper.VIDEO:
                return BingHelper.VIDEO;
            case BingHelper.IMAGE:
                return BingHelper.IMAGE;
            default:
                return BingHelper.GENERIC;
        }
    }

    public @YahooHelper.Type int getYahooType() {
        switch ((specificType & MASK_YAHOO_TYPE)) {
            case YahooHelper.VIDEO:
                return YahooHelper.VIDEO;
            case YahooHelper.IMAGE:
                return YahooHelper.IMAGE;
            default:
                return YahooHelper.GENERIC;
        }
    }

    public @AppSearchHelper.Type int getAppSearchType() {
        switch ((specificType & MASK_APP_SEARCH_TYPE)) {
            case AppSearchHelper.GROSSING:
                return AppSearchHelper.GROSSING;
            case AppSearchHelper.TRENDING:
                return AppSearchHelper.TRENDING;
            case AppSearchHelper.PAID:
                return AppSearchHelper.PAID;
            case AppSearchHelper.FREE:
                return AppSearchHelper.FREE;
            case AppSearchHelper.NEW_PAID:
                return AppSearchHelper.NEW_PAID;
            case AppSearchHelper.NEW_FREE:
                return AppSearchHelper.NEW_FREE;
            case AppSearchHelper.FEATURED:
                return AppSearchHelper.FEATURED;
            case AppSearchHelper.EDITOR:
                return AppSearchHelper.EDITOR;
            default:
                return AppSearchHelper.GENERIC;
        }
    }
}

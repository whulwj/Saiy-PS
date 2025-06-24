package ai.saiy.android.command.search;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.command.search.provider.AppSearchHelper;
import ai.saiy.android.command.search.provider.BingHelper;
import ai.saiy.android.command.search.provider.GoogleHelper;
import ai.saiy.android.command.search.provider.IMDbHelper;
import ai.saiy.android.command.search.provider.NetflixHelper;
import ai.saiy.android.command.search.provider.TwitterHelper;
import ai.saiy.android.command.search.provider.YahooHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public class Search_en {
    private static String application;
    private static String applications;
    private static String game;
    private static String featured;
    private static String editor;
    private static String choice;
    private static String top;
    private static String word_new;
    private static String free;
    private static String paid;
    private static String grossing;
    private static String trending;
    private static String play;
    private static String store;
    private static String pictures;
    private static String picture;
    private static String image;
    private static String images;
    private static String sky;
    private static String stars;
    private static String star;
    private static String universe;
    private static String planet;
    private static String constellation;
    private static String galaxy;
    private static String nebular;
    private static Pattern pHashTag;
    private static Pattern pFacebook;
    private static Pattern pFoursquare;
    private static Pattern pFourSquare;
    private static Pattern pForSquare;
    private static Pattern pYoutube;
    private static Pattern pEbay;
    private static Pattern pGoogle;
    private static Pattern pYahoo;
    private static Pattern pBing;
    private static Pattern pAmazon;
    private static Pattern pYelp;
    private static Pattern pImdb;
    private static Pattern pWolframAlpha;
    private static Pattern pApp;
    private static Pattern pApplication;
    private static Pattern pApplications;
    private static Pattern pGame;
    private static Pattern pGooglePlay;
    private static Pattern pPlayStore;
    private static Pattern pAppStore;
    private static Pattern pFeatured;
    private static Pattern pTop;
    private static Pattern pFree;
    private static Pattern pPaid;
    private static Pattern pGrossing;

    private static String sun;
    private static String moon;
    private static String earth;
    private static String ebay;
    private static String internet;
    private static String net;
    private static String web;
    private static String google;
    private static String yahoo;
    private static String bing;
    private static String twitter;
    private static String hashtag;
    private static String hash_tag;
    private static String facebook;
    private static String amazon;
    private static String yelp;
    private static String foursquare;
    private static String four_square;
    private static String for_square;
    private static String wolfram_alpha;
    private static Pattern pNetflix;
    private static Pattern pGenre;
    private static Pattern pActor;
    private static Pattern pActress;
    private static Pattern pTwitter;
    private static Pattern pHashtag;
    private static Pattern pTrending;
    private static Pattern pPictures;
    private static Pattern pPicture;
    private static Pattern pImage;
    private static Pattern pImages;
    private static Pattern pFilm;
    private static Pattern pMovie;
    private static Pattern pSky;
    private static Pattern pUniverse;
    private static Pattern pStars;
    private static Pattern pEarth;
    private static Pattern pInternet;
    private static Pattern pNet;
    private static Pattern pWeb;
    private static Pattern pVideos;
    private static Pattern pVideo;
    private static String search;
    private static String ask_google;
    private static String word_for;
    private static String the;
    private static String on;
    private static String in;
    private static String a;
    private static String of;
    private static String some;
    private static String an;
    private static String imdb;
    private static String netflix;
    private static String film;
    private static String movie;
    private static String actor;
    private static String actress;
    private static String genre;
    private static String youtube;
    private static String videos;
    private static String video;
    private static String app;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Search_en.class.getSimpleName();

    public Search_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (search == null || pYoutube == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static CommandSearchValues sortSearch(Context context, ArrayList<String> voiceData, ai.saiy.android.localisation.SupportedLanguage supportedLanguage) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sortSearch");
        }
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final CommandSearchValues commandSearchValues = new CommandSearchValues();
        commandSearchValues.setType(CommandSearchValues.Type.UNKNOWN);
        if (search == null || pYoutube == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        for (String voiceDatum : voiceData) {
            String trim = voiceDatum.toLowerCase(locale).trim();
            if (! (trim.startsWith(search) || trim.startsWith(ask_google))) {
                continue;
            }
            if (pNetflix.matcher(trim).matches()) {
                commandSearchValues.setType(CommandSearchValues.Type.NETFLIX);
                if (pGenre.matcher(trim).matches()) {
                    commandSearchValues.setNetflixType(NetflixHelper.GENRE);
                } else if (pActor.matcher(trim).matches() || pActress.matcher(trim).matches()) {
                    commandSearchValues.setNetflixType(NetflixHelper.ACTOR);
                } else {
                    commandSearchValues.setNetflixType(NetflixHelper.GENERIC);
                }
                commandSearchValues.setQuery(detectNetflixQuery(trim));
            } else if (pTwitter.matcher(trim).matches()) {
                commandSearchValues.setType(CommandSearchValues.Type.TWITTER);
                if (pHashtag.matcher(trim).matches() || pHashTag.matcher(trim).matches()) {
                    commandSearchValues.setTwitterType(TwitterHelper.HASHTAG);
                } else {
                    commandSearchValues.setTwitterType(TwitterHelper.GENERIC);
                }
                commandSearchValues.setQuery(detectTwitterQuery(trim));
            } else if (pFacebook.matcher(trim).matches()) {
                commandSearchValues.setType(CommandSearchValues.Type.FACEBOOK);
                commandSearchValues.setQuery(detectFacebookQuery(trim));
                break;
            } else if (pFoursquare.matcher(trim).matches() || pFourSquare.matcher(trim).matches() || pForSquare.matcher(trim).matches()) {
                commandSearchValues.setType(CommandSearchValues.Type.FOURSQUARE);
                commandSearchValues.setQuery(detectFourSquareQuery(trim));
            } else if (pYoutube.matcher(trim).matches()) {
                commandSearchValues.setType(CommandSearchValues.Type.YOUTUBE);
                commandSearchValues.setQuery(detectYoutubeQuery(trim));
                break;
            } else if (pEbay.matcher(trim).matches()) {
                commandSearchValues.setType(CommandSearchValues.Type.EBAY);
                commandSearchValues.setQuery(detectEbayQuery(trim));
                break;
            } else {
                if (pGoogle.matcher(trim).matches() && !pGooglePlay.matcher(trim).matches()) {
                    commandSearchValues.setType(CommandSearchValues.Type.GOOGLE);
                    commandSearchValues.setQuery(detectGoogleQuery(trim, commandSearchValues));
                    break;
                } else if (pYahoo.matcher(trim).matches()) {
                    commandSearchValues.setType(CommandSearchValues.Type.YAHOO);
                    commandSearchValues.setQuery(detectYahooQuery(trim, commandSearchValues));
                    break;
                } else if (pBing.matcher(trim).matches()) {
                    commandSearchValues.setType(CommandSearchValues.Type.BING);
                    commandSearchValues.setQuery(detectBingQuery(trim, commandSearchValues));
                    break;
                } else if (pAmazon.matcher(trim).matches()) {
                    commandSearchValues.setType(CommandSearchValues.Type.AMAZON);
                    commandSearchValues.setQuery(detectAmazonQuery(trim));
                    break;
                } else if (pYelp.matcher(trim).matches()) {
                    commandSearchValues.setType(CommandSearchValues.Type.YELP);
                    commandSearchValues.setQuery(detectYelpQuery(trim));
                    break;
                } else if (pWolframAlpha.matcher(trim).matches()) {
                    commandSearchValues.setType(CommandSearchValues.Type.WOLFRAM_ALPHA);
                    commandSearchValues.setQuery(detectWolframAlphaQuery(trim));
                    break;
                } else if (pImdb.matcher(trim).matches()) {
                    commandSearchValues.setType(CommandSearchValues.Type.IMDB);
                    if (pGenre.matcher(trim).matches()) {
                        commandSearchValues.setIMDbType(IMDbHelper.GENRE);
                    } else if (pActor.matcher(trim).matches() || pActress.matcher(trim).matches()) {
                        commandSearchValues.setIMDbType(IMDbHelper.ACTOR);
                    } else {
                        commandSearchValues.setIMDbType(IMDbHelper.GENERIC);
                    }
                    commandSearchValues.setQuery(detectImdbQuery(trim));
                } else if (pApp.matcher(trim).matches() || pApplication.matcher(trim).matches() || pApplications.matcher(trim).matches() || pGame.matcher(trim).matches() || pGooglePlay.matcher(trim).matches() || pPlayStore.matcher(trim).matches() || pAppStore.matcher(trim).matches()) {
                    commandSearchValues.setType(CommandSearchValues.Type.APPLICATION);
                    commandSearchValues.setQuery(detectAppQuery(trim, commandSearchValues));
                } else if (pFilm.matcher(trim).matches() || pMovie.matcher(trim).matches()) {
                    commandSearchValues.setType(CommandSearchValues.Type.FILM);
                    commandSearchValues.setQuery(detectFilmQuery(trim));
                } else if (pVideo.matcher(trim).matches() || pVideos.matcher(trim).matches()) {
                    commandSearchValues.setType(CommandSearchValues.Type.VIDEO);
                    commandSearchValues.setQuery(detectVideoQuery(trim));
                } else if (pImage.matcher(trim).matches() || pImages.matcher(trim).matches() || pPicture.matcher(trim).matches() || pPictures.matcher(trim).matches()) {
                    commandSearchValues.setType(CommandSearchValues.Type.IMAGE);
                    commandSearchValues.setQuery(detectImageQuery(trim));
                } else if (pSky.matcher(trim).matches() || pUniverse.matcher(trim).matches() || pStars.matcher(trim).matches()) {
                    commandSearchValues.setType(CommandSearchValues.Type.SKY);
                    commandSearchValues.setQuery(detectSkyQuery(trim));
                } else if (pEarth.matcher(trim).matches()) {
                    commandSearchValues.setType(CommandSearchValues.Type.EARTH);
                    commandSearchValues.setQuery(detectEarthQuery(trim));
                    break;
                } else if (pWeb.matcher(trim).matches() || pInternet.matcher(trim).matches() || pNet.matcher(trim).matches()) {
                    commandSearchValues.setType(CommandSearchValues.Type.WEB);
                    commandSearchValues.setQuery(detectWebQuery(trim));
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return commandSearchValues;
    }

    private static String detectNetflixQuery(String str) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(on + Constants.SEP_SPACE + netflix)) {
            trimmed = trimmed.replaceFirst(on + Constants.SEP_SPACE + netflix, "").trim();
        }
        if (trimmed.startsWith(netflix)) {
            trimmed = trimmed.replaceFirst(netflix, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(film)) {
            trimmed = trimmed.replaceFirst(film, "").trim();
        }
        if (trimmed.startsWith(movie)) {
            trimmed = trimmed.replaceFirst(movie, "").trim();
        }
        if (trimmed.startsWith(actor)) {
            trimmed = trimmed.replaceFirst(actor, "").trim();
        }
        if (trimmed.startsWith(actress)) {
            trimmed = trimmed.replaceFirst(actress, "").trim();
        }
        if (trimmed.startsWith(genre)) {
            trimmed = trimmed.replaceFirst(genre, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim();
        }
        return trimmed.endsWith(on + Constants.SEP_SPACE + netflix) ? ai.saiy.android.utils.UtilsString.replaceLast(trimmed, on + Constants.SEP_SPACE + netflix, "").trim() : trimmed;
    }

    private static String detectGoogleQuery(String str, CommandSearchValues commandSearchValues) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(ask_google)) {
            trimmed = trimmed.replaceFirst(ask_google, "").trim();
        }
        if (trimmed.startsWith(on + Constants.SEP_SPACE + google)) {
            trimmed = trimmed.replaceFirst(on + Constants.SEP_SPACE + google, "").trim();
        }
        if (trimmed.startsWith(google)) {
            trimmed = trimmed.replaceFirst(google, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + an + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + an + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + some + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + some + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(images + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(images + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setGoogleType(GoogleHelper.IMAGE);
        }
        if (trimmed.startsWith(image + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(image + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setGoogleType(GoogleHelper.IMAGE);
        }
        if (trimmed.startsWith(picture + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(picture + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setGoogleType(GoogleHelper.IMAGE);
        }
        if (trimmed.startsWith(pictures + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(pictures + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setGoogleType(GoogleHelper.IMAGE);
        }
        if (trimmed.startsWith(images + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(images + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setGoogleType(GoogleHelper.IMAGE);
        }
        if (trimmed.startsWith(image + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(image + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setGoogleType(GoogleHelper.IMAGE);
        }
        if (trimmed.startsWith(picture + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(picture + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setGoogleType(GoogleHelper.IMAGE);
        }
        if (trimmed.startsWith(pictures + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(pictures + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setGoogleType(GoogleHelper.IMAGE);
        }
        if (trimmed.startsWith(videos + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(videos + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setGoogleType(GoogleHelper.VIDEO);
        }
        if (trimmed.startsWith(video + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(video + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setGoogleType(GoogleHelper.VIDEO);
        }
        if (trimmed.startsWith(videos + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(videos + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setGoogleType(GoogleHelper.VIDEO);
        }
        if (trimmed.startsWith(video + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(video + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setGoogleType(GoogleHelper.VIDEO);
        }
        return trimmed.endsWith(on + Constants.SEP_SPACE + google) ? ai.saiy.android.utils.UtilsString.replaceLast(trimmed, on + Constants.SEP_SPACE + google, "").trim() : trimmed;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        search = sr.getString(R.string.search);
        ask_google = sr.getString(R.string.ask_google);
        word_for = sr.getString(R.string.word_for);
        a = sr.getString(R.string.a);
        the = sr.getString(R.string.the);
        on = sr.getString(R.string.on);
        in = sr.getString(R.string.in);
        of = sr.getString(R.string.of);
        some = sr.getString(R.string.some);
        an = sr.getString(R.string.an);
        netflix = sr.getString(R.string.netflix);
        imdb = sr.getString(R.string.imdb);
        film = sr.getString(R.string.film);
        movie = sr.getString(R.string.movie);
        actor = sr.getString(R.string.actor);
        actress = sr.getString(R.string.actress);
        genre = sr.getString(R.string.genre);
        youtube = sr.getString(R.string.youtube);
        videos = sr.getString(R.string.videos);
        video = sr.getString(R.string.video);
        app = sr.getString(R.string.app);
        application = sr.getString(R.string.application);
        applications = sr.getString(R.string.applications);
        game = sr.getString(R.string.game);
        featured = sr.getString(R.string.featured);
        editor = sr.getString(R.string.editor);
        choice = sr.getString(R.string.choice);
        top = sr.getString(R.string.top);
        word_new = sr.getString(R.string.word_new);
        free = sr.getString(R.string.free);
        paid = sr.getString(R.string.paid);
        grossing = sr.getString(R.string.grossing);
        trending = sr.getString(R.string.trending);
        pictures = sr.getString(R.string.pictures);
        picture = sr.getString(R.string.picture);
        image = sr.getString(R.string.image);
        images = sr.getString(R.string.images);
        sky = sr.getString(R.string.sky);
        stars = sr.getString(R.string.stars);
        star = sr.getString(R.string.star);
        universe = sr.getString(R.string.universe);
        planet = sr.getString(R.string.planet);
        constellation = sr.getString(R.string.constellation);
        galaxy = sr.getString(R.string.galaxy);
        nebular = sr.getString(R.string.nebular);
        sun = sr.getString(R.string.sun);
        moon = sr.getString(R.string.moon);
        earth = sr.getString(R.string.earth);
        ebay = sr.getString(R.string.ebay);
        internet = sr.getString(R.string.internet);
        net = sr.getString(R.string.net);
        web = sr.getString(R.string.web);
        google = sr.getString(R.string.google);
        yahoo = sr.getString(R.string.yahoo);
        bing = sr.getString(R.string.bing);
        twitter = sr.getString(R.string.twitter);
        hashtag = sr.getString(R.string.hashtag);
        hash_tag = sr.getString(R.string.hash_tag);
        facebook = sr.getString(R.string.facebook);
        amazon = sr.getString(R.string.amazon);
        yelp = sr.getString(R.string.yelp);
        foursquare = sr.getString(R.string.foursquare);
        four_square = sr.getString(R.string.four_square);
        for_square = sr.getString(R.string.for_square);
        wolfram_alpha = sr.getString(R.string.wolfram_alpha);
        play = sr.getString(R.string.play);
        store = sr.getString(R.string.store);
        pNetflix = Pattern.compile(".*\\b" + netflix + "\\b.*");
        pGenre = Pattern.compile(".*\\b" + genre + "\\b.*");
        pActor = Pattern.compile(".*\\b" + actor + "\\b.*");
        pActress = Pattern.compile(".*\\b" + actress + "\\b.*");
        pTwitter = Pattern.compile(".*\\b" + twitter + "\\b.*");
        pHashtag = Pattern.compile(".*\\b" + hashtag + "\\b.*");
        pHashTag = Pattern.compile(".*\\b" + hash_tag + "\\b.*");
        pFacebook = Pattern.compile(".*\\b" + facebook + "\\b.*");
        pFoursquare = Pattern.compile(".*\\b" + foursquare + "\\b.*");
        pFourSquare = Pattern.compile(".*\\b" + four_square + "\\b.*");
        pForSquare = Pattern.compile(".*\\b" + for_square + "\\b.*");
        pYoutube = Pattern.compile(".*\\b" + youtube + "\\b.*");
        pEbay = Pattern.compile(".*\\b" + ebay + "\\b.*");
        pYahoo = Pattern.compile(".*\\b" + yahoo + "\\b.*");
        pBing = Pattern.compile(".*\\b" + bing + "\\b.*");
        pGoogle = Pattern.compile(".*\\b" + google + "\\b.*");
        pYelp = Pattern.compile(".*\\b" + yelp + "\\b.*");
        pImdb = Pattern.compile(".*\\b" + imdb + "\\b.*");
        pWolframAlpha = Pattern.compile(".*\\b" + wolfram_alpha + "\\b.*");
        pAmazon = Pattern.compile(".*\\b" + amazon + "\\b.*");
        pApp = Pattern.compile(".*\\b" + app + "\\b.*");
        pApplication = Pattern.compile(".*\\b" + application + "\\b.*");
        pApplications = Pattern.compile(".*\\b" + applications + "\\b.*");
        pGame = Pattern.compile(".*\\b" + game + "\\b.*");
        pFeatured = Pattern.compile(".*\\b" + featured + "\\b.*");
        pTop = Pattern.compile(".*\\b" + top + "\\b.*");
        pFree = Pattern.compile(".*\\b" + free + "\\b.*");
        pPaid = Pattern.compile(".*\\b" + paid + "\\b.*");
        pGrossing = Pattern.compile(".*\\b" + grossing + "\\b.*");
        pTrending = Pattern.compile(".*\\b" + trending + "\\b.*");
        pGooglePlay = Pattern.compile(".*\\b" + google + Constants.SEP_SPACE + play + "\\b.*");
        pPlayStore = Pattern.compile(".*\\b" + play + Constants.SEP_SPACE + store + "\\b.*");
        pAppStore = Pattern.compile(".*\\b" + app + Constants.SEP_SPACE + store + "\\b.*");
        pPictures = Pattern.compile(".*\\b" + pictures + "\\b.*");
        pPicture = Pattern.compile(".*\\b" + picture + "\\b.*");
        pImage = Pattern.compile(".*\\b" + image + "\\b.*");
        pImages = Pattern.compile(".*\\b" + images + "\\b.*");
        pFilm = Pattern.compile(".*\\b" + film + "\\b.*");
        pMovie = Pattern.compile(".*\\b" + movie + "\\b.*");
        pSky = Pattern.compile(".*\\b" + sky + "\\b.*");
        pUniverse = Pattern.compile(".*\\b" + universe + "\\b.*");
        pStars = Pattern.compile(".*\\b" + stars + "\\b.*");
        pEarth = Pattern.compile(".*\\b" + earth + "\\b.*");
        pInternet = Pattern.compile(".*\\b" + internet + "\\b.*");
        pNet = Pattern.compile(".*\\b" + net + "\\b.*");
        pWeb = Pattern.compile(".*\\b" + web + "\\b.*");
        pVideo = Pattern.compile(".*\\b" + video + "\\b.*");
        pVideos = Pattern.compile(".*\\b" + videos + "\\b.*");
    }

    private static String detectTwitterQuery(String str) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(on + Constants.SEP_SPACE + twitter)) {
            trimmed = trimmed.replaceFirst(on + Constants.SEP_SPACE + twitter, "").trim();
        }
        if (trimmed.startsWith(twitter)) {
            trimmed = trimmed.replaceFirst(twitter, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(hashtag)) {
            trimmed = trimmed.replaceFirst(hashtag, "").trim();
        }
        if (trimmed.startsWith(hash_tag)) {
            trimmed = trimmed.replaceFirst(hash_tag, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim();
        }
        return trimmed.endsWith(on + Constants.SEP_SPACE + twitter) ? ai.saiy.android.utils.UtilsString.replaceLast(trimmed, on + Constants.SEP_SPACE + twitter, "").trim() : trimmed;
    }

    private static String detectYahooQuery(String str, CommandSearchValues commandSearchValues) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(on + Constants.SEP_SPACE + yahoo)) {
            trimmed = trimmed.replaceFirst(on + Constants.SEP_SPACE + yahoo, "").trim();
        }
        if (trimmed.startsWith(yahoo)) {
            trimmed = trimmed.replaceFirst(yahoo, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + an + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + an + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + some + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + some + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(images + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(images + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setYahooType(YahooHelper.IMAGE);
        }
        if (trimmed.startsWith(image + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(image + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setYahooType(YahooHelper.IMAGE);
        }
        if (trimmed.startsWith(picture + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(picture + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setYahooType(YahooHelper.IMAGE);
        }
        if (trimmed.startsWith(pictures + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(pictures + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setYahooType(YahooHelper.IMAGE);
        }
        if (trimmed.startsWith(images + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(images + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setYahooType(YahooHelper.IMAGE);
        }
        if (trimmed.startsWith(image + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(image + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setYahooType(YahooHelper.IMAGE);
        }
        if (trimmed.startsWith(picture + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(picture + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setYahooType(YahooHelper.IMAGE);
        }
        if (trimmed.startsWith(pictures + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(pictures + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setYahooType(YahooHelper.IMAGE);
        }
        if (trimmed.startsWith(videos + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(videos + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setYahooType(YahooHelper.VIDEO);
        }
        if (trimmed.startsWith(video + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(video + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setYahooType(YahooHelper.VIDEO);
        }
        if (trimmed.startsWith(videos + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(videos + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setYahooType(YahooHelper.VIDEO);
        }
        if (trimmed.startsWith(video + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(video + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setYahooType(YahooHelper.VIDEO);
        }
        return trimmed.endsWith(on + Constants.SEP_SPACE + yahoo) ? ai.saiy.android.utils.UtilsString.replaceLast(trimmed, on + Constants.SEP_SPACE + yahoo, "").trim() : trimmed;
    }

    private static String detectFacebookQuery(String str) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(on + Constants.SEP_SPACE + facebook)) {
            trimmed = trimmed.replaceFirst(on + Constants.SEP_SPACE + facebook, "").trim();
        }
        if (trimmed.startsWith(facebook)) {
            trimmed = trimmed.replaceFirst(facebook, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim();
        }
        return trimmed.endsWith(on + Constants.SEP_SPACE + facebook) ? ai.saiy.android.utils.UtilsString.replaceLast(trimmed, on + Constants.SEP_SPACE + facebook, "").trim() : trimmed;
    }

    private static String detectBingQuery(String str, CommandSearchValues commandSearchValues) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(on + Constants.SEP_SPACE + bing)) {
            trimmed = trimmed.replaceFirst(on + Constants.SEP_SPACE + bing, "").trim();
        }
        if (trimmed.startsWith(bing)) {
            trimmed = trimmed.replaceFirst(bing, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + an + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + an + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + some + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + some + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(images + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(images + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setBingType(BingHelper.IMAGE);
        }
        if (trimmed.startsWith(image + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(image + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setBingType(BingHelper.IMAGE);
        }
        if (trimmed.startsWith(picture + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(picture + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setBingType(BingHelper.IMAGE);
        }
        if (trimmed.startsWith(pictures + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(pictures + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setBingType(BingHelper.IMAGE);
        }
        if (trimmed.startsWith(images + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(images + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setBingType(BingHelper.IMAGE);
        }
        if (trimmed.startsWith(image + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(image + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setBingType(BingHelper.IMAGE);
        }
        if (trimmed.startsWith(picture + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(picture + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setBingType(BingHelper.IMAGE);
        }
        if (trimmed.startsWith(pictures + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(pictures + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setBingType(BingHelper.IMAGE);
        }
        if (trimmed.startsWith(videos + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(videos + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setBingType(BingHelper.VIDEO);
        }
        if (trimmed.startsWith(video + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(video + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setBingType(BingHelper.VIDEO);
        }
        if (trimmed.startsWith(videos + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(videos + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setBingType(BingHelper.VIDEO);
        }
        if (trimmed.startsWith(video + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(video + Constants.SEP_SPACE, "").trim();
            commandSearchValues.setBingType(BingHelper.VIDEO);
        }
        return trimmed.endsWith(on + Constants.SEP_SPACE + bing) ? ai.saiy.android.utils.UtilsString.replaceLast(trimmed, on + Constants.SEP_SPACE + bing, "").trim() : trimmed;
    }

    private static String detectFourSquareQuery(String str) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(on + Constants.SEP_SPACE + foursquare)) {
            trimmed = trimmed.replaceFirst(on + Constants.SEP_SPACE + foursquare, "").trim();
        }
        if (trimmed.startsWith(on + Constants.SEP_SPACE + four_square)) {
            trimmed = trimmed.replaceFirst(on + Constants.SEP_SPACE + four_square, "").trim();
        }
        if (trimmed.startsWith(on + Constants.SEP_SPACE + for_square)) {
            trimmed = trimmed.replaceFirst(on + Constants.SEP_SPACE + for_square, "").trim();
        }
        if (trimmed.startsWith(foursquare)) {
            trimmed = trimmed.replaceFirst(foursquare, "").trim();
        }
        if (trimmed.startsWith(four_square)) {
            trimmed = trimmed.replaceFirst(four_square, "").trim();
        }
        if (trimmed.startsWith(for_square)) {
            trimmed = trimmed.replaceFirst(for_square, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.endsWith(on + Constants.SEP_SPACE + four_square)) {
            trimmed = ai.saiy.android.utils.UtilsString.replaceLast(trimmed, on + Constants.SEP_SPACE + four_square, "").trim();
        }
        return trimmed.endsWith(on + Constants.SEP_SPACE + foursquare) ? ai.saiy.android.utils.UtilsString.replaceLast(trimmed, on + Constants.SEP_SPACE + foursquare, "").trim() : trimmed;
    }

    private static String detectAppQuery(String str, CommandSearchValues commandSearchValues) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(on + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(on + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(on + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(google + Constants.SEP_SPACE + play + Constants.SEP_SPACE + store + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(google + Constants.SEP_SPACE + play + Constants.SEP_SPACE + store + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(google + Constants.SEP_SPACE + play + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(google + Constants.SEP_SPACE + play + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(play + Constants.SEP_SPACE + store + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(play + Constants.SEP_SPACE + store + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(app + Constants.SEP_SPACE + store + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(app + Constants.SEP_SPACE + store + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(app + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(app + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(application + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(application + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(applications + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(applications + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(game + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(game + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.contains(editor) && trimmed.contains(choice)) {
            commandSearchValues.setAppSearchType(AppSearchHelper.EDITOR);
            commandSearchValues.setQuery(editor + "'s " + choice + Constants.SEP_SPACE + applications);
            return trimmed;
        }
        if (pFeatured.matcher(trimmed).matches()) {
            commandSearchValues.setAppSearchType(AppSearchHelper.FEATURED);
            commandSearchValues.setQuery(featured + Constants.SEP_SPACE + applications);
            return trimmed;
        }
        if (pTrending.matcher(trimmed).matches()) {
            commandSearchValues.setAppSearchType(AppSearchHelper.TRENDING);
            commandSearchValues.setQuery(trending + Constants.SEP_SPACE + applications);
            return trimmed;
        }
        if (pTop.matcher(trimmed).matches()) {
            if (trimmed.matches(".*\\b" + word_new + Constants.SEP_SPACE + free + "\\b.*")) {
                commandSearchValues.setAppSearchType(AppSearchHelper.NEW_FREE);
                commandSearchValues.setQuery(top + Constants.SEP_SPACE + word_new + Constants.SEP_SPACE + free + Constants.SEP_SPACE + applications);
                return trimmed;
            }
            if (trimmed.matches(".*\\b" + word_new + Constants.SEP_SPACE + paid + "\\b.*")) {
                commandSearchValues.setAppSearchType(AppSearchHelper.NEW_PAID);
                commandSearchValues.setQuery(top + Constants.SEP_SPACE + word_new + Constants.SEP_SPACE + paid + Constants.SEP_SPACE + applications);
                return trimmed;
            }
            if (pFree.matcher(trimmed).matches()) {
                commandSearchValues.setAppSearchType(AppSearchHelper.FREE);
                commandSearchValues.setQuery(top + Constants.SEP_SPACE + free + Constants.SEP_SPACE + applications);
                return trimmed;
            }
            if (pPaid.matcher(trimmed).matches()) {
                commandSearchValues.setAppSearchType(AppSearchHelper.PAID);
                commandSearchValues.setQuery(top + Constants.SEP_SPACE + paid + Constants.SEP_SPACE + applications);
                return trimmed;
            }
            if (pGrossing.matcher(trimmed).matches()) {
                commandSearchValues.setAppSearchType(AppSearchHelper.GROSSING);
                commandSearchValues.setQuery(top + Constants.SEP_SPACE + grossing + Constants.SEP_SPACE + applications);
                return trimmed;
            }
        }
        return trimmed.startsWith(word_for + Constants.SEP_SPACE) ? trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim() : trimmed;
    }

    private static String detectYoutubeQuery(String str) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(on + Constants.SEP_SPACE + youtube)) {
            trimmed = trimmed.replaceFirst(on + Constants.SEP_SPACE + youtube, "").trim();
        }
        if (trimmed.startsWith(youtube)) {
            trimmed = trimmed.replaceFirst(youtube, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(video + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(video + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(videos + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(videos + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(video)) {
            trimmed = trimmed.replaceFirst(video, "").trim();
        }
        if (trimmed.startsWith(videos)) {
            trimmed = trimmed.replaceFirst(videos, "").trim();
        }
        return trimmed.endsWith(on + Constants.SEP_SPACE + youtube) ? ai.saiy.android.utils.UtilsString.replaceLast(trimmed, on + Constants.SEP_SPACE + youtube, "").trim() : trimmed;
    }

    private static String detectEbayQuery(String str) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(on + Constants.SEP_SPACE + ebay)) {
            trimmed = trimmed.replaceFirst(on + Constants.SEP_SPACE + ebay, "").trim();
        }
        if (trimmed.startsWith(ebay)) {
            trimmed = trimmed.replaceFirst(ebay, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + some + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + some + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim();
        }
        return trimmed.endsWith(on + Constants.SEP_SPACE + ebay) ? ai.saiy.android.utils.UtilsString.replaceLast(trimmed, on + Constants.SEP_SPACE + ebay, "").trim() : trimmed;
    }

    private static String detectAmazonQuery(String str) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(on + Constants.SEP_SPACE + amazon)) {
            trimmed = trimmed.replaceFirst(on + Constants.SEP_SPACE + amazon, "").trim();
        }
        if (trimmed.startsWith(amazon)) {
            trimmed = trimmed.replaceFirst(amazon, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + some + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + some + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim();
        }
        return trimmed.endsWith(on + Constants.SEP_SPACE + amazon) ? ai.saiy.android.utils.UtilsString.replaceLast(trimmed, on + Constants.SEP_SPACE + amazon, "").trim() : trimmed;
    }

    private static String detectYelpQuery(String str) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(on + Constants.SEP_SPACE + yelp)) {
            trimmed = trimmed.replaceFirst(on + Constants.SEP_SPACE + yelp, "").trim();
        }
        if (trimmed.startsWith(yelp)) {
            trimmed = trimmed.replaceFirst(yelp, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + some + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + some + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim();
        }
        return trimmed.endsWith(on + Constants.SEP_SPACE + yelp) ? ai.saiy.android.utils.UtilsString.replaceLast(trimmed, on + Constants.SEP_SPACE + yelp, "").trim() : trimmed;
    }

    private static String detectWolframAlphaQuery(String str) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(on + Constants.SEP_SPACE + wolfram_alpha)) {
            trimmed = trimmed.replaceFirst(on + Constants.SEP_SPACE + wolfram_alpha, "").trim();
        }
        if (trimmed.startsWith(wolfram_alpha)) {
            trimmed = trimmed.replaceFirst(wolfram_alpha, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim();
        }
        return trimmed.endsWith(on + Constants.SEP_SPACE + wolfram_alpha) ? ai.saiy.android.utils.UtilsString.replaceLast(trimmed, on + Constants.SEP_SPACE + wolfram_alpha, "").trim() : trimmed;
    }

    private static String detectImdbQuery(String str) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(on + Constants.SEP_SPACE + imdb)) {
            trimmed = trimmed.replaceFirst(on + Constants.SEP_SPACE + imdb, "").trim();
        }
        if (trimmed.startsWith(imdb)) {
            trimmed = trimmed.replaceFirst(imdb, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(film)) {
            trimmed = trimmed.replaceFirst(film, "").trim();
        }
        if (trimmed.startsWith(movie)) {
            trimmed = trimmed.replaceFirst(movie, "").trim();
        }
        if (trimmed.startsWith(actor)) {
            trimmed = trimmed.replaceFirst(actor, "").trim();
        }
        if (trimmed.startsWith(actress)) {
            trimmed = trimmed.replaceFirst(actress, "").trim();
        }
        if (trimmed.startsWith(genre)) {
            trimmed = trimmed.replaceFirst(genre, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim();
        }
        return trimmed.endsWith(on + Constants.SEP_SPACE + imdb) ? ai.saiy.android.utils.UtilsString.replaceLast(trimmed, on + Constants.SEP_SPACE + imdb, "").trim() : trimmed;
    }

    private static String detectFilmQuery(String str) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(film + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(film + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(movie + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(movie + Constants.SEP_SPACE, "").trim();
        }
        return trimmed.startsWith(word_for + Constants.SEP_SPACE) ? trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim() : trimmed;
    }

    private static String detectVideoQuery(String str) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(video + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(video + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(videos + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(videos + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(video)) {
            trimmed = trimmed.replaceFirst(video, "").trim();
        }
        if (trimmed.startsWith(videos)) {
            trimmed = trimmed.replaceFirst(videos, "").trim();
        }
        return trimmed.startsWith(word_for + Constants.SEP_SPACE) ? trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim() : trimmed;
    }

    private static String detectImageQuery(String str) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + a + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + an + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + an + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + some + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + some + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(images + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(images + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(image + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(image + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(picture + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(picture + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(pictures + Constants.SEP_SPACE + of + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(pictures + Constants.SEP_SPACE + of + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(images + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(images + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(image + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(image + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(picture + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(picture + Constants.SEP_SPACE, "").trim();
        }
        return trimmed.startsWith(pictures + Constants.SEP_SPACE) ? trimmed.replaceFirst(pictures + Constants.SEP_SPACE, "").trim() : trimmed;
    }

    private static String detectSkyQuery(String str) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + sky + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + sky + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(in + Constants.SEP_SPACE + the + Constants.SEP_SPACE + sky + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(in + Constants.SEP_SPACE + the + Constants.SEP_SPACE + sky + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(the + Constants.SEP_SPACE + sky + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(the + Constants.SEP_SPACE + sky + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(sky + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(sky + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(in + Constants.SEP_SPACE + the + Constants.SEP_SPACE + universe + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(in + Constants.SEP_SPACE + the + Constants.SEP_SPACE + universe + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(the + Constants.SEP_SPACE + universe + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(the + Constants.SEP_SPACE + universe + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(universe + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(universe + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(in + Constants.SEP_SPACE + the + Constants.SEP_SPACE + stars + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(in + Constants.SEP_SPACE + the + Constants.SEP_SPACE + stars + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(the + Constants.SEP_SPACE + stars + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(the + Constants.SEP_SPACE + stars + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(stars + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(stars + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(planet + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(planet + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.endsWith(constellation)) {
            trimmed = trimmed.replaceAll(constellation, "");
        }
        if (trimmed.startsWith(constellation + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(constellation + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(star + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(star + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.endsWith(galaxy)) {
            trimmed = trimmed.replaceAll(galaxy, "");
        }
        if (trimmed.startsWith(galaxy + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(galaxy + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.endsWith(nebular)) {
            trimmed = trimmed.replaceAll(nebular, "");
        }
        if (trimmed.startsWith(nebular + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(nebular + Constants.SEP_SPACE, "").trim();
        }
        if (!trimmed.matches(moon)) {
            trimmed = trimmed.replaceFirst("\\b" + moon + "\\b", "").trim();
        }
        if (!trimmed.matches(sun)) {
            trimmed = trimmed.replaceFirst("\\b" + sun + "\\b", "").trim();
        }
        if (trimmed.endsWith(in + Constants.SEP_SPACE + the + Constants.SEP_SPACE + sky)) {
            trimmed = ai.saiy.android.utils.UtilsString.replaceLast(trimmed, in + Constants.SEP_SPACE + the + Constants.SEP_SPACE + sky, "").trim();
        }
        if (trimmed.endsWith(in + Constants.SEP_SPACE + the + Constants.SEP_SPACE + stars)) {
            trimmed = ai.saiy.android.utils.UtilsString.replaceLast(trimmed, in + Constants.SEP_SPACE + the + Constants.SEP_SPACE + stars, "").trim();
        }
        return trimmed.endsWith(in + Constants.SEP_SPACE + the + Constants.SEP_SPACE + universe) ? ai.saiy.android.utils.UtilsString.replaceLast(trimmed, in + Constants.SEP_SPACE + the + Constants.SEP_SPACE + universe, "").trim() : trimmed;
    }

    private static String detectEarthQuery(String str) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(the + Constants.SEP_SPACE + planet + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(the + Constants.SEP_SPACE + planet + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(the + Constants.SEP_SPACE + earth + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(the + Constants.SEP_SPACE + earth + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(earth + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(earth + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(word_for + Constants.SEP_SPACE + the + Constants.SEP_SPACE, "").trim();
        }
        return trimmed.startsWith(word_for + Constants.SEP_SPACE) ? trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim() : trimmed;
    }

    private static String detectWebQuery(String str) {
        String trimmed = str.replaceFirst(search, "").trim();
        if (trimmed.startsWith(the + Constants.SEP_SPACE + web + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(the + Constants.SEP_SPACE + web + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(the + Constants.SEP_SPACE + internet + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(the + Constants.SEP_SPACE + internet + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(the + Constants.SEP_SPACE + net + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(the + Constants.SEP_SPACE + net + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(internet + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(internet + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(web + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(web + Constants.SEP_SPACE, "").trim();
        }
        if (trimmed.startsWith(net + Constants.SEP_SPACE)) {
            trimmed = trimmed.replaceFirst(net + Constants.SEP_SPACE, "").trim();
        }
        return trimmed.startsWith(word_for + Constants.SEP_SPACE) ? trimmed.replaceFirst(word_for + Constants.SEP_SPACE, "").trim() : trimmed;
    }

    public @NonNull ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(voiceData) && ai.saiy.android.utils.UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            String vdLower;
            for (int i = 0; i < size; i++) {
                vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.startsWith(search) || vdLower.startsWith(ask_google)) {
                    toReturn.add(new Pair<>(CC.COMMAND_SEARCH, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "search: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}

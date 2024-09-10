package ai.saiy.android.command.music;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class Music_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Music_en.class.getSimpleName();
    private static final byte MAX_WORDS_COUNT = 3;

    private static String play;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Music_en(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (play == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static CommandMusicValues sortMusic(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final CommandMusicValues commandMusicValues = new CommandMusicValues();
        commandMusicValues.setType(CommandMusicValues.Type.UNKNOWN);
        commandMusicValues.setQuery("");
        final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
        if (play == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        final String playStr = play + XMLResultsHandler.SEP_SPACE;
        final String album = sr.getString(R.string.album);
        final String artist = sr.getString(R.string.artist);
        final String playlist = sr.getString(R.string.playlist);
        final String play_list = sr.getString(R.string.play_list);
        final String track = sr.getString(R.string.track);
        final String tracks = sr.getString(R.string.tracks);
        final String song = sr.getString(R.string.song);
        final String songs = sr.getString(R.string.songs);
        final String music = sr.getString(R.string.music);
        final String genre = sr.getString(R.string.genre);
        final String radio = sr.getString(R.string.radio);
        final String the = sr.getString(R.string.the);
        final String by = sr.getString(R.string.by);
        final String something = sr.getString(R.string.something);
        final String some = sr.getString(R.string.some);
        final String from = sr.getString(R.string.from);
        final String a = sr.getString(R.string.a);
        final String an = sr.getString(R.string.an);
        sr.reset();
        for (String voiceDatum : voiceData) {
            String vdLower = voiceDatum.toLowerCase(locale).trim();
            if (vdLower.startsWith(playStr)) {
                if (vdLower.startsWith(playStr + radio)) {
                    commandMusicValues.setType(CommandMusicValues.Type.RADIO);
                    commandMusicValues.setQuery(radio);
                } else {
                    String query = vdLower.replaceFirst(playStr, "").replaceFirst(something + XMLResultsHandler.SEP_SPACE + by + XMLResultsHandler.SEP_SPACE, "").replaceFirst(something + XMLResultsHandler.SEP_SPACE + from + XMLResultsHandler.SEP_SPACE, "").replaceFirst(some + XMLResultsHandler.SEP_SPACE + music + XMLResultsHandler.SEP_SPACE + by + XMLResultsHandler.SEP_SPACE, "").replaceFirst(some + XMLResultsHandler.SEP_SPACE + music + XMLResultsHandler.SEP_SPACE + from + XMLResultsHandler.SEP_SPACE, "").replaceFirst(some + XMLResultsHandler.SEP_SPACE + songs + XMLResultsHandler.SEP_SPACE + by + XMLResultsHandler.SEP_SPACE, "").replaceFirst(some + XMLResultsHandler.SEP_SPACE + songs + XMLResultsHandler.SEP_SPACE + from + XMLResultsHandler.SEP_SPACE, "").replaceFirst(music + XMLResultsHandler.SEP_SPACE + by + XMLResultsHandler.SEP_SPACE, "").replaceFirst(music + XMLResultsHandler.SEP_SPACE + from + XMLResultsHandler.SEP_SPACE, "").replaceFirst(a + XMLResultsHandler.SEP_SPACE + song + XMLResultsHandler.SEP_SPACE + by + XMLResultsHandler.SEP_SPACE, "").replaceFirst(a + XMLResultsHandler.SEP_SPACE + song + XMLResultsHandler.SEP_SPACE + from + XMLResultsHandler.SEP_SPACE, "").replaceFirst(song + XMLResultsHandler.SEP_SPACE + by + XMLResultsHandler.SEP_SPACE, "").replaceFirst(song + XMLResultsHandler.SEP_SPACE + from + XMLResultsHandler.SEP_SPACE, "").replaceFirst(songs + XMLResultsHandler.SEP_SPACE + by + XMLResultsHandler.SEP_SPACE, "").replaceFirst(songs + XMLResultsHandler.SEP_SPACE + from + XMLResultsHandler.SEP_SPACE, "").replaceFirst(a + XMLResultsHandler.SEP_SPACE + track + XMLResultsHandler.SEP_SPACE + by + XMLResultsHandler.SEP_SPACE, "").replaceFirst(a + XMLResultsHandler.SEP_SPACE + track + XMLResultsHandler.SEP_SPACE + from + XMLResultsHandler.SEP_SPACE, "").replaceFirst(track + XMLResultsHandler.SEP_SPACE + by + XMLResultsHandler.SEP_SPACE, "").replaceFirst(track + XMLResultsHandler.SEP_SPACE + from + XMLResultsHandler.SEP_SPACE, "").replaceFirst(tracks + XMLResultsHandler.SEP_SPACE + by + XMLResultsHandler.SEP_SPACE, "").replaceFirst(tracks + XMLResultsHandler.SEP_SPACE + from + XMLResultsHandler.SEP_SPACE, "");
                    if (query.contains(album)) {
                        commandMusicValues.setType(CommandMusicValues.Type.ALBUM);
                        query = query.replaceFirst(an + XMLResultsHandler.SEP_SPACE + album + XMLResultsHandler.SEP_SPACE, "").replaceFirst(the + XMLResultsHandler.SEP_SPACE + album + XMLResultsHandler.SEP_SPACE, "").replaceFirst(album + XMLResultsHandler.SEP_SPACE, "");
                        if (query.startsWith(by + XMLResultsHandler.SEP_SPACE)) {
                            query = query.replaceFirst(by + XMLResultsHandler.SEP_SPACE, "");
                        } else if (UtilsString.occursInLastNWords(by, query, MAX_WORDS_COUNT)) {
                            query = UtilsString.replaceLast(query, by, "");
                        }
                        commandMusicValues.setQuery(query);
                    } else if (query.contains(artist)) {
                        commandMusicValues.setType(CommandMusicValues.Type.ARTIST);
                        commandMusicValues.setQuery(query.replaceFirst(the + XMLResultsHandler.SEP_SPACE + artist + XMLResultsHandler.SEP_SPACE, "").replaceFirst(artist + XMLResultsHandler.SEP_SPACE, ""));
                    } else if (query.contains(playlist) || query.contains(play_list)) {
                        commandMusicValues.setType(CommandMusicValues.Type.PLAYLIST);
                        commandMusicValues.setQuery(query.replaceFirst(the + XMLResultsHandler.SEP_SPACE + playlist + XMLResultsHandler.SEP_SPACE, "").replaceFirst(playlist + XMLResultsHandler.SEP_SPACE, "").replaceFirst(the + XMLResultsHandler.SEP_SPACE + play_list + XMLResultsHandler.SEP_SPACE, "").replaceFirst(play_list + XMLResultsHandler.SEP_SPACE, ""));
                    } else if (query.contains(track) || query.contains(song)) {
                        commandMusicValues.setType(CommandMusicValues.Type.UNKNOWN);
                        query = query.replaceFirst(the + XMLResultsHandler.SEP_SPACE + track + XMLResultsHandler.SEP_SPACE, "").replaceFirst(track + XMLResultsHandler.SEP_SPACE, "").replaceFirst(the + XMLResultsHandler.SEP_SPACE + song + XMLResultsHandler.SEP_SPACE, "").replaceFirst(song + XMLResultsHandler.SEP_SPACE, "");
                        if (query.startsWith(by + XMLResultsHandler.SEP_SPACE)) {
                            query = query.replaceFirst(by + XMLResultsHandler.SEP_SPACE, "");
                        } else if (UtilsString.occursInLastNWords(by, query, MAX_WORDS_COUNT)) {
                            query = UtilsString.replaceLast(query, by, "");
                        }
                        commandMusicValues.setQuery(query);
                    } else if (query.contains(genre)) {
                        commandMusicValues.setType(CommandMusicValues.Type.GENRE);
                        commandMusicValues.setQuery(query.replaceFirst(the + XMLResultsHandler.SEP_SPACE + genre + XMLResultsHandler.SEP_SPACE, "").replaceFirst(genre + XMLResultsHandler.SEP_SPACE, ""));
                    } else {
                        if (query.startsWith(by + XMLResultsHandler.SEP_SPACE)) {
                            query = query.replaceFirst(by + XMLResultsHandler.SEP_SPACE, "");
                        } else if (UtilsString.occursInLastNWords(by, query, MAX_WORDS_COUNT)) {
                            query = UtilsString.replaceLast(query, by, "");
                        }
                        commandMusicValues.setQuery(query);
                    }
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return commandMusicValues;
    }

    private static void initStrings(@NonNull ai.saiy.android.localisation.SaiyResources sr) {
        play = sr.getString(R.string.play);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(voiceData) && ai.saiy.android.utils.UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final String playStr = play + XMLResultsHandler.SEP_SPACE;
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                if (voiceData.get(i).toLowerCase(locale).trim().startsWith(playStr)) {
                    toReturn.add(new Pair<>(CC.COMMAND_MUSIC, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "music: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}

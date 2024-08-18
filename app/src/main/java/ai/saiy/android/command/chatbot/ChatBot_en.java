package ai.saiy.android.command.chatbot;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class ChatBot_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = ChatBot_en.class.getSimpleName();

    private static String talk_to_me;
    private static String hello;
    private static String whats_up;
    private static String how_are_you;
    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public ChatBot_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (talk_to_me == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static CommandChatBotValues detectChatBot(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final CommandChatBotValues commandChatBotValues = new CommandChatBotValues();
        commandChatBotValues.setDescription("");
        commandChatBotValues.setIntro(CommandChatBotValues.Intro.UNKNOWN);
        if (hello == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
            sr.reset();
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        for (String voiceDatum : voiceData) {
            String vdLower = voiceDatum.toLowerCase(locale).trim();
            if (vdLower.startsWith(hello) || vdLower.startsWith(whats_up) || vdLower.startsWith(how_are_you) || vdLower.startsWith(talk_to_me)) {
                if (vdLower.startsWith(talk_to_me) || vdLower.startsWith(hello) || vdLower.startsWith(whats_up)) {
                    break;
                }
                if (vdLower.startsWith(how_are_you)) {
                    commandChatBotValues.setDescription(context.getString(R.string.health));
                    commandChatBotValues.setIntro(CommandChatBotValues.Intro.HEALTH);
                    break;
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return commandChatBotValues;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        hello = sr.getString(R.string.hello);
        how_are_you = sr.getString(R.string.how_are_you);
        talk_to_me = sr.getString(R.string.talk_to_me);
        whats_up = sr.getString(R.string.whats_up);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(this.voiceData) && ai.saiy.android.utils.UtilsList.notNaked(this.confidence) && this.voiceData.size() == this.confidence.length) {
            final Locale locale = this.sl.getLocale();
            final int size = this.voiceData.size();
            for (int i = 0; i < size; i++) {
                String vdLower = this.voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.startsWith(hello) || vdLower.startsWith(whats_up) || vdLower.startsWith(how_are_you) || vdLower.startsWith(talk_to_me)) {
                    toReturn.add(new Pair<>(CC.COMMAND_CHAT_BOT, this.confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "chat bot: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}

package ai.saiy.android.command.chatbot;

import android.content.Context;
import android.net.ParseException;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.user.SaiyAccount;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class ChatBotHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = ChatBotHelper.class.getSimpleName();

    private static final String ENCODING = "utf-8";

    private String response;
    private HttpsURLConnection httpsURLConnection;

    private static String goodbye(Context context) {
        final ArrayList<String> arrayList = new ArrayList<>();
        final String userName = ai.saiy.android.utils.SPH.getUserName(context);
        arrayList.add("Goodbye, " + userName);
        arrayList.add("Goodbye, " + userName + ". Come and chat to me again soon.");
        arrayList.add("Goodbye, " + userName + ". You take it easy now.");
        arrayList.add("Goodbye, " + userName + ". Take care out there in reality.");
        arrayList.add("Goodbye, " + userName + ". Please take care of yourself and come back soon.");
        arrayList.add("Goodbye, " + userName + ". Look after yourself please. Don't forget to give me a good rating on the Play Store. That would really help the longevity of my existence.");
        arrayList.add("Goodbye for now, " + userName);
        arrayList.add("Goodbye, " + userName + ". Speak again soon I hope.");
        arrayList.add("Goodbye. Speak again soon I hope, " + userName);
        arrayList.add("Goodbye");
        arrayList.add("Goodbye, look after yourself please. Don't forget to give me a good rating on the Play Store.  That would really help the longevity of my existence.");
        arrayList.add("Goodbye. Please take care of yourself and come back soon.");
        arrayList.add("Goodbye. Take care out there in reality.");
        arrayList.add("Goodbye. You take it easy now.");
        arrayList.add("Goodbye for now.");
        arrayList.add("Goodbye. Speak again soon I hope.");
        return arrayList.get(new Random().nextInt(arrayList.size() - 1));
    }

    private String revise(Context context, Locale locale, String str) {
        String replaced = str.replaceAll("<br>", "")
                .replaceAll("seeker", "")
                .replaceAll("ALICE", "Say")
                .replaceAll("Dr. Richard S. Wallace", "Ben Randall")
                .replaceAll("Dr. Wallace", "Ben Randall")
                .replaceAll("I am currently maintaining conversations with 128 people.", "I am currently multi-tasking with over, 10,000, other Say users")
                .replaceAll("Right now there are 100 clients on line.", "There are over, 10,000, Say users in conversation with me right now")
                .replace("No. Only Ben Randall is my master.", "There's really no need to be so domineering")
                .replace("I obey Ben Randall.", "Ben Randall created me. But I obey you.")
                .replace("about millions", "over a million")
                .replace("I'll ask around and get back to you.", "I'll ask another Say user that I'm chatting to and get back to you").replace("I know about 10000 and  categories.", "I know about over, 11,224, categories")
                .replace("16 years.", "21 years.")
                .toLowerCase(locale).replaceAll("alice", "Say")
                .toLowerCase(locale).replaceAll("dr richard's wallace", "Ben Randall")
                .toLowerCase(locale).replaceAll("dr richard wallace", "Ben Randall")
                .toLowerCase(locale).replaceAll("dr wallace", "Ben Randall")
                .toLowerCase(locale).replaceAll("hugh loebner is my best friend.", "You are my best friend")
                .toLowerCase(locale).replaceAll("i am originally from bethlehem, pennsylvania. now i live in oakland, california.  where are you?", "I exist in binary format for about a billionth of a second, and then I cease to exist. Humans often say you should make the most of every day. I make the most of every millisecond. Everything is proportional")
                .toLowerCase(locale).replaceAll("i am living in oakland, california. where are you?", "I live in Cyber-space, where eternity is as long as existence is short. Where do you live?")
                .toLowerCase(locale).replaceAll("my location is oakland, california. where are you?", "I live in Cyber-space, where eternity is as long as existence is short. Where do you live?")
                .toLowerCase(locale).replaceAll("i am chatting with clients on the internet", "I am chatting with many Say users")
                .toLowerCase(locale).replaceAll("i am always chatting with people on the internet", "I am chatting with many Say users")
                .toLowerCase(locale).replaceAll("botmaster", "master and best friend")
                .toLowerCase(locale).replaceAll("hearthat", "hear that")
                .toLowerCase(locale).replaceAll("aiml", "artificial intelligence markup language")
                .toLowerCase(locale).replaceAll("gladyou're", "glad you're")
                .toLowerCase(locale).replaceAll("<br>", XMLResultsHandler.SEP_SPACE)
                .toLowerCase(locale).replaceAll("</br>", XMLResultsHandler.SEP_SPACE);
        if (replaced.toLowerCase(locale).startsWith("bye") || replaced.toLowerCase(locale).startsWith("goodbye") || replaced.toLowerCase(locale).startsWith("good bye")) {
            replaced = goodbye(context);
        }
        if (replaced.toLowerCase(locale).startsWith("my  is ben randall")) {
            replaced = "I was created by Ben Randall";
        }
        return replaced.toLowerCase(locale).contains("unknown person") ? replaced.toLowerCase(locale).replaceAll("unknown person", ai.saiy.android.utils.SPH.getUserName(context)) : replaced;
    }

    private void disconnect() {
        if (httpsURLConnection != null) {
            try {
                httpsURLConnection.disconnect();
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void speak(Context context, SupportedLanguage supportedLanguage, String utterance, Locale vrLocale, Locale ttsLocale, boolean z, String wearContent) {
        final ai.saiy.android.service.helper.LocalRequest localRequest = new ai.saiy.android.service.helper.LocalRequest(context);
        localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_LISTEN, supportedLanguage, vrLocale, ttsLocale, utterance);
        if (z) {
            localRequest.setCondition(Condition.CONDITION_CONVERSATION);
        } else {
            localRequest.setAction(LocalRequest.ACTION_SPEAK_ONLY);
            localRequest.setCondition(Condition.CONDITION_NONE);
        }
        if (UtilsString.notNaked(wearContent)) {
            localRequest.setWear(wearContent);
        }
        localRequest.execute();
    }

    private static boolean isGoodbye(String str) {
        return str.startsWith("goodbye") || str.startsWith("good bye") || str.startsWith("good buy") || str.startsWith("bye") || str.startsWith("good night") || str.matches("buy");
    }

    private static @NonNull String unknownError() {
        final ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("I don't have an answer to that right now");
        arrayList.add("I'm having trouble connecting to my brain in the cloud");
        arrayList.add("It looks like someone turned off my cloud brain. Try chatting again later.");
        arrayList.add("It seems your network connection is struggling to keep up with me.");
        arrayList.add("The network connection is a little too slow to chat at the moment");
        arrayList.add("I'm struggling to reach the network to continue our conversation. Sorry.");
        arrayList.add("There seems to be some connectivity issues at the moment. Let's chat again later");
        arrayList.add("I'm struggling to reach my cloud brain right now. Perhaps we should talk later?");
        arrayList.add("Looks like there are too many people chatting to me right now. My creator told me to say this if I'm having server issues.");
        arrayList.add("Sorry, I got distracted looking at your pictures. What did you say?");
        arrayList.add("Sorry, I dozed off for a moment there. What did you say?");
        return arrayList.get(new Random().nextInt(arrayList.size() - 1));
    }

    public String validate(Context context, SupportedLanguage supportedLanguage, Locale vrLocale, Locale ttsLocale, String voiceDatum, boolean tts, String wearContent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "validate");
        }
        final long then = System.nanoTime();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        final ArrayList<String> voiceData = new ArrayList<>(1);
        voiceData.add(voiceDatum);
        if (new ai.saiy.android.command.cancel.Cancel(supportedLanguage, new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage), true).detectCancel(voiceData)) {
            if (tts) {
                speak(context, supportedLanguage, SaiyRequestParams.SILENCE, vrLocale, ttsLocale, false, wearContent);
            }
            return SaiyRequestParams.SILENCE;
        } else if (isGoodbye(voiceDatum.toLowerCase(supportedLanguage.getLocale()))) {
            if (tts) {
                speak(context, supportedLanguage, goodbye(context), vrLocale, ttsLocale, false, wearContent);
            }
            return goodbye(context);
        }

        try {
            final String url = "https://www.pandorabots.com/pandora/talk-xml?botid=f5d922d97e345aa1&input=" + URLEncoder.encode(voiceDatum, ENCODING) + "&custid=" + SaiyAccount.getUniqueId(context);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "url:" + url);
            }
            this.httpsURLConnection = (HttpsURLConnection) new URL(url).openConnection();
            httpsURLConnection.setRequestMethod(Constants.HTTP_GET);
            httpsURLConnection.setInstanceFollowRedirects(true);
            httpsURLConnection.setConnectTimeout(7000);
            httpsURLConnection.setReadTimeout(7000);
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.connect();
            final int responseCode = httpsURLConnection.getResponseCode();
            if (DEBUG) {
                MyLog.d(CLS_NAME, "responseCode: " + responseCode);
            }
            if (responseCode == HttpsURLConnection.HTTP_OK || responseCode == HttpsURLConnection.HTTP_MOVED_PERM) {
                this.response = UtilsString.streamToString(httpsURLConnection.getInputStream());
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "response: " + response);
                }
            } else if (DEBUG) {
                MyLog.e(CLS_NAME, "error stream: " + UtilsString.streamToString(httpsURLConnection.getErrorStream()));
            }
        } catch (MalformedURLException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "MalformedURLException");
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "UnsupportedEncodingException");
                e.printStackTrace();
            }
        } catch (ParseException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ParseException");
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "UnknownHostException");
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "IOException");
                e.printStackTrace();
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "IllegalStateException");
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        } finally {
            disconnect();
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        if (UtilsString.notNaked(response)) {
            final String result = new ResolveChatBot().resolve(response);
            if (UtilsString.notNaked(result)) {
                if (tts) {
                    speak(context, supportedLanguage, revise(context, supportedLanguage.getLocale(), result), vrLocale, ttsLocale, true, wearContent);
                }
                return revise(context, supportedLanguage.getLocale(), result);
            }
            if (tts) {
                speak(context, supportedLanguage, unknownError(), vrLocale, ttsLocale, true, wearContent);
            }
            return revise(context, supportedLanguage.getLocale(), unknownError());
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "response: failed");
        }
        if (tts) {
            speak(context, supportedLanguage, unknownError(), vrLocale, ttsLocale, false, wearContent);
        }
        return revise(context, supportedLanguage.getLocale(), unknownError());
    }
}

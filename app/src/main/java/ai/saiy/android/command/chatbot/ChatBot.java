package ai.saiy.android.command.chatbot;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class ChatBot implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object chatBot;

    public ChatBot(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public ChatBot(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.chatBot = new ChatBot_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.chatBot = new ChatBot_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.chatBot = new ChatBot_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public CommandChatBotValues detectChatBot(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return ChatBot_en.detectChatBot(context, voiceData, sl);
            case ENGLISH_US:
                return ChatBot_en.detectChatBot(context, voiceData, sl);
            default:
                return ChatBot_en.detectChatBot(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((ChatBot_en) chatBot).detectCallable();
            case ENGLISH_US:
                return ((ChatBot_en) chatBot).detectCallable();
            default:
                return ((ChatBot_en) chatBot).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}

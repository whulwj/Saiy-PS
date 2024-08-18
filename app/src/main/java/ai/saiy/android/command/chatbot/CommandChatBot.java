package ai.saiy.android.command.chatbot;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;

public class CommandChatBot {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandChatBot.class.getSimpleName();

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

    public @NonNull Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr, Locale vrLocale, Locale ttsLocale) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        CommandChatBotValues commandChatBotValues;
        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
            commandChatBotValues = (CommandChatBotValues) cr.getVariableData();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            commandChatBotValues = new ChatBot(supportedLanguage).detectChatBot(context, voiceData);
        }
        if (commandChatBotValues == null) {
            commandChatBotValues = new CommandChatBotValues();
            commandChatBotValues.setIntro(CommandChatBotValues.Intro.UNKNOWN);
        }
        String utterance;
        if (CommandChatBotValues.Intro.HEALTH == commandChatBotValues.getIntro()) {
            utterance = new ChatBotHelper().validate(context, supportedLanguage, vrLocale, ttsLocale, "how are you", false, null);
        } else {
            utterance = new ChatBotHelper().validate(context, supportedLanguage, vrLocale, ttsLocale, "hello", false, null);
        }
        outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
        outcome.setCondition(Condition.CONDITION_CONVERSATION);
        outcome.setOutcome(Outcome.SUCCESS);
        outcome.setUtterance(utterance);
        return returnOutcome(outcome);
    }
}

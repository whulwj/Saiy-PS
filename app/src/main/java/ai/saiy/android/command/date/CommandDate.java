package ai.saiy.android.command.date;

import android.content.Context;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsDate;

public final class CommandDate {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandDate.class.getSimpleName();

    public @NonNull Outcome getResponse(@NonNull Context context, @NonNull SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Outcome outcome = new Outcome();
        outcome.setOutcome(Outcome.SUCCESS);
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        outcome.setUtterance(context.getString(R.string.it_s) + XMLResultsHandler.SEP_SPACE + UtilsDate.getWeekday(context, calendar.get(Calendar.DAY_OF_WEEK)) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.the) + XMLResultsHandler.SEP_SPACE + UtilsDate.getDayOfMonth(context, calendar.get(Calendar.DAY_OF_MONTH)) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.of) + XMLResultsHandler.SEP_SPACE + UtilsDate.getMonth(context, calendar.get(Calendar.MONTH)) + XMLResultsHandler.SEP_SPACE + calendar.get(Calendar.YEAR));
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }
}

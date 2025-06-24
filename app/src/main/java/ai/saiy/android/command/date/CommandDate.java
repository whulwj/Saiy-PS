package ai.saiy.android.command.date;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.Constants;
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
        outcome.setUtterance(context.getString(R.string.it_s) + Constants.SEP_SPACE + UtilsDate.getWeekday(context, calendar.get(Calendar.DAY_OF_WEEK)) + Constants.SEP_SPACE + context.getString(R.string.the) + Constants.SEP_SPACE + UtilsDate.getDayOfMonth(context, calendar.get(Calendar.DAY_OF_MONTH)) + Constants.SEP_SPACE + context.getString(R.string.of) + Constants.SEP_SPACE + UtilsDate.getMonth(context, calendar.get(Calendar.MONTH)) + Constants.SEP_SPACE + calendar.get(Calendar.YEAR));
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }
}

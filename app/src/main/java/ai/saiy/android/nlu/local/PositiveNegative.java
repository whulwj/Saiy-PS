package ai.saiy.android.nlu.local;

import android.content.Context;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;

public class PositiveNegative {
    private int confidence;

    public enum Result {
        UNRESOLVED,
        POSITIVE,
        NEGATIVE,
        CANCEL
    }

    private int calculateConfidence(int positiveCount, int negativeCount) {
        if (negativeCount == 0 || positiveCount == 0) {
            return 100;
        }
        if (positiveCount > negativeCount) {
            return (int) Math.round(((positiveCount + negativeCount) / (positiveCount * 100.0d)));
        }
        if (negativeCount > positiveCount) {
            return (int) Math.round(((positiveCount + negativeCount) / (negativeCount * 100.0d)));
        }
        return 50;
    }

    public int getConfidence() {
        return this.confidence;
    }

    public Result resolve(Context context, ArrayList<String> arrayList, SupportedLanguage supportedLanguage) {
        Locale locale = supportedLanguage.getLocale();
        ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
        Pattern yes = Pattern.compile(".*\\b" + sr.getString(R.string.yes) + "\\b.*");
        Pattern no = Pattern.compile(".*\\b" + sr.getString(R.string.no) + "\\b.*");
        Pattern it_is = Pattern.compile(".*\\b" + sr.getString(R.string.it_is) + "\\b.*");
        Pattern right = Pattern.compile(".*\\b" + sr.getString(R.string.right) + "\\b.*");
        Pattern correct = Pattern.compile(".*\\b" + sr.getString(R.string.correct) + "\\b.*");
        Pattern wrong = Pattern.compile(".*\\b" + sr.getString(R.string.wrong) + "\\b.*");
        Pattern incorrect = Pattern.compile(".*\\b" + sr.getString(R.string.incorrect) + "\\b.*");
        Pattern ok = Pattern.compile(".*\\b" + sr.getString(R.string.ok) + "\\b.*");
        Pattern okay = Pattern.compile(".*\\b" + sr.getString(R.string.okay) + "\\b.*");
        Pattern sure = Pattern.compile(".*\\b" + sr.getString(R.string.sure) + "\\b.*");
        Pattern doNot = Pattern.compile(".*\\b" + sr.getString(R.string.dont) + "\\b.*");
        Pattern do_not = Pattern.compile(".*\\b" + sr.getString(R.string.do_not) + "\\b.*");
        Pattern yeah = Pattern.compile(".*\\b" + sr.getString(R.string.yeah) + "\\b.*");
        sr.reset();
        if (new ai.saiy.android.command.cancel.Cancel(supportedLanguage, new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage), true).detectCancel(arrayList)) {
            return Result.CANCEL;
        }
        int negativeCount = 0;
        int positiveCount = 0;
        for (String string : arrayList) {
            String trim = string.toLowerCase(locale).trim();
            if (yes.matcher(trim).matches() || it_is.matcher(trim).matches() || right.matcher(trim).matches() || correct.matcher(trim).matches() || sure.matcher(trim).matches() || yeah.matcher(trim).matches() || ok.matcher(trim).matches() || okay.matcher(trim).matches()) {
                positiveCount++;
            }
            if (no.matcher(trim).matches() || wrong.matcher(trim).matches() || incorrect.matcher(trim).matches() || doNot.matcher(trim).matches() || do_not.matcher(trim).matches()) {
                negativeCount++;
            }
        }
        this.confidence = calculateConfidence(positiveCount, negativeCount);
        return positiveCount > negativeCount ? Result.POSITIVE : negativeCount > positiveCount ? Result.NEGATIVE : Result.UNRESOLVED;
    }
}

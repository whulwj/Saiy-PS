package ai.saiy.android.command.financial;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class CommandStockQuote {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandStockQuote.class.getSimpleName();

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

    private String getUrl(String query) {
        return "https://autoc.finance.yahoo.com/autoc?query=" + query.trim().replaceAll("\\s", "%20").trim() + "&region=1&lang=en&callback=YAHOO.Finance.SymbolSuggest.ssCallback";
    }

    public @NonNull Outcome getResponse(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull SupportedLanguage supportedLanguage, @NonNull ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        final ArrayList<String> queries = new ArrayList<>();
        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
            CommandStockQuoteValues commandStockQuoteValues = (CommandStockQuoteValues) cr.getVariableData();
            if (UtilsString.notNaked(commandStockQuoteValues.getQuery())) {
                queries.add(commandStockQuoteValues.getQuery());
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            queries.addAll(new StockQuote(supportedLanguage).sortQuery(context, voiceData));
        }
        if (!ai.saiy.android.utils.UtilsList.notNaked(queries)) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getStockQuoteUnknownError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        final ArrayList<String> urls = new ArrayList<>();
        for (String query : queries) {
            urls.add(getUrl(query));
        }
        if (!ai.saiy.android.utils.UtilsList.notNaked(urls)) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getStockQuoteError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }

        final StockQuoteHelper stockQuoteHelper = new StockQuoteHelper();
        final Pair<String, String> symbolPair = stockQuoteHelper.getSymbol(urls);
        if (symbolPair.first == null || symbolPair.second == null) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getStockQuoteError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        final Pair<String, String> stockQuoteIEX = stockQuoteHelper.getStockQuoteIEX(symbolPair.first, symbolPair.second);
        if (stockQuoteIEX.first == null || stockQuoteIEX.second == null) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getStockQuoteError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        outcome.setUtterance(stockQuoteIEX.first + XMLResultsHandler.SEP_SPACE + context.getString(R.string.quote_response) + XMLResultsHandler.SEP_SPACE + stockQuoteIEX.second);
        outcome.setOutcome(Outcome.SUCCESS);
        return returnOutcome(outcome);
    }
}

package ai.saiy.android.command.navigation;

import android.content.Context;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.applications.Install;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.command.calendar.Account;
import ai.saiy.android.command.calendar.CalendarHelper;
import ai.saiy.android.command.calendar.Event;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

public class CommandNavigation {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandNavigation.class.getSimpleName();

    private final Outcome outcome = new Outcome();

    public @NonNull Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData.toString());
        }
        final long then = System.nanoTime();
        if (!UtilsNavigation.haveSupportedApplication(context)) {
            Install.showInstallLink(context, Installed.PACKAGE_GOOGLE_MAPS);
            outcome.setUtterance(PersonalityResponse.getNavigationInstallError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return outcome;
        }
        CommandNavigationValues commandNavigationValues;
        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
            commandNavigationValues = (CommandNavigationValues) cr.getVariableData();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            commandNavigationValues = new Navigation(supportedLanguage).sort(context, voiceData);
        }
        if (commandNavigationValues != null) {
            switch (commandNavigationValues.getType()) {
                case APPOINTMENT:
                    if (!ai.saiy.android.permissions.PermissionHelper.checkCalendarPermissions(context, cr.getBundle())) {
                        outcome.setOutcome(Outcome.FAILURE);
                        outcome.setUtterance(SaiyRequestParams.SILENCE);
                    } else {
                        final CalendarHelper calendarHelper = new CalendarHelper();
                        final ArrayList<Account> accounts = calendarHelper.getAccounts(context);
                        if (!UtilsList.notNaked(accounts)) {
                            outcome.setOutcome(Outcome.FAILURE);
                            outcome.setUtterance(PersonalityResponse.getCalendarAccessError(context, supportedLanguage));
                        } else {
                            Calendar calendar = Calendar.getInstance(Locale.getDefault());
                            final ArrayList<Event> events = calendarHelper.getEvents(context, accounts, calendar.getTimeInMillis(), calendar.getTimeInMillis() + (DateUtils.DAY_IN_MILLIS - 1));
                            if (!UtilsList.notNaked(events)) {
                                outcome.setOutcome(Outcome.FAILURE);
                                outcome.setUtterance(PersonalityResponse.getCalendarEventsError(context, supportedLanguage));
                            } else {
                                for (Event event : events) {
                                    String location = event.getLocation();
                                    if (UtilsString.notNaked(location)) {
                                        if (UtilsNavigation.navigateToAddress(context, location)) {
                                            outcome.setUtterance(context.getString(R.string.navigating) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.to) + XMLResultsHandler.SEP_SPACE + location);
                                            outcome.setOutcome(Outcome.SUCCESS);
                                        } else {
                                            outcome.setOutcome(Outcome.FAILURE);
                                            outcome.setUtterance(context.getString(R.string.error_navigation_app_failed));
                                        }
                                    }
                                }
                                if (outcome.getUtterance() == null) {
                                    outcome.setOutcome(Outcome.FAILURE);
                                    outcome.setUtterance(PersonalityResponse.getCalendarEventsError(context, supportedLanguage) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.with_a_location_listed));
                                }
                            }
                        }
                    }
                    break;
                case DESTINATION:
                    if (!UtilsNavigation.navigateToAddress(context, commandNavigationValues.getAddress())) {
                        outcome.setOutcome(Outcome.FAILURE);
                        outcome.setUtterance(context.getString(R.string.error_navigation_app_failed));
                    } else {
                        outcome.setUtterance(context.getString(R.string.navigating) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.to) + XMLResultsHandler.SEP_SPACE + commandNavigationValues.getAddress());
                        outcome.setOutcome(Outcome.SUCCESS);
                    }
                    break;
                default:
                    outcome.setUtterance(PersonalityResponse.getNavigationDestinationError(context, supportedLanguage));
                    outcome.setOutcome(Outcome.FAILURE);
                    ai.saiy.android.applications.UtilsApplication.launchAppFromPackageName(context, Installed.PACKAGE_GOOGLE_MAPS);
                    break;
            }
        } else {
            outcome.setUtterance(PersonalityResponse.getNavigationDestinationError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            ai.saiy.android.applications.UtilsApplication.launchAppFromPackageName(context, Installed.PACKAGE_GOOGLE_MAPS);
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }
}

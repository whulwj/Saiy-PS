package ai.saiy.android.command.agenda;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.tts.attributes.Gender;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsDate;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

public class CommandAgenda {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandAgenda.class.getSimpleName();

    private long then;

    /**
     * A single point of return to check the elapsed time for debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private Outcome returnOutcome(@NonNull Outcome outcome) {
        if (DEBUG) {
            MyLog.getElapsed(CommandAgenda.class.getSimpleName(), then);
        }
        return outcome;
    }

    private static String noEvents() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("You have no events scheduled ");
        arrayList.add("You have nothing scheduled ");
        arrayList.add("You've a clear day ");
        arrayList.add("You have a clear day ");
        arrayList.add("You've got no events scheduled ");
        arrayList.add("You've a free day ");
        arrayList.add("There's nothing in your calendar ");
        arrayList.add("There's nothing in your diary ");
        arrayList.add("You've nothing planned ");
        arrayList.add("There's nothing planned ");
        arrayList.add("There are no events set ");
        arrayList.add("There are no events scheduled ");
        arrayList.add("You've nothing planned ");
        arrayList.add("You have no events scheduled ");
        arrayList.add("You have nothing scheduled ");
        arrayList.add("You've a clear day ");
        arrayList.add("You have a clear day ");
        arrayList.add("You've got no events scheduled ");
        arrayList.add("You've a free day ");
        arrayList.add("There's nothing in your calendar ");
        arrayList.add("There's nothing in your diary ");
        arrayList.add("You've nothing planned ");
        arrayList.add("There's nothing planned ");
        arrayList.add("There are no events set ");
        arrayList.add("There are no events scheduled ");
        arrayList.add("You've nothing planned ");
        arrayList.add("You have no events scheduled ");
        arrayList.add("You have nothing scheduled ");
        arrayList.add("You've a clear day ");
        arrayList.add("You have a clear day ");
        arrayList.add("You've got no events scheduled ");
        arrayList.add("You've a free day ");
        arrayList.add("There's nothing in your calendar ");
        arrayList.add("There's nothing in your diary ");
        arrayList.add("You've nothing planned ");
        arrayList.add("There's nothing planned ");
        arrayList.add("There are no events set ");
        arrayList.add("There are no events scheduled ");
        arrayList.add("You've nothing planned ");
        return arrayList.get(new Random().nextInt(arrayList.size() - 1));
    }

    private String myDescription(Context context) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("You");
        arrayList.add("yourself");
        arrayList.add("your good self");
        arrayList.add("You");
        arrayList.add("yourself");
        arrayList.add("You");
        arrayList.add("yourself");
        arrayList.add("your good self");
        arrayList.add("the legend that is you");
        if (ai.saiy.android.utils.SPH.getUserGender(context).equals(Gender.MALE)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "Adding Male");
            }
            arrayList.add("your handsome self");
        } else if (ai.saiy.android.utils.SPH.getUserGender(context).equals(Gender.FEMALE)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "Adding Female");
            }
            arrayList.add("your beautiful self");
        } else if (DEBUG) {
            MyLog.v(CLS_NAME, "Don't know user gender");
        }
        arrayList.add("yourself");
        return arrayList.get(new Random().nextInt(arrayList.size() - 1));
    }

    private static String costOfTime(final boolean isAllDay, int index) {
        ArrayList<String> arrayList = new ArrayList<>();
        if (isAllDay) {
            if (index == 0) {
                arrayList.add("which lasts all day. ");
                arrayList.add("which is an all day event. ");
                arrayList.add("which is for the whole day. ");
                arrayList.add("that lasts all day. ");
                arrayList.add("that is an all day event. ");
                arrayList.add("that is for the whole day. ");
                arrayList.add("that is scheduled for the whole day. ");
                arrayList.add("which lasts all day. ");
                arrayList.add("which is an all day event. ");
                arrayList.add("which is for the whole day. ");
                arrayList.add("that lasts all day. ");
                arrayList.add("that is an all day event. ");
                arrayList.add("that is for the whole day. ");
                arrayList.add("that is scheduled for the whole day. ");
                arrayList.add("which lasts all day. ");
                arrayList.add("which is an all day event. ");
                arrayList.add("which is for the whole day. ");
                arrayList.add("that lasts all day. ");
                arrayList.add("that is an all day event. ");
                arrayList.add("that is for the whole day. ");
                arrayList.add("that is scheduled for the whole day. ");
            } else {
                arrayList.add("which also lasts all day. ");
                arrayList.add("which is an all day event too. ");
                arrayList.add("also for the whole day. ");
                arrayList.add("that lasts all day as well. ");
                arrayList.add("that is an all day event too. ");
                arrayList.add("that is also for the whole day. ");
                arrayList.add("that is scheduled for the whole day as well. ");
                arrayList.add("which also lasts all day. ");
                arrayList.add("which is an all day event too. ");
                arrayList.add("also for the whole day. ");
                arrayList.add("that lasts all day as well. ");
                arrayList.add("that is an all day event too. ");
                arrayList.add("that is also for the whole day. ");
                arrayList.add("that is scheduled for the whole day as well. ");
                arrayList.add("which also lasts all day. ");
                arrayList.add("which is an all day event too. ");
                arrayList.add("also for the whole day. ");
                arrayList.add("that lasts all day as well. ");
                arrayList.add("that is an all day event too. ");
                arrayList.add("that is also for the whole day. ");
                arrayList.add("that is scheduled for the whole day as well. ");
            }
        } else {
            arrayList.add("starting at ");
            arrayList.add("beginning at ");
            arrayList.add("commencing at ");
            arrayList.add("which starts at ");
            arrayList.add("starting at ");
            arrayList.add("beginning at ");
            arrayList.add("commencing at ");
            arrayList.add("which starts at ");
            arrayList.add("starting at ");
            arrayList.add("beginning at ");
            arrayList.add("commencing at ");
            arrayList.add("which starts at ");
        }
        return arrayList.get(new Random().nextInt(arrayList.size() - 1));
    }

    private void examEndDate(ArrayList<ai.saiy.android.command.calendar.Event> events) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
        Calendar eventEndCal = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
        ai.saiy.android.command.calendar.Event event;
        for (int i = 0; i < events.size(); ++i) {
            event = events.get(i);
            eventEndCal.setTime(event.getEndDate());
            if (calendar.after(eventEndCal)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Removed event: " + event.getTitle());
                }
                events.remove(i);
                i--;
            } else if (DEBUG) {
                MyLog.i(CLS_NAME, "Kept event: " + event.getTitle());
            }
        }
    }

    private void examineAllDay(@NonNull ArrayList<ai.saiy.android.command.calendar.Event> events, @NonNull AgendaProcess agendaProcess) {
        final Calendar queryCal = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
        queryCal.setTime(agendaProcess.getDate());
        if (DEBUG) {
            MyLog.i(CLS_NAME, "Querying from: " + java.text.DateFormat.getDateTimeInstance().format(queryCal.getTime()));
        }
        ai.saiy.android.command.calendar.Event event;
        for (int i = 0; i < events.size(); ++i) {
            event = events.get(i);
            if (event.isAllDay()) {
                final Calendar eventStartCal = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
                eventStartCal.setTime(event.getStartDate());
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "------------------ all day examine start --------------");
                    MyLog.i(CLS_NAME, "eventName: " + event.getTitle());
                    MyLog.i(CLS_NAME, "startDate: " + java.text.DateFormat.getDateTimeInstance().format(eventStartCal.getTime()));
                    MyLog.i(CLS_NAME, "startHour: " + eventStartCal.get(Calendar.HOUR_OF_DAY));
                    MyLog.i(CLS_NAME, "DST offset: " + eventStartCal.get(Calendar.DST_OFFSET));
                    MyLog.i(CLS_NAME, "Zone offset: " + eventStartCal.get(Calendar.ZONE_OFFSET));
                }
                eventStartCal.add(Calendar.MILLISECOND, -eventStartCal.get(Calendar.DST_OFFSET));
                eventStartCal.add(Calendar.MILLISECOND, -eventStartCal.get(Calendar.ZONE_OFFSET));
                eventStartCal.add(Calendar.MINUTE, 55);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "eventStartCal: updated startHour: " + eventStartCal.get(Calendar.HOUR_OF_DAY));
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "eventStartCal DAY_OF_YEAR: " + eventStartCal.get(Calendar.DAY_OF_YEAR));
                    MyLog.i(CLS_NAME, "queryCal DAY_OF_YEAR: " + queryCal.get(Calendar.DAY_OF_YEAR));
                }

                if (eventStartCal.get(Calendar.DAY_OF_YEAR) != queryCal.get(Calendar.DAY_OF_YEAR)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "calendars don't have same day of year");
                    }
                    if (eventStartCal.get(Calendar.DAY_OF_YEAR) < queryCal.get(Calendar.DAY_OF_YEAR)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "eventStartCal is < today");
                        }
                        final Calendar eventEndCal = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
                        eventEndCal.setTime(event.getEndDate());
                        eventEndCal.add(Calendar.MILLISECOND, -eventEndCal.get(Calendar.DST_OFFSET));
                        eventEndCal.add(Calendar.MILLISECOND, -eventEndCal.get(Calendar.ZONE_OFFSET));
                        eventEndCal.add(Calendar.MINUTE, -55);
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "eventEndCal: updated endHour: " + eventEndCal.get(Calendar.HOUR_OF_DAY));
                            MyLog.i(CLS_NAME, "eventEndCal: updated date: " + java.text.DateFormat.getDateTimeInstance().format(eventEndCal.getTime()));
                            MyLog.i(CLS_NAME, "eventEndCal DAY_OF_YEAR: " + eventEndCal.get(Calendar.DAY_OF_YEAR));
                        }
                        if (eventEndCal.get(Calendar.DAY_OF_YEAR) < queryCal.get(Calendar.DAY_OF_YEAR)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "end time is before query time");
                                MyLog.i(CLS_NAME, "Removing event");
                            }
                            events.remove(i);
                            i--;
                        } else if (DEBUG) {
                            MyLog.i(CLS_NAME, "end time >= query time");
                            MyLog.i(CLS_NAME, "Keeping event");
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "eventStartCal is > today");
                            MyLog.i(CLS_NAME, "Removing event");
                        }
                        events.remove(i);
                        i--;
                    }
                } else if (DEBUG) {
                    MyLog.i(CLS_NAME, "calendars have same day of year");
                    MyLog.i(CLS_NAME, "Keeping event");
                }
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "------------------ all day examine end--------------");
                }
            } else if (DEBUG) {
                MyLog.v(CLS_NAME, "examineAllDay: event not all day - ignoring");
            }
        }
    }

    private static String noRemainingEvent() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("You have no remaining events today ");
        arrayList.add("You have nothing scheduled for the rest of the day ");
        arrayList.add("You've a clear day now.");
        arrayList.add("You have a clear day now. ");
        arrayList.add("You've got no events scheduled for the rest of the day");
        arrayList.add("You've a free day from now on");
        arrayList.add("There's nothing in your calendar for the rest of the day. So you can relax.");
        arrayList.add("There's nothing in your diary for the rest of the day");
        arrayList.add("You've nothing planned for the rest of today");
        arrayList.add("There's nothing planned for the remainder of the day");
        arrayList.add("There are no events set for the rest of the day");
        arrayList.add("There are no events scheduled for the remainder of the day");
        arrayList.add("You've nothing planned for the rest of the day. So you can chill out.");
        arrayList.add("You have no remaining events today ");
        arrayList.add("You have nothing scheduled for the rest of the day ");
        arrayList.add("You've a clear day now.");
        arrayList.add("You have a clear day now. So you can relax and take it easy.");
        arrayList.add("You've got no events scheduled for the rest of the day");
        arrayList.add("You've a free day from now on");
        arrayList.add("There's nothing in your calendar for the rest of the day");
        arrayList.add("There's nothing in your diary for the rest of the day");
        arrayList.add("You've nothing planned for the rest of today");
        arrayList.add("There's nothing planned for the remainder of the day");
        arrayList.add("There are no events set for the rest of the day");
        arrayList.add("There are no events scheduled for the remainder of the day");
        arrayList.add("You've nothing planned for the rest of the day");
        return arrayList.get(new Random().nextInt(arrayList.size() - 1));
    }

    private static String sixOrMoreEvents(Context context) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("you have a busy day, with ");
        arrayList.add("you've a busy day, with ");
        arrayList.add("you've got a busy day, with ");
        arrayList.add("you have a hectic day, with ");
        arrayList.add("you've a hectic day, with ");
        arrayList.add("you have a full day, with ");
        arrayList.add("you've a full day, with ");
        arrayList.add("you've got a full day, with ");
        arrayList.add("it's a busy day for you, with ");
        arrayList.add("you have a busy day, with ");
        arrayList.add("you've a busy day, with ");
        arrayList.add("you've got a busy day, with ");
        arrayList.add("you have a hectic day, with ");
        arrayList.add("you've a hectic day, with ");
        arrayList.add("you have a full day, with ");
        arrayList.add("you've a full day, with ");
        arrayList.add("you've got a full day, with ");
        arrayList.add("it's a busy day for you, with ");
        arrayList.add("it's a busy day for you " + ai.saiy.android.utils.SPH.getUserName(context) + " with ");
        arrayList.add("you have a busy day" + ai.saiy.android.utils.SPH.getUserName(context) + " with ");
        arrayList.add("you've a busy day" + ai.saiy.android.utils.SPH.getUserName(context) + " with ");
        arrayList.add("you've got a busy day" + ai.saiy.android.utils.SPH.getUserName(context) + " with ");
        arrayList.add("you have a hectic day" + ai.saiy.android.utils.SPH.getUserName(context) + " with ");
        arrayList.add("you've a hectic day" + ai.saiy.android.utils.SPH.getUserName(context) + " with ");
        arrayList.add("you have a full day" + ai.saiy.android.utils.SPH.getUserName(context) + " with ");
        arrayList.add("you've a full day" + ai.saiy.android.utils.SPH.getUserName(context) + " with ");
        arrayList.add("you've got a full day" + ai.saiy.android.utils.SPH.getUserName(context) + " with ");
        arrayList.add("you have a busy day, with ");
        arrayList.add("you've a busy day, with ");
        arrayList.add("you've got a busy day, with ");
        arrayList.add("you have a hectic day, with ");
        arrayList.add("you've a hectic day, with ");
        arrayList.add("you have a full day, with ");
        arrayList.add("you've a full day, with ");
        arrayList.add("you've got a full day, with ");
        arrayList.add("it's a busy day for you, with ");
        return arrayList.get(new Random().nextInt(arrayList.size() - 1));
    }

    private static String fiveOrMoreAttendees() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("There are lots of attendees, including ");
        arrayList.add("There are many attendees, including ");
        arrayList.add("There are lots of people attending, including ");
        arrayList.add("There are lots of attendees, including ");
        arrayList.add("There are many attendees, including ");
        arrayList.add("There are lots of people attending, including ");
        arrayList.add("There are lots of attendees, including ");
        arrayList.add("There are many attendees, including ");
        arrayList.add("There are lots of people attending, including ");
        arrayList.add("It's a popular event, with many attendees, including ");
        arrayList.add("It's a popular event, with many attending, including ");
        arrayList.add("There are lots of attendees, including ");
        arrayList.add("There are many attendees, including ");
        arrayList.add("There are lots of people attending, including ");
        return arrayList.get(new Random().nextInt(arrayList.size() - 1));
    }

    private static String oneEvent() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("you have just the ");
        arrayList.add("you've just ");
        arrayList.add("you've got ");
        arrayList.add("you only have ");
        arrayList.add("you have ");
        arrayList.add("there's just ");
        arrayList.add("there's only ");
        arrayList.add("you have just the ");
        arrayList.add("you've just ");
        arrayList.add("you've got ");
        arrayList.add("you only have ");
        arrayList.add("you have ");
        arrayList.add("there's just ");
        arrayList.add("there's only ");
        arrayList.add("you have just the ");
        arrayList.add("you've just ");
        arrayList.add("you've got ");
        arrayList.add("you only have ");
        arrayList.add("you have ");
        arrayList.add("there's just ");
        arrayList.add("there's only ");
        return arrayList.get(new Random().nextInt(arrayList.size() - 1));
    }

    public @NonNull Outcome getResponse(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull SupportedLanguage supportedLanguage, @NonNull ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "isResolved: false");
        }
        if (!ai.saiy.android.permissions.PermissionHelper.checkCalendarPermissions(context, cr.getBundle())) {
            outcome.setOutcome(Outcome.SUCCESS);
            outcome.setUtterance(SaiyRequestParams.SILENCE);
            return returnOutcome(outcome);
        }

        String myName;
        final ai.saiy.android.command.calendar.CalendarHelper calendarHelper = new ai.saiy.android.command.calendar.CalendarHelper();
        final String userProfileName = ai.saiy.android.permissions.PermissionHelper.checkReadContactPermissionNR(context) ? new ai.saiy.android.contacts.ContactHelper().getUserProfileName(context) : null;
        if (UtilsString.notNaked(userProfileName)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "attendeeName: getUserProfileName: " + userProfileName);
            }
            ai.saiy.android.utils.SPH.setUserAttendeeName(context, userProfileName);
            myName = userProfileName;
        } else {
            String attendeeName = ai.saiy.android.utils.SPH.getUserAttendeeName(context);
            if (!UtilsString.notNaked(attendeeName)) {
                attendeeName = calendarHelper.getUserAttendeeName(context);
                if (UtilsString.notNaked(attendeeName)) {
                    ai.saiy.android.utils.SPH.setUserAttendeeName(context, attendeeName);
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "attendeeName attempt: " + attendeeName);
                }
            } else if (DEBUG) {
                MyLog.i(CLS_NAME, "attendeeName: " + attendeeName);
            }
            myName = attendeeName;
        }
        final ArrayList<ai.saiy.android.command.calendar.Account> accounts = calendarHelper.getAccounts(context);
        if (!UtilsList.notNaked(accounts)) {
            outcome.setOutcome(Outcome.FAILURE);
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getCalendarAccessError(context, supportedLanguage));
            return returnOutcome(outcome);
        }

        final AgendaProcess agendaProcess = AgendaProcessHelper.resolve(context, voiceData, supportedLanguage.getLocale());
        if (agendaProcess.getOutcome() != Outcome.SUCCESS) {
            outcome.setUtterance(agendaProcess.getUtterance());
            outcome.setOutcome(Outcome.SUCCESS);
            return returnOutcome(outcome);
        }

        final ArrayList<ai.saiy.android.command.calendar.Event> events = calendarHelper.getEvents(context, accounts, agendaProcess.getBeginTimestamp(), agendaProcess.getEndTimestamp());
        final String weekday = DateFormat.format("EEEE", agendaProcess.getDate()).toString();
        final String dayOfMonthString = UtilsDate.getDayOfMonth(context, agendaProcess.getDayOfMonth());
        final String monthString = DateFormat.format("MMMM", agendaProcess.getDate()).toString();
        final String yearString = DateFormat.format("yyyy", agendaProcess.getDate()).toString();
        if (DEBUG) {
            MyLog.v(CLS_NAME, "weekday: " + weekday);
            MyLog.v(CLS_NAME, "dotm: " + dayOfMonthString);
            MyLog.v(CLS_NAME, "monthString: " + monthString);
            MyLog.v(CLS_NAME, "year: " + yearString);
            MyLog.v(CLS_NAME, "queried from Locale: " + java.text.DateFormat.getDateTimeInstance().format(agendaProcess.getDate()));
        }
        final StringBuilder sb = new StringBuilder();
        boolean haveObsoletedEvent = false;
        if (agendaProcess.isToday()) {
            final int oldSize = events.size();
            examEndDate(events);
            if (oldSize != events.size()) {
                haveObsoletedEvent = true;
            }
        }
        examineAllDay(events, agendaProcess);
        if (events.isEmpty()) {
            outcome.setOutcome(agendaProcess.getOutcome());
            if (!haveObsoletedEvent) {
                sb.append(noEvents());
            }
            if (agendaProcess.isToday()) {
                if (haveObsoletedEvent) {
                    sb.append(noRemainingEvent());
                } else {
                    sb.append("today.");
                }
            } else if (agendaProcess.isTomorrow()) {
                sb.append("tomorrow.");
            } else {
                sb.append("on ").append(weekday).append(", ").append("the ").append(dayOfMonthString).append(" of ").append(monthString);
                if (agendaProcess.getYear() != Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale()).get(Calendar.YEAR)) {
                    sb.append(", ").append(agendaProcess.getYear()).append(".");
                } else {
                    sb.append(".");
                }
            }
            outcome.setUtterance(sb.toString());
            return returnOutcome(outcome);
        }

        if (agendaProcess.isToday()) {
            sb.append("Today, ");
        } else if (agendaProcess.isTomorrow()) {
            sb.append("Tomorrow, ");
        } else {
            sb.append("On ").append(weekday).append(", ").append("the ").append(dayOfMonthString).append(" of ").append(monthString).append(", ");
            if (agendaProcess.getYear() != Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale()).get(Calendar.YEAR)) {
                sb.append(agendaProcess.getYear()).append(", ");
            }
        }
        final int size = events.size();
        String summary;
        switch (size) {
            case 1:
                summary = oneEvent() + size + (haveObsoletedEvent? " remaining event. " : " event. ");
                break;
            case 2:
            case 3:
            case 4:
            case 5:
                summary = "you have " + size + (haveObsoletedEvent? " remaining events. " : " events. ");
                break;
            default:
                summary = sixOrMoreEvents(context) + size + (haveObsoletedEvent? " remaining events. " : " events. ") + "I'll tell you the first five. ";
                break;
        }
        sb.append(summary);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "introString: " + sb);
        }

        ai.saiy.android.command.calendar.Event event;
        for (int i = 0; i < Math.min(size, 5); ++i) {
            event = events.get(i);
            String title = event.getTitle();
            String eventName = UtilsString.notNaked(title) ? title.trim() : "An untitled event";
            switch (i) {
                case 0:
                    if (size > 1) {
                        sb.append("First, ");
                    }
                    break;
                case 1:
                    if (size == 2) {
                        sb.append("Followed by, ");
                    } else {
                        sb.append("Next, ");
                    }
                    break;
                default:
                    if (i == size - 1) {
                        sb.append("Finally, ");
                    } else {
                        sb.append("Then, ");
                    }
                    break;
            }
            boolean isAllDay = event.isAllDay();
            sb.append(eventName).append(", ").append(costOfTime(isAllDay, i));
            if (!isAllDay) {
                String hourString = DateFormat.format("h", event.getStartDate()).toString();
                String minuteString = DateFormat.format("m", event.getStartDate()).toString();
                String amPm = AgendaHelper.getAMPM(event.getStartDate());
                String timeString = minuteString.matches("0") ? hourString + XMLResultsHandler.SEP_SPACE + amPm + XMLResultsHandler.SEP_SPACE : hourString + XMLResultsHandler.SEP_SPACE + minuteString + XMLResultsHandler.SEP_SPACE + amPm + XMLResultsHandler.SEP_SPACE;
                sb.append(timeString);
            }

            ArrayList<String> attendees = event.attendees();
            if (attendees.size() > 1) {
                if (attendees.size() > 4) {
                    sb.append(fiveOrMoreAttendees());
                } else {
                    sb.append("There are ").append(attendees.size()).append(" attendees. ");
                }

                for (int j = 0; j < Math.min(attendees.size(), 5); ++j) {
                    String attendee = attendees.get(j);
                    if (DEBUG) {
                        MyLog.v(CLS_NAME, "attendee: " + attendee);
                    }
                    String descriptionOfAttendee = (myName == null || !TextUtils.equals(myName, attendee)) ? attendee : myDescription(context);
                    if (j == attendees.size() - 1 || j == 4) {
                        sb.append("and ").append(descriptionOfAttendee).append(". ");
                    } else {
                        sb.append(descriptionOfAttendee).append(", ");
                    }
                }
            }
        }
        outcome.setOutcome(agendaProcess.getOutcome());
        outcome.setUtterance(sb.toString());
        return returnOutcome(outcome);
    }
}

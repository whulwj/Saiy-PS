package ai.saiy.android.command.calendar;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import ai.saiy.android.R;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

public class CalendarHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = CalendarHelper.class.getSimpleName();

    public static boolean setEvent(Context context, int year, int month, int date, int hourOfDay, int minute, String title, boolean allDay) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setEvent");
        }
        Calendar beginTimeCalendar = Calendar.getInstance();
        beginTimeCalendar.set(year, month, date, hourOfDay, minute);
        Calendar endTimeCalendar = Calendar.getInstance();
        endTimeCalendar.set(year, month, date, hourOfDay + 1, minute);
        final Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTimeCalendar.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeCalendar.getTimeInMillis());
        intent.putExtra(CalendarContract.Events.TITLE, title);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, "#" + context.getString(R.string.app_name));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (allDay) {
            intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
        }
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException activityNotFoundException) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "ActivityNotFoundException");
                activityNotFoundException.printStackTrace();
            }
            intent.setAction(Intent.ACTION_EDIT);
            intent.setType(CalendarContract.ACTION_HANDLE_CUSTOM_EVENT);
            try {
                context.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "ActivityNotFoundException e1");
                    e.printStackTrace();
                }
                return false;
            }
        }
    }

    public String getUserAttendeeName(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getUserAttendeeName");
        }
        final ArrayList<Account> accounts = getAccounts(context);
        if (UtilsList.notNaked(accounts)) {
            Calendar calendar = Calendar.getInstance(UtilsLocale.getDefaultLocale());
            final ArrayList<Event> events = getEvents(context, accounts, calendar.getTimeInMillis(), 100 * DateUtils.DAY_IN_MILLIS + calendar.getTimeInMillis());
            if (UtilsList.notNaked(events)) {
                ArrayList<String> attendees = new ArrayList<>();
                for (Event event : events) {
                    attendees.addAll(event.attendees());
                }
                if (UtilsList.notNaked(attendees)) {
                    return attendees.get(0);
                }
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "no attendees");
                }
            } else if (DEBUG) {
                MyLog.w(CLS_NAME, "no events");
            }
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "no accounts");
        }
        return null;
    }

    public ArrayList<Event> getEvents(Context context, ArrayList<Account> arrayList, long beginTimestamp, long endTimestamp) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getEvents");
        }
        final long startTime = System.nanoTime();
        final ArrayList<Cursor> cursors = new ArrayList<>();
        final ContentResolver contentResolver = context.getContentResolver();
        final Uri.Builder buildUpon = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(buildUpon, beginTimestamp);
        ContentUris.appendId(buildUpon, endTimestamp);
        Calendar beginTimeCalendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
        Calendar endTimeCalendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
        final ArrayList<Event> events = new ArrayList<>();
        final String[] eventProjection = {CalendarContract.Instances._ID, CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN, CalendarContract.Instances.END, CalendarContract.Instances.EVENT_ID, CalendarContract.Instances.ALL_DAY, CalendarContract.Instances.EVENT_LOCATION};
        final String[] attendeesProjection = {CalendarContract.Attendees.ATTENDEE_NAME};
        try {
            ArrayList<String> attendees = new ArrayList<>();
            for (Account account : arrayList) {
                Cursor cursor = contentResolver.query(buildUpon.build(), eventProjection, CalendarContract.Instances.CALENDAR_ID + "=" + account.getCalendarID(), null, CalendarContract.Instances.START_DAY + " ASC, " + CalendarContract.Instances.START_MINUTE + " ASC");
                if (cursor != null) {
                    cursors.add(cursor);
                    int columnBeginIndex = cursor.getColumnIndex(CalendarContract.Instances.BEGIN);
                    int columnEndIndex = cursor.getColumnIndex(CalendarContract.Instances.END);
                    int columnTitleIndex = cursor.getColumnIndex(CalendarContract.Instances.TITLE);
                    int columnIdIndex = cursor.getColumnIndex(CalendarContract.Instances._ID);
                    int columnAllDayIndex = cursor.getColumnIndex(CalendarContract.Instances.ALL_DAY);
                    int columnEventIdIndex = cursor.getColumnIndex(CalendarContract.Instances.EVENT_ID);
                    int columnLocationIndex = cursor.getColumnIndex(CalendarContract.Instances.EVENT_LOCATION);
                    while (cursor.moveToNext()) {
                        beginTimeCalendar.setTimeInMillis(cursor.getLong(columnBeginIndex));
                        endTimeCalendar.setTimeInMillis(cursor.getLong(columnEndIndex));
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "calendarId: " + account.getCalendarID());
                            MyLog.d(CLS_NAME, "instanceId: " + cursor.getLong(columnIdIndex));
                            MyLog.d(CLS_NAME, "title: " + cursor.getString(columnTitleIndex));
                            MyLog.d(CLS_NAME, "startDate: " + beginTimeCalendar.getTime());
                            MyLog.d(CLS_NAME, "endDate: " + endTimeCalendar.getTime());
                            MyLog.d(CLS_NAME, "eventId: " + cursor.getLong(columnEventIdIndex));
                            MyLog.d(CLS_NAME, "allDay: " + cursor.getInt(columnAllDayIndex));
                            MyLog.d(CLS_NAME, "Location: " + cursor.getString(columnLocationIndex));
                        }
                        Cursor attendeeCursor = CalendarContract.Attendees.query(contentResolver, cursor.getLong(columnEventIdIndex), attendeesProjection);
                        attendees.clear();
                        if (attendeeCursor != null) {
                            cursors.add(attendeeCursor);
                            int columnNameIndex = attendeeCursor.getColumnIndex(CalendarContract.Attendees.ATTENDEE_NAME);
                            while (attendeeCursor.moveToNext()) {
                                String name = attendeeCursor.getString(columnNameIndex);
                                if (UtilsString.notNaked(name)) {
                                    attendees.add(name);
                                }
                            }
                            attendeeCursor.close();
                            cursors.remove(attendeeCursor);
                        }
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "attendees: " + attendees.size() + " : " + attendees);
                        }
                        events.add(new Event(cursor.getString(columnTitleIndex), beginTimeCalendar.getTime(), endTimeCalendar.getTime(), cursor.getInt(columnAllDayIndex) > 0, attendees, cursor.getString(columnLocationIndex)));
                    }
                    cursor.close();
                    cursors.remove(cursor);
                }
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "IllegalStateException");
                e.printStackTrace();
            }
        } catch (SecurityException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "SecurityException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        } finally {
            for (Cursor cursor: cursors) {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Throwable ignored) {
                }
            }
            if (DEBUG && !cursors.isEmpty()) {
                MyLog.d(CLS_NAME, "getEvents finally closed" );
            }
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "eventArray: size - " + events.size());
            MyLog.getElapsed(CLS_NAME, startTime);
        }
        return events;
    }

    public @NonNull ArrayList<Account> getAccounts(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAccounts");
        }
        final long startTime = System.nanoTime();
        final ArrayList<Account> accounts = new ArrayList<>();
        final String[] projection = {CalendarContract.Calendars._ID, CalendarContract.Calendars.ACCOUNT_NAME, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CalendarContract.Calendars.VISIBLE};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, CalendarContract.Calendars._ID + " ASC");
            if (cursor == null) {
                return accounts;
            }
            final int columnAccountNameIndex = cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME);
            final int columnCalendarNameIndex = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME);
            final int columnVisibleIndex = cursor.getColumnIndex(CalendarContract.Calendars.VISIBLE);
            final int columnIdIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID);
            while (cursor.moveToNext()) {
                String accountName = cursor.getString(columnAccountNameIndex);
                if (!UtilsString.notNaked(accountName)) {
                    continue;
                }
                String calendarName = cursor.getString(columnCalendarNameIndex);
                if (!UtilsString.notNaked(calendarName)) {
                    continue;
                }
                if (cursor.getInt(columnVisibleIndex) <= 0) {
                    continue;
                }
                accounts.add(new Account(cursor.getLong(columnIdIndex), calendarName, accountName));
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "IllegalStateException");
                e.printStackTrace();
            }
        } catch (SecurityException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "SecurityException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getAccounts: IllegalStateException");
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getAccounts: Exception");
                }
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getAccounts: finally closing");
            }
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "accountArray: size - " + accounts.size());
            MyLog.getElapsed(CLS_NAME, startTime);
        }
        return accounts;
    }
}

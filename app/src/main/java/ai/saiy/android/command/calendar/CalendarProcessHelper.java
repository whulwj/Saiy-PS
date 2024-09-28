package ai.saiy.android.command.calendar;

import static ai.saiy.android.utils.UtilsDate.MONTH_OFFSET;

import android.content.Context;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsDate;
import ai.saiy.android.utils.UtilsLocale;

public class CalendarProcessHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = CalendarProcessHelper.class.getSimpleName();

    private static CalendarProcess calendarProcess;

    private static Pattern pFIRST;
    private static Pattern pSECOND;
    private static Pattern pTHIRD;
    private static Pattern pFOURTH;
    private static Pattern pFIFTH;
    private static Pattern pSIXTH;
    private static Pattern pSEVENTH;
    private static Pattern pEIGHTH;
    private static Pattern pNINTH;
    private static Pattern pTENTH;
    private static Pattern pELEVENTH;
    private static Pattern pTWELFTH;
    private static Pattern pTHIRTEENTH;
    private static Pattern pFOURTEENTH;
    private static Pattern pFIFTEENTH;
    private static Pattern pSIXTEENTH;
    private static Pattern pSEVENTEENTH;
    private static Pattern pEIGHTEENTH;
    private static Pattern pNINETEENTH;
    private static Pattern pTWENTIETH;
    private static Pattern pTHIRTIETH;
    private static Pattern pJANUARY;
    private static Pattern pFEBRUARY;
    private static Pattern pMARCH;
    private static String name;
    private static String calls;
    private static String call;
    private static String cold;
    private static Pattern pTOMORROW;
    private static Pattern pTODAY;
    private static String allday;
    private static String all_day;
    private static String O__CLOCK;
    private static String O_CLOCK;
    private static String IN_THE_MORNING;
    private static String IN_THE_AFTERNOON;
    private static String IN_THE_EVENING;
    private static String _MORNING;
    private static String _EVENING;
    private static String _NIGHT;
    private static String _AFTERNOON;
    private static String FIVE_TO;
    private static String TEN_TO;
    private static Pattern pAPRIL;
    private static Pattern pMAY;
    private static Pattern pJUNE;
    private static Pattern pJULY;
    private static Pattern pAUGUST;
    private static Pattern pSEPTEMBER;
    private static Pattern pOCTOBER;
    private static Pattern pNOVEMBER;
    private static Pattern pDECEMBER;
    private static Pattern pMONDAY;
    private static Pattern pTUESDAY;
    private static Pattern pWEDNESDAY;
    private static Pattern pTHURSDAY;
    private static Pattern pFRIDAY;
    private static Pattern pSATURDAY;
    private static Pattern pSUNDAY;
    private static Pattern pCalled;
    private static Pattern pNamed;
    private static Pattern pNames;
    private static Pattern pName;
    private static Pattern pCalls;
    private static Pattern pCall;
    private static Pattern pCold;
    private static String called;
    private static String named;
    private static String names;
    private static String TENTH;
    private static String ELEVENTH;
    private static String TWELFTH;
    private static String minute;
    private static String hour;
    private static String NUMERIC_TEN_TO;
    private static String QUARTER_TO;
    private static String TWENTY_TO;
    private static String NUMERIC_TWENTY_FIVE_TO;
    private static String TWENTY_FIVE_TO;
    private static String HALF_PAST;
    private static String TWENTY_FIVE_PAST;
    private static String TWENTY_NUMERIC_FIVE_PAST;
    private static String NUMERIC_TWENTY_FIVE_PAST;
    private static String TWENTY_PAST;
    private static String NUMERIC_TWENTY_PAST;
    private static String QUARTER_PAST;
    private static String TEN_PAST;
    private static String NUMERIC_TEN_PAST;
    private static String FIVE_PAST;
    private static String NUMERIC_FIVE_PAST;
    private static String OF_THE_;
    private static String FIRST;
    private static String SECOND;
    private static String THIRD;
    private static String FOURTH;
    private static String FIFTH;
    private static String SIXTH;
    private static String SEVENTH;
    private static String EIGHTH;
    private static String NINTH;

    private static final Pattern p3Digits = Pattern.compile(".*\\b(\\d{3})\\b.*");
    private static final Pattern p4Digits = Pattern.compile(".*\\b(\\d{4})\\b.*");
    private static final Pattern p3DigitsGap = Pattern.compile(".*\\b(\\d\\s\\d{2})\\b.*");
    private static final Pattern p4DigitsGap = Pattern.compile(".*\\b(\\d{2}\\s\\d{2})\\b.*");
    private static final Pattern p1DigitAMNS = Pattern.compile(".*\\b\\dam\\b.*");
    private static final Pattern p2DigitAMNS = Pattern.compile(".*\\b\\d\\dam\\b.*");
    private static final Pattern p1DigitPMNS = Pattern.compile(".*\\b\\dpm\\b.*");
    private static final Pattern p2DigitPMNS = Pattern.compile(".*\\b\\d\\dpm\\b.*");
    private static final Pattern p1DigitAMDOT = Pattern.compile(".*\\b\\d[a.m.]\\b.*");
    private static final Pattern p2DigitAMDOT = Pattern.compile(".*\\b\\d\\d[a.m.]\\b.*");
    private static final Pattern p1DigitPMDOT = Pattern.compile(".*\\b\\d[p.m.]\\b.*");
    private static final Pattern p2DigitPMDOT = Pattern.compile(".*\\b\\d\\d[p.m.]\\b.*");
    private static final Pattern p1DigitSAMDOT = Pattern.compile(".*\\b\\d\\s[a.m.]\\b.*");
    private static final Pattern p2DigitSAMDOT = Pattern.compile(".*\\b\\d\\d\\s[a.m.]\\b.*");
    private static final Pattern p1DigitSPMDOT = Pattern.compile(".*\\b\\d\\s[p.m.]\\b.*");
    private static final Pattern p2DigitSPMDOT = Pattern.compile(".*\\b\\d\\d\\s[p.m.]\\b.*");
    private static final Pattern p1DigitAM = Pattern.compile(".*\\b\\d\\sam\\b.*");
    private static final Pattern p1DigitASM = Pattern.compile(".*\\b\\d\\sa\\sm\\b.*");
    private static final Pattern p2DigitAM = Pattern.compile(".*\\b\\d\\d\\sam\\b.*");
    private static final Pattern p2DigitASM = Pattern.compile(".*\\b\\d\\d\\sa\\sm\\b.*");
    private static final Pattern p1DigitPM = Pattern.compile(".*\\b\\d\\spm\\b.*");
    private static final Pattern p1DigitPSM = Pattern.compile(".*\\b\\d\\sp\\sm\\b.*");
    private static final Pattern p2DigitPM = Pattern.compile(".*\\b\\d\\d\\spm\\b.*");
    private static final Pattern p2DigitPSM = Pattern.compile(".*\\b\\d\\d\\sp\\sm\\b.*");
    private static final Pattern pAM = Pattern.compile(".*\\bam\\b.*");
    private static final Pattern pPM = Pattern.compile(".*\\bpm\\b.*");
    private static final Pattern pAMDOT = Pattern.compile(".*\\ba[.m.]\\b.*");
    private static final Pattern pPMDOT = Pattern.compile(".*\\bp[.m.]\\b.*");
    private static final Pattern pAM1DDOT = Pattern.compile(".*\\b\\da[.m.]\\b.*");
    private static final Pattern pPM1DDOT = Pattern.compile(".*\\b\\dp[.m.]\\b.*");
    private static final Pattern pAM2DDOT = Pattern.compile(".*\\b\\d\\da[.m.]\\b.*");
    private static final Pattern pPM2DDOT = Pattern.compile(".*\\b\\d\\dp[.m.]\\b.*");
    private static final Pattern pAMS = Pattern.compile(".*\\ba\\sm\\b.*");
    private static final Pattern pPMS = Pattern.compile(".*\\bp\\sm\\b.*");
    private static final Pattern pAM1D = Pattern.compile(".*\\b\\dam\\b.*");
    private static final Pattern pPM1D = Pattern.compile(".*\\b\\dpm\\b.*");
    private static final Pattern pPM2D = Pattern.compile(".*\\b\\d\\dpm\\b.*");
    private static final Pattern pAM2D = Pattern.compile(".*\\b\\d\\dam\\b.*");

    private static final Pattern p1st = Pattern.compile(".*\\b1st\\b.*");
    private static final Pattern p2nd = Pattern.compile(".*\\b2nd\\b.*");
    private static final Pattern p3rd = Pattern.compile(".*\\b3rd\\b.*");
    private static final Pattern p4th = Pattern.compile(".*\\b4th\\b.*");
    private static final Pattern p5th = Pattern.compile(".*\\b5th\\b.*");
    private static final Pattern p6th = Pattern.compile(".*\\b6th\\b.*");
    private static final Pattern p7th = Pattern.compile(".*\\b7th\\b.*");
    private static final Pattern p8th = Pattern.compile(".*\\b8th\\b.*");
    private static final Pattern p9th = Pattern.compile(".*\\b9th\\b.*");
    private static final Pattern p10th = Pattern.compile(".*\\b10th\\b.*");
    private static final Pattern p11th = Pattern.compile(".*\\b11th\\b.*");
    private static final Pattern p12th = Pattern.compile(".*\\b12th\\b.*");
    private static final Pattern p13th = Pattern.compile(".*\\b13th\\b.*");
    private static final Pattern p14th = Pattern.compile(".*\\b14th\\b.*");
    private static final Pattern p15th = Pattern.compile(".*\\b15th\\b.*");
    private static final Pattern p16th = Pattern.compile(".*\\b16th\\b.*");
    private static final Pattern p17th = Pattern.compile(".*\\b17th\\b.*");
    private static final Pattern p18th = Pattern.compile(".*\\b18th\\b.*");
    private static final Pattern p19th = Pattern.compile(".*\\b19th\\b.*");
    private static final Pattern p20th = Pattern.compile(".*\\b20th\\b.*");
    private static final Pattern p21st = Pattern.compile(".*\\b21st\\b.*");
    private static final Pattern p22nd = Pattern.compile(".*\\b22nd\\b.*");
    private static final Pattern p23rd = Pattern.compile(".*\\b23rd\\b.*");
    private static final Pattern p24th = Pattern.compile(".*\\b24th\\b.*");
    private static final Pattern p25th = Pattern.compile(".*\\b25th\\b.*");
    private static final Pattern p26th = Pattern.compile(".*\\b26th\\b.*");
    private static final Pattern p27th = Pattern.compile(".*\\b27th\\b.*");
    private static final Pattern p28th = Pattern.compile(".*\\b28th\\b.*");
    private static final Pattern p29th = Pattern.compile(".*\\b29th\\b.*");
    private static final Pattern p30th = Pattern.compile(".*\\b30th\\b.*");
    private static final Pattern p31st = Pattern.compile(".*\\b31st\\b.*");
    private static final Pattern p2020 = Pattern.compile(".*\\b2020\\b.*");
    private static final Pattern p2021 = Pattern.compile(".*\\b2021\\b.*");
    private static final Pattern p2022 = Pattern.compile(".*\\b2022\\b.*");
    private static final Pattern p2023 = Pattern.compile(".*\\b2023\\b.*");
    private static final Pattern p2024 = Pattern.compile(".*\\b2024\\b.*");
    private static final Pattern p2025 = Pattern.compile(".*\\b2025\\b.*");
    private static final Pattern p2026 = Pattern.compile(".*\\b2026\\b.*");
    private static final Pattern p2027 = Pattern.compile(".*\\b2027\\b.*");
    private static final Pattern p2028 = Pattern.compile(".*\\b2028\\b.*");
    private static final Pattern p2029 = Pattern.compile(".*\\b2029\\b.*");
    private static final Pattern p2030 = Pattern.compile(".*\\b2030\\b.*");
    private static final Pattern p2031 = Pattern.compile(".*\\b2031\\b.*");
    private static final Pattern p2032 = Pattern.compile(".*\\b2032\\b.*");
    private static final Pattern p2033 = Pattern.compile(".*\\b2033\\b.*");
    private static final Pattern p2034 = Pattern.compile(".*\\b2034\\b.*");

    public static CalendarProcess resolve(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        calendarProcess = new CalendarProcess();
        if (pJANUARY == null || NUMERIC_FIVE_PAST == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "resolveCalendar: initialising strings");
            }
            initStrings(context);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "resolveCalendar: strings initialised");
        }
        String str;
        String structuredString = "";
        boolean hasTime = false;
        for (int i = 0, size = voiceData.size(); i < size; ++i) {
            String vdLower = voiceData.get(i).toLowerCase(supportedLanguage.getLocale());
            if (DEBUG) {
                MyLog.d(CLS_NAME, "calLoop: " + vdLower);
            }
            str = vdLower.replaceAll(":", "");
            if (checkAlarmStructure(str)) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "checkAlarmStructure: true");
                }
                calendarProcess.haveHourOrMinute = true;
                if (DEBUG) {
                    MyLog.getElapsed(CLS_NAME, then);
                }
                return calendarProcess;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "checkAlarmStructure: false");
            }
            if (hasTime(context, str)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "isStructured: true");
                }
                structuredString = str;
                hasTime = true;
            }
        }
        if (!hasTime) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "hasStructure: FALSE");
            }
            calendarProcess.outcome = Outcome.FAILURE;
            calendarProcess.utterance = context.getString(R.string.calendar_error_structure);
            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, then);
            }
            return calendarProcess;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "hasStructure: true");
        }
        if (calendarProcess.isPM && calendarProcess.hourOfDay < 13) {
            increaseTwelveHours(calendarProcess.hourOfDay);
        }
        final String minuteString = calendarProcess.minute < 10 ? "0" + calendarProcess.minute : String.valueOf(calendarProcess.minute);
        if (calendarProcess.isAM && calendarProcess.hourOfDay == 12) {
            calendarProcess.hourOfDay = 0;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "calendarProcess.haveHour: " + calendarProcess.haveHour);
            MyLog.d(CLS_NAME, "calendarProcess.haveMinute: " + calendarProcess.haveMinute);
            MyLog.d(CLS_NAME, "calendarProcess.haveWeekday: " + calendarProcess.haveWeekday);
            MyLog.d(CLS_NAME, "calendarProcess.haveDate: " + calendarProcess.haveDate);
            MyLog.d(CLS_NAME, "calendarProcess.haveMonth: " + calendarProcess.haveMonth);
            MyLog.d(CLS_NAME, "calendarProcess.haveYear: " + calendarProcess.haveYear);
            MyLog.d(CLS_NAME, "calendarProcess.am: " + calendarProcess.isAM);
            MyLog.d(CLS_NAME, "calendarProcess.pm: " + calendarProcess.isPM);
            MyLog.d(CLS_NAME, "calendarProcess.hourInt: " + calendarProcess.hourOfDay);
            MyLog.d(CLS_NAME, "calendarProcess.minuteInt: " + calendarProcess.minute);
            MyLog.d(CLS_NAME, "calendarProcess.monthInt: " + calendarProcess.month);
            MyLog.d(CLS_NAME, "calendarProcess.dateInt: " + calendarProcess.dayOfMonth);
            MyLog.d(CLS_NAME, "calendarProcess.year: " + calendarProcess.year);
            MyLog.d(CLS_NAME, "calendarProcess.allDay: " + calendarProcess.allDay);
            MyLog.d(CLS_NAME, "getName: " + getName(structuredString, supportedLanguage.getLocale()));
            MyLog.d(CLS_NAME, "endTime: " + (calendarProcess.hourOfDay + 1) + ":" + calendarProcess.minute);
        }
        if (!calendarProcess.extraWeekdayDescription.isEmpty()) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "result: " + calendarProcess.dayOfMonth + "/" + calendarProcess.month + "/" + calendarProcess.year + calendarProcess.extraWeekdayDescription);
            }
            calendarProcess.outcome = Outcome.FAILURE;
            calendarProcess.utterance = context.getString(R.string.that_date_is) + calendarProcess.extraWeekdayDescription;
            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, then);
            }
            return calendarProcess;
        }
        if (calendarProcess.allDay) {
            if (CalendarHelper.setEvent(context, calendarProcess.year, calendarProcess.month - MONTH_OFFSET, calendarProcess.dayOfMonth, calendarProcess.hourOfDay, calendarProcess.minute, getName(structuredString, supportedLanguage.getLocale()), true)) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "result: " + calendarProcess.dayOfMonth + "/" + calendarProcess.month + "/" + calendarProcess.year + " : All Day : " + getName(structuredString, supportedLanguage.getLocale()));
                }
                calendarProcess.utterance = PersonalityResponse.getGenericAcknowledgement(context, supportedLanguage);
            } else {
                calendarProcess.outcome = Outcome.FAILURE;
                calendarProcess.utterance = context.getString(R.string.calendar_error_insert);
            }
        } else if (CalendarHelper.setEvent(context, calendarProcess.year, calendarProcess.month - MONTH_OFFSET, calendarProcess.dayOfMonth, calendarProcess.hourOfDay, calendarProcess.minute, getName(structuredString, supportedLanguage.getLocale()), false)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "result: " + calendarProcess.dayOfMonth + "/" + calendarProcess.month + "/" + calendarProcess.year + " : " + calendarProcess.hourOfDay + ":" + minuteString + " : " + getName(structuredString, supportedLanguage.getLocale()));
            }
            calendarProcess.utterance = PersonalityResponse.getGenericAcknowledgement(context, supportedLanguage);
        } else {
            calendarProcess.outcome = Outcome.FAILURE;
            calendarProcess.utterance = context.getString(R.string.calendar_error_insert);
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return calendarProcess;
    }

    private static String getName(String str, Locale locale) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "getName: " + str);
        }
        String[] separated = {""};
        if (pCalled.matcher(str).matches()) {
            separated = str.split(called);
        } else if (pCall.matcher(str).matches()) {
            separated = str.split(call);
        } else if (pCalls.matcher(str).matches()) {
            separated = str.split(calls);
        } else if (pNamed.matcher(str).matches()) {
            separated = str.split(named);
        } else if (pName.matcher(str).matches()) {
            separated = str.split(name);
        } else if (pNames.matcher(str).matches()) {
            separated = str.split(names);
        } else if (pCold.matcher(str).matches()) {
            separated = str.split(cold);
        }
        if (separated.length > 1) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "cs length: " + separated.length);
                MyLog.d(CLS_NAME, "cs0: " + separated[0]);
                MyLog.d(CLS_NAME, "cs1: " + separated[1]);
            }
            if (!separated[1].trim().isEmpty()) {
                return ai.saiy.android.utils.UtilsString.convertProperCase(separated[1].trim(), locale);
            }
        }
        return "";
    }

    private static void compareTime() {
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
        final int hour = calendar.get(Calendar.HOUR);
        if (calendarProcess.hourOfDay < hour) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "compareTime: calendarProcess.hourInt < thisHour: TOMORROW");
            }
            calendar.add(Calendar.DAY_OF_YEAR, 7);
            calendarProcess.month = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
            calendarProcess.dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveDate = true;
            calendarProcess.haveYear = true;
            calendarProcess.haveMonth = true;
            return;
        }
        if (calendarProcess.hourOfDay > hour) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "compareTime: calendarProcess.hourInt < thisHour: TODAY");
            }
            calendarProcess.month = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
            calendarProcess.dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveDate = true;
            calendarProcess.haveYear = true;
            calendarProcess.haveMonth = true;
            return;
        }
        final int minute = calendar.get(Calendar.MINUTE);
        if (calendarProcess.minute < minute) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "compareTime: calendarProcess.minuteInt < thisMinute: TOMORROW");
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendarProcess.month = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
            calendarProcess.dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveDate = true;
            calendarProcess.haveYear = true;
            calendarProcess.haveMonth = true;
            return;
        }
        if (calendarProcess.minute > minute) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "compareTime: calendarProcess.minuteInt > thisMinute: TODAY");
            }
            calendarProcess.month = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
            calendarProcess.dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveDate = true;
            calendarProcess.haveYear = true;
            calendarProcess.haveMonth = true;
        }
    }

    private static void getWeekday(final int weekday) {
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
        final int thisWeekday = calendar.get(Calendar.DAY_OF_WEEK);
        if (weekday < thisWeekday) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getWeekday: dotw < weekday");
            }
            calendar.add(Calendar.DAY_OF_YEAR, (Calendar.SATURDAY - thisWeekday) + weekday);
            calendarProcess.month = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
            calendarProcess.dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveDate = true;
            calendarProcess.haveYear = true;
            calendarProcess.haveMonth = true;
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getWeekday: calendarProcess.monthInt: " + calendarProcess.month);
                MyLog.d(CLS_NAME, "getWeekday: calendarProcess.dateInt: " + calendarProcess.dayOfMonth);
                MyLog.d(CLS_NAME, "getWeekday: calendarProcess.year: " + calendarProcess.year);
            }
            return;
        } else if (weekday > thisWeekday) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getWeekday: dotw > weekday");
            }
            calendar.add(Calendar.DAY_OF_YEAR, weekday - thisWeekday);
            calendarProcess.month = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
            calendarProcess.dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveDate = true;
            calendarProcess.haveYear = true;
            calendarProcess.haveMonth = true;
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getWeekday: calendarProcess.monthInt: " + calendarProcess.month);
                MyLog.d(CLS_NAME, "getWeekday: calendarProcess.dateInt: " + calendarProcess.dayOfMonth);
                MyLog.d(CLS_NAME, "getWeekday: calendarProcess.year: " + calendarProcess.year);
            }
            return;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "getWeekday: else: compareTime");
        }
        compareTime();
    }

    private static void initStrings(Context context) {
        pJANUARY = Pattern.compile(".*\\b" + context.getString(R.string.JANUARY) + "\\b.*");
        pFEBRUARY = Pattern.compile(".*\\b" + context.getString(R.string.FEBRUARY) + "\\b.*");
        pMARCH = Pattern.compile(".*\\b" + context.getString(R.string.MARCH) + "\\b.*");
        pAPRIL = Pattern.compile(".*\\b" + context.getString(R.string.APRIL) + "\\b.*");
        pMAY = Pattern.compile(".*\\b" + context.getString(R.string.MAY) + "\\b.*");
        pJUNE = Pattern.compile(".*\\b" + context.getString(R.string.JUNE) + "\\b.*");
        pJULY = Pattern.compile(".*\\b" + context.getString(R.string.JULY) + "\\b.*");
        pAUGUST = Pattern.compile(".*\\b" + context.getString(R.string.AUGUST) + "\\b.*");
        pSEPTEMBER = Pattern.compile(".*\\b" + context.getString(R.string.SEPTEMBER) + "\\b.*");
        pOCTOBER = Pattern.compile(".*\\b" + context.getString(R.string.OCTOBER) + "\\b.*");
        pNOVEMBER = Pattern.compile(".*\\b" + context.getString(R.string.NOVEMBER) + "\\b.*");
        pDECEMBER = Pattern.compile(".*\\b" + context.getString(R.string.DECEMBER) + "\\b.*");
        final String MONDAY = context.getString(R.string.MONDAY);
        final String TUESDAY = context.getString(R.string.TUESDAY);
        final String WEDNESDAY = context.getString(R.string.WEDNESDAY);
        final String THURSDAY = context.getString(R.string.THURSDAY);
        final String FRIDAY = context.getString(R.string.FRIDAY);
        final String SATURDAY = context.getString(R.string.SATURDAY);
        final String SUNDAY = context.getString(R.string.SUNDAY);
        pMONDAY = Pattern.compile(".*\\b" + MONDAY + "\\b.*");
        pTUESDAY = Pattern.compile(".*\\b" + TUESDAY + "\\b.*");
        pWEDNESDAY = Pattern.compile(".*\\b" + WEDNESDAY + "\\b.*");
        pTHURSDAY = Pattern.compile(".*\\b" + THURSDAY + "\\b.*");
        pFRIDAY = Pattern.compile(".*\\b" + FRIDAY + "\\b.*");
        pSATURDAY = Pattern.compile(".*\\b" + SATURDAY + "\\b.*");
        pSUNDAY = Pattern.compile(".*\\b" + SUNDAY + "\\b.*");
        minute = context.getString(R.string.minute);
        hour = context.getString(R.string.hour);
        called = context.getString(R.string.called);
        named = context.getString(R.string.named);
        names = context.getString(R.string.names);
        name = context.getString(R.string.name);
        calls = context.getString(R.string.calls);
        call = context.getString(R.string.call);
        cold = context.getString(R.string.cold);
        pCalled = Pattern.compile(".*\\b" + called + "\\b.*");
        pNamed = Pattern.compile(".*\\b" + named + "\\b.*");
        pNames = Pattern.compile(".*\\b" + names + "\\b.*");
        pName = Pattern.compile(".*\\b" + name + "\\b.*");
        pCalls = Pattern.compile(".*\\b" + calls + "\\b.*");
        pCall = Pattern.compile(".*\\b" + call + "\\b.*");
        pCold = Pattern.compile(".*\\b" + cold + "\\b.*");
        FIRST = context.getString(R.string.FIRST);
        SECOND = context.getString(R.string.SECOND);
        THIRD = context.getString(R.string.THIRD);
        FOURTH = context.getString(R.string.FOURTH);
        FIFTH = context.getString(R.string.FIFTH);
        SIXTH = context.getString(R.string.SIXTH);
        SEVENTH = context.getString(R.string.SEVENTH);
        EIGHTH = context.getString(R.string.EIGHTH);
        NINTH = context.getString(R.string.NINTH);
        TENTH = context.getString(R.string.TENTH);
        ELEVENTH = context.getString(R.string.ELEVENTH);
        TWELFTH = context.getString(R.string.TWELFTH);
        pFIRST = Pattern.compile(".*\\b" + FIRST + "\\b.*");
        pSECOND = Pattern.compile(".*\\b" + SECOND + "\\b.*");
        pTHIRD = Pattern.compile(".*\\b" + THIRD + "\\b.*");
        pFOURTH = Pattern.compile(".*\\b" + FOURTH + "\\b.*");
        pFIFTH = Pattern.compile(".*\\b" + FIFTH + "\\b.*");
        pSIXTH = Pattern.compile(".*\\b" + SIXTH + "\\b.*");
        pSEVENTH = Pattern.compile(".*\\b" + SEVENTH + "\\b.*");
        pEIGHTH = Pattern.compile(".*\\b" + EIGHTH + "\\b.*");
        pNINTH = Pattern.compile(".*\\b" + NINTH + "\\b.*");
        pTENTH = Pattern.compile(".*\\b" + TENTH + "\\b.*");
        pELEVENTH = Pattern.compile(".*\\b" + ELEVENTH + "\\b.*");
        pTWELFTH = Pattern.compile(".*\\b" + TWELFTH + "\\b.*");
        pTHIRTEENTH = Pattern.compile(".*\\b" + context.getString(R.string.THIRTEENTH) + "\\b.*");
        pFOURTEENTH = Pattern.compile(".*\\b" + context.getString(R.string.FOURTEENTH) + "\\b.*");
        pFIFTEENTH = Pattern.compile(".*\\b" + context.getString(R.string.FIFTEENTH) + "\\b.*");
        pSIXTEENTH = Pattern.compile(".*\\b" + context.getString(R.string.SIXTEENTH) + "\\b.*");
        pSEVENTEENTH = Pattern.compile(".*\\b" + context.getString(R.string.SEVENTEENTH) + "\\b.*");
        pEIGHTEENTH = Pattern.compile(".*\\b" + context.getString(R.string.EIGHTEENTH) + "\\b.*");
        pNINETEENTH = Pattern.compile(".*\\b" + context.getString(R.string.NINETEENTH) + "\\b.*");
        pTWENTIETH = Pattern.compile(".*\\b" + context.getString(R.string.TWENTIETH) + "\\b.*");
        pTHIRTIETH = Pattern.compile(".*\\b" + context.getString(R.string.THIRTIETH) + "\\b.*");
        pTODAY = Pattern.compile(".*\\b" + context.getString(R.string.TODAY) + "\\b.*");
        pTOMORROW = Pattern.compile(".*\\b" + context.getString(R.string.TOMORROW) + "\\b.*");
        _MORNING = context.getString(R.string._MORNING);
        _EVENING = context.getString(R.string._EVENING);
        _NIGHT = context.getString(R.string._NIGHT);
        _AFTERNOON = context.getString(R.string._AFTERNOON);
        allday = context.getString(R.string.allday);
        all_day = context.getString(R.string.all_day);
        OF_THE_ = context.getString(R.string.OF_THE_);
        O__CLOCK = context.getString(R.string.O__CLOCK);
        O_CLOCK = context.getString(R.string.O_CLOCK);
        IN_THE_MORNING = context.getString(R.string.IN_THE_MORNING);
        IN_THE_AFTERNOON = context.getString(R.string.IN_THE_AFTERNOON);
        IN_THE_EVENING = context.getString(R.string.IN_THE_EVENING);
        FIVE_TO = context.getString(R.string.FIVE_TO);
        TEN_TO = context.getString(R.string.TEN_TO);
        NUMERIC_TEN_TO = context.getString(R.string.NUMERIC_TEN_TO);
        QUARTER_TO = context.getString(R.string.QUARTER_TO);
        TWENTY_TO = context.getString(R.string.TWENTY_TO);
        NUMERIC_TWENTY_FIVE_TO = context.getString(R.string.NUMERIC_TWENTY_FIVE_TO);
        TWENTY_FIVE_TO = context.getString(R.string.TWENTY_FIVE_TO);
        HALF_PAST = context.getString(R.string.HALF_PAST);
        TWENTY_FIVE_PAST = context.getString(R.string.TWENTY_FIVE_PAST);
        TWENTY_NUMERIC_FIVE_PAST = context.getString(R.string.TWENTY_NUMERIC_FIVE_PAST);
        NUMERIC_TWENTY_FIVE_PAST = context.getString(R.string.NUMERIC_TWENTY_FIVE_PAST);
        TWENTY_PAST = context.getString(R.string.TWENTY_PAST);
        NUMERIC_TWENTY_PAST = context.getString(R.string.NUMERIC_TWENTY_PAST);
        QUARTER_PAST = context.getString(R.string.QUARTER_PAST);
        TEN_PAST = context.getString(R.string.TEN_PAST);
        NUMERIC_TEN_PAST = context.getString(R.string.NUMERIC_TEN_PAST);
        FIVE_PAST = context.getString(R.string.FIVE_PAST);
        NUMERIC_FIVE_PAST = context.getString(R.string.NUMERIC_FIVE_PAST);
    }

    private static boolean hasTime(Context context, String str) {
        calendarProcess.haveYear = isYear(str);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "calendarProcess.haveYear: " + calendarProcess.haveYear);
        }
        if (!dissectHourAndMinute(str)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: false");
            }
            return false;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "hasTime: TRUE");
        }
        calendarProcess.haveMonth = isMonth(context, str);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "calendarProcess.haveMonth: " + calendarProcess.haveMonth);
        }
        calendarProcess.haveDate = isDate(str);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "calendarProcess.haveDate: " + calendarProcess.haveDate);
        }
        calendarProcess.haveWeekday = isWeekday(str);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "calendarProcess.haveWeekday: " + calendarProcess.haveWeekday);
        }
        if (!calendarProcess.haveMonth && !calendarProcess.haveDate && !calendarProcess.haveWeekday) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isMonth: isDate: isWeekday: FALSE");
            }
            getCurrentDate();
            return validateDate(context);
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "isMonth: or isDate: or isWeekday: true");
        }
        if (!calendarProcess.haveWeekday) {
            if (calendarProcess.haveDate && !calendarProcess.haveMonth) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "isMonth: or isDate: or isWeekday: TRUE : isDate: TRUE: isMonth: FALSE");
                }
                getMonth(calendarProcess.dayOfMonth);
                return validateDate(context);
            }
            if (!calendarProcess.haveMonth || calendarProcess.haveDate) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "isMonth: or isDate: or isWeekday: TRUE : isDate: TRUE: isMonth: TRUE");
                }
                return validateDate(context);
            }
            if (!DEBUG) {
                return false;
            }
            MyLog.i(CLS_NAME, "isMonth: or isDate: or isWeekday: TRUE : isMonth: TRUE: isDate: FALSE");
            return false;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "isMonth: or isDate: or isWeekday: TRUE : isWeekday: TRUE");
        }
        if (!calendarProcess.haveMonth && !calendarProcess.haveDate) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isMonth: or isDate: or isWeekday: TRUE : isWeekday: TRUE: isMonth: FALSE: isDate: FALSE");
            }
            return validateDate(context);
        }
        if (!calendarProcess.haveMonth) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isMonth: or isDate: or isWeekday: TRUE : isWeekday: TRUE: isMonth: FALSE: isDate: TRUE");
            }
            getMonth(calendarProcess.dayOfMonth);
            return validateDate(context);
        }
        if (calendarProcess.haveDate) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isMonth: or isDate: or isWeekday: TRUE : isWeekday: TRUE: isMonth: TRUE: isDate: TRUE");
            }
            return validateDate(context);
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "isMonth: or isDate: or isWeekday: TRUE : isWeekday: TRUE: isMonth: TRUE: isDate: FALSE");
        }
        return false;
    }

    private static boolean hasHourAndMinute(String str, String delimiter, boolean less) {
        String[] separated = str.split(delimiter);
        if (separated.length <= 0) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectTime remove = 0 ");
            }
            return false;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "remove length: " + separated.length);
            MyLog.d(CLS_NAME, "remove0: " + separated[0]);
            MyLog.d(CLS_NAME, "remove1: " + separated[1]);
        }
        String hourString = separated[1];
        if (DEBUG) {
            MyLog.d(CLS_NAME, "dissectTime hour: " + hourString);
        }
        separated = hourString.split(XMLResultsHandler.SEP_SPACE);
        if (separated.length <= 0) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectTime number = 0 ");
            }
            return false;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "dissect length: " + separated.length);
            MyLog.d(CLS_NAME, "dissect0: " + separated[0]);
            MyLog.d(CLS_NAME, "dissect1: " + separated[1]);
        }
        final String number = separated[1];
        if (DEBUG) {
            MyLog.d(CLS_NAME, "dissectTime number: " + number);
        }
        if (!number.matches("[0-9]+")) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectTime number.matches 0-9+: false" + number);
            }
            return false;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "dissectTime number.matches 0-9+: " + number);
        }
        try {
            final int hour = Integer.parseInt(number);
            if (hour <= 0 || hour >= 25) {
                return false;
            }
            if (less) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "dissectTime less: true");
                }
                calendarProcess.hourOfDay = hour - 1;
            } else {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "dissectTime less: false");
                }
                calendarProcess.hourOfDay = hour;
            }
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectTime: calendarProcess.hourInt: " + calendarProcess.hourOfDay);
            }
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectTime: calendarProcess.minuteInt: " + calendarProcess.minute);
            }
            if (calendarProcess.hourOfDay < 25 && calendarProcess.minute < 60) {
                hasAMPM(str);
                return true;
            }
            if (DEBUG) {
                MyLog.w(CLS_NAME, "dissectHourMin: out of bounds");
            }
            return isAllDay(str);
        } catch (NumberFormatException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "number not numeric");
            }
            return false;
        }
    }

    private static boolean hasAMPM(String str) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "in hasAMPM");
        }
        calendarProcess.isAM = false;
        calendarProcess.isPM = false;
        if (pAM.matcher(str).matches() && pPM.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "hasAMPM: BOTH");
            }
            return false;
        }
        if (pAM.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pAM: AM");
            }
            calendarProcess.isAM = true;
            return true;
        }
        if (pPM.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pPM: PM");
            }
            calendarProcess.isPM = true;
            return true;
        }
        if (pPMDOT.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pPMDOT: PM");
            }
            calendarProcess.isPM = true;
            return true;
        }
        if (pPMS.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pPMS: PM");
            }
            calendarProcess.isPM = true;
            return true;
        }
        if (pPM1D.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pPM1D: PM");
            }
            calendarProcess.isPM = true;
            return true;
        }
        if (pPM2D.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pPM2D: PM");
            }
            calendarProcess.isPM = true;
            return true;
        }
        if (pAMDOT.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pAMDOT : AM");
            }
            calendarProcess.isAM = true;
            return true;
        }
        if (pAMS.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pAMS: AM");
            }
            calendarProcess.isAM = true;
            return true;
        }
        if (pAM1D.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pAM1D: AM");
            }
            calendarProcess.isAM = true;
            return true;
        }
        if (pAM2D.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pAM2D: AM");
            }
            calendarProcess.isAM = true;
            return true;
        }
        if (pAM1DDOT.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pAM1DDOT: AM");
            }
            calendarProcess.isAM = true;
            return true;
        }
        if (pAM2DDOT.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pAM2DDOT: AM");
            }
            calendarProcess.isAM = true;
            return true;
        }
        if (pPM1DDOT.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pPM1DDOT: PM");
            }
            calendarProcess.isPM = true;
            return true;
        }
        if (pPM2DDOT.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pPM2DDOT: PM");
            }
            calendarProcess.isPM = true;
            return true;
        }
        if (str.contains(_MORNING)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: morning");
            }
            calendarProcess.isAM = true;
            return false;
        }
        if (str.contains(_AFTERNOON)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: afternoon");
            }
            calendarProcess.isPM = true;
            return false;
        }
        if (str.contains(_EVENING)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: evening");
            }
            calendarProcess.isPM = true;
            return false;
        }
        if (!str.contains(_NIGHT)) {
            return false;
        }
        if (DEBUG) {
            MyLog.v(CLS_NAME, "hasAMPM: night");
        }
        calendarProcess.isPM = true;
        return false;
    }

    private static void getCurrentDate() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
        calendarProcess.dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        calendarProcess.month = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
        calendarProcess.year = calendar.get(Calendar.YEAR);
        if (DEBUG) {
            MyLog.d(CLS_NAME, "getCurrentDate: calendarProcess.dateInt: " + calendarProcess.dayOfMonth);
            MyLog.d(CLS_NAME, "getCurrentDate: calendarProcess.monthInt: " + calendarProcess.month);
            MyLog.d(CLS_NAME, "getCurrentDate: calendarProcess.year: " + calendarProcess.year);
        }
    }

    private static void getYear(int month) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getYear: moty: " + month);
        }
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
        final int thisMonth = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getYear: thisMonth: " + thisMonth);
        }
        if (month < thisMonth) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getYear: moty < thisMonth");
            }
            calendar.add(Calendar.YEAR, 1);
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveYear = true;
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getYear: calendarProcess.monthInt: " + calendarProcess.month);
                MyLog.d(CLS_NAME, "getYear: calendarProcess.dateInt: " + calendarProcess.dayOfMonth);
                MyLog.d(CLS_NAME, "getYear: calendarProcess.year: " + calendarProcess.year);
                return;
            }
            return;
        }
        if (month > thisMonth) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getYear: moty > thisMonth");
            }
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveYear = true;
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getYear: calendarProcess.year: " + calendarProcess.year);
            }
            return;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "getYear: else: comparing date");
        }
        if (calendarProcess.dayOfMonth < calendar.get(Calendar.DAY_OF_MONTH)) {
            calendar.add(Calendar.YEAR, 1);
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveYear = true;
            return;
        }
        if (calendarProcess.dayOfMonth > calendar.get(Calendar.DAY_OF_MONTH)) {
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveYear = true;
            return;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "getYear: else: comparing hour");
        }
        if (calendarProcess.hourOfDay > calendar.get(Calendar.HOUR)) {
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveYear = true;
            return;
        }
        if (calendarProcess.hourOfDay < calendar.get(Calendar.HOUR)) {
            calendar.add(Calendar.YEAR, 1);
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveYear = true;
            return;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "getYear: else: comparing minute");
        }
        if (calendarProcess.minute > calendar.get(Calendar.MINUTE)) {
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveYear = true;
        } else if (calendarProcess.minute < calendar.get(Calendar.MINUTE)) {
            calendar.add(Calendar.YEAR, 1);
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveYear = true;
        }
    }

    private static boolean validateDate(Context context) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "validateDate: calendarProcess.dateInt:" + calendarProcess.dayOfMonth);
            MyLog.d(CLS_NAME, "validateDate: calendarProcess.monthInt:" + calendarProcess.month);
            MyLog.d(CLS_NAME, "validateDate: calendarProcess.yearInt:" + calendarProcess.year);
        }
        if (calendarProcess.month == 0 && calendarProcess.dayOfMonth != 0) {
            getMonth(calendarProcess.dayOfMonth);
        }
        if (calendarProcess.year == 0 && calendarProcess.month != 0) {
            getYear(calendarProcess.month);
        }
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
        if (calendarProcess.year == 0) {
            calendarProcess.year = calendar.get(Calendar.YEAR);
        }
        if (calendarProcess.month == 0) {
            calendarProcess.month = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
        }
        if (calendarProcess.dayOfMonth == 0) {
            calendarProcess.dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        }
        String dayOfMonthString;
        String monthString;
        final String yearString = String.valueOf(calendarProcess.year);
        if (calendarProcess.month > 0 && calendarProcess.month < 10) {
            monthString = "0" + calendarProcess.month;
        } else {
            monthString = String.valueOf(calendarProcess.month);
        }
        if (calendarProcess.dayOfMonth > 0 && calendarProcess.dayOfMonth < 10) {
            dayOfMonthString = "0" + calendarProcess.dayOfMonth;
        } else {
            dayOfMonthString = String.valueOf(calendarProcess.dayOfMonth);
        }
        final String dateString = dayOfMonthString + monthString + yearString;
        if (DEBUG) {
            MyLog.d(CLS_NAME, "validateDate: " + dateString);
        }
        try {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy", Locale.US);
            simpleDateFormat.setLenient(false);
            simpleDateFormat.parse(dateString);
            if (calendarProcess.haveWeekday) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "validateDate: calendarProcess.haveWeekday: true");
                }
                calendar.set(Integer.parseInt(yearString), Integer.parseInt(monthString) - 1, Integer.parseInt(dayOfMonthString));
                if (calendarProcess.weekday != calendar.get(Calendar.DAY_OF_WEEK)) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "validateDate: calendarProcess.dayInt != dotw: " + calendarProcess.weekday + " : " + calendar.get(Calendar.DAY_OF_WEEK));
                    }
                    switch (calendarProcess.weekday) {
                        case 0:
                            return false;
                        case Calendar.SUNDAY:
                        case Calendar.MONTH:
                        case Calendar.TUESDAY:
                        case Calendar.WEDNESDAY:
                        case Calendar.THURSDAY:
                        case Calendar.FRIDAY:
                        case Calendar.SATURDAY:
                            calendarProcess.extraWeekdayDescription = XMLResultsHandler.SEP_SPACE + context.getString(R.string.not_a) + XMLResultsHandler.SEP_SPACE + UtilsDate.getWeekday(context, calendarProcess.weekday);
                            break;
                        default:
                            return false;
                    }
                }
            }
            if (DEBUG) {
                MyLog.v(CLS_NAME, "checkDateFormat: True");
            }
            return true;
        } catch (ParseException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "checkDateFormat ParseException");
            }
            e.printStackTrace();
            return false;
        }
    }

    private static boolean dissectHourAndMinute(String str) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "in hasTime");
        }
        if (isAllDay(str)) {
            return true;
        }
        String replace = str.replace(O__CLOCK, "00").replace(O_CLOCK, "00").replace(IN_THE_MORNING, "am").replace(IN_THE_AFTERNOON, "pm").replace(IN_THE_EVENING, "pm");
        if (p3Digits.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "dissectHourMin: threeD");
            }
            Matcher matcher = p3Digits.matcher(replace);
            if (matcher.find()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Matcher found");
                }
                String group = matcher.group(1);
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "timeDigits: " + group);
                }
                String[] separated = group.split("(?<=\\G.{1})");
                calendarProcess.hourOfDay = Integer.parseInt(separated[0]);
                calendarProcess.minute = Integer.parseInt(separated[1] + separated[2]);
            }
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectHourMin: hour: " + calendarProcess.hourOfDay);
                MyLog.d(CLS_NAME, "dissectHourMin: minute: " + calendarProcess.minute);
            }
            if (calendarProcess.hourOfDay >= 25 || calendarProcess.minute >= 60) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "dissectHourMin: out of bounds");
                }
                return false;
            }
            hasAMPM(replace);
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            return true;
        }
        if (p3DigitsGap.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "It matched spaced time! p3DigitsGap");
            }
            Matcher matcher = p3DigitsGap.matcher(replace);
            if (matcher.find()) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "Matcher found");
                }
                String group = matcher.group(1);
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "timeDigits: " + group);
                }
                String[] separated = group.split("\\s");
                calendarProcess.hourOfDay = Integer.parseInt(separated[0]);
                calendarProcess.minute = Integer.parseInt(separated[1]);
            }
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectHourMin: hour: " + calendarProcess.hourOfDay);
                MyLog.d(CLS_NAME, "dissectHourMin: minute: " + calendarProcess.minute);
            }
            if (calendarProcess.hourOfDay >= 25 || calendarProcess.minute >= 60) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "dissectHourMin: out of bounds");
                }
                return false;
            }
            hasAMPM(replace);
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            return true;
        }
        if (p4DigitsGap.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "It matched spaced time! p4DigitsGap");
            }
            Matcher matcher = p4DigitsGap.matcher(replace);
            if (matcher.find()) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "Matcher found");
                }
                String group = matcher.group(1);
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "timeDigits: " + group);
                }
                String[] separated = group.split("\\s");
                calendarProcess.hourOfDay = Integer.parseInt(separated[0]);
                calendarProcess.minute = Integer.parseInt(separated[1]);
            }
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectHourMin: hour: " + calendarProcess.hourOfDay);
                MyLog.d(CLS_NAME, "dissectHourMin: minute: " + calendarProcess.minute);
            }
            if (calendarProcess.hourOfDay >= 25 || calendarProcess.minute >= 60) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "dissectHourMin: out of bounds");
                }
                return false;
            }
            hasAMPM(replace);
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            return true;
        }
        if (p4Digits.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasTime: 4 dddd");
            }
            ArrayList<String> arrayList = new ArrayList<>();
            Matcher matcher = Pattern.compile("\\b(\\d{4})\\b").matcher(replace);
            int count = 0;
            while (matcher.find()) {
                count++;
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Matcher found count: " + count + " : " + matcher.group(1));
                }
                arrayList.add(matcher.group(1));
            }
            String timeDigits = "";
            if (arrayList.size() > 1 && calendarProcess.haveYear) {
                for (String s : arrayList) {
                    timeDigits = s;
                    if (!timeDigits.matches(String.valueOf(calendarProcess.year))) {
                        break;
                    }
                }
            } else {
                calendarProcess.haveYear = false;
                calendarProcess.year = 0;
                timeDigits = arrayList.get(0);
            }
            if (DEBUG) {
                MyLog.d(CLS_NAME, "timeDigits: " + timeDigits);
            }
            String[] separated = timeDigits.split("(?<=\\G.{2})");
            calendarProcess.hourOfDay = Integer.parseInt(separated[0]);
            calendarProcess.minute = Integer.parseInt(separated[1]);
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectHourMin: hour: " + calendarProcess.hourOfDay);
                MyLog.d(CLS_NAME, "dissectHourMin: minute: " + calendarProcess.minute);
            }
            if (calendarProcess.hourOfDay >= 25 || calendarProcess.minute >= 60) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "dissectHourMin: out of bounds");
                }
                return false;
            }
            hasAMPM(replace);
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            return true;
        }
        if (replace.contains(FIVE_TO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: five to");
            }
            if (!hasHourAndMinute(replace, FIVE_TO, true)) {
                return false;
            }
            calendarProcess.minute = 55;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(TEN_TO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: ten to");
            }
            if (!hasHourAndMinute(replace, TEN_TO, true)) {
                return false;
            }
            calendarProcess.minute = 50;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(NUMERIC_TEN_TO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: 10 to");
            }
            if (!hasHourAndMinute(replace, NUMERIC_TEN_TO, true)) {
                return false;
            }
            calendarProcess.minute = 50;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(QUARTER_TO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: quarter to");
            }
            if (!hasHourAndMinute(replace, QUARTER_TO, true)) {
                return false;
            }
            calendarProcess.minute = 45;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(TWENTY_TO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: twenty to");
            }
            if (!hasHourAndMinute(replace, TWENTY_TO, true)) {
                return false;
            }
            calendarProcess.minute = 40;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(NUMERIC_TWENTY_FIVE_TO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: 25 to");
            }
            if (!hasHourAndMinute(replace, NUMERIC_TWENTY_FIVE_TO, true)) {
                return false;
            }
            calendarProcess.minute = 35;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(TWENTY_FIVE_TO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: twenty five to");
            }
            if (!hasHourAndMinute(replace, TWENTY_FIVE_TO, true)) {
                return false;
            }
            calendarProcess.minute = 35;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(HALF_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: half past");
            }
            if (!hasHourAndMinute(replace, HALF_PAST, false)) {
                return false;
            }
            calendarProcess.minute = 30;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(TWENTY_FIVE_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: twenty five past");
            }
            if (!hasHourAndMinute(replace, TWENTY_FIVE_PAST, false)) {
                return false;
            }
            calendarProcess.minute = 25;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(TWENTY_NUMERIC_FIVE_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: twenty five past");
            }
            if (!hasHourAndMinute(replace, TWENTY_NUMERIC_FIVE_PAST, false)) {
                return false;
            }
            calendarProcess.minute = 25;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(NUMERIC_TWENTY_FIVE_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: 25 past");
            }
            if (!hasHourAndMinute(replace, NUMERIC_TWENTY_FIVE_PAST, false)) {
                return false;
            }
            calendarProcess.minute = 25;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(TWENTY_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: twenty past");
            }
            if (!hasHourAndMinute(replace, TWENTY_PAST, false)) {
                return false;
            }
            calendarProcess.minute = 20;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(NUMERIC_TWENTY_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: twenty past");
            }
            if (!hasHourAndMinute(replace, NUMERIC_TWENTY_PAST, false)) {
                return false;
            }
            calendarProcess.minute = 20;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(QUARTER_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: quarter past");
            }
            if (!hasHourAndMinute(replace, QUARTER_PAST, false)) {
                return false;
            }
            calendarProcess.minute = 15;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(TEN_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: ten past");
            }
            if (!hasHourAndMinute(replace, TEN_PAST, false)) {
                return false;
            }
            calendarProcess.minute = 10;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(NUMERIC_TEN_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: 10 past");
            }
            if (!hasHourAndMinute(replace, NUMERIC_TEN_PAST, false)) {
                return false;
            }
            calendarProcess.minute = 10;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(FIVE_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: five past");
            }
            if (!hasHourAndMinute(replace, FIVE_PAST, false)) {
                return false;
            }
            calendarProcess.minute = 5;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(NUMERIC_FIVE_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: 5 past");
            }
            if (!hasHourAndMinute(replace, NUMERIC_FIVE_PAST, false)) {
                return false;
            }
            calendarProcess.minute = 5;
            calendarProcess.haveHour = true;
            calendarProcess.haveMinute = true;
            hasAMPM(replace);
            return true;
        }
        if (p1DigitAM.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitAM");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)\\sam\\b").matcher(replace);
            if (matcher.find()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Matcher found count: " + matcher.group(1));
                }
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitAM.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitAM");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)\\sam\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitPM.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitPM");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)\\spm\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitPM.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitPM");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)\\spm\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitASM.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitASM");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)\\sa\\sm\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitASM.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitASM");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)\\sa\\sm\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitPSM.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitPSM");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)\\sp\\sm\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitPSM.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitPSM");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)\\sp\\sm\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitAMNS.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitAMNS");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)am\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitAMNS.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitAMNS");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)am\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitPMNS.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitPMNS");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)pm\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitPMNS.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitPMNS");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)pm\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitPMDOT.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitPMDOT");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)[p.m.]\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "group1: " + matcher.group(1));
                }
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitPMDOT.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitPMDOT");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)[p.m.]\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "group1: " + matcher.group(1));
                }
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitAMDOT.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitAMDOT");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)[a.m.]\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "group1: " + matcher.group(1));
                }
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitAMDOT.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitAMDOT");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)[a.m.]\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "group1: " + matcher.group(1));
                }
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitSPMDOT.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitSPMDOT");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)\\s[p.m.]\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "group1: " + matcher.group(1));
                }
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitSPMDOT.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitSPMDOT");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)\\s[p.m.]\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "group1: " + matcher.group(1));
                }
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitSAMDOT.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitSAMDOT");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)\\s[a.m.]\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "group1: " + matcher.group(1));
                }
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitSAMDOT.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitSAMDOT");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)\\s[a.m.]\\b").matcher(replace);
            if (matcher.find()) {
                calendarProcess.hourOfDay = Integer.parseInt(matcher.group(1));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "group1: " + matcher.group(1));
                }
            }
            hasAMPM(replace);
            return true;
        }
        return false;
    }

    private static boolean isAllDay(String str) {
        calendarProcess.allDay = false;
        if (!str.contains(all_day) && !str.contains(allday)) {
            return false;
        }
        if (DEBUG) {
            MyLog.v(CLS_NAME, "isAllDay: TRUE");
        }
        calendarProcess.allDay = true;
        return true;
    }

    private static void getMonth(int dayOfMonth) {
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
        final int thisDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        if (dayOfMonth < thisDayOfMonth) {
            calendar.add(Calendar.MONTH, 1);
            calendarProcess.month = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveYear = true;
            calendarProcess.haveMonth = true;
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getMonth: calendarProcess.monthInt+1: " + calendarProcess.month);
                MyLog.d(CLS_NAME, "getMonth: calendarProcess.year: " + calendarProcess.year);
            }
            return;
        }
        if (dayOfMonth > thisDayOfMonth) {
            calendarProcess.month = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveYear = true;
            calendarProcess.haveMonth = true;
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getMonth: calendarProcess.monthInt: " + calendarProcess.month);
                MyLog.d(CLS_NAME, "getMonth: calendarProcess.year: " + calendarProcess.year);
            }
            return;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "getMonth: else: compareTime");
        }
        final int hour = calendar.get(Calendar.HOUR);
        if (calendarProcess.hourOfDay > hour) {
            calendarProcess.month = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveYear = true;
            calendarProcess.haveMonth = true;
            return;
        }
        if (calendarProcess.hourOfDay < hour) {
            calendar.add(Calendar.MONTH, 1);
            calendarProcess.month = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveYear = true;
            calendarProcess.haveMonth = true;
            return;
        }
        final int minute = calendar.get(Calendar.MINUTE);
        if (calendarProcess.minute > minute) {
            calendarProcess.month = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveYear = true;
            calendarProcess.haveMonth = true;
            return;
        }
        if (calendarProcess.minute < minute) {
            calendar.add(Calendar.MONTH, 1);
            calendarProcess.month = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
            calendarProcess.year = calendar.get(Calendar.YEAR);
            calendarProcess.haveYear = true;
            calendarProcess.haveMonth = true;
        }
    }

    private static boolean isMonth(Context context, String str) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "in isMonth");
        }
        calendarProcess.haveMonth = true;
        if (pJANUARY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: pJanuary");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.JANUARY;
            return true;
        }
        if (pFEBRUARY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: pFebruary");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.FEBRUARY;
            return true;
        }
        if (pMARCH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: pMarch");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.MARCH;
            return true;
        }
        if (pAPRIL.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: pApril");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.APRIL;
            return true;
        }
        if (pMAY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: pMay");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.MAY;
            return true;
        }
        if (pJUNE.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: pJune");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.JUNE;
            return true;
        }
        if (pJULY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: pJuly");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.JULY;
            return true;
        }
        if (pAUGUST.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: pAugust");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.AUGUST;
            return true;
        }
        if (pSEPTEMBER.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: pSeptember");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.SEPTEMBER;
            return true;
        }
        if (pOCTOBER.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: pOctober");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.OCTOBER;
            return true;
        }
        if (pNOVEMBER.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: pNovember");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.NOVEMBER;
            return true;
        }
        if (pDECEMBER.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: pDecember");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.DECEMBER;
            return true;
        }
        if (str.contains(OF_THE_ + "1st") || str.contains(OF_THE_ + FIRST)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: of the 1st");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.JANUARY;
            return true;
        }
        if (str.contains(OF_THE_ + "2nd") || str.contains(OF_THE_ + SECOND)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: of the 2nd");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.FEBRUARY;
            return true;
        }
        if (str.contains(OF_THE_ + "3rd") || str.contains(OF_THE_ + THIRD)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: of the 3rd");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.MARCH;
            return true;
        }
        if (str.contains(OF_THE_ + "4th") || str.contains(OF_THE_ + FOURTH)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: of the 4th");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.APRIL;
            return true;
        }
        if (str.contains(OF_THE_ + "5th") || str.contains(OF_THE_ + FIFTH)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: of the 5th");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.MAY;
            return true;
        }
        if (str.contains(OF_THE_ + "6th") || str.contains(OF_THE_ + SIXTH)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: of the 6th");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.JUNE;
            return true;
        }
        if (str.contains(OF_THE_ + "7th") || str.contains(OF_THE_ + SEVENTH)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: of the 7th");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.JULY;
            return true;
        }
        if (str.contains(OF_THE_ + "8th") || str.contains(OF_THE_ + EIGHTH)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: of the 8th");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.AUGUST;
            return true;
        }
        if (str.contains(OF_THE_ + "9th") || str.contains(OF_THE_ + NINTH)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: of the 9th");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.SEPTEMBER;
            return true;
        }
        if (str.contains(OF_THE_ + "10th") || str.contains(OF_THE_ + TENTH)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: of the 10th");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.OCTOBER;
            return true;
        }
        if (str.contains(OF_THE_ + "11th") || str.contains(OF_THE_ + ELEVENTH)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isMonth: of the 11th");
            }
            calendarProcess.month = MONTH_OFFSET + Calendar.NOVEMBER;
            return true;
        }
        if (!str.contains(OF_THE_ + "12th") && !str.contains(OF_THE_ + TWELFTH)) {
            return false;
        }
        if (DEBUG) {
            MyLog.v(CLS_NAME, "isMonth: of the 12th");
        }
        calendarProcess.month = MONTH_OFFSET + Calendar.DECEMBER;
        return true;
    }

    private static boolean isWeekday(String str) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "in isWeekday");
        }
        if (pMONDAY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pMonday");
            }
            calendarProcess.weekday = Calendar.MONDAY;
            if (!calendarProcess.haveDate) {
                getWeekday(calendarProcess.weekday);
            }
            return true;
        }
        if (pTUESDAY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pTuesday");
            }
            calendarProcess.weekday = Calendar.TUESDAY;
            if (!calendarProcess.haveDate) {
                getWeekday(calendarProcess.weekday);
            }
            return true;
        }
        if (pWEDNESDAY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pWednesday");
            }
            calendarProcess.weekday = Calendar.WEDNESDAY;
            if (!calendarProcess.haveDate) {
                getWeekday(calendarProcess.weekday);
            }
            return true;
        }
        if (pTHURSDAY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pThursday");
            }
            calendarProcess.weekday = Calendar.THURSDAY;
            if (!calendarProcess.haveDate) {
                getWeekday(calendarProcess.weekday);
            }
            return true;
        }
        if (pFRIDAY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pFriday");
            }
            calendarProcess.weekday = Calendar.FRIDAY;
            if (!calendarProcess.haveDate) {
                getWeekday(calendarProcess.weekday);
            }
            return true;
        }
        if (pSATURDAY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pSaturday");
            }
            calendarProcess.weekday = Calendar.SATURDAY;
            if (!calendarProcess.haveDate) {
                getWeekday(calendarProcess.weekday);
            }
            return true;
        }
        if (pSUNDAY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pSunday");
            }
            calendarProcess.weekday = Calendar.SUNDAY;
            if (!calendarProcess.haveDate) {
                getWeekday(calendarProcess.weekday);
            }
            return true;
        }
        if (!pTODAY.matcher(str).matches()) {
            if (!pTOMORROW.matcher(str).matches()) {
                return false;
            }
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pTomorrow");
            }
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendarProcess.weekday = calendar.get(Calendar.DAY_OF_WEEK);
            getWeekday(calendarProcess.weekday);
            return true;
        }
        if (DEBUG) {
            MyLog.v(CLS_NAME, "isWeekday: pToday");
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
        calendarProcess.weekday = calendar.get(Calendar.DAY_OF_WEEK);
        calendarProcess.month = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
        calendarProcess.dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        calendarProcess.year = calendar.get(Calendar.YEAR);
        calendarProcess.haveDate = true;
        calendarProcess.haveYear = true;
        calendarProcess.haveMonth = true;
        return true;
    }

    private static void increaseTwelveHours(int pmHour) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "pmHour");
        }
        switch (pmHour) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
                calendarProcess.hourOfDay = (pmHour + 12);
                break;
            default:
                break;
        }
    }

    private static boolean checkAlarmStructure(String str) {
        if (str.contains(hour) || str.contains(minute)) {
            return hasHourOrMinute(str);
        }
        return false;
    }

    private static boolean isYear(String str) {
        if (p2020.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isYear: p2020");
            }
            calendarProcess.year = 2020;
            return true;
        }
        if (p2021.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isYear: p2021");
            }
            calendarProcess.year = 2021;
            return true;
        }
        if (p2022.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isYear: p2022");
            }
            calendarProcess.year = 2022;
            return true;
        }
        if (p2023.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isYear: p2023");
            }
            calendarProcess.year = 2023;
            return true;
        }
        if (p2024.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isYear: p2024");
            }
            calendarProcess.year = 2024;
            return true;
        }
        if (p2025.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isYear: p2025");
            }
            calendarProcess.year = 2025;
            return true;
        }
        if (p2026.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isYear: p2026");
            }
            calendarProcess.year = 2026;
            return true;
        }
        if (p2027.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isYear: p2027");
            }
            calendarProcess.year = 2027;
            return true;
        }
        if (p2028.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isYear: p2028");
            }
            calendarProcess.year = 2028;
            return true;
        }
        if (p2029.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isYear: p2029");
            }
            calendarProcess.year = 2029;
            return true;
        }
        if (p2030.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isYear: p2030");
            }
            calendarProcess.year = 2030;
            return true;
        }
        if (p2031.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isYear: p2031");
            }
            calendarProcess.year = 2031;
            return true;
        }
        if (p2032.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isYear: p2032");
            }
            calendarProcess.year = 2032;
            return true;
        }
        if (p2033.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isYear: p2033");
            }
            calendarProcess.year = 2033;
            return true;
        }
        if (p2034.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isYear: p2034");
            }
            calendarProcess.year = 2034;
            return true;
        }
        return false;
    }

    private static boolean hasHourOrMinute(String str) {
        final String[] separated = str.trim().split(XMLResultsHandler.SEP_SPACE);
        final int length = separated.length;
        for (int i = 1; i < length; i++) {
            if (separated[i].contains(hour)) {
                try {
                    String hourString = separated[i - 1];
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "dissectH: " + hourString);
                    }
                    try {
                        Integer.parseInt(hourString);
                        if (!str.contains(minute)) {
                            if (DEBUG) {
                                MyLog.v(CLS_NAME, "checkHourMinute: hour");
                            }
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "Number of hours not numeric");
                        }
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "dissectH: -1");
                        e.printStackTrace();
                    }
                }
                return false;
            }
            if (separated[i].contains(minute)) {
                try {
                    String minuteString = separated[i - 1];
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "dissectM: " + minuteString);
                    }
                    try {
                        Integer.parseInt(minuteString);
                        if (DEBUG) {
                            MyLog.v(CLS_NAME, "checkHourMinute: hour and or minute");
                        }
                        return true;
                    } catch (NumberFormatException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "Number of minutes not numeric");
                        }
                        return false;
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "dissectM: -1");
                    }
                    return false;
                }
            }
        }
        return false;
    }

    private static boolean isDate(String str) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "in isDate");
        }
        calendarProcess.haveDate = true;
        if (p1st.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p1st");
            }
            calendarProcess.dayOfMonth = 1;
            return true;
        }
        if (p2nd.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p2nd");
            }
            calendarProcess.dayOfMonth = 2;
            return true;
        }
        if (p3rd.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p3rd");
            }
            calendarProcess.dayOfMonth = 3;
            return true;
        }
        if (p4th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p4th");
            }
            calendarProcess.dayOfMonth = 4;
            return true;
        }
        if (p5th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p5th");
            }
            calendarProcess.dayOfMonth = 5;
            return true;
        }
        if (p6th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p6th");
            }
            calendarProcess.dayOfMonth = 6;
            return true;
        }
        if (p7th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p7th");
            }
            calendarProcess.dayOfMonth = 7;
            return true;
        }
        if (p8th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p8th");
            }
            calendarProcess.dayOfMonth = 8;
            return true;
        }
        if (p9th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p9th");
            }
            calendarProcess.dayOfMonth = 9;
            return true;
        }
        if (p10th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p10th");
            }
            calendarProcess.dayOfMonth = 10;
            return true;
        }
        if (p11th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p11th");
            }
            calendarProcess.dayOfMonth = 11;
            return true;
        }
        if (p12th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p12th");
            }
            calendarProcess.dayOfMonth = 12;
            return true;
        }
        if (p13th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p13th");
            }
            calendarProcess.dayOfMonth = 13;
            return true;
        }
        if (p14th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p14th");
            }
            calendarProcess.dayOfMonth = 14;
            return true;
        }
        if (p15th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p15th");
            }
            calendarProcess.dayOfMonth = 15;
            return true;
        }
        if (p16th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p16th");
            }
            calendarProcess.dayOfMonth = 16;
            return true;
        }
        if (p17th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p17th");
            }
            calendarProcess.dayOfMonth = 17;
            return true;
        }
        if (p18th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p18th");
            }
            calendarProcess.dayOfMonth = 18;
            return true;
        }
        if (p19th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p19th");
            }
            calendarProcess.dayOfMonth = 19;
            return true;
        }
        if (p20th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p20th");
            }
            calendarProcess.dayOfMonth = 20;
            return true;
        }
        if (p21st.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p21st");
            }
            calendarProcess.dayOfMonth = 21;
            return true;
        }
        if (p22nd.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p22nd");
            }
            calendarProcess.dayOfMonth = 22;
            return true;
        }
        if (p23rd.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p23rd");
            }
            calendarProcess.dayOfMonth = 23;
            return true;
        }
        if (p24th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p24th");
            }
            calendarProcess.dayOfMonth = 24;
            return true;
        }
        if (p25th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p25th");
            }
            calendarProcess.dayOfMonth = 25;
            return true;
        }
        if (p26th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p26th");
            }
            calendarProcess.dayOfMonth = 26;
            return true;
        }
        if (p27th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p27th");
            }
            calendarProcess.dayOfMonth = 27;
            return true;
        }
        if (p28th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p28th");
            }
            calendarProcess.dayOfMonth = 28;
            return true;
        }
        if (p29th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p29th");
            }
            calendarProcess.dayOfMonth = 29;
            return true;
        }
        if (p30th.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p30th");
            }
            calendarProcess.dayOfMonth = 30;
            return true;
        }
        if (p31st.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: p31st");
            }
            calendarProcess.dayOfMonth = 31;
            return true;
        }
        if (pFIRST.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pFirst");
            }
            calendarProcess.dayOfMonth = 1;
            return true;
        }
        if (pSECOND.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pSecond");
            }
            calendarProcess.dayOfMonth = 2;
            return true;
        }
        if (pTHIRD.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pThird");
            }
            calendarProcess.dayOfMonth = 3;
            return true;
        }
        if (pFOURTH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pFourth");
            }
            calendarProcess.dayOfMonth = 4;
            return true;
        }
        if (pFIFTH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pFifth");
            }
            calendarProcess.dayOfMonth = 5;
            return true;
        }
        if (pSIXTH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pSixth");
            }
            calendarProcess.dayOfMonth = 6;
            return true;
        }
        if (pSEVENTH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pSeventh");
            }
            calendarProcess.dayOfMonth = 7;
            return true;
        }
        if (pEIGHTH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pEight");
            }
            calendarProcess.dayOfMonth = 8;
            return true;
        }
        if (pNINTH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pNineth");
            }
            calendarProcess.dayOfMonth = 9;
            return true;
        }
        if (pTENTH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pTenth");
            }
            calendarProcess.dayOfMonth = 10;
            return true;
        }
        if (pELEVENTH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pEleventh");
            }
            calendarProcess.dayOfMonth = 11;
            return true;
        }
        if (pTWELFTH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pTwelth");
            }
            calendarProcess.dayOfMonth = 12;
            return true;
        }
        if (pTHIRTEENTH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pThirteenth");
            }
            calendarProcess.dayOfMonth = 13;
            return true;
        }
        if (pFOURTEENTH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pFourteenth");
            }
            calendarProcess.dayOfMonth = 14;
            return true;
        }
        if (pFIFTEENTH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pFifteenth");
            }
            calendarProcess.dayOfMonth = 15;
            return true;
        }
        if (pSIXTEENTH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pSixteenth");
            }
            calendarProcess.dayOfMonth = 16;
            return true;
        }
        if (pSEVENTEENTH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pSeventeenth");
            }
            calendarProcess.dayOfMonth = 17;
            return true;
        }
        if (pEIGHTEENTH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pEighteenth");
            }
            calendarProcess.dayOfMonth = 18;
            return true;
        }
        if (pNINETEENTH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pNineteenth");
            }
            calendarProcess.dayOfMonth = 19;
            return true;
        }
        if (pTWENTIETH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pTwentieth");
            }
            calendarProcess.dayOfMonth = 20;
            return true;
        }
        if (pTHIRTIETH.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isDate: pThirtieth");
            }
            calendarProcess.dayOfMonth = 30;
            return true;
        }

        calendarProcess.haveDate = false;
        return false;
    }
}

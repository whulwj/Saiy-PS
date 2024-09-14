package ai.saiy.android.command.agenda;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.utils.MyLog;

public class AgendaHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = AgendaHelper.class.getSimpleName();
    protected static final int TODAY = 345;
    protected static final int TOMORROW = 346;
    protected static final int DAY_AFTER_TOMORROW = 347;

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
    private static final Pattern p21th = Pattern.compile(".*\\b21st\\b.*");
    private static final Pattern p22th = Pattern.compile(".*\\b22nd\\b.*");
    private static final Pattern p23th = Pattern.compile(".*\\b23rd\\b.*");
    private static final Pattern p24th = Pattern.compile(".*\\b24th\\b.*");
    private static final Pattern p25th = Pattern.compile(".*\\b25th\\b.*");
    private static final Pattern p26th = Pattern.compile(".*\\b26th\\b.*");
    private static final Pattern p27th = Pattern.compile(".*\\b27th\\b.*");
    private static final Pattern p28th = Pattern.compile(".*\\b28th\\b.*");
    private static final Pattern p29th = Pattern.compile(".*\\b29th\\b.*");
    private static final Pattern p30th = Pattern.compile(".*\\b30th\\b.*");
    private static final Pattern p31th = Pattern.compile(".*\\b31st\\b.*");

    public static Pair<Boolean, Integer> isMonth(@NonNull Context context, String str) {
        final long then = System.nanoTime();
        final Pattern pJANUARY = Pattern.compile(".*\\b" + context.getString(R.string.JANUARY) + "\\b.*");
        final Pattern pFEBRUARY = Pattern.compile(".*\\b" + context.getString(R.string.FEBRUARY) + "\\b.*");
        final Pattern pMARCH = Pattern.compile(".*\\b" + context.getString(R.string.MARCH) + "\\b.*");
        final Pattern pAPRIL = Pattern.compile(".*\\b" + context.getString(R.string.APRIL) + "\\b.*");
        final Pattern pMAY = Pattern.compile(".*\\b" + context.getString(R.string.MAY) + "\\b.*");
        final Pattern pJUNE = Pattern.compile(".*\\b" + context.getString(R.string.JUNE) + "\\b.*");
        final Pattern pJULY = Pattern.compile(".*\\b" + context.getString(R.string.JULY) + "\\b.*");
        final Pattern pAUGUST = Pattern.compile(".*\\b" + context.getString(R.string.AUGUST) + "\\b.*");
        final Pattern pSEPTEMBER = Pattern.compile(".*\\b" + context.getString(R.string.SEPTEMBER) + "\\b.*");
        final Pattern pOCTOBER = Pattern.compile(".*\\b" + context.getString(R.string.OCTOBER) + "\\b.*");
        final Pattern pNOVEMBER = Pattern.compile(".*\\b" + context.getString(R.string.NOVEMBER) + "\\b.*");
        final Pattern pDECEMBER = Pattern.compile(".*\\b" + context.getString(R.string.DECEMBER) + "\\b.*");
        final String OF_THE_ = context.getString(R.string.OF_THE_);
        final Pattern pThe1st = Pattern.compile(".*\\b" + OF_THE_ + "1st\\b.*");
        final Pattern pThe2nd = Pattern.compile(".*\\b" + OF_THE_ + "2nd\\b.*");
        final Pattern pThe3rd = Pattern.compile(".*\\b" + OF_THE_ + "3rd\\b.*");
        final Pattern pThe4th = Pattern.compile(".*\\b" + OF_THE_ + "4th\\b.*");
        final Pattern pThe5th = Pattern.compile(".*\\b" + OF_THE_ + "5th\\b.*");
        final Pattern pThe6th = Pattern.compile(".*\\b" + OF_THE_ + "6th\\b.*");
        final Pattern pThe7th = Pattern.compile(".*\\b" + OF_THE_ + "7th\\b.*");
        final Pattern pThe8th = Pattern.compile(".*\\b" + OF_THE_ + "8th\\b.*");
        final Pattern pThe9th = Pattern.compile(".*\\b" + OF_THE_ + "9th\\b.*");
        final Pattern pThe10th = Pattern.compile(".*\\b" + OF_THE_ + "10th\\b.*");
        final Pattern pThe11th = Pattern.compile(".*\\b" + OF_THE_ + "11th\\b.*");
        final Pattern pThe12th = Pattern.compile(".*\\b" + OF_THE_ + "12th\\b.*");
        final Pattern pTheFIRST = Pattern.compile(".*\\b" + OF_THE_ + context.getString(R.string.FIRST) + "\\b.*");
        final Pattern pTheSECOND = Pattern.compile(".*\\b" + OF_THE_ + context.getString(R.string.SECOND) + "\\b.*");
        final Pattern pTheTHIRD = Pattern.compile(".*\\b" + OF_THE_ + context.getString(R.string.THIRD) + "\\b.*");
        final Pattern pTheFOURTH = Pattern.compile(".*\\b" + OF_THE_ + context.getString(R.string.FOURTH) + "\\b.*");
        final Pattern pTheFIFTH = Pattern.compile(".*\\b" + OF_THE_ + context.getString(R.string.FIFTH) + "\\b.*");
        final Pattern pTheSIXTH = Pattern.compile(".*\\b" + OF_THE_ + context.getString(R.string.SIXTH) + "\\b.*");
        final Pattern pTheSEVENTH = Pattern.compile(".*\\b" + OF_THE_ + context.getString(R.string.SEVENTH) + "\\b.*");
        final Pattern pTheEIGHTH = Pattern.compile(".*\\b" + OF_THE_ + context.getString(R.string.EIGHTH) + "\\b.*");
        final Pattern pTheNINTH = Pattern.compile(".*\\b" + OF_THE_ + context.getString(R.string.NINTH) + "\\b.*");
        final Pattern pTheTENTH = Pattern.compile(".*\\b" + OF_THE_ + context.getString(R.string.TENTH) + "\\b.*");
        final Pattern pTheELEVENTH = Pattern.compile(".*\\b" + OF_THE_ + context.getString(R.string.ELEVENTH) + "\\b.*");
        final Pattern pTheTWELFTH = Pattern.compile(".*\\b" + OF_THE_ + context.getString(R.string.TWELFTH) + "\\b.*");

        final Pair<Boolean, Integer> results;
        if (pJANUARY.matcher(str).matches() || pThe1st.matcher(str).matches() || pTheFIRST.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.JANUARY);
        } else if (pFEBRUARY.matcher(str).matches() || pThe2nd.matcher(str).matches() || pTheSECOND.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.FEBRUARY);
        } else if (pMARCH.matcher(str).matches() || pThe3rd.matcher(str).matches() || pTheTHIRD.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.MARCH);
        } else if (pAPRIL.matcher(str).matches() || pThe4th.matcher(str).matches() || pTheFOURTH.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.APRIL);
        } else if (pMAY.matcher(str).matches() || pThe5th.matcher(str).matches() || pTheFIFTH.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.MAY);
        } else if (pJUNE.matcher(str).matches() || pThe6th.matcher(str).matches() || pTheSIXTH.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.JUNE);
        } else if (pJULY.matcher(str).matches() || pThe7th.matcher(str).matches() || pTheSEVENTH.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.JULY);
        } else if (pAUGUST.matcher(str).matches() || pThe8th.matcher(str).matches() || pTheEIGHTH.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.AUGUST);
        } else if (pSEPTEMBER.matcher(str).matches() || pThe9th.matcher(str).matches() || pTheNINTH.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.SEPTEMBER);
        } else if (pOCTOBER.matcher(str).matches() || pThe10th.matcher(str).matches() || pTheTENTH.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.OCTOBER);
        } else if (pNOVEMBER.matcher(str).matches() || pThe11th.matcher(str).matches() || pTheELEVENTH.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.NOVEMBER);
        } else if (pDECEMBER.matcher(str).matches() || pThe12th.matcher(str).matches() || pTheTWELFTH.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.DECEMBER);
        } else {
            results = new Pair<>(false, 999);
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "results.first: " + results.first);
            MyLog.d(CLS_NAME, "results.second: " + results.second);
            MyLog.getElapsed(CLS_NAME + ": isMonth -", then);
        }
        return results;
    }

    public static Pair<Boolean, Integer> isYear(String str) {
        final long then = System.nanoTime();
        final Pair<Boolean, Integer> results;
        if (p2020.matcher(str).matches()) {
            results = new Pair<>(true, 2020);
        } else if (p2021.matcher(str).matches()) {
            results = new Pair<>(true, 2021);
        } else if (p2022.matcher(str).matches()) {
            results = new Pair<>(true, 2022);
        } else if (p2023.matcher(str).matches()) {
            results = new Pair<>(true, 2023);
        } else if (p2024.matcher(str).matches()) {
            results = new Pair<>(true, 2024);
        } else if (p2025.matcher(str).matches()) {
            results = new Pair<>(true, 2025);
        } else if (p2026.matcher(str).matches()) {
            results = new Pair<>(true, 2026);
        } else if (p2027.matcher(str).matches()) {
            results = new Pair<>(true, 2027);
        } else if (p2028.matcher(str).matches()) {
            results = new Pair<>(true, 2028);
        } else if (p2029.matcher(str).matches()) {
            results = new Pair<>(true, 2029);
        } else if (p2030.matcher(str).matches()) {
            results = new Pair<>(true, 2030);
        } else if (p2031.matcher(str).matches()) {
            results = new Pair<>(true, 2031);
        } else if (p2032.matcher(str).matches()) {
            results = new Pair<>(true, 2032);
        } else if (p2033.matcher(str).matches()) {
            results = new Pair<>(true, 2033);
        } else if (p2034.matcher(str).matches()) {
            results = new Pair<>(true, 2034);
        } else {
             results = new Pair<>(false, 999);
         }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "results.first: " + results.first);
            MyLog.d(CLS_NAME, "results.second: " + results.second);
            MyLog.getElapsed(CLS_NAME + ": isYear -", then);
        }
        return results;
    }

    public static String getAMPM(@NonNull Date date) {
        try {
            return Integer.parseInt(DateFormat.format("k", date).toString()) <= 11 ? "A.M. ..." : "P.M. ...";
        } catch (NumberFormatException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "getAMPM - NumberFormatException");
                e.printStackTrace();
            }
        }
        return "";
    }

    public static void updateWeekday(@NonNull Calendar calendar, int weekday) {
        final int thisWeekday = calendar.get(Calendar.DAY_OF_WEEK);
        if (weekday < thisWeekday) {
            calendar.add(Calendar.DAY_OF_YEAR, (Calendar.SATURDAY - thisWeekday) + weekday);
        } else if (weekday > thisWeekday) {
            calendar.add(Calendar.DAY_OF_YEAR, weekday - thisWeekday);
        }
    }

    public static Pair<Boolean, Integer> isDate(@NonNull Context context, String str) {
        final long then = System.nanoTime();
        final Pattern pFIRST = Pattern.compile(".*\\b" + context.getString(R.string.FIRST) + "\\b.*");
        final Pattern pSECOND = Pattern.compile(".*\\b" + context.getString(R.string.SECOND) + "\\b.*");
        final Pattern pTHIRD = Pattern.compile(".*\\b" + context.getString(R.string.THIRD) + "\\b.*");
        final Pattern pFOURTH = Pattern.compile(".*\\b" + context.getString(R.string.FOURTH) + "\\b.*");
        final Pattern pFIFTH = Pattern.compile(".*\\b" + context.getString(R.string.FIFTH) + "\\b.*");
        final Pattern  pSIXTH = Pattern.compile(".*\\b" + context.getString(R.string.SIXTH) + "\\b.*");
        final Pattern pSEVENTH = Pattern.compile(".*\\b" + context.getString(R.string.SEVENTH) + "\\b.*");
        final Pattern pEIGHTH = Pattern.compile(".*\\b" + context.getString(R.string.EIGHTH) + "\\b.*");
        final Pattern pNINTH = Pattern.compile(".*\\b" + context.getString(R.string.NINTH) + "\\b.*");
        final Pattern pTENTH = Pattern.compile(".*\\b" + context.getString(R.string.TENTH) + "\\b.*");
        final Pattern pELEVENTH = Pattern.compile(".*\\b" + context.getString(R.string.ELEVENTH) + "\\b.*");
        final Pattern pTWELFTH = Pattern.compile(".*\\b" + context.getString(R.string.TWELFTH) + "\\b.*");
        final Pattern pTHIRTEENTH = Pattern.compile(".*\\b" + context.getString(R.string.THIRTEENTH) + "\\b.*");
        final Pattern pFOURTEENTH = Pattern.compile(".*\\b" + context.getString(R.string.FOURTEENTH) + "\\b.*");
        final Pattern pFIFTEENTH = Pattern.compile(".*\\b" + context.getString(R.string.FIFTEENTH) + "\\b.*");
        final Pattern pSIXTEENTH = Pattern.compile(".*\\b" + context.getString(R.string.SIXTEENTH) + "\\b.*");
        final Pattern pSEVENTEENTH = Pattern.compile(".*\\b" + context.getString(R.string.SEVENTEENTH) + "\\b.*");
        final Pattern pEIGHTEENTH = Pattern.compile(".*\\b" + context.getString(R.string.EIGHTEENTH) + "\\b.*");
        final Pattern pNINETEENTH = Pattern.compile(".*\\b" + context.getString(R.string.NINETEENTH) + "\\b.*");
        final Pattern pTWENTIETH = Pattern.compile(".*\\b" + context.getString(R.string.TWENTIETH) + "\\b.*");
        final Pattern pTHIRTIETH = Pattern.compile(".*\\b" + context.getString(R.string.THIRTIETH) + "\\b.*");

        Pair<Boolean, Integer> results;
        if (p1st.matcher(str).matches() || pFIRST.matcher(str).matches()) {
            results = new Pair<>(true, 1);
        } else if (p2nd.matcher(str).matches() || pSECOND.matcher(str).matches()) {
            results = new Pair<>(true, 2);
        } else if (p3rd.matcher(str).matches() || pTHIRD.matcher(str).matches()) {
            results = new Pair<>(true, 3);
        } else if (p4th.matcher(str).matches() || pFOURTH.matcher(str).matches()) {
            results = new Pair<>(true, 4);
        } else if (p5th.matcher(str).matches() || pFIFTH.matcher(str).matches()) {
            results = new Pair<>(true, 5);
        } else if (p6th.matcher(str).matches() || pSIXTH.matcher(str).matches()) {
            results = new Pair<>(true, 6);
        } else if (p7th.matcher(str).matches() || pSEVENTH.matcher(str).matches()) {
            results = new Pair<>(true, 7);
        } else if (p8th.matcher(str).matches() || pEIGHTH.matcher(str).matches()) {
            results = new Pair<>(true, 8);
        } else if (p9th.matcher(str).matches() || pNINTH.matcher(str).matches()) {
            results = new Pair<>(true, 9);
        } else if (p10th.matcher(str).matches() || pTENTH.matcher(str).matches()) {
            results = new Pair<>(true, 10);
        } else if (p11th.matcher(str).matches() || pELEVENTH.matcher(str).matches()) {
            results = new Pair<>(true, 11);
        } else if (p12th.matcher(str).matches() || pTWELFTH.matcher(str).matches()) {
            results = new Pair<>(true, 12);
        } else if (p13th.matcher(str).matches() || pTHIRTEENTH.matcher(str).matches()) {
            results = new Pair<>(true, 13);
        } else if (p14th.matcher(str).matches() || pFOURTEENTH.matcher(str).matches()) {
            results = new Pair<>(true, 14);
        } else if (p15th.matcher(str).matches() || pFIFTEENTH.matcher(str).matches()) {
            results = new Pair<>(true, 15);
        } else if (p16th.matcher(str).matches() || pSIXTEENTH.matcher(str).matches()) {
            results = new Pair<>(true, 16);
        } else if (p17th.matcher(str).matches() || pSEVENTEENTH.matcher(str).matches()) {
            results = new Pair<>(true, 17);
        } else if (p18th.matcher(str).matches() || pEIGHTEENTH.matcher(str).matches()) {
            results = new Pair<>(true, 18);
        } else if (p19th.matcher(str).matches() || pNINETEENTH.matcher(str).matches()) {
            results = new Pair<>(true, 19);
        } else if (p20th.matcher(str).matches() || pTWENTIETH.matcher(str).matches()) {
            results = new Pair<>(true, 20);
        } else if (p21th.matcher(str).matches()) {
            results = new Pair<>(true, 21);
        } else if (p22th.matcher(str).matches()) {
            results = new Pair<>(true, 22);
        } else if (p23th.matcher(str).matches()) {
            results = new Pair<>(true, 23);
        } else if (p24th.matcher(str).matches()) {
            results = new Pair<>(true, 24);
        } else if (p25th.matcher(str).matches()) {
            results = new Pair<>(true, 25);
        } else if (p26th.matcher(str).matches()) {
            results = new Pair<>(true, 26);
        } else if (p27th.matcher(str).matches()) {
            results = new Pair<>(true, 27);
        } else if (p28th.matcher(str).matches()) {
            results = new Pair<>(true, 28);
        } else if (p29th.matcher(str).matches()) {
            results = new Pair<>(true, 29);
        } else if (p30th.matcher(str).matches() || pTHIRTIETH.matcher(str).matches()) {
            results = new Pair<>(true, 30);
        } else if (p31th.matcher(str).matches()) {
            results = new Pair<>(true, 31);
        } else {
            results = new Pair<>(false, 999);
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "results.first: " + results.first);
            MyLog.d(CLS_NAME, "results.second: " + results.second);
            MyLog.getElapsed(CLS_NAME + ": isDate -", then);
        }
        return results;
    }

    public static Pair<Boolean, Integer> isWeekday(@NonNull Context context, String str) {
        final long then = System.nanoTime();
        final Pattern pMONDAY = Pattern.compile(".*\\b" + context.getString(R.string.MONDAY) + "\\b.*");
        final Pattern pTUESDAY = Pattern.compile(".*\\b" + context.getString(R.string.TUESDAY) + "\\b.*");
        final Pattern pWEDNESDAY = Pattern.compile(".*\\b" + context.getString(R.string.WEDNESDAY) + "\\b.*");
        final Pattern pTHURSDAY = Pattern.compile(".*\\b" + context.getString(R.string.THURSDAY) + "\\b.*");
        final Pattern pFRIDAY = Pattern.compile(".*\\b" + context.getString(R.string.FRIDAY) + "\\b.*");
        final Pattern pSATURDAY = Pattern.compile(".*\\b" + context.getString(R.string.SATURDAY) + "\\b.*");
        final Pattern pSUNDAY = Pattern.compile(".*\\b" + context.getString(R.string.SUNDAY) + "\\b.*");
        final Pattern pTODAY = Pattern.compile(".*\\b" + context.getString(R.string.TODAY) + "\\b.*");
        final Pattern pTOMORROW = Pattern.compile(".*\\b" + context.getString(R.string.TOMORROW) + "\\b.*");
        final Pattern pDayAfterTomorrow = Pattern.compile(".*\\b" + context.getString(R.string.day_after_tomorrow) + "\\b.*");

        Pair<Boolean, Integer> results;
        if (pMONDAY.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.MONDAY);
        } else if (pTUESDAY.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.TUESDAY);
        } else if (pWEDNESDAY.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.WEDNESDAY);
        } else if (pTHURSDAY.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.THURSDAY);
        } else if (pFRIDAY.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.FRIDAY);
        } else if (pSATURDAY.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.SATURDAY);
        } else if (pSUNDAY.matcher(str).matches()) {
            results = new Pair<>(true, Calendar.SUNDAY);
        } else if (pTODAY.matcher(str).matches()) {
            results = new Pair<>(true, TODAY);
        } else if (pTOMORROW.matcher(str).matches()) {
            results = new Pair<>(true, TOMORROW);
        } else if (pDayAfterTomorrow.matcher(str).matches()) {
            results = new Pair<>(true, DAY_AFTER_TOMORROW);
        } else {
            results = new Pair<>(false, 999);
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "results.first: " + results.first);
            MyLog.d(CLS_NAME, "results.second: " + results.second);
            MyLog.getElapsed(CLS_NAME + ": isWeekday -", then);
        }
        return results;
    }
}

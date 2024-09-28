/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.TimeZone;

import ai.saiy.android.command.horoscope.CommandHoroscopeValues;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.service.helper.SelfAwareHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsFile;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

/**
 * Broadcast Receiver for Boot Completed.
 * <p/>
 * Created by benrandall76@gmail.com on 25/03/2016.
 */
public class BRBoot extends BroadcastReceiver {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = BRBoot.class.getSimpleName();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onReceive");
        }

        if (SPH.getSelfAwareEnabled(context.getApplicationContext())
                && SPH.getStartAtBoot(context.getApplicationContext())) {

            final String action = getAction(intent);
            if (UtilsString.notNaked(action) && action.equals(Intent.ACTION_BOOT_COMPLETED)
                    || action.equals("android.intent.action.QUICKBOOT_POWERON")
                    || action.equals("com.htc.intent.action.QUICKBOOT_POWERON")) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onReceive: starting service");
                }

                SelfAwareHelper.startSelfAwareIfRequired(context.getApplicationContext());

                if (SPH.getHotwordBoot(context.getApplicationContext())) {

                    final LocalRequest request = new LocalRequest(context.getApplicationContext());
                    request.prepareDefault(LocalRequest.ACTION_START_HOTWORD, null);
                    request.execute();

                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onReceive: start at boot hotword disabled by user");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onReceive: action naked or unknown");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onReceive: start at boot disabled by user");
            }
        }
        if (SPH.getStarSign(context) != CommandHoroscopeValues.Sign.UNKNOWN) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onReceive: setting user birthday alarm");
            }
            final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            final PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, new Intent(context, BRBirthday.class), PendingIntent.FLAG_IMMUTABLE);
            final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.DAY_OF_MONTH, SPH.getDobDay(context));
            calendar.set(Calendar.MONTH, SPH.getDobMonth(context));
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            calendar.set(Calendar.MINUTE, 30);
            if (Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale()).after(calendar)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onReceive: calendar after: adding year");
                }
                calendar.add(Calendar.YEAR, 1);
            } else if (DEBUG) {
                MyLog.i(CLS_NAME, "onReceive: calendar before");
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onReceive: calendar: date: " + calendar.get(Calendar.DAY_OF_MONTH));
                MyLog.i(CLS_NAME, "onReceive: calendar: month: " + (calendar.get(Calendar.MONTH) + 1));
                MyLog.i(CLS_NAME, "onReceive: calendar: year: " + calendar.get(Calendar.YEAR));
            }
            alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), broadcast);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "onReceive: DOB unknown");
        }
        UtilsFile.createDirs(context);
    }

    /**
     * Get the Intent action, if there is one.
     *
     * @param intent received by the Broadcast
     * @return the Intent Action or an empty string
     */
    private String getAction(final Intent intent) {
        if (intent != null && intent.getAction() != null) {
            return intent.getAction();
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onReceive: unknown intent");
            }
        }
        return "";
    }
}
package ai.saiy.android.command.call;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.CallLog;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import androidx.core.content.PermissionChecker;

import com.android.internal.telephony.ITelephony;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class CallHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = CallHelper.class.getSimpleName();

    public static String getLastOutgoingCall(Context context) {
        return CallLog.Calls.getLastOutgoingCall(context);
    }

    private static boolean answerCall1(Context context, TelephonyManager telephonyManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (PermissionChecker.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PermissionChecker.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                return answerCall2(context, telephonyManager);
            }
            ((TelecomManager) context.getSystemService(Context.TELECOM_SERVICE)).acceptRingingCall();
            return true;
        }
        try {
            telephonyManager.getClass().getMethod("answerRingingCall").invoke(telephonyManager);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "answerCall1: success");
            }
            return true;
        } catch (IllegalAccessException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "answerCall1: IllegalAccessException");
                e.printStackTrace();
            }
            return answerCall2(context, telephonyManager);
        } catch (NoSuchMethodException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "answerCall1: NoSuchMethodException");
                e.printStackTrace();
            }
            return answerCall2(context, telephonyManager);
        } catch (SecurityException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "answerCall1: SecurityException");
                e.printStackTrace();
            }
            return answerCall2(context, telephonyManager);
        } catch (InvocationTargetException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "answerCall1: InvocationTargetException");
                e.printStackTrace();
            }
            return answerCall2(context, telephonyManager);
        }
    }

    public static boolean callNumber(Context context, String str) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "callNumber: " + str);
        }
        final Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + str));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "callNumber ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (SecurityException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "callNumber SecurityException");
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean answerCall(Context context, boolean onSpeaker) {
        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return (telephonyManager == null || !onSpeaker) ? answerCall1(context, telephonyManager) : answerCallOnSpeaker(context);
    }

    private static boolean rejectCall1(TelephonyManager telephonyManager) {
        try {
            final Method declaredMethod = Class.forName(telephonyManager.getClass().getName()).getDeclaredMethod("getITelephony");
            declaredMethod.setAccessible(true);
            ((ITelephony) declaredMethod.invoke(telephonyManager, new Object[0])).endCall();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "rejectCall1: success");
            }
            return true;
        } catch (RemoteException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "rejectCall1: RemoteException");
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "rejectCall1: ClassNotFoundException");
                e.printStackTrace();
            }
        } catch (IllegalAccessException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "rejectCall1: IllegalAccessException");
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "rejectCall1: NoSuchMethodException");
                e.printStackTrace();
            }
        } catch (SecurityException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "rejectCall1: SecurityException");
                e.printStackTrace();
            }
        } catch (InvocationTargetException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "rejectCall1: InvocationTargetException");
                e.printStackTrace();
            }
        }
        return false;
    }

    private static void broadcastHeadsetConnected(Context context, boolean connected) {
        final String action;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            action = AudioManager.ACTION_HEADSET_PLUG;
        } else {
            action = Intent.ACTION_HEADSET_PLUG;
        }
        final Intent intent = new Intent(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra("state", connected ? 1 : 0);
        intent.putExtra("name", "mysms");
        try {
            context.sendOrderedBroadcast(intent, null);
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "broadcastHeadsetConnected: Exception");
                e.printStackTrace();
            }
        }
    }

    public static boolean hasTelephonyFeature(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    private static boolean answerCall2(Context context, TelephonyManager telephonyManager) {
        try {
            final Method declaredMethod = Class.forName(telephonyManager.getClass().getName()).getDeclaredMethod("getITelephony");
            declaredMethod.setAccessible(true);
            final ITelephony iTelephony = (ITelephony) declaredMethod.invoke(telephonyManager, new Object[0]);
            iTelephony.silenceRinger();
            iTelephony.answerRingingCall();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "answerCall2: success");
            }
            return true;
        } catch (RemoteException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "answerCall2: RemoteException");
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "answerCall2: ClassNotFoundException");
                e.printStackTrace();
            }
        } catch (IllegalAccessException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "answerCall2: IllegalAccessException");
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "answerCall2: NoSuchMethodException");
                e.printStackTrace();
            }
        } catch (SecurityException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "answerCall2: SecurityException");
                e.printStackTrace();
            }
        } catch (InvocationTargetException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "answerCall2: InvocationTargetException");
                e.printStackTrace();
            }
        }
        return answerCall3(context);
    }

    public static boolean dialNumber(Context context, String str) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "dialNumber: " + str);
        }
        final Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + str));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "dialNumber ActivityNotFoundException");
                e.printStackTrace();
            }
            return false;
        } catch (SecurityException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "dialNumber SecurityException");
                e.printStackTrace();
            }
            return false;
        }
    }

    public static String getLastMissedCall(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "dialNumber: " + "getLastMissedCall");
        }
        final String[] projection = new String[] {CallLog.Calls.NUMBER};
        final String selection = CallLog.Calls.TYPE + " = " + CallLog.Calls.MISSED_TYPE;
        final String sortOrder = CallLog.Calls.DATE + " DESC";
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, projection, selection, null, sortOrder);
        } catch (SecurityException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "SecurityException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "Exception");
            }
        }
        if (cursor == null) {
            return null;
        }
        try {
            int columnNumberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            while (cursor.moveToNext()) {
                String number = cursor.getString(columnNumberIndex);
                if (!UtilsString.notNaked(number) && number.matches("-1")) {
                    continue;
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getLastMissedCall: " + number);
                }
                return number;
            }
        } catch (SecurityException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "SecurityException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "Exception");
            }
        } finally {
            try {
                cursor.close();
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "Exception");
                }
            }
        }
        return null;
    }

    public static boolean rejectCall(Context context) {
        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager != null && rejectCall1(telephonyManager);
    }

    private static boolean answerCallOnSpeaker(final Context context) {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            return false;
        }
        final boolean needBroadcast = "HTC".equalsIgnoreCase(Build.MANUFACTURER) && !audioManager.isWiredHeadsetOn();
        if (needBroadcast) {
            broadcastHeadsetConnected(context, false);
        }
        final Intent headsetHookDown = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
        final Intent headsetHookUp = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
        try {
            context.sendOrderedBroadcast(headsetHookDown, Manifest.permission.CALL_PRIVILEGED);
            context.sendOrderedBroadcast(headsetHookUp, Manifest.permission.CALL_PRIVILEGED);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                    audioManager.setSpeakerphoneOn(true);
                    ai.saiy.android.utils.SPH.setResetSpeaker(context, true);
                }
            }, 1500L);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "answerCallOnSpeaker: success");
            }
            if (needBroadcast) {
                broadcastHeadsetConnected(context, false);
            }
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "answerCallOnSpeaker: Exception");
                e.printStackTrace();
            }
            if (needBroadcast) {
                broadcastHeadsetConnected(context, false);
            }
            return answerCallOnSpeaker1(context);
        } catch (Throwable th) {
            if (needBroadcast) {
                broadcastHeadsetConnected(context, false);
            }
            return false;
        }
    }

    private static boolean answerCallOnSpeaker1(Context context) {
        final Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
        try {
            context.sendOrderedBroadcast(intent, Manifest.permission.CALL_PRIVILEGED);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "answerCallOnSpeaker1: success");
            }
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "answerCallOnSpeaker1: Exception");
                e.printStackTrace();
            }
            return false;
        }
    }

    private static boolean answerCall3(Context context) {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            return false;
        }
        final boolean needBroadcast = "HTC".equalsIgnoreCase(Build.MANUFACTURER) && !audioManager.isWiredHeadsetOn();
        if (needBroadcast) {
            broadcastHeadsetConnected(context, false);
        }
        final Intent headsetHookDown = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
        final Intent headsetHookUp = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
        try {
            try {
                context.sendOrderedBroadcast(headsetHookDown, Manifest.permission.CALL_PRIVILEGED);
                context.sendOrderedBroadcast(headsetHookUp, Manifest.permission.CALL_PRIVILEGED);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "answerCall3: success");
                }
                if (needBroadcast) {
                    broadcastHeadsetConnected(context, false);
                }
                return true;
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "answerCall3: Exception");
                    e.printStackTrace();
                }
                if (needBroadcast) {
                    broadcastHeadsetConnected(context, false);
                }
                return answerCall4();
            }
        } catch (Throwable th) {
            if (needBroadcast) {
                broadcastHeadsetConnected(context, false);
            }
            return false;
        }
    }

    private static boolean answerCall4() {
        try {
            Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_HEADSETHOOK);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "answerCall4: success");
            }
            return true;
        } catch (IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "answerCall4: IOException");
                e.printStackTrace();
            }
            return answerCall4();
        }
    }
}

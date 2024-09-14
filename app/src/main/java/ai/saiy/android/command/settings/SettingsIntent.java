package ai.saiy.android.command.settings;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Iterator;

import ai.saiy.android.applications.Installed;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.utils.MyLog;

public class SettingsIntent {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = SettingsIntent.class.getSimpleName();
    private Type mType;
    private String mCommand;

    public enum Type {
        UNKNOWN,
        NFC,
        SECURITY,
        BATTERY,
        LOCATION,
        PRIVACY,
        QUICK_LAUNCH,
        VOICE_SEARCH,
        SEARCH,
        SOUND,
        SYNC,
        DICTIONARY,
        WIFI,
        BLUETOOTH,
        DATA,
        TTS,
        DATE,
        DEVICE,
        ACCESSIBILITY,
        APN,
        APPLICATION,
        DISPLAY,
        INPUT,
        LOCALE,
        MEMORY_INTERNAL,
        MEMORY_EXTERNAL,
        NETWORK,
        SETTINGS,
        NOTIFICATION_ACCESS
    }

    public static void clearPackagePreferredActivities(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "clearPackagePreferredActivities");
        }
        context.getPackageManager().clearPackagePreferredActivities(context.getPackageName());
    }

    public static boolean settingsIntent(Context context, Type type) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "settingsIntent");
        }
        final Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        switch (type) {
            case NFC:
                intent.setAction(Settings.ACTION_NFCSHARING_SETTINGS);
                break;
            case SECURITY:
                intent.setAction(Settings.ACTION_SECURITY_SETTINGS);
                break;
            case BATTERY:
                intent.setAction(Intent.ACTION_POWER_USAGE_SUMMARY);
                break;
            case LOCATION:
                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                break;
            case PRIVACY:
                intent.setAction(Settings.ACTION_PRIVACY_SETTINGS);
                break;
            case QUICK_LAUNCH:
                intent.setAction(Settings.ACTION_QUICK_LAUNCH_SETTINGS);
                break;
            case VOICE_SEARCH:
                return voiceSearchSettings(context);
            case SEARCH:
                intent.setAction(Settings.ACTION_SEARCH_SETTINGS);
                break;
            case SOUND:
                intent.setAction(Settings.ACTION_SOUND_SETTINGS);
                break;
            case SYNC:
                intent.setAction(Settings.ACTION_SYNC_SETTINGS);
                break;
            case DICTIONARY:
                intent.setAction(Settings.ACTION_USER_DICTIONARY_SETTINGS);
                break;
            case WIFI:
                intent.setAction(Settings.ACTION_WIFI_SETTINGS);
                break;
            case BLUETOOTH:
                intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
                break;
            case DATA:
                intent.setAction(Settings.ACTION_DATA_ROAMING_SETTINGS);
                break;
            case TTS:
                intent.setAction("com.android.settings.TTS_SETTINGS");
                break;
            case DATE:
                intent.setAction(Settings.ACTION_DATE_SETTINGS);
                break;
            case DEVICE:
                intent.setAction(Settings.ACTION_DEVICE_INFO_SETTINGS);
                break;
            case ACCESSIBILITY:
                intent.setAction(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                break;
            case APN:
                intent.setAction(Settings.ACTION_APN_SETTINGS);
                break;
            case APPLICATION:
                intent.setAction(Settings.ACTION_APPLICATION_SETTINGS);
                break;
            case DISPLAY:
                intent.setAction(Settings.ACTION_DISPLAY_SETTINGS);
                break;
            case INPUT:
                intent.setAction(Settings.ACTION_INPUT_METHOD_SETTINGS);
                break;
            case LOCALE:
                intent.setAction(Settings.ACTION_LOCALE_SETTINGS);
                break;
            case MEMORY_INTERNAL:
                intent.setAction(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
                break;
            case MEMORY_EXTERNAL:
                intent.setAction(Settings.ACTION_MEMORY_CARD_SETTINGS);
                break;
            case NETWORK:
                intent.setAction(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
                break;
            case SETTINGS:
                intent.setAction(Settings.ACTION_SETTINGS);
                break;
            case NOTIFICATION_ACCESS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    intent.setAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                } else {
                    intent.setAction("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                }
                break;
            default:
                return false;
        }
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "settingsIntent: ActivityNotFoundException");
                e.printStackTrace();
            }
            return false;
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "settingsIntent: Exception");
                e.printStackTrace();
            }
            return false;
        }
    }

    private static boolean voiceSearchSettings(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceSearchSettings");
        }
        final Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ArrayList<ComponentName> arrayList = new ArrayList<>();
        arrayList.add(new ComponentName(Installed.PACKAGE_NAME_GOOGLE_NOW, IntentConstants.COMPONENT_VOICE_SEARCH_PREFERENCES_VELVET));
        arrayList.add(new ComponentName(Installed.PACKAGE_NAME_GOOGLE_NOW, IntentConstants.COMPONENT_VOICE_SEARCH_PREFERENCES));
        arrayList.add(new ComponentName(Installed.PACKAGE_NAME_GOOGLE_NOW, "com.google.android.apps.gsa.settingsui.VoiceSearchPreferences"));
        Iterator<ComponentName> it = arrayList.iterator();
        while (it.hasNext()) {
            try {
                intent.setComponent(it.next());
                context.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "voiceSearchSettings: ActivityNotFoundException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "voiceSearchSettings: Exception");
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public Type getType() {
        return this.mType;
    }

    public void setType(Type type) {
        this.mType = type;
    }

    public void setCommand(String str) {
        this.mCommand = str;
    }

    public String getCommand() {
        return this.mCommand;
    }
}

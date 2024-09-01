package ai.saiy.android.command.superuser;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class Superuser_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Superuser_en.class.getSimpleName();

    private static String super_user;
    private static String superuser;
    private static String reboot;
    private static String recovery;
    private static String bootloader;
    private static String boot_loader;
    private static String fastboot;
    private static String screen_shot;
    private static String screenshot;
    private static String cpu;
    private static String governor;
    private static String power;
    private static String hot_reboot;
    private static String shutdown;
    private static String shut_down;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Superuser_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (superuser == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static CommandSuperuserValues sortSuperuser(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final CommandSuperuserValues commandSuperuserValues = new CommandSuperuserValues();
        commandSuperuserValues.setDescription("");
        commandSuperuserValues.setRoot(CommandSuperuserValues.Root.UNKNOWN);
        if (superuser == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
            sr.reset();
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        for (String voiceDatum : voiceData) {
            String vdLower = voiceDatum.toLowerCase(locale).trim();
            if (vdLower.startsWith(superuser) || vdLower.startsWith(super_user)) {
                if (vdLower.contains(recovery)) {
                    commandSuperuserValues.setRoot(CommandSuperuserValues.Root.RECOVERY);
                    commandSuperuserValues.setDescription(recovery);
                    break;
                }
                if (vdLower.contains(bootloader) || vdLower.contains(boot_loader)) {
                    commandSuperuserValues.setRoot(CommandSuperuserValues.Root.BOOTLOADER);
                    commandSuperuserValues.setDescription(bootloader);
                    break;
                }
                if (vdLower.contains(fastboot)) {
                    commandSuperuserValues.setRoot(CommandSuperuserValues.Root.FASTBOOT);
                    commandSuperuserValues.setDescription(fastboot);
                    break;
                }
                if (vdLower.contains(screenshot) || vdLower.contains(screen_shot)) {
                    commandSuperuserValues.setRoot(CommandSuperuserValues.Root.SCREENSHOT);
                    commandSuperuserValues.setDescription(screenshot);
                    break;
                }
                if (vdLower.contains(cpu) && vdLower.contains(governor)) {
                    commandSuperuserValues.setRoot(CommandSuperuserValues.Root.GOVERNOR);
                    commandSuperuserValues.setDescription(governor);
                    break;
                }
                if (vdLower.contains(hot_reboot)) {
                    commandSuperuserValues.setRoot(CommandSuperuserValues.Root.HOT_REBOOT);
                    commandSuperuserValues.setDescription(hot_reboot);
                    break;
                }
                if (vdLower.contains(power) || vdLower.contains(shutdown) || vdLower.contains(shut_down)) {
                    commandSuperuserValues.setRoot(CommandSuperuserValues.Root.SHUTDOWN);
                    commandSuperuserValues.setDescription(shutdown);
                    break;
                }
                if (vdLower.contains(reboot)) {
                    commandSuperuserValues.setRoot(CommandSuperuserValues.Root.REBOOT);
                    commandSuperuserValues.setDescription(reboot);
                    break;
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return commandSuperuserValues;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        superuser = sr.getString(R.string.superuser);
        super_user = sr.getString(R.string.super_user);
        reboot = sr.getString(R.string.reboot);
        recovery = sr.getString(R.string.recovery);
        bootloader = sr.getString(R.string.bootloader);
        boot_loader = sr.getString(R.string.boot_loader);
        fastboot = sr.getString(R.string.fastboot);
        shut_down = sr.getString(R.string.shut_down);
        screen_shot = sr.getString(R.string.screen_shot);
        screenshot = sr.getString(R.string.screenshot);
        cpu = sr.getString(R.string.cpu);
        governor = sr.getString(R.string.governor);
        power = sr.getString(R.string.power);
        hot_reboot = sr.getString(R.string.hot_reboot);
        shutdown = sr.getString(R.string.shutdown);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(voiceData) && ai.saiy.android.utils.UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                String vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.startsWith(superuser) || vdLower.startsWith(super_user)) {
                    toReturn.add(new Pair<>(CC.COMMAND_SUPERUSER, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "superuser: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}

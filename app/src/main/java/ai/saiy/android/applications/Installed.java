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

package ai.saiy.android.applications;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import ai.saiy.android.BuildConfig;
import ai.saiy.android.defaults.notes.NoteProvider;
import ai.saiy.android.defaults.songrecognition.SongRecognitionProvider;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 20/04/2016.
 */
public class Installed {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Installed.class.getSimpleName();

    public static final String PACKAGE_ALEXA = "com.amazon.dee.app";
    public static final String PACKAGE_AMAZON = "com.amazon.mShop.android.shopping";
    public static final String PACKAGE_AMAZON_WINDOW_SHOP = "com.amazon.windowshop";
    public static final String PACKAGE_AMAZON_MUSIC = "com.amazon.mp3";
    public static final String PACKAGE_AUTOMATE = "com.llamalab.automate";
    public static final String PACKAGE_BOX = "com.box.android";
    public static final String PACKAGE_DEEZER = "deezer.android.app";
    public static final String PACKAGE_DROPBOX = "com.dropbox.android";
    public static final String PACKAGE_EBAY = "com.ebay.mobile";
    public static final String PACKAGE_EVERNOTE = "com.evernote";
    public static final String PACKAGE_FACEBOOK = "com.facebook.katana";
    public static final String PACKAGE_FOUR_SQUARED = "com.joelapenna.foursquared";
    public static final String PACKAGE_GOOGLE_DOCS = "com.google.android.apps.docs";
    public static final String PACKAGE_GOOGLE_EARTH = "com.google.earth";
    public static final String PACKAGE_GOOGLE_HANGOUT = "com.google.android.talk";
    public static final String PACKAGE_GOOGLE_MAPS = "com.google.android.apps.maps";
    public static final String PACKAGE_GOOGLE_MESSAGE = "com.google.android.apps.messaging";
    public static final String PACKAGE_GOOGLE_MUSIC = "com.google.android.music";
    public static final String PACKAGE_GOOGLE_KEEP = "com.google.android.keep";
    public static final String PACKAGE_GOOGLE_SKY = "com.google.android.stardroid";
    public static final String PACKAGE_GOOGLE_STORE = "com.android.vending";
    public static final String PACKAGE_GOOGLE_TRANSLATE = "com.google.android.apps.translate";
    public static final String PACKAGE_GOOGLE_YOUTUBE = "com.google.android.youtube";
    public static final String PACKAGE_IMDB = "com.imdb.mobile";
    public static final String PACKAGE_LINKED_IN = "com.linkedin.android";
    public static final String PACKAGE_MICROSOFT_CORTANA = "com.microsoft.cortana";
    public static final String PACKAGE_NETFLIX = "com.netflix.mediaclient";
    public static final String PACKAGE_TWITTER = "com.twitter.android";
    public static final String PACKAGE_SNAPCHAT = "com.snapchat.android";
    public static final String PACKAGE_WHATSAPP = "com.whatsapp";
    public static final String PACKAGE_TINDER = "com.tinder";
    public static final String PACKAGE_ASPIRO_TINDER = "com.aspiro.tidal";
    public static final String PACKAGE_SHAZAM = "com.shazam.android";
    public static final String PACKAGE_SHAZAM_ENCORE = "com.shazam.encore.android";
    public static final String PACKAGE_SPOTIFY_MUSIC = "com.spotify.music";
    public static final String PACKAGE_SOUND_HOUND = "com.melodis.midomiMusicIdentifier.freemium";
    public static final String PACKAGE_SOUND_HOUND_PREMIUM = "com.melodis.midomiMusicIdentifier";
    public static final String PACKAGE_SKYPE = "com.skype.raider";
    @Deprecated
    public static final String PACKAGE_TRACK_ID = "com.sonyericsson.trackid";
    public static final String PACKAGE_GOOGLE_SOUND_SEARCH = "com.google.android.ears";
    public static final String PACKAGE_NAME_GOOGLE = "com.google.android";
    public static final String PACKAGE_NAME_GOOGLE_NOW = "com.google.android.googlequicksearchbox";
    public static final String PACKAGE_NAME_GOOGLE_NOW_LAUNCHER = "com.google.android.googlequicksearchbox";
    public static final String PACKAGE_TASKER_DIRECT = "net.dinglisch.android.tasker";
    public static final String PACKAGE_TASKER_MARKET = PACKAGE_TASKER_DIRECT + "m";
    public static final String PACKAGE_UBER = "com.ubercab";
    public static final String PACKAGE_WOLFRAM_ALPHA = "com.wolfram.android.alpha";
    public static final String PACKAGE_YELP = "com.yelp.android";

    private static final Pattern pCONTROL_SAIY = Pattern.compile(Constants.PERMISSION_CONTROL_SAIY);
    private static final Pattern pSAIY_PACKAGE = Pattern.compile(BuildConfig.APPLICATION_ID);

    /**
     * Get a list of all of the installed applications that hold the {@link Constants#PERMISSION_CONTROL_SAIY},
     * regardless of whether or not the permission has been explicitly granted. The results exclude our own
     * package.
     * <p>
     * This method is slower than {@link #declaresSaiyPermissionLegacy(Context)} but avoids the 'transaction
     * too large' errors.
     *
     * @param ctx the application context
     * @return an ArrayList containing a {@link Pair} with the first parameter containing the application name
     * and the second the package name.
     */
    public static ArrayList<Pair<String, String>> declaresSaiyPermission(@NonNull final Context ctx) {

        final long then = System.nanoTime();

        final ArrayList<Pair<String, String>> holdsPermission = new ArrayList<>();
        final PackageManager pm = ctx.getPackageManager();
        final List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        CharSequence appNameChar;
        String appName;
        String[] permissions;
        PackageInfo packageInfo;
        for (final ApplicationInfo applicationInfo : apps) {

            try {

                packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);

                permissions = packageInfo.requestedPermissions;
                if (permissions != null) {

                    for (final String permission : permissions) {
                        if (pCONTROL_SAIY.matcher(permission).matches()) {

                            appNameChar = packageInfo.applicationInfo.loadLabel(pm);

                            if (appNameChar != null) {
                                appName = packageInfo.applicationInfo.loadLabel(pm).toString();

                                if (UtilsString.notNaked(appName)) {
                                    if (!pSAIY_PACKAGE.matcher(packageInfo.packageName).matches()) {
                                        holdsPermission.add(new Pair<>(appName, packageInfo.packageName));
                                    }
                                    break;
                                }
                            }

                            holdsPermission.add(new Pair<>(packageInfo.packageName, packageInfo.packageName));
                        }
                    }
                }
            } catch (final PackageManager.NameNotFoundException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "isPackageInstalled: NameNotFoundException");
                }
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(Installed.class.getSimpleName(), then);
        }

        return holdsPermission;
    }

    public static ArrayList<Application> getInstalledApplications(Context context, boolean includeSystemApplication, boolean sortByName) {
        final long startTime = System.nanoTime();
        final ArrayList<Application> arrayList = new ArrayList<>();
        final PackageManager packageManager = context.getPackageManager();
        final List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(0);
        if (sortByName) {
            Collections.sort(installedApplications, new ApplicationInfo.DisplayNameComparator(packageManager));
        }
        CharSequence label;
        String packageName;
        for (ApplicationInfo applicationInfo : installedApplications) {
            if (includeSystemApplication || applicationInfo.flags != ApplicationInfo.FLAG_SYSTEM) {
                label = applicationInfo.loadLabel(packageManager);
                packageName = applicationInfo.packageName;
                if (packageName != null) {
                    arrayList.add(new Application(label, packageName, applicationInfo.loadIcon(packageManager)));
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, startTime);
        }
        return arrayList;
    }

    public static ArrayList<Application> getSearchApplications(@NonNull PackageManager packageManager) {
        final long startTime = System.nanoTime();
        final Intent intent = new Intent(Intent.ACTION_SEARCH);
        intent.putExtra("query", "blah");
        final List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(intent, 0);
        Collections.sort(queryIntentActivities, new ResolveInfo.DisplayNameComparator(packageManager));
        final ArrayList<Application> arrayList = new ArrayList<>();
        CharSequence label;
        String packageName;
        Drawable icon;
        Application application;
        for (ResolveInfo resolveInfo : queryIntentActivities) {
            if (resolveInfo.activityInfo.exported) {
                label = resolveInfo.loadLabel(packageManager);
                packageName = resolveInfo.activityInfo.packageName;
                icon = resolveInfo.loadIcon(packageManager);
                if (packageName != null && label != null && icon != null) {
                    application = new Application(label, packageName, icon);
                    application.setAction(Intent.ACTION_SEARCH);
                    arrayList.add(application);
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, startTime);
        }
        return insertPlayFromSearch(arrayList, packageManager);
    }

    private static ArrayList<Application> insertPlayFromSearch(@NonNull ArrayList<Application> arrayList, @NonNull PackageManager packageManager) {
        final long startTime = System.nanoTime();
        final Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        intent.putExtra("query", "blah");
        final List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(intent, 0);
        Collections.sort(queryIntentActivities, new ResolveInfo.DisplayNameComparator(packageManager));
        CharSequence label;
        String packageName;
        Drawable icon;
        Application application;
        for (ResolveInfo resolveInfo : queryIntentActivities) {
            if (resolveInfo.activityInfo.exported) {
                label = resolveInfo.loadLabel(packageManager);
                packageName = resolveInfo.activityInfo.packageName;
                icon = resolveInfo.loadIcon(packageManager);
                if (packageName != null && label != null && icon != null) {
                    application = new Application(label, packageName, icon);
                    application.setAction(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
                    arrayList.add(0, application);
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, startTime);
        }
        return arrayList;
    }

    /**
     * Get a list of all of the installed applications that hold the {@link Constants#PERMISSION_CONTROL_SAIY},
     * regardless of whether or not the permission has been explicitly granted. The results exclude our own
     * package.
     *
     * @param ctx the application context
     * @return an ArrayList containing a {@link Pair} with the first parameter containing the application name
     * and the second the package name.
     */
    public static ArrayList<Pair<String, String>> declaresSaiyPermissionLegacy(@NonNull final Context ctx) {

        final ArrayList<Pair<String, String>> holdsPermission = new ArrayList<>();
        final PackageManager pm = ctx.getPackageManager();
        final List<PackageInfo> apps = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        CharSequence appNameChar;
        String appName;
        String[] permissions;
        for (final PackageInfo packageInfo : apps) {

            permissions = packageInfo.requestedPermissions;
            if (permissions != null) {

                for (final String permission : permissions) {
                    if (pCONTROL_SAIY.matcher(permission).matches()) {

                        appNameChar = packageInfo.applicationInfo.loadLabel(pm);

                        if (appNameChar != null) {
                            appName = packageInfo.applicationInfo.loadLabel(pm).toString();

                            if (UtilsString.notNaked(appName)) {
                                if (!pSAIY_PACKAGE.matcher(packageInfo.packageName).matches()) {
                                    holdsPermission.add(new Pair<>(appName, packageInfo.packageName));
                                }
                                break;
                            }
                        }

                        holdsPermission.add(new Pair<>(packageInfo.packageName, packageInfo.packageName));
                    }
                }
            }
        }

        return holdsPermission;
    }

    /**
     * Check if the user has a package installed
     *
     * @param ctx         the application context
     * @param packageName the application package name
     * @return true if the package is installed
     */
    public static boolean isPackageInstalled(@NonNull final Context ctx, @NonNull final String packageName) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "isPackageInstalled");
        }

        try {
            ctx.getApplicationContext().getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (final PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "isPackageInstalled: NameNotFoundException");
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "isPackageInstalled: NullPointerException");
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "isPackageInstalled: Exception");
            }
        }

        return false;
    }

    public static ArrayList<Application> getAccessibleApplications(@NonNull PackageManager packageManager) {
        final long startTime = System.nanoTime();
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(intent, 0);
        Collections.sort(queryIntentActivities, new ResolveInfo.DisplayNameComparator(packageManager));
        final ArrayList<Application> arrayList = new ArrayList<>();
        CharSequence label;
        String packageName;
        Drawable icon;
        for (ResolveInfo resolveInfo : queryIntentActivities) {
            label = resolveInfo.loadLabel(packageManager);
            packageName = resolveInfo.activityInfo.packageName;
            icon = resolveInfo.loadIcon(packageManager);
            if (packageName != null && label != null && icon != null) {
                arrayList.add(new Application(label, packageName, icon));
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, startTime);
        }
        return arrayList;
    }

    /**
     * Check if the user has either of the Shazam applications installed
     *
     * @param ctx the application context
     * @return true if the package is installed
     */
    public static boolean shazamInstalled(@NonNull final Context ctx) {
        try {
            ctx.getApplicationContext().getPackageManager().getApplicationInfo(PACKAGE_SHAZAM, 0);
            return true;
        } catch (final PackageManager.NameNotFoundException e) {
            try {
                ctx.getApplicationContext().getPackageManager().getApplicationInfo(PACKAGE_SHAZAM_ENCORE, 0);
                return true;
            } catch (final PackageManager.NameNotFoundException ee) {
                return false;
            }
        }
    }

    /**
     * Check if the user has either of the Sound Hound applications installed
     *
     * @param ctx the application context
     * @return true if the package is installed
     */
    public static boolean soundHoundInstalled(@NonNull final Context ctx) {
        try {
            ctx.getApplicationContext().getPackageManager().getApplicationInfo(PACKAGE_SOUND_HOUND, 0);
            return true;
        } catch (final PackageManager.NameNotFoundException e) {
            try {
                ctx.getApplicationContext().getPackageManager().getApplicationInfo(PACKAGE_SOUND_HOUND_PREMIUM, 0);
                return true;
            } catch (final PackageManager.NameNotFoundException ee) {
                return false;
            }
        }
    }

    /**
     * Check if the user has either of the Sound Hound applications installed
     *
     * @param ctx the application context
     * @return true if the package is installed
     */
    public static boolean amazonInstalled(@NonNull final Context ctx) {
        try {
            ctx.getApplicationContext().getPackageManager().getApplicationInfo(PACKAGE_AMAZON, 0);
            return true;
        } catch (final PackageManager.NameNotFoundException e) {
            try {
                ctx.getApplicationContext().getPackageManager().getApplicationInfo(PACKAGE_AMAZON_WINDOW_SHOP, 0);
                return true;
            } catch (final PackageManager.NameNotFoundException ignored) {
                return false;
            }
        }
    }

    /**
     * Check to see which supported applications are installed that can recognise music tracks.
     * <p>
     * We don't add both the freemium and premium packages, as they both respond to the same
     * application specific intent.
     * <p>
     * In the future, only applications that respond to saiy.intent.action.MEDIA_RECOGNIZE will
     * be supported.
     *
     * @param ctx the application context
     * @return an {@link ArrayList} containing {@link SongRecognitionProvider} providers
     */
    public static ArrayList<SongRecognitionProvider> getSongRecognitionProviders(@NonNull final Context ctx) {

        final ArrayList<SongRecognitionProvider> providers = new ArrayList<>();

        try {
            ctx.getApplicationContext().getPackageManager().getApplicationInfo(PACKAGE_SHAZAM, 0);
            providers.add(SongRecognitionProvider.SHAZAM);
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Shazam installed");
            }
        } catch (final PackageManager.NameNotFoundException e) {
            try {
                ctx.getApplicationContext().getPackageManager().getApplicationInfo(PACKAGE_SHAZAM_ENCORE, 0);
                providers.add(SongRecognitionProvider.SHAZAM);
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "Shazam Encore installed");
                }
            } catch (final PackageManager.NameNotFoundException ee) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "Shazam variant not installed");
                }
            }
        }

        try {
            ctx.getApplicationContext().getPackageManager().getApplicationInfo(PACKAGE_SOUND_HOUND, 0);
            providers.add(SongRecognitionProvider.SOUND_HOUND);
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Sound Hound installed");
            }
        } catch (final PackageManager.NameNotFoundException e) {
            try {
                ctx.getApplicationContext().getPackageManager().getApplicationInfo(PACKAGE_SOUND_HOUND_PREMIUM, 0);
                providers.add(SongRecognitionProvider.SOUND_HOUND);
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "Sound Hound Premium installed");
                }
            } catch (final PackageManager.NameNotFoundException ee) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "Sound Hound variant not installed");
                }
            }
        }

        try {
            ctx.getApplicationContext().getPackageManager().getApplicationInfo(PACKAGE_GOOGLE_SOUND_SEARCH, 0);
            providers.add(SongRecognitionProvider.GOOGLE);
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Google Sound Search installed");
            }
        } catch (final PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Google Sound Search not installed");
            }
        }

        try {
            ctx.getApplicationContext().getPackageManager().getApplicationInfo(PACKAGE_TRACK_ID, 0);
            providers.add(SongRecognitionProvider.TRACK_ID);
            if (DEBUG) {
                MyLog.d(CLS_NAME, "TrackID installed");
            }
        } catch (final PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "TrackID not installed");
            }
        }

        return providers;
    }

    public static ArrayList<NoteProvider> getEvernote(Context context) {
        final ArrayList<NoteProvider> arrayList = new ArrayList<>(1);
        try {
            context.getApplicationContext().getPackageManager().getApplicationInfo(PACKAGE_EVERNOTE, 0);
            arrayList.add(NoteProvider.EVERNOTE);
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Evernote installed");
            }
        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Evernote not installed");
            }
        }
        return arrayList;
    }

    public static Pair<Boolean, Integer> getVoiceNoteProvidersCount(Context context) {
        final int size = context.getPackageManager().queryIntentActivities(new Intent(NoteProvider.VOICE_NOTE), PackageManager.GET_META_DATA).size();
        return size > 0 ? new Pair<>(true, size) : new Pair<>(false, 0);
    }

    public static Pair<String, String> getVoiceNoteProviders(Context context) {
        final Intent intent = new Intent(NoteProvider.VOICE_NOTE);
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA);
        if (UtilsList.notNaked(queryIntentActivities)) {
            return new Pair<>(queryIntentActivities.get(0).activityInfo.packageName, queryIntentActivities.get(0).loadLabel(packageManager).toString());
        }
        return null;
    }

    /**
     * Get the package name of the user's default launcher
     *
     * @param ctx the application context
     * @return the package name of the default launcher
     */
    public static String getDefaultLauncherPackage(@NonNull final Context ctx) {

        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        final ResolveInfo resolveInfo = ctx.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

        if (resolveInfo != null) {
            return resolveInfo.activityInfo.packageName;
        } else {
            return null;
        }
    }

    /**
     * Check if the user's default launcher is Google Now Launcher
     *
     * @param ctx the application context
     * @return the package name of the default launcher
     */
    public static boolean isGoogleNowLauncherDefault(@NonNull final Context ctx) {

        final String defaultLauncherPackage = getDefaultLauncherPackage(ctx);

        if (DEBUG) {
            MyLog.d(CLS_NAME, "isGoogleNowLauncherDefault: " + defaultLauncherPackage);
        }

        return UtilsString.notNaked(defaultLauncherPackage)
                && defaultLauncherPackage.matches(PACKAGE_NAME_GOOGLE_NOW_LAUNCHER);
    }
}

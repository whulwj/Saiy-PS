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

package ai.saiy.android.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ai.saiy.android.tts.helper.TTSDefaults;

/**
 * Utility class of handy file method. Static for ease of access
 * <p>
 * Created by benrandall76@gmail.com on 10/09/2016.
 */

public class UtilsFile {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = UtilsFile.class.getSimpleName();

    public static final String FILE_PROVIDER = "ai.saiy.android.fileprovider";
    private static final String SOUND_EFFECT_DIR = "/se/";
    @Deprecated private static final String TTS_GOOGLE_DIRECTORY = "/Android/data/" + TTSDefaults.TTS_PKG_NAME_GOOGLE + "/files/";
    private static final String SAIY_DIRECTORY = "/Saiy";
    private static final String SOUND_DIRECTORY = "/Sounds";
    private static final String RELATIVE_SOUND_DIRECTORY = SAIY_DIRECTORY + SOUND_DIRECTORY;
    private static final String IMPORT_DIRECTORY = "/Import";
    private static final String RELATIVE_IMPORT_DIRECTORY = SAIY_DIRECTORY + IMPORT_DIRECTORY;
    private static final String EXPORT_DIRECTORY = "/Export";
    private static final String RELATIVE_EXPORT_DIRECTORY = SAIY_DIRECTORY + EXPORT_DIRECTORY;
    public static final String EXPORT_FILE_SUFFIX = "saiy";
    private static final String QUICK_LAUNCH_FILE = "/saiy_ql.apk";
    private static final String NO_MEDIA_FILE = "/.nomedia";

    /**
     * Prevent instantiation
     */
    public UtilsFile() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    /**
     * Convert a raw resource to a file on the private storage
     *
     * @param ctx        the application context
     * @param resourceId the resource identifier
     * @return the file or null if the process failed
     */
    public static File oggToCacheAndGrant(@NonNull final Context ctx, final int resourceId, final String packageName) {

        final String cachePath = getPrivateDirPath(ctx);

        if (UtilsString.notNaked(cachePath)) {

            final String name = ctx.getResources().getResourceEntryName(resourceId);

            final File file = resourceToFile(ctx, resourceId, new File(cachePath + SOUND_EFFECT_DIR + name
                    + "." + Constants.OGG_AUDIO_FILE_SUFFIX));

            if (file != null) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "wavToCacheAndGrant file exists: " + file.exists());
                    MyLog.i(CLS_NAME, "wavToCacheAndGrant file path: " + file.getAbsolutePath());
                }

                final Uri contentUri = FileProvider.getUriForFile(ctx, FILE_PROVIDER, file);
                ctx.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                return file;
            } else {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "wavToCacheAndGrant file null");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "wavToCacheAndGrant failed to get any path");
            }
        }

        return null;

    }

    /**
     * Utility to copy the contents of a raw resource to a file
     *
     * @param ctx        the application context
     * @param resourceId the resource identifier
     * @param file       the destination file
     * @return the completed file or null if the process failed
     */
    private static File resourceToFile(@NonNull final Context ctx, final int resourceId,
                                       @NonNull final File file) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //NoMethodException: File#toPath()
                FileUtils.copyInputStreamToFile(ctx.getResources().openRawResource(resourceId), file);
            } else {
                try (InputStream inputStream = ctx.getResources().openRawResource(resourceId)) {
                    try (FileOutputStream outputStream = FileUtils.openOutputStream(file)) {
                        IOUtils.copy(inputStream, outputStream);
                    }
                }
            }
            return file;
        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "resourceToFile IOException");
                e.printStackTrace();
            }
        } catch (final Resources.NotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "resourceToFile NotFoundException");
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Attempt to get a directory that does not require permission to read/write. This should be
     * simple but @see <a href="https://code.google.com/p/android/issues/detail?id=81357"/>
     *
     * @param ctx the application context
     * @return the directory or null if all efforts fail.
     */
    public static File getPrivateDir(@NonNull final Context ctx) {

        Pair<Boolean, File> dirPair = getExternalFilesDir(ctx);

        if (dirPair.first) {
            return dirPair.second;
        }

        dirPair = getExternalCacheDir(ctx);

        if (dirPair.first) {
            return dirPair.second;
        }

        dirPair = getCacheDir(ctx);

        if (dirPair.first) {
            return dirPair.second;
        }

        return null;
    }

    /**
     * Attempt to get a directory location that does not require permission to read/write. This should be
     * simple but @see https://code.google.com/p/android/issues/detail?id=81357
     *
     * @param ctx the application context
     * @return the absolute path of the location or null if all efforts fail.
     */
    public static String getPrivateDirPath(@NonNull final Context ctx) {

        final File file = getPrivateDir(ctx);

        if (file != null) {
            return file.getAbsolutePath();
        }

        return null;
    }

    /**
     * Check we can create a file in the desired location by attempting to create a temporary file
     * there.
     *
     * @param ctx the application context
     * @return a {@link Pair} with the first parameter denoting success and the second the directory
     * or null if the process failed.
     */
    private static Pair<Boolean, File> getExternalFilesDir(@NonNull final Context ctx) {

        File tempFile = null;

        try {

            tempFile = File.createTempFile(Constants.DEFAULT_TEMP_FILE_PREFIX,
                    "." + Constants.DEFAULT_TEMP_FILE_SUFFIX, ContextCompat.getExternalFilesDirs(ctx, null)[0]);

            if (tempFile.exists()) {
                return new Pair<>(true, ctx.getExternalFilesDir(null));
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getExternalFilesDir: file does not exist");
                }
            }
        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getExternalFilesDir: IOException");
                e.printStackTrace();
            }
        } catch (final IndexOutOfBoundsException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getExternalFilesDir: IndexOutOfBoundsException");
                e.printStackTrace();
            }
        } finally {

            if (tempFile != null && tempFile.exists()) {
                final boolean deleted = tempFile.delete();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getExternalFilesDir: finally file deleted: " + deleted);
                }
            }
        }

        if (DEBUG) {
            MyLog.w(CLS_NAME, "getExternalFilesDir: failed");
        }

        return new Pair<>(false, null);
    }

    /**
     * Check we can create a file in the desired location by attempting to create a temporary file
     * there.
     *
     * @param ctx the application context
     * @return a {@link Pair} with the first parameter denoting success and the second the directory
     * or null if the process failed.
     */
    private static Pair<Boolean, File> getExternalCacheDir(@NonNull final Context ctx) {

        File tempFile = null;

        try {

            tempFile = File.createTempFile(Constants.DEFAULT_TEMP_FILE_PREFIX,
                    "." + Constants.DEFAULT_TEMP_FILE_SUFFIX, ContextCompat.getExternalCacheDirs(ctx)[0]);

            if (tempFile.exists()) {
                return new Pair<>(true, ctx.getExternalCacheDir());
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getExternalCacheDir: file does not exist");
                }
            }
        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getExternalCacheDir: IOException");
                e.printStackTrace();
            }
        } catch (final IndexOutOfBoundsException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getExternalCacheDir: IndexOutOfBoundsException");
                e.printStackTrace();
            }
        } finally {

            if (tempFile != null && tempFile.exists()) {
                final boolean deleted = tempFile.delete();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getExternalCacheDir: finally file deleted: " + deleted);
                }
            }
        }

        if (DEBUG) {
            MyLog.w(CLS_NAME, "getExternalCacheDir: failed");
        }

        return new Pair<>(false, null);
    }

    /**
     * Check we can create a file in the desired location by attempting to create a temporary file
     * there.
     *
     * @param ctx the application context
     * @return a {@link Pair} with the first parameter denoting success and the second the directory
     * or null if the process failed.
     */
    private static Pair<Boolean, File> getCacheDir(@NonNull final Context ctx) {

        File tempFile = null;

        try {

            tempFile = File.createTempFile(Constants.DEFAULT_TEMP_FILE_PREFIX,
                    "." + Constants.DEFAULT_TEMP_FILE_SUFFIX, ctx.getCacheDir());

            if (tempFile.exists()) {
                return new Pair<>(true, ctx.getCacheDir());
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getCacheDir: file does not exist");
                }
            }
        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getCacheDir: IOException");
                e.printStackTrace();
            }
        } finally {

            if (tempFile != null && tempFile.exists()) {
                final boolean deleted = tempFile.delete();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getCacheDir: finally file deleted: " + deleted);
                }
            }
        }

        if (DEBUG) {
            MyLog.w(CLS_NAME, "getCacheDir: failed");
        }

        return new Pair<>(false, null);
    }

    /**
     * Get a default temporary file to write audio to
     *
     * @param ctx the application context
     * @return the created file
     */
    public static File getTempAudioFile(@NonNull final Context ctx) {

        final File tempFile = getPrivateDir(ctx);

        if (tempFile != null) {

            try {
                return File.createTempFile(Constants.DEFAULT_AUDIO_FILE_PREFIX,
                        "." + Constants.DEFAULT_AUDIO_FILE_SUFFIX, tempFile);
            } catch (final IOException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getTempAudioFile: IOException");
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private static boolean createNewFile(String str) {
        File file = new File(str);
        if (!file.exists()) {
            try {
                boolean createNewFile = file.createNewFile();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "createNoMediaFile: success: " + createNewFile);
                }
            } catch (IOException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "createNoMediaFile: IOException");
                    e.printStackTrace();
                }
                return false;
            }
        }
        return true;
    }

    public static ArrayList<File> sortByLastModified(ArrayList<File> files) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //NoMethodException: File#toPath()
            files.sort(org.apache.commons.io.comparator.LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
        } else {
            files.sort(new Comparator<File>() {
                /**
                 * Compares the last the last modified date/time of two files.
                 *
                 * @param file1 The first file to compare.
                 * @param file2 The second file to compare.
                 * @return a negative value if the first file's last modified date/time is less than the second, zero if the last
                 *         modified date/time are the same and a positive value if the first files last modified date/time is
                 *         greater than the second file.
                 */
                @Override
                public int compare(final File file1, final File file2) {
                    final long result = file1.lastModified() - file2.lastModified();
                    if (result < 0) {
                        return -1;
                    } else if (result > 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
        }
        return files;
    }

    public static File quickLaunchFile() {
        return new File(saiyDirectory() + QUICK_LAUNCH_FILE);
    }

    public static boolean isExternalStorageReadable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || Environment.getExternalStorageDirectory().canRead();
    }

    public static boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || Environment.getExternalStorageDirectory().canWrite();
    }

    public static boolean isSaiyDirectoryExists() {
        File file = new File(Environment.getExternalStorageDirectory() + SAIY_DIRECTORY);
        return file.exists() && file.isDirectory();
    }

    public static boolean createSaiyDirectory() {
        return new File(Environment.getExternalStorageDirectory() + SAIY_DIRECTORY).mkdir();
    }

    public static File saiyDirectory() {
        return new File(Environment.getExternalStorageDirectory() + SAIY_DIRECTORY);
    }

    public static boolean isSoundDirectoryExists() {
        File file = new File(Environment.getExternalStorageDirectory() + RELATIVE_SOUND_DIRECTORY);
        return file.exists() && file.isDirectory();
    }

    public static boolean createSoundDirectory() {
        if (isSaiyDirectoryExists()) {
            return new File(Environment.getExternalStorageDirectory() + RELATIVE_SOUND_DIRECTORY).mkdir();
        }
        boolean result = createSaiyDirectory() && new File(Environment.getExternalStorageDirectory() + SAIY_DIRECTORY + SOUND_DIRECTORY).mkdir();
        if (result) {
            createNewFile(soundDirectory() + NO_MEDIA_FILE);
        }
        return true;
    }

    public static File soundDirectory() {
        return new File(Environment.getExternalStorageDirectory() + RELATIVE_SOUND_DIRECTORY);
    }

    private static boolean createQuickLaunchFile(Context context) {
        File file = resourceToFile(context, ai.saiy.android.R.raw.saiy_ql, quickLaunchFile());
        return file != null && file.exists();
    }

    public static boolean isImportDirectoryExists() {
        File file = new File(Environment.getExternalStorageDirectory() + RELATIVE_IMPORT_DIRECTORY);
        return file.exists() && file.isDirectory();
    }

    public static boolean createImportDirectory() {
        return !isSaiyDirectoryExists() ? createSaiyDirectory() && new File(Environment.getExternalStorageDirectory() + SAIY_DIRECTORY + IMPORT_DIRECTORY).mkdir() : new File(Environment.getExternalStorageDirectory() + RELATIVE_IMPORT_DIRECTORY).mkdir();
    }

    public static File getImportDirectory() {
        return new File(Environment.getExternalStorageDirectory() + RELATIVE_IMPORT_DIRECTORY);
    }

    public static boolean isExportDirectoryExists() {
        File file = new File(Environment.getExternalStorageDirectory() + RELATIVE_EXPORT_DIRECTORY);
        return file.exists() && file.isDirectory();
    }

    public static boolean createExportDirectory() {
        return !isSaiyDirectoryExists() ? createSaiyDirectory() && new File(Environment.getExternalStorageDirectory() + SAIY_DIRECTORY + EXPORT_DIRECTORY).mkdir() : new File(Environment.getExternalStorageDirectory() + RELATIVE_EXPORT_DIRECTORY).mkdir();
    }

    public static File createExportFile(String str) {
        return new File(Environment.getExternalStorageDirectory() + RELATIVE_EXPORT_DIRECTORY + "/" + str);
    }

    public static boolean createDirs(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "createDirs");
        }
        if (!ai.saiy.android.permissions.PermissionHelper.checkFilePermissionsNR(context)) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "createDirs: permission denied");
            }
            return false;
        }
        if (isSaiyDirectoryExists() && isImportDirectoryExists() && isExportDirectoryExists() && isSoundDirectoryExists() && isNoMediaFileExists() && isQuickLaunchFileExists()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "createDirs: all dirs exist: true");
            }
            return true;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "createDirs: dirs missing: false");
        }
        if (!isExternalStorageWritable()) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "createDirs: isExternalStorageWritable: false");
            }
            return false;
        }
        if (isSaiyDirectoryExists()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "createDirs: saiyDirExists: true");
            }
            if (!isQuickLaunchFileExists()) {
                if (createQuickLaunchFile(context)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "createDirs: createQL: success");
                    }
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "createDirs: createQL: failed");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "createDirs: saiyDirExists: false");
            }
            if (!createSaiyDirectory()) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "createDirs: createSaiyDir: failed");
                }
                return false;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "createDirs: createSaiyDir: success");
            }
            if (!isQuickLaunchFileExists()) {
                if (createQuickLaunchFile(context)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "createDirs: createQL: success");
                    }
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "createDirs: createQL: failed");
                }
            }
        }
        if (isSoundDirectoryExists()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "createDirs: soundDirExists: true");
            }
            if (!isNoMediaFileExists()) {
                if (createNewFile(soundDirectory() + NO_MEDIA_FILE)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "createDirs: createNoMediaFile: success");
                    }
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "createDirs: createNoMediaFile: failed");
                }
            }
        } else {
            if (!createSoundDirectory()) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "createDirs: createSoundDir: failed");
                }
                return false;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "createDirs: createSoundDir: success");
            }
            if (!isNoMediaFileExists()) {
                if (createNewFile(soundDirectory() + NO_MEDIA_FILE)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "createDirs: createNoMediaFile: success");
                    }
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "createDirs: createNoMediaFile: failed");
                }
            }
        }
        if (isImportDirectoryExists()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "createDirs: importDirExists: true");
            }
        } else {
            if (!createImportDirectory()) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "createDirs: createImportDir: failed");
                }
                return false;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "createDirs: createImportDir: success");
            }
        }
        if (isExportDirectoryExists()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "createDirs: exportDirExists: true");
            }
        } else {
            if (!createExportDirectory()) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "createDirs: createExportDir: failed");
                }
                return false;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "createDirs: createExportDir: success");
            }
        }
        return true;
    }

    public static @NonNull List<File> getSoundFiles() { //TODO Manifest.permission#READ_MEDIA_AUDIO
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //NoClassDefFoundError: IOFileFilter
                return new ArrayList<>(org.apache.commons.io.FileUtils.listFiles(soundDirectory(), soundFileFilter(), null));
            } else {
                final File[] fileArray = soundDirectory().listFiles((dir, name) -> {
                    if (name != null) {
                        return IOCase.INSENSITIVE.checkEndsWith(name, Constants.DEFAULT_AUDIO_FILE_SUFFIX) || IOCase.INSENSITIVE.checkEndsWith(name, Constants.OGG_AUDIO_FILE_SUFFIX) || IOCase.INSENSITIVE.checkEndsWith(name, Constants.MP3_AUDIO_FILE_SUFFIX);
                    }
                    return false;
                });
                if (fileArray == null|| fileArray.length == 0) {
                    return Collections.emptyList();
                }
                return new ArrayList<>(Arrays.asList(fileArray));
            }
        } catch (Throwable t) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getSoundFiles " + t.getClass().getSimpleName() + "," + t.getMessage());
            }
        }
        return Collections.emptyList();
    }

    public static @Nullable ArrayList<File> getImportFiles() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //NoClassDefFoundError: IOFileFilter
                return new ArrayList<>(org.apache.commons.io.FileUtils.listFiles(getImportDirectory(), importFileFilter(), null));
            } else {
                final File[] fileArray = getImportDirectory().listFiles((dir, name) -> {
                    if (name != null) {
                        return IOCase.INSENSITIVE.checkEndsWith(name, EXPORT_FILE_SUFFIX);
                    }
                    return false;
                });
                if (fileArray == null|| fileArray.length == 0) {
                    return null;
                }
                return new ArrayList<>(Arrays.asList(fileArray));
            }
        } catch (Throwable t) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getSoundFiles " + t.getClass().getSimpleName() + "," + t.getMessage());
            }
        }
        return null;
    }

    private static org.apache.commons.io.filefilter.IOFileFilter importFileFilter() {
        return FileFilterUtils.suffixFileFilter(EXPORT_FILE_SUFFIX, IOCase.INSENSITIVE);
    }

    private static org.apache.commons.io.filefilter.IOFileFilter soundFileFilter() {
        return FileFilterUtils.or(FileFilterUtils.suffixFileFilter(Constants.DEFAULT_AUDIO_FILE_SUFFIX, IOCase.INSENSITIVE), FileFilterUtils.suffixFileFilter(Constants.OGG_AUDIO_FILE_SUFFIX, IOCase.INSENSITIVE), FileFilterUtils.suffixFileFilter(Constants.MP3_AUDIO_FILE_SUFFIX, IOCase.INSENSITIVE));
    }

    private static boolean isQuickLaunchFileExists() {
        return new File(saiyDirectory() + QUICK_LAUNCH_FILE).exists();
    }

    private static boolean isNoMediaFileExists() {
        return new File(soundDirectory() + NO_MEDIA_FILE).exists();
    }
}

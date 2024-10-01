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

package ai.saiy.android.localisation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import java.util.Locale;

import ai.saiy.android.utils.MyLog;

/**
 * Class to manage fetching {@link Resources} for a specific {@link Locale}. API levels less
 * than {@link Build.VERSION_CODES#JELLY_BEAN_MR1} require an ugly implementation.
 * <p>
 * <a href="https://stackoverflow.com/questions/5244889/load-language-specific-string-from-resource"/>
 * <a href="https://stackoverflow.com/questions/9475589/how-to-get-string-from-different-locales-in-android"/>
 * Created by benrandall76@gmail.com on 27/03/2016.
 */
public final class SaiyResources {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SaiyResources.class.getSimpleName();

    private final Context mContext;
    private final AssetManager assetManager;
    private final DisplayMetrics metrics;
    private final Configuration configuration;
    private final Locale targetLocale;

    /**
     * Constructor
     *
     * @param context the application context
     * @param sl       the {@link SupportedLanguage}
     */
    public SaiyResources(@NonNull final Context context, @NonNull final SupportedLanguage sl) {
        this.mContext = context;
        final Resources resources = this.mContext.getResources();
        this.assetManager = resources.getAssets();
        this.metrics = resources.getDisplayMetrics();
        this.configuration = new Configuration(resources.getConfiguration());
        this.targetLocale = sl.getLocale();
    }

    /**
     * Must be called once no further localised resources are required. If it is not,
     * {@link Build.VERSION_CODES#JELLY_BEAN_MR1} devices will have their global system local changed.
     */
    @SuppressLint("AppBundleLocaleChanges")
    public void reset() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "reset");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 && checkNotNull()) {
            configuration.locale = Locale.getDefault(); // reset
            new MyResource(assetManager, metrics, configuration); // reset
        }
    }

    /**
     * Simple check to avoid null pointers prior to resetting.
     *
     * @return if the configuration can be safely reset. False otherwise.
     */
    private boolean checkNotNull() {
        return configuration != null && assetManager != null && metrics != null;
    }

    /**
     * Get a localised string array
     *
     * @param resourceId of the string array
     * @return the string array
     */
    public String[] getStringArray(final int resourceId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getStringArray");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(targetLocale);
            return mContext.createConfigurationContext(configuration).getResources().getStringArray(resourceId);
        } else {
            configuration.locale = targetLocale;
            return new MyResource(assetManager, metrics, configuration).getStringArray(resourceId);
        }
    }

    /**
     * Get a localised string
     *
     * @param resourceId of the string
     * @return the string
     */
    public String getString(final int resourceId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getString");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(targetLocale);
            return mContext.createConfigurationContext(configuration).getResources().getString(resourceId);
        } else {
            configuration.locale = targetLocale;
            return new MyResource(assetManager, metrics, configuration).getString(resourceId);
        }
    }

    /**
     * Only here in case of future functionality requirements.
     * <p>
     * Such as possible resource not found exception handling.
     */
    private static class MyResource extends Resources {
        public MyResource(final AssetManager assets, final DisplayMetrics metrics, final Configuration config) {
            super(assets, metrics, config);
        }
    }
}

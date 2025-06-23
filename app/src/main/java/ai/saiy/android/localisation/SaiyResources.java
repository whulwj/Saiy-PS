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

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import java.util.Locale;

import ai.saiy.android.utils.MyLog;

/**
 * Class to manage fetching {@link Resources} for a specific {@link Locale}.
 * <p>
 * <a href="https://stackoverflow.com/questions/5244889/load-language-specific-string-from-resource"/>
 * <a href="https://stackoverflow.com/questions/9475589/how-to-get-string-from-different-locales-in-android"/>
 * Created by benrandall76@gmail.com on 27/03/2016.
 */
public final class SaiyResources {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SaiyResources.class.getSimpleName();

    private final Context mContext;

    /**
     * Constructor
     *
     * @param context the application context
     * @param sl       the {@link SupportedLanguage}
     */
    public SaiyResources(@NonNull final Context context, @NonNull final SupportedLanguage sl) {
        final Resources resources = context.getResources();
        final Configuration configuration = new Configuration(resources.getConfiguration());
        configuration.setLocale(sl.getLocale());
        this.mContext = context.createConfigurationContext(configuration);
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

        return mContext.getResources().getStringArray(resourceId);
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

        return mContext.getResources().getString(resourceId);
    }
}

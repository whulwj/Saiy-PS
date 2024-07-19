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

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A collection of handy methods. Static for easy access
 * <p/>
 * Created by benrandall76@gmail.com on 07/02/2016.
 */
public class UtilsString {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = UtilsString.class.getSimpleName();

    /**
     * Prevent instantiation
     */
    public UtilsString() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    /**
     * Get a readable string from the {@link InputStream}
     *
     * @param is the {@link InputStream}
     * @return a readable String
     * @throws IOException
     */
    public static String streamToString(@Nullable final InputStream is) throws IOException {

        String output = "";

        if (is != null) {

            final StringBuilder stringBuilder = new StringBuilder();

            String line;

            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            bufferedReader.close();
            is.close();

            output = stringBuilder.toString();
        }

        return output;
    }

    public static String convertProperCase(String str, @NonNull Locale locale) {
        if (!notNaked(str)) {
            return "";
        }
        final String[] separated = str.trim().split("");
        final Pattern question_mark = Pattern.compile("\\?");
        final Pattern exclamation_mark = Pattern.compile("\\!");
        final Pattern period = Pattern.compile("\\.");
        final Pattern space = Pattern.compile(XMLResultsHandler.SEP_SPACE);
        final int length = separated.length - 2;
        for (int i = 0; i < length; i++) {
            if ((question_mark.matcher(separated[i]).matches() || exclamation_mark.matcher(separated[i]).matches() || period.matcher(separated[i]).matches()) && space.matcher(separated[i + 1]).matches()) {
                separated[i + 2] = separated[i + 2].toUpperCase(locale);
            }
            if (i == 1) {
                separated[1] = separated[1].toUpperCase(locale);
            }
        }
        final StringBuilder sb = new StringBuilder();
        for (String separatedString : separated) {
            sb.append(separatedString);
        }
        return sb.toString().replaceAll(" i ", " I ").replaceAll(" i'", " I'");
    }

    /**
     * Utility method to check if a string is null or empty. Purely to prevent clutter wherever such
     * as check is needed.
     *
     * @param toCheck the input String
     * @return true if the String is neither null or empty
     */
    public static boolean notNaked(@Nullable final String toCheck) {
        return toCheck != null && !toCheck.trim().isEmpty();
    }

    /**
     * Remove any spaces before punctuation, left by a response failing to add in the user's name.
     *
     * @param input the input String
     * @return the stripped string
     */
    public static String stripNameSpace(@NonNull final String input) {
        return input.replaceAll(" ,", ",").replaceAll(" \\.", ".").replaceAll(" \\?", "?");
    }

    /**
     * Remove any punctuation from the beginning of utterances that might otherwise be pronounced
     * by the voice engine.
     *
     * @param input the input String
     * @return the stripped string
     */
    public static String stripLeadingPunctuation(@NonNull final String input) {
        return input.trim().matches("\\p{P}.*") ? input.trim().replaceFirst("\\p{P}", "").trim() : input.trim();
    }

    /**
     * Utility method to remove the last instance of a character from a String
     *
     * @param inputString the String to be manipulated
     * @param from        character
     * @param to          character
     * @return the manipulated String
     */
    public static String replaceLast(@NonNull final String inputString, @NonNull final String from,
                                     @NonNull final String to) {

        final int lastIndex = inputString.lastIndexOf(from);

        if (lastIndex >= 0) {
            return inputString.substring(0, lastIndex) + inputString.substring(lastIndex).replaceFirst(from, to);
        } else {
            return inputString;
        }
    }

    public static int stripSpace(String str) {
        String trim = str.trim();
        if (trim.isEmpty()) {
            return 0;
        }
        return trim.split("\\s+").length;
    }

    public static boolean regexCheck(String str) {
        if (str == null) {
            return false;
        }
        try {
            "testsdVVasdasdas".matches(str);
            return true;
        } catch (PatternSyntaxException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "regexCheck: PatternSyntaxException");
                e.printStackTrace();
            }
        } catch (RuntimeException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "regexCheck: RuntimeException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "regexCheck: Exception");
                e.printStackTrace();
            }
        }
        return false;
    }
}

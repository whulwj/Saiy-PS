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

package ai.saiy.android.command.translate.provider.google;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.Translation;
import com.google.cloud.translate.v3.TranslationServiceClient;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;

import ai.saiy.android.configuration.GoogleConfiguration;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 02/05/2016.
 */
public class GoogleTranslateAPI {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = GoogleTranslateAPI.class.getSimpleName();

    /**
     * Perform a synchronous translation request
     *
     * @param ctx      the application context
     * @param text     the text to be translated
     * @param language the {@link TranslationLanguageGoogle}
     * @return a {@link Pair} with the first parameter donating success and the second the result
     */
    public Pair<Boolean, String> execute(@NonNull final Context ctx, @NonNull final String text,
                                         @NonNull final TranslationLanguageGoogle language) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "execute");
        }

        final long then = System.nanoTime();

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        try (TranslationServiceClient client = TranslationServiceClient.create()) {
            // Supported Locations: `global`, [glossary location], or [model location]
            // Glossaries must be hosted in `us-central1`
            // Custom Models must use the same location as your model. (us-central1)
            LocationName parent = LocationName.of(GoogleConfiguration.GOOGLE_TRANSLATE_PROJECT_ID, "global");

            // Supported Mime Types: https://cloud.google.com/translate/docs/supported-formats
            TranslateTextRequest request =
                    TranslateTextRequest.newBuilder()
                            .setParent(parent.toString())
                            .setMimeType("text/plain")
                            .setTargetLanguageCode(language.getLanguage())
                            .addContents(text)
                            .build();

            final TranslateTextResponse response = client.translateText(request);

            if (response != null && !response.getTranslationsList().isEmpty()) {
                if (DEBUG) {
                    for (final Translation resource : response.getTranslationsList()) {
                        MyLog.i(CLS_NAME, "resource: " + resource.getTranslatedText());
                    }
                }

                if (DEBUG) {
                    MyLog.getElapsed(CLS_NAME, then);
                }

                return new Pair<>(true, StringEscapeUtils.unescapeHtml4(response.getTranslationsList().get(0).getTranslatedText()));
            }
        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "execute: IOException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "execute: Exception");
                e.printStackTrace();
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return new Pair<>(false, null);
    }
}

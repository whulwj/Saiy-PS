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

package ai.saiy.android.tts;

import android.content.Context;
import android.os.Build;

import java.util.Locale;

import ai.saiy.android.tts.attributes.Gender;
import ai.saiy.android.utils.MyLog;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Due to misbehaving voice engines, it's necessary to subclass the TTS object here and handle extra
 * eventualities that have caused many crashes along the way.
 * <p/>
 * Additionally, handling the try/catch inside the methods keeps other classes tidy and more readable.
 * <p/>
 * Created by benrandall76@gmail.com on 13/03/2016.
 */
public final class TextToSpeechLegacy extends SaiyTextToSpeech {
    private static final String CLS_NAME = TextToSpeechLegacy.class.getSimpleName();

    /**
     * Constructor
     *
     * @param context the application context
     * @param listener the {@link OnInitListener} to monitor the tts engine
     */
    public TextToSpeechLegacy(final Context context, final OnInitListener listener) {
        super(context, listener);
    }

    /**
     * Constructor
     *
     * @param context the application context
     * @param listener the {@link OnInitListener} to monitor the tts engine
     * @param engine   package name of a requested engine
     */
    public TextToSpeechLegacy(final Context context, final OnInitListener listener, final String engine) {
        super(context, listener, engine);
    }

    @Override
    protected Gender getGender() {
        return Gender.UNDEFINED;
    }

    /**
     * Store the initialised Text to Speech Engine
     */
    @Override
    public void initialised() {
        super.initialised();

/*        if (DEBUG) {
            getInfo();
        }*/
    }

    /**
     * Examine TTS objects in an overly verbose way. Debugging only.
     */
    private void getInfo() {
        MyLog.i(CLS_NAME, "getQuickInfo");

        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    try {
                        final Locale defaultLanguage = TextToSpeechLegacy.this.getDefaultLanguage();
                        MyLog.v(CLS_NAME, "defaultLanguage toString: " + defaultLanguage.toString());
                    } catch (final NullPointerException e) {
                        MyLog.w(CLS_NAME, "NullPointerException");
                        e.printStackTrace();
                    } catch (final Exception e) {
                        MyLog.w(CLS_NAME, "Exception");
                        e.printStackTrace();
                    } finally {
                        try {
                            final Locale languageLocale = TextToSpeechLegacy.this.getLanguage();
                            MyLog.v(CLS_NAME, "languageLocale toString: " + languageLocale.toString());
                        } catch (final NullPointerException e) {
                            MyLog.w(CLS_NAME, "NullPointerException");
                            e.printStackTrace();
                        } catch (final Exception e) {
                            MyLog.w(CLS_NAME, "Exception");
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        final Locale languageLocale = TextToSpeechLegacy.this.getLanguage();
                        MyLog.v(CLS_NAME, "languageLocale toString: " + languageLocale.toString());
                    } catch (final NullPointerException e) {
                        MyLog.w(CLS_NAME, "NullPointerException");
                        e.printStackTrace();
                    } catch (final Exception e) {
                        MyLog.w(CLS_NAME, "Exception");
                        e.printStackTrace();
                    }
                }

                TextToSpeechLegacy.this.getVerboseInfo();
            }
        });
    }
}

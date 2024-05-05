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

package ai.saiy.android.nlu.apiai;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.NLUCoerce;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 03/06/2016.
 */
public class ResolveAPIAI {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ResolveAPIAI.class.getSimpleName();

    private NLUAPIAI nluAPIAI;

    private final ArrayList<String> resultsArray;
    private final float[] confidenceArray;
    private final Locale vrLocale;
    private final Locale ttsLocale;
    private final SupportedLanguage sl;
    private final Context mContext;

    public ResolveAPIAI(@NonNull final Context mContext, @NonNull final SupportedLanguage sl,
                        @NonNull final Locale vrLocale, @NonNull final Locale ttsLocale,
                        @NonNull final float[] confidenceArray,
                        @NonNull final ArrayList<String> resultsArray) {

        this.confidenceArray = confidenceArray;
        this.resultsArray = resultsArray;
        this.vrLocale = vrLocale;
        this.ttsLocale = ttsLocale;
        this.sl = sl;
        this.mContext = mContext;
    }

    public void unpack(@NonNull final DetectIntentResponse response) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "unpacking");
        }

        nluAPIAI = new NLUAPIAI(confidenceArray, resultsArray,
                response.getQueryResult().getIntent().getAction(), response.getQueryResult().getParameters().getFieldsMap());

        new NLUCoerce(getNLUAPIAI(), getContext(), getSupportedLanguage(), getVRLocale(), getTTSLocale(),
                getConfidenceArray(), getResultsArray()).coerce();
    }

    public NLUAPIAI getNLUAPIAI() {
        return nluAPIAI;
    }

    public Locale getVRLocale() {
        return vrLocale;
    }

    public Locale getTTSLocale() {
        return ttsLocale;
    }

    public SupportedLanguage getSupportedLanguage() {
        return sl;
    }

    public ArrayList<String> getResultsArray() {
        return resultsArray;
    }

    public Context getContext() {
        return mContext;
    }

    public float[] getConfidenceArray() {
        return confidenceArray;
    }
}

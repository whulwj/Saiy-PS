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

package ai.saiy.android.custom;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.concurrent.Callable;

import ai.saiy.android.algorithms.Algorithm;
import ai.saiy.android.api.request.Regex;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.service.helper.LocalRequest;

/**
 * Created by benrandall76@gmail.com on 20/04/2016.
 */
public class CustomCommand implements Callable<Boolean> {

    @SerializedName("customAction")
    private final CCC customAction;
    @SerializedName("commandConstant")
    private final CC commandConstant;
    @SerializedName("keyphrase")
    private final String keyphrase;
    @SerializedName("responseSuccess")
    private final String responseSuccess;
    @SerializedName("responseError")
    private final String responseError;
    @SerializedName("ttsLocale")
    private final String ttsLocale;
    @SerializedName("vrLocale")
    private final String vrLocale;
    @SerializedName("action")
    private final int action;

    @SerializedName("intent")
    private String intent;
    @SerializedName("exactMatch")
    private boolean exactMatch;
    @SerializedName("score")
    private double score;
    @SerializedName("utterance")
    private String utterance;
    @SerializedName("algorithm")
    private Algorithm algorithm;
    @SerializedName("regex")
    private Regex regex;
    @SerializedName("regularExpression")
    private String regularExpression;
    @SerializedName("extraText")
    private String extraText;
    @SerializedName("extraText2")
    private String extraText2;
    @SerializedName("actionOfIntent")
    private String actionOfIntent;

    /**
     * Constructor
     *
     * @param customAction    the {@link CCC}
     * @param commandConstant the {@link CC}
     * @param keyphrase       the phrase to trigger the command
     * @param responseSuccess the utterance if the command execution failed
     * @param responseError   the utterance if the command execution was successful
     * @param ttsLocale       the String representation of the Text to Speech {@link java.util.Locale}
     * @param vrLocale        the String representation of the Voice Recognition {@link java.util.Locale}
     * @param action          one of {@link LocalRequest#ACTION_SPEAK_LISTEN}
     *                        or {@link LocalRequest#ACTION_SPEAK_ONLY}
     */
    public CustomCommand(@NonNull final CCC customAction, @NonNull final CC commandConstant,
                         @NonNull final String keyphrase, @NonNull final String responseSuccess,
                         @NonNull final String responseError, @NonNull final String ttsLocale,
                         @NonNull final String vrLocale, final int action) {

        this.customAction = customAction;
        this.commandConstant = commandConstant;
        this.keyphrase = keyphrase;
        this.responseError = responseError;
        this.responseSuccess = responseSuccess;
        this.ttsLocale = ttsLocale;
        this.vrLocale = vrLocale;
        this.action = action;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(@NonNull final String intent) {
        this.intent = intent;
    }

    public int getAction() {
        return action;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(@NonNull final Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public String getUtterance() {
        return utterance;
    }

    public void setUtterance(@NonNull final String utterance) {
        this.utterance = utterance;
    }

    public double getScore() {
        return score;
    }

    public void setScore(final double score) {
        this.score = score;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(final boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    public CCC getCustomAction() {
        return customAction;
    }

    public CC getCommandConstant() {
        return commandConstant;
    }

    public String getKeyphrase() {
        return keyphrase;
    }

    public String getResponseError() {
        return responseError;
    }

    public String getResponseSuccess() {
        return responseSuccess;
    }

    public String getTTSLocale() {
        return ttsLocale;
    }

    public String getVRLocale() {
        return vrLocale;
    }

    public Regex getRegex() {
        return regex == null ? Regex.MATCHES : regex;
    }

    public void setRegex(@NonNull final Regex regex) {
        this.regex = regex;
    }

    public String getRegularExpression() {
        return regularExpression == null ? "" : regularExpression;
    }

    public void setRegularExpression(@NonNull final String regularExpression) {
        this.regularExpression = regularExpression;
    }

    public String getExtraText() {
        return extraText;
    }

    public void setExtraText(@NonNull final String extraText) {
        this.extraText = extraText;
    }

    public String getExtraText2() {
        return extraText2;
    }

    public void setExtraText2(@NonNull final String extraText2) {
        this.extraText2 = extraText2;
    }

    public String getActionOfIntent() {
        return actionOfIntent;
    }

    public void setActionOfIntent(String actionOfIntent) {
        this.actionOfIntent = actionOfIntent;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     */
    @Override
    public Boolean call() {
        return null;
    }
}

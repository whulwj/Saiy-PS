package ai.saiy.android.custom.exports;

import com.google.gson.annotations.SerializedName;

import ai.saiy.android.algorithms.Algorithm;
import ai.saiy.android.api.request.Regex;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.custom.CCC;

public class CustomCommandExport {
    @SerializedName("action")
    private final int action;
    @SerializedName("algorithm")
    private Algorithm algorithm;
    @SerializedName("commandConstant")
    private final CC commandConstant;
    @SerializedName("customAction")
    private final CCC customAction;
    @SerializedName("exportConfiguration")
    private ExportConfiguration exportConfiguration;
    @SerializedName("extraText")
    private String extraText;
    @SerializedName("extraText2")
    private String extraText2;
    @SerializedName("intent")
    private String intent;
    @SerializedName("keyphrase")
    private final String keyphrase;
    @SerializedName("regex")
    private Regex regex;
    @SerializedName("regularExpression")
    private String regularExpression;
    @SerializedName("responseError")
    private final String responseError;
    @SerializedName("responseSuccess")
    private final String responseSuccess;
    @SerializedName("ttsLocale")
    private final String ttsLocale;
    @SerializedName("vrLocale")
    private final String vrLocale;

    public CustomCommandExport(CCC ccc, CC cc, String keyphrase, String responseSuccess, String responseError, String ttsLocale, String vrLocale, int action) {
        this.customAction = ccc;
        this.commandConstant = cc;
        this.keyphrase = keyphrase;
        this.responseError = responseError;
        this.responseSuccess = responseSuccess;
        this.ttsLocale = ttsLocale;
        this.vrLocale = vrLocale;
        this.action = action;
    }

    public int getAction() {
        return this.action;
    }

    public Algorithm getAlgorithm() {
        return this.algorithm;
    }

    public CC getCommandConstant() {
        return this.commandConstant;
    }

    public CCC getCustomAction() {
        return this.customAction;
    }

    public ExportConfiguration getExportConfiguration() {
        return this.exportConfiguration;
    }

    public String getExtraText() {
        return this.extraText;
    }

    public String getExtraText2() {
        return this.extraText2;
    }

    public String getIntent() {
        return this.intent;
    }

    public String getKeyphrase() {
        return this.keyphrase;
    }

    public Regex getRegex() {
        return this.regex == null ? Regex.MATCHES : this.regex;
    }

    public String getRegularExpression() {
        return this.regularExpression == null ? "" : this.regularExpression;
    }

    public String getResponseError() {
        return this.responseError;
    }

    public String getResponseSuccess() {
        return this.responseSuccess;
    }

    public String getTTSLocale() {
        return this.ttsLocale;
    }

    public String getVRLocale() {
        return this.vrLocale;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public void setExportConfiguration(ExportConfiguration exportConfiguration) {
        this.exportConfiguration = exportConfiguration;
    }

    public void setExtraText(String str) {
        this.extraText = str;
    }

    public void setExtraText2(String str) {
        this.extraText2 = str;
    }

    public void setIntent(String str) {
        this.intent = str;
    }

    public void setRegex(Regex regex) {
        this.regex = regex;
    }

    public void setRegularExpression(String str) {
        this.regularExpression = str;
    }
}

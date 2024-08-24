package ai.saiy.android.custom.exports;

import com.google.gson.annotations.SerializedName;

public class CustomPhraseExport {
    @SerializedName("exportConfiguration")
    private ExportConfiguration exportConfiguration;
    @SerializedName("keyphrase")
    private String keyphrase;
    @SerializedName("response")
    private String response;
    @SerializedName("voiceRecognition")
    private boolean voiceRecognition;

    public CustomPhraseExport(String keyphrase, String response, boolean condition) {
        this.keyphrase = keyphrase;
        this.response = response;
        this.voiceRecognition = condition;
    }

    public ExportConfiguration getExportConfiguration() {
        return this.exportConfiguration;
    }

    public String getKeyphrase() {
        return this.keyphrase;
    }

    public String getResponse() {
        return this.response;
    }

    public boolean getStartVoiceRecognition() {
        return this.voiceRecognition;
    }

    public void setExportConfiguration(ExportConfiguration exportConfiguration) {
        this.exportConfiguration = exportConfiguration;
    }

    public void setKeyphrase(String keyphrase) {
        this.keyphrase = keyphrase;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setStartVoiceRecognition(boolean condition) {
        this.voiceRecognition = condition;
    }
}

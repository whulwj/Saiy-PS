package ai.saiy.android.custom.exports;

import java.io.Serializable;

public class CustomPhraseExport implements Serializable {
    private static final long serialVersionUID = 1432642933816444252L;
    private ExportConfiguration exportConfiguration;
    private String keyphrase;
    private String response;
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

package ai.saiy.android.custom.exports;

import com.google.gson.annotations.SerializedName;

public class CustomReplacementExport {
    @SerializedName("exportConfiguration")
    private ExportConfiguration exportConfiguration;
    @SerializedName("keyphrase")
    private String keyphrase;
    @SerializedName("replacement")
    private String replacement;

    public CustomReplacementExport(String keyphrase, String replacement) {
        this.keyphrase = keyphrase;
        this.replacement = replacement;
    }

    public ExportConfiguration getExportConfiguration() {
        return this.exportConfiguration;
    }

    public String getKeyphrase() {
        return this.keyphrase;
    }

    public String getReplacement() {
        return this.replacement;
    }

    public void setExportConfiguration(ExportConfiguration exportConfiguration) {
        this.exportConfiguration = exportConfiguration;
    }

    public void setKeyphrase(String keyphrase) {
        this.keyphrase = keyphrase;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }
}

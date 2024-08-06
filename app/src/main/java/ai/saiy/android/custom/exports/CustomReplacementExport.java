package ai.saiy.android.custom.exports;

import java.io.Serializable;

public class CustomReplacementExport implements Serializable {
    private static final long serialVersionUID = -5926685237621915003L;
    private ExportConfiguration exportConfiguration;
    private String keyphrase;
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

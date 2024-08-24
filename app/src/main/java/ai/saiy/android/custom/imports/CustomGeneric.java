package ai.saiy.android.custom.imports;

import com.google.gson.annotations.SerializedName;

import ai.saiy.android.custom.exports.ExportConfiguration;

public class CustomGeneric {
    @SerializedName("exportConfiguration")
    private final ExportConfiguration exportConfiguration;

    public CustomGeneric(ExportConfiguration exportConfiguration) {
        this.exportConfiguration = exportConfiguration;
    }

    public ExportConfiguration getExportConfiguration() {
        return this.exportConfiguration;
    }
}

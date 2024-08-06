package ai.saiy.android.custom.imports;

import java.io.Serializable;

import ai.saiy.android.custom.exports.ExportConfiguration;

public class CustomGeneric implements Serializable {
    private static final long serialVersionUID = -4038692259311840903L;
    private final ExportConfiguration exportConfiguration;

    public CustomGeneric(ExportConfiguration exportConfiguration) {
        this.exportConfiguration = exportConfiguration;
    }

    public ExportConfiguration getExportConfiguration() {
        return this.exportConfiguration;
    }
}

package ai.saiy.android.custom;

import com.google.gson.annotations.SerializedName;

public class CustomReplacement {
    @SerializedName("keyphrase")
    private String keyphrase;
    @SerializedName("replacement")
    private String replacement;
    @SerializedName("rowId")
    private long rowId;
    private transient String serialised;

    public CustomReplacement(String keyphrase, String replacement) {
        this.keyphrase = keyphrase;
        this.replacement = replacement;
    }

    public CustomReplacement(String keyphrase, String replacement, long id) {
        this.keyphrase = keyphrase;
        this.replacement = replacement;
        this.rowId = id;
    }

    public CustomReplacement(String keyphrase, String replacement, long id, String serialised) {
        this.keyphrase = keyphrase;
        this.replacement = replacement;
        this.rowId = id;
        this.serialised = serialised;
    }

    public String getKeyphrase() {
        return this.keyphrase;
    }

    public String getReplacement() {
        return this.replacement;
    }

    public long getRowId() {
        return this.rowId;
    }

    public String getSerialised() {
        return this.serialised != null ? this.serialised : "";
    }

    public void setKeyphrase(String keyphrase) {
        this.keyphrase = keyphrase;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    public void setRowId(long id) {
        this.rowId = id;
    }

    public void setSerialised(String serialised) {
        this.serialised = serialised;
    }
}

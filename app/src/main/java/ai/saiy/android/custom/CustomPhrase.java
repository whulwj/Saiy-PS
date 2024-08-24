package ai.saiy.android.custom;

import com.google.gson.annotations.SerializedName;

public class CustomPhrase {
    @SerializedName("keyphrase")
    private String keyphrase;
    @SerializedName("response")
    private String response;
    @SerializedName("rowId")
    private long rowId;
    private transient String serialised;
    @SerializedName("voiceRecognition")
    private boolean voiceRecognition;

    public CustomPhrase(String keyphrase, String response, boolean condition) {
        this.keyphrase = keyphrase;
        this.response = response;
        this.voiceRecognition = condition;
    }

    public CustomPhrase(String keyphrase, String response, boolean condition, long id) {
        this.keyphrase = keyphrase;
        this.response = response;
        this.rowId = id;
        this.voiceRecognition = condition;
    }

    public CustomPhrase(String keyphrase, String response, boolean condition, long id, String serialised) {
        this.keyphrase = keyphrase;
        this.response = response;
        this.rowId = id;
        this.serialised = serialised;
        this.voiceRecognition = condition;
    }

    public String getKeyphrase() {
        return this.keyphrase;
    }

    public String getResponse() {
        return this.response;
    }

    public long getRowId() {
        return this.rowId;
    }

    public String getSerialised() {
        return this.serialised != null ? this.serialised : "";
    }

    public boolean getStartVoiceRecognition() {
        return this.voiceRecognition;
    }

    public void setKeyphrase(String keyphrase) {
        this.keyphrase = keyphrase;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setRowId(long id) {
        this.rowId = id;
    }

    public void setSerialised(String serialised) {
        this.serialised = serialised;
    }

    public void setStartVoiceRecognition(boolean condition) {
        this.voiceRecognition = condition;
    }
}

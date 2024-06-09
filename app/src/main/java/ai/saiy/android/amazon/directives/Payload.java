package ai.saiy.android.amazon.directives;

import com.google.gson.annotations.SerializedName;

import ai.saiy.android.utils.UtilsString;

public class Payload {
    @SerializedName("url")
    private final String url;
    @SerializedName("format")
    private final String format;

    @SerializedName("profile")
    private String profile;

    @SerializedName("token")
    private final String token;

    public Payload(String url, String format, String token) {
        this.url = url;
        this.format = format;
        this.token = token;
    }

    public void setProfile(String str) {
        this.profile = str;
    }

    public boolean isCancel() {
        return UtilsString.notNaked(this.url) && this.url.contains("CancelAction");
    }

    public boolean isAbandon() {
        return (UtilsString.notNaked(this.url) && this.url.contains("ActionableAbandon")) || (UtilsString.notNaked(this.token) && this.token.contains("Fallback"));
    }
}

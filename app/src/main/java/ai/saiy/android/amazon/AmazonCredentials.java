package ai.saiy.android.amazon;

import com.google.gson.annotations.SerializedName;

public class AmazonCredentials {
    static final String REFRESH_TOKEN = "refresh_token";

    @SerializedName("access_token")
    private final String accessToken;
    @SerializedName("refresh_token")
    private final String refreshToken;
    @SerializedName("expires_in")
    private final long expiryTime;
    @SerializedName("token_type")
    private final String tokenType;

    public AmazonCredentials(String accessToken, String refreshToken, long expiryTime, String tokenType) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiryTime = expiryTime;
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public long getExpiryTime() {
        return this.expiryTime;
    }
}

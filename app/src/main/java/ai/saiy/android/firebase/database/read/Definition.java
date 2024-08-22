package ai.saiy.android.firebase.database.read;

@com.google.firebase.database.IgnoreExtraProperties
public class Definition {
    @com.google.firebase.database.PropertyName("api_key")
    public String apiKey;

    @com.google.firebase.database.Exclude
    public String getApiKey() {
        return this.apiKey;
    }
}

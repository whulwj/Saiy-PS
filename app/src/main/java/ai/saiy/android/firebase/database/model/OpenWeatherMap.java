package ai.saiy.android.firebase.database.model;

@com.google.firebase.database.IgnoreExtraProperties
public class OpenWeatherMap {
    @com.google.firebase.database.PropertyName("api_key")
    public String apiKey;

    @com.google.firebase.database.Exclude
    public String getApiKey() {
        return this.apiKey;
    }
}

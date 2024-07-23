package ai.saiy.android.firebase.database.model;

@com.google.firebase.database.IgnoreExtraProperties
public class WeatherProvider {
    public static final int WEATHER_ONLINE = 1;
    public static final int OPEN_WEATHER_MAP = 2;

    @com.google.firebase.database.PropertyName("default_provider")
    public int defaultProvider;

    @com.google.firebase.database.Exclude
    public int getDefaultProvider() {
        return this.defaultProvider;
    }
}

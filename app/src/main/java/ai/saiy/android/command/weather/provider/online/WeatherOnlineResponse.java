package ai.saiy.android.command.weather.provider.online;

public class WeatherOnlineResponse {
    @com.google.gson.annotations.SerializedName("data")
    private final Data data;

    public WeatherOnlineResponse(Data data) {
        this.data = data;
    }

    public Data getData() {
        return this.data;
    }
}

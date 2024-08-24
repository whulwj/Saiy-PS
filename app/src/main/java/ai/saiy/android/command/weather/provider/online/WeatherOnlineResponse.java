package ai.saiy.android.command.weather.provider.online;

import com.google.gson.annotations.SerializedName;

public class WeatherOnlineResponse {
    @SerializedName("data")
    private final Data data;

    public WeatherOnlineResponse(Data data) {
        this.data = data;
    }

    public Data getData() {
        return this.data;
    }
}

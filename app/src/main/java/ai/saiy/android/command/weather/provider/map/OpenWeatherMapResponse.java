package ai.saiy.android.command.weather.provider.map;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OpenWeatherMapResponse {
    @SerializedName("weather")
    private final List<Description> weather;
    @SerializedName("main")
    private final Temperature temperature;
    @SerializedName("name")
    private final String name;

    public OpenWeatherMapResponse(List<Description> weather, Temperature temperature, String name) {
        this.weather = weather;
        this.temperature = temperature;
        this.name = name;
    }

    public List<Description> getWeather() {
        return this.weather;
    }

    public Temperature getTemperature() {
        return this.temperature;
    }

    public String getName() {
        return this.name;
    }
}

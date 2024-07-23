package ai.saiy.android.command.weather.provider.map;

import java.util.List;

public class OpenWeatherMapResponse {
    @com.google.gson.annotations.SerializedName("weather")
    private final List<Description> weather;
    @com.google.gson.annotations.SerializedName("main")
    private final Temperature temperature;
    @com.google.gson.annotations.SerializedName("name")
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

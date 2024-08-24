package ai.saiy.android.command.weather.provider.online;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Condition {
    @SerializedName("temp_C")
    private final String temperatureCelsius;
    @SerializedName("temp_F")
    private final String temperatureFahrenheit;
    @SerializedName("weatherDesc")
    private final List<Value> weatherDescription;

    public Condition(String temperatureCelsius, String temperatureFahrenheit, List<Value> weatherDescription) {
        this.temperatureCelsius = temperatureCelsius;
        this.temperatureFahrenheit = temperatureFahrenheit;
        this.weatherDescription = weatherDescription;
    }

    public List<Value> getWeatherDescription() {
        return this.weatherDescription;
    }

    public String getTemperatureCelsius() {
        return this.temperatureCelsius;
    }

    public String getTemperatureFahrenheit() {
        return this.temperatureFahrenheit;
    }
}

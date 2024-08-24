package ai.saiy.android.command.weather.provider.map;

import com.google.gson.annotations.SerializedName;

public class Temperature {
    private static final double ABSOLUTE_ZERO_IN_FAHRENHEIT = -459.67d;
    private static final double ABSOLUTE_ZERO_IN_CELSIUS = -273.15d;

    @SerializedName("temp")
    private final double temperatureKelvin;

    public Temperature(double temperatureKelvin) {
        this.temperatureKelvin = temperatureKelvin;
    }

    public double getTemperatureCelsius() {
        try {
            return temperatureKelvin + ABSOLUTE_ZERO_IN_CELSIUS;
        } catch (NumberFormatException e) {
            return -99.0d;
        }
    }

    public double getTemperatureFahrenheit() {
        try {
            return ((temperatureKelvin * 9.0d) / 5.0d) + ABSOLUTE_ZERO_IN_FAHRENHEIT;
        } catch (NumberFormatException e) {
            return -99.0d;
        }
    }
}

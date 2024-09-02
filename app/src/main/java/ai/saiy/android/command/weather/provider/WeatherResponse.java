package ai.saiy.android.command.weather.provider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ai.saiy.android.command.weather.provider.map.Temperature;
import ai.saiy.android.command.weather.provider.map.OpenWeatherMapResponse;
import ai.saiy.android.command.weather.provider.online.Condition;
import ai.saiy.android.command.weather.provider.online.WeatherOnlineResponse;
import ai.saiy.android.utils.MyLog;

public class WeatherResponse {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = WeatherResponse.class.getSimpleName();

    private int temperatureCelsius;
    private int temperatureFahrenheit;
    private String description;
    private String query;

    public static @Nullable WeatherResponse getResponse(@NonNull OpenWeatherMapResponse openWeatherMapResponse) {
        if (DEBUG) {
           MyLog.i(CLS_NAME, "getResponse OpenWeatherMapResponse");
        }
        try {
            final WeatherResponse weatherResponse = new WeatherResponse();
            final String description = openWeatherMapResponse.getWeather().get(0).getDescription();
            weatherResponse.setDescription(description);
            final Temperature temperature = openWeatherMapResponse.getTemperature();
            final int temperatureCelsius = BigDecimal.valueOf(temperature.getTemperatureCelsius()).setScale(0, RoundingMode.HALF_UP).intValue();
            final int temperatureFahrenheit = BigDecimal.valueOf(temperature.getTemperatureFahrenheit()).setScale(0, RoundingMode.HALF_UP).intValue();
            weatherResponse.setTemperatureCelsius(temperatureCelsius);
            weatherResponse.setTemperatureFahrenheit(temperatureFahrenheit);
            final String query = openWeatherMapResponse.getName();
            weatherResponse.setQuery(query);
            if (DEBUG) {
               MyLog.i(CLS_NAME, "tempC: " + temperatureCelsius);
               MyLog.i(CLS_NAME, "tempF: " + temperatureFahrenheit);
               MyLog.i(CLS_NAME, "description: " + description);
               MyLog.i(CLS_NAME, "query: " + query);
            }
            return weatherResponse;
        } catch (IndexOutOfBoundsException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "IndexOutOfBoundsException");
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            }
        } catch (NumberFormatException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NumberFormatException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }
        return null;
    }

    public static @Nullable WeatherResponse getResponse(@NonNull WeatherOnlineResponse weatherOnlineResponse) {
        if (DEBUG) {
           MyLog.i(CLS_NAME, "getResponse WeatherOnlineResponse");
        }
        try {
            final WeatherResponse weatherResponse = new WeatherResponse();
            final Condition condition = weatherOnlineResponse.getData().getCurrentCondition().get(0);
            final int temperatureCelsius = BigDecimal.valueOf(Double.parseDouble(condition.getTemperatureCelsius())).setScale(0, RoundingMode.HALF_UP).intValue();
            final int temperatureFahrenheit = BigDecimal.valueOf(Double.parseDouble(condition.getTemperatureFahrenheit())).setScale(0, RoundingMode.HALF_UP).intValue();
            weatherResponse.setTemperatureCelsius(temperatureCelsius);
            weatherResponse.setTemperatureFahrenheit(temperatureFahrenheit);
            final String description = condition.getWeatherDescription().get(0).getValue();
            weatherResponse.setDescription(description);
            final String query = weatherOnlineResponse.getData().getRequest().get(0).getQuery();
            weatherResponse.setQuery(query);
            if (DEBUG) {
               MyLog.i(CLS_NAME, "tempC: " + temperatureCelsius);
               MyLog.i(CLS_NAME, "tempF: " + temperatureFahrenheit);
               MyLog.i(CLS_NAME, "description: " + description);
               MyLog.i(CLS_NAME, "query: " + query);
            }
            return weatherResponse;
        } catch (IndexOutOfBoundsException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "IndexOutOfBoundsException");
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            }
        } catch (NumberFormatException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NumberFormatException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getDescription() {
        return this.description;
    }

    public void setTemperatureCelsius(int temperature) {
        this.temperatureCelsius = temperature;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTemperatureCelsius() {
        return this.temperatureCelsius;
    }

    public void setTemperatureFahrenheit(int temperature) {
        this.temperatureFahrenheit = temperature;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getTemperatureFahrenheit() {
        return this.temperatureFahrenheit;
    }

    public String getQuery() {
        return this.query;
    }
}

package ai.saiy.android.command.weather;

import android.content.Context;
import android.location.Location;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.command.battery.BatteryInformation;
import ai.saiy.android.command.location.helper.FusedLocationHelper;
import ai.saiy.android.command.weather.provider.WeatherResponse;
import ai.saiy.android.command.weather.provider.map.OpenWeatherMapHelper;
import ai.saiy.android.command.weather.provider.map.OpenWeatherMapResponse;
import ai.saiy.android.command.weather.provider.online.WeatherOnlineHelper;
import ai.saiy.android.command.weather.provider.online.WeatherOnlineResponse;
import ai.saiy.android.firebase.database.read.WeatherProvider;
import ai.saiy.android.firebase.database.reference.OpenWeatherMapReference;
import ai.saiy.android.firebase.database.reference.WeatherOnlineReference;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

public class CommandWeather {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandWeather.class.getSimpleName();

    private long then;

    /**
     * A single point of return to check the elapsed time for debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private Outcome returnOutcome(@NonNull Outcome outcome) {
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }

    private String getUrl(Context context, @NonNull Location location) {
        Pair<Boolean, String> authPair;
        if (SPH.getWeatherProvider(context) == WeatherProvider.WEATHER_ONLINE) {
            authPair = new WeatherOnlineReference().getAPIKey(context);
            if (authPair.first) {
                return "https://api.worldweatheronline.com/premium/v1/weather.ashx?key=" + authPair.second + "&q=" + location.getLatitude() + XMLResultsHandler.SEP_COMMA + location.getLongitude() + "&format=json&num_of_days=1";
            }
            if (DEBUG) {
                MyLog.w(CLS_NAME, "authPair error");
            }
            return "https://api.worldweatheronline.com/premium/v1/weather.ashx?key=&q=" + location.getLatitude() + XMLResultsHandler.SEP_COMMA + location.getLongitude() + "&format=json&num_of_days=1";
        }
        authPair = new OpenWeatherMapReference().getAPIKey(context);
        if (authPair.first) {
            return "http://api.openweathermap.org/data/2.5/weather?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&appid=" + authPair.second;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "authPair error");
        }
        return "http://api.openweathermap.org/data/2.5/weather?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&appid=";
    }

    private String getUrl(Context context, String query) {
        Pair<Boolean, String> authPair;
        if (SPH.getWeatherProvider(context) == WeatherProvider.WEATHER_ONLINE) {
            authPair = new WeatherOnlineReference().getAPIKey(context);
            if (authPair.first) {
                return "https://api.worldweatheronline.com/premium/v1/weather.ashx?key=" + authPair.second + "&q=" + query.trim().replaceAll("\\s", "%20").trim() + "&format=json&num_of_days=1";
            }
            if (DEBUG) {
                MyLog.w(CLS_NAME, "authPair error");
            }
            return "https://api.worldweatheronline.com/premium/v1/weather.ashx?key=&q=" + query.trim().replaceAll("\\s", "%20").trim() + "&format=json&num_of_days=1";
        }
        authPair = new OpenWeatherMapReference().getAPIKey(context);
        if (authPair.first) {
            return "http://api.openweathermap.org/data/2.5/weather?q=" + query.trim().replaceAll("\\s", "%20").trim() + "&appid=" + authPair.second;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "authPair error");
        }
        return "http://api.openweathermap.org/data/2.5/weather?q=" + query.trim().replaceAll("\\s", "%20").trim() + "&appid=";
    }

    public @NonNull Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
           MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        final ArrayList<String> queries = new ArrayList<>();
        final ArrayList<String> urls = new ArrayList<>();
        if (cr.isResolved()) {
            if (DEBUG) {
               MyLog.i(CLS_NAME, "isResolved: true");
            }
            final CommandWeatherValues commandWeatherValues = (CommandWeatherValues) cr.getVariableData();
            if (UtilsString.notNaked(commandWeatherValues.getQuery())) {
                queries.add(commandWeatherValues.getQuery());
            }
        } else {
            if (DEBUG) {
               MyLog.i(CLS_NAME, "isResolved: false");
            }
            queries.addAll(new Weather(supportedLanguage).sort(context, voiceData));
        }
        boolean isIndoor;
        if (UtilsList.notNaked(queries)) {
            for (String query : queries) {
                urls.add(getUrl(context, query));
            }
            isIndoor = true;
        } else if (!ai.saiy.android.permissions.PermissionHelper.checkLocationPermissions(context, cr.getBundle())) {
            outcome.setUtterance(SaiyRequestParams.SILENCE);
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        } else {
            Location location;
            if (SPH.getLocationProvider(context) == Constants.DEFAULT_LOCATION_PROVIDER) {
                final ai.saiy.android.command.location.LocationHelper locationHelper = new ai.saiy.android.command.location.LocationHelper();
                location = locationHelper.getLastKnownLocation(context);
            } else {
                final FusedLocationHelper fusedLocationHelper = new FusedLocationHelper();
                fusedLocationHelper.prepare(context);
                location = fusedLocationHelper.getLastLocation();
                fusedLocationHelper.destroy();
            }
            if (location != null) {
                urls.add(getUrl(context, location));
                isIndoor = true;
            } else {
                outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getLocationAccessError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            }
        }
        if (UtilsList.notNaked(urls)) {
            final WeatherResponse weatherResponse = getResponse(context, urls);
            if (weatherResponse != null) {
                int temperature;
                String scaleString;
                if (SPH.getDefaultTemperatureUnits(context) == BatteryInformation.FAHRENHEIT) {
                    scaleString = context.getString(R.string.fahrenheit);
                    temperature = weatherResponse.getTemperatureFahrenheit();
                } else {
                    scaleString = context.getString(R.string.celsius);
                    temperature = weatherResponse.getTemperatureCelsius();
                }
                if (isIndoor) {
                    outcome.setUtterance(context.getString(R.string.in) + XMLResultsHandler.SEP_SPACE + weatherResponse.getQuery() + ", " + context.getString(R.string.it_s) + XMLResultsHandler.SEP_SPACE + temperature + XMLResultsHandler.SEP_SPACE + context.getString(R.string.degrees) + XMLResultsHandler.SEP_SPACE + scaleString + ", " + context.getString(R.string.and) + XMLResultsHandler.SEP_SPACE + weatherResponse.getDescription());
                } else {
                    outcome.setUtterance(context.getString(R.string.outside) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.it_s) + XMLResultsHandler.SEP_SPACE + temperature + XMLResultsHandler.SEP_SPACE + context.getString(R.string.degrees) + XMLResultsHandler.SEP_SPACE + scaleString + ", " + context.getString(R.string.and) + XMLResultsHandler.SEP_SPACE + weatherResponse.getDescription());
                }
                outcome.setOutcome(Outcome.SUCCESS);
                return returnOutcome(outcome);
            }
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getWeatherError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getWeatherError(context, supportedLanguage));
        outcome.setOutcome(Outcome.FAILURE);
        return returnOutcome(outcome);
    }

    public WeatherResponse getResponse(Context context, ArrayList<String> urls) {
        if (SPH.getWeatherProvider(context) == WeatherProvider.WEATHER_ONLINE) {
            final Pair<Boolean, WeatherOnlineResponse> weatherOnlineResponsePair = new WeatherOnlineHelper().execute(urls);
            if (weatherOnlineResponsePair.first) {
                return WeatherResponse.getResponse(weatherOnlineResponsePair.second);
            }
            return null;
        }
        final Pair<Boolean, OpenWeatherMapResponse> weatherMapResponsePair = new OpenWeatherMapHelper().execute(urls);
        if (weatherMapResponsePair.first) {
            return WeatherResponse.getResponse(weatherMapResponsePair.second);
        }
        return null;
    }
}

package ai.saiy.android.command.time.online;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ai.saiy.android.command.time.online.model.TimeResponse;
import ai.saiy.android.utils.MyLog;

public class WeatherOnlineTimeResponse {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = WeatherOnlineTimeResponse.class.getSimpleName();

    private String time;
    private String location;

    public static @Nullable WeatherOnlineTimeResponse getResponse(@NonNull TimeResponse timeResponse) {
        if (DEBUG) {
           MyLog.i(CLS_NAME, "getResponse WeatherOnlineTimeResponse");
        }
        try {
            final WeatherOnlineTimeResponse weatherOnlineTimeResponse = new WeatherOnlineTimeResponse();
            weatherOnlineTimeResponse.setTime(timeResponse.getData().getTimeZone().get(0).getLocalTime());
            weatherOnlineTimeResponse.setLocation(timeResponse.getData().getRequest().get(0).getQuery());
            if (DEBUG) {
               MyLog.i(CLS_NAME, "location: " + weatherOnlineTimeResponse.getLocation());
               MyLog.i(CLS_NAME, "time: " + weatherOnlineTimeResponse.getTime());
            }
            return weatherOnlineTimeResponse;
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

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

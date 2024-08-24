package ai.saiy.android.command.time.online.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {
    @SerializedName("time_zone")
    private final List<LocalTime> timeZone;

    @SerializedName("request")
    private final List<Query> request;

    public Data(List<LocalTime> timeZone, List<Query> request) {
        this.timeZone = timeZone;
        this.request = request;
    }

    public List<LocalTime> getTimeZone() {
        return this.timeZone;
    }

    public List<Query> getRequest() {
        return this.request;
    }
}

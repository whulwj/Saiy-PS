package ai.saiy.android.command.time.online.model;

import java.util.List;

public class Data {
    @com.google.gson.annotations.SerializedName("time_zone")
    private final List<LocalTime> timeZone;

    @com.google.gson.annotations.SerializedName("request")
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

package ai.saiy.android.command.weather.provider.online;

import java.util.List;

public class Data {
    @com.google.gson.annotations.SerializedName("current_condition")
    private final List<Condition> currentCondition;
    @com.google.gson.annotations.SerializedName("request")
    private final List<Query> request;

    public Data(List<Condition> currentCondition, List<Query> request) {
        this.currentCondition = currentCondition;
        this.request = request;
    }

    public List<Condition> getCurrentCondition() {
        return this.currentCondition;
    }

    public List<Query> getRequest() {
        return this.request;
    }
}

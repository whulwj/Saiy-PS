package ai.saiy.android.command.weather.provider.online;

import com.google.gson.annotations.SerializedName;

public class Query {
    @SerializedName("query")
    private final String query;

    public Query(String query) {
        this.query = query;
    }

    public String getQuery() {
        return this.query;
    }
}

package ai.saiy.android.command.weather.provider.online;

public class Query {
    @com.google.gson.annotations.SerializedName("query")
    private final String query;

    public Query(String query) {
        this.query = query;
    }

    public String getQuery() {
        return this.query;
    }
}

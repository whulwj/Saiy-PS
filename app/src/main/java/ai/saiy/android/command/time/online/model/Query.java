package ai.saiy.android.command.time.online.model;

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

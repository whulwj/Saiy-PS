package ai.saiy.android.command.financial;

public class StockQuoteResponse {
    @com.google.gson.annotations.SerializedName("ResultSet")
    private final ResultSet resultSet;

    public StockQuoteResponse(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public ResultSet getResultSet() {
        return this.resultSet;
    }
}

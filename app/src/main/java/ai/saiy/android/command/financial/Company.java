package ai.saiy.android.command.financial;

public class Company {
    @com.google.gson.annotations.SerializedName("symbol")
    private final String symbol;

    @com.google.gson.annotations.SerializedName("name")
    private final String name;

    public Company(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getSymbol() {
        return this.symbol;
    }
}

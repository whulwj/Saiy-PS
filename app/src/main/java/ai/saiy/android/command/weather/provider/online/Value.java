package ai.saiy.android.command.weather.provider.online;

public class Value {
    @com.google.gson.annotations.SerializedName("value")
    private final String value;

    public Value(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}

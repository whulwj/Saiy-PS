package ai.saiy.android.command.weather.provider.online;

import com.google.gson.annotations.SerializedName;

public class Value {
    @SerializedName("value")
    private final String value;

    public Value(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}

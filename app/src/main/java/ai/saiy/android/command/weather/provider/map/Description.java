package ai.saiy.android.command.weather.provider.map;

import com.google.gson.annotations.SerializedName;

public class Description {
    @SerializedName("description")
    private final String description;

    public Description(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}

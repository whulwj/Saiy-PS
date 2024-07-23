package ai.saiy.android.command.weather.provider.map;

public class Description {
    @com.google.gson.annotations.SerializedName("description")
    private final String description;

    public Description(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}

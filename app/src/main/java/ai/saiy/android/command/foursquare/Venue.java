package ai.saiy.android.command.foursquare;

import com.google.gson.annotations.SerializedName;

public class Venue {
    @SerializedName("id")
    private final String id;
    @SerializedName("name")
    private final String name;

    public Venue(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }
}

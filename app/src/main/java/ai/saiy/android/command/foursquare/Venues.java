package ai.saiy.android.command.foursquare;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Venues {
    @SerializedName("venues")
    private final List<Venue> venues;

    public Venues(List<Venue> venues) {
        this.venues = venues;
    }

    public List<Venue> getVenues() {
        return this.venues;
    }
}

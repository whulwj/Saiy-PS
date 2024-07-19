package ai.saiy.android.command.foursquare;

import com.google.gson.annotations.SerializedName;

public class VenuesResponse {
    @SerializedName("response")
    private final Venues response;

    public VenuesResponse(Venues venues) {
        this.response = venues;
    }

    public Venues getResponse() {
        return this.response;
    }
}

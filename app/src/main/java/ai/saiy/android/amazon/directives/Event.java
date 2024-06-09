package ai.saiy.android.amazon.directives;

import com.google.gson.annotations.SerializedName;

public class Event {
    @SerializedName("header")
    private final Header header;
    @SerializedName("payload")
    private final Payload payload;

    public Event(Header header, Payload payload) {
        this.header = header;
        this.payload = payload;
    }
}

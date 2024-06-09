package ai.saiy.android.amazon.directives;

import androidx.core.app.NotificationCompat;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StreamableEvent {
    @SerializedName(NotificationCompat.CATEGORY_EVENT)
    private final Event event;
    @SerializedName("context")
    private final List<Event> context;

    public StreamableEvent(Event event, List<Event> list) {
        this.event = event;
        this.context = list;
    }

    public String toJson() {
        return new com.google.gson.Gson().toJson(this) + "\n";
    }
}

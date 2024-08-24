package ai.saiy.android.command.time.online.model;

import com.google.gson.annotations.SerializedName;

public class TimeResponse {
    @SerializedName("data")
    private final Data data;

    public TimeResponse(Data data) {
        this.data = data;
    }

    public Data getData() {
        return this.data;
    }
}

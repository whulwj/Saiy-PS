package ai.saiy.android.firebase.database.write;

import com.google.firebase.database.PropertyName;

@com.google.firebase.database.IgnoreExtraProperties
public class Enhancement {
    @PropertyName("description")
    public String description;
    @PropertyName("date")
    public String date;

    public Enhancement() {
    }

    public Enhancement(String description, String date) {
        this.description = description;
        this.date = date;
    }
}

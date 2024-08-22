package ai.saiy.android.firebase.database.write;

import com.google.firebase.database.PropertyName;

@com.google.firebase.database.IgnoreExtraProperties
public class Design {
    @PropertyName("type")
    public String type;
    @PropertyName("web_link")
    public String webLink;
    @PropertyName("summary")
    public String summary;
    @PropertyName("date")
    public String date;

    public Design() {
    }

    public Design(String type, String webLink, String summary, String date) {
        this.summary = summary;
        this.type = type;
        this.webLink = webLink;
        this.date = date;
    }

    @com.google.firebase.database.Exclude
    public static String getType(int index) {
        switch (index) {
            case 0:
                return "app";
            case 1:
                return "web";
            default:
                return "other";
        }
    }

    @com.google.firebase.database.Exclude
    public String getType() {
        return this.type;
    }
}

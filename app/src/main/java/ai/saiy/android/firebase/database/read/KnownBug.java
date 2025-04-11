package ai.saiy.android.firebase.database.read;

@com.google.firebase.database.IgnoreExtraProperties
public class KnownBug {
    @com.google.firebase.database.PropertyName("title")
    public String title;

    @com.google.firebase.database.PropertyName("content")
    public String content;

    @com.google.firebase.database.Exclude
    public String getTitle() {
        return this.title;
    }

    @com.google.firebase.database.Exclude
    public String getContent() {
        return this.content;
    }
}

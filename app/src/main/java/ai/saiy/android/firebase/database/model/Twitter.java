package ai.saiy.android.firebase.database.model;

@com.google.firebase.database.IgnoreExtraProperties
public class Twitter {
    @com.google.firebase.database.PropertyName("key")
    public String key;

    @com.google.firebase.database.PropertyName("secret")
    public String secret;

    @com.google.firebase.database.Exclude
    public String getKey() {
        return this.key;
    }

    @com.google.firebase.database.Exclude
    public String getSecret() {
        return this.secret;
    }
}

package ai.saiy.android.firebase.database.model;

@com.google.firebase.database.IgnoreExtraProperties
public class Foursquare {
    @com.google.firebase.database.PropertyName("client_id")
    public String clientId;

    @com.google.firebase.database.Exclude
    public String getClientId() {
        return this.clientId;
    }
}

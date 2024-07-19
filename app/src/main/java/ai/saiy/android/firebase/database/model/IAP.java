package ai.saiy.android.firebase.database.model;

@com.google.firebase.database.IgnoreExtraProperties
public class IAP {
    @com.google.firebase.database.PropertyName("version_code")
    public String versionCode;

    @com.google.firebase.database.Exclude
    public String getVersionCode() {
        return this.versionCode;
    }
}

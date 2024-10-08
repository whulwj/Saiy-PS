package ai.saiy.android.firebase.database.read;

@com.google.firebase.database.IgnoreExtraProperties
public class IAPVersion {
    @com.google.firebase.database.PropertyName("version_code")
    public String versionCode;

    @com.google.firebase.database.Exclude
    public String getVersionCode() {
        return this.versionCode;
    }
}

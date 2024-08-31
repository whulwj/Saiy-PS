package ai.saiy.android.firebase.database.read;

@com.google.firebase.database.IgnoreExtraProperties
public class IAPCode {
    @com.google.firebase.database.PropertyName("iap_code")
    public String iapCode;

    @com.google.firebase.database.Exclude
    public String getIapCode() {
        return this.iapCode;
    }
}

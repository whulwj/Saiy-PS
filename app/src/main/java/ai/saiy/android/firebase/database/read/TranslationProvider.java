package ai.saiy.android.firebase.database.read;

@com.google.firebase.database.IgnoreExtraProperties
public class TranslationProvider {
    @com.google.firebase.database.PropertyName("default_provider")
    public int defaultProvider;

    @com.google.firebase.database.Exclude
    public int getDefaultProvider() {
        return this.defaultProvider;
    }
}

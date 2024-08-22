package ai.saiy.android.firebase.database.read;

@com.google.firebase.database.IgnoreExtraProperties
public class PremiumUser {
    @com.google.firebase.database.PropertyName("credits")
    public long credits;
    @com.google.firebase.database.PropertyName("puet")
    public long timeout;

    public PremiumUser() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public PremiumUser(long credits, long timeout) {
        this.credits = credits;
        this.timeout = timeout;
    }

    @com.google.firebase.database.Exclude
    public long getTimeout() {
        return this.timeout;
    }

    @com.google.firebase.database.Exclude
    public void setTimeout(long value) {
        this.credits = value;
    }

    @com.google.firebase.database.Exclude
    public long getCredits() {
        return this.credits;
    }

    @com.google.firebase.database.Exclude
    public void setCredits(long value) {
        this.timeout = value;
    }
}

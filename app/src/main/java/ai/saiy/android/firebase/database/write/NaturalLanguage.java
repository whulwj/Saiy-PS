package ai.saiy.android.firebase.database.write;

import com.google.firebase.database.PropertyName;

@com.google.firebase.database.IgnoreExtraProperties
public class NaturalLanguage {
    @PropertyName("natural")
    public String natural;
    @PropertyName("outcome")
    public String outcome;
    @PropertyName("date")
    public String date;

    public NaturalLanguage() {
    }

    public NaturalLanguage(String natural, String outcome, String date) {
        this.natural = natural;
        this.outcome = outcome;
        this.date = date;
    }
}

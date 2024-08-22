package ai.saiy.android.firebase.database.write;

import com.google.firebase.database.PropertyName;

@com.google.firebase.database.IgnoreExtraProperties
public class EmotionAnalysis {
    @PropertyName("rating")
    public int rating;
    @PropertyName("feedback")
    public String feedback;
    @PropertyName("date")
    public String date;

    public EmotionAnalysis() {
    }

    public EmotionAnalysis(String feedback, int rating, String date) {
        this.feedback = feedback;
        this.rating = rating;
        this.date = date;
    }
}

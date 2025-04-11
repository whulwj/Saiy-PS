package ai.saiy.android.firebase.database.read;

import androidx.annotation.NonNull;

import java.util.List;

import ai.saiy.android.utils.UtilsList;

@com.google.firebase.database.IgnoreExtraProperties
public class KnownBugs {
    @com.google.firebase.database.PropertyName("known_bugs")
    public List<KnownBug> bugs;

    @com.google.firebase.database.Exclude
    public List<KnownBug> getBugs() {
        return this.bugs;
    }

    @com.google.firebase.database.Exclude
    public @NonNull String toString() {
        if (!UtilsList.notNaked(this.bugs)) {
            return super.toString();
        }
        StringBuilder sb = new StringBuilder();
        for (KnownBug knownBug : this.bugs) {
            sb.append(knownBug.getTitle());
            sb.append(knownBug.getContent());
        }
        return sb.toString();
    }
}

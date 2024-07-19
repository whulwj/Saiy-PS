package ai.saiy.android.firebase;

public class UserFirebase {
    private String uid;
    private boolean isAnonymous;
    private boolean isMigrated;

    public String getUid() {
        return this.uid;
    }

    public void setUid(String str) {
        this.uid = str;
    }

    public void setAnonymous(boolean condition) {
        this.isAnonymous = condition;
    }

    public void setMigrated(boolean condition) {
        this.isMigrated = condition;
    }
}

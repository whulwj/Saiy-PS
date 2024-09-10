package ai.saiy.android.command.audio;

public class CommandAudioValues {
    private Type type;
    private String description;

    public enum Type {
        UNKNOWN,
        SILENT,
        VIBRATE,
        NORMAL
    }

    public String getDescription() {
        return this.description;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setDescription(String str) {
        this.description = str;
    }

    public Type getType() {
        return this.type;
    }
}

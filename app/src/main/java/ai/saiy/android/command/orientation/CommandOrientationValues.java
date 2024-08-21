package ai.saiy.android.command.orientation;

public class CommandOrientationValues {
    private Type type;
    private String description;

    public enum Type {
        UNKNOWN,
        PORTRAIT,
        LANDSCAPE,
        REVERSE_PORTRAIT,
        REVERSE_LANDSCAPE,
        SOMERSAULT_FORWARD,
        SOMERSAULT_BACKWARD,
        LOCK,
        UNLOCK
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

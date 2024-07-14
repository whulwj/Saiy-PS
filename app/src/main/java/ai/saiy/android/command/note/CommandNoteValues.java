package ai.saiy.android.command.note;

public class CommandNoteValues {
    private Type type;

    public enum Type {
        UNKNOWN,
        NOTE,
        VOICE_NOTE
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}

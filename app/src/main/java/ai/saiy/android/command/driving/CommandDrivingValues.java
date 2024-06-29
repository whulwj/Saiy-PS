package ai.saiy.android.command.driving;

public class CommandDrivingValues {
    private Action action = Action.UNKNOWN;

    public enum Action {
        UNKNOWN,
        ENABLE,
        DISABLE,
        TOGGLE
    }

    public Action getAction() {
        return this.action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}

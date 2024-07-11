package ai.saiy.android.command.navigation;

public class CommandNavigationValues {
    private Type type;
    private String action;
    private String address;

    public enum Type {
        UNKNOWN,
        DESTINATION,
        APPOINTMENT
    }

    public String getAddress() {
        return this.address;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Type getType() {
        return this.type;
    }

    public void setAction(String action) {
        this.action = action;
    }
}

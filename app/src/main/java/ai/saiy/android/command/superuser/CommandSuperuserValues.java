package ai.saiy.android.command.superuser;

public class CommandSuperuserValues {
    private Root root;
    private String description;

    public enum Root {
        UNKNOWN,
        REBOOT,
        BOOTLOADER,
        FASTBOOT,
        RECOVERY,
        HOT_REBOOT,
        GOVERNOR,
        SCREENSHOT,
        SHUTDOWN
    }

    public Root getRoot() {
        return this.root;
    }

    public void setRoot(Root root) {
        this.root = root;
    }

    public void setDescription(String str) {
        this.description = str;
    }
}

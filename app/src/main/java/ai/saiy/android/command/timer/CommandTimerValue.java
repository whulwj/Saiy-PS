package ai.saiy.android.command.timer;

public final class CommandTimerValue {
    private boolean isValid;
    private int runningTotal;
    private int hour;
    private int minute;
    private int second;
    private String callee;

    public int getHour() {
        return this.hour;
    }

    public void setHour(int value) {
        this.hour = value;
    }

    public void setCallee(String str) {
        this.callee = str;
    }

    public void setValidness(boolean condition) {
        this.isValid = condition;
    }

    public int getMinute() {
        return this.minute;
    }

    public void setMinute(int value) {
        this.minute = value;
    }

    public int getSecond() {
        return this.second;
    }

    public void setSecond(int value) {
        this.second = value;
    }

    public void setRunningTotal(int value) {
        this.runningTotal = value;
    }

    public boolean isValid() {
        return this.isValid;
    }

    public int getRunningTotal() {
        return this.runningTotal;
    }

    public String getCallee() {
        return this.callee;
    }
}

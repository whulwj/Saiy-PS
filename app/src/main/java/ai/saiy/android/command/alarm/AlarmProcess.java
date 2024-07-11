package ai.saiy.android.command.alarm;

public class AlarmProcess {
    public static final int TYPE_HOUR = 1;
    public static final int TYPE_MINUTE = 2;
    public static final int TYPE_HOUR_MINUTE = 3;

    private boolean isValid;
    private boolean outsideTwentyFour;
    private boolean isAM;
    private boolean isPM;
    private int type;
    private int hourOfDay;
    private int minute;
    private int weekday;
    private String timesString = "";

    public void setType(int type) {
        this.type = type;
    }

    public void setTimeString(String timesString) {
        this.timesString = timesString;
    }

    public void setValidness(boolean condition) {
        this.isValid = condition;
    }

    public boolean isValid() {
        return this.isValid;
    }

    public void setHourOfDay(int hour) {
        this.hourOfDay = hour;
    }

    public void setOutsideTwentyFour(boolean condition) {
        this.outsideTwentyFour = condition;
    }

    public boolean outsideTwentyFour() {
        return this.outsideTwentyFour;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public void setAM(boolean isAM) {
        this.isAM = isAM;
    }

    public boolean isAM() {
        return this.isAM;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }

    public void setPM(boolean isPM) {
        this.isPM = isPM;
    }

    public boolean isPM() {
        return this.isPM;
    }

    public int getType() {
        return this.type;
    }

    public int getHourOfDay() {
        return this.hourOfDay;
    }

    public int getMinute() {
        return this.minute;
    }

    public int getWeekday() {
        return this.weekday;
    }

    public String getTimeString() {
        return this.timesString;
    }
}

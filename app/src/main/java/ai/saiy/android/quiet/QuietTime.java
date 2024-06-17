package ai.saiy.android.quiet;

public class QuietTime {
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;

    public QuietTime(int startHour, int startMinute, int endHour, int endMinute) {
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.startHour = startHour;
        this.startMinute = startMinute;
    }

    public int getEndHour() {
        return this.endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return this.endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public int getStartHour() {
        return this.startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return this.startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }
}

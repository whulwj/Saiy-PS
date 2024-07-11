package ai.saiy.android.command.calendar;

public class Account {
    private final long calendarID;
    private final String calendarName;
    private final String accountName;

    public Account(long id, String calendarName, String accountName) {
        this.calendarID = id;
        this.calendarName = calendarName;
        this.accountName = accountName;
    }

    public long getCalendarID() {
        return this.calendarID;
    }

    public String getCalendarName() {
        return this.calendarName;
    }

    public String getAccountName() {
        return this.accountName;
    }
}

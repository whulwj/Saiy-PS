package ai.saiy.android.command.calendar;

import java.util.ArrayList;
import java.util.Date;

public class Event {
    private String title;
    private String location;
    private Date startDate;
    private Date endDate;
    private boolean isAllDay;
    private ArrayList<String> attendees;

    public Event() {
        this.attendees = new ArrayList<>();
    }

    public Event(String title, Date startDate, Date endDate, boolean isAllDay, ArrayList<String> attendees, String location) {
        this.attendees = new ArrayList<>();
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isAllDay = isAllDay;
        this.attendees = attendees;
        this.location = location;
    }

    public String getLocation() {
        return this.location;
    }

    public String getTitle() {
        return this.title;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public boolean isAllDay() {
        return this.isAllDay;
    }

    public ArrayList<String> attendees() {
        return this.attendees;
    }
}

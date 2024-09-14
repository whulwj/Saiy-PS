package ai.saiy.android.command.agenda;

import java.util.Date;

import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.UtilsDate;

public class AgendaProcess {
    private int year;
    /**
     * The valid month starts from {@link UtilsDate#MONTH_OFFSET}
     */
    private int month;
    private int dayOfMonth;
    private int weekday;
    private boolean haveMonth;
    private boolean haveDate;
    private boolean haveWeekday;
    private boolean haveYear;
    private String utterance;
    private @Outcome.Result int outcome;
    private long beginTime;
    private long endTime;
    private boolean isTomorrow;
    private boolean isToday;
    private Date date;

    public void setOutcome(@Outcome.Result int outcome) {
        this.outcome = outcome;
    }

    public void setBeginTime(long timestamp) {
        this.beginTime = timestamp;
    }

    public void setUtterance(String str) {
        this.utterance = str;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setIsTomorrow(boolean condition) {
        this.isTomorrow = condition;
    }

    public boolean isTomorrow() {
        return this.isTomorrow;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }

    public void setEndTime(long timestamp) {
        this.endTime = timestamp;
    }

    public void setIsToday(boolean condition) {
        this.isToday = condition;
    }

    public boolean isToday() {
        return this.isToday;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public void setHaveYear(boolean condition) {
        this.haveYear = condition;
    }

    public long getBeginTimestamp() {
        return this.beginTime;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setHaveMonth(boolean condition) {
        this.haveMonth = condition;
    }

    public long getEndTimestamp() {
        return this.endTime;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setHaveDate(boolean condition) {
        this.haveDate = condition;
    }

    public @Outcome.Result int getOutcome() {
        return this.outcome;
    }

    public void setHaveWeekday(boolean condition) {
        this.haveWeekday = condition;
    }

    public String getUtterance() {
        return this.utterance;
    }

    public boolean haveYear() {
        return this.haveYear;
    }

    public boolean haveMonth() {
        return this.haveMonth;
    }

    public boolean haveDate() {
        return this.haveDate;
    }

    public boolean haveWeekday() {
        return this.haveWeekday;
    }

    public int getWeekday() {
        return this.weekday;
    }

    public int getDayOfMonth() {
        return this.dayOfMonth;
    }

    /**
     * @return the month of year
     */
    public int getMonth() {
        return this.month;
    }

    public int getYear() {
        return this.year;
    }
}

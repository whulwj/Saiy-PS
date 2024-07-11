package ai.saiy.android.command.sms;

public class Message {
    private String body;
    private String address;

    private boolean isRead;
    private long date;
    private String person;

    public Message(String address, String body, long date, String person, boolean isRead) {
        this.address = address;
        this.body = body;
        this.date = date;
        this.person = person;
        this.isRead = isRead;
    }

    public String getAddress() {
        return this.address;
    }

    public String getBody() {
        return this.body;
    }

    public long getDate() {
        return this.date;
    }

    public String getPerson() {
        return this.person;
    }

    public boolean isRead() {
        return this.isRead;
    }
}

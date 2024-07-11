package ai.saiy.android.custom;

public class Phone {
    private String name;
    private String number;

    private int type;

    public Phone(String name, String number, int type) {
        this.name = name;
        this.number = number;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public String getNumber() {
        return this.number;
    }

    public int getType() {
        return this.type;
    }
}

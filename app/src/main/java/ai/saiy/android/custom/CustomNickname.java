package ai.saiy.android.custom;

import java.io.Serializable;

public class CustomNickname implements Serializable {
    private static final long serialVersionUID = -1652757421630998048L;
    private String contactName;
    private String nickname;
    private long rowId;
    private String serialised;

    public CustomNickname(String nickname, String contactName) {
        this.nickname = nickname;
        this.contactName = contactName;
    }

    public CustomNickname(String nickname, String contactName, long id) {
        this.nickname = nickname;
        this.contactName = contactName;
        this.rowId = id;
    }

    public CustomNickname(String nickname, String contactName, long id, String serialised) {
        this.nickname = nickname;
        this.contactName = contactName;
        this.rowId = id;
        this.serialised = serialised;
    }

    public String getContactName() {
        return this.contactName;
    }

    public String getNickname() {
        return this.nickname;
    }

    public long getRowId() {
        return this.rowId;
    }

    public String getSerialised() {
        return this.serialised != null ? this.serialised : "";
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setRowId(long id) {
        this.rowId = id;
    }

    public void setSerialised(String serialised) {
        this.serialised = serialised;
    }
}

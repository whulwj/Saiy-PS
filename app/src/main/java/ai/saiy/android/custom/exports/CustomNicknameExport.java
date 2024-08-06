package ai.saiy.android.custom.exports;

import java.io.Serializable;

public class CustomNicknameExport implements Serializable {
    private static final long serialVersionUID = 7099509969388343880L;
    private String contactName;
    private ExportConfiguration exportConfiguration;
    private String nickname;

    public CustomNicknameExport(String nickname, String contactName) {
        this.nickname = nickname;
        this.contactName = contactName;
    }

    public String getContactName() {
        return this.contactName;
    }

    public ExportConfiguration getExportConfiguration() {
        return this.exportConfiguration;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setExportConfiguration(ExportConfiguration exportConfiguration) {
        this.exportConfiguration = exportConfiguration;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}

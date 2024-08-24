package ai.saiy.android.custom.exports;

import com.google.gson.annotations.SerializedName;

public class CustomNicknameExport {
    @SerializedName("contactName")
    private String contactName;
    @SerializedName("exportConfiguration")
    private ExportConfiguration exportConfiguration;
    @SerializedName("nickname")
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

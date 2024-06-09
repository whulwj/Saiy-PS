package ai.saiy.android.amazon.directives;

import com.google.gson.annotations.SerializedName;

public class Header {
    @SerializedName("namespace")
    private final String namespace;

    @SerializedName("name")
    private final String name;

    @SerializedName("messageId")
    private final String messageId;

    @SerializedName("dialogRequestId")
    private final String dialogRequestId;

    public Header(String namespace, String name, String messageId, String dialogRequestId) {
        this.namespace = namespace;
        this.name = name;
        this.messageId = messageId;
        this.dialogRequestId = dialogRequestId;
    }

    public String getName() {
        return this.name;
    }
}

package ai.saiy.android.amazon.directives;

import com.google.gson.annotations.SerializedName;

public class Directive {
    @SerializedName("header")
    private final Header header;
    @SerializedName("payload")
    private final Payload payload;

    public Directive(Header header, Payload payload) {
        this.header = header;
        this.payload = payload;
    }

    public Header getHeader() {
        return this.header;
    }

    public Payload getPayload() {
        return this.payload;
    }

    public boolean isExpectSpeech() {
        return header.getName().matches("ExpectSpeech");
    }

    public boolean isDirectiveVolume() {
        return header.getName().matches("AdjustVolume") || header.getName().matches("SetVolume") || header.getName().matches("SetMute");
    }

    public boolean isDirectiveMedia() {
        return header.getName().matches("PlayCommandIssued") || header.getName().matches("PauseCommandIssued") || header.getName().matches("NextCommandIssued") || header.getName().matches("PreviousCommandIssue");
    }
}

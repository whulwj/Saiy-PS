package ai.saiy.android.command.definition.online;

import com.google.gson.annotations.SerializedName;

public class DefinitionResponse {
    @SerializedName("word")
    private final String word;

    @SerializedName("text")
    private final String text;

    public DefinitionResponse(String word, String text) {
        this.word = word;
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public String getWord() {
        return this.word;
    }
}

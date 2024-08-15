package ai.saiy.android.command.definition.online;

public class DefinitionResponse {
    @com.google.gson.annotations.SerializedName("word")
    private final String word;

    @com.google.gson.annotations.SerializedName("text")
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

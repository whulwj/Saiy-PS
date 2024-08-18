package ai.saiy.android.command.chatbot;

public class CommandChatBotValues {
    private Intro intro;
    private String description;

    public enum Intro {
        UNKNOWN,
        GREETING,
        HEALTH
    }

    public Intro getIntro() {
        return this.intro;
    }

    public void setIntro(Intro intro) {
        this.intro = intro;
    }

    public void setDescription(String str) {
        this.description = str;
    }
}

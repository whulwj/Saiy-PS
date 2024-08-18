package ai.saiy.android.command.chatbot;

import org.simpleframework.xml.Root;

@Root(name = "result", strict = false)
public class Result {
    @org.simpleframework.xml.Element(name = "that")
    private String response;

    public Result(@org.simpleframework.xml.Element(name = "that") String str) {
        this.response = str;
    }

    public String getResponse() {
        return this.response;
    }
}

package ai.saiy.android.command.chatbot;

import org.simpleframework.xml.core.Persister;

import ai.saiy.android.utils.MyLog;

public class ResolveChatBot {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ResolveChatBot.class.getSimpleName();

    public String resolve(String xmlResponse) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "resolve");
        }
        try {
            return new Persister().read(Result.class, xmlResponse, false).getResponse();
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }
        return null;
    }
}

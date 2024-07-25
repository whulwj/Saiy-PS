package ai.saiy.android.command.http;

import android.util.Pair;

import org.checkerframework.checker.lock.qual.NewObject;

import ai.saiy.android.utils.MyLog;

public class HttpsProcessor {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = HttpsProcessor.class.getSimpleName();

    public CustomHttp createDefault() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "createDefault");
        }
        return new CustomHttp("", true, CustomHttp.TYPE_POST, CustomHttp.OUTPUT_TYPE_NONE, CustomHttp.SUCCESS_NONE, CustomHttp.ERROR_NONE);
    }

    public @NewObject Pair<Boolean, Object> process(@NewObject CustomHttp customHttp) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "process");
            MyLog.i(CLS_NAME, "url:" + customHttp.getUrlString());
            MyLog.i(CLS_NAME, "https:" + customHttp.isHttps());
        }
        switch (customHttp.getType()) {
            case CustomHttp.TYPE_POST:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "TYPE_POST");
                }
                return customHttp.isHttps() ? new HttpsPost().process(customHttp) : new HttpPost().process(customHttp);
            case CustomHttp.TYPE_GET:
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "TYPE_GET");
                }
                return customHttp.isHttps() ? new HttpsGet().process(customHttp) : new HttpGet().process(customHttp);
            case CustomHttp.TYPE_PUT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "TYPE_PUT");
                }
                return customHttp.isHttps() ? new HttpsPut().process(customHttp) : new HttpPut().process(customHttp);
            case CustomHttp.TYPE_DELETE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "TYPE_DELETE");
                }
                return customHttp.isHttps() ? new HttpsDelete().process(customHttp) : new HttpDelete().process(customHttp);
        }
    }
}

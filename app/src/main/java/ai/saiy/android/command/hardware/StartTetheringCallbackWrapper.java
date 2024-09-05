package ai.saiy.android.command.hardware;

import androidx.annotation.Nullable;

import ai.saiy.android.utils.MyLog;

public class StartTetheringCallbackWrapper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String TAG = "OnStartTetheringCallback";

    private static final String ON_START_TETHERING_CALLBACK_CLASS_NAME = "android.net.ConnectivityManager.OnStartTetheringCallback";
    protected static Class sStartTetheringCallbackClass;

    static {
        try {
            sStartTetheringCallbackClass = Class.forName(ON_START_TETHERING_CALLBACK_CLASS_NAME);
        } catch (Throwable t) {
            if (DEBUG) {
                MyLog.w(TAG, "Error getting class:" + t.getMessage());
            }
        }
    }

    private Object mProxyInstance;
    public void onTetheringStarted() {}
    public void onTetheringFailed() {}

    public StartTetheringCallbackWrapper() {
    }

    //https://stackoverflow.com/questions/1082850/java-reflection-create-an-implementing-class/9583681#9583681
    public @Nullable Object getProxyInstance() {
        if (mProxyInstance != null) {
            return mProxyInstance;
        }
        final Class listenerClass = sStartTetheringCallbackClass;
        if (listenerClass == null) {
            return null;
        }
        mProxyInstance = java.lang.reflect.Proxy.newProxyInstance(
                listenerClass.getClassLoader(),
                new java.lang.Class[] { listenerClass },
                new java.lang.reflect.InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws java.lang.Throwable {
                        final String method_name = method.getName();
                        if (method_name.equals("onTetheringStarted")) {
                            StartTetheringCallbackWrapper.this.onTetheringStarted();
                        } else if (method_name.equals("onTetheringFailed")) {
                            StartTetheringCallbackWrapper.this.onTetheringFailed();
                        } else if (method_name.equals("hashCode")) {
                            return proxy.hashCode();
                        } else if (method_name.equals("equals")) {
                            if (args.length > 0) {
                                return proxy.equals(args[0]);
                            }
                            return false;
                        } else if (method_name.equals("toString")) {
                            return proxy.toString();
                        }
                        return null;
                    }
                });
        return mProxyInstance;
    }
}

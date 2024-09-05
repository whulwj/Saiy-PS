package ai.saiy.android.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * reflection util
 */
public abstract class UtilsReflection {
    private static final boolean DEBUG = MyLog.DEBUG;
    public static final String TAG = "UtilsReflection";

    private static Method sForNameMethod;
    private static Method sGetDeclaredMethod;
    private static Method sGetFieldMethod;

    /*
     * init reflection and cache it
     */
    static {
        try {
            sForNameMethod = Class.class.getDeclaredMethod("forName", String.class);
            sGetDeclaredMethod = Class.class
                    .getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
            sGetFieldMethod = Class.class.getDeclaredMethod("getDeclaredField", String.class);
        } catch (Throwable t) {
            if (DEBUG) {
                MyLog.w(TAG, "bootstrap:" + t.getMessage());
            }
        }
    }

    /**
     * get class
     *
     * @param clzName the class name
     */
    @Nullable
    public static Class<?> getClass(@NonNull String clzName) {
        Class<?> clz = null;
        if (canReflection()) {
            try {
                clz = (Class<?>) sForNameMethod.invoke(null, clzName);
            } catch (Throwable t) {
                if (DEBUG) {
                    MyLog.w(TAG, "getClass:" + clzName + ", " + t.getMessage());
                }
            }
        }

        return clz;
    }

    /**
     * get field
     *
     * @param src the instance
     * @param clz the class name
     * @param fieldName the field name
     * @param defObj the default val
     * @return the field of the instance
     */
    @Nullable
    public static Object getFieldObj(@Nullable Object src, @NonNull Class<?> clz,
                                     @NonNull String fieldName, @Nullable Object defObj) {
        Object result = defObj;
        try {
            final Field field = getField(clz, fieldName);
            if (field != null) {
                result = field.get(src);
            }
        } catch (Throwable t) {
            if (DEBUG) {
                MyLog.w(TAG, "getFieldObj:" + fieldName + ", " + t.getMessage());
            }
        }

        return result;
    }

    /**
     * get field
     *
     * @param clz the class
     * @param fieldName the field name
     * @return the class field
     */
    @Nullable
    public static Field getField(@NonNull Class<?> clz, @NonNull String fieldName) {
        Field field = null;
        if (canReflection()) {
            try {
                field = (Field) sGetFieldMethod.invoke(clz, fieldName);
                field.setAccessible(true);
            } catch (Throwable t) {
                if (DEBUG) {
                    MyLog.w(TAG, "getField:" + fieldName + ", " + t.getMessage());
                }
            }
        }

        return field;
    }

    /**
     * get method
     *
     * @param clz the class
     * @param methodName the method name
     * @param clzArgs method params
     * @return method
     */
    @Nullable
    public static Method getMethod(@NonNull Class<?> clz, @NonNull String methodName,
                                   Class... clzArgs) {
        Method method = null;
        if (canReflection()) {
            try {
                method = (Method) sGetDeclaredMethod.invoke(clz, methodName, clzArgs);
                method.setAccessible(true);
            } catch (Throwable t) {
                if (DEBUG) {
                    MyLog.w(TAG, "getMethod:" + methodName + ", " + t.getMessage());
                }
            }
        }
        return method;
    }

    /**
     * invoke method
     *
     * @param src the instance
     * @param clz the class
     * @param methodName the method name
     * @return obj
     */
    public static Object invokeMethod(@NonNull Object src, @NonNull Class<?> clz,
                                      @NonNull String methodName) {
        Object result = null;
        try {
            final Method method = getMethod(clz, methodName);
            if (method != null) {
                result = method.invoke(src);
            }
        } catch (Throwable t) {
            if (DEBUG) {
                MyLog.w(TAG, "invokeMethod:" + methodName + ", " + t.getMessage());
            }
        }
        return result;
    }

    /**
     * invoke method
     *
     * @param src the instance
     * @param clz the class
     * @param methodName the method name
     * @param clzArgs args class array
     * @param objArgs args
     * @return obj
     */
    public static Object invokeMethod(@NonNull Object src, @NonNull Class<?> clz,
                                      @NonNull String methodName, Class[] clzArgs, Object... objArgs) {
        Object result = null;
        try {
            final Method method = getMethod(clz, methodName, clzArgs);
            if (method != null) {
                result = method.invoke(src, objArgs);
            }
        } catch (Throwable t) {
            if (DEBUG) {
                MyLog.w(TAG, "invokeMethod:" + methodName + ", " + t.getMessage());
            }
        }
        return result;
    }

    /**
     * check can reflation
     *
     * @return can use reflection or no
     */
    private static boolean canReflection() {
        return sForNameMethod != null && sGetDeclaredMethod != null && sGetFieldMethod != null;
    }
}

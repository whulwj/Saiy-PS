package ai.saiy.android.location;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({ValueType.VALUE_BOOLEAN, ValueType.VALUE_INTEGER, ValueType.VALUE_LONG, ValueType.VALUE_FLOAT, ValueType.VALUE_DOUBLE,
        ValueType.VALUE_STRING, ValueType.VALUE_BIG_INTEGER, ValueType.VALUE_BIG_DECIMAL})
@Retention(RetentionPolicy.SOURCE)
public @interface ValueType {
    String VALUE_BOOLEAN = "a";
    String VALUE_BYTE = "b";
    String VALUE_SHORT = "r";
    String VALUE_INTEGER = "i";
    String VALUE_LONG = "l";
    String VALUE_FLOAT = "f";
    String VALUE_DOUBLE = "d";
    String VALUE_STRING = "s";
    String VALUE_BIG_INTEGER = "c";
    String VALUE_BIG_DECIMAL = "e";
}

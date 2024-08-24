package ai.saiy.android.custom;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.database.DBTaskerVariable;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class TaskerVariableHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = TaskerVariableHelper.class.getSimpleName();

    private static final Object lock = new Object();

    public static Pair<Boolean, Long> setTaskerVariable(Context context, TaskerVariable taskerVariable, SupportedLanguage supportedLanguage, long j) {
        synchronized (lock) {
            final DBTaskerVariable dbTaskerVariable = new DBTaskerVariable(context);
            final Pair<Boolean, Long> duplicatePair = j > -1 ? new Pair<>(true, j) : variableExists(context, dbTaskerVariable, taskerVariable, supportedLanguage);

            return dbTaskerVariable.insertPopulatedRow(taskerVariable.getVariableName(), taskerVariable.getVariableValue(), duplicatePair.first, duplicatePair.second);
        }
    }

    private static Pair<Boolean, Long> variableExists(Context context, DBTaskerVariable dbTaskerVariable, TaskerVariable taskerVariable, SupportedLanguage supportedLanguage) {
        if (dbTaskerVariable.databaseExists()) {
            final ArrayList<TaskerVariable> variableArray = dbTaskerVariable.getVariables();
            if (ai.saiy.android.utils.UtilsList.notNaked(variableArray)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "have variables");
                }
                if (supportedLanguage == null) {
                    supportedLanguage = SupportedLanguage.getSupportedLanguage(ai.saiy.android.utils.SPH.getVRLocale(context));
                }
                final Locale locale = supportedLanguage.getLocale();
                for (TaskerVariable tv : variableArray) {
                    if (tv.getVariableName().toLowerCase(locale).trim().matches(taskerVariable.getVariableName().toLowerCase(locale).trim())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "variable names matched: " + tv.getVariableName() + " ~ " + taskerVariable.getVariableName());
                        }
                        return new Pair<>(true, tv.getRowId());
                    }
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "variable is not a duplicate");
                }
            } else if (DEBUG) {
                MyLog.i(CLS_NAME, "no variables");
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "databaseExists: false");
        }
        return new Pair<>(false, -1L);
    }
}

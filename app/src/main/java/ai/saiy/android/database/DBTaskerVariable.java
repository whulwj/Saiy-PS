package ai.saiy.android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import java.io.File;
import java.util.ArrayList;

import ai.saiy.android.custom.TaskerVariable;
import ai.saiy.android.utils.MyLog;

public class DBTaskerVariable extends SQLiteOpenHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = DBTaskerVariable.class.getSimpleName();

    private static final String DATABASE_NAME = "taskerVariable.db";
    public static final String TABLE_TASKER_VARIABLE = "tasker_variable";
    private static final int DATABASE_VERSION = 1;

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_VALUE = "value";
    public static final String COLUMN_SERIALISED = "serialised";

    private static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_NAME, COLUMN_VALUE, COLUMN_SERIALISED};

    private static final String DATABASE_CREATE = "create table " + "tasker_variable"
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_VALUE + " text not null, "
            + COLUMN_SERIALISED + " text not null);";

    private final String databasePath;
    private SQLiteDatabase database;

    public DBTaskerVariable(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "Constructor");
        }
        this.databasePath = context.getDatabasePath(DATABASE_NAME).getPath();
    }

    public void open() throws SQLException {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "open");
        }
        this.database = getWritableDatabase();
    }

    @Override
    public void close() throws SQLException {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "close");
        }
        database.close();
    }

    @Override
    public void onCreate(SQLiteDatabase dataBase) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        dataBase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onUpgrade");
            MyLog.w(CLS_NAME, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        }
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKER_VARIABLE);
        onCreate(db);
    }

    /**
     * Insert a row, separating the tasker variable and serialised class.
     *
     * @param name        the name
     * @param value       the value to be used
     * @param serialised  the serialised class
     * @param isDuplicate true if a command is being replaced
     * @param rowId       the row id of the command to be replaced
     * @return true if the insertion was successful. False otherwise
     */
    public Pair<Boolean, Long> insertPopulatedRow(String name, String value, String serialised, boolean isDuplicate, long rowId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "insertPopulatedRow: duplicate: " + isDuplicate + " " + rowId);
        }

        boolean success = false;
        long insertId = -1;

        try {
            open();

            if (database.isOpen()) {
                final ContentValues values = new ContentValues();
                values.put(COLUMN_NAME, name);
                values.put(COLUMN_VALUE, value);
                values.put(COLUMN_SERIALISED, serialised);

                insertId = database.insert(TABLE_TASKER_VARIABLE, null, values);
                final Cursor cursor = database.query(TABLE_TASKER_VARIABLE, ALL_COLUMNS,
                        COLUMN_ID + " = " + insertId, null, null,
                        null, null);
                cursor.moveToFirst();
                cursor.close();
                success = true;
            }

        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "insertPopulatedRow: IllegalStateException");
                e.printStackTrace();
            }
        } catch (final SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "insertPopulatedRow: SQLException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "insertPopulatedRow: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (database.isOpen()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "insertPopulatedRow: finally closing");
                    }
                    close();
                }
            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "insertPopulatedRow: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (final SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "insertPopulatedRow: SQLException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "insertPopulatedRow: Exception");
                    e.printStackTrace();
                }
            }
        }

        if (isDuplicate) {
            deleteRow(rowId);
        }

        return new Pair<>(success, insertId);
    }

    /**
     * Delete a row from the database
     *
     * @param rowID the row identifier
     */
    public void deleteRow(long rowID) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "deleteRow");
        }
        try {
            open();
            if (database.isOpen()) {
                database.delete(TABLE_TASKER_VARIABLE, COLUMN_ID + "=?", new String[]{String.valueOf(rowID)});
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteRow: IllegalStateException");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteRow: SQLException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteRow: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (database.isOpen()) {
                    close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteRow: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteRow: SQLException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteRow: Exception");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Check if the database exists
     *
     * @return true if it exists. False otherwise
     */
    public boolean databaseExists() {
        return new File(databasePath).exists();
    }

    /**
     * Delete all entries in the current table
     */
    public boolean deleteTable() {
        try {
            open();
            if (database.isOpen()) {
                database.delete(TABLE_TASKER_VARIABLE, null, null);
                return true;
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteTable: database not open");
                }
            }
        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteTable: IllegalStateException");
                e.printStackTrace();
            }
        } catch (final SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteTable: SQLException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteTable: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (database.isOpen()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "deleteTable: finally closing");
                    }
                    close();
                }
            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteTable: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (final SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteTable: SQLException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteTable: Exception");
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    /**
     * Get all variables from the database, including the corresponding row identifier and
     * serialised command data.
     *
     * @return the {@link TaskerVariable} list
     */
    public ArrayList<TaskerVariable> getVariables() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getVariables");
        }
        final ArrayList<TaskerVariable> variables = new ArrayList<>();
        Cursor cursor = null;
        try {
            open();
            if (database.isOpen()) {
                cursor = database.query(TABLE_TASKER_VARIABLE, ALL_COLUMNS, null, null, null, null, null);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    variables.add(new TaskerVariable(cursor.getString(1), cursor.getString(2), cursor.getLong(0), cursor.getString(3)));
                    cursor.moveToNext();
                }
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getVariables: IllegalStateException");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getVariables: SQLException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getVariables: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (cursor != null && cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Throwable t) {
                MyLog.w(CLS_NAME, "getApplications: Throwable");
            } finally {
                try {
                    if (database.isOpen()) {
                        close();
                    }
                } catch (IllegalStateException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getVariables: IllegalStateException");
                        e.printStackTrace();
                    }
                } catch (SQLException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getVariables: SQLException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getVariables: Exception");
                        e.printStackTrace();
                    }
                }
            }
        }
        return variables;
    }
}

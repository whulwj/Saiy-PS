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

import ai.saiy.android.custom.CustomReplacement;
import ai.saiy.android.utils.MyLog;

public class DBCustomReplacement extends SQLiteOpenHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = DBCustomReplacement.class.getSimpleName();

    private static final String DATABASE_NAME = "customReplacement.db";
    public static final String TABLE_CUSTOM_REPLACEMENT = "custom_replacement";
    private static final int DATABASE_VERSION = 1;

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_KEYPHRASE = "keyphrase";
    public static final String COLUMN_REPLACEMENT = "replacement";
    public static final String COLUMN_SERIALISED = "serialised";

    private static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_KEYPHRASE, COLUMN_REPLACEMENT, COLUMN_SERIALISED};
    private static final String DATABASE_CREATE = "create table " + "custom_replacement"+
            "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_KEYPHRASE + " text not null, "
            + COLUMN_REPLACEMENT + " text not null, "
            + COLUMN_SERIALISED + " text not null);";

    private final String databasePath;
    private SQLiteDatabase database;

    public DBCustomReplacement(Context context) {
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOM_REPLACEMENT);
        onCreate(db);
    }

    /**
     * Insert a row, separating the command replacement and serialised class.
     *
     * @param keyphrase   the keyphrase
     * @param replacement the replacement to be used
     * @param serialised  the serialised class
     * @param isDuplicate true if a command is being replaced
     * @param rowId       the row id of the command to be replaced
     * @return a {@link Pair}, which #first field will be true if the insertion was successful. False otherwise
     */
    public Pair<Boolean, Long> insertPopulatedRow(String keyphrase, String replacement, String serialised, boolean isDuplicate, long rowId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "insertPopulatedRow: duplicate: " + isDuplicate + " " + rowId);
        }

        boolean success = false;
        long insertId = -1;

        try {
            open();

            if (database.isOpen()) {
                final ContentValues values = new ContentValues();
                values.put(COLUMN_KEYPHRASE, keyphrase);
                values.put(COLUMN_REPLACEMENT, replacement);
                values.put(COLUMN_SERIALISED, serialised);

                insertId = database.insert(TABLE_CUSTOM_REPLACEMENT, null, values);
                try (Cursor cursor = database.query(TABLE_CUSTOM_REPLACEMENT, ALL_COLUMNS,
                        COLUMN_ID + " = " + insertId, null, null,
                        null, null)) {
                    cursor.moveToFirst();
                }
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
                if (database != null && database.isOpen()) {
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
                database.delete(TABLE_CUSTOM_REPLACEMENT, COLUMN_ID + "=?", new String[]{String.valueOf(rowID)});
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
                if (database != null && database.isOpen()) {
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
                database.delete(TABLE_CUSTOM_REPLACEMENT, null, null);
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
                if (database != null && database.isOpen()) {
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
     * Get all replacements from the database, including the corresponding row identifier and
     * serialised command data.
     *
     * @return the {@link CustomReplacement} list
     */
    public ArrayList<CustomReplacement> getReplacements() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getReplacements");
        }
        final ArrayList<CustomReplacement> replacements = new ArrayList<>();
        try {
            open();
            if (database.isOpen()) {
                try (Cursor cursor = database.query(TABLE_CUSTOM_REPLACEMENT, ALL_COLUMNS, null, null, null, null, null)) {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        replacements.add(new CustomReplacement(cursor.getString(1), cursor.getString(2), cursor.getLong(0), cursor.getString(3)));
                        cursor.moveToNext();
                    }
                }
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getReplacements: IllegalStateException");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getReplacements: SQLException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getReplacements: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (database != null && database.isOpen()) {
                    close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getReplacements: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getReplacements: SQLException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getReplacements: Exception");
                    e.printStackTrace();
                }
            }
        }
        return replacements;
    }
}

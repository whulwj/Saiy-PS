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

import ai.saiy.android.custom.CustomNickname;
import ai.saiy.android.utils.MyLog;

public class DBCustomNickname extends SQLiteOpenHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = DBCustomReplacement.class.getSimpleName();

    private static final String DATABASE_NAME = "customNickname.db";
    public static final String TABLE_CUSTOM_NICKNAME = "custom_nickname";
    private static final int DATABASE_VERSION = 1;

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NICKNAME = "nickname";
    public static final String COLUMN_CONTACT_NAME = "contact_name";
    public static final String COLUMN_SERIALISED = "serialised";

    private static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_NICKNAME, COLUMN_CONTACT_NAME, COLUMN_SERIALISED};

    private static final String DATABASE_CREATE = "create table " + TABLE_CUSTOM_NICKNAME
            + "("+ COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NICKNAME + " text not null, "
            + COLUMN_CONTACT_NAME + " text not null, "
            + COLUMN_SERIALISED + " text not null);";

    private final String databasePath;
    private SQLiteDatabase database;

    public DBCustomNickname(Context context) {
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOM_NICKNAME);
        onCreate(db);
    }

    /**
     * Insert a row, separating the command nickname and serialised class.
     *
     * @param nickname    the nickname
     * @param contactName the contact name to be used
     * @param serialised  the serialised class
     * @param isDuplicate true if a command is being replaced
     * @param rowId       the row id of the command to be replaced
     * @return a {@link Pair}, which #first field will be if the insertion was successful. False otherwise
     */
    public Pair<Boolean, Long> insertPopulatedRow(String nickname, String contactName, String serialised, boolean isDuplicate, long rowId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "insertPopulatedRow: duplicate: " + isDuplicate + " " + rowId);
        }

        boolean success = false;
        long insertId = -1;

        try {
            open();

            if (database.isOpen()) {
                final ContentValues values = new ContentValues();
                values.put(COLUMN_NICKNAME, nickname);
                values.put(COLUMN_CONTACT_NAME, contactName);
                values.put(COLUMN_SERIALISED, serialised);

                insertId = database.insert(TABLE_CUSTOM_NICKNAME, null, values);
                final Cursor cursor = database.query(TABLE_CUSTOM_NICKNAME, ALL_COLUMNS,
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
                database.delete(TABLE_CUSTOM_NICKNAME, COLUMN_ID + "=?", new String[]{String.valueOf(rowID)});
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
                database.delete(TABLE_CUSTOM_NICKNAME, null, null);
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
     * Get all nicknames from the database, including the corresponding row identifier and
     * serialised command data.
     *
     * @return the {@link CustomNickname} list
     */
    public ArrayList<CustomNickname> getNicknames() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getNicknames");
        }
        final ArrayList<CustomNickname> nicknames = new ArrayList<>();
        Cursor cursor = null;
        try {
            open();
            if (database.isOpen()) {
                cursor = database.query(TABLE_CUSTOM_NICKNAME, ALL_COLUMNS, null, null, null, null, null);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    nicknames.add(new CustomNickname(cursor.getString(1), cursor.getString(2), cursor.getLong(0), cursor.getString(3)));
                    cursor.moveToNext();
                }
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNicknames: IllegalStateException");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNicknames: SQLException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNicknames: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (cursor != null && cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Throwable t) {
                MyLog.w(CLS_NAME, "getNicknames: Throwable");
            } finally {
                try {
                    if (database.isOpen()) {
                        close();
                    }
                } catch (IllegalStateException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getNicknames: IllegalStateException");
                        e.printStackTrace();
                    }
                } catch (SQLException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getNicknames: SQLException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getNicknames: Exception");
                        e.printStackTrace();
                    }
                }
            }
        }
        return nicknames;
    }
}

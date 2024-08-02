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

import ai.saiy.android.custom.CustomPhrase;
import ai.saiy.android.utils.MyLog;

public class DBCustomPhrase extends SQLiteOpenHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = DBCustomPhrase.class.getSimpleName();

    private static final String DATABASE_NAME = "customPhrase.db";
    public static final String TABLE_CUSTOM_PHRASE = "custom_phrase";
    private static final int DATABASE_VERSION = 1;

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_KEYPHRASE = "keyphrase";
    public static final String COLUMN_RESPONSE = "response";
    public static final String COLUMN_VOICE_RECOGNITION = "voice_recognition";
    public static final String COLUMN_SERIALISED = "serialised";

    private static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_KEYPHRASE, COLUMN_RESPONSE, COLUMN_VOICE_RECOGNITION, COLUMN_SERIALISED};

    private static final String DATABASE_CREATE = "create table " + TABLE_CUSTOM_PHRASE
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_KEYPHRASE + " text not null, "
            + COLUMN_RESPONSE + " text not null, "
            + COLUMN_VOICE_RECOGNITION + " integer default 0, "
            + COLUMN_SERIALISED + " text not null);";

    private final String databasePath;
    private SQLiteDatabase database;

    public DBCustomPhrase(Context context) {
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOM_PHRASE);
        onCreate(db);
    }

    /**
     * Insert a row, separating the command phrase and serialised class.
     *
     * @param keyphrase          the keyphrase
     * @param response           the response to be used
     * @param isVoiceRecognition true if it is voice recognition
     * @param serialised         the serialised class
     * @param isDuplicate        true if a command is being replaced
     * @param rowId              the row id of the command to be replaced
     * @return true if the insertion was successful. False otherwise
     */
    public Pair<Boolean, Long> insertPopulatedRow(String keyphrase, String response, boolean isVoiceRecognition, String serialised, boolean isDuplicate, long rowId) {
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
                values.put(COLUMN_RESPONSE, response);
                values.put(COLUMN_VOICE_RECOGNITION, isVoiceRecognition ? 1 : 0);
                values.put(COLUMN_SERIALISED, serialised);

                insertId = database.insert(TABLE_CUSTOM_PHRASE, null, values);
                final Cursor cursor = database.query(TABLE_CUSTOM_PHRASE, ALL_COLUMNS,
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
                database.delete(TABLE_CUSTOM_PHRASE, COLUMN_ID + "=?", new String[]{String.valueOf(rowID)});
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
                database.delete(TABLE_CUSTOM_PHRASE, null, null);
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
     * Get all phrases from the database, including the corresponding row identifier and
     * serialised command data.
     *
     * @return the {@link CustomPhrase} list
     */
    public ArrayList<CustomPhrase> getPhrases() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getPhrases");
        }
        final ArrayList<CustomPhrase> phrases = new ArrayList<>();
        Cursor cursor = null;
        try {
            open();
            if (database.isOpen()) {
                cursor = database.query(TABLE_CUSTOM_PHRASE, ALL_COLUMNS, null, null, null, null, null);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    phrases.add(new CustomPhrase(cursor.getString(1), cursor.getString(2), cursor.getLong(3) == 1, cursor.getLong(0), cursor.getString(4)));
                    cursor.moveToNext();
                }
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getPhrases: IllegalStateException");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getPhrases: SQLException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getPhrases: Exception");
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
                        MyLog.w(CLS_NAME, "getPhrases: IllegalStateException");
                        e.printStackTrace();
                    }
                } catch (SQLException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getPhrases: SQLException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getPhrases: Exception");
                        e.printStackTrace();
                    }
                }
            }
        }
        return phrases;
    }
}

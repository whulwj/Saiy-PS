package ai.saiy.android.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Pair;

import java.util.ArrayList;

import ai.saiy.android.utils.MyLog;

public class DBApplication extends SQLiteOpenHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = DBApplication.class.getSimpleName();

    private static final String DATABASE_NAME = "application.db";
    private static final String TABLE_APPLICATION = "table_application";
    private static final int DATABASE_VERSION = 1;

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_APPLICATION_NAME = "name";
    private static final String COLUMN_APPLICATION_PACKAGE = "package";

    private static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_APPLICATION_NAME, COLUMN_APPLICATION_PACKAGE};
    private static final String DATABASE_CREATE = "create table " + TABLE_APPLICATION
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_APPLICATION_NAME + " text not null, "
            + COLUMN_APPLICATION_PACKAGE + " text not null);";

    private SQLiteDatabase database;

    public DBApplication(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "Constructor");
        }
    }

    public void open() throws SQLException {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "open");
        }
        this.database = getWritableDatabase();
    }

    public void insertData(ArrayList<Pair<String, String>> arrayList) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "insertData");
        }
        final long then = System.nanoTime();
        deleteTable();
        try {
            open();
            if (database.isOpen()) {
                database.beginTransaction();
                final SQLiteStatement compileStatement = database.compileStatement("insert into " + TABLE_APPLICATION + "(" + COLUMN_APPLICATION_NAME + ", " + COLUMN_APPLICATION_PACKAGE + ") values (?, ?)");
                for (Pair<String, String> next : arrayList) {
                    compileStatement.bindString(1, next.first);
                    compileStatement.bindString(2, next.second);
                    compileStatement.executeInsert();
                }
                database.setTransactionSuccessful();
                database.endTransaction();
            } else if (DEBUG) {
                MyLog.w(CLS_NAME, "insertData: failed to open");
            }
        } catch (IllegalStateException illegalStateException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "insertData: IllegalStateException");
                illegalStateException.printStackTrace();
            }
        } catch (SQLException sqlException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "insertData: SQLException");
                sqlException.printStackTrace();
            }
        } catch (Exception exception) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "insertData: Exception");
                exception.printStackTrace();
            }
        } finally {
            try {
                if (database != null && database.isOpen()) {
                    close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "insertData: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "insertData: SQLException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "insertData: Exception");
                    e.printStackTrace();
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, "insertData", then);
        }
    }

    /**
     * Delete all entries in the current table
     */
    public boolean deleteTable() {
        try {
            open();
            if (database.isOpen()) {
                database.delete(TABLE_APPLICATION, null, null);
                return true;
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteTable: database not open");
                }
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteTable: IllegalStateException");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteTable: SQLException");
                e.printStackTrace();
            }
        } catch (Exception e) {
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
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteTable: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteTable: SQLException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteTable: Exception");
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    /**
     * Get all applications from the database.
     *
     * @return the {@link Pair} label and package name
     */
    public ArrayList<Pair<String, String>> getApplications() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getApplications");
        }
        final long then = System.nanoTime();
        final ArrayList<Pair<String, String>> applications = new ArrayList<>();
        try {
            open();
            if (database.isOpen()) {
                try (Cursor cursor = database.query(TABLE_APPLICATION, ALL_COLUMNS, null, null, null, null, null)) {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        applications.add(new Pair<>(cursor.getString(1), cursor.getString(2)));
                        cursor.moveToNext();
                    }
                }
            }
        } catch (IllegalStateException illegalStateException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getApplications: IllegalStateException");
                illegalStateException.printStackTrace();
            }
        } catch (SQLException sqlException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getApplications: SQLException");
                sqlException.printStackTrace();
            }
        } catch (Exception exception) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getApplications: Exception");
                exception.printStackTrace();
            }
        } finally {
            try {
                if (database != null && database.isOpen()) {
                    close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getApplications: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getApplications: SQLException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getApplications: Exception");
                    e.printStackTrace();
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, "getApplications", then);
            MyLog.d(CLS_NAME, "getApplications: size: " + applications.size());
        }
        return applications;
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
            MyLog.w(CLS_NAME, "Upgrading database from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
        }
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPLICATION);
        onCreate(db);
    }
}

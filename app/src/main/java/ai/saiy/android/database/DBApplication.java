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
            if (this.database.isOpen()) {
                this.database.beginTransaction();
                final SQLiteStatement compileStatement = this.database.compileStatement("insert into " + TABLE_APPLICATION + "(" + COLUMN_APPLICATION_NAME + ", " + COLUMN_APPLICATION_PACKAGE + ") values (?, ?)");
                for (Pair<String, String> next : arrayList) {
                    compileStatement.bindString(1, next.first);
                    compileStatement.bindString(2, next.second);
                    compileStatement.executeInsert();
                }
                this.database.setTransactionSuccessful();
                this.database.endTransaction();
            } else if (DEBUG) {
                MyLog.w(CLS_NAME, "insertData: failed to open");
            }
            try {
                if (this.database.isOpen()) {
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
        } catch (IllegalStateException illegalStateException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "insertData: IllegalStateException");
                illegalStateException.printStackTrace();
            }
            try {
                if (this.database.isOpen()) {
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
        } catch (SQLException sqlException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "insertData: SQLException");
                sqlException.printStackTrace();
            }
            try {
                if (this.database.isOpen()) {
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
        } catch (Exception exception) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "insertData: Exception");
                exception.printStackTrace();
            }
            try {
                if (this.database.isOpen()) {
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
        } catch (Throwable t) {
            try {
                if (this.database.isOpen()) {
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

    public boolean deleteTable() {
        try {
            open();
            if (this.database.isOpen()) {
                this.database.delete(TABLE_APPLICATION, null, null);
                try {
                    if (!this.database.isOpen()) {
                        return true;
                    }
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "deleteTable: finally closing");
                    }
                    close();
                    return true;
                } catch (IllegalStateException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "deleteTable: IllegalStateException");
                        e.printStackTrace();
                    }
                    return true;
                } catch (SQLException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "deleteTable: SQLException");
                        e.printStackTrace();
                    }
                    return true;
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "deleteTable: Exception");
                        e.printStackTrace();
                    }
                    return true;
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "deleteTable: database not open");
                }
                try {
                    if (this.database.isOpen()) {
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
                return false;
            }
        } catch (IllegalStateException illegalStateException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteTable: IllegalStateException");
                illegalStateException.printStackTrace();
            }
            try {
                if (this.database.isOpen()) {
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
        } catch (SQLException sqlException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteTable: SQLException");
                sqlException.printStackTrace();
            }
            try {
                if (this.database.isOpen()) {
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
        } catch (Exception exception) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "deleteTable: Exception");
                exception.printStackTrace();
            }
            try {
                if (this.database.isOpen()) {
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
        } finally {
            try {
                if (this.database.isOpen()) {
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

    public ArrayList<Pair<String, String>> getApplications() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getApplications");
        }
        final long then = System.nanoTime();
        ArrayList<Pair<String, String>> arrayList = new ArrayList<>();
        Cursor query = null;
        try {
            open();
            if (this.database.isOpen()) {
                query = this.database.query(TABLE_APPLICATION, ALL_COLUMNS, null, null, null, null, null);
                query.moveToFirst();
                while (!query.isAfterLast()) {
                    arrayList.add(new Pair<>(query.getString(1), query.getString(2)));
                    query.moveToNext();
                }
                query.close();
            }
            try {
                if (this.database.isOpen()) {
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
        } catch (IllegalStateException illegalStateException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getApplications: IllegalStateException");
                illegalStateException.printStackTrace();
            }
            try {
                if (this.database.isOpen()) {
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
        } catch (SQLException sqlException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getApplications: SQLException");
                sqlException.printStackTrace();
            }
            try {
                if (this.database.isOpen()) {
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
        } catch (Exception exception) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getApplications: Exception");
                exception.printStackTrace();
            }
            try {
                if (this.database.isOpen()) {
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
        } finally {
            try {
                if (query != null && query.isClosed()) {
                    query.close();
                }
            } catch (Throwable t) {
                MyLog.w(CLS_NAME, "getApplications: Throwable");
            }
            try {
                if (this.database.isOpen()) {
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
            MyLog.d(CLS_NAME, "getApplications: size: " + arrayList.size());
        }
        return arrayList;
    }

    @Override
    public void close() throws SQLException {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "close");
        }
        this.database.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        sQLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int oldVersion, int newVersion) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onUpgrade");
            MyLog.w(CLS_NAME, "Upgrading database from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
        }
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_APPLICATION);
        onCreate(sQLiteDatabase);
    }
}

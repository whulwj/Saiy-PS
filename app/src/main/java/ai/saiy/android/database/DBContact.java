package ai.saiy.android.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;

import ai.saiy.android.contacts.Contact;
import ai.saiy.android.utils.MyLog;

public class DBContact extends SQLiteOpenHelper {
    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = DBContact.class.getSimpleName();

    private static final String DATABASE_NAME = "contacts.db";
    private static final String TABLE_CONTACTS = "contacts";
    private static final int DATABASE_VERSION = 1;

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_CONTACT_ID = "contact_id";
    private static final String COLUMN_CONTACT_NAME = "contact_name";
    private static final String COLUMN_CONTACT_NAME_PHONETIC = "contact_name_phonetic";
    private static final String COLUMN_CONTACT_FORENAME = "contact_forename";
    private static final String COLUMN_CONTACT_SURNAME = "contact_surname";
    private static final String COLUMN_CONTACT_ENCODED = "contact_encoded";
    private static final String COLUMN_CONTACT_WORD_COUNT = "contact_word_count";
    private static final String COLUMN_CONTACT_FREQUENCY = "contact_frequency";
    private static final String COLUMN_CONTACT_HAS_NUMBER = "contact_has_number";
    private static final String COLUMN_CONTACT_HAS_ADDRESS = "contact_has_address";
    private static final String COLUMN_CONTACT_HAS_EMAIL = "contact_has_email";

    private static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_CONTACT_ID, COLUMN_CONTACT_NAME, COLUMN_CONTACT_NAME_PHONETIC,
            COLUMN_CONTACT_FORENAME, COLUMN_CONTACT_SURNAME, COLUMN_CONTACT_ENCODED, COLUMN_CONTACT_WORD_COUNT, COLUMN_CONTACT_FREQUENCY,
            COLUMN_CONTACT_HAS_NUMBER, COLUMN_CONTACT_HAS_ADDRESS, COLUMN_CONTACT_HAS_EMAIL};

    private static final String DATABASE_CREATE = "create table " + TABLE_CONTACTS
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_CONTACT_ID + " text not null, "
            + COLUMN_CONTACT_NAME + " text not null, "
            + COLUMN_CONTACT_NAME_PHONETIC + " text not null, "
            + COLUMN_CONTACT_FORENAME + " text not null, "
            + COLUMN_CONTACT_SURNAME + " text not null, "
            + COLUMN_CONTACT_ENCODED + " text not null, "
            + COLUMN_CONTACT_WORD_COUNT + " integer default 0, "
            + COLUMN_CONTACT_FREQUENCY + " integer default 0, "
            + COLUMN_CONTACT_HAS_NUMBER + " integer default 0, "
            + COLUMN_CONTACT_HAS_ADDRESS + " integer default 0, "
            + COLUMN_CONTACT_HAS_EMAIL + " integer default 0);";

    private SQLiteDatabase database;

    public DBContact(Context context) {
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

    public void insertData(ArrayList<Contact> contacts) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "insertData");
        }
        final long then = System.nanoTime();
        deleteTable();
        try {
            open();
            if (this.database.isOpen()) {
                this.database.beginTransaction();
                final SQLiteStatement compileStatement = this.database.compileStatement("insert into " + TABLE_CONTACTS + "(" + COLUMN_CONTACT_ID + ", " + COLUMN_CONTACT_NAME
                        + ", " + COLUMN_CONTACT_NAME_PHONETIC + ", " + COLUMN_CONTACT_FORENAME + ", " + COLUMN_CONTACT_SURNAME + ", " + COLUMN_CONTACT_ENCODED + ", " + COLUMN_CONTACT_WORD_COUNT
                        + ", " + COLUMN_CONTACT_FREQUENCY + ", " + COLUMN_CONTACT_HAS_NUMBER + ", " + COLUMN_CONTACT_HAS_ADDRESS + ", " + COLUMN_CONTACT_HAS_EMAIL + ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                for (Contact contact : contacts) {
                    compileStatement.bindString(1, contact.getID());
                    compileStatement.bindString(2, contact.getName());
                    compileStatement.bindString(3, contact.getPhoneticName());
                    compileStatement.bindString(4, contact.getForename());
                    compileStatement.bindString(5, contact.getSurname());
                    compileStatement.bindString(6, contact.getEncoded());
                    compileStatement.bindLong(7, contact.getWordCount());
                    compileStatement.bindLong(8, contact.getFrequency());
                    compileStatement.bindLong(9, contact.hasPhoneNumber() ? 1 : 0);
                    compileStatement.bindLong(10, contact.hasAddress() ? 1 : 0);
                    compileStatement.bindLong(11, contact.hasEmail() ? 1 : 0);
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
        } finally {
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
            MyLog.getElapsed("insertData", then);
        }
    }

    public boolean deleteTable() {
        final long then = System.nanoTime();
        try {
            open();
            if (this.database.isOpen()) {
                this.database.delete(TABLE_CONTACTS, null, null);
                if (DEBUG) {
                    MyLog.getElapsed("deleteTable", then);
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
                return true;
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
                if (DEBUG) {
                    MyLog.getElapsed(CLS_NAME, "deleteTable", then);
                }
                return false;
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

    public ArrayList<Contact> getContacts() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getContacts");
        }
        final long then = System.nanoTime();
        final ArrayList<Contact> contacts = new ArrayList<>();
        Cursor query = null;
        try {
            open();
            if (this.database.isOpen()) {
                query = this.database.query(TABLE_CONTACTS, ALL_COLUMNS, null, null, null, null, null);
                query.moveToFirst();
                while (!query.isAfterLast()) {
                    contacts.add(new Contact(query.getString(1), query.getString(2), query.getString(3), query.getString(4), query.getString(5), query.getString(6), query.getInt(7), query.getInt(8), query.getInt(9) == 1, query.getInt(10) == 1, query.getInt(11) == 1));
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
                    MyLog.w(CLS_NAME, "getContacts: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getContacts: SQLException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getContacts: Exception");
                    e.printStackTrace();
                }
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getContacts: IllegalStateException");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getContacts: SQLException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getContacts: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (query != null && query.isClosed()) {
                    query.close();
                }
            } catch (Throwable t) {
                MyLog.w(CLS_NAME, "getContacts: Throwable");
            }
            try {
                if (this.database.isOpen()) {
                    close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getContacts: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getContacts: SQLException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getContacts: Exception");
                    e.printStackTrace();
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed("getContacts", then);
            MyLog.d("getContacts", "size: " + contacts.size());
        }
        return contacts;
    }

    @Override
    public void close() throws SQLException {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "close");
        }
        this.database.close();
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }
}

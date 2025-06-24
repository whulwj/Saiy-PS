package ai.saiy.android.contacts;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Pair;

import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.saiy.android.algorithms.Algorithm;
import ai.saiy.android.command.contact.Choice;
import ai.saiy.android.custom.Phone;
import ai.saiy.android.database.DBContact;
import ai.saiy.android.nlu.local.AlgorithmicResolver;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ContactHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ContactHelper.class.getSimpleName();

    private final Object lock = new Object();
    private final AtomicBoolean isBusy = new AtomicBoolean(false);

    private ArrayList<Contact> setAddress(Context context, ArrayList<Contact> contacts, String str) {
        final long then = System.nanoTime();
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID}, str, null, null);
        if (cursor != null) {
            try {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "setAddress cursor count: " + cursor.getCount());
                }
                int columnFormattedAddressIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
                int columnContactIdIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID);
                while (cursor.moveToNext()) {
                    String formattedAddress = cursor.getString(columnFormattedAddressIndex);
                    String contactId = cursor.getString(columnContactIdIndex);
                    if (formattedAddress != null && !formattedAddress.trim().isEmpty()) {
                        for (Contact contact : contacts) {
                            if (contact.getID().equals(contactId)) {
                                contact.setHasAddress(true);
                                break;
                            }
                        }
                    }
                }
            } catch (IllegalStateException illegalStateException) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "setAddress: IllegalStateException");
                    illegalStateException.printStackTrace();
                }
            } catch (SQLiteException sqLiteException) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "setAddress: SQLiteException");
                    sqLiteException.printStackTrace();
                }
            } catch (Exception exception) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "setAddress: Exception");
                    exception.printStackTrace();
                }
            } finally {
                try {
                    if (!cursor.isClosed()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "setAddress: finally closing");
                        }
                        cursor.close();
                    }
                } catch (IllegalStateException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "setAddress: IllegalStateException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "setAddress: Exception");
                        e.printStackTrace();
                    }
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed("setAddress", then);
        }
        return setEmail(context, contacts, str);
    }

    private ArrayList<Contact> getContactAlgorithmically(Context context, Locale locale, ArrayList<Contact> contacts, ArrayList<String> inputData, boolean precision) {
        ai.saiy.android.nlu.local.AlgorithmicResolver<?> algorithmicResolver;
        final long then = System.nanoTime();
        final ArrayList<Contact> toReturn = new ArrayList<>();
        if (useLegacy(context)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getContactAlgorithmically: legacy");
            }
            algorithmicResolver = new ai.saiy.android.nlu.local.AlgorithmicResolver<>(context, new Algorithm[]{Algorithm.JARO_WINKLER}, locale, inputData, contacts, AlgorithmicResolver.THREADS_TIMEOUT_2000, precision);
        } else {
            algorithmicResolver = new ai.saiy.android.nlu.local.AlgorithmicResolver<>(context, new Algorithm[]{Algorithm.JARO_WINKLER, Algorithm.SOUNDEX, Algorithm.METAPHONE, Algorithm.DOUBLE_METAPHONE}, locale, inputData, contacts, AlgorithmicResolver.THREADS_TIMEOUT_2000, precision);
        }
        final ArrayList<ai.saiy.android.nlu.local.AlgorithmicContainer> algorithmicContainers = algorithmicResolver.fetch();
        if (UtilsList.notNaked(algorithmicContainers)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "containerArray size: " + algorithmicContainers.size());
            }
            final Collator collator = Collator.getInstance(locale);
            collator.setStrength(0);
            final CollationKey firstCollationKey = collator.getCollationKey(contacts.get(algorithmicContainers.get(0).getParentPosition()).getName());
            boolean notMatched;
            for (ai.saiy.android.nlu.local.AlgorithmicContainer algorithmicContainer : algorithmicContainers) {
                Contact contact = contacts.get(algorithmicContainer.getParentPosition());
                CollationKey collationKey = collator.getCollationKey(contact.getName());
                if (firstCollationKey.compareTo(collationKey) == 0) {
                    notMatched = true;
                    for (Contact value : toReturn) {
                        if (value.getID().matches(contact.getID())) {
                            notMatched = false;
                            break;
                        }
                    }
                    if (notMatched) {
                        contact.setGeneric(algorithmicContainer.getInput());
                        toReturn.add(contact);
                    }
                }
            }
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "failed to find a match");
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getContactAlgorithmically: size " + toReturn.size());
            MyLog.getElapsed("getContactAlgorithmically", then);
        }
        return toReturn;
    }

    private void saveContacts(final Context context, final ArrayList<Contact> contacts) {
        if (!UtilsList.notNaked(contacts) || this.isBusy.get()) {
            return;
        }
        ContactHelper.this.isBusy.set(true);
        synchronized (ContactHelper.this.lock) {
            new DBContact(context).insertData(contacts);
        }
        ContactHelper.this.isBusy.set(false);
    }

    private ArrayList<Contact> setEmail(Context context, ArrayList<Contact> contacts, String str) {
        final long then = System.nanoTime();
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Email.ADDRESS, ContactsContract.CommonDataKinds.Email.CONTACT_ID}, str, null, null);
        if (cursor != null) {
            try {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "setEmail cursor count: " + cursor.getCount());
                }
                int columnAddressIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                int columnContactIdIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID);
                while (cursor.moveToNext()) {
                    String address = cursor.getString(columnAddressIndex);
                    String contactID = cursor.getString(columnContactIdIndex);
                    if (address != null && address.contains("@") && !address.trim().isEmpty()) {
                        for (Contact contact : contacts) {
                            if (contact.getID().equals(contactID)) {
                                contact.setHasEmail(true);
                                break;
                            }
                        }
                    }
                }
            } catch (IllegalStateException illegalStateException) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "setEmail: IllegalStateException");
                    illegalStateException.printStackTrace();
                }
            } catch (SQLiteException sqLiteException) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "setEmail: SQLiteException");
                    sqLiteException.printStackTrace();
                }
            } catch (Exception exception) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "setEmail: Exception");
                    exception.printStackTrace();
                }
            } finally {
                try {
                    if (!cursor.isClosed()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "setEmail: finally closing");
                        }
                        cursor.close();
                    }
                } catch (IllegalStateException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "setEmail: IllegalStateException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "setEmail: Exception");
                        e.printStackTrace();
                    }
                }
            }
        }
        Collections.sort(contacts, new Comparator<Contact>() {
            public int compare(Contact contact, Contact contact2) {
                return Integer.compare(contact2.getWordCount(), contact.getWordCount());
            }
        });
        if (DEBUG) {
            MyLog.getElapsed("setEmails", then);
        }
        return contacts;
    }

    private boolean useLegacy(Context context) {
        return !SPH.getJwdUpperThresholdForContact(context);
    }

    public Pair<Boolean, Contact> getContact(Context context, ArrayList<Contact> contacts, Contact.Weighting weighting, int type) {
        for (int i = 0; i < contacts.size(); i++) {
            switch (weighting) {
                case NONE:
                case NAME:
                    if (contacts.get(i).hasPhoneNumber()) {
                        return new Pair<>(true, contacts.get(i));
                    }
                    if (i == contacts.size() - 1) {
                        return new Pair<>(true, contacts.get(0));
                    }
                    break;
                case NUMBER:
                    String number = getNumber(context, contacts.get(i).getID(), type);
                    if (number != null) {
                        contacts.get(i).setNumber(number);
                        return new Pair<>(true, contacts.get(i));
                    }
                    break;
                case ADDRESS:
                    String address = getAddress(context, contacts.get(i).getID(), type);
                    if (address != null) {
                        contacts.get(i).setAddress(address);
                        return new Pair<>(true, contacts.get(i));
                    }
                    break;
                case EMAIL:
                    String email = getEmail(context, contacts.get(i).getID(), type);
                    if (email != null) {
                        contacts.get(i).setEmailAddress(email);
                        return new Pair<>(true, contacts.get(i));
                    }
                    break;
                case IM:
                    if (getIM(context, contacts.get(i).getID(), type) != null) {
                        return new Pair<>(true, contacts.get(i));
                    }
                    break;
            }
        }
        return !contacts.isEmpty() ? new Pair<>(false, contacts.get(0)) : new Pair<>(false, null);
    }

    public String getNameFromUri(Context context, Uri uri) {
        final long then = System.nanoTime();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, new String[] {ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameFromUri: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameFromUri: Exception");
            }
        }
        if (cursor == null) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "getNameFromUri cursor null");
            }
            return null;
        }
        try {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getNameFromUri cursor count: " + cursor.getCount());
            }
            int columnDisplayNameIndex = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME);
            while (cursor.moveToNext()) {
                String displayName = cursor.getString(columnDisplayNameIndex);
                if (UtilsString.notNaked(displayName)) {
                    continue;
                }
                if (DEBUG) {
                    MyLog.getElapsed(CLS_NAME, "getNameFromUri", then);
                }
                return displayName;
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameFromUri: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameFromUri: Exception");
            }
        }  finally {
            try {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getNameFromUri: IllegalStateException");
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getNameFromUri: Exception");
                }
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getNameFromUri: finally closing");
            }
        }
        return null;
    }

    public String getNameFromSMSPerson(Context context, String args) {
        final long then = java.lang.System.nanoTime();
        final String[] projection = new String[] {ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY};
        final String[] selectionArgs = new String[] {args};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, projection, ContactsContract.RawContacts._ID + " = ?", selectionArgs, null);
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameFromSMSPerson: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameFromSMSPerson: Exception");
            }
        }
        if (cursor == null) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "getNameFromSMSPerson cursor null");
            }
            return null;
        }
        try {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getNameFromSMSPerson cursor count: " + cursor.getCount());
            }
            int columnDisplayNameIndex = cursor.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
            while (cursor.moveToNext()) {
                String displayName = cursor.getString(columnDisplayNameIndex);
                if (UtilsString.notNaked(displayName)) {
                    continue;
                }
                if (DEBUG) {
                    MyLog.getElapsed(CLS_NAME, "getNameFromSMSPerson", then);
                }
                return displayName;
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameFromSMSPerson: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameFromSMSPerson: Exception");
            }
        }  finally {
            try {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getNameFromSMSPerson: IllegalStateException");
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getNameFromSMSPerson: Exception");
                }
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getNameFromSMSPerson: finally closing");
            }
        }
        return null;
    }

    public String getNumber(Context context, String args, int type) {
        if (DEBUG) {
            switch (type) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    MyLog.i(CLS_NAME, "getNumber: TYPE_HOME");
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    MyLog.i(CLS_NAME, "getNumber: TYPE_MOBILE");
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    MyLog.i(CLS_NAME, "getNumber: TYPE_WORK");
                    break;
                default:
                    break;
            }
        }
        final long then = java.lang.System.nanoTime();
        final String[] projection = new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.IS_PRIMARY, ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
        final String[] selectionArgs = new String[] {args};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", selectionArgs, null);
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNumber: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNumber: Exception");
            }
        }
        if (cursor == null) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "getNumber cursor null");
            }
            return null;
        }
        try {
            if (DEBUG) {
                MyLog.d(CLS_NAME, " cursor count: " + cursor.getCount());
            }
            int columnNumberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int columnTypeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
            int columnIsPrimaryIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY);
            while (cursor.moveToNext()) {
                String number = cursor.getString(columnNumberIndex);
                if (!UtilsString.notNaked(number)) {
                    continue;
                }
                if (type != ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM) {
                    if (cursor.getInt(columnTypeIndex) != type) {
                        continue;
                    }
                }
                int isPrimaryFlag = cursor.getInt(columnIsPrimaryIndex);
                if (isPrimaryFlag != 1) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "getNumber secondary: " + number);
                    }
                    continue;
                }
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "getNumber primary: " + number);
                }
                if (DEBUG) {
                    MyLog.getElapsed(CLS_NAME, "getNumber", then);
                }
                return number;
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNumber: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNumber: Exception");
            }
        }  finally {
            try {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getNumber: IllegalStateException");
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getNumber: Exception");
                }
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getNumber: finally closing");
            }
        }
        return null;
    }

    public ArrayList<Contact> getContacts(Context context) {
        ArrayList<Contact> contacts = new DBContact(context).getContacts();
        if (UtilsList.notNaked(contacts)) {
            return contacts;
        }
        contacts = getAllContacts(context);
        if (!UtilsList.notNaked(contacts)) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getContacts: failed");
            }
            return null;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getContacts: reinserting");
        }
        saveContacts(context, contacts);
        return contacts;
    }

    public ArrayList<Choice> getChoices(Context context, ArrayList<Contact> contacts) {
        final long then = System.nanoTime();
        final String[] contactIDarray = new String[contacts.size()];
        for (int i = 0; i < contacts.size(); ++i) {
            contactIDarray[i] = contacts.get(i).getID();
        }
        final ArrayList<Choice> choices = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.IS_PRIMARY, ContactsContract.CommonDataKinds.Phone.CONTACT_ID}, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " IN (" + TextUtils.join(Constants.SEP_COMMA, contactIDarray) + ")", null, null);
        if (cursor != null) {
            try {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "getChoices cursor count: " + cursor.getCount());
                }
                int columnNumberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int columnTypeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
                int columnIsPrimaryIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY);
                int columnContactIdIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
                while (cursor.moveToNext()) {
                    String number = cursor.getString(columnNumberIndex);
                    if (number != null && !number.trim().isEmpty()) {
                        Choice choice = new Choice(cursor.getString(columnContactIdIndex), cursor.getInt(columnTypeIndex), number, cursor.getInt(columnIsPrimaryIndex) == 1);
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "getChoices number: " + number + " : primary: " + (cursor.getInt(columnIsPrimaryIndex) == 1));
                        }
                        choices.add(choice);
                    }
                }
            } catch (IllegalStateException illegalStateException) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getChoices: IllegalStateException");
                    illegalStateException.printStackTrace();
                }
            } catch (SQLiteException sqLiteException) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getChoices: SQLiteException");
                    sqLiteException.printStackTrace();
                }
            } catch (Exception exception) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getChoices: Exception");
                    exception.printStackTrace();
                }
            } finally {
                try {
                    if (!cursor.isClosed()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getChoices: finally closing");
                        }
                        cursor.close();
                    }
                } catch (IllegalStateException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getChoices: IllegalStateException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getChoices: Exception");
                        e.printStackTrace();
                    }
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed("getChoices", then);
        }
        return choices;
    }

    public ArrayList<Contact> getContactFromName(Context context, Locale locale, ArrayList<String> arrayList, boolean precise) {
        ArrayList<Contact> contacts = new ArrayList<>();
        if (UtilsList.notNaked(arrayList)) {
            final ArrayList<Contact> allContacts = getContacts(context);
            if (!UtilsList.notNaked(allContacts)) {
                return null;
            }
            final long then = System.nanoTime();
            final Collator collator = Collator.getInstance(locale);
            collator.setStrength(0);
            boolean notMatched;
            for (String key : arrayList) {
                CollationKey collationKey = collator.getCollationKey(key);
                for (Contact contact : allContacts) {
                    if (collationKey.compareTo(collator.getCollationKey(contact.getName())) == 0) {
                        notMatched = true;
                        for (Contact matchedContact : contacts) {
                            if (matchedContact.getID().matches(contact.getID())) {
                                notMatched = false;
                                break;
                            }
                        }
                        if (notMatched) {
                            contacts.add(contact);
                        }
                    }
                }
            }
            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, "collator", then);
            }
            if (!contacts.isEmpty()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getContactFromName: PRIMARY match: " + contacts.size());
                    for (Contact contact : contacts) {
                        MyLog.v(CLS_NAME, "PRIMARY match: " + contact.getName());
                    }
                }
                return contacts;
            }
            String[] separated = arrayList.get(0).split("\\s");
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getContactFromName: " + separated.length);
            }
            contacts.addAll(getContactAlgorithmically(context, locale, allContacts, arrayList, precise));
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "getContactFromName: naked");
        }
        return contacts;
    }

    public Phone getNameAndNumberFromUri(Context context, Uri uri) {
        Phone phone = null;
        final long then = System.nanoTime();
        Cursor cursor = context.getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.Data.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.TYPE}, null, null, null);
        try {
            if (cursor != null) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "getNameAndNumberFromUri cursor count: " + cursor.getCount());
                }
                int columnDisplayNameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int columnNumberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int columnTypeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
                while (cursor.moveToNext()) {
                    String name = cursor.getString(columnDisplayNameIndex);
                    String number = cursor.getString(columnNumberIndex);
                    int type = cursor.getInt(columnTypeIndex);
                    if (UtilsString.notNaked(name) && UtilsString.notNaked(number)) {
                        phone = new Phone(name, number, type);
                        break;
                    }
                }
            } else if (DEBUG) {
                MyLog.v(CLS_NAME, "getNameAndNumberFromUri cursor null");
            }
        } catch (SQLiteException sqLiteException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameAndNumberFromUri: SQLiteException");
                sqLiteException.printStackTrace();
            }
        } catch (IllegalStateException illegalStateException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameAndNumberFromUri: IllegalStateException");
                illegalStateException.printStackTrace();
            }
        } catch (Exception exception) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameAndNumberFromUri: Exception");
                exception.printStackTrace();
            }
        } finally {
            try {
                if (cursor != null && !cursor.isClosed()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getNameAndNumberFromUri: finally closing");
                    }
                    cursor.close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getNameAndNumberFromUri: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getNameAndNumberFromUri: Exception");
                    e.printStackTrace();
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, "getNameAndNumberFromUri", then);
        }
        return phone;
    }

    public String getNameFromNumber(Context context, String number) {
        final long then = java.lang.System.nanoTime();
        final String[] projection = new java.lang.String[] {ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)), projection, null, null, null);
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameFromNumber: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameFromNumber: Exception");
            }
        }
        if (cursor == null) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "getNameFromNumber cursor null");
            }
            return null;
        }
        try {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getNameFromNumber cursor count: " + cursor.getCount());
            }
            int columnDisplayNameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
            while (cursor.moveToNext()) {
                String displayName = cursor.getString(columnDisplayNameIndex);
                if (UtilsString.notNaked(displayName)) {
                    continue;
                }
                if (DEBUG) {
                    MyLog.getElapsed(CLS_NAME, "getNameFromNumber", then);
                }
                return displayName;
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameFromNumber: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameFromNumber: Exception");
            }
        } finally {
            try {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getNameFromNumber: IllegalStateException");
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getNameFromNumber: Exception");
                }
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getNameFromNumber: finally closing");
            }
        }
        return null;
    }

    public String getAddress(Context context, String contactID, int type) {
        String address = null;
        if (DEBUG) {
            switch (type) {
                case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME:
                    MyLog.i(CLS_NAME, "getAddress: TYPE_HOME");
                    break;
                case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK:
                    MyLog.i(CLS_NAME, "getAddress: TYPE_WORK");
                    break;
            }
        }
        final long then = System.nanoTime();
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, ContactsContract.CommonDataKinds.StructuredPostal.TYPE}, ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?", new String[]{contactID}, null);
        if (cursor != null) {
            try {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "getAddress cursor count: " + cursor.getCount());
                }
                int columnFormattedAddressIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
                int columnTypeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE);
                while (cursor.moveToNext()) {
                    String formattedAddress = cursor.getString(columnFormattedAddressIndex);
                    if (formattedAddress != null && !formattedAddress.trim().isEmpty() && (type == ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM || cursor.getInt(columnTypeIndex) == type)) {
                        address = formattedAddress;
                    }
                }
                if (DEBUG) {
                    MyLog.getElapsed("getAddress", then);
                }
            } catch (SQLiteException sqLiteException) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getAddress: SQLiteException");
                    sqLiteException.printStackTrace();
                }
            } catch (IllegalStateException illegalStateException) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getAddress: IllegalStateException");
                    illegalStateException.printStackTrace();
                }
            } catch (Exception exception) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getAddress: Exception");
                    exception.printStackTrace();
                }
            } finally {
                try {
                    if (!cursor.isClosed()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getAddress: finally closing");
                        }
                        cursor.close();
                    }
                } catch (IllegalStateException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getAddress: IllegalStateException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getAddress: Exception");
                        e.printStackTrace();
                    }
                }
            }
        }
        return address;
    }

    public void syncContacts(final Context context) {
        if (this.isBusy.get()) {
            return;
        }
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_LESS_FAVORABLE);
                ContactHelper.this.isBusy.set(true);
                final ArrayList<Contact> allContacts = new ContactHelper().getAllContacts(context);
                ContactHelper.this.isBusy.set(false);
                ContactHelper.this.saveContacts(context, allContacts);
            }
        });
    }

    public String getNameFromEmail(Context context, String address) {
        final long then = java.lang.System.nanoTime();
        final String[] projection = new String[] {ContactsContract.CommonDataKinds.Email.ADDRESS, ContactsContract.CommonDataKinds.Email.DISPLAY_NAME};
        final String[] selectionArgs = new String[] {address};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, projection, ContactsContract.CommonDataKinds.Email.ADDRESS + " LIKE ?", selectionArgs, null);
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameFromEmail: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameFromEmail: Exception");
            }
        }
        if (cursor == null) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "getNameFromEmail cursor null");
            }
            return null;
        }
        try {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getNameFromEmail cursor count: " + cursor.getCount());
            }
            int columnDisplayNameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME);
            while (cursor.moveToNext()) {
                String displayName = cursor.getString(columnDisplayNameIndex);
                if (UtilsString.notNaked(displayName)) {
                    continue;
                }
                if (DEBUG) {
                    MyLog.getElapsed(CLS_NAME, "getNameFromEmail", then);
                }
                return displayName;
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameFromEmail: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNameFromEmail: Exception");
            }
        }  finally {
            try {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getNameFromEmail: IllegalStateException");
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getNameFromEmail: Exception");
                }
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getNameFromEmail: finally closing");
            }
        }
        return null;
     }

    public String getEmail(Context context, String contactID, int type) {
        String emailAddress = null;
        if (DEBUG) {
            switch (type) {
                case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                    MyLog.i(CLS_NAME, "getEmail: TYPE_HOME");
                    break;
                case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                    MyLog.i(CLS_NAME, "getEmail: TYPE_WORK");
                    break;
            }
        }
        final long then = System.nanoTime();
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Email.ADDRESS, ContactsContract.CommonDataKinds.Email.TYPE}, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{contactID}, null);
        if (cursor != null) {
            try {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "getEmail cursor count: " + cursor.getCount());
                }
                int columnAddressIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                int columnTypeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE);
                String address;
                while (cursor.moveToNext()) {
                    address = cursor.getString(columnAddressIndex);
                    if (address != null && address.contains("@") && !address.trim().isEmpty()) {
                        if (type == 0 || cursor.getInt(columnTypeIndex) == type) {
                            emailAddress = address;
                        }
                    }
                }
                if (DEBUG) {
                    MyLog.getElapsed("getEmail", then);
                }
            } catch (IllegalStateException illegalStateException) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getEmail: IllegalStateException");
                    illegalStateException.printStackTrace();
                }
            } catch (SQLiteException sqLiteException) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getEmail: SQLiteException");
                    sqLiteException.printStackTrace();
                }
            } catch (Exception exception) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getEmail: Exception");
                    exception.printStackTrace();
                }
            } finally {
                try {
                    if (!cursor.isClosed()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getEmail: finally closing");
                        }
                        cursor.close();
                    }
                } catch (IllegalStateException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getEmail: IllegalStateException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getEmail: Exception");
                        e.printStackTrace();
                    }
                }
            }
        }
        return emailAddress;
    }

    public ArrayList<Contact> getAllContacts(Context context) {
        final long then = System.nanoTime();
        final ArrayList<Contact> contacts = new ArrayList<>();
        final String[] selectionArgs = {ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
        final String[] projection = new String[] {ContactsContract.Data.HAS_PHONE_NUMBER,
                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                ContactsContract.Data.PHONETIC_NAME,
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.Data.TIMES_CONTACTED,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? ContactsContract.Data.TIMES_USED : ContactsContract.Data.TIMES_CONTACTED};
        Cursor cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projection, ContactsContract.Data.MIMETYPE + " = ?", selectionArgs, null);
        if (cursor != null) {
            try {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "getAllContacts cursor count: " + cursor.getCount());
                }
                int columnIndex = cursor.getColumnIndex(ContactsContract.Data.HAS_PHONE_NUMBER);
                int columnContactIdIndex = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID);
                int columnDisplayNameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME);
                int columnPhoneticNameIndex = cursor.getColumnIndex(ContactsContract.Data.PHONETIC_NAME);
                int columnGivenNameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
                int columnFamilyNameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME);
                int columnTimesIndex = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? cursor.getColumnIndex(ContactsContract.Data.TIMES_USED) : cursor.getColumnIndex(ContactsContract.Data.TIMES_CONTACTED);
                while (cursor.moveToNext()) {
                    String contactID = cursor.getString(columnContactIdIndex);
                    String name = cursor.getString(columnDisplayNameIndex);
                    String phoneticName = cursor.getString(columnPhoneticNameIndex);
                    String forename = cursor.getString(columnGivenNameIndex);
                    String surname = cursor.getString(columnFamilyNameIndex);
                    if (name != null && contactID != null && !name.trim().isEmpty() && !name.contains("@")) {
                        contacts.add(new Contact(contactID, name, phoneticName, forename, surname, name.split("\\s").length, cursor.getInt(columnTimesIndex), cursor.getInt(columnIndex) > 0, false, false));
                    }
                }
                cursor.close();
            } catch (IllegalStateException illegalStateException) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getAllContacts: IllegalStateException");
                    illegalStateException.printStackTrace();
                }
            } catch (SQLiteException sqLiteException) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getAllContacts: SQLiteException");
                    sqLiteException.printStackTrace();
                }
            } catch (Exception exception) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getAllContacts: Exception");
                    exception.printStackTrace();
                }
            } finally {
                try {
                    if (!cursor.isClosed()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getAllContacts: finally closing");
                        }
                        cursor.close();
                    }
                } catch (IllegalStateException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getAllContacts: IllegalStateException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getAllContacts: Exception");
                        e.printStackTrace();
                    }
                }
            }
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "getAllContacts: size " + contacts.size());
            MyLog.getElapsed("getAllContacts", then);
        }
        final String[] contactIDs = new String[contacts.size()];
        for (int i = 0; i < contacts.size(); ++i) {
            contactIDs[i] = contacts.get(i).getID();
        }
        return setAddress(context, contacts, ContactsContract.Data.CONTACT_ID + " IN (" + TextUtils.join(Constants.SEP_COMMA, contactIDs) + ")");
    }

    public String getUserProfileName(Context context) {
        String displayName = null;
        final long then = System.nanoTime();
        Cursor cursor = context.getContentResolver().query(ContactsContract.Profile.CONTENT_URI, new String[]{ContactsContract.Profile.DISPLAY_NAME}, null, null, null);
        try {
            if (cursor != null) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "getUserProfileName cursor count: " + cursor.getCount());
                }
                if (cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Profile.DISPLAY_NAME));
                }
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "getUserProfileName cursor empty");
                }
            } else if (DEBUG) {
                MyLog.v(CLS_NAME, "getUserProfileName cursor null");
            }
            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, "getUserProfileName", then);
            }
        } catch (SQLiteException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getUserProfileName: SQLiteException");
                e.printStackTrace();
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getUserProfileName: IllegalStateException");
                e.printStackTrace();
            }
        } catch (IllegalArgumentException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getUserProfileName: IllegalArgumentException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getUserProfileName: Exception");
                e.printStackTrace();
            }
        } finally {
            try {
                if (cursor != null && !cursor.isClosed()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getUserProfileName: finally closing");
                    }
                    cursor.close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getUserProfileName: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getUserProfileName: Exception");
                    e.printStackTrace();
                }
            }
        }
        return displayName;
    }

    public String getNormalisedNumber(Context context, String number) {
        final long then = java.lang.System.nanoTime();
        final String[] projection = new String[] {ContactsContract.PhoneLookup.NORMALIZED_NUMBER};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)), projection, null, null, null);
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNormalisedNumber: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNormalisedNumber: Exception");
            }
        }
        if (cursor == null) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "getNormalisedNumber cursor null");
            }
            return null;
        }
        try {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getNormalisedNumber cursor count: " + cursor.getCount());
            }
            int columnNormalisedNumberIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.NORMALIZED_NUMBER);
            while (cursor.moveToNext()) {
                String normalisedNumber = cursor.getString(columnNormalisedNumberIndex);
                if (UtilsString.notNaked(normalisedNumber)) {
                    continue;
                }
                if (DEBUG) {
                    MyLog.getElapsed(CLS_NAME, "getNormalisedNumber", then);
                }
                return normalisedNumber;
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNormalisedNumber: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNormalisedNumber: Exception");
            }
        } finally {
            try {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getNormalisedNumber: IllegalStateException");
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getNormalisedNumber: Exception");
                }
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getNormalisedNumber: finally closing");
            }
        }
        return null;
     }

    public String getIM(Context context, String contactID, int type) {
        String str = null;
        if (DEBUG) {
            if (type == ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE) {
                MyLog.i(CLS_NAME, "getIM: PROTOCOL_SKYPE");
            } else {
                MyLog.w(CLS_NAME, "getIM: PROTOCOL unknown: " + type);
            }
        }
        final long then = System.nanoTime();
        Cursor cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.DATA}, ContactsContract.Data.CONTACT_ID + " = ?", new String[]{contactID}, null);
        if (cursor != null) {
            try {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "getIM cursor count: " + cursor.getCount());
                }
                int columnDataIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA);
                int columnProtocolIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL);
                while (cursor.moveToNext()) {
                    String data = cursor.getString(columnDataIndex);
                    if (data != null && !data.trim().isEmpty()) {
                        if (cursor.getInt(columnProtocolIndex) == ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE) {
                            str = data;
                        }
                    }
                }
            } catch (IllegalStateException illegalStateException) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getIM: IllegalStateException");
                    illegalStateException.printStackTrace();
                }
            } catch (SQLiteException sqLiteException) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getIM: SQLiteException");
                    sqLiteException.printStackTrace();
                }
            } catch (Exception exception) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getIM: Exception");
                    exception.printStackTrace();
                }
            } finally {
                try {
                    if (!cursor.isClosed()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getIM: finally closing");
                        }
                        cursor.close();
                    }
                } catch (IllegalStateException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getIM: IllegalStateException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getIM: Exception");
                        e.printStackTrace();
                    }
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed("getIM", then);
        }
        return str;
    }

    public String getIdFromNumber(Context context, String number) {
        final long then = java.lang.System.nanoTime();
        final String[] projection = new String[] {ContactsContract.PhoneLookup._ID};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)), projection, null, null, null);
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getIdFromNumber: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getIdFromNumber: Exception");
            }
        }
        if (cursor == null) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "getIdFromNumber cursor null");
            }
            return null;
        }
        try {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getIdFromNumber cursor count: " + cursor.getCount());
            }
            int columnIdIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID);
            while (cursor.moveToNext()) {
                String id = cursor.getString(columnIdIndex);
                if (UtilsString.notNaked(id)) {
                    continue;
                }
                if (DEBUG) {
                    MyLog.getElapsed(CLS_NAME, "getIdFromNumber", then);
                }
                return id;
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getIdFromNumber: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getIdFromNumber: Exception");
            }
        } finally {
            try {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getIdFromNumber: IllegalStateException");
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getIdFromNumber: Exception");
                }
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getIdFromNumber: finally closing");
            }
        }
        return null;
     }

    public ArrayList<String> getRawIdArray(Context context, String displayName) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "getRawIdArray searching for: " + displayName);
        }
        final long then = System.nanoTime();
        final ArrayList<String> rawIDs = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, new String[]{ContactsContract.RawContacts._ID}, ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY + " LIKE ?", new String[]{displayName}, null);
        try {
            if (cursor != null) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "getRawIdArray cursor count: " + cursor.getCount());
                }
                int columnIdIndex = cursor.getColumnIndex(ContactsContract.RawContacts._ID);
                while (cursor.moveToNext()) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "getRawIdArray id: " + cursor.getString(columnIdIndex));
                    }
                    rawIDs.add(cursor.getString(columnIdIndex));
                }
            } else if (DEBUG) {
                MyLog.v(CLS_NAME, "getRawIdArray cursor null");
            }
        } catch (SQLiteException sqLiteException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRawIdArray: SQLiteException");
                sqLiteException.printStackTrace();
            }
        } catch (IllegalStateException illegalStateException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRawIdArray: IllegalStateException");
                illegalStateException.printStackTrace();
            }
        } catch (Exception exception) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRawIdArray: Exception");
                exception.printStackTrace();
            }
        } finally {
            try {
                if (cursor != null && !cursor.isClosed()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getRawIdArray: finally closing");
                    }
                    cursor.close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getRawIdArray: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getRawIdArray: Exception");
                    e.printStackTrace();
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed("getRawIdArray", then);
        }
        return rawIDs;
    }
}

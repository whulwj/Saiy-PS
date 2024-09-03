package ai.saiy.android.command.sms;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.text.TextUtils;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.List;

import ai.saiy.android.R;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.contacts.ContactHelper;
import ai.saiy.android.utils.MyLog;

public class SmsHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = SmsHelper.class.getSimpleName();

    private static String exclamation_mark;
    private static String exclamation_point;
    private static String question_mark;
    private static String kiss;
    private static String kiss_kiss;
    private static String kiss_kiss_kiss;

    private static final Uri CONTENT_URI;
    private static final String READ;
    private static final String DATE;
    private static final String PERSON;
    private static final String ADDRESS;
    private static final String BODY;

    private static final String DEFAULT_SORT_ORDER;
    private static final String VND_ANDROID_DIR_MMS_SMS = "vnd.android-dir/mms-sms";

    static {
        CONTENT_URI = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Telephony.Sms.Inbox.CONTENT_URI : Uri.parse("content://sms/inbox");
        READ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Telephony.Sms.Inbox.READ : "read";
        DATE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Telephony.Sms.Inbox.DATE : "date";
        PERSON = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Telephony.Sms.Inbox.PERSON : "person";
        ADDRESS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Telephony.Sms.Inbox.ADDRESS : "address";
        BODY = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Telephony.Sms.Inbox.BODY : "body";
        DEFAULT_SORT_ORDER = DATE + " DESC";
    }

    public static String replaceMarks(Context context, String str) {
        if (!ai.saiy.android.utils.UtilsString.notNaked(str)) {
            return "";
        }
        if (exclamation_mark == null) {
            initStrings(context);
        }
        String replaceAll = str.replaceAll("\\b " + exclamation_mark + "\\b", "!").replaceAll("\\b " + exclamation_point + "\\b", "!").replaceAll("\\b " + question_mark + "\\b", "?").replaceAll("\\b" + kiss_kiss_kiss + "\\b", "xxx").replaceAll("\\b" + kiss_kiss + "\\b", "xx");
        return replaceAll.endsWith(kiss) ? ai.saiy.android.utils.UtilsString.replaceLast(replaceAll, kiss, "x") : replaceAll;
    }

    public static boolean sendSMS(Context context, String receiver, String message, final boolean proofRead) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sendSMS: proofRead: " + proofRead);
            MyLog.i(CLS_NAME, "sendSMS: message size: " + message.length());
        }
        final String messageSignature = ai.saiy.android.utils.SPH.getMessageSignature(context);
        if (ai.saiy.android.utils.SPH.getMessageSignatureCondition(context) && ai.saiy.android.utils.UtilsString.notNaked(messageSignature)) {
            message = message + "\n\n" + messageSignature;
        }
        if (proofRead) {
            Intent intent;
            final String defaultSmsPackage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Telephony.Sms.getDefaultSmsPackage(context) : null;
            if (ai.saiy.android.utils.SPH.getSmsBodyFix(context) || (defaultSmsPackage != null && defaultSmsPackage.matches(Installed.PACKAGE_GOOGLE_HANGOUT))) {
                intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + receiver));
                if (defaultSmsPackage != null) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "sendSMS: defaultSMS: " + defaultSmsPackage);
                    }
                    intent.setPackage(defaultSmsPackage);
                }
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.putExtra("address", receiver);
                intent.setType(VND_ANDROID_DIR_MMS_SMS);
            }
            intent.putExtra("sms_body", message);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "sendSMS Exception");
                    e.printStackTrace();
                }
            }
        } else {
            try {
                final SmsManager smsManager = SmsManager.getDefault();
                final ArrayList<String> messageParts = smsManager.divideMessage(message);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "sendSMS: messageParts: " + messageParts.size());
                }
                if (messageParts.size() > 1) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "sendSMS: sending multipart");
                    }
                    smsManager.sendMultipartTextMessage(receiver, null, messageParts, null, null);
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "sendSMS: sending single message");
                    }
                    smsManager.sendTextMessage(receiver, null, message, null, null);
                }
                return true;
            } catch (IllegalArgumentException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "sendSMS IllegalArgumentException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "sendSMS Exception");
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static boolean isLengthWithinMax(String str) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "isLengthWithinMax: messageParts: " + SmsManager.getDefault().divideMessage(str).size());
        }
        return SmsManager.getDefault().divideMessage(str).size() < 7;
    }

    public static String getDefaultSMSPackage(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getDefaultSMSPackage: getDefaultSmsPackage: " + Telephony.Sms.getDefaultSmsPackage(context));
            }
            return Telephony.Sms.getDefaultSmsPackage(context);
        }
        final List<ResolveInfo> queryIntentActivities = context.getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_VIEW).addCategory(Intent.CATEGORY_DEFAULT).setType(VND_ANDROID_DIR_MMS_SMS), PackageManager.MATCH_DEFAULT_ONLY);
        if (ai.saiy.android.utils.UtilsList.notNaked(queryIntentActivities)) {
            final String packageName = UtilsApplication.getPackageName(queryIntentActivities.get(0));
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getDefaultSMSPackage: resolveInfoList: " + packageName);
            }
            return packageName;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "getDefaultSMSPackage: not resolved");
        }
        return null;
    }

    private static void initStrings(Context context) {
        exclamation_mark = context.getString(R.string.exclamation_mark);
        exclamation_point = context.getString(R.string.exclamation_point);
        question_mark = context.getString(R.string.question_mark);
        kiss = context.getString(R.string.kiss);
        kiss_kiss = context.getString(R.string.kiss_kiss);
        kiss_kiss_kiss = context.getString(R.string.kiss_kiss_kiss);
    }

    public Message getMostRecentMessage(Context context) {
        Message message = null;
        final long then = System.nanoTime();
        Cursor cursor = context.getContentResolver().query(CONTENT_URI, new String[]{BODY, ADDRESS, READ, DATE, PERSON}, null, null, DEFAULT_SORT_ORDER + " LIMIT 1");
        if (cursor == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getMostRecentMessage cursor: null");
            }
            return null;
        }
        try {
            if (cursor.getCount() > 0) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "getMostRecentMessage cursor count: " + cursor.getCount());
                }
                int columnBodyIndex = cursor.getColumnIndex(BODY);
                int columnAddressIndex = cursor.getColumnIndex(ADDRESS);
                int columnReadIndex = cursor.getColumnIndex(READ);
                int columnDateIndex = cursor.getColumnIndex(DATE);
                int columnPersonIndex = cursor.getColumnIndex(PERSON);
                cursor.moveToFirst();
                String body = cursor.getString(columnBodyIndex);
                String address = cursor.getString(columnAddressIndex);
                boolean isRead = cursor.getInt(columnReadIndex) > 0;
                long date = cursor.getLong(columnDateIndex);
                String person = cursor.getString(columnPersonIndex);
                if (body != null && !body.trim().isEmpty()) {
                    message = new Message(address, body, date, person, isRead);
                }
            }
        } catch (SQLiteException sqLiteException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getMostRecentMessage: SQLiteException");
                sqLiteException.printStackTrace();
            }
        } catch (IllegalStateException illegalStateException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getMostRecentMessage: IllegalStateException");
                illegalStateException.printStackTrace();
            }
        } catch (Exception exception) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getMostRecentMessage: Exception");
                exception.printStackTrace();
            }
        } finally {
            try {
                if (!cursor.isClosed()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getMostRecentMessage: finally closing");
                    }
                    cursor.close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getMostRecentMessage: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getMostRecentMessage: Exception");
                    e.printStackTrace();
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed("getMostRecentMessage", then);
        }
        return message;
    }

    public Message getMostRecentContactMessage(Context context, ArrayList<String> rawIDs) {
        Message message = null;
        final long then = System.nanoTime();
        final java.lang.String[] rawIdArray = new java.lang.String[rawIDs.size()];
        for (int i = 0; i < rawIDs.size(); ++i) {
            rawIdArray[i] = rawIDs.get(i);
        }
        final String selection = PERSON + " IN " + "(" + TextUtils.join(XMLResultsHandler.SEP_COMMA, rawIdArray) + ")";
        Cursor cursor = context.getContentResolver().query(CONTENT_URI, new String[]{BODY, ADDRESS, READ, DATE, PERSON}, selection, null, DEFAULT_SORT_ORDER + " LIMIT 1");
        if (cursor == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getMostRecentContactMessage cursor null");
            }
            return null;
        }
        try {
            if (cursor.getCount() > 0) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "getMostRecentContactMessage cursor count: " + cursor.getCount());
                }
                int columnBodyIndex = cursor.getColumnIndex(BODY);
                int columnAddressIndex = cursor.getColumnIndex(ADDRESS);
                int columnReadIndex = cursor.getColumnIndex(READ);
                int columnDateIndex = cursor.getColumnIndex(DATE);
                int columnPersonIndex = cursor.getColumnIndex(PERSON);
                cursor.moveToFirst();
                String body = cursor.getString(columnBodyIndex);
                String address = cursor.getString(columnAddressIndex);
                boolean isRead = cursor.getInt(columnReadIndex) > 0;
                long date = cursor.getLong(columnDateIndex);
                String person = cursor.getString(columnPersonIndex);
                if (body != null && !body.trim().isEmpty()) {
                    message = new Message(address, body, date, person, isRead);
                }
            }
        } catch (SQLiteException sqLiteException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getMostRecentContactMessage: SQLiteException");
                sqLiteException.printStackTrace();
            }
        } catch (IllegalStateException illegalStateException) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getMostRecentContactMessage: IllegalStateException");
                illegalStateException.printStackTrace();
            }
        } catch (Exception exception) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getMostRecentContactMessage: Exception");
                exception.printStackTrace();
            }
        } finally {
            try {
                if (!cursor.isClosed()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getMostRecentContactMessage: finally closing");
                    }
                    cursor.close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getMostRecentContactMessage: IllegalStateException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getMostRecentContactMessage: Exception");
                    e.printStackTrace();
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed("getMostRecentContactMessage", then);
        }
        return message;
    }

    public String resolveSender(Context context, String person, String address) {
        String name = null;
        final ContactHelper contactHelper = new ContactHelper();
        if (ai.saiy.android.utils.UtilsString.notNaked(address) && !org.apache.commons.lang3.StringUtils.isAlpha(address)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "resolveSender: trying address");
            }
            name = contactHelper.getNameFromNumber(context, address);
            if (ai.saiy.android.utils.UtilsString.notNaked(name)) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "resolveSender: name resolved from address: " + name);
                }
                return name;
            }
            if (DEBUG) {
                MyLog.d(CLS_NAME, "resolveSender: trying address: failed");
            }
        }
        if (ai.saiy.android.utils.UtilsString.notNaked(person)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "resolveSender: trying person");
            }
            name = contactHelper.getNameFromSMSPerson(context, person);
            if (ai.saiy.android.utils.UtilsString.notNaked(name)) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "resolveSender: name resolved from person: " + name);
                }
                return name;
            }
            if (DEBUG) {
                MyLog.d(CLS_NAME, "resolveSender: trying person: failed");
            }
        }
        if (ai.saiy.android.utils.UtilsString.notNaked(address) && org.apache.commons.lang3.StringUtils.isAlpha(address)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "resolveSender: returning address as alpha");
            }
            return address;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "resolveSender: failed to resolve sender");
        }
        return name;
    }

    public Message getMostRecentMessageByName(Context context, ArrayList<String> senders) {
        final long then = System.nanoTime();
        final String[] selectionArgs = new String[senders.size()];
        for (int i = 0; i < senders.size(); ++i) {
            selectionArgs[i] = senders.get(i);
        }
        final String selection = ADDRESS + " LIKE " + android.text.TextUtils.join(" OR " + ADDRESS + " LIKE ", java.util.Collections.nCopies(selectionArgs.length, "?"));
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(CONTENT_URI, new String[]{BODY, ADDRESS, READ, DATE, PERSON}, selection, selectionArgs, DEFAULT_SORT_ORDER + " LIMIT 1");
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getMostRecentMessageByName: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getMostRecentMessageByName: Exception");
            }
        }
        if (cursor == null) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getMostRecentMessageByName cursor null");
            }
            return null;
        }
        try {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getMostRecentMessageByName cursor count: " + cursor.getCount());
            }
            int columnBodyIndex = cursor.getColumnIndex(BODY);
            int columnAddressIndex = cursor.getColumnIndex(ADDRESS);
            int columnReadIndex = cursor.getColumnIndex(READ);
            int columnDateIndex = cursor.getColumnIndex(DATE);
            int columnPersonIndex = cursor.getColumnIndex(PERSON);
            if (cursor.moveToFirst()) {
                String body = cursor.getString(columnBodyIndex);
                if (!ai.saiy.android.utils.UtilsString.notNaked(body)) {
                    return null;
                }
                final Message message = new Message(cursor.getString(columnAddressIndex), body, cursor.getLong(columnDateIndex), cursor.getString(columnPersonIndex), cursor.getInt(columnReadIndex) > 0);
                if (DEBUG) {
                    MyLog.getElapsed(CLS_NAME, "getMostRecentMessageByName", then);
                }
                return message;
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getMostRecentMessageByName: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getMostRecentMessageByName: Exception");
            }
        }  finally {
            try {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getMostRecentMessageByName: IllegalStateException");
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getMostRecentMessageByName: Exception");
                }
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getMostRecentMessageByName: finally closing");
            }
        }
        return null;
    }

    public Message getMostRecentMessageByNumber(Context context, List<String> normalisedNumbers) {
        final long then = System.nanoTime();
        final String[] selectionArgs = new String[normalisedNumbers.size()];
        for (int i = 0; i < normalisedNumbers.size(); ++i) {
            selectionArgs[i] = normalisedNumbers.get(i);
        }
        final String selection = ADDRESS + " LIKE " + android.text.TextUtils.join(" OR " + ADDRESS + " LIKE ", java.util.Collections.nCopies(selectionArgs.length, "?"));
        final String[] projection = new String[]{BODY, ADDRESS, READ, DATE, PERSON};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(CONTENT_URI, projection, selection, selectionArgs, DEFAULT_SORT_ORDER + " LIMIT 1");
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getMostRecentMessageByNumber: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getMostRecentMessageByNumber: Exception");
            }
        }
        if (cursor == null) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getMostRecentMessageByNumber cursor null");
            }
            return null;
        }
        try {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "getMostRecentMessageByNumber cursor count: " + cursor.getCount());
            }
            int columnBodyIndex = cursor.getColumnIndex(BODY);
            int columnAddressIndex = cursor.getColumnIndex(ADDRESS);
            int columnReadIndex = cursor.getColumnIndex(READ);
            int columnDateIndex = cursor.getColumnIndex(DATE);
            int columnPersonIndex = cursor.getColumnIndex(PERSON);
            if (cursor.moveToFirst()) {
                String body = cursor.getString(columnBodyIndex);
                if (!ai.saiy.android.utils.UtilsString.notNaked(body)) {
                    return null;
                }
                final Message message = new Message(cursor.getString(columnAddressIndex), body, cursor.getLong(columnDateIndex), cursor.getString(columnPersonIndex), cursor.getInt(columnReadIndex) > 0);
                if (DEBUG) {
                    MyLog.getElapsed(CLS_NAME, "getMostRecentMessageByNumber", then);
                }
                return message;
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getMostRecentMessageByNumber: IllegalStateException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getMostRecentMessageByNumber: Exception");
            }
        }  finally {
            try {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getMostRecentMessageByNumber: IllegalStateException");
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getMostRecentMessageByNumber: Exception");
                }
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getMostRecentMessageByNumber: finally closing");
            }
        }
        return null;
    }
}

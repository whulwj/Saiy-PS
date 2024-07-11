package ai.saiy.android.contacts;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.Html;

import ai.saiy.android.R;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

public class UtilsContact {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = UtilsContact.class.getSimpleName();

    public static boolean displayContact(Context context, String str) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "displayContact");
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri withAppendedPath = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(str));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(withAppendedPath);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "displayContact ActivityNotFoundException");
                e.printStackTrace();
            }
            return false;
        }
    }

    public static boolean sendEmail(Context context, String receiver, String subject, String text, boolean withAttachment, String attachment) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sendEmail");
        }
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{receiver});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final String messageSignature = SPH.getMessageSignature(context);
        if (SPH.getMessageSignatureCondition(context) && UtilsString.notNaked(messageSignature)) {
            intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(text + "<br><br>" + UtilsString.replaceLast(messageSignature, Constants.SAIY, context.getString(R.string.saiy_hyperlink))));
        } else {
            intent.putExtra(Intent.EXTRA_TEXT, text);
        }
        if (withAttachment) {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + attachment));
        }
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "sendEmail ActivityNotFoundException");
                e.printStackTrace();
            }
            return false;
        }
    }

    public static boolean skypeContact(Context context, String str) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "skypeContact");
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(Installed.PACKAGE_SKYPE, Installed.PACKAGE_SKYPE + ".Main"));
        intent.setData(Uri.parse("skype:" + str + "?call&video=true"));
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "skypeContact ActivityNotFoundException");
                e.printStackTrace();
            }
            return false;
        }
    }
}

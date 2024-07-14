package ai.saiy.android.defaults.notes;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

import ai.saiy.android.R;
import ai.saiy.android.command.note.NoteValues;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

public enum NoteProvider {
    UNKNOWN,
    EVERNOTE;

    public static final String EVERNOTE_ACTION_NEW_NOTE = "com.evernote.action.CREATE_NEW_NOTE";
    public static final String EVERNOTE_ACTION_NEW_VOICE_NOTE = "com.evernote.action.NEW_VOICE_NOTE";
    public static final String EVERNOTE_EXTRA_QUICK_SEND = "QUICK_SEND";
    public static final String EVERNOTE_EXTRA_SOURCE_APP = "SOURCE_APP";
    public static final String EVERNOTE_EXTRA_TAGS = "TAG_NAME_LIST";
    public static final int NOTE_ACTION_AUTO_SEND = 3;
    public static final int NOTE_ACTION_CREATE_NOTE = 2;
    public static final int NOTE_ACTION_SELF_NOTE = 0;
    public static final int NOTE_ACTION_SEND = 1;
    public static final String NOTE_AUTO_SEND = "com.google.android.gm.action.AUTO_SEND";
    public static final String NOTE_CREATE = "com.google.android.gms.actions.CREATE_NOTE";
    public static final String NOTE_MIME = "text/plain";
    public static final String EXTRA_TEXT = "com.google.android.gms.actions.extra.TEXT";
    public static final String EXTRA_NAME = "com.google.android.gms.actions.extra.NAME";
    public static final String SELF_NOTE = "com.google.android.voicesearch.SELF_NOTE";
    public static final String VOICE_NOTE = "saiy.intent.action.VOICE_NOTE";
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = NoteProvider.class.getSimpleName();

    public static String getApplicationName(Context context, SupportedLanguage supportedLanguage, NoteProvider noteProvider) {
        if (noteProvider == NoteProvider.EVERNOTE) {
            return context.getString(R.string.evernote);
        }
        return "";
    }

    public static List<ResolveInfo> getNoteActionSendProviders(PackageManager packageManager) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_TEXT, Constants.SAIY);
        intent.putExtra(Intent.EXTRA_TITLE, Constants.SAIY);
        intent.setType(NOTE_MIME);
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    }

    public static List<ResolveInfo> getNoteAutoSendProviders(PackageManager packageManager) {
        final Intent intent = new Intent(NOTE_AUTO_SEND);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_TEXT, Constants.SAIY);
        intent.putExtra(Intent.EXTRA_TITLE, Constants.SAIY);
        intent.putExtra(EXTRA_TEXT, Constants.SAIY);
        intent.putExtra(EXTRA_NAME, Constants.SAIY);
        intent.setType(NOTE_MIME);
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    }

    public static List<ResolveInfo> getNoteCreateNoteProviders(PackageManager packageManager) {
        final Intent intent = new Intent(NOTE_CREATE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_TEXT, Constants.SAIY);
        intent.putExtra(Intent.EXTRA_TITLE, Constants.SAIY);
        intent.putExtra(EXTRA_TEXT, Constants.SAIY);
        intent.putExtra(EXTRA_NAME, Constants.SAIY);
        intent.setType(NOTE_MIME);
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    }

    public static List<ResolveInfo> getNoteSelfNoteProviders(PackageManager packageManager) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addCategory(SELF_NOTE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_TEXT, Constants.SAIY);
        intent.putExtra(Intent.EXTRA_TITLE, Constants.SAIY);
        intent.setType(NOTE_MIME);
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    }

    public static NoteProvider getProvider(String str) {
        try {
            return valueOf(str);
        } catch (IllegalArgumentException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "IllegalArgumentException");
                e.printStackTrace();
            }
            return UNKNOWN;
        }
    }

    public static boolean haveProviders(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        final ArrayList<ResolveInfo> resolveInfos = new ArrayList<>();
        resolveInfos.addAll(getNoteActionSendProviders(packageManager));
        resolveInfos.addAll(getNoteAutoSendProviders(packageManager));
        resolveInfos.addAll(getNoteCreateNoteProviders(packageManager));
        resolveInfos.addAll(getNoteSelfNoteProviders(packageManager));
        if (DEBUG) {
            MyLog.i(CLS_NAME, "haveProviders: " + resolveInfos.size());
            for (ResolveInfo resolveInfo : resolveInfos) {
                MyLog.i(CLS_NAME, "info: " + resolveInfo.activityInfo.packageName);
            }
        }
        return !resolveInfos.isEmpty();
    }

    public static boolean publishNote(Context context, NoteValues noteValues) {
        switch (SPH.getDefaultNote(context)) {
            case NOTE_ACTION_SEND:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "publishNote: NOTE_ACTION_SEND");
                }
                return publishNoteActionSend(context, noteValues);
            case NOTE_ACTION_CREATE_NOTE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "publishNote: NOTE_ACTION_CREATE_NOTE");
                }
                return publishNoteCreateNote(context, noteValues);
            case NOTE_ACTION_AUTO_SEND:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "publishNote: NOTE_ACTION_AUTO_SEND");
                }
                return publishNoteAutoSend(context, noteValues);
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "publishNote: NOTE_ACTION_SELF_NOTE");
                }
                return publishNoteSelfNote(context, noteValues);
        }
    }

    public static boolean publishNoteActionSend(Context context, NoteValues noteValues) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_TEXT, noteValues.getNoteBody());
        intent.putExtra(Intent.EXTRA_TITLE, noteValues.getNoteTitle());
        intent.setType(NOTE_MIME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final ArrayList<String> tagNameList = new ArrayList<>(1);
        tagNameList.add(context.getString(R.string.app_name_created));
        intent.putExtra(EVERNOTE_EXTRA_QUICK_SEND, true);
        intent.putExtra(EVERNOTE_EXTRA_TAGS, tagNameList);
        intent.putExtra(EVERNOTE_EXTRA_SOURCE_APP, context.getString(R.string.app_name));
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ActivityNotFoundException");
                e.printStackTrace();
            }
            return false;
        }
    }

    public static boolean publishNoteActionSendChooser(Context context, NoteValues noteValues) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_TEXT, noteValues.getNoteBody());
        intent.putExtra(Intent.EXTRA_TITLE, noteValues.getNoteTitle());
        intent.setType(NOTE_MIME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final ArrayList<String> tagNameList = new ArrayList<>(1);
        tagNameList.add(context.getString(R.string.app_name_created));
        intent.putExtra(EVERNOTE_EXTRA_QUICK_SEND, true);
        intent.putExtra(EVERNOTE_EXTRA_TAGS, tagNameList);
        intent.putExtra(EVERNOTE_EXTRA_SOURCE_APP, context.getString(R.string.app_name));
        final Intent createChooser = Intent.createChooser(intent, context.getString(R.string.test_note_prompt));
        createChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            return false;
        }
        try {
            context.startActivity(createChooser);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ActivityNotFoundException");
                e.printStackTrace();
            }
            return false;
        }
    }

    public static boolean publishNoteAutoSend(Context context, NoteValues noteValues) {
        final Intent intent = new Intent(NOTE_AUTO_SEND);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_TEXT, noteValues.getNoteBody());
        intent.putExtra(Intent.EXTRA_TITLE, noteValues.getNoteTitle());
        intent.putExtra(EXTRA_TEXT, noteValues.getNoteBody());
        intent.putExtra(EXTRA_NAME, noteValues.getNoteTitle());
        intent.setType(NOTE_MIME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final ArrayList<String> tagNameList = new ArrayList<>(1);
        tagNameList.add(context.getString(R.string.app_name_created));
        intent.putExtra(EVERNOTE_EXTRA_QUICK_SEND, true);
        intent.putExtra(EVERNOTE_EXTRA_TAGS, tagNameList);
        intent.putExtra(EVERNOTE_EXTRA_SOURCE_APP, context.getString(R.string.app_name));
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ActivityNotFoundException");
                e.printStackTrace();
            }
            return false;
        }
    }

    public static boolean publishNoteAutoSendChooser(Context context, NoteValues noteValues) {
        final Intent intent = new Intent(NOTE_AUTO_SEND);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_TEXT, noteValues.getNoteBody());
        intent.putExtra(Intent.EXTRA_TITLE, noteValues.getNoteTitle());
        intent.putExtra(EXTRA_TEXT, noteValues.getNoteBody());
        intent.putExtra(EXTRA_NAME, noteValues.getNoteTitle());
        intent.setType(NOTE_MIME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final ArrayList<String> tagNameList = new ArrayList<>(1);
        tagNameList.add(context.getString(R.string.app_name_created));
        intent.putExtra(EVERNOTE_EXTRA_QUICK_SEND, true);
        intent.putExtra(EVERNOTE_EXTRA_TAGS, tagNameList);
        intent.putExtra(EVERNOTE_EXTRA_SOURCE_APP, context.getString(R.string.app_name));
        Intent createChooser = Intent.createChooser(intent, context.getString(R.string.test_note_prompt));
        createChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            return false;
        }
        try {
            context.startActivity(createChooser);
            return true;
        } catch (ActivityNotFoundException e) {
            if (!DEBUG) {
                return false;
            }
            MyLog.w(CLS_NAME, "ActivityNotFoundException");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean publishNoteCreateNote(Context context, NoteValues noteValues) {
        final Intent intent = new Intent(NOTE_CREATE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_TEXT, noteValues.getNoteBody());
        intent.putExtra(Intent.EXTRA_TITLE, noteValues.getNoteTitle());
        intent.putExtra(EXTRA_TEXT, noteValues.getNoteBody());
        intent.putExtra(EXTRA_NAME, noteValues.getNoteTitle());
        intent.setType(NOTE_MIME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final ArrayList<String> tagNameList = new ArrayList<>(1);
        tagNameList.add(context.getString(R.string.app_name_created));
        intent.putExtra(EVERNOTE_EXTRA_QUICK_SEND, true);
        intent.putExtra(EVERNOTE_EXTRA_TAGS, tagNameList);
        intent.putExtra(EVERNOTE_EXTRA_SOURCE_APP, context.getString(R.string.app_name));
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ActivityNotFoundException");
                e.printStackTrace();
            }
            return false;
        }
    }

    public static boolean publishNoteCreateNoteChooser(Context context, NoteValues noteValues) {
        final Intent intent = new Intent(NOTE_CREATE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_TEXT, noteValues.getNoteBody());
        intent.putExtra(Intent.EXTRA_TITLE, noteValues.getNoteTitle());
        intent.putExtra(EXTRA_TEXT, noteValues.getNoteBody());
        intent.putExtra(EXTRA_NAME, noteValues.getNoteTitle());
        intent.setType(NOTE_MIME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final ArrayList<String> tagNameList = new ArrayList<>(1);
        tagNameList.add(context.getString(R.string.app_name_created));
        intent.putExtra(EVERNOTE_EXTRA_QUICK_SEND, true);
        intent.putExtra(EVERNOTE_EXTRA_TAGS, tagNameList);
        intent.putExtra(EVERNOTE_EXTRA_SOURCE_APP, context.getString(R.string.app_name));
        final Intent createChooser = Intent.createChooser(intent, context.getString(R.string.test_note_prompt));
        createChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            return false;
        }
        try {
            context.startActivity(createChooser);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ActivityNotFoundException");
                e.printStackTrace();
            }
            return false;
        }
    }

    public static boolean publishNoteSelfNote(Context context, NoteValues noteValues) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addCategory(SELF_NOTE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_TEXT, noteValues.getNoteBody());
        intent.putExtra(Intent.EXTRA_TITLE, noteValues.getNoteTitle());
        intent.setType(NOTE_MIME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final ArrayList<String> tagNameList = new ArrayList<>(1);
        tagNameList.add(context.getString(R.string.app_name_created));
        intent.putExtra(EVERNOTE_EXTRA_QUICK_SEND, true);
        intent.putExtra(EVERNOTE_EXTRA_TAGS, tagNameList);
        intent.putExtra(EVERNOTE_EXTRA_SOURCE_APP, context.getString(R.string.app_name));
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ActivityNotFoundException");
                e.printStackTrace();
            }
            return false;
        }
    }

    public static boolean publishNoteSelfNoteChooser(Context context, NoteValues noteValues) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addCategory(SELF_NOTE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_TEXT, noteValues.getNoteBody());
        intent.putExtra(Intent.EXTRA_TITLE, noteValues.getNoteTitle());
        intent.setType(NOTE_MIME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final ArrayList<String> tagNameList = new ArrayList<>(1);
        tagNameList.add(context.getString(R.string.app_name_created));
        intent.putExtra(EVERNOTE_EXTRA_QUICK_SEND, true);
        intent.putExtra(EVERNOTE_EXTRA_TAGS, tagNameList);
        intent.putExtra(EVERNOTE_EXTRA_SOURCE_APP, context.getString(R.string.app_name));
        final Intent createChooser = Intent.createChooser(intent, context.getString(R.string.test_note_prompt));
        createChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            return false;
        }
        try {
            context.startActivity(createChooser);
            return true;
        } catch (ActivityNotFoundException e) {
            if (!DEBUG) {
                return false;
            }
            MyLog.w(CLS_NAME, "ActivityNotFoundException");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean publishNoteTest(Context context, NoteValues noteValues, int i) {
        switch (i) {
            case NOTE_ACTION_SEND:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "publishNote: NOTE_ACTION_SEND");
                }
                return publishNoteActionSendChooser(context, noteValues);
            case NOTE_ACTION_CREATE_NOTE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "publishNote: NOTE_ACTION_CREATE_NOTE");
                }
                return publishNoteCreateNoteChooser(context, noteValues);
            case NOTE_ACTION_AUTO_SEND:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "publishNote: NOTE_ACTION_AUTO_SEND");
                }
                return publishNoteAutoSendChooser(context, noteValues);
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "publishNote: NOTE_ACTION_SELF_NOTE");
                }
                return publishNoteSelfNoteChooser(context, noteValues);
        }
    }
}

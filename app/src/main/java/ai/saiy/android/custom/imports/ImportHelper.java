package ai.saiy.android.custom.imports;

import android.content.ContentResolver;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import org.apache.commons.io.IOCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ai.saiy.android.custom.CustomCommandHelper;
import ai.saiy.android.custom.CustomNickname;
import ai.saiy.android.custom.CustomNicknameHelper;
import ai.saiy.android.custom.CustomPhrase;
import ai.saiy.android.custom.CustomPhraseHelper;
import ai.saiy.android.custom.CustomReplacement;
import ai.saiy.android.custom.CustomReplacementHelper;
import ai.saiy.android.custom.exports.CustomCommandExport;
import ai.saiy.android.custom.exports.CustomNicknameExport;
import ai.saiy.android.custom.exports.CustomPhraseExport;
import ai.saiy.android.custom.exports.CustomReplacementExport;
import ai.saiy.android.custom.exports.ExportConfiguration;
import ai.saiy.android.files.CachingDocumentFile;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsFile;

public final class ImportHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = ImportHelper.class.getSimpleName();
    private static final String JSON_TYPE = "application/json";

    public static @NonNull List<File> getImportFiles(@NonNull final Context ctx) {
        final ArrayList<File> importFiles = ai.saiy.android.utils.UtilsFile.getImportFiles(ctx);
        return ai.saiy.android.utils.UtilsList.notNaked(importFiles) ? ai.saiy.android.utils.UtilsFile.sortByLastModified(importFiles) : Collections.emptyList();
    }

    public static @NonNull List<CachingDocumentFile> getImportFiles(@NonNull DocumentFile[] documentFiles) {
        final ArrayList<CachingDocumentFile> importFiles = new ArrayList<>();
        String name;
        String type;
        for (DocumentFile documentFile : documentFiles) {
            if (documentFile.isDirectory()) {
                continue;
            }
            type = documentFile.getType();
            if (TextUtils.isEmpty(type)) {
                name = documentFile.getName();
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "check importFile name:" + name);
                }
                if (IOCase.INSENSITIVE.checkEndsWith(name, UtilsFile.EXPORT_FILE_SUFFIX)) {
                    importFiles.add(new CachingDocumentFile(documentFile));
                } else if (IOCase.INSENSITIVE.checkEndsWith(name, UtilsFile.OLD_EXPORT_FILE_SUFFIX)) {
                    importFiles.add(new CachingDocumentFile(documentFile));
                }
            } else {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "check importFile type:" + type);
                }
                if (TextUtils.equals(type, JSON_TYPE)) {
                    importFiles.add(new CachingDocumentFile(documentFile));
                }
            }
        }
        if (!ai.saiy.android.utils.UtilsList.notNaked(importFiles)){
            return Collections.emptyList();
        }
        importFiles.sort(new Comparator<CachingDocumentFile>() {
            /**
             * Compares the last the last modified date/time of two CachingDocumentFiles.
             *
             * @param o1 The first CachingDocumentFile to compare.
             * @param o2 The second CachingDocumentFile to compare.
             * @return a negative value if the first CachingDocumentFile's last modified date/time is less than the second, zero if the last
             *         modified date/time are the same and a positive value if the first CachingDocumentFile's last modified date/time is
             *         greater than the second CachingDocumentFile.
             */
            @Override
            public int compare(CachingDocumentFile o1, CachingDocumentFile o2) {
                final long result = o1.getLastModified() - o2.getLastModified();
                if (result < 0) {
                    return -1;
                } else if (result > 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        return importFiles;
    }

    private final Gson mGson;
    public ImportHelper() {
        this.mGson = new com.google.gson.GsonBuilder().disableHtmlEscaping().create();
    }

    private CustomNickname toCustomNickname(CustomNicknameExport customNicknameExport) {
        return new CustomNickname(customNicknameExport.getNickname(), customNicknameExport.getContactName());
    }

    private CustomPhrase toCustomPhrase(CustomPhraseExport customPhraseExport) {
        return new CustomPhrase(customPhraseExport.getKeyphrase(), customPhraseExport.getResponse(), customPhraseExport.getStartVoiceRecognition());
    }

    private CustomReplacement toCustomReplacement(CustomReplacementExport customReplacementExport) {
        return new CustomReplacement(customReplacementExport.getKeyphrase(), customReplacementExport.getReplacement());
    }

    private ai.saiy.android.custom.CustomCommand toCustomCommand(CustomCommandExport customCommandExport) {
        final ai.saiy.android.custom.CustomCommand customCommand = new ai.saiy.android.custom.CustomCommand(customCommandExport.getCustomAction(), customCommandExport.getCommandConstant(), customCommandExport.getKeyphrase(), customCommandExport.getResponseSuccess(), customCommandExport.getResponseError(), customCommandExport.getTTSLocale(), customCommandExport.getVRLocale(), customCommandExport.getAction());
        customCommand.setExtraText(customCommandExport.getExtraText());
        customCommand.setExtraText2(customCommandExport.getExtraText2());
        customCommand.setAlgorithm(customCommandExport.getAlgorithm());
        customCommand.setIntent(customCommandExport.getIntent());
        customCommand.setRegex(customCommandExport.getRegex());
        return customCommand;
    }

    private boolean exportToCustomNickname(Context context, CustomNickname customNickname) {
        return CustomNicknameHelper.setNickname(context, customNickname, null, -1L).first;
    }

    private boolean exportToCustomPhrase(Context context, CustomPhrase customPhrase) {
        return CustomPhraseHelper.setPhrase(context, customPhrase, null, -1L).first;
    }

    private boolean exportToCustomReplacement(Context context, CustomReplacement customReplacement) {
        return CustomReplacementHelper.setReplacement(context, customReplacement, null, -1L).first;
    }

    private boolean exportToCustomCommand(Context context, ai.saiy.android.custom.CustomCommand customCommand) {
        return CustomCommandHelper.setCommand(context, customCommand, -1L).first;
    }

    public int insertCommands(Context context, ArrayList<Object> objectArray) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "insertCommands: insertArray: size " + objectArray.size());
        }
        int i = 0;
        for (Object object : objectArray) {
            if (object instanceof CustomNicknameExport) {
                if (exportToCustomNickname(context, toCustomNickname((CustomNicknameExport) object))) {
                    i++;
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "insertCommands: exportToCustomNickname: failed");
                }
            } else if (object instanceof CustomReplacementExport) {
                if (exportToCustomReplacement(context, toCustomReplacement((CustomReplacementExport) object))) {
                    i++;
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "insertCommands: exportToCustomReplacement: failed");
                }
            } else if (object instanceof CustomPhraseExport) {
                if (exportToCustomPhrase(context, toCustomPhrase((CustomPhraseExport) object))) {
                    i++;
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "insertCommands: exportToCustomPhrase: failed");
                }
            } else if (object instanceof CustomCommandExport) {
                if (exportToCustomCommand(context, toCustomCommand((CustomCommandExport) object))) {
                    i++;
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "insertCommands: exportToCustomCommand: failed");
                }
            } else if (DEBUG) {
                MyLog.w(CLS_NAME, "insertCommands: instanceOf generic?");
            }
        }
        return i;
    }

    public ArrayList<Object> runImport(@NonNull List<File> files) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "runImport");
        }
        ArrayList<Object> objectArray = new ArrayList<>();
        Object object;
        for (File file : files) {
            try {
                final ExportConfiguration exportConfiguration = runImport(new BufferedReader(new FileReader(file)));
                if (exportConfiguration == null) {
                    continue;
                }
                object = runImport(exportConfiguration, new BufferedReader(new FileReader(file)));
                if (object != null) {
                    objectArray.add(object);
                }
            } catch (FileNotFoundException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "runImport: FileNotFoundException");
                    e.printStackTrace();
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "runImport: objectArray: size " + objectArray.size());
        }
        return objectArray;
    }

    public ArrayList<Object> runImport(@NonNull final Context ctx, @NonNull List<CachingDocumentFile> documentFiles) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "runImport");
        }
        final ContentResolver contentResolver = ctx.getContentResolver();
        final com.google.gson.Gson gson = new com.google.gson.GsonBuilder().disableHtmlEscaping().create();
        ArrayList<Object> objectArray = new ArrayList<>();
        Object object;
        for (CachingDocumentFile documentFile : documentFiles) {
            try {
                final ExportConfiguration exportConfiguration = runImport(new BufferedReader(new InputStreamReader(contentResolver.openInputStream(documentFile.getUri()))));
                if (exportConfiguration == null) {
                    continue;
                }
                object = runImport(exportConfiguration, new BufferedReader(new InputStreamReader(contentResolver.openInputStream(documentFile.getUri()))));
                if (object != null) {
                    objectArray.add(object);
                }
            } catch (FileNotFoundException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "runImport: FileNotFoundException");
                    e.printStackTrace();
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "runImport: objectArray: size " + objectArray.size());
        }
        return objectArray;
    }

    private @Nullable ExportConfiguration runImport(@NonNull Reader reader) {
        JsonReader jsonReader = null;
        try {
            jsonReader = new JsonReader(reader);
            final ExportConfiguration exportConfiguration = ((CustomGeneric) mGson.fromJson(jsonReader, new com.google.gson.reflect.TypeToken<CustomGeneric>() {
            }.getType())).getExportConfiguration();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "exportConfiguration: getVersion: " + exportConfiguration.getVersion());
                MyLog.i(CLS_NAME, "exportConfiguration: getTimestamp: " + exportConfiguration.getTimestamp());
                MyLog.i(CLS_NAME, "exportConfiguration: getCustom: " + exportConfiguration.getCustom().name());
            }
            return exportConfiguration;
        } catch (JsonSyntaxException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "runImport: JsonSyntaxException");
                e.printStackTrace();
            }
        } catch (JsonIOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "runImport: JsonIOException");
                e.printStackTrace();
            }
        } finally {
            try {
                reader.close();
            } catch (Throwable t) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "runImport: " + t.getClass().getSimpleName() + ", " + t.getMessage());
                }
            }
            if (jsonReader != null) {
                try {
                    jsonReader.close();
                } catch (Throwable t) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "runImport: " + t.getClass().getSimpleName() + ", " + t.getMessage());
                    }
                }
            }
        }
        return null;
    }

    private @Nullable Object runImport(@NonNull ExportConfiguration exportConfiguration, @NonNull BufferedReader reader) {
        JsonReader jsonReader = null;
        try {
            jsonReader = new JsonReader(reader);
            switch (exportConfiguration.getCustom()) {
                case CUSTOM_PHRASE:
                    return mGson.fromJson(jsonReader, new com.google.gson.reflect.TypeToken<CustomPhraseExport>() {
                    }.getType());
                case CUSTOM_NICKNAME:
                    return mGson.fromJson(jsonReader, new com.google.gson.reflect.TypeToken<CustomNicknameExport>() {
                    }.getType());
                case CUSTOM_REPLACEMENT:
                    return mGson.fromJson(jsonReader, new com.google.gson.reflect.TypeToken<CustomReplacementExport>() {
                    }.getType());
                case CUSTOM_COMMAND:
                    return mGson.fromJson(jsonReader, new com.google.gson.reflect.TypeToken<CustomCommandExport>() {
                    }.getType());
                case CUSTOM_INTRO:
                    break;
                default:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "runImport: instanceOfNone");
                    }
                    break;
            }
        } catch (JsonSyntaxException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "runImport: JsonSyntaxException");
                e.printStackTrace();
            }
        } catch (JsonIOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "runImport: JsonIOException");
                e.printStackTrace();
            }
        } finally {
            try {
                reader.close();
            } catch (Throwable t) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "runImport: " + t.getClass().getSimpleName() + ", " + t.getMessage());
                }
            }
            if (jsonReader != null) {
                try {
                    jsonReader.close();
                } catch (Throwable t) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "runImport: " + t.getClass().getSimpleName() + ", " + t.getMessage());
                    }
                }
            }
        }
        return null;
    }
}

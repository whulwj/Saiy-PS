package ai.saiy.android.custom.exports;

import android.content.Context;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ai.saiy.android.custom.Custom;
import ai.saiy.android.custom.CustomCommand;
import ai.saiy.android.custom.CustomNickname;
import ai.saiy.android.custom.CustomPhrase;
import ai.saiy.android.custom.CustomReplacement;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsFile;

public final class ExportHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ExportHelper.class.getSimpleName();
    private static final String JSON_TYPE = "application/json";

    private final Gson mGson;

    public ExportHelper(Gson mGson) {
        this.mGson = mGson;
    }

    private String buildFileName(String prefix) {
        return StringUtils.substring(prefix.trim().replaceAll("[^a-zA-Z0-9]", ""), 0, 10).trim().replaceAll(Constants.SEP_SPACE, "_") + "_" + new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date()) + UtilsFile.EXPORT_FILE_SUFFIX;
    }

    public boolean exportCustomNickname(@NonNull final Context ctx, @Nullable final DocumentFile documentFile, @NonNull CustomNickname customNickname) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "exportCustomNickname");
        }
        final CustomNicknameExport customNicknameExport = new CustomNicknameExport(customNickname.getNickname(), customNickname.getContactName());
        customNicknameExport.setExportConfiguration(new ExportConfiguration(Custom.CUSTOM_NICKNAME, ExportConfiguration.VERSION_1, System.currentTimeMillis()));
        Writer writer = null;
        ParcelFileDescriptor pfd = null;
        try {
            if (documentFile == null) {
                File exportFile = UtilsFile.createExportFile(ctx, buildFileName(customNickname.getNickname()));
                writer = new FileWriter(exportFile);
            } else {
                DocumentFile exportFile = documentFile.createFile(JSON_TYPE, buildFileName(customNickname.getNickname()));
                pfd = ctx.getContentResolver().openFileDescriptor(exportFile.getUri(), "w");;
                writer = new OutputStreamWriter(new FileOutputStream(pfd.getFileDescriptor()));
            }
            mGson.toJson(customNicknameExport, new com.google.gson.reflect.TypeToken<CustomNicknameExport>() {}.getType(), writer);
            writer.flush();
            return true;
        } catch (JsonIOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "exportCustomNickname: JsonIOException");
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "exportCustomNickname: IOException");
                e.printStackTrace();
            }
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable t) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "exportCustomNickname: " + t.getClass().getSimpleName() + ", " + t.getMessage());
                    }
                }
            }
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (Throwable t) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "exportCustomNickname: " + t.getClass().getSimpleName() + ", " + t.getMessage());
                    }
                }
            }
        }
        return false;
    }

    public boolean exportCustomPhrase(@NonNull final Context ctx, @Nullable final DocumentFile documentFile, @NonNull CustomPhrase customPhrase) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "exportCustomPhrase");
        }
        final CustomPhraseExport customPhraseExport = new CustomPhraseExport(customPhrase.getKeyphrase(), customPhrase.getResponse(), customPhrase.getStartVoiceRecognition());
        customPhraseExport.setExportConfiguration(new ExportConfiguration(Custom.CUSTOM_PHRASE, ExportConfiguration.VERSION_1, System.currentTimeMillis()));
        Writer writer = null;
        ParcelFileDescriptor pfd = null;
        try {
            if (documentFile == null) {
                File exportFile = UtilsFile.createExportFile(ctx, buildFileName(customPhrase.getKeyphrase()));
                writer = new FileWriter(exportFile);
            } else {
                DocumentFile exportFile = documentFile.createFile(JSON_TYPE, buildFileName(customPhrase.getKeyphrase()));
                pfd = ctx.getContentResolver().openFileDescriptor(exportFile.getUri(), "w");;
                writer = new OutputStreamWriter(new FileOutputStream(pfd.getFileDescriptor()));
            }
            mGson.toJson(customPhraseExport, new com.google.gson.reflect.TypeToken<CustomPhraseExport>() {
            }.getType(), writer);
            writer.flush();
            return true;
        } catch (JsonIOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "exportCustomPhrase: JsonIOException");
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "exportCustomPhrase: IOException");
                e.printStackTrace();
            }
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable t) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "exportCustomPhrase: " + t.getClass().getSimpleName() + ", " + t.getMessage());
                    }
                }
            }
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (Throwable t) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "exportCustomPhrase: " + t.getClass().getSimpleName() + ", " + t.getMessage());
                    }
                }
            }
        }
        return false;
    }

    public boolean exportCustomReplacement(@NonNull final Context ctx, @Nullable final DocumentFile documentFile, CustomReplacement customReplacement) {
        final CustomReplacementExport customReplacementExport = new CustomReplacementExport(customReplacement.getKeyphrase(), customReplacement.getReplacement());
        customReplacementExport.setExportConfiguration(new ExportConfiguration(Custom.CUSTOM_REPLACEMENT, ExportConfiguration.VERSION_1, System.currentTimeMillis()));
        Writer writer = null;
        ParcelFileDescriptor pfd = null;
        try {
            if (documentFile == null) {
                File exportFile = UtilsFile.createExportFile(ctx, buildFileName(customReplacement.getKeyphrase()));
                writer = new FileWriter(exportFile);
            } else {
                DocumentFile exportFile = documentFile.createFile(JSON_TYPE, buildFileName(customReplacement.getKeyphrase()));
                pfd = ctx.getContentResolver().openFileDescriptor(exportFile.getUri(), "w");
                writer = new OutputStreamWriter(new FileOutputStream(pfd.getFileDescriptor()));
            }
            mGson.toJson(customReplacementExport, new com.google.gson.reflect.TypeToken<CustomReplacementExport>() {
            }.getType(), writer);
            writer.flush();
            return true;
        } catch (JsonIOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "exportCustomReplacement: JsonIOException");
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "exportCustomReplacement: IOException");
                e.printStackTrace();
            }
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable t) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "exportCustomReplacement: " + t.getClass().getSimpleName() + ", " + t.getMessage());
                    }
                }
            }
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (Throwable t) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "exportCustomReplacement: " + t.getClass().getSimpleName() + ", " + t.getMessage());
                    }
                }
            }
        }
        return false;
    }

    public boolean exportCustomCommand(@NonNull final Context ctx, @Nullable final DocumentFile documentFile, @NonNull CustomCommand customCommand) {
        final CustomCommandExport customCommandExport = new CustomCommandExport(customCommand.getCustomAction(), customCommand.getCommandConstant(), customCommand.getKeyphrase(), customCommand.getResponseSuccess(), customCommand.getResponseError(), customCommand.getTTSLocale(), customCommand.getVRLocale(), customCommand.getAction());
        customCommandExport.setAlgorithm(customCommand.getAlgorithm());
        customCommandExport.setExtraText(customCommand.getExtraText());
        customCommandExport.setExtraText2(customCommand.getExtraText2());
        customCommandExport.setRegex(customCommand.getRegex());
        customCommandExport.setIntent(customCommand.getIntent());
        customCommandExport.setRegularExpression(customCommand.getRegularExpression());
        customCommandExport.setExportConfiguration(new ExportConfiguration(Custom.CUSTOM_COMMAND, ExportConfiguration.VERSION_1, System.currentTimeMillis()));
        Writer writer = null;
        ParcelFileDescriptor pfd = null;
        try {
            if (documentFile == null) {
                File exportFile = UtilsFile.createExportFile(ctx, buildFileName(customCommand.getKeyphrase()));
                writer = new FileWriter(exportFile);
            } else {
                DocumentFile exportFile = documentFile.createFile(JSON_TYPE, buildFileName(customCommand.getKeyphrase()));
                pfd = ctx.getContentResolver().openFileDescriptor(exportFile.getUri(), "w");
                writer = new OutputStreamWriter(new FileOutputStream(pfd.getFileDescriptor()));
            }
            mGson.toJson(customCommandExport, new com.google.gson.reflect.TypeToken<CustomCommandExport>() {
            }.getType(), writer);
            writer.flush();
            return true;
        } catch (JsonIOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "exportCustomCommand: JsonIOException");
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "exportCustomCommand: IOException");
                e.printStackTrace();
            }
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable t) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "exportCustomCommand: " + t.getClass().getSimpleName() + ", " + t.getMessage());
                    }
                }
            }
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (Throwable t) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "exportCustomCommand: " + t.getClass().getSimpleName() + ", " + t.getMessage());
                    }
                }
            }
        }
        return false;
    }

    public static boolean exportProceed(Context context) {
        return UtilsFile.createDirs(context);
    }
}

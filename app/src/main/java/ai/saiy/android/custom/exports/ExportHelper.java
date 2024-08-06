package ai.saiy.android.custom.exports;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ai.saiy.android.custom.Custom;
import ai.saiy.android.custom.CustomCommand;
import ai.saiy.android.custom.CustomNickname;
import ai.saiy.android.custom.CustomPhrase;
import ai.saiy.android.custom.CustomReplacement;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsFile;

public class ExportHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ExportHelper.class.getSimpleName();

    public boolean exportCustomNickname(@NonNull CustomNickname customNickname) {
        CustomNicknameExport customNicknameExport = new CustomNicknameExport(customNickname.getNickname(), customNickname.getContactName());
        customNicknameExport.setExportConfiguration(new ExportConfiguration(Custom.CUSTOM_NICKNAME, ExportConfiguration.VERSION_1, System.currentTimeMillis()));
        File exportFile = UtilsFile.createExportFile(StringUtils.substring(customNickname.getNickname().trim().replaceAll("[^a-zA-Z0-9]", ""), 0, 10).trim().replaceAll(XMLResultsHandler.SEP_SPACE, "_") + "_" + new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date()) + "." + UtilsFile.EXPORT_FILE_SUFFIX);
        Gson gson = new com.google.gson.GsonBuilder().disableHtmlEscaping().create();
        try {
            FileWriter fileWriter = new FileWriter(exportFile);
            gson.toJson(customNicknameExport, new com.google.gson.reflect.TypeToken<CustomNicknameExport>() {}.getType(), fileWriter);
            fileWriter.flush();
            fileWriter.close();
            return true;
        } catch (JsonIOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "exportCustomNickname: JsonIOException");
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "exportCustomReplacement: IOException");
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean exportCustomPhrase(@NonNull CustomPhrase customPhrase) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "exportCustomPhrase");
        }
        CustomPhraseExport customPhraseExport = new CustomPhraseExport(customPhrase.getKeyphrase(), customPhrase.getResponse(), customPhrase.getStartVoiceRecognition());
        customPhraseExport.setExportConfiguration(new ExportConfiguration(Custom.CUSTOM_PHRASE, ExportConfiguration.VERSION_1, System.currentTimeMillis()));
        File exportFile = UtilsFile.createExportFile(StringUtils.substring(customPhrase.getKeyphrase().trim().replaceAll("[^a-zA-Z0-9]", ""), 0, 10).trim().replaceAll(XMLResultsHandler.SEP_SPACE, "_") + "_" + new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date()) + "." + UtilsFile.EXPORT_FILE_SUFFIX);
        Gson gson = new com.google.gson.GsonBuilder().disableHtmlEscaping().create();
        try {
            FileWriter fileWriter = new FileWriter(exportFile);
            gson.toJson(customPhraseExport, new com.google.gson.reflect.TypeToken<CustomPhraseExport>() {
            }.getType(), fileWriter);
            fileWriter.flush();
            fileWriter.close();
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
        }
        return false;
    }

    public boolean exportCustomReplacement(CustomReplacement customReplacement) {
        CustomReplacementExport customReplacementExport = new CustomReplacementExport(customReplacement.getKeyphrase(), customReplacement.getReplacement());
        customReplacementExport.setExportConfiguration(new ExportConfiguration(Custom.CUSTOM_REPLACEMENT, ExportConfiguration.VERSION_1, System.currentTimeMillis()));
        File exportFile = UtilsFile.createExportFile(StringUtils.substring(customReplacement.getKeyphrase().trim().replaceAll("[^a-zA-Z0-9]", ""), 0, 10).trim().replaceAll(XMLResultsHandler.SEP_SPACE, "_") + "_" + new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date()) + "." + UtilsFile.EXPORT_FILE_SUFFIX);
        Gson gson = new com.google.gson.GsonBuilder().disableHtmlEscaping().create();
        try {
            FileWriter fileWriter = new FileWriter(exportFile);
            gson.toJson(customReplacementExport, new com.google.gson.reflect.TypeToken<CustomReplacementExport>() {
            }.getType(), fileWriter);
            fileWriter.flush();
            fileWriter.close();
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
        }
        return false;
    }

    public boolean exportCustomCommand(@NonNull CustomCommand customCommand) {
        CustomCommandExport customCommandExport = new CustomCommandExport(customCommand.getCustomAction(), customCommand.getCommandConstant(), customCommand.getKeyphrase(), customCommand.getResponseSuccess(), customCommand.getResponseError(), customCommand.getTTSLocale(), customCommand.getVRLocale(), customCommand.getAction());
        customCommandExport.setAlgorithm(customCommand.getAlgorithm());
        customCommandExport.setExtraText(customCommand.getExtraText());
        customCommandExport.setExtraText2(customCommand.getExtraText2());
        customCommandExport.setRegex(customCommand.getRegex());
        customCommandExport.setIntent(customCommand.getIntent());
        customCommandExport.setRegularExpression(customCommand.getRegularExpression());
        customCommandExport.setExportConfiguration(new ExportConfiguration(Custom.CUSTOM_COMMAND, ExportConfiguration.VERSION_1, System.currentTimeMillis()));
        File exportFile = UtilsFile.createExportFile(StringUtils.substring(customCommand.getKeyphrase().trim().replaceAll("[^a-zA-Z0-9]", ""), 0, 10).trim().replaceAll(XMLResultsHandler.SEP_SPACE, "_") + "_" + new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date()) + "." + UtilsFile.EXPORT_FILE_SUFFIX);
        Gson gson = new com.google.gson.GsonBuilder().disableHtmlEscaping().create();
        try {
            FileWriter fileWriter = new FileWriter(exportFile);
            gson.toJson(customCommandExport, new com.google.gson.reflect.TypeToken<CustomCommandExport>() {
            }.getType(), fileWriter);
            fileWriter.flush();
            fileWriter.close();
            return true;
        } catch (JsonIOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "exportCustomCommand: JsonIOException");
                e.printStackTrace();
            }
        } catch (IOException e2) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "exportCustomCommand: IOException");
                e2.printStackTrace();
            }
        }
        return false;
    }

    public boolean exportProceed(Context context) {
        return UtilsFile.createDirs(context);
    }
}

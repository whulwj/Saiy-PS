package ai.saiy.android.custom.imports;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
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
import ai.saiy.android.utils.MyLog;

public class ImportHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ImportHelper.class.getSimpleName();

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
        customCommand.setSerialised(customCommandExport.getSerialised());
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

    public @NonNull List<File> getImportFiles() {
        final ArrayList<File> importFiles = ai.saiy.android.utils.UtilsFile.getImportFiles();
        return ai.saiy.android.utils.UtilsList.notNaked(importFiles) ? ai.saiy.android.utils.UtilsFile.sortByLastModified(importFiles) : Collections.emptyList();
    }

    public ArrayList<Object> runImport(@NonNull List<File> files) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "runImport");
        }
        final com.google.gson.Gson gson = new com.google.gson.GsonBuilder().disableHtmlEscaping().create();
        ArrayList<Object> objectArray = new ArrayList<>();
        FileReader fileReader = null;
        JsonReader jsonReader = null;
        for (File file : files) {
            try {
                fileReader = new FileReader(file);
                jsonReader = new JsonReader(fileReader);

                ExportConfiguration exportConfiguration = ((CustomGeneric) gson.fromJson(jsonReader, new com.google.gson.reflect.TypeToken<CustomGeneric>() {
                }.getType())).getExportConfiguration();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "exportConfiguration: getVersion: " + exportConfiguration.getVersion());
                    MyLog.i(CLS_NAME, "exportConfiguration: getTimestamp: " + exportConfiguration.getTimestamp());
                    MyLog.i(CLS_NAME, "exportConfiguration: getCustom: " + exportConfiguration.getCustom().name());
                }
                switch (exportConfiguration.getCustom()) {
                    case CUSTOM_PHRASE:
                        objectArray.add(gson.fromJson(jsonReader, new com.google.gson.reflect.TypeToken<CustomPhraseExport>() {
                        }.getType()));
                        break;
                    case CUSTOM_NICKNAME:
                        objectArray.add(gson.fromJson(jsonReader, new com.google.gson.reflect.TypeToken<CustomNicknameExport>() {
                        }.getType()));
                        break;
                    case CUSTOM_REPLACEMENT:
                        objectArray.add(gson.fromJson(jsonReader, new com.google.gson.reflect.TypeToken<CustomReplacementExport>() {
                        }.getType()));
                        break;
                    case CUSTOM_COMMAND:
                        objectArray.add(gson.fromJson(jsonReader, new com.google.gson.reflect.TypeToken<CustomCommandExport>() {
                        }.getType()));
                        break;
                    case CUSTOM_INTRO:
                        break;
                    default:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "runImport: instanceOfNone");
                        }
                        break;
                }
            } catch (FileNotFoundException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "runImport: FileNotFoundException");
                    e.printStackTrace();
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
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (Throwable t) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "runImport: " + t.getClass().getSimpleName() + ", " + t.getMessage());
                        }
                    }
                    fileReader = null;
                }
                if (jsonReader != null) {
                    try {
                        jsonReader.close();
                    } catch (Throwable t) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "runImport: " + t.getClass().getSimpleName() + ", " + t.getMessage());
                        }
                    }
                    jsonReader = null;
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "runImport: objectArray: size " + objectArray.size());
        }
        return objectArray;
    }
}

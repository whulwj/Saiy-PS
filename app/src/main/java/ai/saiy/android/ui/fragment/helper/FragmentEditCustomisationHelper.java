package ai.saiy.android.ui.fragment.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Pair;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import ai.saiy.android.R;
import ai.saiy.android.api.request.Regex;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.command.http.CustomHttp;
import ai.saiy.android.command.intent.CustomIntent;
import ai.saiy.android.custom.CCC;
import ai.saiy.android.custom.CustomCommand;
import ai.saiy.android.custom.CustomNickname;
import ai.saiy.android.custom.CustomPhrase;
import ai.saiy.android.custom.CustomReplacement;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.UIEditCustomisationAdapter;
import ai.saiy.android.ui.containers.ContainerCustomisation;
import ai.saiy.android.ui.fragment.FragmentEditCustomisation;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

public class FragmentEditCustomisationHelper {
    private final FragmentEditCustomisation parentFragment;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentEditCustomisationHelper.class.getSimpleName();

    private final InputFilter inputFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dStart, int dEnd) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, source + " ~ " + start + " ~ " + end + " ~ " + dest + " ~ " + dStart + " ~ " + dEnd);
            }
            if (source == null || end < 1) {
                return null;
            }
            return !org.apache.commons.lang3.ArrayUtils.contains(getApplicationContext().getString(R.string.input_digits).toCharArray(), source.charAt(end - 1)) ? "" : source;
        }
    };

    public FragmentEditCustomisationHelper(FragmentEditCustomisation parentFragment) {
        this.parentFragment = parentFragment;
    }

    private String getString(@StringRes int resId) {
        return getApplicationContext().getString(resId);
    }

    private void deleteCustomNickname(final int index, final long rowId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ai.saiy.android.custom.CustomNicknameHelper.deleteCustomNickname(getApplicationContext(), rowId);
                getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getParent().getObjects().remove(index);
                        getParent().getAdapter().notifyItemRemoved(index);
                        if (getParent().getObjects().isEmpty()) {
                            getParentActivity().onBackPressed();
                        }
                    }
                });
            }
        });
    }

    private void setNickname(final CustomNickname customNickname, final int index, final long rowId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final Pair<Boolean, Long> duplicatePair = ai.saiy.android.custom.CustomNicknameHelper.setNickname(getApplicationContext(), customNickname, null, rowId);
                if (duplicatePair.first) {
                    final String gsonString = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().toJson(customNickname);
                    customNickname.setSerialised(gsonString);
                    getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final ContainerCustomisation containerCustomisation = getParent().getObjects().get(index);
                            containerCustomisation.setSerialised(gsonString);
                            containerCustomisation.setRowId(duplicatePair.second);
                            containerCustomisation.setTitle(customNickname.getNickname());
                            containerCustomisation.setSubtitle(customNickname.getContactName());
                            getParent().getAdapter().notifyItemChanged(index);
                        }
                    });
                }
            }
        });
    }

    private void setPhrase(final CustomPhrase customPhrase, final int index, final long rowId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final Pair<Boolean, Long> duplicatePair = ai.saiy.android.custom.CustomPhraseHelper.setPhrase(getApplicationContext(), customPhrase, null, rowId);
                if (duplicatePair.first) {
                    final String b2 = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().toJson(customPhrase);
                    customPhrase.setSerialised(b2);
                    getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final ContainerCustomisation containerCustomisation = getParent().getObjects().get(index);
                            containerCustomisation.setSerialised(b2);
                            containerCustomisation.setRowId(duplicatePair.second);
                            containerCustomisation.setTitle(customPhrase.getKeyphrase());
                            containerCustomisation.setSubtitle(customPhrase.getResponse());
                            getParent().getAdapter().notifyItemChanged(index);
                        }
                    });
                }
            }
        });
    }

    private void setReplacement(final CustomReplacement customReplacement, final int index, final long rowId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final Pair<Boolean, Long> duplicatePair = ai.saiy.android.custom.CustomReplacementHelper.setReplacement(getApplicationContext(), customReplacement, null, rowId);
                if (duplicatePair.first) {
                    final String gsonString = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().toJson(customReplacement);
                    customReplacement.setSerialised(gsonString);
                    getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final ContainerCustomisation containerCustomisation = getParent().getObjects().get(index);
                            containerCustomisation.setSerialised(gsonString);
                            containerCustomisation.setRowId(duplicatePair.second);
                            containerCustomisation.setTitle(customReplacement.getKeyphrase());
                            containerCustomisation.setSubtitle(customReplacement.getReplacement());
                            getParent().getAdapter().notifyItemChanged(index);
                        }
                    });
                }
            }
        });
    }

    private void setCommand(final ai.saiy.android.custom.CustomCommand bVar, final int index, final long rowId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final Pair<Boolean, Long> duplicatePair = ai.saiy.android.custom.CustomCommandHelper.setCommand(getApplicationContext(), bVar, rowId);
                if (duplicatePair.first) {
                    final String gsonString = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().toJson(bVar);
                    bVar.setSerialised(gsonString);
                    getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final ContainerCustomisation containerCustomisation = getParent().getObjects().get(index);
                            containerCustomisation.setSerialised(gsonString);
                            containerCustomisation.setRowId(duplicatePair.second);
                            containerCustomisation.setTitle(bVar.getKeyphrase());
                            getParent().getAdapter().notifyItemChanged(index);
                        }
                    });
                }
            }
        });
    }

    private void deleteCustomReplacement(final int index, final long rowId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ai.saiy.android.custom.CustomReplacementHelper.deleteCustomReplacement(getApplicationContext(), rowId);
                getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getParent().getObjects().remove(index);
                        getParent().getAdapter().notifyItemRemoved(index);
                        if (getParent().getObjects().isEmpty()) {
                            getParentActivity().onBackPressed();
                        }
                    }
                });
            }
        });
    }

    private void deleteCustomPhrase(final int index, final long rowId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ai.saiy.android.custom.CustomPhraseHelper.deleteCustomPhrase(getApplicationContext(), rowId);
                getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getParent().getObjects().remove(index);
                        getParent().getAdapter().notifyItemRemoved(index);
                        if (getParent().getObjects().isEmpty()) {
                            getParentActivity().onBackPressed();
                        }
                    }
                });
            }
        });
    }

    private Context getApplicationContext() {
        return parentFragment.getApplicationContext();
    }

    private void deleteCustomCommand(final int index, final long rowId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ai.saiy.android.custom.CustomCommandHelper.deleteCustomCommand(getApplicationContext(), rowId);
                getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getParent().getObjects().remove(index);
                        getParent().getAdapter().notifyItemRemoved(index);
                        if (getParent().getObjects().isEmpty()) {
                            getParentActivity().onBackPressed();
                        }
                    }
                });
            }
        });
    }

    public UIEditCustomisationAdapter getAdapter(ArrayList<ContainerCustomisation> objects) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAdapter");
        }
        return new UIEditCustomisationAdapter(objects, getParent(), getParent());
    }

    public RecyclerView getRecyclerView(View view) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getRecyclerView");
        }
        final RecyclerView recyclerView = view.findViewById(R.id.layout_common_fragment_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        recyclerView.addItemDecoration(new ai.saiy.android.ui.components.DividerItemDecoration(getParentActivity(), null));
        return recyclerView;
    }

    public void finaliseUI() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (getParent().isActive()) {
                    if (getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)) {
                        try {
                            Thread.sleep(200L);
                        } catch (InterruptedException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "finaliseUI InterruptedException");
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "finaliseUI Fragment detached");
                }
                if (getParent().isActive()) {
                    getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getParent().getObjects() == null) {
                                toast(getString(R.string.error_edit_customisations), Toast.LENGTH_LONG);
                            } else {
                                final List<ContainerCustomisation> objects = getParent().getObjectsFromArguments();
                                if (getParent().getObjects() == null || objects == null) {
                                    toast(getString(R.string.error_edit_customisations), Toast.LENGTH_LONG);
                                } else {
                                    getParent().getObjects().addAll(objects);
                                    getParent().getAdapter().notifyItemRangeInserted(0, getParent().getObjects().size());
                                }
                            }
                            showProgress(false);
                        }
                    });
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "finaliseUI Fragment detached");
                }
            }
        });
    }

    public void showCustomHttpDialog(CustomCommand customCommand, final CustomHttp customHttp, final int index, final long rowId) {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setCancelable(false)
                .setView(R.layout.custom_http_dialog_layout)
                .setTitle(R.string.menu_custom_http)
                .setIcon(R.drawable.ic_language)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomHttpDialog: onPositive");
                        }
                        if (dialog instanceof AlertDialog) {
                            EditText editTextUrl = ((AlertDialog) dialog).getWindow().findViewById(R.id.etUrl);
                            if (editTextUrl.getText() == null) {
                                dialog.dismiss();
                                return;
                            }
                            final String url = editTextUrl.getText().toString().trim();
                            if (!ai.saiy.android.utils.UtilsString.notNaked(url)) {
                                toast(getString(R.string.custom_url_naked_error), Toast.LENGTH_SHORT);
                                return;
                            }
                            customHttp.setUrlString(url);
                            customHttp.setHttps(((CheckBox) (((AlertDialog) dialog).getWindow()).findViewById(R.id.cbHttps)).isChecked());
                            final int checkedId = ((RadioGroup) (((AlertDialog) dialog).getWindow()).findViewById(R.id.rgHttpType)).getCheckedRadioButtonId();
                            if (R.id.rbDelete == checkedId) {
                                customHttp.setType(CustomHttp.TYPE_DELETE);
                            } else if (R.id.rbGet == checkedId) {
                                customHttp.setType(ai.saiy.android.command.http.CustomHttp.TYPE_GET);
                            } else if (R.id.rbPost == checkedId) {
                                customHttp.setType(ai.saiy.android.command.http.CustomHttp.TYPE_POST);
                            } else if (R.id.rbPut == checkedId) {
                                customHttp.setType(ai.saiy.android.command.http.CustomHttp.TYPE_PUT);
                            }
                            EditText editTextPhrase = (((AlertDialog) dialog).getWindow()).findViewById(R.id.etKeyphrase);
                            if (editTextPhrase.getText() == null) {
                                dialog.dismiss();
                                return;
                            }
                            final String phrase = editTextPhrase.getText().toString().trim();
                            if (!ai.saiy.android.utils.UtilsString.notNaked(phrase)) {
                                toast(getString(R.string.custom_naked_error), Toast.LENGTH_SHORT);
                                return;
                            } else if (!ai.saiy.android.utils.UtilsString.regexCheck(phrase)) {
                                toast(getString(R.string.custom_format_error), Toast.LENGTH_SHORT);
                                return;
                            }

                            String successResponse;
                            String errorResponse;
                            if (!((CheckBox) (((AlertDialog) dialog).getWindow()).findViewById(R.id.cbUseHttpOutputSuccess)).isChecked()) {
                                EditText editTextSuccessResponse = (((AlertDialog) dialog).getWindow()).findViewById(R.id.etSuccessResponse);
                                successResponse = editTextSuccessResponse.getText() != null ? editTextSuccessResponse.getText().toString().trim() : "";
                                if (((CheckBox) (((AlertDialog) dialog).getWindow()).findViewById(R.id.cbVoiceRecognitionSuccess)).isChecked()) {
                                    customHttp.setSuccessHandling(CustomHttp.SUCCESS_SPEAK_LISTEN);
                                } else {
                                    customHttp.setSuccessHandling(CustomHttp.SUCCESS_SPEAK);
                                }
                            } else if (((CheckBox) (((AlertDialog) dialog).getWindow()).findViewById(R.id.cbVoiceRecognitionSuccess)).isChecked()) {
                                customHttp.setSuccessHandling(CustomHttp.SUCCESS_SPEAK_LISTEN_OUTPUT);
                                successResponse = "";
                            } else {
                                customHttp.setSuccessHandling(CustomHttp.SUCCESS_SPEAK_OUTPUT);
                                successResponse = "";
                            }
                            if (!((CheckBox) (((AlertDialog) dialog).getWindow()).findViewById(R.id.cbUseHttpOutputError)).isChecked()) {
                                EditText editTextErrorResponse = (((AlertDialog) dialog).getWindow()).findViewById(R.id.etErrorResponse);
                                errorResponse = editTextErrorResponse.getText() != null ? editTextErrorResponse.getText().toString().trim() : "";
                                if (((CheckBox) (((AlertDialog) dialog).getWindow()).findViewById(R.id.cbVoiceRecognitionError)).isChecked()) {
                                    customHttp.setErrorHandling(CustomHttp.ERROR_SPEAK_LISTEN);
                                } else {
                                    customHttp.setErrorHandling(CustomHttp.ERROR_SPEAK);
                                }
                            } else if (((CheckBox) (((AlertDialog) dialog).getWindow()).findViewById(R.id.cbVoiceRecognitionError)).isChecked()) {
                                customHttp.setErrorHandling(CustomHttp.ERROR_SPEAK_LISTEN_OUTPUT);
                                errorResponse = "";
                            } else {
                                customHttp.setErrorHandling(CustomHttp.ERROR_SPEAK_OUTPUT);
                                errorResponse = "";
                            }
                            customHttp.setTasker(((CheckBox) (((AlertDialog) dialog).getWindow()).findViewById(R.id.cbOutputTasker)).isChecked());
                            if (customHttp.getSuccessHandling() == CustomHttp.SUCCESS_SPEAK_OUTPUT || customHttp.getSuccessHandling() == CustomHttp.SUCCESS_SPEAK_LISTEN_OUTPUT || customHttp.getErrorHandling() == CustomHttp.ERROR_SPEAK_OUTPUT || customHttp.getErrorHandling() == CustomHttp.ERROR_SPEAK_LISTEN_OUTPUT || customHttp.isTasker()) {
                                customHttp.setOutputType(CustomHttp.OUTPUT_TYPE_STRING);
                            }
                            if (!customHttp.isTasker()) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showCustomHttpDialog: cbSaveOutput: false");
                                }
                                dialog.dismiss();
                                final CustomCommand cc = new CustomCommand(CCC.CUSTOM_HTTP, CC.COMMAND_USER_CUSTOM, phrase, successResponse, errorResponse, SPH.getTTSLocale(getApplicationContext()).toString(), SPH.getVRLocale(getApplicationContext()).toString(), ((CheckBox) (((AlertDialog) dialog).getWindow()).findViewById(R.id.cbVoiceRecognitionSuccess)).isChecked() ? LocalRequest.ACTION_SPEAK_LISTEN : LocalRequest.ACTION_SPEAK_ONLY);
                                final String gsonString = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().toJson(customHttp);
                                customHttp.setSerialised(gsonString);
                                cc.setExtraText(gsonString);
                                setCommand(cc, index, rowId);
                                return;
                            }

                            EditText editTextTaskerName = (((AlertDialog) dialog).getWindow()).findViewById(R.id.etTaskName);
                            if (editTextTaskerName.getText() != null) {
                                String taskerName = editTextTaskerName.getText().toString().trim();
                                if (ai.saiy.android.utils.UtilsString.notNaked(taskerName)) {
                                    final ArrayList<ai.saiy.android.thirdparty.tasker.TaskerTask> taskerTasks = new ai.saiy.android.thirdparty.tasker.TaskerHelper().getTasks(getApplicationContext());
                                    if (ai.saiy.android.utils.UtilsList.notNaked(taskerTasks)) {
                                        boolean matchWithATask = false;
                                        for (ai.saiy.android.thirdparty.tasker.TaskerTask taskerTask : taskerTasks) {
                                            if (taskerTask.getTaskName().matches(taskerName)) {
                                                matchWithATask = true;
                                                break;
                                            }
                                        }
                                        if (matchWithATask) {
                                            customHttp.setTaskerTaskName(taskerName);
                                        } else {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "showCustomHttpDialog: onSave: task not matched");
                                            }
                                            toast(getString(R.string.menu_tasker_task_not_found), Toast.LENGTH_SHORT);
                                            editTextTaskerName.requestFocus();
                                        }
                                    } else {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showCustomHttpDialog: onSave: no tasks found");
                                        }
                                        toast(getString(R.string.menu_tasker_task_not_found), Toast.LENGTH_SHORT);
                                        editTextTaskerName.requestFocus();
                                    }
                                } else {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "showCustomHttpDialog: onSave: task naked");
                                    }
                                    toast(getString(R.string.menu_tasker_task_not_found), Toast.LENGTH_SHORT);
                                    editTextTaskerName.requestFocus();
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showCustomHttpDialog: onSave: etTaskName null");
                                }
                                toast(getString(R.string.menu_tasker_task_not_found), Toast.LENGTH_SHORT);
                                editTextTaskerName.requestFocus();
                            }
                            EditText editTextVariableName = (((AlertDialog) dialog).getWindow()).findViewById(R.id.etVariableName);
                            if (editTextVariableName.getText() == null) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showCustomHttpDialog: onSave: etVariableName null");
                                }
                                toast(getString(R.string.menu_tasker_variable_not_found), Toast.LENGTH_SHORT);
                                editTextTaskerName.requestFocus();
                                return;
                            }
                            String variableName = editTextVariableName.getText().toString().trim();
                            if (!ai.saiy.android.utils.UtilsString.notNaked(variableName)) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showCustomHttpDialog: onSave: variable naked");
                                }
                                toast(getString(R.string.menu_tasker_variable_not_found), Toast.LENGTH_SHORT);
                                editTextTaskerName.requestFocus();
                                return;
                            }
                            String variable = !variableName.startsWith("%") ? "%" + variableName : variableName;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                customHttp.setTaskerVariableName(variable.toLowerCase(Locale.getDefault(Locale.Category.DISPLAY)));
                            } else {
                                customHttp.setTaskerVariableName(variable.toLowerCase(Locale.getDefault()));
                            }
                            dialog.dismiss();
                            final CustomCommand cc = new CustomCommand(CCC.CUSTOM_HTTP, CC.COMMAND_USER_CUSTOM, phrase, successResponse, errorResponse, SPH.getTTSLocale(getApplicationContext()).toString(), SPH.getVRLocale(getApplicationContext()).toString(), ((CheckBox) ((AlertDialog) dialog).getWindow().findViewById(R.id.cbVoiceRecognitionSuccess)).isChecked() ? LocalRequest.ACTION_SPEAK_LISTEN : LocalRequest.ACTION_SPEAK_ONLY);
                            final String gsonString = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().toJson(customHttp);
                            customHttp.setSerialised(gsonString);
                            cc.setExtraText(gsonString);
                            setCommand(cc, index, rowId);
                        }
                    }
                })
                .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomHttpDialog: onNeutral");
                        }
                        dialog.dismiss();
                        deleteCustomCommand(index, rowId);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomHttpDialog: onNegative");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomHttpDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();

        ((EditText) materialDialog.getWindow().findViewById(R.id.etUrl)).setText(customHttp.getUrlString());
        ((CheckBox) materialDialog.getWindow().findViewById(R.id.cbHttps)).setChecked(customHttp.isHttps());
        switch (customHttp.getType()) {
            case CustomHttp.TYPE_POST:
                ((RadioButton) materialDialog.getWindow().findViewById(R.id.rbPost)).setChecked(true);
                break;
            case CustomHttp.TYPE_GET:
                ((RadioButton) materialDialog.getWindow().findViewById(R.id.rbGet)).setChecked(true);
                break;
            case CustomHttp.TYPE_PUT:
                ((RadioButton) materialDialog.getWindow().findViewById(R.id.rbPut)).setChecked(true);
                break;
            case CustomHttp.TYPE_DELETE:
                ((RadioButton) materialDialog.getWindow().findViewById(R.id.rbDelete)).setChecked(true);
                break;
        }
        ((EditText) materialDialog.getWindow().findViewById(R.id.etKeyphrase)).setText(customCommand.getKeyphrase());
        ((EditText) materialDialog.getWindow().findViewById(R.id.etSuccessResponse)).setText(customCommand.getResponseSuccess());
        CheckBox checkBox = materialDialog.getWindow().findViewById(R.id.cbUseHttpOutputSuccess);
        checkBox.setChecked(customHttp.getSuccessHandling() == CustomHttp.SUCCESS_SPEAK_OUTPUT || customHttp.getSuccessHandling() == CustomHttp.SUCCESS_SPEAK_LISTEN_OUTPUT);
        ((CheckBox) materialDialog.getWindow().findViewById(R.id.cbVoiceRecognitionSuccess)).setChecked(customHttp.getSuccessHandling() == CustomHttp.SUCCESS_SPEAK_LISTEN || customHttp.getSuccessHandling() == CustomHttp.SUCCESS_SPEAK_LISTEN_OUTPUT);
        ((EditText) materialDialog.getWindow().findViewById(R.id.etErrorResponse)).setText(customCommand.getResponseError());
        checkBox = materialDialog.getWindow().findViewById(R.id.cbUseHttpOutputError);
        checkBox.setChecked(customHttp.getErrorHandling() == CustomHttp.ERROR_SPEAK_OUTPUT || customHttp.getErrorHandling() == CustomHttp.ERROR_SPEAK_LISTEN_OUTPUT);
        ((CheckBox) materialDialog.getWindow().findViewById(R.id.cbVoiceRecognitionError)).setChecked(customHttp.getErrorHandling() == CustomHttp.ERROR_SPEAK_LISTEN || customHttp.getErrorHandling() == CustomHttp.ERROR_SPEAK_LISTEN_OUTPUT);
        checkBox = materialDialog.getWindow().findViewById(R.id.cbOutputTasker);
        checkBox.setEnabled(new ai.saiy.android.thirdparty.tasker.TaskerHelper().isTaskerInstalled(getApplicationContext()).first);
        if (checkBox.isEnabled()) {
            checkBox.setChecked(customHttp.isTasker());
        }
        if (checkBox.isChecked()) {
            ((EditText) materialDialog.getWindow().findViewById(R.id.etVariableName)).setText(customHttp.getTaskerVariableName());
            ((EditText) materialDialog.getWindow().findViewById(R.id.etTaskName)).setText(customHttp.getTaskerTaskName());
        }
        final LinearLayout linearLayout = materialDialog.getWindow().findViewById(R.id.linOutputTasker);
        linearLayout.setVisibility(checkBox.isChecked() ? View.VISIBLE : View.GONE);
        ((CheckBox) materialDialog.getWindow().findViewById(R.id.cbOutputTasker)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "rbOutputTasker: onCheckedChanged: " + isChecked);
                }
                if (!isChecked) {
                    linearLayout.setVisibility(View.GONE);
                    return;
                }
                linearLayout.setVisibility(View.VISIBLE);
                EditText editTextVariableName = materialDialog.getWindow().findViewById(R.id.etVariableName);
                editTextVariableName.requestFocus();
                if (editTextVariableName.getText() == null) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "etVariableName: null");
                    }
                    return;
                }
                String variableName = editTextVariableName.getText().toString().trim();
                if (!ai.saiy.android.utils.UtilsString.notNaked(variableName)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "etVariableName: naked adding prefix");
                    }
                    editTextVariableName.setText("%");
                    editTextVariableName.setSelection(1);
                    return;
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "etVariableName: not naked");
                }
                if (variableName.matches(getString(R.string.menu_existing_local_variable_name))) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "etVariableName: matches hint");
                    }
                } else if (DEBUG) {
                    MyLog.i(CLS_NAME, "etVariableName: doesn't match hint");
                }
            }
        });
        final LinearLayout linearLayoutSuccessResponse = materialDialog.getWindow().findViewById(R.id.linSuccessResponse);
        linearLayoutSuccessResponse.setVisibility(checkBox.isChecked() ? View.GONE : View.VISIBLE);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "cbUseHttpOutputSuccess: onCheckedChanged: " + isChecked);
                }
                linearLayoutSuccessResponse.setVisibility(isChecked? View.GONE : View.VISIBLE);
            }
        });
        final LinearLayout linearLayoutErrorResponse = materialDialog.getWindow().findViewById(R.id.linErrorResponse);
        linearLayoutErrorResponse.setVisibility(checkBox.isChecked() ? View.GONE : View.VISIBLE);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "cbUseHttpOutputError: onCheckedChanged: " + isChecked);
                }
                linearLayoutErrorResponse.setVisibility(isChecked? View.GONE : View.VISIBLE);
            }
        });
    }

    public void showCustomIntentDialog(CustomCommand customCommand, final CustomIntent customIntent, final int index, final long rowId) {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setCancelable(false)
                .setView(R.layout.custom_intent_dialog_layout)
                .setTitle(R.string.menu_custom_intent)
                .setIcon(R.drawable.ic_memory)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomIntentDialog: onPositive");
                        }
                        if (dialog instanceof AlertDialog) {
                            EditText editTextKeyphrase = ((AlertDialog) dialog).getWindow().findViewById(R.id.etKeyphrase);
                            EditText editTextSuccessResponse = ((AlertDialog) dialog).getWindow().findViewById(R.id.etSuccessResponse);
                            EditText editTextErrorResponse = ((AlertDialog) dialog).getWindow().findViewById(R.id.etErrorResponse);
                            if (editTextKeyphrase.getText() == null || editTextSuccessResponse.getText() == null || editTextErrorResponse.getText() == null) {
                                dialog.dismiss();
                                return;
                            }
                            String keyphrase = editTextKeyphrase.getText().toString().trim();
                            String successResponse = editTextSuccessResponse.getText().toString().trim();
                            String errorResponse = editTextErrorResponse.getText().toString().trim();
                            if (!ai.saiy.android.utils.UtilsString.notNaked(keyphrase)) {
                                toast(getString(R.string.custom_naked_error), Toast.LENGTH_SHORT);
                                return;
                            } else if (!ai.saiy.android.utils.UtilsString.regexCheck(keyphrase)) {
                                toast(getString(R.string.custom_format_error), Toast.LENGTH_SHORT);
                                return;
                            }
                            switch (((Spinner) ((AlertDialog) dialog).getWindow().findViewById(R.id.spIntentTarget)).getSelectedItemPosition()) {
                                case 1:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "showCustomIntentDialog: TARGET_BROADCAST_RECEIVER");
                                    }
                                    customIntent.setTarget(CustomIntent.TARGET_BROADCAST_RECEIVER);
                                    break;
                                case 2:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "showCustomIntentDialog: TARGET_SERVICE");
                                    }
                                    customIntent.setTarget(CustomIntent.TARGET_SERVICE);
                                    break;
                                default:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "showCustomIntentDialog: TARGET_ACTIVITY");
                                    }
                                    customIntent.setTarget(CustomIntent.TARGET_ACTIVITY);
                                    break;
                            }
                            EditText editTextIntentAction = ((AlertDialog) dialog).getWindow().findViewById(R.id.etIntentAction);
                            if (editTextIntentAction.getText() != null) {
                                customIntent.setAction(editTextIntentAction.getText().toString().trim());
                            } else {
                                customIntent.setAction("");
                            }
                            EditText editTextIntentCategory = ((AlertDialog) dialog).getWindow().findViewById(R.id.etIntentCategory);
                            if (editTextIntentCategory.getText() != null) {
                                customIntent.setCategory(editTextIntentCategory.getText().toString().trim());
                            } else {
                                customIntent.setCategory("");
                            }
                            EditText editTextIntentPackage = ((AlertDialog) dialog).getWindow().findViewById(R.id.etIntentPackage);
                            if (editTextIntentPackage.getText() != null) {
                                customIntent.setPackageName(editTextIntentPackage.getText().toString().trim());
                            } else {
                                customIntent.setPackageName("");
                            }
                            EditText editTextIntentClass = ((AlertDialog) dialog).getWindow().findViewById(R.id.etIntentClass);
                            if (editTextIntentClass.getText() != null) {
                                customIntent.setClassName(editTextIntentClass.getText().toString().trim());
                            } else {
                                customIntent.setClassName("");
                            }
                            EditText editTextIntentData = ((AlertDialog) dialog).getWindow().findViewById(R.id.etIntentData);
                            if (editTextIntentData.getText() != null) {
                                customIntent.setData(editTextIntentData.getText().toString().trim());
                            } else {
                                customIntent.setData("");
                            }
                            EditText editTextIntentMimeType = ((AlertDialog) dialog).getWindow().findViewById(R.id.etIntentMimeType);
                            if (editTextIntentMimeType.getText() != null) {
                                customIntent.setMimeType(editTextIntentMimeType.getText().toString().trim());
                            } else {
                                customIntent.setMimeType("");
                            }
                            EditText editTextIntentExtras = ((AlertDialog) dialog).getWindow().findViewById(R.id.etIntentExtras);
                            final String intentExtras = (editTextIntentExtras.getText() == null)? null: editTextIntentExtras.getText().toString().trim();
                            if (!ai.saiy.android.utils.UtilsString.notNaked(intentExtras)) {
                                customIntent.setExtras("");
                                dialog.dismiss();
                                toast(getString(R.string.menu_success_exclamation), Toast.LENGTH_SHORT);
                                final CustomCommand cc = new CustomCommand(CCC.CUSTOM_SEND_INTENT, CC.COMMAND_USER_CUSTOM, keyphrase, successResponse, errorResponse, SPH.getTTSLocale(getApplicationContext()).toString(), SPH.getVRLocale(getApplicationContext()).toString(), ((CheckBox) ((AlertDialog) dialog).getWindow().findViewById(R.id.cbVoiceRecognition)).isChecked() ? LocalRequest.ACTION_SPEAK_LISTEN : LocalRequest.ACTION_SPEAK_ONLY);
                                final String gsonString = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().toJson(customIntent);
                                customIntent.setSerialised(gsonString);
                                cc.setExtraText(gsonString);
                                setCommand(cc, index, rowId);
                                return;
                            }
                            if (!ai.saiy.android.utils.UtilsBundle.stringExtrasToBundle(intentExtras)) {
                                toast(getString(R.string.content_extras_format_incorrect), Toast.LENGTH_SHORT);
                                return;
                            }
                            customIntent.setExtras(intentExtras);
                            dialog.dismiss();
                            toast(getString(R.string.menu_success_exclamation), Toast.LENGTH_SHORT);
                            final CustomCommand cc = new CustomCommand(CCC.CUSTOM_SEND_INTENT, CC.COMMAND_USER_CUSTOM, keyphrase, successResponse, errorResponse, SPH.getTTSLocale(getApplicationContext()).toString(), SPH.getVRLocale(getApplicationContext()).toString(), ((CheckBox) ((AlertDialog) dialog).getWindow().findViewById(R.id.cbVoiceRecognition)).isChecked() ? LocalRequest.ACTION_SPEAK_LISTEN : LocalRequest.ACTION_SPEAK_ONLY);
                            final String gsonString = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().toJson(customIntent);
                            customIntent.setSerialised(gsonString);
                            cc.setExtraText(gsonString);
                            setCommand(cc, index, rowId);
                        }
                    }
                })
                .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomIntentDialog: onNeutral");
                        }
                        dialog.dismiss();
                        deleteCustomCommand(index, rowId);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomIntentDialog: onNegative");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomIntentDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();

        ((EditText) materialDialog.getWindow().findViewById(R.id.etKeyphrase)).setText(customCommand.getKeyphrase());
        ((EditText) materialDialog.getWindow().findViewById(R.id.etSuccessResponse)).setText(customCommand.getResponseSuccess());
        ((EditText) materialDialog.getWindow().findViewById(R.id.etErrorResponse)).setText(customCommand.getResponseError());
        Spinner spinner = materialDialog.getWindow().findViewById(R.id.spIntentTarget);
        switch (customIntent.getTarget()) {
            case CustomIntent.TARGET_BROADCAST_RECEIVER:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "showCustomIntentDialog: TARGET_BROADCAST_RECEIVER");
                }
                spinner.setSelection(1);
                break;
            case CustomIntent.TARGET_SERVICE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "showCustomIntentDialog: TARGET_SERVICE");
                }
                spinner.setSelection(2);
                break;
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "showCustomIntentDialog: TARGET_ACTIVITY");
                }
                spinner.setSelection(0);
                break;
        }
        ((EditText) materialDialog.getWindow().findViewById(R.id.etIntentAction)).setText(customIntent.getAction());
        ((EditText) materialDialog.getWindow().findViewById(R.id.etIntentCategory)).setText(customIntent.getCategory());
        ((EditText) materialDialog.getWindow().findViewById(R.id.etIntentPackage)).setText(customIntent.getPackageName());
        ((EditText) materialDialog.getWindow().findViewById(R.id.etIntentClass)).setText(customIntent.getClassName());
        ((EditText) materialDialog.getWindow().findViewById(R.id.etIntentData)).setText(customIntent.getData());
        ((EditText) materialDialog.getWindow().findViewById(R.id.etIntentMimeType)).setText(customIntent.getMimeType());
        ((EditText) materialDialog.getWindow().findViewById(R.id.etIntentExtras)).setText(customIntent.getExtras());
    }

    public void showCustomCommandInputDialog(CustomCommand customCommand, final CCC ccc, String content, final String extraText, final String extraText2, boolean voiceRecognition, final int index, final long rowId) {
        String keyphrase;
        String hintForKeyphrase;
        if (CCC.CUSTOM_SEARCHABLE == ccc) {
            keyphrase = getApplicationContext().getString(R.string.menu_keyphrase_hint_searchable, extraText2);
            hintForKeyphrase = getApplicationContext().getString(R.string.menu_keyphrase_hint_searchable, extraText2);
        } else {
            keyphrase = null;
            hintForKeyphrase = getString(R.string.menu_keyphrase_hint);
        }
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setCancelable(false)
                .setView(R.layout.custom_command_input_dialog_layout)
                .setTitle(R.string.menu_custom_commands)
                .setIcon(R.drawable.ic_shape_plus)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomCommandInputDialog: onPositive");
                        }
                        if (dialog instanceof AlertDialog) {
                            EditText editTextKeyphrase = ((AlertDialog) dialog).findViewById(R.id.etKeyphrase);
                            EditText editTextSuccessResponse = ((AlertDialog) dialog).findViewById(R.id.etSuccessResponse);
                            EditText editTextErrorResponse = ((AlertDialog) dialog).findViewById(R.id.etErrorResponse);
                            if (editTextKeyphrase.getText() == null || editTextSuccessResponse.getText() == null || editTextErrorResponse.getText() == null) {
                                dialog.dismiss();
                                return;
                            }
                            String keyphrase = editTextKeyphrase.getText().toString().trim();
                            String successResponse = editTextSuccessResponse.getText().toString().trim();
                            String errorResponse = editTextErrorResponse.getText().toString().trim();
                            if (!ai.saiy.android.utils.UtilsString.notNaked(keyphrase)) {
                                toast(getString(R.string.custom_naked_error), Toast.LENGTH_SHORT);
                                return;
                            }
                            if (!ai.saiy.android.utils.UtilsString.regexCheck(keyphrase)) {
                                toast(getString(R.string.custom_format_error), Toast.LENGTH_SHORT);
                                return;
                            }

                            dialog.dismiss();
                            toast(getString(R.string.menu_success_exclamation), Toast.LENGTH_SHORT);
                            final CustomCommand cc = new CustomCommand(ccc, CC.COMMAND_USER_CUSTOM, keyphrase, successResponse, errorResponse, SPH.getTTSLocale(getApplicationContext()).toString(), SPH.getVRLocale(getApplicationContext()).toString(), ((CheckBox) ((AlertDialog) dialog).findViewById(R.id.cbVoiceRecognition)).isChecked() ? LocalRequest.ACTION_SPEAK_LISTEN : LocalRequest.ACTION_SPEAK_ONLY);
                            switch (ccc) {
                                case CUSTOM_TASKER_TASK:
                                    cc.setExtraText(extraText);
                                    break;
                                case CUSTOM_DISPLAY_CONTACT:
                                case CUSTOM_CALL_CONTACT:
                                case CUSTOM_LAUNCH_APPLICATION:
                                case CUSTOM_LAUNCH_SHORTCUT:
                                case CUSTOM_SEARCHABLE:
                                case CUSTOM_AUTOMATE_FLOW:
                                    cc.setExtraText(extraText);
                                    cc.setExtraText2(extraText2);
                                    break;
                                case CUSTOM_ACTIVITY:
                                    cc.setIntent(extraText);
                                    cc.setExtraText(extraText2);
                                    break;
                                default:
                                    cc.setExtraText(extraText);
                                    cc.setRegex(Regex.STARTS_WITH);
                                    break;
                            }
                            setCommand(cc, index, rowId);
                        }
                    }
                })
                .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomCommandInputDialog: onNeutral");
                        }
                        dialog.dismiss();
                        deleteCustomCommand(index, rowId);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomCommandInputDialog: onNegative");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomCommandInputDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();

        EditText editTextKeyphrase = materialDialog.getWindow().findViewById(R.id.etKeyphrase);
        editTextKeyphrase.setHint(hintForKeyphrase);
        if (keyphrase != null) {
            editTextKeyphrase.setText(keyphrase);
        }
        ((TextView) materialDialog.getWindow().findViewById(R.id.tvContent)).setText(content);
        ((EditText) materialDialog.getWindow().findViewById(R.id.etKeyphrase)).setText(customCommand.getKeyphrase());
        ((EditText) materialDialog.getWindow().findViewById(R.id.etSuccessResponse)).setText(customCommand.getResponseSuccess());
        ((EditText) materialDialog.getWindow().findViewById(R.id.etErrorResponse)).setText(customCommand.getResponseError());
        ((CheckBox) materialDialog.getWindow().findViewById(R.id.cbVoiceRecognition)).setChecked(voiceRecognition);
    }

    public void toast(String text, int duration) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "makeToast: " + text);
        }
        if (getParent().isActive()) {
            getParentActivity().toast(text, duration);
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "toast Fragment detached");
        }
    }

    public void showRemoteIntentDialog(String appName, final int index, final long rowId) {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setTitle(R.string.title_external_command)
                .setMessage(Html.fromHtml(String.format(getString(R.string.content_external_command), appName).replace("\n", "<br/>")))
                .setIcon(R.drawable.ic_shape_plus)
                .setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showRemoteIntentDialog: onPositive");
                        }
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showRemoteIntentDialog: onNeutral");
                        }
                        dialog.dismiss();
                        deleteCustomCommand(index, rowId);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showRemoteIntentDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                })
                .create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
        materialDialog.show();
    }

    public void showNicknameDialog(final String nickname, final String contactName, final int index, final long rowId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final String[] nicknameHints = ai.saiy.android.localisation.SaiyResourcesHelper.getArrayResource(getApplicationContext(), SupportedLanguage.getSupportedLanguage(SPH.getVRLocale(getApplicationContext())), R.array.array_nickname_hints);
                getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                                .setView(R.layout.text_input_dialog_layout)
                                .setTitle(R.string.menu_nicknames)
                                .setIcon(R.drawable.ic_account_switch)
                                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        CharSequence charSequence = null;
                                        if (dialog instanceof AlertDialog) {
                                            final EditText editText = ((AlertDialog) dialog).findViewById(android.R.id.input);
                                            charSequence = (editText == null) ? null : editText.getText();
                                        }
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showNicknameDialog: onInput: " + charSequence);
                                        }

                                        if (charSequence == null) {
                                            toast(getString(R.string.nickname_naked_error), Toast.LENGTH_SHORT);
                                            return;
                                        }
                                        String newNickName = charSequence.toString().trim();
                                        if (!ai.saiy.android.utils.UtilsString.notNaked(newNickName)) {
                                            toast(getString(R.string.nickname_naked_error), Toast.LENGTH_SHORT);
                                            return;
                                        }
                                        if (!ai.saiy.android.utils.UtilsString.regexCheck(newNickName)) {
                                            toast(getString(R.string.nickname_format_error), Toast.LENGTH_SHORT);
                                            return;
                                        }
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showNicknameDialog: creating: " + contactName + " ~ " + newNickName);
                                        }
                                        dialog.dismiss();
                                        toast(getString(R.string.menu_success_exclamation), Toast.LENGTH_SHORT);
                                        setNickname(new CustomNickname(newNickName, contactName), index, rowId);
                                    }
                                })
                                .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showNicknameDialog: onNeutral");
                                        }
                                        dialog.dismiss();
                                        deleteCustomNickname(index, rowId);
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showNicknameDialog: onNegative");
                                        }
                                        dialog.dismiss();
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showNicknameDialog: onCancel");
                                        }
                                        dialog.dismiss();
                                    }
                                }).create();
                        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
                        materialDialog.show();

                        final TextInputLayout textInputLayout = materialDialog.getWindow().findViewById(android.R.id.inputArea);
                        textInputLayout.setHint(getString(R.string.title_enter_nickname_for) + XMLResultsHandler.SEP_SPACE + contactName);
                        final EditText editText = textInputLayout.findViewById(android.R.id.input);
                        editText.setInputType(InputType.TYPE_CLASS_TEXT);
                        editText.setHint(nicknameHints[new Random().nextInt(nicknameHints.length)]);
                        editText.setText(nickname);
                        editText.setFilters(new InputFilter[]{inputFilter});
                    }
                });
            }
        });
    }

    public void showCustomPhrasesDialog(String keyphrase, String response, boolean voiceRecognition, final int index, final long rowId) {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setCancelable(false)
                .setView(R.layout.phrase_dialog_layout)
                .setTitle(R.string.menu_custom_phrases)
                .setIcon(R.drawable.ic_format_quote)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomPhrasesDialog: onPositive");
                        }
                        if (dialog instanceof AlertDialog) {
                            EditText editTextKeyphrase = ((AlertDialog) dialog).findViewById(R.id.etInput);
                            EditText editTextResponse = ((AlertDialog) dialog).findViewById(R.id.etResponse);
                            if (editTextKeyphrase.getText() == null || editTextResponse.getText() == null) {
                                dialog.dismiss();
                                return;
                            }
                            final String newKeyphrase = editTextKeyphrase.getText().toString().trim();
                            final String newResponse = editTextResponse.getText().toString().trim();
                            if (!ai.saiy.android.utils.UtilsString.notNaked(newKeyphrase) || !ai.saiy.android.utils.UtilsString.notNaked(newResponse)) {
                                toast(getString(R.string.custom_phrase_naked_error), Toast.LENGTH_SHORT);
                                return;
                            }
                            if (!ai.saiy.android.utils.UtilsString.regexCheck(newKeyphrase) || !ai.saiy.android.utils.UtilsString.regexCheck(newResponse)) {
                                toast(getString(R.string.custom_phrase_format_error), Toast.LENGTH_SHORT);
                                return;
                            }
                            dialog.dismiss();
                            toast(getString(R.string.menu_success_exclamation), Toast.LENGTH_SHORT);
                            setPhrase(new CustomPhrase(newKeyphrase, newResponse, ((CheckBox) ((AlertDialog) dialog).findViewById(R.id.cbVoiceRecognition)).isChecked()), index, rowId);
                        }
                    }
                })
                .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomPhrasesDialog: onNeutral");
                        }
                        dialog.dismiss();
                        deleteCustomPhrase(index, rowId);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomPhrasesDialog: onNegative");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomPhrasesDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();

        ((EditText) materialDialog.getWindow().findViewById(R.id.etInput)).setText(keyphrase);
        ((EditText) materialDialog.getWindow().findViewById(R.id.etResponse)).setText(response);
        ((CheckBox) materialDialog.getWindow().findViewById(R.id.cbVoiceRecognition)).setChecked(voiceRecognition);
    }

    public void showProgress(boolean visible) {
        if (getParent().isActive()) {
            getParentActivity().showProgress(visible);
        }
    }

    public ActivityHome getParentActivity() {
        return this.parentFragment.getParentActivity();
    }

    public void showCustomReplacementDialog(String keyphrase, String replacement, final int index, final long rowId) {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setCancelable(false)
                .setView(R.layout.replacements_dialog_layout)
                .setTitle(R.string.menu_replace_words)
                .setIcon(R.drawable.ic_swap_horizontal)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomReplacementDialog: onPositive");
                        }
                        if (dialog instanceof AlertDialog) {
                            EditText editTextKeyphrase = ((AlertDialog) dialog).findViewById(R.id.etPhrase);
                            EditText editTextReplacement = ((AlertDialog) dialog).findViewById(R.id.etReplacement);
                            if (editTextKeyphrase.getText() == null || editTextReplacement.getText() == null) {
                                dialog.dismiss();
                                return;
                            }
                            final String newKeyphrase = editTextKeyphrase.getText().toString().trim();
                            final String newReplacement = editTextReplacement.getText().toString().trim();
                            if (!ai.saiy.android.utils.UtilsString.notNaked(newKeyphrase) || !ai.saiy.android.utils.UtilsString.notNaked(newReplacement)) {
                                toast(getString(R.string.replace_words_naked_error), Toast.LENGTH_SHORT);
                                return;
                            }
                            if (!ai.saiy.android.utils.UtilsString.regexCheck(newKeyphrase) || !ai.saiy.android.utils.UtilsString.regexCheck(newReplacement)) {
                                toast(getString(R.string.replace_words_format_error), Toast.LENGTH_SHORT);
                                return;
                            }
                            dialog.dismiss();
                            toast(getString(R.string.menu_success_exclamation), Toast.LENGTH_SHORT);
                            setReplacement(new CustomReplacement(newKeyphrase, newReplacement), index, rowId);
                        }
                    }
                })
                .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomReplacementDialog: onNeutral");
                        }
                        dialog.dismiss();
                        deleteCustomReplacement(index, rowId);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomReplacementDialog: onNegative");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomReplacementDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();

        ((EditText) materialDialog.getWindow().findViewById(R.id.etPhrase)).setText(keyphrase);
        ((EditText) materialDialog.getWindow().findViewById(R.id.etReplacement)).setText(replacement);
    }

    public FragmentEditCustomisation getParent() {
        return parentFragment;
    }
}

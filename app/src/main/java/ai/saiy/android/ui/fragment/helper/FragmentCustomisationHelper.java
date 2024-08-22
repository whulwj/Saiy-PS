/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.ui.fragment.helper;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Pair;
import android.view.LayoutInflater;
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

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import ai.saiy.android.R;
import ai.saiy.android.api.request.Regex;
import ai.saiy.android.applications.ApplicationActivityBasic;
import ai.saiy.android.applications.ApplicationBasic;
import ai.saiy.android.applications.Install;
import ai.saiy.android.applications.Installed;
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
import ai.saiy.android.thirdparty.tasker.TaskerIntent;
import ai.saiy.android.ui.activity.ActivityActivityPicker;
import ai.saiy.android.ui.activity.ActivityApplicationPicker;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.activity.ActivityShortcutPicker;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIMainAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentCustomisation;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

/**
 * Utility class to assist its parent fragment and avoid clutter there
 * <p>
 * Created by benrandall76@gmail.com on 25/07/2016.
 */

public class FragmentCustomisationHelper {
    public static final int NICKNAME_REQ_CODE = 1001;
    private static final int CUSTOM_DISPLAY_CONTACT_REQ_CODE = 1002;
    private static final int CUSTOM_CALL_CONTACT_REQ_CODE = 1003;
    private static final int CUSTOM_TASKER_REQ_CODE = 1004;
    private static final int APP_PICKER_REQ_CODE = 1005;
    private static final int SHORTCUT_PICKER_REQ_CODE = 1006;
    private static final int SEARCHABLE_REQ_CODE = 1007;
    private static final int ACTIVITY_REQ_CODE = 1008;
    private static final int AUTOMATE_FLOW_PICKER_REQ_CODE = 1009;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentCustomisationHelper.class.getSimpleName();

    private final FragmentCustomisation parentFragment;
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

    /**
     * Constructor
     *
     * @param parentFragment the parent fragment for this helper class
     */
    public FragmentCustomisationHelper(@NonNull final FragmentCustomisation parentFragment) {
        this.parentFragment = parentFragment;
    }

    private String getString(@StringRes int resId) {
        return getApplicationContext().getString(resId);
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

    /**
     * Get the components for this fragment
     *
     * @return a list of {@link ContainerUI} elements
     */
    private ArrayList<ContainerUI> getUIComponents() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getUIComponents");
        }

        final ArrayList<ContainerUI> mObjects = new ArrayList<>();
        final int chevronResource = R.drawable.chevron;

        ContainerUI containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_custom_name));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_set));
        containerUI.setIconMain(R.drawable.ic_person);
        containerUI.setIconExtra(chevronResource);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_custom_intro));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_crown);
        containerUI.setIconExtra(chevronResource);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_custom_commands));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_create));
        containerUI.setIconMain(R.drawable.ic_shape_plus);
        containerUI.setIconExtra(chevronResource);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_custom_phrases));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_create));
        containerUI.setIconMain(R.drawable.ic_format_quote);
        containerUI.setIconExtra(chevronResource);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_nicknames));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_create));
        containerUI.setIconMain(R.drawable.ic_account_switch);
        containerUI.setIconExtra(chevronResource);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_replace_words));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_set));
        containerUI.setIconMain(R.drawable.ic_swap_horizontal);
        containerUI.setIconExtra(chevronResource);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_sound_effects));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_play));
        containerUI.setIconMain(R.drawable.ic_music_square);
        containerUI.setIconExtra(chevronResource);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_edit_customisations));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_edit));
        containerUI.setIconMain(R.drawable.ic_pencil);
        containerUI.setIconExtra(chevronResource);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_import));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_import));
        containerUI.setIconMain(R.drawable.ic_import);
        containerUI.setIconExtra(chevronResource);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_export));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_export));
        containerUI.setIconMain(R.drawable.ic_export);
        containerUI.setIconExtra(chevronResource);
        mObjects.add(containerUI);

        return mObjects;
    }

    /**
     * Get the recycler view for this fragment
     *
     * @param parent the view parent
     * @return the {@link RecyclerView}
     */
    public RecyclerView getRecyclerView(@NonNull final View parent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getRecyclerView");
        }

        final RecyclerView recyclerView = parent.findViewById(R.id.layout_common_fragment_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getParentActivity(), null));

        return recyclerView;
    }

    /**
     * Update the parent fragment with the UI components
     */
    public void finaliseUI() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ContainerUI> tempArray = FragmentCustomisationHelper.this.getUIComponents();

                if (FragmentCustomisationHelper.this.getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)) {

                    try {
                        Thread.sleep(FragmentHome.DRAWER_CLOSE_DELAY);
                    } catch (final InterruptedException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "finaliseUI InterruptedException");
                            e.printStackTrace();
                        }
                    }
                }

                if (FragmentCustomisationHelper.this.getParent().isActive()) {

                    FragmentCustomisationHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        FragmentCustomisationHelper.this.getParent().getObjects().addAll(tempArray);
                        FragmentCustomisationHelper.this.getParent().getAdapter().notifyItemRangeInserted(0, FragmentCustomisationHelper.this.getParent().getObjects().size());
                    }});

                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "finaliseUI Fragment detached");
                    }
                }
            }
        });
    }

    /**
     * Get the adapter for this fragment
     *
     * @param mObjects list of {@link ContainerUI} elements
     * @return the {@link UIMainAdapter}
     */
    public UIMainAdapter getAdapter(@NonNull final ArrayList<ContainerUI> mObjects) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAdapter");
        }
        return new UIMainAdapter(mObjects, getParent(), getParent());
    }

    /**
     * Show the custom intro dialog
     */
    @SuppressWarnings("ConstantConditions")
    public void showCustomIntroDialog() {

        final String currentIntro = SPH.getCustomIntro(getApplicationContext());
        String hint = null;
        String content = null;

        if (currentIntro != null) {
            if (currentIntro.isEmpty()) {
                hint = getApplicationContext().getString(R.string.silence);
            } else {
                content = currentIntro;
            }
        } else {
            hint = getApplicationContext().getString(R.string.custom_intro_hint);
        }

        final View customView = LayoutInflater.from(getParentActivity()).inflate(R.layout.md_dialog_input, null, false);
        final CheckBox checkBox = customView.findViewById(R.id.md_promptCheckbox);
        checkBox.setText(R.string.custom_intro_checkbox_title);
        checkBox.setChecked(SPH.getCustomIntroRandom(getApplicationContext()));
        final EditText editText = customView.findViewById(android.R.id.input);
        editText.setHint(hint);
        editText.setText(content);

        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setView(customView)
                .setTitle(R.string.menu_custom_intro)
                .setIcon(R.drawable.ic_crown)
                .setMessage(R.string.custom_intro_text)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final CharSequence input = editText.getText();
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomIntroDialog: input: " + input);
                        }

                        if (input != null) {
                            if (UtilsString.notNaked(input.toString())) {
                                SPH.setCustomIntroRandom(FragmentCustomisationHelper.this.getApplicationContext(),
                                        checkBox.isChecked());
                                SPH.setCustomIntro(FragmentCustomisationHelper.this.getApplicationContext(), input.toString().trim());
                            } else {
                                SPH.setCustomIntroRandom(FragmentCustomisationHelper.this.getApplicationContext(), false);
                                SPH.setCustomIntro(FragmentCustomisationHelper.this.getApplicationContext(), "");
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    private void showApplicationPicker(int requestCode, int type) {
        final Intent intent = new Intent(getApplicationContext(), ActivityApplicationPicker.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(ActivityApplicationPicker.EXTRA_TYPE, type);
        try {
            getParent().startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }
    }

    private void showCustomCommandInputDialog(final CCC ccc, String content, final String extraText, final String extraText2, final String actionOfIntent) {
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
                                    cc.setExtraText(extraText2);
                                    cc.setActionOfIntent(actionOfIntent);
                                    cc.setRegex(Regex.STARTS_WITH);
                                    break;
                            }
                            setCommand(cc);
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
    }

    private void setNickname(final CustomNickname customNickname) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ai.saiy.android.custom.CustomNicknameHelper.setNickname(getApplicationContext(), customNickname, null, -1L);
            }
        });
    }

    private void setPhrase(final CustomPhrase customPhrase) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ai.saiy.android.custom.CustomPhraseHelper.setPhrase(getApplicationContext(), customPhrase, null, -1L);
            }
        });
    }

    private void setReplacement(final CustomReplacement customReplacement) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ai.saiy.android.custom.CustomReplacementHelper.setReplacement(getApplicationContext(), customReplacement, null, -1L);
            }
        });
    }

    private void setCommand(final ai.saiy.android.custom.CustomCommand customCommand) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ai.saiy.android.custom.CustomCommandHelper.setCommand(getApplicationContext(), customCommand, -1L);
            }
        });
    }

    private void showNicknameDialog(String contactName) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final String[] nicknameHints = ai.saiy.android.localisation.SaiyResourcesHelper.getArrayResource(getApplicationContext(), SupportedLanguage.getSupportedLanguage(SPH.getVRLocale(getApplicationContext())), R.array.array_nickname_hints);
                getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                                .setCancelable(false)
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
                                        String nickName = charSequence.toString().trim();
                                        if (!ai.saiy.android.utils.UtilsString.notNaked(nickName)) {
                                            toast(getString(R.string.nickname_naked_error), Toast.LENGTH_SHORT);
                                            return;
                                        }
                                        if (!ai.saiy.android.utils.UtilsString.regexCheck(nickName)) {
                                            toast(getString(R.string.nickname_format_error), Toast.LENGTH_SHORT);
                                            return;
                                        }
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showNicknameDialog: creating: " + contactName + " ~ " + nickName);
                                        }
                                        dialog.dismiss();
                                        toast(getString(R.string.menu_success_exclamation), Toast.LENGTH_SHORT);
                                        setNickname(new CustomNickname(nickName, contactName));
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
                        editText.setFilters(new InputFilter[]{inputFilter});
                    }
                });
            }
        });
    }

    private void showActivityPicker() {
        final Intent intent = new Intent(getApplicationContext(), ActivityActivityPicker.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            getParent().startActivityForResult(intent, FragmentCustomisationHelper.ACTIVITY_REQ_CODE);
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }
    }

    private void showShortcutPicker(String packageName, int requestCode) {
        final Intent intent = new Intent(getApplicationContext(), ActivityShortcutPicker.class);
        if (UtilsString.notNaked(packageName)) {
            intent.putExtra(ActivityShortcutPicker.EXTRA_PACKAGE, packageName);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            getParent().startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }
    }

    private void showCustomIntentDialog() {
        final CustomIntent customIntent = new CustomIntent();
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
                                CustomCommand cc = new CustomCommand(CCC.CUSTOM_SEND_INTENT, CC.COMMAND_USER_CUSTOM, keyphrase, successResponse, errorResponse, SPH.getTTSLocale(getApplicationContext()).toString(), SPH.getVRLocale(getApplicationContext()).toString(), ((CheckBox) ((AlertDialog) dialog).getWindow().findViewById(R.id.cbVoiceRecognition)).isChecked() ? LocalRequest.ACTION_SPEAK_LISTEN : LocalRequest.ACTION_SPEAK_ONLY);
                                String gsonString = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().toJson(customIntent);
                                customIntent.setSerialised(gsonString);
                                cc.setExtraText(gsonString);
                                setCommand(cc);
                                return;
                            }
                            if (!ai.saiy.android.utils.UtilsBundle.stringExtrasToBundle(intentExtras)) {
                                toast(getString(R.string.content_extras_format_incorrect), Toast.LENGTH_SHORT);
                                return;
                            }
                            customIntent.setExtras(intentExtras);
                            dialog.dismiss();
                            toast(getString(R.string.menu_success_exclamation), Toast.LENGTH_SHORT);
                            CustomCommand cc = new CustomCommand(CCC.CUSTOM_SEND_INTENT, CC.COMMAND_USER_CUSTOM, keyphrase, successResponse, errorResponse, SPH.getTTSLocale(getApplicationContext()).toString(), SPH.getVRLocale(getApplicationContext()).toString(), ((CheckBox) ((AlertDialog) dialog).getWindow().findViewById(R.id.cbVoiceRecognition)).isChecked() ? LocalRequest.ACTION_SPEAK_LISTEN : LocalRequest.ACTION_SPEAK_ONLY);
                            String gsonString = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().toJson(customIntent);
                            customIntent.setSerialised(gsonString);
                            cc.setExtraText(gsonString);
                            setCommand(cc);
                        }
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
    }

    private void showCustomHttpDialog() {
        final CustomHttp customHttp = new ai.saiy.android.command.http.HttpsProcessor().createDefault();
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
                                setCommand(cc);
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
                            setCommand(cc);
                        }
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
        final CheckBox checkBox = materialDialog.getWindow().findViewById(R.id.cbOutputTasker);
        checkBox.setEnabled(new ai.saiy.android.thirdparty.tasker.TaskerHelper().isTaskerInstalled(getApplicationContext()).first);
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

    private void showTaskPicker() {
        getParent().startActivityForResult(TaskerIntent.getTaskSelectIntent(), CUSTOM_TASKER_REQ_CODE);
    }

    public void showContactPicker(int requestCode) {
        if (!ai.saiy.android.permissions.PermissionHelper.checkContactGroupPermissions(getApplicationContext(), null)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "showContactPicker: requesting contact permissions");
            }
            return;
        }
        final Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        if (requestCode == CUSTOM_CALL_CONTACT_REQ_CODE) {
            intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        } else {
            intent.setData(ContactsContract.Contacts.CONTENT_URI);
        }
        try {
            getParent().startActivityForResult(intent, requestCode);
            getParentActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onActivityResult: " + requestCode + " ~ " + resultCode);
        }
        if (resultCode != Activity.RESULT_OK) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onActivityResult: RESULT_CANCELLED");
            }
            return;
        }
        switch (requestCode) {
            case NICKNAME_REQ_CODE: {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: NICKNAME_REQ_CODE");
                }
                if (data == null) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: data null");
                    }
                    break;
                }
                final Uri uri = data.getData();
                if (uri == null) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: contact uri null");
                    }
                    break;
                }
                final String displayName = new ai.saiy.android.contacts.ContactHelper().getNameFromUri(getApplicationContext(), uri);
                if (ai.saiy.android.utils.UtilsString.notNaked(displayName)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onActivityResult: displayName: " + displayName);
                    }
                    showNicknameDialog(displayName);
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: displayName naked");
                    }
                }
            }
            break;
            case CUSTOM_DISPLAY_CONTACT_REQ_CODE: {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: CUSTOM_DISPLAY_CONTACT_REQ_CODE");
                }
                if (data == null) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: data null");
                    }
                    break;
                }
                final Uri uri = data.getData();
                if (uri == null) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: contact uri null");
                    }
                    break;
                }
                final String displayName = new ai.saiy.android.contacts.ContactHelper().getNameFromUri(getApplicationContext(), uri);
                if (!ai.saiy.android.utils.UtilsString.notNaked(displayName)) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: displayName naked");
                    }
                    break;
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: displayName: " + displayName);
                }
                final String contactId = uri.getLastPathSegment();
                if (ai.saiy.android.utils.UtilsString.notNaked(contactId)) {
                    showCustomCommandInputDialog(CCC.CUSTOM_DISPLAY_CONTACT, getApplicationContext().getString(R.string.content_display_contact, displayName), contactId, displayName, null);
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: contactId naked");
                    }
                }
            }
            break;
            case CUSTOM_CALL_CONTACT_REQ_CODE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: CUSTOM_CALL_CONTACT_REQ_CODE");
                }
                if (data == null) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: data null");
                    }
                    break;
                }
                final Uri uri = data.getData();
                if (uri == null) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: contact uri null");
                    }
                    break;
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: contactUri: " + uri);
                }
                final ai.saiy.android.custom.Phone contactPair = new ai.saiy.android.contacts.ContactHelper().getNameAndNumberFromUri(getApplicationContext(), uri);
                if (contactPair == null) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: contactPair null");
                    }
                    break;
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: name: " + contactPair.getName());
                    MyLog.i(CLS_NAME, "onActivityResult: number: " + contactPair.getNumber());
                    MyLog.i(CLS_NAME, "onActivityResult: type: " + contactPair.getType());
                }
                if (ai.saiy.android.utils.UtilsString.notNaked(contactPair.getNumber())) {
                    showCustomCommandInputDialog(CCC.CUSTOM_CALL_CONTACT, getApplicationContext().getString(R.string.content_call_contact, contactPair.getName(), contactPair.getNumber()), contactPair.getNumber(), contactPair.getName(), null);
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: customCall.getNumber() naked");
                    }
                }
                break;
            case CUSTOM_TASKER_REQ_CODE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: CUSTOM_TASKER_REQ_CODE");
                }
                if (data == null) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: data null");
                    }
                    break;
                }
                final String taskName = data.getDataString();
                if (ai.saiy.android.utils.UtilsString.notNaked(taskName)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onActivityResult: taskName: " + taskName);
                    }
                    showCustomCommandInputDialog(CCC.CUSTOM_TASKER_TASK, getApplicationContext().getString(R.string.content_execute_task, taskName), taskName, null, null);
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: taskName naked");
                    }
                }
                break;
            case APP_PICKER_REQ_CODE: {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: APP_PICKER_REQ_CODE");
                }
                if (data == null) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: data null");
                    }
                    break;
                }
                if (!data.hasExtra(ActivityApplicationPicker.EXTRA_APPLICATION)) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: no application object");
                    }
                    break;
                }
                final ApplicationBasic applicationBasic = data.getParcelableExtra(ActivityApplicationPicker.EXTRA_APPLICATION);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getName: " + applicationBasic.getName());
                    MyLog.i(CLS_NAME, "getPackageName: " + applicationBasic.getPackageName());
                }
                if (ai.saiy.android.utils.UtilsString.notNaked(applicationBasic.getPackageName())) {
                    showCustomCommandInputDialog(CCC.CUSTOM_LAUNCH_APPLICATION, getApplicationContext().getString(R.string.content_launch_app, applicationBasic.getName()), applicationBasic.getPackageName(), applicationBasic.getName(), null);
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: application.getPackageName() naked");
                    }
                }
            }
            break;
            case SHORTCUT_PICKER_REQ_CODE: {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: SHORTCUT_PICKER_REQ_CODE");
                }
                if (data == null) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: data null");
                    }
                    break;
                }
                final String shortcutName = data.getStringExtra(ActivityShortcutPicker.EXTRA_SHORTCUT_NAME);
                final String shortcutUri = data.getStringExtra(ActivityShortcutPicker.EXTRA_SHORTCUT_URI);
                if (!ai.saiy.android.utils.UtilsString.notNaked(shortcutName) || !ai.saiy.android.utils.UtilsString.notNaked(shortcutUri)) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: name or uri naked");
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onActivityResult: shortcutName: " + shortcutName);
                        MyLog.i(CLS_NAME, "onActivityResult: shortcutUri: " + shortcutUri);
                    }
                    showCustomCommandInputDialog(CCC.CUSTOM_LAUNCH_SHORTCUT, getApplicationContext().getString(R.string.content_run_shortcut, shortcutName), shortcutUri, shortcutName, null);
                }
            }
            break;
            case SEARCHABLE_REQ_CODE: {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: SEARCHABLE_REQ_CODE");
                }
                if (data == null) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: data null");
                    }
                    break;
                }
                if (!data.hasExtra(ActivityApplicationPicker.EXTRA_APPLICATION)) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: no application object");
                    }
                    break;
                }
                final ApplicationBasic applicationBasic = data.getParcelableExtra(ActivityApplicationPicker.EXTRA_APPLICATION);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getName: " + applicationBasic.getName());
                    MyLog.i(CLS_NAME, "getPackageName: " + applicationBasic.getPackageName());
                }
                if (ai.saiy.android.utils.UtilsString.notNaked(applicationBasic.getPackageName())) {
                    showCustomCommandInputDialog(CCC.CUSTOM_SEARCHABLE, getApplicationContext().getString(R.string.content_search_on, applicationBasic.getName()), applicationBasic.getPackageName(), applicationBasic.getName(), applicationBasic.getAction());
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: application.getPackageName() naked");
                    }
                }
            }
            break;
            case ACTIVITY_REQ_CODE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: ACTIVITY_REQ_CODE");
                }
                if (data == null) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: data null");
                    }
                    break;
                }
                if (!data.hasExtra(ActivityActivityPicker.EXTRA_APPLICATION)) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: no application object");
                    }
                    break;
                }
                final ApplicationActivityBasic applicationActivityBasic = data.getParcelableExtra(ActivityActivityPicker.EXTRA_APPLICATION);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getName: " + applicationActivityBasic.getName());
                    MyLog.i(CLS_NAME, "getPackageName: " + applicationActivityBasic.getPackageName());
                    MyLog.i(CLS_NAME, "getActivityName: " + applicationActivityBasic.getActivityName());
                }
                if (!ai.saiy.android.utils.UtilsString.notNaked(applicationActivityBasic.getPackageName()) || !ai.saiy.android.utils.UtilsString.notNaked(applicationActivityBasic.getActivityName())) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: applicationAB.getPackage/ActivityName() naked");
                    }
                } else {
                    final Intent intent = new Intent();
                    intent.setPackage(applicationActivityBasic.getPackageName());
                    intent.setComponent(new ComponentName(applicationActivityBasic.getPackageName(), applicationActivityBasic.getActivityName()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtras(ai.saiy.android.utils.UtilsBundle.stringExtra(applicationActivityBasic.getIntentExtras()));
                    showCustomCommandInputDialog(CCC.CUSTOM_ACTIVITY, getApplicationContext().getString(R.string.content_launch_activity, applicationActivityBasic.getPackageName()), intent.toUri(0), applicationActivityBasic.getPackageName(), null);
                }
                break;
            case AUTOMATE_FLOW_PICKER_REQ_CODE: {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: AUTOMATE_FLOW_PICKER_REQ_CODE");
                }
                if (data == null) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: data null");
                    }
                    break;
                }
                final String shortcutName = data.getStringExtra(ActivityShortcutPicker.EXTRA_SHORTCUT_NAME);
                final String shortcutUri = data.getStringExtra(ActivityShortcutPicker.EXTRA_SHORTCUT_URI);
                if (!ai.saiy.android.utils.UtilsString.notNaked(shortcutName) || !ai.saiy.android.utils.UtilsString.notNaked(shortcutUri)) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onActivityResult: name or uri naked");
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onActivityResult: shortcutName: " + shortcutName);
                        MyLog.i(CLS_NAME, "onActivityResult: shortcutUri: " + shortcutUri);
                    }
                    showCustomCommandInputDialog(CCC.CUSTOM_AUTOMATE_FLOW, getApplicationContext().getString(R.string.content_automate_flow, shortcutName), shortcutUri, shortcutName, null);
                }
            }
            break;
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: DEFAULT");
                }
                break;
        }
    }

    public void showImportWarningDialog() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showImportWarningDialog");
        }
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setTitle(R.string.menu_import)
                .setMessage(R.string.content_import_warning)
                .setIcon(R.drawable.ic_image_unknown)
                .setPositiveButton(R.string.title_understood, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showImportWarningDialog: onPositive");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showImportWarningDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                })
                .create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
        materialDialog.show();
    }

    public void showExportWarningDialog() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showExportWarningDialog");
        }
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setTitle(R.string.menu_export)
                .setMessage(R.string.content_export_warning)
                .setIcon(R.drawable.ic_image_unknown)
                .setPositiveButton(R.string.title_understood, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showExportWarningDialog: onPositive");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showExportWarningDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                })
                .create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
        materialDialog.show();
    }

    public void showSoundEffectDialog() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setCancelable(false)
                .setView(R.layout.sound_effect_dialog_layout)
                .setTitle(R.string.menu_sound_effects)
                .setIcon(R.drawable.ic_music_square)
                .setNeutralButton(R.string.title_cool, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showSoundEffectDialog: onNeutral");
                        }
                        if (dialog instanceof AlertDialog) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    EditText editText = ((AlertDialog) dialog).getWindow().findViewById(R.id.etSoundEffect);
                                    if (editText.getText() == null || !ai.saiy.android.utils.UtilsString.notNaked(editText.getText().toString())) {
                                        return;
                                    }
                                    getParentActivity().speak(editText.getText().toString(), LocalRequest.ACTION_SPEAK_ONLY);
                                }
                            }).start();
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showSoundEffectDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    public void showUserNameDialog() {
        final String defaultName = getString(R.string.master);
        String userName = ai.saiy.android.utils.SPH.getUserName(getApplicationContext());
        if (!ai.saiy.android.utils.UtilsString.notNaked(userName)) {
            ai.saiy.android.utils.SPH.setUserName(getApplicationContext(), defaultName);
            userName = defaultName;
        }
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setView(R.layout.text_input_dialog_layout)
                .setTitle(R.string.menu_custom_name)
                .setMessage(R.string.custom_user_name)
                .setIcon(R.drawable.ic_person)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CharSequence charSequence = null;
                        if (dialog instanceof AlertDialog) {
                            final EditText editText = ((AlertDialog) dialog).findViewById(android.R.id.input);
                            charSequence = (editText == null) ? null : editText.getText();
                        }
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showUserNameDialog: input: " + charSequence);
                        }

                        if (charSequence == null) {
                            return;
                        }
                        final String nickName = charSequence.toString().trim();
                        if (!ai.saiy.android.utils.UtilsString.notNaked(nickName)) {
                            return;
                        }
                        ai.saiy.android.utils.SPH.setUserName(getApplicationContext(), nickName);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showUserNameDialog: onNegative");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showUserNameDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();

        final TextInputLayout textInputLayout = materialDialog.getWindow().findViewById(android.R.id.inputArea);
        final EditText editText = textInputLayout.findViewById(android.R.id.input);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setText(userName);
        editText.setHint(defaultName);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50) {}});
    }

    public void showCustomReplacementDialog() {
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
                            setReplacement(new CustomReplacement(newKeyphrase, newReplacement));
                        }
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
    }

    public void showCustomPhrasesDialog() {
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
                            setPhrase(new CustomPhrase(newKeyphrase, newResponse, ((CheckBox) ((AlertDialog) dialog).findViewById(R.id.cbVoiceRecognition)).isChecked()));
                        }
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
    }

    public void showCustomCommandsDialog() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setCancelable(false)
                .setTitle(R.string.content_custom_command)
                .setSingleChoiceItems(R.array.array_custom_commands, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomCommandsDialog: onSelection: " + which);
                        }
                        switch (which) {
                            case 0:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showCustomCommandsDialog: CUSTOM_DISPLAY_CONTACT");
                                }
                                showContactPicker(CUSTOM_DISPLAY_CONTACT_REQ_CODE);
                                break;
                            case 1:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showCustomCommandsDialog: CUSTOM_CALL_CONTACT");
                                }
                                showContactPicker(CUSTOM_CALL_CONTACT_REQ_CODE);
                                break;
                            case 2:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showCustomCommandsDialog: CUSTOM_OPEN_APPLICATION");
                                }
                                showApplicationPicker(APP_PICKER_REQ_CODE, ActivityApplicationPicker.ACCESSIBLE_APPLICATION_TYPE);
                                break;
                            case 3:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showCustomCommandsDialog: CUSTOM_LAUNCH_ACTIVITY");
                                }
                                showActivityPicker();
                                break;
                            case 4:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showCustomCommandsDialog: CUSTOM_TASKER_TASK");
                                }
                                final ai.saiy.android.thirdparty.tasker.TaskerHelper taskerHelper = new ai.saiy.android.thirdparty.tasker.TaskerHelper();
                                final Pair<Boolean, String> taskerPair = taskerHelper.isTaskerInstalled(getApplicationContext());
                                if (!taskerPair.first) {
                                    Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_TASKER_MARKET);
                                } else {
                                    final Pair<Boolean, Boolean> taskerStatusPair = taskerHelper.canInteract(getApplicationContext());
                                    if (!taskerStatusPair.first) {
                                        ai.saiy.android.applications.UtilsApplication.launchAppFromPackageName(getApplicationContext(), (String) taskerPair.second);
                                        toast(getString(R.string.error_tasker_enable), Toast.LENGTH_LONG);
                                    } else if (!taskerStatusPair.second) {
                                        taskerHelper.showTaskerExternalAccess(getApplicationContext());
                                        toast(getString(R.string.error_tasker_external_access), Toast.LENGTH_LONG);
                                    } else if (!taskerHelper.receiverExists(getApplicationContext())) {
                                        toast(getString(R.string.error_tasker_install_order), Toast.LENGTH_LONG);
                                    } else {
                                        showTaskPicker();
                                    }
                                }
                                break;
                            case 5:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showCustomCommandsDialog: CUSTOM_AUTOMATE_FLOW");
                                }
                                if (!ai.saiy.android.applications.Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_AUTOMATE)) {
                                    Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_AUTOMATE);
                                } else {
                                    showShortcutPicker(Installed.PACKAGE_AUTOMATE, AUTOMATE_FLOW_PICKER_REQ_CODE);
                                }
                                break;
                            case 6:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showCustomCommandsDialog: CUSTOM_APP_SHORTCUT");
                                }
                                showShortcutPicker(null, SHORTCUT_PICKER_REQ_CODE);
                                break;
                            case 7:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showCustomCommandsDialog: CUSTOM_APP_SEARCHABLE");
                                }
                                showApplicationPicker(SEARCHABLE_REQ_CODE, ActivityApplicationPicker.SEARCH_APPLICATION_TYPE);
                                break;
                            case 8:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showCustomCommandsDialog: CUSTOM_IOT_CONNECTION");
                                }
                                toast(getString(R.string.content_coming_soon_exclamation), Toast.LENGTH_SHORT);
                                break;
                            case 9:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showCustomCommandsDialog: CUSTOM_SEND_INTENT");
                                }
                                showCustomIntentDialog();
                                break;
                            case 10:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showCustomCommandsDialog: CUSTOM_HTTP");
                                }
                                showCustomHttpDialog();
                                break;
                            case 11:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showCustomCommandsDialog: CUSTOM_CAST");
                                }
                                toast(getString(R.string.content_coming_soon_exclamation), Toast.LENGTH_SHORT);
                                break;
                        }
                        switch (which) {
                            case 8:
                            case 11:
                                break;
                            default:
                                dialog.dismiss();
                                break;
                        }
                    }
                })
                .setIcon(R.drawable.ic_shape_plus)
                .setPositiveButton(R.string.menu_select, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showLanguageSelector: " + which);
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showLanguageSelector: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    /**
     * Utility method to ensure we double check the context being used.
     *
     * @return the application context
     */
    private Context getApplicationContext() {
        return parentFragment.getApplicationContext();
    }

    /**
     * Utility to return the parent activity neatly cast. No need for instanceOf as this
     * fragment helper will never be attached to another activity.
     *
     * @return the {@link ActivityHome} parent
     */

    public ActivityHome getParentActivity() {
        return parentFragment.getParentActivity();
    }

    /**
     * Utility method to return the parent fragment this helper is helping.
     *
     * @return the parent fragment
     */
    public FragmentCustomisation getParent() {
        return parentFragment;
    }
}

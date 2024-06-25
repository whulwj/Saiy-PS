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

import static android.widget.AdapterView.INVALID_POSITION;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.api.SaiyDefaults;
import ai.saiy.android.applications.Install;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.command.battery.BatteryInformation;
import ai.saiy.android.command.settings.SettingsIntent;
import ai.saiy.android.command.unknown.Unknown;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.thirdparty.tasker.TaskerHelper;
import ai.saiy.android.tts.helper.SaiyVoice;
import ai.saiy.android.tts.helper.TTSDefaults;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIMainAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentApplications;
import ai.saiy.android.ui.fragment.FragmentDiagnostics;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.ui.fragment.FragmentSettings;
import ai.saiy.android.utils.BluetoothConstants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsParcelable;
import ai.saiy.android.utils.UtilsString;

/**
 * Utility class to assist its parent fragment and avoid clutter there
 * <p>
 * Created by benrandall76@gmail.com on 25/07/2016.
 */
public class FragmentSettingsHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentSettingsHelper.class.getSimpleName();

    private final FragmentSettings parentFragment;
    private TextToSpeech tts;
    private String defaultTTSPackage;
    private final TextToSpeech.OnInitListener initListener = new TextToSpeech.OnInitListener() {
        @Override
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onInit: SUCCESS");
                }
                if (tts == null) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onInit: tts null");
                    }
                    shutdownTTS();
                    toast(getString(R.string.error_tts_initialisation), Toast.LENGTH_SHORT);
                    return;
                }
                Set<Voice> set = null;
                if (UtilsString.notNaked(defaultTTSPackage) && defaultTTSPackage.matches(TTSDefaults.TTS_PKG_NAME_GOOGLE)) {
                    try {
                        set = tts.getVoices();
                    } catch (NullPointerException e) {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "getVoices: NullPointerException");
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "getVoices: Exception");
                            e.printStackTrace();
                        }
                    }
                    receivedVoices(set);
                    return;
                }
                defaultTTSPackage = tts.getDefaultEngine();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onInit: defaultTTSPackage: " + defaultTTSPackage);
                }
                if (UtilsString.notNaked(defaultTTSPackage)) {
                    if (!defaultTTSPackage.matches(TTSDefaults.TTS_PKG_NAME_GOOGLE)) {
                        showGoogleDefaultDialog();
                        shutdownTTS();
                        return;
                    }
                    try {
                        set = tts.getVoices();
                    } catch (NullPointerException e) {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "getVoices: NullPointerException");
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "getVoices: Exception");
                            e.printStackTrace();
                        }
                    }
                    receivedVoices(set);
                    return;
                }
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onInit: defaultTTSPackage null");
                }
                defaultTTSPackage = reflectEngine();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "reflect: defaultTTSPackage: " + defaultTTSPackage);
                }
                if (!UtilsString.notNaked(defaultTTSPackage)) {
                    shutdownTTS();
                    toast(getString(R.string.error_tts_initialisation), Toast.LENGTH_SHORT);
                    return;
                }
                if (!defaultTTSPackage.matches(TTSDefaults.TTS_PKG_NAME_GOOGLE)) {
                    showGoogleDefaultDialog();
                    shutdownTTS();
                    return;
                }
                try {
                    set = tts.getVoices();
                } catch (NullPointerException e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "getVoices: NullPointerException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "getVoices: Exception");
                        e.printStackTrace();
                    }
                }
                receivedVoices(set);
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onInit: ERROR");
                }
                shutdownTTS();
                toast(getString(R.string.error_tts_initialisation), Toast.LENGTH_SHORT);
            }
        }
    };

    /**
     * Constructor
     *
     * @param parentFragment the parent fragment for this helper class
     */
    public FragmentSettingsHelper(@NonNull final FragmentSettings parentFragment) {
        this.parentFragment = parentFragment;
    }

    private String getString(@StringRes int resId) {
        return getApplicationContext().getString(resId);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setVoice(Voice voice) {
        if (this.tts == null) {
            shutdownTTS();
            toast(getString(R.string.error_tts_initialisation), Toast.LENGTH_SHORT);
            return;
        }
        switch (this.tts.setVoice(voice)) {
            case TextToSpeech.ERROR:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "setVoice: ERROR");
                }
                toast("Voice error!", Toast.LENGTH_SHORT);
                break;
            case TextToSpeech.SUCCESS:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoice: SUCCESS");
                }
                switch (this.tts.speak("Testing testing 1 2 3", TextToSpeech.QUEUE_FLUSH, new Bundle(), getApplicationContext().getPackageName())) {
                    case TextToSpeech.ERROR:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "speak: ERROR");
                        }
                        toast("Speech error!", Toast.LENGTH_SHORT);
                        break;
                    case TextToSpeech.SUCCESS:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "speak: SUCCESS");
                        }
                        break;
                    default:
                        break;
                }
            default:
                break;
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void showVoiceSelector(final Set<Voice> set) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Iterator<Voice> it = set.iterator();
                while (it.hasNext()) {
                    Voice voice = it.next();
                    if (!ai.saiy.android.utils.UtilsLocale.localesLanguageMatch(voice.getLocale(), SupportedLanguage.ENGLISH.getLocale())) {
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "removing locale: " + voice.getLocale().toString());
                        }
                        it.remove();
                    } else if (DEBUG) {
                        MyLog.d(CLS_NAME, "keeping locale: " + voice.getLocale().toString());
                    }
                }
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "post supported language check: " + set.size());
                }
                if (set.isEmpty()) {
                    shutdownTTS();
                    toast(getString(R.string.diagnostics_no_tts_sl), Toast.LENGTH_LONG);
                    SettingsIntent.settingsIntent(getApplicationContext(), SettingsIntent.Type.TTS);
                    return;
                }
                final ArrayList<Voice> voices = new ArrayList<>(set);
                Collections.sort(voices, new SaiyVoice.VoiceComparator());
                if (DEBUG) {
                    for (Voice voice : voices) {
                        MyLog.d("Voice: ", voice.toString());
                    }
                }
                final ArrayList<String> namesOfVoice = new ArrayList<>(voices.size());
                for (Voice voice : voices) {
                    namesOfVoice.add(voice.getName());
                }
                String defaultSaiyVoiceString = ai.saiy.android.utils.SPH.getDefaultTTSVoice(getApplicationContext());
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "defaultSaiyVoiceString: " + defaultSaiyVoiceString);
                }
                int checkedIndex = 0;
                if (ai.saiy.android.utils.UtilsString.notNaked(defaultSaiyVoiceString)) {
                    SaiyVoice saiyVoice = UtilsParcelable.unmarshall(defaultSaiyVoiceString, SaiyVoice.CREATOR);
                    if (saiyVoice != null) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "defaultSaiyVoice: " + saiyVoice.getName());
                        }
                        for (int i = 0; i < namesOfVoice.size(); ++i) {
                            if (namesOfVoice.get(i).matches("(?i)" + Pattern.quote(saiyVoice.getName()))) {
                                checkedIndex = i;
                                break;
                            }
                        }
                    } else if (DEBUG) {
                        MyLog.w(CLS_NAME, "defaultSaiyVoice: null");
                    }
                }

                if (DEBUG) {
                    MyLog.d(CLS_NAME, "finalCheckedIndex: " + checkedIndex);
                }
                final int finalCheckedIndex = checkedIndex;
                getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                                .setCancelable(false)
                                .setTitle(R.string.menu_tts_voice)
                                .setMessage("Test & select a voice")
                                .setIcon(R.drawable.ic_voice_over)
                                .setSingleChoiceItems(namesOfVoice.toArray(new String[0]), finalCheckedIndex, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showVoiceSelector: onSelection: " + which + ": " + namesOfVoice.get(which));
                                        }
                                        if (voices.get(which).isNetworkConnectionRequired()) {
                                            toast("Requires network", Toast.LENGTH_SHORT);
                                        }
                                    }
                                })
                                .setNeutralButton("Sample", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dialog instanceof AlertDialog) {
                                            final int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "showVoiceSelector: onNeutral: " + voices.get(position));
                                            }
                                            setVoice(voices.get(position));
                                        }
                                    }
                                })
                                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dialog instanceof AlertDialog) {
                                            final int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "showVoiceSelector: onPositive: " + position);
                                            }
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Voice voice = voices.get(position);
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "showVoiceSelector: onPositive: " + voice.toString());
                                                    }
                                                    TTSDefaults.Google[] values = TTSDefaults.Google.values();
                                                    SaiyVoice saiyVoice = new SaiyVoice(voice);
                                                    saiyVoice.setEngine(TTSDefaults.TTS_PKG_NAME_GOOGLE);
                                                    String quote = Pattern.quote(voice.getName());
                                                    for (TTSDefaults.Google google : values) {
                                                        if (google.getVoiceName().matches("(?i)" + quote)) {
                                                            saiyVoice.setGender(google.getGender());
                                                            break;
                                                        }
                                                    }
                                                    String base64String = UtilsParcelable.parcelable2String(saiyVoice);
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "setDefaultVoice: base64String: " + base64String);
                                                    }
                                                    SPH.setDefaultTTSVoice(getApplicationContext(), base64String);
                                                }
                                            }).start();
                                            shutdownTTS();
                                        }
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showVoiceSelector: onNegative");
                                        }
                                        dialog.dismiss();
                                        shutdownTTS();
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showVoiceSelector: onCancel");
                                        }
                                        dialog.dismiss();
                                    }
                                }).create();
                        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
                        materialDialog.show();
                        showProgress(false);
                    }
                });
            }
        });
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

    public void showProgress(boolean visible) {
        if (getParent().isActive()) {
            getParentActivity().showProgress(visible);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void receivedVoices(Set<Voice> set) {
        if (set != null && !set.isEmpty()) {
            showVoiceSelector(set);
            return;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "receivedVoices: naked");
        }
        shutdownTTS();
        toast(getString(R.string.diagnostics_no_tts_sl), Toast.LENGTH_LONG);
        SettingsIntent.settingsIntent(getApplicationContext(), SettingsIntent.Type.TTS);
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

        ContainerUI containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_supported_languages));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_options));
        containerUI.setIconMain(R.drawable.ic_language);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_tts_voice));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_options));
        containerUI.setIconMain(R.drawable.ic_voice_over);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_unknown_commands));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_options));
        containerUI.setIconMain(R.drawable.ic_help_circle);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_bluetooth_headsets));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_options));
        containerUI.setIconMain(R.drawable.ic_bluetooth_connect);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_volume_settings));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_set));
        containerUI.setIconMain(R.drawable.ic_volume_high);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_alexa_shortcut));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_alexa);
        containerUI.setIconExtra(SPH.showAlexaNotification(getApplicationContext())
                ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_launcher_shortcut));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_create));
        containerUI.setIconMain(R.drawable.ic_link);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_synthesised_voice));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_google);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_temperature_units));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_options));
        containerUI.setIconMain(R.drawable.ic_thermometer);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
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

        final RecyclerView mRecyclerView = parent.findViewById(R.id.layout_common_fragment_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getParentActivity(), null));

        return mRecyclerView;
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
     * Update the parent fragment with the UI components
     */
    public void finaliseUI() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ContainerUI> tempArray = getUIComponents();

                if (getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)) {

                    try {
                        Thread.sleep(FragmentHome.DRAWER_CLOSE_DELAY);
                    } catch (final InterruptedException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "finaliseUI InterruptedException");
                            e.printStackTrace();
                        }
                    }
                }

                if (getParent().isActive()) {

                    getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            getParent().getObjects().addAll(tempArray);
                            getParent().getAdapter().notifyItemRangeInserted(0, getParent().getObjects().size());
                        }
                    });

                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "finaliseUI Fragment detached");
                    }
                }
            }
        });
    }

    public void showVoicesDialog() {
        final boolean needDiagnostics = SPH.getRunDiagnostics(getApplicationContext());
        if (needDiagnostics && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getVoices();
            return;
        }
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(getParentActivity())
                .setTitle(R.string.menu_tts_voice)
                .setIcon(R.drawable.ic_voice_over);
        dialogBuilder.setNeutralButton(R.string.title_diagnostics, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "showVoicesDialog: onNeutral");
                }
                dialog.dismiss();
                if (getParent().isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_DIAGNOSTICS))) {
                    getParentActivity().doFragmentAddTransaction(FragmentDiagnostics.newInstance(null), String.valueOf(ActivityHome.INDEX_FRAGMENT_DIAGNOSTICS), ActivityHome.ANIMATION_FADE, ActivityHome.MENU_INDEX_SETTINGS);
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "onClick: INDEX_FRAGMENT_DIAGNOSTICS being added");
                }
            }
        });
        dialogBuilder.setPositiveButton(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP? R.string.title_voice:R.string.title_doh_, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "showVoicesDialog: onPositive");
                }
                dialog.dismiss();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getVoices();
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialogBuilder.setMessage(R.string.diagnostics_voice_dialog_content_suggest); //message
        } else {
            String suffix;
            if (needDiagnostics) {
                suffix = getString(R.string.diagnostics_voice_dialog_content_suggest_settings);
            } else {
                suffix = getString(R.string.diagnostics_voice_dialog_content_suggest_diagnostics);
            }
            dialogBuilder.setMessage(getString(R.string.diagnostics_voice_dialog_content_unsupported) + suffix);
            dialogBuilder.setNegativeButton(needDiagnostics ? R.string.title_settings:android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "showVoicesDialog: onNegative");
                    }
                    dialog.dismiss();
                    if (needDiagnostics) {
                        SettingsIntent.settingsIntent(getApplicationContext(), SettingsIntent.Type.DEVICE);
                    }
                }
            });
        }
        final AlertDialog materialDialog = dialogBuilder.create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
        materialDialog.show();
    }

    /**
     * Show the temperature units selector
     */
    @SuppressWarnings("ConstantConditions")
    public void showTemperatureUnitsSelector() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final String[] units = getParent().getResources().getStringArray(R.array.array_temperature_units);

                for (int i = 0; i < units.length; i++) {
                    units[i] = StringUtils.capitalize(units[i]);
                }

                getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                                .setCancelable(false)
                                .setTitle(R.string.menu_temperature_units)
                                .setIcon(R.drawable.ic_thermometer)
                                .setSingleChoiceItems(units, SPH.getDefaultTemperatureUnits(getApplicationContext()), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showTemperatureUnitsSelector: onSelection: " + which + ": " + units[which]);
                                        }
                                    }
                                })

                                .setPositiveButton(R.string.menu_select, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dialog instanceof AlertDialog) {
                                            switch (((AlertDialog) dialog).getListView().getCheckedItemPosition()) {

                                                case BatteryInformation.CELSIUS:
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "showTemperatureUnitsSelector: onPositive: CELSIUS");
                                                    }
                                                    SPH.setDefaultTemperatureUnits(getApplicationContext(),
                                                            BatteryInformation.CELSIUS);
                                                    break;
                                                case BatteryInformation.FAHRENHEIT:
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "showTemperatureUnitsSelector: onPositive: FAHRENHEIT");
                                                    }
                                                    SPH.setDefaultTemperatureUnits(getApplicationContext(),
                                                            BatteryInformation.FAHRENHEIT);
                                                    break;

                                            }
                                        }
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showTemperatureUnitsSelector: onNegative");
                                        }
                                        dialog.dismiss();
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showTemperatureUnitsSelector: onCancel");
                                        }
                                        dialog.dismiss();
                                    }
                                }).create();

                        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
                        materialDialog.show();
                    }
                });
            }
        });
    }

    public void showHeadsetOverviewDialog() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showHeadsetOverviewDialog");
        }
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setTitle(R.string.menu_headset_woes)
                .setMessage(R.string.content_headset_overview)
                .setIcon(R.drawable.ic_bluetooth_connect)
                .setPositiveButton(R.string.menu_grey_hairs, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showHeadsetOverviewDialog: onPositive");
                        }
                        dialog.dismiss();
                        showHeadsetDialog();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showHeadsetOverviewDialog: onNegative");
                        }
                        dialog.dismiss();
                        showHeadsetDialog();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showHeadsetOverviewDialog: onCancel");
                        }
                        dialog.dismiss();
                        showHeadsetDialog();
                    }
                })
                .create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
        materialDialog.show();
    }

    public void showHeadsetDialog() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setView(R.layout.bluetooth_headsets_dialog_layout)
                .setCancelable(false)
                .setTitle(R.string.menu_bluetooth_headsets)
                .setIcon(R.drawable.ic_bluetooth_connect)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showHeadsetDialog: onPositive");
                        }
                        if (dialog instanceof AlertDialog) {
                            int checkedRadioButtonId = ((RadioGroup) ((AlertDialog) dialog).getWindow().findViewById(R.id.rgStreamType)).getCheckedRadioButtonId();
                            if (R.id.rbStreamCommunication == checkedRadioButtonId) {
                                SPH.setHeadsetStreamType(getApplicationContext(), BluetoothConstants.STREAM_COMMUNICATION);
                            } else if (R.id.rbStreamCall == checkedRadioButtonId) {
                                SPH.setHeadsetStreamType(getApplicationContext(), BluetoothConstants.STREAM_CALL);
                            } else if (R.id.rbStreamVoiceCall == checkedRadioButtonId) {
                                SPH.setHeadsetStreamType(getApplicationContext(), BluetoothConstants.STREAM_VOICE_CALL);
                            }
                            checkedRadioButtonId = ((RadioGroup) ((AlertDialog) dialog).getWindow().findViewById(R.id.rgConnectionType)).getCheckedRadioButtonId();
                            if (R.id.rbConnectionTypeA2DP == checkedRadioButtonId) {
                                SPH.setHeadsetConnectionType(getApplicationContext(), BluetoothConstants.CONNECTION_A2DP);
                            } else if (R.id.rbConnectionSCO == checkedRadioButtonId) {
                                SPH.setHeadsetConnectionType(getApplicationContext(), BluetoothConstants.CONNECTION_SCO);
                            }
                            checkedRadioButtonId = ((RadioGroup) ((AlertDialog) dialog).getWindow().findViewById(R.id.rgSystem)).getCheckedRadioButtonId();
                            if (R.id.rbSystem1 == checkedRadioButtonId) {
                                SPH.setHeadsetSystem(getApplicationContext(), BluetoothConstants.SYSTEM_ONE);
                            } else if (R.id.rbSystem2 == checkedRadioButtonId) {
                                SPH.setHeadsetSystem(getApplicationContext(), BluetoothConstants.SYSTEM_TWO);
                            } else if (R.id.rbSystem3 == checkedRadioButtonId) {
                                SPH.setHeadsetSystem(getApplicationContext(), BluetoothConstants.SYSTEM_THREE);
                            }
                            SPH.setAutoConnectHeadset(getApplicationContext(), ((CheckBox) ((AlertDialog) dialog).getWindow().findViewById(R.id.cbAutoConnect)).isChecked());
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showHeadsetDialog: onNegative");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showHeadsetDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();

        RadioGroup radioGroup = materialDialog.findViewById(R.id.rgSystem);
        switch (SPH.getHeadsetSystem(getApplicationContext())) {
            case BluetoothConstants.SYSTEM_ONE:
                radioGroup.check(R.id.rbSystem1);
                break;
            case BluetoothConstants.SYSTEM_TWO:
                radioGroup.check(R.id.rbSystem2);
                break;
            case BluetoothConstants.SYSTEM_THREE:
                radioGroup.check(R.id.rbSystem3);
                break;
        }
        radioGroup = materialDialog.findViewById(R.id.rgStreamType);
        switch (SPH.getHeadsetStreamType(getApplicationContext())) {
            case BluetoothConstants.STREAM_COMMUNICATION:
                radioGroup.check(R.id.rbStreamCommunication);
                break;
            case BluetoothConstants.STREAM_CALL:
                radioGroup.check(R.id.rbStreamCall);
                break;
            case BluetoothConstants.STREAM_VOICE_CALL:
                radioGroup.check(R.id.rbStreamVoiceCall);
                break;
        }
        radioGroup = materialDialog.findViewById(R.id.rgConnectionType);
        switch (SPH.getHeadsetConnectionType(getApplicationContext())) {
            case BluetoothConstants.CONNECTION_A2DP:
                radioGroup.check(R.id.rbConnectionTypeA2DP);
                break;
            case BluetoothConstants.CONNECTION_SCO:
                radioGroup.check(R.id.rbConnectionSCO);
                break;
        }
        ((CheckBox) materialDialog.findViewById(R.id.cbAutoConnect)).setChecked(SPH.isAutoConnectHeadset(getApplicationContext()));
    }

    /**
     * Show the unknown command action selector
     */
    @SuppressWarnings("ConstantConditions")
    public void showUnknownCommandSelector() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final String[] actions = getParent().getResources().getStringArray(R.array.array_unknown_action);

                for (int i = 0; i < actions.length; i++) {

                    switch (i) {

                    case Unknown.UNKNOWN_STATE:
                    case Unknown.UNKNOWN_REPEAT:
                         break;
                    case Unknown.UNKNOWN_GOOGLE_SEARCH:
                    case Unknown.UNKNOWN_ALEXA:
                    case Unknown.UNKNOWN_MICROSOFT_CORTANA:
                    case Unknown.UNKNOWN_WOLFRAM_ALPHA:
                    case Unknown.UNKNOWN_TASKER:
                            actions[i] = getParent().getString(R.string.menu_send_to) + " " + actions[i];
                        break;
                    }
            }

                final ArrayList<Integer> disabledIndicesList = new ArrayList<>();
                if (!Installed.isPackageInstalled(getApplicationContext(),
                        Installed.PACKAGE_MICROSOFT_CORTANA)) {
                    disabledIndicesList.add(Unknown.UNKNOWN_MICROSOFT_CORTANA);
                }
                if (!Installed.isPackageInstalled(getApplicationContext(),
                        Installed.PACKAGE_WOLFRAM_ALPHA)) {
                    disabledIndicesList.add(Unknown.UNKNOWN_WOLFRAM_ALPHA);
                }
                if (!new TaskerHelper().isTaskerInstalled(getApplicationContext()).first) {
                    disabledIndicesList.add(Unknown.UNKNOWN_TASKER);
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    disabledIndicesList.add(Unknown.UNKNOWN_ALEXA);
                }

                getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final int defaultIndex = SPH.getCommandUnknownAction(getApplicationContext());
                        int checkedItem = INVALID_POSITION;
                        final List<String> items = new ArrayList<>();
                        for (int i = 0, j = 0; i < actions.length; i++, j++) {
                            if (disabledIndicesList.contains(i)) {
                                continue;
                            }
                            items.add(actions[i]);
                            if (Unknown.UNKNOWN_ALEXA == i && !ai.saiy.android.amazon.TokenHelper.hasToken(getApplicationContext())) {
                                continue;
                            }
                            if (i == defaultIndex) {
                                checkedItem = j;
                            }
                        }
                        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                        .setCancelable(false)
                        .setTitle(R.string.content_unknown_command)
                        .setIcon(R.drawable.ic_help_circle)
                        .setSingleChoiceItems(items.toArray(new String[0]), checkedItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUnknownCommandSelector: onSelection: " + which + ": " + items.get(which));
                                }
                            }
                        })

                                .setPositiveButton(R.string.menu_select, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dialog instanceof AlertDialog) {
                                            final int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                            int selectedIndex = INVALID_POSITION;
                                            if (position != INVALID_POSITION) {
                                                for (int i = 0; i < actions.length; i++) {
                                                    if (TextUtils.equals(actions[i], items.get(position))) {
                                                        selectedIndex = i;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "showUnknownCommandSelector: onPositive: " + selectedIndex);
                                            }

                                            if (Unknown.UNKNOWN_ALEXA != selectedIndex || ai.saiy.android.amazon.TokenHelper.hasToken(getApplicationContext())) {
                                                SPH.setCommandUnknownAction(getApplicationContext(),
                                                        (selectedIndex == INVALID_POSITION) ? Unknown.UNKNOWN_STATE : selectedIndex);
                                                SPH.setToastUnknown(getApplicationContext(), selectedIndex <= Unknown.UNKNOWN_REPEAT);
                                                if (Unknown.UNKNOWN_ALEXA != selectedIndex) {
                                                    SPH.setDefaultRecognition(getApplicationContext(), SaiyDefaults.VR.NATIVE);
                                                }
                                            } else {
                                                getParentActivity().speak(R.string.amazon_auth_request, LocalRequest.ACTION_SPEAK_ONLY);
                                                if (getParent().isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_SUPPORTED_APPS))) {
                                                    getParentActivity().doFragmentAddTransaction(FragmentApplications.newInstance(null), String.valueOf(ActivityHome.INDEX_FRAGMENT_SUPPORTED_APPS), ActivityHome.ANIMATION_FADE, ActivityHome.INDEX_FRAGMENT_SETTINGS);
                                                } else if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "onClick: INDEX_FRAGMENT_SUPPORTED_APPS being added");
                                                }
                                            }
                                        }
                                        dialog.dismiss();
                                    }
                                })

                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showUnknownCommandSelector: onNegative");
                                        }
                                        dialog.dismiss();
                                    }
                                })

                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showUnknownCommandSelector: onCancel");
                                        }

                                        dialog.dismiss();
                                    }
                                }).create();

                        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
                        materialDialog.show();
                    }
                });
            }
        });
    }

    /**
     * Show the pause detection slider
     */
    @SuppressWarnings("ConstantConditions")
    public void showVolumeSettingsSlider() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setView(R.layout.tts_volume_dialog_layout)
                .setCancelable(false)
                .setTitle(R.string.menu_volume_settings)
                .setIcon(R.drawable.ic_volume_high)
                .setNeutralButton(R.string.text_default, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog instanceof AlertDialog) {
                            ((SeekBar) ((AlertDialog) dialog).findViewById(R.id.volumeSeekBar))
                                    .setProgress(4);
                            ((CheckBox) ((AlertDialog) dialog).findViewById(R.id.cbSystemManagedVolume)).setChecked(true);
                            ((CheckBox) ((AlertDialog) dialog).findViewById(R.id.cbGlobalVolume)).setChecked(true);
                            ((CheckBox) ((AlertDialog) dialog).findViewById(R.id.cbToastVolume)).setChecked(true);
                        }
                    }
                })
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog instanceof AlertDialog) {
                            final int volume = ((SeekBar) ((AlertDialog) dialog).findViewById(R.id.volumeSeekBar))
                                    .getProgress() * 10 - 40;

                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "showVolumeSettingsSlider: onPositive: setting: " + volume);
                            }

                            SPH.setTTSVolume(getApplicationContext(), volume);
                            SPH.setAssumeGlobalVolume(getApplicationContext(), ((CheckBox) ((AlertDialog) dialog).findViewById(R.id.cbGlobalVolume)).isChecked());
                            SPH.setToastVolumeWarnings(getApplicationContext(), ((CheckBox) ((AlertDialog) dialog).findViewById(R.id.cbToastVolume)).isChecked());
                            SPH.setSystemManagedVolume(getApplicationContext(), ((CheckBox) ((AlertDialog) dialog).findViewById(R.id.cbSystemManagedVolume)).isChecked());
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showVolumeSettingsSlider: onNegative");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showVolumeSettingsSlider: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();

        final int userVolume = SPH.getTTSVolume(getApplicationContext());
        final TextView seekText = materialDialog.findViewById(R.id.volumeSeekBarText);
        final SeekBar seekbar = materialDialog.findViewById(R.id.volumeSeekBar);

        switch (userVolume) {
            case -40:
                seekText.setText("40% " + getParent().getString(R.string.below) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(0);
                break;
            case -30:
                seekText.setText("30% " + getParent().getString(R.string.below) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(1);
                break;
            case -20:
                seekText.setText("20% " + getParent().getString(R.string.below) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(2);
                break;
            case -10:
                seekText.setText("10% " + getParent().getString(R.string.below) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(3);
                break;
            case 0:
                seekText.setText(StringUtils.capitalize(getParent().getString(R.string.adhere_to)) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(4);
                break;
            case 10:
                seekText.setText("10% " + getParent().getString(R.string.above) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(5);
                break;
            case 20:
                seekText.setText("20% " + getParent().getString(R.string.above) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(6);
                break;
            case 30:
                seekText.setText("30% " + getParent().getString(R.string.above) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(7);
                break;
            case 40:
                seekText.setText("40% " + getParent().getString(R.string.above) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(8);
        }

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {

                switch (progress) {
                    case 0:
                        seekText.setText("40% " + getParent().getString(R.string.below) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 1:
                        seekText.setText("30% " + getParent().getString(R.string.below) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 2:
                        seekText.setText("20% " + getParent().getString(R.string.below) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 3:
                        seekText.setText("10% " + getParent().getString(R.string.below) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 4:
                        seekText.setText(StringUtils.capitalize(getParent().getString(R.string.adhere_to))
                                + " " + getParent().getString(R.string.media_stream));
                        break;
                    case 5:
                        seekText.setText("10% " + getParent().getString(R.string.above) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 6:
                        seekText.setText("20% " + getParent().getString(R.string.above) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 7:
                        seekText.setText("30% " + getParent().getString(R.string.above) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 8:
                        seekText.setText("40% " + getParent().getString(R.string.above) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }
        });
        ((CheckBox) materialDialog.getWindow().findViewById(R.id.cbGlobalVolume)).setChecked(SPH.getAssumeGlobalVolume(getApplicationContext()));
        ((CheckBox) materialDialog.getWindow().findViewById(R.id.cbToastVolume)).setChecked(SPH.getToastVolumeWarnings(getApplicationContext()));
        ((CheckBox) materialDialog.getWindow().findViewById(R.id.cbSystemManagedVolume)).setChecked(SPH.getSystemManagedVolume(getApplicationContext()));
    }

    public void showNetworkSynthesisDialog() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final String[] stringArray = getParent().getResources().getStringArray(R.array.array_google_synthesised_voice);
                String[] networkSynthesis;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // The last one not available
                    networkSynthesis = new String[stringArray.length - 1];
                    System.arraycopy(stringArray, 0, networkSynthesis, 0, stringArray.length - 1);
                } else {
                    networkSynthesis = stringArray;
                }
                final boolean[] checkedItems = new boolean[networkSynthesis.length];
                if (SPH.getNetworkSynthesis(getApplicationContext())) {
                    checkedItems[0] = true;
                }
                if (SPH.isNetworkSynthesisWifi(getApplicationContext())) {
                    checkedItems[1] = true;
                }
                if (SPH.isNetworkSynthesis4g(getApplicationContext())) {
                    checkedItems[2] = true;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && SPH.isCacheSpeech(getApplicationContext())) {
                    checkedItems[3] = true;
                }
                getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                                .setCancelable(false)
                                .setTitle(R.string.menu_network_synthesis)
                                .setMessage(R.string.synthesis_intro_text)
                                .setIcon(R.drawable.ic_google)
                                .setMultiChoiceItems(stringArray, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showNetworkSynthesisDialog: onSelection: " + which + ", " + isChecked);
                                        }
                                    }
                                })
                                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showNetworkSynthesisDialog: onPositive");
                                        }
                                        if (dialog instanceof AlertDialog) {
                                            final List<Integer> selectedIndices = new ArrayList<>();
                                            for (int i = 0; i < checkedItems.length; ++i) {
                                                if (checkedItems[i]) {
                                                    selectedIndices.add(i);
                                                }
                                            }
                                            final Integer[] selected = selectedIndices.toArray(new Integer[0]);
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "showNetworkSynthesisDialog: onPositive: length: " + selected.length);
                                                for (Integer num : selected) {
                                                    MyLog.i(CLS_NAME, "showNetworkSynthesisDialog: onPositive: " + num);
                                                }
                                            }
                                            SPH.setNetworkSynthesis(getApplicationContext(), ArrayUtils.contains(selected, 0));
                                            SPH.setNetworkSynthesisWifi(getApplicationContext(), ArrayUtils.contains(selected, 1) || ArrayUtils.contains(selected, 2));
                                            SPH.setNetworkSynthesis4g(getApplicationContext(), ArrayUtils.contains(selected, 2));
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                SPH.setCacheSpeech(getApplicationContext(), ArrayUtils.contains(selected, 3));
                                            }
                                        }
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showNetworkSynthesisDialog: onNegative");
                                        }
                                        dialog.dismiss();
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showNetworkSynthesisDialog: onCancel");
                                        }
                                        dialog.dismiss();
                                    }
                                }).create();
                        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
                        materialDialog.show();
                    }
                });
            }
        });
    }

    private void showNoEnginesToast() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showNoEnginesToast");
        }
        showProgress(false);
        toast(getString(R.string.diagnostics_install_tts), Toast.LENGTH_LONG);
        Install.showInstallLink(getApplicationContext(), TTSDefaults.TTS_PKG_NAME_GOOGLE);
    }

    private void showGoogleDefaultDialog() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showGoogleDefaultDialog");
        }
        showProgress(false);
        getParentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                        .setTitle(R.string.title_google_tts_engine)
                        .setMessage(R.string.diagnostics_only_google_supported)
                        .setIcon(R.drawable.ic_google)
                        .setPositiveButton(R.string.title_settings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showVoiceSelector: onPositive");
                                }
                                dialog.dismiss();
                                SettingsIntent.settingsIntent(getApplicationContext(), SettingsIntent.Type.TTS);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showVoiceSelector: onNegative");
                                }
                                dialog.dismiss();
                            }
                        })
                        .create();
                materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
                materialDialog.show();
            }
        });
    }

    private void showOnlyGoogleSupportedDialog() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showOnlyGoogleSupportedDialog");
        }
        showProgress(false);
        getParentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                        .setTitle(R.string.title_google_tts_engine)
                        .setMessage(R.string.diagnostics_install_google)
                        .setIcon(R.drawable.ic_google)
                        .setPositiveButton(R.string.title_install, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showOnlyGoogleSupportedDialog: onPositive");
                                }
                                dialog.dismiss();
                                Install.showInstallLink(getApplicationContext(), TTSDefaults.TTS_PKG_NAME_GOOGLE);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showOnlyGoogleSupportedDialog: onNegative");
                                }
                                dialog.dismiss();
                            }
                        })
                        .create();
                materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
                materialDialog.show();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void getVoices() {
        showProgress(true);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                List<ResolveInfo> queryIntentActivities = getParentActivity().getPackageManager().queryIntentActivities(intent, PackageManager.GET_META_DATA);
                if (!ai.saiy.android.utils.UtilsList.notNaked(queryIntentActivities)) {
                    showNoEnginesToast();
                    return;
                }
                boolean haveGoogleTTS = false;
                for (ResolveInfo resolveInfo: queryIntentActivities) {
                    if (resolveInfo.activityInfo.applicationInfo.packageName.matches(TTSDefaults.TTS_PKG_NAME_GOOGLE)) {
                        haveGoogleTTS = true;
                        break;
                    }
                }
                if (!haveGoogleTTS) {
                    showOnlyGoogleSupportedDialog();
                    return;
                }
                defaultTTSPackage = getDefaultTTSPackage();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getVoices: defaultTTSPackage: " + defaultTTSPackage);
                }
                if (!ai.saiy.android.utils.UtilsString.notNaked(defaultTTSPackage)) {
                    tts = new TextToSpeech(getApplicationContext(), initListener, TTSDefaults.TTS_PKG_NAME_GOOGLE);
                } else if (!defaultTTSPackage.matches(TTSDefaults.TTS_PKG_NAME_GOOGLE)) {
                    showGoogleDefaultDialog();
                } else {
                    tts = new TextToSpeech(getApplicationContext(), initListener, TTSDefaults.TTS_PKG_NAME_GOOGLE);
                }
            }
        });
    }

    private String getDefaultTTSPackage() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getDefaultTTSPackage");
        }
        return Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.TTS_DEFAULT_SYNTH);
    }

    private String reflectEngine() {
        try {
            Field declaredField = this.tts.getClass().getSuperclass().getDeclaredField("mCurrentEngine");
            declaredField.setAccessible(true);
            return (String) declaredField.get(this.tts);
        } catch (IllegalAccessException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "IllegalAccessException");
                e.printStackTrace();
            }
            return null;
        } catch (NoSuchFieldException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NoSuchFieldException");
                e.printStackTrace();
            }
            return null;
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            }
            return null;
        }
    }

    private void shutdownTTS() {
        showProgress(false);
        if (this.tts != null) {
            this.tts.shutdown();
            this.tts = null;
        }
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
    public FragmentSettings getParent() {
        return parentFragment;
    }
}

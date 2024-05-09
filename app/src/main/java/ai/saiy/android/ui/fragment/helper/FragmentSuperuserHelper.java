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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.Pair;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.saiy.android.R;
import ai.saiy.android.api.helper.BlackListHelper;
import ai.saiy.android.applications.Install;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.permissions.PermissionHelper;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.service.helper.SelfAwareConditions;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIMainAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.ui.fragment.FragmentSuperUser;
import ai.saiy.android.user.ISaiyAccount;
import ai.saiy.android.user.SaiyAccount;
import ai.saiy.android.user.SaiyAccountHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 25/07/2016.
 */

public class FragmentSuperuserHelper implements ISaiyAccount {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentSuperuserHelper.class.getSimpleName();

    private static final long ARBITRARY_DELAY = 7000;
    private final AtomicBoolean accountInitialised = new AtomicBoolean();
    public final AtomicBoolean enrollmentCancelled = new AtomicBoolean();

    private final FragmentSuperUser parentFragment;
    private volatile Timer timer;

    /**
     * Constructor
     *
     * @param parentFragment the parent fragment for this helper class
     */
    public FragmentSuperuserHelper(@NonNull final FragmentSuperUser parentFragment) {
        this.parentFragment = parentFragment;
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
        containerUI.setTitle(getParent().getString(R.string.menu_root));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_linux);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_start_boot));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_fan);
        containerUI.setIconExtra(SPH.getStartAtBoot(getApplicationContext())
                ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_vocal_verification));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_account_key);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_ping));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_radio_tower);
        containerUI.setIconExtra(SPH.getPingCheck(getApplicationContext())
                ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_blacklist));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_traffic_light);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_memory_usage));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_memory);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_intercept_google));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_google);
        containerUI.setIconExtra(SPH.getInterceptGoogle(getApplicationContext())
                ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_algorithms));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_function);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_recogniser_busy_fix));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_block_helper);
        containerUI.setIconExtra(SPH.getRecogniserBusyFix(getApplicationContext())
                ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_okay_google_fix));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_google);
        containerUI.setIconExtra(SPH.getOkayGoogleFix(getApplicationContext())
                ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
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

        final RecyclerView mRecyclerView = (RecyclerView) parent.findViewById(R.id.layout_common_fragment_recycler_view);
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
                final ArrayList<ContainerUI> tempArray = FragmentSuperuserHelper.this.getUIComponents();

                try {
                    Thread.sleep(FragmentHome.DRAWER_CLOSE_DELAY);
                } catch (final InterruptedException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "finaliseUI InterruptedException");
                        e.printStackTrace();
                    }
                }

                if (FragmentSuperuserHelper.this.getParent().isActive()) {

                    FragmentSuperuserHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            FragmentSuperuserHelper.this.getParent().getObjects().addAll(tempArray);
                            FragmentSuperuserHelper.this.getParent().getAdapter().notifyItemRangeInserted(0, FragmentSuperuserHelper.this.getParent().getObjects().size());
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

    /**
     * Show the Blacklist selector
     *
     * @return true if the applications installed to populate the selector, false otherwise
     */
    public boolean showBlackListSelector() {

        final BlackListHelper blackListHelper = new BlackListHelper();
        final ArrayList<String> blackListArray = blackListHelper.fetch(getApplicationContext());
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showBlackListSelector: blackListArray: " + blackListArray.size());
        }

        final ArrayList<Pair<String, String>> installedPackages = Installed.declaresSaiyPermission(
                getApplicationContext());
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showBlackListSelector: installedPackages: " + installedPackages.size());
        }

        if (!UtilsList.notNaked(installedPackages)) {
            return false;
        }

        final ArrayList<String> appNames = new ArrayList<>(installedPackages.size());
        final ArrayList<Integer> selectedList = new ArrayList<>();

        for (final Pair appPair : installedPackages) {
            appNames.add((String) appPair.first);

            //noinspection SuspiciousMethodCalls
            if (blackListArray.contains(appPair.second)) {
                selectedList.add((appNames.size() - 1));
            }
        }

        getParentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final CharSequence[] items = appNames.toArray(new String[0]);
                final boolean[] checkedItems = new boolean[items.length];
                if (checkedItems.length > 0) {
                    checkedItems[0] = true;
                }
                final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                        .setCancelable(false)
                        .setTitle(R.string.menu_blacklist)
                        .setMessage(R.string.blacklist_intro_text)
                        .setIcon(R.drawable.ic_traffic_light)
                        .setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showBlackListSelector: onSelection: " + which + ", " + isChecked);
                                }
                            }
                        })
                        .setNeutralButton(R.string.clear, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (dialog instanceof AlertDialog) {
                                    final ListAdapter adapter = ((AlertDialog) dialog).getListView().getAdapter();
                                    if (adapter instanceof BaseAdapter) {
                                        for (int i = checkedItems.length - 1; i >= 0; --i) {
                                            checkedItems[i] = false;
                                        }
                                        ((BaseAdapter) adapter).notifyDataSetChanged();
                                    } else {
                                        MyLog.e(CLS_NAME, "onNegative:" + (adapter == null ? "adapter null" : "adapter not BaseAdapter"));
                                    }
                                }
                            }
                        })
                        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showBlackListSelector: onPositive");
                                }

                                final List<Integer> selectedIndices = new ArrayList<>();
                                for (int i = checkedItems.length - 1; i >= 0; --i) {
                                    if (checkedItems[i]) {
                                        selectedIndices.add(i);
                                    }
                                }
                                final Integer[] selected = selectedIndices.toArray(new Integer[0]);
                                assert selected != null;
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showBlackListSelector: onPositive: " + selected.length);
                                }

                                final ArrayList<String> userBlackListed = new ArrayList<>(selected.length);

                                for (final Integer aSelected : selected) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "showBlackListSelector: onPositive: "
                                                + installedPackages.get(aSelected).second);
                                    }
                                    userBlackListed.add(installedPackages.get(aSelected).second);
                                }

                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showBlackListSelector: onPositive: would save: "
                                            + userBlackListed);
                                }

                                blackListHelper.save(FragmentSuperuserHelper.this.getApplicationContext(), userBlackListed);

                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showBlackListSelector: onNegative");
                                }
                                dialog.dismiss();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(final DialogInterface dialog) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showBlackListSelector: onCancel");
                                }
                                dialog.dismiss();
                            }
                        }).create();

                materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
                materialDialog.show();

            }
        });

        return true;
    }

    /**
     * Show the memory slider
     */
    @SuppressWarnings("ConstantConditions")
    public void showMemorySlider() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setView(R.layout.memory_dialog_layout)
                .setCancelable(false)
                .setTitle(R.string.menu_memory_usage)
                .setIcon(R.drawable.ic_memory)
                .setNegativeButton(R.string.text_default, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog instanceof AlertDialog) {
                            ((SeekBar) ((AlertDialog) dialog).findViewById(R.id.memorySeekBar))
                                    .setProgress((int) ((SelfAwareConditions.DEFAULT_INACTIVITY_TIMEOUT / 60000L) - 1));
                        }
                    }
                })
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog instanceof AlertDialog) {
                            final int position = ((SeekBar) ((AlertDialog) dialog).findViewById(R.id.memorySeekBar)).getProgress();
                            final long timeout = (position + 1) * 60000L;

                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "showMemorySlider: onPositive: position: " + position);
                                MyLog.i(CLS_NAME, "showMemorySlider: onPositive: timeout: " + timeout);
                            }

                            SPH.setInactivityTimeout(FragmentSuperuserHelper.this.getApplicationContext(), timeout);
                        }
                        dialog.dismiss();
                    }
                })

                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showMemorySlider: onNegative");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showMemorySlider: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();

        final int currentTimeout = (int) (SPH.getInactivityTimeout(getApplicationContext()) / 60000L);
        final TextView seekText = (TextView) materialDialog.findViewById(R.id.memorySeekBarText);

        switch (currentTimeout) {
            case 1:
                seekText.setText(getParent().getString(R.string.memory_usage_text)
                        + " " + currentTimeout + " " + getParent().getString(R.string.minute));
                break;
            default:
                seekText.setText(getParent().getString(R.string.memory_usage_text)
                        + " " + currentTimeout + " " + getParent().getString(R.string.minutes));
                break;
        }

        final SeekBar seekbar = (SeekBar) materialDialog.findViewById(R.id.memorySeekBar);
        seekbar.setProgress(currentTimeout - 1);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(final SeekBar seekBar, int progress, final boolean fromUser) {

                progress++;

                switch (progress) {
                    case 1:
                        seekText.setText(getParent().getString(R.string.memory_usage_text)
                                + " " + progress + " " + getParent().getString(R.string.minute));
                        break;
                    default:
                        seekText.setText(getParent().getString(R.string.memory_usage_text)
                                + " " + progress + " " + getParent().getString(R.string.minutes));
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

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    /**
     * Show the account picker dialog
     */
    @SuppressWarnings("MissingPermission, ConstantConditions")
    public void showAccountPicker() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showAccountPicker");
        }

        if (PermissionHelper.checkContactGroupPermissions(getApplicationContext())) {

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                    final AccountManager accountManager = AccountManager.get(FragmentSuperuserHelper.this.getApplicationContext());
                    final Account[] accounts = accountManager.getAccountsByType(Install.getAccountType());

                    if (accounts.length > 0) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAccountPicker: accounts: " + accounts.length);
                        }

                    final String[] accountNames = new String[accounts.length];

                    for (int i = 0; i < accounts.length; i++) {
                        if (DEBUG) {
                            MyLog.v(CLS_NAME, "account : " + accounts[i].toString());
                            MyLog.v(CLS_NAME, "name : " + accounts[i].name);
                            MyLog.v(CLS_NAME, "type : " + accounts[i].type);
                        }

                            accountNames[i] = accounts[i].name;
                        }

                        FragmentSuperuserHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                                .setCancelable(false)
                                .setTitle(R.string.dialog_id_verification)
                                .setMessage(R.string.dialog_id_verification_content)
                                .setIcon(R.drawable.ic_account_key)
                                .setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorTint)))
                                        .setSingleChoiceItems(accountNames, 0, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "showAccountPicker: onSelection: " + which + ": " + accountNames[which]);
                                                }
                                            }
                                        })
                                        .setNeutralButton(StringUtils.capitalize(getParent().getString(R.string.add_new)), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                ExecuteIntent.settingsIntent(FragmentSuperuserHelper.this.getApplicationContext(),
                                                        IntentConstants.SETTINGS_ADD_ACCOUNT);
                                                dialog.dismiss();
                                            }
                                        })
                                        .setPositiveButton(R.string.menu_select, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (dialog instanceof AlertDialog) {
                                                    final int selected = ((AlertDialog) dialog).getListView().getCheckedItemPosition();

                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "showAccountPicker: onPositive: " + selected + ": " + which);
                                                    }

                                                    final Account account = accounts[selected];

                                                    if (DEBUG) {
                                                        MyLog.v(CLS_NAME, "account : " + account.toString());
                                                        MyLog.v(CLS_NAME, "name : " + account.name);
                                                        MyLog.v(CLS_NAME, "type : " + account.type);
                                                    }
                                                    FragmentSuperuserHelper.this.startEnrollment(account.name);
                                                }
                                                dialog.dismiss();
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "showAccountPicker: onNegative");
                                                }
                                                dialog.dismiss();
                                            }
                                        })
                                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(final DialogInterface dialog) {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "showAccountPicker: onCancel");
                                                }
                                                dialog.dismiss();
                                            }
                                        }).create();

                                materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
                                materialDialog.show();
                            }
                        });

                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "showAccountPicker: no accounts");
                        }

                        ExecuteIntent.settingsIntent(FragmentSuperuserHelper.this.getApplicationContext(), IntentConstants.SETTINGS_ADD_ACCOUNT);
                        FragmentSuperuserHelper.this.getParentActivity().speak(R.string.error_vi_no_account,
                                LocalRequest.ACTION_SPEAK_ONLY);

                    }

                }
            });
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "showAccountPicker: permission denied");
            }

            ExecuteIntent.openApplicationSpecificSettings(getApplicationContext(),
                    getApplicationContext().getPackageName());
            getParentActivity().speak(R.string.permission_group_contacts_denied,
                    LocalRequest.ACTION_SPEAK_ONLY);
        }
    }

    /**
     * Start the process of enrolling the user's voice against the given account, first ensuring that
     * an association does not already exist.
     *
     * @param accountName the account name
     */
    @SuppressWarnings("ConstantConditions")
    private void startEnrollment(@NonNull final String accountName) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startEnrollment");
        }

        if (SaiyAccountHelper.accountExists(getApplicationContext(), accountName, null)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "startEnrollment: account exists");
            }

            final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                    .setCancelable(false)
                    .setTitle(R.string.menu_unlink_association)
                    .setMessage(getParent().getString(R.string.content_unlink_association, accountName))
                    .setIcon(R.drawable.ic_account_switch)
                    .setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorTint)))

                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "startEnrollment: onPositive");
                            }

                            if (SaiyAccountHelper.deleteAccount(FragmentSuperuserHelper.this.getApplicationContext(), accountName, null)) {
                                FragmentSuperuserHelper.this.proceedEnrollment(accountName);
                            }

                            dialog.dismiss();
                        }
                    })

                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "startEnrollment: onNegative");
                            }
                            dialog.dismiss();
                        }
                    })

                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(final DialogInterface dialog) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "startEnrollment: onCancel");
                            }
                            dialog.dismiss();
                        }
                    }).create();

            materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
            materialDialog.show();

        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "startEnrollment: account does not exist");
            }

            proceedEnrollment(accountName);
        }
    }

    /**
     * Proceed with the enrollment process knowing that any existing association to the
     * given account has been deleted consensually along with any remote profile.
     * <p>
     * Once the account set up is complete a callback will be provided from the
     * {@link ISaiyAccount} interface. Any issue in the account creation will be
     * handled by {@link #monitorAccountCreation(String)}
     *
     * @param accountName the account name
     */
    private void proceedEnrollment(@NonNull final String accountName) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "proceedEnrollment");
        }

        final SaiyAccount saiyAccount = new SaiyAccount(accountName, true);
        saiyAccount.setAccountId(getApplicationContext(), this);
        saiyAccount.setPseudonym(SPH.getUserName(getApplicationContext()), true);

        getParentActivity().showProgress(true);
        monitorAccountCreation(accountName);

    }

    @Override
    public void onAccountInitialisation(@NonNull final SaiyAccount saiyAccount) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onAccountInitialisation");
        }

        if (!enrollmentCancelled.get()) {
            accountInitialised.set(true);
            cancelTimer();

            getParentActivity().showProgress(false);

            if (accountSetUpComplete(saiyAccount)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAccountInitialisation: set up complete");
                }
                audioEnroll(saiyAccount.getProfileItem().getId());
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onAccountInitialisation: set up failed");
                }
                enrollmentFailed(saiyAccount.getAccountName());
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAccountInitialisation: enrollment cancelled");
            }
        }
    }

    /**
     * Method to monitor the background set up process of the {@link SaiyAccount}. Due to any
     * condition that could cause a failure, most probably network related, we draw a line
     * at {@link #ARBITRARY_DELAY} and notify the user that things have gone wrong.
     *
     * @param accountName the account name associated with the account being created
     */
    private void monitorAccountCreation(@NonNull final String accountName) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "monitorAccountCreation");
        }

        timer = new Timer();
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "timerTask: checking account status");
                }

                getParentActivity().showProgress(false);

                if (accountInitialised.get()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "account set up successfully");
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "account set up failed");
                    }

                    enrollmentCancelled.set(true);
                    enrollmentFailed(accountName);
                }
            }
        };

        timer.schedule(timerTask, ARBITRARY_DELAY);
    }

    /**
     * Notify the user that the account creation failed
     *
     * @param accountName the account name
     */
    private void enrollmentFailed(@NonNull final String accountName) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "enrollmentFailed");
        }

        final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(SPH.getVRLocale(getApplicationContext()));

        getParentActivity().speak(PersonalityResponse.getEnrollmentAPIError(getApplicationContext(), sl),
                LocalRequest.ACTION_SPEAK_ONLY);
        SaiyAccountHelper.deleteAccount(getApplicationContext(), accountName, null);
    }


    /**
     * All is well - begin the audio enrollment
     */
    private void audioEnroll(@NonNull final String profileId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "audioEnroll");
        }


        final String utterance;

        if (SPH.getEnrollmentVerbose(getApplicationContext())) {
            utterance = getApplicationContext().getString(R.string.speech_enroll_instructions_40);
        } else {
            SPH.setEnrollmentVerbose(getApplicationContext());
            utterance = getApplicationContext().getString(R.string.speech_enroll_instructions_first);
        }

        final LocalRequest request = new LocalRequest(getApplicationContext());
        request.prepareDefault(LocalRequest.ACTION_SPEAK_LISTEN, utterance);
        request.setQueueType(TextToSpeech.QUEUE_ADD);
        request.setCondition(Condition.CONDITION_IDENTITY);
        request.setIdentityProfile(profileId);
        request.execute();
    }

    /**
     * Utility method to check if the asynchronous operations have completed.
     *
     * @param saiyAccount the {@link SaiyAccount} object
     * @return true if the asynchronous operations have completed, false otherwise
     */
    private boolean accountSetUpComplete(@NonNull final SaiyAccount saiyAccount) {
        return UtilsString.notNaked(saiyAccount.getAccountId())
                && saiyAccount.getProfileItem() != null
                && UtilsString.notNaked(saiyAccount.getProfileItem().getId());
    }

    /**
     * Cancel the timer, as it's no longer needed.
     */
    public void cancelTimer() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "cancelTimer");
        }

        if (timer != null) {

            try {
                timer.cancel();
                timer.purge();
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "cancelTimer: NullPointerException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "cancelTimer: Exception");
                    e.printStackTrace();
                }
            }
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
    public FragmentSuperUser getParent() {
        return parentFragment;
    }
}

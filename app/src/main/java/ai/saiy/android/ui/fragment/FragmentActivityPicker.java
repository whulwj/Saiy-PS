package ai.saiy.android.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.applications.Application;
import ai.saiy.android.applications.ApplicationActivityBasic;
import ai.saiy.android.ui.activity.ActivityActivityPicker;
import ai.saiy.android.ui.components.UIActivityPickerAdapter;
import ai.saiy.android.ui.fragment.helper.FragmentActivityPickerHelper;
import ai.saiy.android.utils.MyLog;

public class FragmentActivityPicker extends Fragment implements AdapterView.OnItemLongClickListener, ExpandableListView.OnChildClickListener {
    private static final Object lock = new Object();

    private PackageManager packageManager;
    private UIActivityPickerAdapter mAdapter;
    private ArrayList<Application> mObjects;
    private FragmentActivityPickerHelper helper;
    private Context mContext;

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentActivityPicker.class.getSimpleName();
    private ExpandableListView expandableListView = null;

    public static FragmentActivityPicker newInstance(Bundle args) {
        return new FragmentActivityPicker();
    }

    public void showIntentExtrasDialog(final ApplicationActivityBasic applicationActivityBasic) {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setCancelable(false)
                .setView(R.layout.text_input_dialog_layout)
                .setTitle(R.string.menu_intent_extras)
                .setIcon(R.drawable.ic_memory)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CharSequence charSequence = "";
                        if (dialog instanceof AlertDialog) {
                            final EditText editText = ((AlertDialog) dialog).findViewById(android.R.id.input);
                            charSequence = (editText == null) ? null : editText.getText();
                        }
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showIntentExtrasDialog: onInput: " + charSequence);
                        }
                        if (charSequence == null || !ai.saiy.android.utils.UtilsString.notNaked(charSequence.toString())) {
                            dialog.dismiss();
                            applicationActivityBasic.setIntentExtras(null);
                            getParentActivity().setResult(applicationActivityBasic);
                        } else {
                            if (!ai.saiy.android.utils.UtilsBundle.stringExtrasToBundle(charSequence.toString())) {
                                toast(getString(R.string.content_extras_format_incorrect), Toast.LENGTH_SHORT);
                                return;
                            }
                            dialog.dismiss();
                            applicationActivityBasic.setIntentExtras(charSequence.toString().trim());
                            getParentActivity().setResult(applicationActivityBasic);
                        }
                    }
                })
                .setNegativeButton(R.string.title_none, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showIntentExtrasDialog: onNegative");
                        }
                        dialog.dismiss();
                        getParentActivity().setResult(applicationActivityBasic);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showIntentExtrasDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();

        final TextInputLayout textInputLayout = materialDialog.getWindow().findViewById(android.R.id.inputArea);
        textInputLayout.setHint(R.string.custom_extras_title);
        final EditText editText = textInputLayout.findViewById(android.R.id.input);
        editText.setHint(R.string.custom_extras_hint);
    }

    public void toast(String text, int duration) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "makeToast: " + text);
        }
        if (isActive()) {
            getParentActivity().toast(text, duration);
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "toast Fragment detached");
        }
    }

    public boolean isActive() {
        return getActivity() != null && getParentActivity().isActive() && isAdded() && !isRemoving();
    }

    public ActivityActivityPicker getParentActivity() {
        return (ActivityActivityPicker) getActivity();
    }

    public Context getApplicationContext() {
        return mContext;
    }

    public UIActivityPickerAdapter getAdapter() {
        return mAdapter;
    }

    public ArrayList<Application> getObjects() {
        return mObjects;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.mContext = activity.getApplicationContext();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context.getApplicationContext();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onChildClick: groupPosition: " + groupPosition + " : childPosition: " + childPosition);
        }
        try {
            final PackageInfo packageInfo = packageManager.getPackageInfo(mObjects.get(groupPosition).getPackageName(), PackageManager.GET_ACTIVITIES);
            if (packageInfo.activities[childPosition] != null) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "tmp.activities[childPosition].name: " + packageInfo.activities[childPosition].name);
                    MyLog.i(CLS_NAME, "tmp.activities[childPosition].packageName: " + packageInfo.activities[childPosition].packageName);
                }
                if (packageInfo.activities[childPosition].exported) {
                    showIntentExtrasDialog(new ApplicationActivityBasic(mObjects.get(groupPosition).getLabel(), mObjects.get(groupPosition).getPackageName(), packageInfo.activities[childPosition].name, null));
                } else {
                    toast(getString(R.string.content_access_forbidden), Toast.LENGTH_SHORT);
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "tmp.activities[childPosition] null");
                }
                getParentActivity().setResult(null);
            }
        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "NameNotFoundException");
            }
            getParentActivity().setResult(null);
        }
        return true;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate: savedInstanceState: " + (bundle != null));
        }
        this.packageManager = getApplicationContext().getPackageManager();
        this.helper = new FragmentActivityPickerHelper(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateView");
        }
        final View rootView = layoutInflater.inflate(R.layout.layout_fragment_activity_picker, viewGroup, false);
        this.expandableListView = helper.getExpandableListView(rootView);
        this.mObjects = new ArrayList<>();
        this.mAdapter = helper.getAdapter(mObjects);
        this.expandableListView.setAdapter(mAdapter);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (ExpandableListView.getPackedPositionType(id) != ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            return false;
        }
        if (DEBUG) {
            final int packedPositionGroup = ExpandableListView.getPackedPositionGroup(id);
            final int packedPositionChild = ExpandableListView.getPackedPositionChild(id);
            MyLog.d(CLS_NAME, "groupPosition: " + packedPositionGroup + " : childPosition :" + packedPositionChild);
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPause");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onResume");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onStart");
        }
        synchronized (lock) {
            if (mObjects.isEmpty()) {
                getParentActivity().setTitle(getString(R.string.title_select_app_activity));
                helper.finaliseUI();
            }
        }
    }
}

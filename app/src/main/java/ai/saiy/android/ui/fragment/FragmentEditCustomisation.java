package ai.saiy.android.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.saiy.android.R;
import ai.saiy.android.command.http.CustomHttp;
import ai.saiy.android.command.intent.CustomIntent;
import ai.saiy.android.custom.CustomNickname;
import ai.saiy.android.custom.CustomPhrase;
import ai.saiy.android.custom.CustomReplacement;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.containers.ContainerCustomisation;
import ai.saiy.android.ui.fragment.helper.FragmentEditCustomisationHelper;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;

public class FragmentEditCustomisation extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    public static final String EXTRA_KEY = "container_customisation_key";
    private static final Object lock = new Object();

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentEditCustomisation.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<?> mAdapter;
    private ArrayList<ContainerCustomisation> mObjects;
    private FragmentEditCustomisationHelper helper;
    private Context mContext;

    public static FragmentEditCustomisation newInstance(Bundle args) {
        final FragmentEditCustomisation fragmentEditCustomisation = new FragmentEditCustomisation();
        fragmentEditCustomisation.setArguments(args);
        return fragmentEditCustomisation;
    }

    public List<ContainerCustomisation> getObjectsFromArguments() {
        final Bundle args = getArguments();
        if (args != null && args.containsKey(EXTRA_KEY)) {
            return args.getParcelableArrayList(EXTRA_KEY);
        } else {
            return Collections.emptyList();
        }
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

    public ActivityHome getParentActivity() {
        return (ActivityHome) getActivity();
    }

    public Context getApplicationContext() {
        return mContext;
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return mAdapter;
    }

    public ArrayList<ContainerCustomisation> getObjects() {
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
    public void onClick(View view) {
        if (Global.isInVoiceTutorial()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onClick: tutorialActive");
            }
            toast(getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
            return;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onClick: " + view.getTag());
        }
        final int position = (Integer) view.getTag();
        final ContainerCustomisation containerCustomisation = mObjects.get(position);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getCustom: " + containerCustomisation.getCustom().name());
        }
        final com.google.gson.Gson gson = new com.google.gson.GsonBuilder().disableHtmlEscaping().create();
        final String serialised = containerCustomisation.getSerialised();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "serialised: " + serialised);
        }
        switch (containerCustomisation.getCustom()) {
            case CUSTOM_PHRASE:
                final CustomPhrase customPhrase = gson.fromJson(serialised, new TypeToken<CustomPhrase>() {
                }.getType());
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "customPhrase: " + customPhrase.getKeyphrase());
                    MyLog.i(CLS_NAME, "customPhrase: " + containerCustomisation.getRowId());
                }
                this.helper.showCustomPhrasesDialog(customPhrase.getKeyphrase(), customPhrase.getResponse(), customPhrase.getStartVoiceRecognition(), position, containerCustomisation.getRowId());
                break;
            case CUSTOM_NICKNAME:
                final CustomNickname customNickname = gson.fromJson(serialised, new TypeToken<CustomNickname>() {
                }.getType());
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "customNickname: " + customNickname.getNickname());
                    MyLog.i(CLS_NAME, "customNickname: " + containerCustomisation.getRowId());
                }
                helper.showNicknameDialog(customNickname.getNickname(), customNickname.getContactName(), position, containerCustomisation.getRowId());
                break;
            case CUSTOM_REPLACEMENT:
                final CustomReplacement customReplacement = gson.fromJson(serialised, new TypeToken<CustomReplacement>() {
                }.getType());
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "customReplacement: " + customReplacement.getKeyphrase());
                    MyLog.i(CLS_NAME, "customReplacement: " + containerCustomisation.getRowId());
                }
                helper.showCustomReplacementDialog(customReplacement.getKeyphrase(), customReplacement.getReplacement(), position, containerCustomisation.getRowId());
                break;
            case CUSTOM_COMMAND:
                final ai.saiy.android.custom.CustomCommand customCommand = gson.fromJson(serialised, new TypeToken<ai.saiy.android.custom.CustomCommand>() {
                }.getType());
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "customCommandContainer: " + customCommand.getKeyphrase());
                    MyLog.i(CLS_NAME, "customCommandContainer: " + containerCustomisation.getRowId());
                }
                switch (customCommand.getCustomAction()) {
                    case CUSTOM_DISPLAY_CONTACT:
                        helper.showCustomCommandInputDialog(customCommand, customCommand.getCustomAction(), getString(R.string.content_display_contact, customCommand.getExtraText2()), customCommand.getExtraText(), customCommand.getExtraText2(), customCommand.getAction() == LocalRequest.ACTION_SPEAK_LISTEN, position, containerCustomisation.getRowId());
                        break;
                    case CUSTOM_TASKER_TASK:
                        helper.showCustomCommandInputDialog(customCommand, customCommand.getCustomAction(), getString(R.string.content_execute_task, customCommand.getExtraText()), customCommand.getExtraText(), null, customCommand.getAction() == LocalRequest.ACTION_SPEAK_LISTEN, position, containerCustomisation.getRowId());
                        break;
                    case CUSTOM_ACTIVITY:
                        helper.showCustomCommandInputDialog(customCommand, customCommand.getCustomAction(), getString(R.string.content_launch_activity, customCommand.getExtraText()), customCommand.getIntent(), customCommand.getExtraText(), customCommand.getAction() == LocalRequest.ACTION_SPEAK_LISTEN, position, containerCustomisation.getRowId());
                        break;
                    case CUSTOM_CALL_CONTACT:
                        helper.showCustomCommandInputDialog(customCommand, customCommand.getCustomAction(), getString(R.string.content_call_contact, customCommand.getExtraText2(), customCommand.getExtraText()), customCommand.getExtraText(), customCommand.getExtraText2(), customCommand.getAction() == LocalRequest.ACTION_SPEAK_LISTEN, position, containerCustomisation.getRowId());
                        break;
                    case CUSTOM_LAUNCH_APPLICATION:
                        helper.showCustomCommandInputDialog(customCommand, customCommand.getCustomAction(), getString(R.string.content_launch_app, customCommand.getExtraText2()), customCommand.getExtraText(), customCommand.getExtraText2(), customCommand.getAction() == LocalRequest.ACTION_SPEAK_LISTEN, position, containerCustomisation.getRowId());
                        break;
                    case CUSTOM_AUTOMATE_FLOW:
                        helper.showCustomCommandInputDialog(customCommand, customCommand.getCustomAction(), getString(R.string.content_automate_flow, customCommand.getExtraText2()), customCommand.getExtraText(), customCommand.getExtraText2(), customCommand.getAction() == LocalRequest.ACTION_SPEAK_LISTEN, position, containerCustomisation.getRowId());
                        break;
                    case CUSTOM_LAUNCH_SHORTCUT:
                        helper.showCustomCommandInputDialog(customCommand, customCommand.getCustomAction(), getString(R.string.content_run_shortcut, customCommand.getExtraText2()), customCommand.getExtraText(), customCommand.getExtraText2(), customCommand.getAction() == LocalRequest.ACTION_SPEAK_LISTEN, position, containerCustomisation.getRowId());
                        break;
                    case CUSTOM_SEARCHABLE:
                        helper.showCustomCommandInputDialog(customCommand, customCommand.getCustomAction(), getString(R.string.content_search_on, customCommand.getExtraText2()), customCommand.getExtraText(), customCommand.getExtraText2(), customCommand.getAction() == LocalRequest.ACTION_SPEAK_LISTEN, position, containerCustomisation.getRowId());
                        break;
                    case CUSTOM_INTENT_SERVICE:
                        Intent intent = null;
                        try {
                            intent = Intent.parseUri(customCommand.getIntent(), 0);
                        } catch (NullPointerException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "remoteIntent.parseUri: NullPointerException");
                                e.printStackTrace();
                            }
                        } catch (URISyntaxException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "remoteIntent.parseUri: URISyntaxException");
                                e.printStackTrace();
                            }
                        }
                        String appName;
                        if (intent != null) {
                            final Pair<Boolean, String> appNameFromPackage = ai.saiy.android.applications.UtilsApplication.getAppNameFromPackage(getApplicationContext(), intent.getPackage());
                            appName = appNameFromPackage.first ? appNameFromPackage.second : intent.getPackage();
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "remoteIntent null");
                            }
                            appName = getString(R.string.an_unknown_application);
                        }
                        helper.showRemoteIntentDialog(appName, position, containerCustomisation.getRowId());
                        break;
                    case CUSTOM_HTTP:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "CUSTOM_HTTP");
                        }
                        helper.showCustomHttpDialog(customCommand, gson.fromJson(customCommand.getExtraText(), new TypeToken<CustomHttp>() {
                        }.getType()), position, containerCustomisation.getRowId());
                        break;
                    case CUSTOM_SEND_INTENT:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "CUSTOM_SEND_INTENT");
                        }
                        helper.showCustomIntentDialog(customCommand, gson.fromJson(customCommand.getExtraText(), new TypeToken<CustomIntent>() {
                        }.getType()), position, containerCustomisation.getRowId());
                        break;
                    default:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "default custom type?");
                        }
                        break;
                }
            default:
                break;
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        this.helper = new FragmentEditCustomisationHelper(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateView");
        }
        View inflate = layoutInflater.inflate(R.layout.layout_common_fragment_parent, viewGroup, false);
        this.mRecyclerView = helper.getRecyclerView(inflate);
        this.mObjects = new ArrayList<>();
        this.mAdapter = helper.getAdapter(mObjects);
        this.mRecyclerView.setAdapter(mAdapter);
        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (Global.isInVoiceTutorial()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onClick: tutorialActive");
            }
            toast(getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
            return true;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onLongClick: " + view.getTag());
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
                getParentActivity().setTitle(getString(R.string.menu_edit_customisations));
                helper.finaliseUI();
            }
        }
    }
}

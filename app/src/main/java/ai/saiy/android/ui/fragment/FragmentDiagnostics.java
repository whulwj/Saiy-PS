package ai.saiy.android.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.diagnostic.DiagnosticInfoListener;
import ai.saiy.android.diagnostic.DiagnosticsHelper;
import ai.saiy.android.diagnostic.RecommendationHelper;
import ai.saiy.android.diagnostic.DiagnosticsInfo;
import ai.saiy.android.diagnostic.VoiceEngine;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsAnalytic;

public class FragmentDiagnostics extends Fragment implements DiagnosticInfoListener, View.OnClickListener {
    private static final Object lock = new Object();

    private TextView tvDiagnostics;
    private TextView tvASRCount;
    private TextView tvTTSCount;
    private TextView tvErrorCount;
    private TextView tvPassedCount;
    private FloatingActionButton fab;
    private ScrollView svDiagnostics;
    private ArrayList<VoiceEngine> containerVEArray;
    private int requestCount;
    private FirebaseAnalytics firebaseAnalytics;
    private TelephonyManager telephonyManager;
    private Context mContext;

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentDiagnostics.class.getSimpleName();
    private DiagnosticsHelper helper = null;
    private final AtomicInteger linesCount = new AtomicInteger();
    private final AtomicBoolean isCompleted = new AtomicBoolean();
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            super.onCallStateChanged(state, phoneNumber);
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "PhoneStateListener: TelephonyManager.CALL_STATE_RINGING");
                }
                if (helper == null || !helper.isDiagnosing()) {
                    return;
                }
                helper.cancel();
            }
        }
    };

    public static FragmentDiagnostics newInstance(Bundle args) {
        return new FragmentDiagnostics();
    }

    private void changeGravity(final int gravity) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "changeGravity");
        }
        if (isActive() && helper != null && !helper.isCancelled()) {
            getParentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvDiagnostics.setGravity(gravity);
                }
            });
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "changeGravity: fragment detached");
        }
    }

    private void setFABImage(@DrawableRes final int resId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setFABImage");
        }
        if (isActive()) {
            getParentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fab.setImageResource(resId);
                }
            });
        }
    }

    private void resetCounts() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "resetCounts");
        }
        if (isActive()) {
            tvASRCount.setText(String.valueOf(0));
            tvErrorCount.setText(String.valueOf(0));
            tvPassedCount.setText(String.valueOf(0));
            tvTTSCount.setText(String.valueOf(0));
        }
    }

    private void scrollToTop() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "scrollToTop");
        }
        if (isActive() && helper != null && !helper.isCancelled()) {
            getParentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvDiagnostics.scrollTo(0, 0);
                }
            });
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "scrollToTop: fragment detached");
        }
    }

    private void getEngines() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getEngines");
        }
        requestCount = 0;
        Intent intent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        final PackageManager packageManager = getParentActivity().getPackageManager();
        List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA);
        containerVEArray = new ArrayList<>(queryIntentActivities.size());
        for (int i = 0; i < queryIntentActivities.size(); i++) {
            VoiceEngine voiceEngine = new VoiceEngine();
            voiceEngine.setApplicationName(queryIntentActivities.get(i).loadLabel(packageManager).toString());
            voiceEngine.setPackageName(queryIntentActivities.get(i).activityInfo.applicationInfo.packageName);
            Intent checkVoiceDataIntent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            checkVoiceDataIntent.setPackage(voiceEngine.getPackageName());
            checkVoiceDataIntent.getStringArrayListExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES);
            checkVoiceDataIntent.getStringArrayListExtra(TextToSpeech.Engine.EXTRA_UNAVAILABLE_VOICES);
            voiceEngine.setIntent(checkVoiceDataIntent);
            containerVEArray.add(voiceEngine);
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "containerVEArray: " + containerVEArray.size());
        }
        // Check whether the voice data for the engine is ok.
        for (int i = 0; i < containerVEArray.size(); ++i) {
            startActivityForResult(containerVEArray.get(i).getIntent(), i);
        }
    }

    private void checkErrors() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkErrors");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String charSequence = tvDiagnostics.getText().toString();
                for (int i = 0; i < 20 && !isSame(charSequence) && isActive(); i++) {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e) {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "checkErrors InterruptedException");
                            e.printStackTrace();
                        }
                    }
                    charSequence = tvDiagnostics.getText().toString();
                }
                if (isActive()) {
                    appendDiagnosticInfo("\n" + getString(R.string.diagnostics_analysing));
                }
            }
        }).start();
    }

    private boolean isSame(String str) {
        return tvDiagnostics.getText().toString().matches(Pattern.quote(str));
    }

    public ActivityHome getParentActivity() {
        return (ActivityHome) getActivity();
    }

    @Override
    public void onComplete(final DiagnosticsInfo diagnosticsInfo, boolean forceStop) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "onComplete: forceStop: " + forceStop);
        }
        setFABImage(R.drawable.ic_media_play);
        if (forceStop) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(300L);
                    } catch (InterruptedException e) {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "checkErrors InterruptedException");
                            e.printStackTrace();
                        }
                    }
                    setDiagnosticInfo("");
                }
            }).start();
            return;
        }

        UtilsAnalytic.diagnosticsComplete(getApplicationContext(), firebaseAnalytics, diagnosticsInfo.getErrorCount() > 0);
        checkErrors();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ai.saiy.android.utils.SPH.setRunDiagnostics(getApplicationContext(), true);
                ArrayList<String> recommendations = new RecommendationHelper().recommend(getApplicationContext(), diagnosticsInfo);
                changeGravity(Gravity.TOP);
                try {
                    Thread.sleep(1500L);
                } catch (InterruptedException e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "onComplete InterruptedException");
                        e.printStackTrace();
                    }
                }
                isCompleted.set(true);
                for (int i = 0; i < recommendations.size(); ++i) {
                    if (0 == i) {
                        setDiagnosticInfo(recommendations.get(i));
                    } else {
                        appendDiagnosticInfo(recommendations.get(i));
                    }
                    try {
                        Thread.sleep(2L);
                    } catch (InterruptedException e) {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "onComplete InterruptedException");
                            e.printStackTrace();
                        }
                    }
                }
                scrollToTop();
            }
        });
    }

    @Override
    public void appendDiagnosticInfo(final String str) {
        if (!isActive() || helper == null || helper.isCancelled()) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "appendDiagnosticInfo: fragment detached");
            }
            return;
        }
        getParentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvDiagnostics.append(str + "\n");
            }
        });
        if (linesCount.incrementAndGet() == 15) {
            changeGravity(Gravity.BOTTOM);
        } else {
            if (isCompleted.get()) {
                return;
            }
            svDiagnostics.postDelayed(new Runnable() {
                @Override
                public void run() {
                    svDiagnostics.fullScroll(View.FOCUS_DOWN);
                }
            }, 100L);
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

    public Context getApplicationContext() {
        return mContext;
    }

    @Override
    public void setDiagnosticInfo(final String str) {
        if (isActive()) {
            final String text = str + "\n";
            getParentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvDiagnostics.setText(text);
                }
            });
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "setDiagnosticInfo: fragment detached");
        }
    }

    @Override
    public void replaceDiagnosticInfo(final String str) {
        if (isActive()) {
            getParentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int length = tvDiagnostics.length();
                    int lastIndexOf = tvDiagnostics.getText().toString().endsWith("\n") ? tvDiagnostics.getText().toString().substring(0, length - 1).lastIndexOf("\n") : tvDiagnostics.getText().toString().lastIndexOf("\n");
                    if (length <= 0 || lastIndexOf <= 0) {
                        return;
                    }
                    tvDiagnostics.getEditableText().replace(lastIndexOf + 1, length, str + "\n");
                }
            });
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "replaceDiagnosticInfo: fragment detached");
        }
    }

    public boolean isActive() {
        return !isRemoving() && isAdded() && getParentActivity() != null && getParentActivity().isActive() && !getParentActivity().isFinishing();
    }

    @Override
    public void setASRCount(final String str) {
        if (isActive()) {
            getParentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvASRCount.setTextColor(Color.BLUE);
                    tvASRCount.setText(String.valueOf(str));
                }
            });
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "setASRCount: fragment detached");
        }
    }

    @Override
    public void setTTSCount(final String str) {
        if (isActive()) {
            getParentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvTTSCount.setTextColor(Color.BLUE);
                    tvTTSCount.setText(str);
                }
            });
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "setTTSCount: fragment detached");
        }
    }

    @Override
    public void setErrorCount(final String str) {
        if (isActive()) {
            getParentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvErrorCount.setTextColor(Color.RED);
                    tvErrorCount.setText(str);
                }
            });
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "setErrorCount: fragment detached");
        }
    }

    @Override
    public void setPassedCount(final String str) {
        if (isActive()) {
            getParentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvPassedCount.setTextColor(Color.BLUE);
                    tvPassedCount.setText(str);
                }
            });
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "setPassedCount: fragment detached");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onActivityResult: requestCount:  - requestCode: " + requestCode);
        }
        requestCount++;
        if (intent == null) {
            return;
        }
        final Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        ArrayList<String> dataFiles;
        ArrayList<String> dataFilesInfo;
        try {
            if (DEBUG) {
                MyLog.d(CLS_NAME, containerVEArray.get(requestCode).getApplicationName() + " - Bundle Data");
                for (String str : extras.keySet()) {
                    MyLog.d(CLS_NAME, "Key: " + str + " = " + extras.get(str));
                }
            }
            if (extras.containsKey(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES)) {
                containerVEArray.get(requestCode).setAvailableLocales(extras.getStringArrayList(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES));
            } else {
                containerVEArray.get(requestCode).setAvailableLocales(Collections.emptyList());
            }
            if (extras.containsKey(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES_INFO)) {
                if (extras.get(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES_INFO) instanceof String[]) {
                    String[] stringArray = extras.getStringArray(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES_INFO);
                    if (stringArray != null) {
                        MyLog.d(CLS_NAME, "dataFilesInfo: " + Arrays.asList(stringArray));
                    }
                } else if ((extras.get(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES_INFO) instanceof ArrayList) && (dataFilesInfo = extras.getStringArrayList(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES_INFO)) != null) {
                    MyLog.d(CLS_NAME, "dataFilesInfo: " + dataFilesInfo);
                }
            }
            if (extras.containsKey(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES)) {
                if (extras.get(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES) instanceof String[]) {
                    String[] stringArray = extras.getStringArray(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES);
                    if (stringArray != null) {
                        MyLog.d(CLS_NAME, "dataFiles: " + Arrays.asList(stringArray));
                    }
                } else if ((extras.get(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES) instanceof ArrayList) && (dataFiles = extras.getStringArrayList(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES)) != null) {
                    MyLog.d(CLS_NAME, "dataFiles: " + dataFiles);
                }
            }
            if (requestCount == containerVEArray.size()) {
                new Thread() {
                    @Override
                    public void run() {
                        for (int i = 0; i < containerVEArray.size(); ++i) {
                            if (extras.containsKey(TextToSpeech.Engine.EXTRA_VOICE_DATA_ROOT_DIRECTORY)) {
                                containerVEArray.get(i).setDataRoot(extras.getString(TextToSpeech.Engine.EXTRA_VOICE_DATA_ROOT_DIRECTORY));
                            }
                            if (DEBUG) {
                                MyLog.v(CLS_NAME, "cve: " + containerVEArray.get(i).getApplicationName() + " - " + containerVEArray.get(i).getAvailableLocales().size() + " - " + containerVEArray.get(i).getAvailableLocales().toString());
                            }
                            try {
                                Thread.sleep(20L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        System.gc();
                        Collections.shuffle(containerVEArray, new Random(System.nanoTime()));
                        if (!helper.isCancelled()) {
                            helper.doInBackground(containerVEArray);
                        } else if (DEBUG) {
                            MyLog.w(CLS_NAME, "diagnostics.isCancelled()");
                        }
                    }
                }.start();
            }
        } catch (IndexOutOfBoundsException e) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "IndexOutOfBoundsException");
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mContext = activity.getApplicationContext();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context.getApplicationContext();
    }

    @Override
    public void onClick(View view) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onClick");
        }
        if (Global.isInVoiceTutorial()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onClick: tutorialActive");
            }
            toast(getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
            return;
        }
        if (helper != null && !helper.isCompleted() && !helper.isCancelled()) {
            setFABImage(R.drawable.ic_media_play);
            helper.cancel();
            return;
        }
        setFABImage(R.drawable.ic_media_stop);
        linesCount.set(0);
        isCompleted.set(false);
        resetCounts();
        helper = new DiagnosticsHelper(getApplicationContext(), this);
        setDiagnosticInfo("");
        changeGravity(Gravity.TOP);
        getEngines();
        UtilsAnalytic.diagnosticsStarted(getApplicationContext(), firebaseAnalytics);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        this.telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateView");
        }
        View rootView = layoutInflater.inflate(R.layout.fragment_diagnostics_layout, viewGroup, false);
        tvDiagnostics = rootView.findViewById(R.id.tv_diagnostics);
        tvASRCount = rootView.findViewById(R.id.tvASRCount);
        tvTTSCount = rootView.findViewById(R.id.tvTTSCount);
        tvErrorCount = rootView.findViewById(R.id.tvErrorCount);
        tvPassedCount = rootView.findViewById(R.id.tvPassedCount);
        fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(this);
        svDiagnostics = rootView.findViewById(R.id.svDiagnostics);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
        if (helper != null && !helper.isCancelled()) {
            helper.cancel();
        }
        this.firebaseAnalytics = null;
        if (telephonyManager != null) {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            telephonyManager = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPause: cancelling diagnostics: " + (telephonyManager != null && telephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE && helper != null && helper.isDiagnosing()));
        }
        if (telephonyManager == null || telephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE || helper == null || !helper.isDiagnosing()) {
            return;
        }
        helper.cancel();
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
            if (fab != null && fab.getVisibility() == View.INVISIBLE) {
                getParentActivity().setTitle(getString(R.string.title_diagnostics));
                fab.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fab.show();
                    }
                }, 1250L);
            }
        }
    }
}

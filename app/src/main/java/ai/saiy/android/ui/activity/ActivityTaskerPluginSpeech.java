package ai.saiy.android.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;

import ai.saiy.android.R;
import ai.saiy.android.broadcast.BRTaskerReceiver;
import ai.saiy.android.cognitive.identity.provider.microsoft.Speaker;
import ai.saiy.android.thirdparty.tasker.TaskerHelper;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

public class ActivityTaskerPluginSpeech extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityTaskerPluginSpeech.class.getSimpleName();

    private EditText etTaskerInput;
    private RadioGroup rgTaskerAction;
    private CheckBox cbTaskerStartListening;
    private boolean isCancelled;

    private String restraintBlurb(String str, String prefix) {
        String blurb = prefix + str;
        return blurb.length() > getResources().getInteger(R.integer.max_blurb_length) ? blurb.substring(0, getResources().getInteger(R.integer.max_blurb_length)) : blurb;
    }

    private void hideIME() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "hideIME");
        }
        if (etTaskerInput != null) {
            ((InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(etTaskerInput.getApplicationWindowToken(), 0);
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "hideIME null");
        }
    }

    private boolean isValid(String variableName) {
        return variableName.startsWith("%") && variableName.length() > 3 && !variableName.contains(Constants.SEP_SPACE);
    }

    @Override
    public void finish() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "finish");
        }
        if (isCancelled) {
            setResult(Activity.RESULT_CANCELED);
            super.finish();
            return;
        }
        final String input = etTaskerInput.getText() != null ? etTaskerInput.getText().toString().trim() : null;
        if (!UtilsString.notNaked(input)) {
            ai.saiy.android.utils.UtilsToast.showToast(getApplicationContext(), R.string.tasker_error_empty_input, Toast.LENGTH_SHORT);
            return;
        }

        final Bundle bundle = new Bundle();
        final int checkedId = rgTaskerAction.getCheckedRadioButtonId();
        if (R.id.rbNotify == checkedId) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "notify");
            }
            if (TaskerHelper.pTask.matcher(input).matches()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "notify: have tasker variables");
                }
            }
            bundle.putString(Speaker.EXTRA_VALUE, input);
            bundle.putInt(BRTaskerReceiver.EXTRA_ACTION, BRTaskerReceiver.ACTION_NOTIFY);
            bundle.putString(BRTaskerReceiver.EXTRA_BLURB, restraintBlurb(input, getString(R.string.tasker_prefix_notify)));
        } else if (R.id.rbSpeak == checkedId) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "speak");
            }
            if (TaskerHelper.pTask.matcher(input).matches()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "speak: have tasker variables");
                }
            }
            bundle.putString(Speaker.EXTRA_VALUE, input);
            bundle.putInt(BRTaskerReceiver.EXTRA_ACTION, BRTaskerReceiver.ACTION_SPEAK);
            bundle.putString(BRTaskerReceiver.EXTRA_BLURB, restraintBlurb(input, getString(R.string.tasker_prefix_speak)));
        } else if (R.id.rbValue == checkedId) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "varOnly");
            }
            bundle.putBoolean(BRTaskerReceiver.EXTRA_VAR_ONLY, true);
            bundle.putString(BRTaskerReceiver.EXTRA_BLURB, restraintBlurb(input, getString(R.string.tasker_prefix_send)));
            final ArrayList<String> arrayList = Lists.newArrayList(Splitter.on(Constants.SEP_COMMA).trimResults().split(input));
            arrayList.removeAll(Collections.singleton(""));
            final int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                String variableName = arrayList.get(i);
                if (!isValid(variableName)) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "bad variable name: " + variableName);
                    }
                    ai.saiy.android.utils.UtilsToast.showToast(getApplicationContext(), getString(R.string.tasker_error_format) + " - " + variableName, Toast.LENGTH_LONG);
                    return;
                }
                String name = BRTaskerReceiver.EXTRA_VAR_VALUE_ + i;
                String value = BRTaskerReceiver.EXTRA_VAR_NAME_ + i;
                bundle.putString(name, variableName);
                bundle.putString(value, variableName);
            }
            bundle.putString(Speaker.EXTRA_VALUE, input);
            bundle.putInt(BRTaskerReceiver.EXTRA_VARIABLE_COUNT, size);
        }

        bundle.putBoolean(Speaker.EXTRA_START_VR, cbTaskerStartListening.isChecked());
        bundle.putString(Speaker.EXTRA_LOCALE, UtilsLocale.getDefaultLocale().toString());
        final Intent intent = new Intent();
        intent.putExtra(BRTaskerReceiver.EXTRA_BUNDLE, bundle);
        setResult(Activity.RESULT_OK, intent);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        this.isCancelled = true;
        super.onBackPressed();
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCheckedChanged: checkedId: " + checkedId);
        }
        if (R.id.rbNotify == checkedId) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onCheckedChanged: rdNotify");
            }
            etTaskerInput.setHint(R.string.variable_text_hint);
            cbTaskerStartListening.setEnabled(true);
        } else if (R.id.rbSpeak == checkedId) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onCheckedChanged: rbSpeak");
            }
            etTaskerInput.setHint(R.string.variable_text_hint);
            cbTaskerStartListening.setEnabled(true);
        } else if (R.id.rbValue == checkedId) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onCheckedChanged: rdValue");
            }
            etTaskerInput.setHint(R.string.variable_hint);
            cbTaskerStartListening.setChecked(false);
            cbTaskerStartListening.setEnabled(false);
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onCheckedChanged: default");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasker_speech_plugin_layout);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate: savedInstanceState: " + (savedInstanceState != null));
        }
        this.etTaskerInput = findViewById(R.id.etTaskerInput);
        this.cbTaskerStartListening = findViewById(R.id.cbTaskerStartListening);
        this.rgTaskerAction = findViewById(R.id.rgTaskerAction);
        rgTaskerAction.setOnCheckedChangeListener(this);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (savedInstanceState == null) {
            final Bundle forwardedBundle = getIntent().getBundleExtra(BRTaskerReceiver.EXTRA_BUNDLE);
            if (!ai.saiy.android.utils.UtilsBundle.isSuspicious(forwardedBundle)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "forwardedBundle naked");
                }
                etTaskerInput.setText("%");
                etTaskerInput.setSelection(1);
                return;
            }
            String string = forwardedBundle.getString(Speaker.EXTRA_VALUE);
            if (UtilsString.notNaked(string)) {
                etTaskerInput.setText(string);
                etTaskerInput.setSelection(string.length());
            }
            if (!forwardedBundle.getBoolean(BRTaskerReceiver.EXTRA_VAR_ONLY, false)) {
                switch (forwardedBundle.getInt(BRTaskerReceiver.EXTRA_ACTION, BRTaskerReceiver.ACTION_SPEAK)) {
                    case BRTaskerReceiver.ACTION_SPEAK:
                        rgTaskerAction.check(R.id.rbSpeak);
                        break;
                    case BRTaskerReceiver.ACTION_NOTIFY:
                        rgTaskerAction.check(R.id.rbNotify);
                        break;
                }
            } else {
                rgTaskerAction.check(R.id.rbValue);
                cbTaskerStartListening.setChecked(false);
                cbTaskerStartListening.setEnabled(false);
            }
            if (cbTaskerStartListening.isEnabled()) {
                cbTaskerStartListening.setChecked(forwardedBundle.getBoolean(Speaker.EXTRA_START_VR, false));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateOptionsMenu");
        }
        getMenuInflater().inflate(R.menu.menu_tasker_activity, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
        hideIME();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onOptionsItemSelected");
        }
        hideIME();
        final int menuItemId = menuItem.getItemId();
        if (android.R.id.home == menuItemId) {
            this.isCancelled = false;
            finish();
            return true;
        } else if (R.id.action_discard == menuItemId) {
            this.isCancelled = true;
            finish();
            return true;
        } else {
            this.isCancelled = true;
            finish();
            return super.onOptionsItemSelected(menuItem);
        }
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
    protected void onStart() {
        super.onStart();
        getSupportActionBar().setTitle(R.string.tasker_capital);
        getSupportActionBar().setSubtitle(R.string.tasker_subtitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}

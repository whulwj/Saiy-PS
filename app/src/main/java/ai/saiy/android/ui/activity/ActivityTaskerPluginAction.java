package ai.saiy.android.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.broadcast.BRTaskerReceiver;
import ai.saiy.android.cognitive.identity.provider.microsoft.Speaker;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsBundle;

public class ActivityTaskerPluginAction extends AppCompatActivity {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityTaskerPluginAction.class.getSimpleName();

    private RadioGroup rgTaskerAction;
    private boolean isCancelled;

    private String restraintBlurb(String str, String prefix) {
        String blurb = prefix + str;
        return blurb.length() > getResources().getInteger(R.integer.max_blurb_length) ? blurb.substring(0, getResources().getInteger(R.integer.max_blurb_length)) : blurb;
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
        String blurb;
        @BRTaskerReceiver.Action int action;
        final int checkedId = rgTaskerAction.getCheckedRadioButtonId();
        if (R.id.rbActivate == checkedId) {
            action = BRTaskerReceiver.ACTION_ACTIVATE;
            blurb = getString(R.string.menu_activate);
        } else if (R.id.rbDisableDrivingProfile == checkedId) {
            action = BRTaskerReceiver.ACTION_DISABLE_DRIVING_PROFILE;
            blurb = getString(R.string.menu_disable_driving_profile);
        } else if (R.id.rbDisableHotword == checkedId) {
            action = BRTaskerReceiver.ACTION_DISABLE_HOTWORD;
            blurb = getString(R.string.menu_disable_hotword);
        } else if (R.id.rbDisableNotificationAnnouncements == checkedId) {
            action = BRTaskerReceiver.ACTION_DISABLE_NOTIFICATIONS;
            blurb = getString(R.string.menu_disable_notification_announcements);
        } else if (R.id.rbEnableDrivingProfile == checkedId) {
            action = BRTaskerReceiver.ACTION_ENABLE_DRIVING_PROFILE;
            blurb = getString(R.string.menu_enable_driving_profile);
        } else if (R.id.rbEnableHotword == checkedId) {
            action = BRTaskerReceiver.ACTION_ENABLE_HOTWORD;
            blurb = getString(R.string.menu_enable_hotword);
        } else if (R.id.rbEnableNotificationAnnouncements == checkedId) {
            action = BRTaskerReceiver.ACTION_ENABLE_NOTIFICATIONS;
            blurb = getString(R.string.menu_enable_notification_announcements);
        } else if (R.id.rbShutdown == checkedId) {
            action = BRTaskerReceiver.ACTION_SHUTDOWN;
            blurb = getString(R.string.menu_shutdown);
        } else {
            action = BRTaskerReceiver.ACTION_ENABLE_HOTWORD;
            blurb = getString(R.string.menu_enable_hotword);
        }
        final Bundle bundle = new Bundle(4);
        bundle.putInt(BRTaskerReceiver.EXTRA_ACTION, action);
        bundle.putString(BRTaskerReceiver.EXTRA_BLURB, restraintBlurb(blurb, getString(R.string.tasker_prefix_action)));
        bundle.putString(Speaker.EXTRA_VALUE, "placeholder");
        bundle.putString(Speaker.EXTRA_LOCALE, Locale.getDefault().toString());
        final Intent data = new Intent();
        data.putExtra(BRTaskerReceiver.EXTRA_BUNDLE, bundle);
        setResult(Activity.RESULT_OK, data);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        this.isCancelled = true;
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasker_action_plugin_layout);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate: savedInstanceState: " + (savedInstanceState != null));
        }
        this.rgTaskerAction = findViewById(R.id.rgTaskerAction);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (savedInstanceState == null) {
            final Bundle forwardedBundle = getIntent().getBundleExtra(BRTaskerReceiver.EXTRA_BUNDLE);
            if (!UtilsBundle.notNaked(forwardedBundle)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "forwardedBundle naked");
                }
                rgTaskerAction.check(R.id.rbEnableHotword);
                return;
            }
            switch (forwardedBundle.getInt(BRTaskerReceiver.EXTRA_ACTION, BRTaskerReceiver.ACTION_ENABLE_HOTWORD)) {
                case BRTaskerReceiver.ACTION_ENABLE_HOTWORD:
                    rgTaskerAction.check(R.id.rbEnableHotword);
                    break;
                case BRTaskerReceiver.ACTION_DISABLE_HOTWORD:
                    rgTaskerAction.check(R.id.rbDisableHotword);
                    break;
                case BRTaskerReceiver.ACTION_ENABLE_DRIVING_PROFILE:
                    rgTaskerAction.check(R.id.rbEnableDrivingProfile);
                    break;
                case BRTaskerReceiver.ACTION_DISABLE_DRIVING_PROFILE:
                    rgTaskerAction.check(R.id.rbDisableDrivingProfile);
                    break;
                case BRTaskerReceiver.ACTION_ENABLE_NOTIFICATIONS:
                    rgTaskerAction.check(R.id.rbEnableNotificationAnnouncements);
                    break;
                case BRTaskerReceiver.ACTION_DISABLE_NOTIFICATIONS:
                    rgTaskerAction.check(R.id.rbDisableNotificationAnnouncements);
                    break;
                case BRTaskerReceiver.ACTION_ACTIVATE:
                    rgTaskerAction.check(R.id.rbActivate);
                    break;
                case BRTaskerReceiver.ACTION_SHUTDOWN:
                    rgTaskerAction.check(R.id.rbShutdown);
                    break;
                default:
                    break;
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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onOptionsItemSelected");
        }
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

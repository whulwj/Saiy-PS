package ai.saiy.android.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ai.saiy.android.R;
import ai.saiy.android.utils.MyLog;

public class ActivityShortcutPicker extends AppCompatActivity {
    public static final String EXTRA_SHORTCUT_NAME = "extra_shortcut_name";
    public static final String EXTRA_SHORTCUT_URI = "extra_shortcut_uri";
    public static final String EXTRA_PACKAGE = "extra_package";
    private static final int REQUEST_PICK_SHORTCUT = 1120;
    private static final int REQUEST_CREATE_SHORTCUT = 1121;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityShortcutPicker.class.getSimpleName();

    public void sendResult(String name, String uri) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sendResult");
        }
        final Intent intent = new Intent();
        int resultCode;
        if (name == null || uri == null) {
            resultCode = Activity.RESULT_CANCELED;
        } else {
            resultCode = Activity.RESULT_OK;
            intent.putExtra(EXTRA_SHORTCUT_NAME, name);
            intent.putExtra(EXTRA_SHORTCUT_URI, uri);
        }
        setResult(resultCode, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onActivityResult: " + requestCode + " ~ " + resultCode);
        }
        if (resultCode != Activity.RESULT_OK || data == null) {
            sendResult(null, null);
            return;
        }
        switch (requestCode) {
            case REQUEST_PICK_SHORTCUT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: REQUEST_PICK_SHORTCUT");
                }
                startActivityForResult(data, REQUEST_CREATE_SHORTCUT);
                break;
            case REQUEST_CREATE_SHORTCUT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: REQUEST_CREATE_SHORTCUT");
                }
                final Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
                if (intent == null || !data.hasExtra(Intent.EXTRA_SHORTCUT_NAME)) {
                    sendResult(null, null);
                    break;
                }
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "name: " + data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME));
                    MyLog.d(CLS_NAME, "uri: " + intent.toURI());
                }
                sendResult(data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME), intent.toUri(Intent.URI_INTENT_SCHEME));
                break;
            default:
                sendResult(null, null);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBackPressed");
        }
        sendResult(null, null);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        setFinishOnTouchOutside(false);
        final Intent createShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_PACKAGE)) {
            createShortcutIntent.setPackage(intent.getStringExtra(EXTRA_PACKAGE));
            try {
                startActivityForResult(createShortcutIntent, REQUEST_CREATE_SHORTCUT);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } catch (SecurityException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "SecurityException");
                    e.printStackTrace();
                }
                sendResult(null, null);
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "Exception");
                    e.printStackTrace();
                }
                sendResult(null, null);
            }
            return;
        }
        final Intent pickActivityIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickActivityIntent.putExtra(Intent.EXTRA_INTENT, createShortcutIntent);
        pickActivityIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_select_shortcut));
        try {
            startActivityForResult(pickActivityIntent, REQUEST_PICK_SHORTCUT);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } catch (SecurityException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "SecurityException");
                e.printStackTrace();
            }
            sendResult(null, null);
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
            sendResult(null, null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
    }
}

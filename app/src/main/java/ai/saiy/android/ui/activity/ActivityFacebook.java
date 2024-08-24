package ai.saiy.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import java.util.Collections;

import ai.saiy.android.R;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsBundle;
import ai.saiy.android.utils.UtilsString;

public class ActivityFacebook extends AppCompatActivity {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityFacebook.class.getSimpleName();

    public static final String EXTRA_REQUEST_TYPE = "request_type";
    public static final int TYPE_AUTH = 1;
    public static final int TYPE_DIALOG = 2;

    private CallbackManager callbackManager;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        setFinishOnTouchOutside(false);
        final Bundle extras = getIntent().getExtras();
        if (!UtilsBundle.notNaked(extras) || UtilsBundle.isSuspicious(extras)) {
            finish();
            return;
        }
        if (extras.containsKey(EXTRA_REQUEST_TYPE)) {
            switch (extras.getInt(EXTRA_REQUEST_TYPE, 0)) {
                case TYPE_AUTH:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "TYPE_AUTH");
                    }
                    this.callbackManager = CallbackManager.Factory.create();
                    LoginManager.getInstance().logInWithReadPermissions(this, Collections.singletonList("publish_actions"));
                    LoginManager.getInstance().registerCallback(callbackManager, new com.facebook.FacebookCallback<com.facebook.login.LoginResult>() {
                        @Override
                        public void onError(@NonNull FacebookException facebookException) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "onError");
                                facebookException.printStackTrace();
                            }
                            ai.saiy.android.utils.UtilsToast.showToast(getApplicationContext(), getString(R.string.facebook_error), Toast.LENGTH_LONG);
                            finish();
                        }

                        @Override
                        public void onSuccess(com.facebook.login.LoginResult result) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onSuccess");
                                MyLog.i(CLS_NAME, "onSuccess: token: " + result.getAccessToken());
                            }
                            if (DEBUG) {
                                for (String s : result.getRecentlyDeniedPermissions()) {
                                    MyLog.i(CLS_NAME, "deniedPermissions: " + s);
                                }
                                for (String s : result.getRecentlyGrantedPermissions()) {
                                    MyLog.i(CLS_NAME, "grantedPermissions: " + s);
                                }
                            }
                            finish();
                        }

                        @Override
                        public void onCancel() {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onCancel");
                            }
                            finish();
                        }
                    });
                    break;
                case TYPE_DIALOG:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "TYPE_DIALOG");
                    }
                    final String postContent = extras.getString(Intent.EXTRA_TEXT);
                    if (UtilsString.notNaked(postContent)) {
                        ai.saiy.android.command.clipboard.ClipboardHelper.setClipboardContent(getApplicationContext(), postContent);
                        ShareDialog.show(this, new ShareLinkContent.Builder().setQuote(postContent).build());
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "postContent naked");
                        }
                        ai.saiy.android.utils.UtilsToast.showToast(getApplicationContext(), getString(R.string.facebook_error), Toast.LENGTH_LONG);
                    }
                    finish();
                    break;
                default:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "TYPE_UNKNOWN");
                    }
                    finish();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
        super.onDestroy();
    }
}

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

package ai.saiy.android.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ai.saiy.android.R;
import ai.saiy.android.applications.Install;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.error.Issue;
import ai.saiy.android.error.IssueContent;
import ai.saiy.android.tts.helper.TTSDefaults;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 16/04/2016.
 */
public class ActivityIssue extends AppCompatActivity {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityIssue.class.getSimpleName();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }

        final Bundle bundle = getIntent().getExtras();

        if (bundle != null && !bundle.isEmpty()) {
            if (bundle.containsKey(Issue.ISSUE_CONTENT)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "bundle contains: IssueContent.ISSUE_CONTENT");
                }

                final IssueContent issueContent = bundle.getParcelable(Issue.ISSUE_CONTENT);

                if (issueContent != null) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "issueContent: " + issueContent.getIssueText());
                    }

                    switch (issueContent.getIssueConstant()) {
                        case Issue.ISSUE_NO_VR:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "ISSUE_NO_VR");
                            }
                            showNoVRDialog(issueContent.getIssueText());
                            return;
                        case Issue.ISSUE_NO_TTS_ENGINE:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "ISSUE_NO_TTS_ENGINE");
                            }
                            showNoTTSDialog(issueContent.getIssueText());
                            return;
                        case Issue.ISSUE_NO_TTS_LANGUAGE:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "ISSUE_NO_TTS_LANGUAGE");
                            }
                            break;
                        case Issue.ISSUE_VLINGO:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "ISSUE_VLINGO");
                            }
                            break;
                        default:
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "Issue default");
                            }
                            break;
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "bundle missing: IssueContent null");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "bundle missing: IssueContent.ISSUE_CONTENT");
            }
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
        super.onDestroy();
    }

    private void showNoVRDialog(String message) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showNoVRDialog");
        }
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.menu_voice_recognition)
                .setMessage(message)
                .setIcon(R.drawable.ic_info)
                .setNeutralButton(R.string.title_install, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showNoVRDialog: onNeutral");
                        }

                        Install.showInstallLink(ActivityIssue.this.getApplicationContext(), Installed.PACKAGE_NAME_GOOGLE_NOW);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showNoVRDialog: onNegative");
                        }
                        finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showNoVRDialog: onCancel");
                        }
                        finish();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
        materialDialog.show();
    }

    private void showNoTTSDialog(String message) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showNoTTSDialog");
        }
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.menu_tts)
                .setMessage(message)
                .setIcon(R.drawable.ic_info)
                .setNeutralButton(R.string.title_install, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showNoTTSDialog: onNeutral");
                        }

                        Install.showInstallLink(ActivityIssue.this.getApplicationContext(), TTSDefaults.TTS_PKG_NAME_GOOGLE);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showNoTTSDialog: onNegative");
                        }
                        finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showNoTTSDialog: onCancel");
                        }
                        finish();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
        materialDialog.show();
    }
}

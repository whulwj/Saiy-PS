package ai.saiy.android.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import ai.saiy.android.R;
import ai.saiy.android.command.horoscope.HoroscopeHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsBundle;

public class ActivityShowDialog extends AppCompatActivity {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityShowDialog.class.getSimpleName();
    private long then;

    private void showDOBDialog() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(this)
                .setView(R.layout.date_of_birth_dialog_layout)
                .setCancelable(false)
                .setTitle(R.string.menu_date_of_birth)
                .setIcon(R.drawable.ic_baby)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDOBDialog: onPositive");
                        }
                        if (dialog instanceof AlertDialog) {
                            DatePicker datePicker = ((AlertDialog) dialog).getWindow().findViewById(R.id.dobDatePicker);
                            HoroscopeHelper.calculateHoroscope(getApplicationContext(), datePicker.getDayOfMonth(), datePicker.getMonth(), datePicker.getYear());
                        }
                        dialog.dismiss();
                        ActivityShowDialog.this.finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDOBDialog: onNegative");
                        }
                        dialog.dismiss();
                        ActivityShowDialog.this.finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDOBDialog: onCancel");
                        }
                        dialog.dismiss();
                        ActivityShowDialog.this.finish();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        calendar.setLenient(true);
        calendar.set(SPH.getDobYear(getApplicationContext()), SPH.getDobMonth(getApplicationContext()), SPH.getDobDay(getApplicationContext()));
        ((DatePicker) materialDialog.getWindow().findViewById(R.id.dobDatePicker)).updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        setFinishOnTouchOutside(false);
        this.then = System.nanoTime();
        final Bundle extras = getIntent().getExtras();
        if (!UtilsBundle.notNaked(extras) || UtilsBundle.isSuspicious(extras)) {
            return;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "DIALOG_DOB");
        }
        showDOBDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
            MyLog.getElapsed(CLS_NAME, this.then);
        }
    }
}

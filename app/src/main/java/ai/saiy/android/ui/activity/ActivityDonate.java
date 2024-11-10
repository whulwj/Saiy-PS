package ai.saiy.android.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.user.UserFirebaseHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;
import ai.saiy.android.utils.UtilsToast;

public class ActivityDonate extends AppCompatActivity implements OnUserEarnedRewardListener {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityDonate.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        final AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        RewardedAd.load(this, getString(R.string.reward_id),
                adRequest, rewardedAdLoadCallback);
    }

    private boolean isActive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !isDestroyed() && !isFinishing();
        } else {
            return !isFinishing();
        }
    }

    /**
     * Utility method to toast making sure it's on the main thread
     *
     * @param text   to toast
     * @param duration one of {@link Toast#LENGTH_SHORT} {@link Toast#LENGTH_LONG}
     */
    private void toast(@Nullable final String text, final int duration) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "makeToast: " + text);
        }

        if (isActive() && UtilsString.notNaked(text)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UtilsToast.showToast(getApplicationContext(), text, duration);
                }
            });
        }
    }

    private void updateFirebase() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "updateFirebase");
        }
        new UserFirebaseHelper().updateUser(getApplicationContext(), 5L, DateUtils.DAY_IN_MILLIS);
    }

    private final RewardedAdLoadCallback rewardedAdLoadCallback = new RewardedAdLoadCallback() {
        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAdFailedToLoad:" + loadAdError);
            }
            toast(getString(R.string.ad_error_playback), Toast.LENGTH_LONG);
            finish();
        }

        @Override
        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAdLoaded");
            }
            if (isActive()) {
                rewardedAd.show(ActivityDonate.this, ActivityDonate.this);
            }
        }
    };

    @Override
    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onUserEarnedReward: " + rewardItem.getType() + ", " + rewardItem.getAmount());
        }
        if (!isActive()) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onUserEarnedReward: no longer active");
            }
            return;
        }
        String format;
        final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(getApplicationContext(), SupportedLanguage.getSupportedLanguage(SPH.getVRLocale(getApplicationContext())));
        if (SPH.getCreditsVerbose(getApplicationContext()) < 2) {
            SPH.incrementCreditsVerbose(getApplicationContext());
            format = String.format(sr.getString(R.string.donate_credit_addition), 5L, "24") + XMLResultsHandler.SEP_SPACE + sr.getString(R.string.donate_credit_verbose);
        } else {
            format = String.format(sr.getString(R.string.donate_credit_addition), 5L, "24");
        }
        sr.reset();
        final LocalRequest localRequest = new LocalRequest(getApplicationContext());
        localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, format);
        localRequest.execute();
        updateFirebase();
    }
}

package ai.saiy.android.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.viewmodel.ViewModelAdvertisement;
import ai.saiy.android.ui.viewmodel.ViewModelFirebaseAuth;
import ai.saiy.android.user.UserFirebaseHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class FragmentAdvertisement extends Fragment implements OnUserEarnedRewardListener {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentAdvertisement.class.getSimpleName();

    protected @Inject Handler mResultHandler;
    private Context mContext;
    private AdView adView;
    private InterstitialAd interstitialAd;
    private ProgressBar adProgress;
    private ViewModelFirebaseAuth viewModelFirebaseAuth;
    private ViewModelAdvertisement viewModelAdvertisement;
    private Timer timer;

    private boolean isAdLoaded;
    private final AtomicInteger retryCount = new AtomicInteger();
    private final AtomicBoolean isRewardLoading = new AtomicBoolean();
    private final AdListener adListener = new AdListener() {
        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            super.onAdFailedToLoad(loadAdError);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAdFailedToLoad");
            }
            if (isAdLoaded) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAdFailedToLoad: ad is displaying, leaving");
                }
            } else if (retryCount.incrementAndGet() < 6) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAdFailedToLoad: retrying");
                }
                if (timer != null) {
                    timer.cancel();
                }
                timer = new Timer(CLS_NAME);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (!isActive()) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "onAdFailedToLoad is Finishing");
                            }
                            return;
                        }
                        if (Boolean.TRUE.equals(viewModelFirebaseAuth.isAddFree().getValue())) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "userAdFree true");
                            }
                            return;
                        }
                        mResultHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (adView == null) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "onAdFailedToLoad fast quit");
                                    }
                                    return;
                                }
                                try {
                                    final AdRequest adRequest = new AdRequest.Builder().build();
                                    adView.loadAd(adRequest);
                                } catch (Throwable t) {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "onAdFailedToLoad retrying " + t.getClass().getSimpleName() + ", " + t.getMessage());
                                    }
                                    destroyAd();
                                }
                            }
                        });
                    }
                }, 5000L);
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAdFailedToLoad max retries");
                }
                destroyAd();
            }
        }

        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAdLoaded");
            }
            FragmentAdvertisement.this.isAdLoaded = true;
            adProgress.setVisibility(View.GONE);
            super.onAdLoaded();
        }
    };

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);
        this.mContext = context.getApplicationContext();
    }

    @Override
    public void onAttach(@NonNull final Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.mContext = activity.getApplicationContext();
        }
    }

    @Override
    public @Nullable View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.adview_float_bottom_main, container, false);
        final ViewModelProvider viewModelProvider = new ViewModelProvider(getActivity());
        this.viewModelFirebaseAuth = viewModelProvider.get(ViewModelFirebaseAuth.class);
        this.viewModelAdvertisement = viewModelProvider.get(ViewModelAdvertisement.class);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.adView = view.findViewById(R.id.adViewMain);
        this.adProgress = view.findViewById(R.id.adProgress);
        viewModelFirebaseAuth.isAddFree().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean value) {
                onDetermineAdFree(value);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adView != null) {
            adView.pause();
        }
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
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        destroyAd();
        destroyInterstitial();
    }

    public void setupAdView() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setupAdView");
        }
        adView.setAdListener(adListener);
        final AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    public void runInterstitial() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "runInterstitial");
        }
        if (Boolean.TRUE.equals(viewModelFirebaseAuth.isAddFree().getValue())) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "runInterstitial: userAdFree: true");
            }
            return;
        }

        final AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(getApplicationContext(), getString(R.string.interstitial_fragment_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                FragmentAdvertisement.this.interstitialAd = interstitialAd;
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAdLoaded: interstitial");
                }
                if (!Boolean.TRUE.equals(viewModelFirebaseAuth.isAddFree().getValue())) {
                    try {
                        interstitialAd.show(getParentActivity());
                    } catch (Throwable t) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "interstitial " + t.getClass().getSimpleName() + ", " + t.getMessage());
                        }

                    }
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "runInterstitial: userAdFree: true");
                }
            }
        });
    }

    public void initialiseReward() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "initialiseReward");
        }
        loadReward();
    }

    public void showReward() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showReward");
        }
        if (viewModelAdvertisement != null) {
            final RewardedAd rewardedAd = viewModelAdvertisement.getRewardedAd();
            if (rewardedAd != null) {
                rewardedAd.show(getParentActivity(), this);
                return;
            }
        }
        if (isRewardLoading.get()) {
            return;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "showReward: ad not loaded");
        }
        getParentActivity().toast(getString(R.string.ad_error_playback), Toast.LENGTH_LONG);
        loadReward();
    }

    private void destroyAd() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "destroyAd");
        }
        if (adProgress != null) {
            adProgress.setVisibility(View.GONE);
        }
        if (adView != null) {
            try {
                adView.setAdListener(null);
                adView.removeAllViews();
                final ViewGroup viewGroup = ((ViewGroup) adView.getParent());
                viewGroup.removeView(adView);
                viewGroup.removeView(adProgress);
                adView.destroyDrawingCache();
                adView.invalidate();
                adView.destroy();
                this.adView = null;
            } catch (Throwable t) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "destroyAd " + t.getClass().getSimpleName() + ", " + t.getMessage());
                }
            }
        }
    }

    private void destroyInterstitial() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "destroyInterstitial");
        }
        if (interstitialAd != null) {
            interstitialAd = null;
        }
    }

    private void loadReward() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "loadReward");
        }
        final AdRequest adRequest = new AdRequest.Builder().build();
        isRewardLoading.set(true);
        RewardedAd.load(getParentActivity(), getString(R.string.reward_id),
                adRequest, rewardedAdLoadCallback);
    }

    private final RewardedAdLoadCallback rewardedAdLoadCallback = new RewardedAdLoadCallback() {
        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            isRewardLoading.set(false);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAdFailedToLoad:" + loadAdError);
            }
            viewModelAdvertisement.setRewardedAd(null);
        }

        @Override
        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
            isRewardLoading.set(false);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAdLoaded");
            }
            viewModelAdvertisement.setRewardedAd(rewardedAd);
            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onAdDismissedFullScreenContent");
                    }
                    if (isRewardLoading.get()) {
                        return;
                    }
                    loadReward();
                }
            });
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
        getParentActivity().speak(format, LocalRequest.ACTION_SPEAK_ONLY);
        updateFirebase();
        viewModelFirebaseAuth.isAddFree().postValue(true);
    }

    @UiThread
    private void onDetermineAdFree(boolean isAddFree) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDetermineAdFree: " + isAddFree);
        }
        if (isAddFree && isActive()) {
            destroyAd();
            destroyInterstitial();
        }
    }

    private void updateFirebase() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "updateFirebase");
        }
        new UserFirebaseHelper().updateUser(getApplicationContext(), 5L, DateUtils.DAY_IN_MILLIS);
    }

    public boolean isActive() {
        return getActivity() != null && getParentActivity().isActive() && isAdded() && !isRemoving();
    }

    /**
     * Utility to return the parent activity neatly cast. No need for instanceOf as this fragment will
     * never be attached to another activity.
     *
     * @return the {@link ActivityHome} parent
     */
    public ActivityHome getParentActivity() {
        return (ActivityHome) getActivity();
    }

    /**
     * Utility method to ensure we double check the context being used.
     *
     * @return the application context
     */
    public Context getApplicationContext() {
        return this.mContext;
    }
}

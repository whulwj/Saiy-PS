package ai.saiy.android.ui.viewmodel;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.ads.rewarded.RewardedAd;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class ViewModelAdvertisement extends ViewModel  {
    private RewardedAd rewardedAd;

    public @Nullable RewardedAd getRewardedAd() {
        return rewardedAd;
    }

    public void setRewardedAd(@Nullable RewardedAd rewardedAd) {
        this.rewardedAd = rewardedAd;
    }
}

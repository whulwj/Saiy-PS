package ai.saiy.android.advertisement;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.ump.FormError;
import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Inject;

import ai.saiy.android.ui.activity.CurrentActivityProvider;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.PrivacyRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public final class ViewModelConsent extends AndroidViewModel implements GoogleMobileAdsConsentManager.OnConsentGatheringCompleteListener {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = "ViewModelConsent";
    private final MutableLiveData<Boolean> mIsEEAAdFree = new MutableLiveData<>();
    private final GoogleMobileAdsConsentManager mConsentManager;
    private final CurrentActivityProvider mActivityProvider;

    public @Inject ViewModelConsent(Application application, CurrentActivityProvider activityProvider) {
        super(application);
        this.mActivityProvider = activityProvider;
        this.mConsentManager = GoogleMobileAdsConsentManager.getInstance(application);
//        final List<String> testDeviceIds = Collections.singletonList("C88A1847EC746D0F448033595A47D851");
        final byte rangeOfAge = PrivacyRepository.getRangeOfAge(application.getApplicationContext());
        final boolean canShowPersonalizedAds;
        if (ConsentHelper.isGDPR(application)) {
            canShowPersonalizedAds = ConsentHelper.canShowPersonalizedAds(application);
        } else {
            canShowPersonalizedAds = true;
        }
        final RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder()
                .setTagForChildDirectedTreatment((PrivacyRepository.AgeRange.CHILD == rangeOfAge) ? RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE : RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED)
                .setTagForUnderAgeOfConsent((PrivacyRepository.AgeRange.MIDDLE_ADOLESCENT == rangeOfAge) ? RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE : RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED)
                .setMaxAdContentRating(((PrivacyRepository.AgeRange.CHILD == rangeOfAge) ? RequestConfiguration.MAX_AD_CONTENT_RATING_G : (PrivacyRepository.AgeRange.MIDDLE_ADOLESCENT == rangeOfAge) ? RequestConfiguration.MAX_AD_CONTENT_RATING_T :
                        (PrivacyRepository.AgeRange.ADOLESCENT == rangeOfAge) ? RequestConfiguration.MAX_AD_CONTENT_RATING_PG : RequestConfiguration.MAX_AD_CONTENT_RATING_MA))
                .setPublisherPrivacyPersonalizationState(canShowPersonalizedAds ? RequestConfiguration.PublisherPrivacyPersonalizationState.DEFAULT : RequestConfiguration.PublisherPrivacyPersonalizationState.DISABLED)
//                .setTestDeviceIds(testDeviceIds)
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);
        FirebaseAnalytics.getInstance(application).setUserProperty(FirebaseAnalytics.UserProperty.ALLOW_AD_PERSONALIZATION_SIGNALS, String.valueOf(canShowPersonalizedAds));
    }

    public @NonNull GoogleMobileAdsConsentManager getConsentManager() {
        return mConsentManager;
    }

    public @NonNull LiveData<Boolean> isEEAAdFree() {
        return mIsEEAAdFree;
    }

    /**
     * Gets the users consent status and either requests consent or displays the appropriate ad mode
     */
    public void getConsentStatus(Activity activity) {
        mConsentManager.gatherConsent(activity, this::consentGatheringComplete,
                formError -> {
                    consentGatheringComplete(formError);
                    logConsentChoices(getApplication());
                });
    }

    @Override
    public void consentGatheringComplete(@Nullable FormError consentError) {
        if (consentError != null) {
            // Consent not obtained in current session.
            if (DEBUG) {
                MyLog.w(CLS_NAME,
                        "Consent Error: "
                                + String.format(
                                "%s: %s", consentError.getErrorCode(), consentError.getMessage()));
            }
        }

        // Consent has been gathered.
        if (DEBUG && !mConsentManager.canRequestAds()) {
            MyLog.i(CLS_NAME, "Consent not available to request ads");
        }

        // Check ConsentInformation.getPrivacyOptionsRequirementStatus() to see the menu item  be shown or hidden.
        if (mConsentManager.areGDPRConsentMessagesRequired()) {
            // Regenerate the options menu to include a privacy setting.
            mActivityProvider.withActivity(activity -> {
                if (activity == null || activity.isDestroyed() || activity.isFinishing() || activity.isChangingConfigurations()) {
                    return;
                }
                activity.invalidateOptionsMenu();
            });
        }
    }

    private void logConsentChoices(Context context) {
        // After completing the consent workflow, check the
        // strings in SharedPreferences to see what they
        // consented to and act accordingly
        final boolean canShow = ConsentHelper.canShowAds(context);
        final boolean isEEA = ConsentHelper.isGDPR(context);
        final boolean canShowPersonalizedAds;
        if (isEEA) {
            canShowPersonalizedAds = ConsentHelper.canShowPersonalizedAds(context);
        } else {
            canShowPersonalizedAds = true;
        }
        // Check what level of consent the user actually provided
        if (DEBUG) {
            MyLog.d(CLS_NAME, "Consent Choices is EEA =" + isEEA + ", can show ads = " + canShow + ", personalized ads = " + ConsentHelper.canShowPersonalizedAds(context));
        }

        final RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder()
                .setPublisherPrivacyPersonalizationState(canShowPersonalizedAds ? RequestConfiguration.PublisherPrivacyPersonalizationState.DEFAULT : RequestConfiguration.PublisherPrivacyPersonalizationState.DISABLED)
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);
        FirebaseAnalytics.getInstance(context).setUserProperty(FirebaseAnalytics.UserProperty.ALLOW_AD_PERSONALIZATION_SIGNALS, String.valueOf(canShowPersonalizedAds));
        final boolean isEEAAdFree = isEEA && !mConsentManager.canRequestAds();
        if (isEEAAdFree ^ Boolean.TRUE.equals(mIsEEAAdFree.getValue())) {
            mIsEEAAdFree.postValue(isEEAAdFree);
        }
    }

    public void showPrivacyOptionsForm(@NonNull Activity activity, @NonNull View view) {
        mConsentManager.showPrivacyOptionsForm(
                activity,
                formError -> {
                    if (formError != null) {
                        // Handle the error.
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "Failed to present privacy options form: " +
                                    formError.getErrorCode() + ", " + formError.getMessage());
                        }
                        Snackbar.make(activity, view, formError.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                    logConsentChoices(getApplication());
                }
        );
    }
}

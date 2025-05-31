package ai.saiy.android.advertisement;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.ump.ConsentForm.OnConsentFormDismissedListener;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

import ai.saiy.android.utils.PrivacyRepository;

/**
 * Manages app user's consent using the Google User Messaging Platform SDK or a Consent Management
 * Platform (CMP) that has been certified by Google.
 * See <a href="https://support.google.com/admanager/answer/10113209" /> for more information about GDPR messages for apps.
 * See also <a href="https://support.google.com/adsense/answer/13554116" /> for more information about
 * Google consent management requirements for serving ads in the EEA and UK.
 */
public final class GoogleMobileAdsConsentManager {
    private static GoogleMobileAdsConsentManager INSTANCE;
    private final ConsentInformation mConsentInformation;

    /** Interface definition for a callback to be invoked when consent gathering is complete. */
    public interface OnConsentGatheringCompleteListener {
        void consentGatheringComplete(FormError error);
    }

    /** Private constructor. */
    private GoogleMobileAdsConsentManager(Context context) {
        this.mConsentInformation = UserMessagingPlatform.getConsentInformation(context);
    }

    /** Public constructor. */
    public static GoogleMobileAdsConsentManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new GoogleMobileAdsConsentManager(context);
        }

        return INSTANCE;
    }

    /**
     * Returns true if the app has the user consent for showing ads.
     * Note: canRequestAds() always returns false until you have called requestConsentInfoUpdate()
     */
    public boolean canRequestAds() {
        return mConsentInformation.canRequestAds();
    }

    /** Helper function to determine if GDPR consent messages are required. */
    public boolean areGDPRConsentMessagesRequired() {
        return mConsentInformation.getPrivacyOptionsRequirementStatus()
                == PrivacyOptionsRequirementStatus.REQUIRED;
    }

    /** Load remote updates of consent messages and gather previously cached user consent. */
    public void gatherConsent(Activity activity, @NonNull ConsentInformation.OnConsentInfoUpdateFailureListener onConsentInfoUpdateFailureListener,
                              @NonNull OnConsentFormDismissedListener onConsentFormDismissedListener) {
        // For testing purposes, you can force a DebugGeography of EEA or NOT_EEA.
/*        ConsentDebugSettings debugSettings =
                new ConsentDebugSettings.Builder(activity)
                         .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                        // Check your logcat output for the hashed device ID, such as
                        // "Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("ABCDEF012345")" to use
                        // the debug functionality.
                        .addTestDeviceHashedId("C88A1847EC746D0F448033595A47D851")
                        .build();*/

        // Set up parameters for this sample app to download a consent request form. If your app has
        // different consent requirements, change this parameter to make appropriate consent requests.
        ConsentRequestParameters params =
                new ConsentRequestParameters.Builder()
                        // Set the following tag to false to indicate that your app users are not under the
                        // age of consent.
                        .setTagForUnderAgeOfConsent(PrivacyRepository.isUnderAgeOfConsent(activity))
//                        .setConsentDebugSettings(debugSettings)
                        .build();

        // Requesting an update to consent information should be called on every app launch.
        mConsentInformation.requestConsentInfoUpdate(
                activity,
                params,
                () ->
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                                activity,
                                formError -> {
                                    // Consent gathering failed if formError is not null, otherwise maybe consent has been gathered.
                                    onConsentFormDismissedListener.onConsentFormDismissed(formError);
                                }),
                requestConsentError ->
                        // Consent gathering failed.
                        onConsentInfoUpdateFailureListener.onConsentInfoUpdateFailure(requestConsentError));
    }

    /** Shows a form to app users for collecting their consent. */
    public void showPrivacyOptionsForm(
            Activity activity, OnConsentFormDismissedListener onConsentFormDismissedListener) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener);
    }
}

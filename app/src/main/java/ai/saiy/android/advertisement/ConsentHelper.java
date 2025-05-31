package ai.saiy.android.advertisement;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.IntDef;
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.saiy.android.utils.MyLog;

/**
 * Code added for consent and non-consent checks
 * <a href="https://itnext.io/android-admob-consent-with-ump-personalized-or-non-personalized-ads-in-eea-3592e192ec90" />
 * <a href="https://stackoverflow.com/questions/65351543/how-to-implement-ump-sdk-correctly-for-eu-consent/68310602#68310602" />
 */
public abstract class ConsentHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = "ConsentHelper";
    private static final int GOOGLE_ID = 755;

    public static boolean isGDPR(Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        final int gdprApplies = preferences.getInt(IAB_TCF.IABTCF_gdprApplies, GDPRApplies.UNKNOWN); // 1 - GDPR Applies, 0 - GDPR Does not apply
        return gdprApplies == GDPRApplies.GDPR_APPLIES;
    }

    public static boolean canShowAds(Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String purposeConsent = preferences.getString(IAB_TCF.IABTCF_PurposeConsents, "");
        String vendorConsent = preferences.getString(IAB_TCF.IABTCF_VendorConsents, "");
        String vendorLI = preferences.getString(IAB_TCF.IABTCF_VendorLegitimateInterests, "");
        String purposeLI = preferences.getString(IAB_TCF.IABTCF_PurposeLegitimateInterests, "");

        boolean hasGoogleVendorConsent = hasAttribute(vendorConsent, GOOGLE_ID);
        boolean hasGoogleVendorLI = hasAttribute(vendorLI, GOOGLE_ID);

        List<Integer> indexes = Collections.singletonList(DataUsagePurpose.storeAndAccessInformationOnADevice);

        List<Integer> indexesLI = new ArrayList<>(4);
        indexesLI.add(DataUsagePurpose.selectBasicAds);
        indexesLI.add(DataUsagePurpose.measureAdPerformance);
        indexesLI.add(DataUsagePurpose.applyMarketResearchToGenerateAudienceInsights);
        indexesLI.add(DataUsagePurpose.developAndImproveProducts);

        // Minimum required for at least non-personalized ads
        return hasConsentFor(indexes, purposeConsent, hasGoogleVendorConsent)
                && hasConsentOrLegitimateInterestFor(indexesLI, purposeConsent, purposeLI, hasGoogleVendorConsent, hasGoogleVendorLI);
    }

    public static boolean canShowPersonalizedAds(Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String purposeConsent = preferences.getString(IAB_TCF.IABTCF_PurposeConsents, "");
        String vendorConsent = preferences.getString(IAB_TCF.IABTCF_VendorConsents, "");
        String vendorLI = preferences.getString(IAB_TCF.IABTCF_VendorLegitimateInterests, "");
        String purposeLI = preferences.getString(IAB_TCF.IABTCF_PurposeLegitimateInterests, "");

        boolean hasGoogleVendorConsent = hasAttribute(vendorConsent, GOOGLE_ID);
        boolean hasGoogleVendorLI = hasAttribute(vendorLI, GOOGLE_ID);

        List<Integer> indexes = new ArrayList<>(3);
        indexes.add(DataUsagePurpose.storeAndAccessInformationOnADevice);
        indexes.add(DataUsagePurpose.createAPersonalisedAdsProfile);
        indexes.add(DataUsagePurpose.selectPersonalisedAds);

        List<Integer> indexesLI = new ArrayList<>(4);
        indexesLI.add(DataUsagePurpose.selectBasicAds);
        indexesLI.add(DataUsagePurpose.measureAdPerformance);
        indexesLI.add(DataUsagePurpose.applyMarketResearchToGenerateAudienceInsights);
        indexesLI.add(DataUsagePurpose.developAndImproveProducts);

        return hasConsentFor(indexes, purposeConsent, hasGoogleVendorConsent)
                && hasConsentOrLegitimateInterestFor(indexesLI, purposeConsent, purposeLI, hasGoogleVendorConsent, hasGoogleVendorLI);
    }

    // Check if a binary string has a "1" at position "index" (1-based)
    private static boolean hasAttribute(String input, int index) {
        if (input == null) return false;
        return input.length() >= index && input.charAt(index - 1) == '1';
    }

    // Check if consent is given for a list of purposes
    private static boolean hasConsentFor(List<Integer> indexes, String purposeConsent, boolean hasVendorConsent) {
        for (Integer index : indexes) {
            if (!hasAttribute(purposeConsent, index)) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "hasConsentFor: denied for purpose #" + index);
                }
                return false;
            }
        }
        return hasVendorConsent;
    }

    // Check if a vendor either has consent or legitimate interest for a list of purposes
    private static boolean hasConsentOrLegitimateInterestFor(List<Integer> indexes, String purposeConsent, String purposeLI, boolean hasVendorConsent, boolean hasVendorLI) {
        for (Integer index : indexes) {
            boolean purposeAndVendorLI = hasAttribute(purposeLI, index) && hasVendorLI;
            boolean purposeConsentAndVendorConsent = hasAttribute(purposeConsent, index) && hasVendorConsent;
            boolean isOk = purposeAndVendorLI || purposeConsentAndVendorConsent;
            if (!isOk) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "hasConsentOrLegitimateInterestFor: denied for #" + index);
                }
                return false;
            }
        }
        return true;
    }

    // TCF stands for Transparency & Consent Framework
    @StringDef({IAB_TCF.IABTCF_gdprApplies, IAB_TCF.IABTCF_PurposeConsents, IAB_TCF.IABTCF_VendorConsents, IAB_TCF.IABTCF_PurposeLegitimateInterests, IAB_TCF.IABTCF_VendorLegitimateInterests})
    public @interface IAB_TCF {
        String IABTCF_gdprApplies = "IABTCF_gdprApplies";
        String IABTCF_PurposeConsents = "IABTCF_PurposeConsents";
        String IABTCF_VendorConsents = "IABTCF_VendorConsents";
        String IABTCF_PurposeLegitimateInterests = "IABTCF_PurposeLegitimateInterests";
        String IABTCF_VendorLegitimateInterests = "IABTCF_VendorLegitimateInterests";
    }

    @IntDef({GDPRApplies.UNKNOWN, GDPRApplies.GDPR_DOES_NOT_APPLY, GDPRApplies.GDPR_APPLIES})
    public @interface GDPRApplies {
        int UNKNOWN = -1;
        int GDPR_DOES_NOT_APPLY = 0;
        int GDPR_APPLIES = 1;
    }

    @IntDef({DataUsagePurpose.storeAndAccessInformationOnADevice, DataUsagePurpose.selectBasicAds, DataUsagePurpose.createAPersonalisedAdsProfile,
            DataUsagePurpose.selectPersonalisedAds, DataUsagePurpose.createAPersonalisedContentProfile, DataUsagePurpose.selectPersonalisedContent,
            DataUsagePurpose.measureAdPerformance, DataUsagePurpose.measureContentPerformance, DataUsagePurpose.applyMarketResearchToGenerateAudienceInsights,
            DataUsagePurpose.developAndImproveProducts, DataUsagePurpose.useLimitedDataToSelectContent})
    @Retention(RetentionPolicy.SOURCE)
    private @interface DataUsagePurpose {
        int storeAndAccessInformationOnADevice = 1;
        int selectBasicAds = 2;
        int createAPersonalisedAdsProfile = 3;
        int selectPersonalisedAds = 4;
        int createAPersonalisedContentProfile = 5;
        int selectPersonalisedContent = 6;
        int measureAdPerformance = 7;
        int measureContentPerformance = 8;
        int applyMarketResearchToGenerateAudienceInsights = 9;
        int developAndImproveProducts = 10;
        int useLimitedDataToSelectContent = 11;
    }
}

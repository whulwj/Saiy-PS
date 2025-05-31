package ai.saiy.android.utils;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalDate;
import java.time.Period;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class PrivacyRepository {
    /**
     * {@link java.time.Month#JANUARY}.getValue()<br/>
     * Use the constant value to avoid a native crash on API 23: art/runtime/entrypoints/quick/quick_trampoline_entrypoints.cc:923] Check failed: called != nullptr int j$.time.Month.getValue() j$.time.Month[] virtual 22
     */
    private static final int JANUARY = 1;

    public static boolean isUnderAgeOfConsent(final Context context) {
        return getRangeOfAge(context) == AgeRange.CHILD;
    }

    public static @NonNull LocalDate getDateOfBirth(final Context context) {
        return LocalDate.of(SPH.getDobYear(context), SPH.getDobMonth(context) + JANUARY, SPH.getDobDay(context));
    }

    public static void setDateOfBirth(int year, int monthOfYear, int dayOfMonth) {
        final LocalDate dateOfBirth = LocalDate.of(year, monthOfYear, dayOfMonth);
        final byte rangeOfAge = PrivacyRepository.getRangeOfAge(dateOfBirth);
        final RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder()
                .setTagForChildDirectedTreatment((AgeRange.CHILD == rangeOfAge) ? RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE : RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED)
                .setTagForUnderAgeOfConsent((AgeRange.MIDDLE_ADOLESCENT == rangeOfAge) ? RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE : RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED)
                .setMaxAdContentRating(((AgeRange.CHILD == rangeOfAge) ? RequestConfiguration.MAX_AD_CONTENT_RATING_G : (AgeRange.MIDDLE_ADOLESCENT == rangeOfAge) ? RequestConfiguration.MAX_AD_CONTENT_RATING_T :
                        (AgeRange.ADOLESCENT == rangeOfAge) ? RequestConfiguration.MAX_AD_CONTENT_RATING_PG : RequestConfiguration.MAX_AD_CONTENT_RATING_MA))
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);
    }

    public static @AgeRange byte getRangeOfAge(Context context) {
        final LocalDate dateOfBirth = getDateOfBirth(context);
        return getRangeOfAge(dateOfBirth);
    }

    private static @AgeRange byte getRangeOfAge(@NonNull LocalDate dateOfBirth) {
        final Period period = Period.between(dateOfBirth, LocalDate.now());
        if (period.getYears() <= AgeRange.CHILD) {
            return AgeRange.CHILD;
        } else if (period.getYears() <= AgeRange.MIDDLE_ADOLESCENT) {
            return AgeRange.MIDDLE_ADOLESCENT;
        } else if (period.getYears() <= AgeRange.ADOLESCENT) {
            return AgeRange.ADOLESCENT;
        }
        return AgeRange.ADULT;
    }

    public static @NonNull AdRequest buildAdRequest(Context context) {
        final Bundle networkExtrasBundle = new Bundle();
        if (PrivacyRepository.AgeRange.CHILD == PrivacyRepository.getRangeOfAge(context)) {
            networkExtrasBundle.putInt("rdp", 1); //https://business.safety.google/rdp/
        }
        return new AdManagerAdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, networkExtrasBundle)
                .build();
    }

    @IntDef({AgeRange.CHILD, AgeRange.MIDDLE_ADOLESCENT, AgeRange.ADOLESCENT, AgeRange.ADULT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AgeRange {
        byte CHILD = 12;
        byte MIDDLE_ADOLESCENT = 15;
        byte ADOLESCENT = 17;
        byte ADULT = 18;
    }
}

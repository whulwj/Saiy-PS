package ai.saiy.android.location;

import android.location.Location;

abstract class UtilsLocatio {
    private static final int MAX_LATITUDE = 90;
    private static final int MAX_LONGITUDE = 180;
    private static final double MAX_LATITUDE_BY_FLOAT = MAX_LATITUDE;
    private static final double MAX_LONGITUDE_BY_FLOAT = MAX_LONGITUDE;
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else {
            return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
        }
    }

    /** Checks whether two providers are the same */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public static boolean isNearBy(double firstLatitude, double firstLongitude,
                                   double secondLatitude, double secondLongitude) {
        double minNormal = Math.ulp(MAX_LATITUDE_BY_FLOAT);
        if (Math.abs(firstLatitude - secondLatitude) > minNormal) {
            return false;
        }
        minNormal = Math.ulp(MAX_LONGITUDE_BY_FLOAT);
        return !(Math.abs(firstLongitude - secondLongitude) > minNormal);
    }
}

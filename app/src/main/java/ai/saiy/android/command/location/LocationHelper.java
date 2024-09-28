package ai.saiy.android.command.location;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.List;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

public class LocationHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = LocationHelper.class.getSimpleName();

    private @NonNull Pair<Boolean, String> resolveAddress(Context context, SupportedLanguage supportedLanguage, List<Address> list) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "resolveAddress");
        }
        if (UtilsList.notNaked(list)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "addressList: size: " + list.size());
            }
            final Address address = list.get(0);
            if (address != null && address.getMaxAddressLineIndex() > -1) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "address: getAdminArea: " + address.getAddressLine(0));
                    MyLog.i(CLS_NAME, "address: getAdminArea: " + address.getAdminArea());
                    MyLog.i(CLS_NAME, "address: getCountryName: " + address.getCountryName());
                    MyLog.i(CLS_NAME, "address: getFeatureName: " + address.getFeatureName());
                    MyLog.i(CLS_NAME, "address: getLocality: " + address.getLocality());
                    MyLog.i(CLS_NAME, "address: getPremises: " + address.getPremises());
                    MyLog.i(CLS_NAME, "address: getSubAdminArea: " + address.getSubAdminArea());
                    MyLog.i(CLS_NAME, "address: getSubLocality: " + address.getSubLocality());
                    MyLog.i(CLS_NAME, "address: getThoroughfare: " + address.getThoroughfare());
                    MyLog.i(CLS_NAME, "address: getSubThoroughfare: " + address.getSubThoroughfare());
                }
                final String addressLine = address.getAddressLine(0);
                final String thoroughfare = address.getThoroughfare();
                final String locality = address.getLocality();
                final String subAdminArea = address.getSubAdminArea();
                return (UtilsString.notNaked(thoroughfare) && UtilsString.notNaked(locality)) ? new Pair<>(true, thoroughfare + XMLResultsHandler.SEP_SPACE + ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.in) + XMLResultsHandler.SEP_SPACE + locality) : (UtilsString.notNaked(thoroughfare) && UtilsString.notNaked(subAdminArea)) ? new Pair<>(true, thoroughfare + XMLResultsHandler.SEP_SPACE + ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.in) + XMLResultsHandler.SEP_SPACE + subAdminArea) : UtilsString.notNaked(thoroughfare) ? new Pair<>(true, thoroughfare) : UtilsString.notNaked(addressLine) ? new Pair<>(true, addressLine) : new Pair<>(false, null);
            }
            if (DEBUG) {
                MyLog.w(CLS_NAME, "address naked");
            }
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "addressList: naked");
        }
        return new Pair<>(false, null);
    }

    public @Nullable Location getLastKnownLocation(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getLastKnownLocation");
        }
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        final List<String> providers = locationManager.getProviders(true);
        Location location = null;
        for (int i = providers.size() - 1; i >= 0; --i) {
            try {
                location = locationManager.getLastKnownLocation(providers.get(i));
            } catch (SecurityException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "SecurityException");
                }
            }
            if (location != null) {
                return location;
            }
        }
        return null;
    }

    public @NonNull Pair<Boolean, String> getAddress(Context context, SupportedLanguage sl, @NonNull Location location) {
        final long then = System.nanoTime();
        try {
            final android.location.Geocoder geocoder = new android.location.Geocoder(context, UtilsLocale.getDefaultLocale());
            final double latitude = location.getLatitude();
            final double longitude = location.getLongitude();
            final List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            return resolveAddress(context, sl, addresses);
        } catch (IllegalArgumentException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "IllegalArgumentException");
            }
        } catch (java.io.IOException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "IOException");
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "NullPointerException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "Exception");
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return new Pair<>(false, null);
    }
}

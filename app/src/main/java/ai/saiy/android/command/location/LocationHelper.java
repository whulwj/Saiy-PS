package ai.saiy.android.command.location;

import android.Manifest;
import android.content.Context;
import android.location.Address;
import android.location.LocationManager;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.PermissionChecker;
import androidx.core.location.LocationManagerCompat;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.List;
import java.util.concurrent.TimeUnit;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.location.AndroidLocationService;
import ai.saiy.android.location.LocalLocation;
import ai.saiy.android.location.LocationService;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

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

    public @Nullable LocalLocation getLocation(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getLocation");
        }
        if (PermissionChecker.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PermissionChecker.PERMISSION_GRANTED &&
                PermissionChecker.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PermissionChecker.PERMISSION_GRANTED) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "no location permission");
            }
            return null;
        }
        final LocationService locationService = new AndroidLocationService(context);
        return locationService.getCurrentLocation(true, LocationHelper.isLocationEnabled(context)? LocationService.LOCATION_REQUEST_TIME_OUT:LocationService.LOCAL_LOCATION_REQUEST_TIME_OUT, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .onErrorComplete(throwable -> {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onError: " + throwable.getClass().getSimpleName() + ", " + throwable.getMessage());
                    }
                    return true;
                }).blockingGet();
    }

    public @NonNull Pair<Boolean, String> getAddress(Context context, SupportedLanguage sl, @NonNull LocalLocation location) {
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

    /**
     * Adapted from <a href="http://stackoverflow.com/a/22980843/4248895" />.
     */
    public static boolean isLocationEnabled(Context context) {
        final LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return LocationManagerCompat.isLocationEnabled(lm);
    }
}

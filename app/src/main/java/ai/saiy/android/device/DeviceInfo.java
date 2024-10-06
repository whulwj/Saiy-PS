/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.device;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionService;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.common.io.BaseEncoding;
import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import ai.saiy.android.BuildConfig;
import ai.saiy.android.R;
import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.firebase.database.read.IAPVersion;
import ai.saiy.android.firebase.database.reference.IAPVersionReference;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

/**
 * Utility class for quick access to device specific settings
 * <p>
 * Created by benrandall76@gmail.com on 25/08/2016.
 */

public class DeviceInfo {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = DeviceInfo.class.getSimpleName();

    /**
     * Utility method to prepare certain device information to send along with any feedback.
     *
     * @param ctx the application context
     * @return a formatted string containing the required device info.
     */
    public static String getDeviceInfo(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getDeviceInfo");
        }

        return "\n\n\n" +
                "--------------------" +
                "\n" +
                ctx.getString(R.string.app_name) +
                " V" +
                BuildConfig.VERSION_NAME +
                "B\n" +
                ctx.getString(R.string.model) +
                ": " +
                Build.MODEL +
                "\n" +
                ctx.getString(R.string.manufacturer) +
                ": " +
                Build.MANUFACTURER +
                "\n" +
                ctx.getString(R.string.android) +
                ": " +
                Build.VERSION.SDK_INT +
                "\n" +
                ctx.getString(R.string.locale) +
                ": " +
                UtilsLocale.getDefaultLocale() +
                "\n" +
                "VR " + ctx.getString(R.string.locale) +
                ": " +
                SPH.getVRLocale(ctx) +
                "\n" +
                "TTS " + ctx.getString(R.string.locale) +
                ": " +
                SPH.getTTSLocale(ctx) +
                "\n" +
                "TTS " + ctx.getString(R.string.engine) +
                ": " +
                getDefaultTTSProvider(ctx) +
                "\n" +
                "VR " + ctx.getString(R.string.engine) +
                ": " +
                getDefaultVRProvider(ctx) +
                "\n" +
                "--------------------";
    }

    /**
     * Get the default Text to Speech provider
     *
     * @param ctx the application context
     * @return the default or an empty string if one is not present
     */
    public static String getDefaultTTSProvider(@NonNull final Context ctx) {
        final String engine = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.TTS_DEFAULT_SYNTH);
        return UtilsString.notNaked(engine) ? engine : getDefaultTTSProviderIntent(ctx);
    }

    /**
     * Get the default voice recognition provider
     *
     * @param ctx the application context
     * @return the default or an empty string if one is not present
     */
    public static String getDefaultVRProvider(@NonNull final Context ctx) {
        final List<ResolveInfo> services = ctx.getPackageManager().queryIntentServices(
                new Intent(RecognitionService.SERVICE_INTERFACE), 0);
        return UtilsList.notNaked(services) ? UtilsApplication.getPackageName(services.get(0)) : "";
    }

    private static String getDefaultTTSProviderIntent(Context context) {
        final Intent intent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        final List<ResolveInfo> queryIntentActivities = context.getPackageManager().queryIntentActivities(intent, PackageManager.GET_META_DATA);
        if (UtilsList.notNaked(queryIntentActivities)) {
            try {
                return UtilsApplication.getPackageName(queryIntentActivities.get(0));
            } catch (NullPointerException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "getDefaultTTSProviderIntent: NullPointerException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "getDefaultTTSProviderIntent: Exception");
                    e.printStackTrace();
                }
            }
        }
        return "No Engine installed";
    }

    public String createKeys(Context context, String str) {
        try {
            final IAPVersion iapVersion = new IAPVersionReference().getRequestIAP();
            if (iapVersion == null || !UtilsString.notNaked(iapVersion.getVersionCode())) {
                return null;
            }
            final AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(str);
            final Pair<String, Integer> keys = createKeys(context);
            return (keys == null || keys.first == null) ? null : AesCbcWithIntegrity.decryptString(cipherTextIvMac, AesCbcWithIntegrity.generateKeyFromPassword(Base64.encodeToString((keys.first + keys.second).getBytes(StandardCharsets.UTF_8), Base64.DEFAULT), Base64.encodeToString(iapVersion.getVersionCode().getBytes(StandardCharsets.UTF_8), Base64.DEFAULT)));
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createKeys: NullPointerException");
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createKeys: NoSuchAlgorithmException");
                e.printStackTrace();
            }
        } catch (CertificateEncodingException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createKeys: CertificateEncodingException");
                e.printStackTrace();
            }
        } catch (CertificateException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createKeys: CertificateException");
                e.printStackTrace();
            }
        } catch (GeneralSecurityException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createKeys: GeneralSecurityException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createKeys: Exception");
                e.printStackTrace();
            }
        }
        return null;
    }

    public Pair<String, Integer> createKeys(Context context) {
        try {
           final Signature[] signatures = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
            if (signatures != null && signatures.length > 0) {
                final X509Certificate x509Certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(signatures[0].toByteArray()));
                final int modulusHash = ((RSAPublicKey) x509Certificate.getPublicKey()).getModulus().hashCode();
                return new Pair<>(BaseEncoding.base16().ignoreCase().encode(MessageDigest.getInstance("SHA1").digest(x509Certificate.getEncoded())), modulusHash);
            }
        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createKeys: NullPointerException");
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createKeys: NoSuchAlgorithmException");
                e.printStackTrace();
            }
        } catch (CertificateEncodingException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createKeys: CertificateEncodingException");
                e.printStackTrace();
            }
        } catch (CertificateException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createKeys: CertificateException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "createKeys: Exception");
                e.printStackTrace();
            }
        }
        return null;
    }
}

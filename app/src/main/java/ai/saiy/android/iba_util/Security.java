package ai.saiy.android.iba_util;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.api.client.util.SecurityUtils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import ai.saiy.android.utils.MyLog;

/**
 * @link { android.content.pm.PackageParser }
 */
public class Security {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = "Security";

    public static PublicKey parsePublicKey(@NonNull String encodedPublicKey) throws IOException {
        EncodedKeySpec keySpec;
        try {
            final byte[] encoded = Base64.decode(encodedPublicKey, Base64.DEFAULT);
            keySpec = new X509EncodedKeySpec(encoded);
        } catch (IllegalArgumentException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Could not parse verifier public key; invalid Base64");
            }
            return null;
        }

        /* First try the key as an RSA key. */
        try {
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Could not parse public key: RSA KeyFactory not included in build");
            }
        } catch (InvalidKeySpecException e) {
            // Not a RSA public key.
        }

        /* Not a supported key type */
        return null;
    }

    public static boolean verifyPurchase(String encodedPublicKey, String content, String signature) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(encodedPublicKey) || TextUtils.isEmpty(signature)) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Purchase verification failed: missing data.");
            }
            return false;
        }
        try {
            final PublicKey publicKey = parsePublicKey(encodedPublicKey);
            if (publicKey == null) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "Unable to parse verifier public key");
                }
                return false;
            }
            return verifySignature(publicKey, content, signature);
        } catch (IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "IOException: " + e.getClass().getSimpleName() + ", " + e.getMessage());
            }
        }
        return false;
    }

    public static boolean verifySignature(@NonNull PublicKey publicKey, String content, String signature) {
        try {
            final byte[] signatureBytes = Base64.decode(signature, Base64.DEFAULT);
            try {
                final Signature signatureAlgorithm = SecurityUtils.getSha1WithRsaSignatureAlgorithm();
                return SecurityUtils.verify(signatureAlgorithm, publicKey, signatureBytes, content.getBytes());
            } catch (InvalidKeyException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "Invalid key specification.");
                }
            } catch (NoSuchAlgorithmException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "NoSuchAlgorithmException.");
                }
            } catch (SignatureException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "Signature exception.");
                }
            }
        } catch (IllegalArgumentException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Base64 decoding failed.");
            }
        }
        return false;
    }
}

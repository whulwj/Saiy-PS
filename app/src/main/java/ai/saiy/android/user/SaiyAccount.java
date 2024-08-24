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

package ai.saiy.android.user;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

import ai.saiy.android.applications.Install;
import ai.saiy.android.cognitive.identity.provider.microsoft.containers.EnrollmentID;
import ai.saiy.android.cognitive.identity.provider.microsoft.containers.ProfileItem;
import ai.saiy.android.cognitive.identity.provider.microsoft.http.CreateIDProfile;
import ai.saiy.android.configuration.MicrosoftConfiguration;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 08/09/2016.
 */

public class SaiyAccount {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = SaiyAccount.class.getSimpleName();

    @SerializedName("name")
    private String accountName;
    @SerializedName("id")
    private volatile String accountId;
    @SerializedName("pseudonym")
    private String pseudonym;
    @SerializedName("profile")
    private volatile ProfileItem profileItem;
    @SerializedName("pseudonymLinked")
    private boolean pseudonymLinked;
    @SerializedName("autoEnroll")
    private final boolean autoEnroll;

    /**
     * Constructor
     * <p>
     * Create a basic account using the email address
     *
     * @param accountName the email address
     * @param autoEnroll  true if the account should be saved automatically into the shared preferences
     *                    once the {@link #accountId} has been asynchronously returned and a profile id
     *                    should be fetched.
     */
    public SaiyAccount(@NonNull final String accountName, final boolean autoEnroll) {
        this.autoEnroll = autoEnroll;
        this.accountName = accountName;
    }

    public static String getUniqueId(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getUniqueId");
        }
        String uniqueId = ai.saiy.android.utils.SPH.getUniqueID(context);
        if (!UtilsString.notNaked(uniqueId)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getUniqueId: storedUniqueId naked");
            }
            uniqueId = getAdvertisingId(context);
            if (UtilsString.notNaked(uniqueId)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getUniqueId: advertisingId: " + uniqueId);
                }
                ai.saiy.android.utils.SPH.setUniqueID(context, uniqueId);
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getUniqueId: advertisingId naked");
                }
                uniqueId = UUID.randomUUID().toString();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getUniqueId: createdUniqueId: " + uniqueId);
                }
                ai.saiy.android.utils.SPH.setUniqueID(context, uniqueId);
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "getUniqueId: storedUniqueId: " + uniqueId);
        }
        return uniqueId;
    }

    private static String getAdvertisingId(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAdvertisingId");
        }
        try {
            AdvertisingIdClient.Info advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            if (advertisingIdInfo != null) {
                String id = advertisingIdInfo.getId();
                if (UtilsString.notNaked(id)) {
                    return id;
                }
            }
        } catch (GooglePlayServicesNotAvailableException e) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getAdvertisingId: GooglePlayServicesNotAvailableException");
                e.printStackTrace();
            }
        } catch (GooglePlayServicesRepairableException e) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getAdvertisingId: GooglePlayServicesRepairableException");
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getAdvertisingId: IOException");
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Asynchronously set the account id associated with the email address and create a
     * profile id if {@link #autoEnroll} is set to true;
     *
     * @param ctx the application context
     */
    public void setAccountId(@NonNull final Context ctx, @NonNull final ISaiyAccount listener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setAccountId: autoEnroll: " + autoEnroll);
        }

        if (autoEnroll) {

            new Thread() {
                public void run() {

                    final Pair<Boolean, EnrollmentID> enrollmentPair = new CreateIDProfile(ctx,
                            MicrosoftConfiguration.OCP_APIM_KEY_1, Locale.getDefault()).getID();

                    if (enrollmentPair.first) {
                        profileItem = new ProfileItem(enrollmentPair.second.getId());
                    }

                    accountId = Install.getAccountId(ctx, accountName);

                    listener.onAccountInitialisation(SaiyAccount.this);
                    SaiyAccountHelper.addSaiyAccount(ctx, SaiyAccount.this);

                }
            }.start();
        }
    }

    public String getPseudonym() {
        return pseudonym;
    }

    /**
     * Set the pseudonym associated with this user.
     *
     * @param pseudonym       the associated pseudonym
     * @param pseudonymLinked true if this is derived from the vocal user name, false otherwise
     */
    public void setPseudonym(@NonNull final String pseudonym, final boolean pseudonymLinked) {
        this.pseudonym = pseudonym;
        this.pseudonymLinked = pseudonymLinked;
    }

    public boolean isPseudonymLinked() {
        return pseudonymLinked;
    }

    public ProfileItem getProfileItem() {
        return profileItem;
    }

    public void setProfileItem(@NonNull final ProfileItem profileItem) {
        this.profileItem = profileItem;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

}

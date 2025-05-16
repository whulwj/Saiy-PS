package ai.saiy.android.user;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.List;

import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BillingValidator implements AcknowledgePurchaseResponseListener {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = BillingValidator.class.getSimpleName();
    public static final byte FLOW = 3;
    public static final byte AUTOMATIC = 2;
    public static final byte ASYNCHRONOUS = 0;

    private final Context mContext;
    private final BillingClient billingClient;

    public BillingValidator(Context context, BillingClient billingClient) {
        this.mContext = context;
        this.billingClient = billingClient;
    }

    public void validate(final List<com.android.billingclient.api.PurchaseHistoryRecord> list) {
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "validate: GoogleApiAvailability: SUCCESS");
                    }
                    if (!UtilsList.notNaked(list)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "queryPurchasesSynchronous: purchaseList naked");
                        }
                        SPH.setPremiumContentVerbose(mContext, false);
                        return;
                    }

                    List<String> productIds;
                    for (PurchaseHistoryRecord purchaseHistoryRecord : list) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "validate: purchase getProducts:" + purchaseHistoryRecord.getProducts());
                        }
                        if (!TextUtils.isEmpty(purchaseHistoryRecord.getDeveloperPayload())) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "validate: purchase payload:" + purchaseHistoryRecord.getDeveloperPayload() + ", " + purchaseHistoryRecord.getPurchaseTime());
                            }
                        }
                        productIds = purchaseHistoryRecord.getProducts();
                        if (UtilsList.notNaked(productIds)) {
                            if (productIds.contains(UserFirebaseHelper.LEVEL_1) || productIds.contains(UserFirebaseHelper.LEVEL_2) || productIds.contains(UserFirebaseHelper.LEVEL_3) || productIds.contains(UserFirebaseHelper.LEVEL_4) || productIds.contains(UserFirebaseHelper.LEVEL_5)) {
                                if (DEBUG) {
                                    MyLog.d(CLS_NAME, "validate: has LEVEL: TRUE");
                                }
                                if (billingClient != null && !TextUtils.isEmpty(purchaseHistoryRecord.getPurchaseToken()) && !SPH.getPremiumContentVerbose(mContext)) { //Non-consumable Products
                                    final AcknowledgePurchaseParams acknowledgePurchaseParams =
                                            AcknowledgePurchaseParams.newBuilder()
                                                    .setPurchaseToken(purchaseHistoryRecord.getPurchaseToken())
                                                    .build();
                                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, BillingValidator.this);
                                }
                                SPH.setPremiumContentVerbose(mContext, true);
                                return;
                            }
                            if (DEBUG) {
                                MyLog.d(CLS_NAME, "validate: has LEVEL: FALSE");
                            }
                            SPH.setPremiumContentVerbose(mContext, false);
                        } else if (DEBUG) {
                            MyLog.w(CLS_NAME, "validate: product naked");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "validate: GoogleApiAvailability: play services unavailable");
                    }
                }
            }
        });
    }

    public void validate(final List<com.android.billingclient.api.Purchase> list, final byte condition) {
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "validate: GoogleApiAvailability: SUCCESS");
                    }
                    if (!UtilsList.notNaked(list)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "queryPurchasesSynchronous: purchaseList naked");
                        }
                        switch (condition) {
                            case FLOW:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "validate: FLOW: NAKED");
                                }
                                break;
                            case AUTOMATIC:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "validate: AUTOMATIC: NAKED");
                                }
                                break;
                            default:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "validate: ASYNCHRONOUS: NAKED");
                                }
                                break;
                        }
                        SPH.setPremiumContentVerbose(mContext, false);
                        return;
                    }

                    List<String> productIds;
                    for (Purchase purchase : list) {
                        if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "validate: purchase state:" + purchase.getPurchaseState());
                            }
                            continue;
                        }
                        productIds = purchase.getProducts();
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "validate: purchase getProducts:" + purchase.getProducts());
                        }
                        if (UtilsList.notNaked(productIds)) {
                            if (productIds.contains(UserFirebaseHelper.LEVEL_1) || productIds.contains(UserFirebaseHelper.LEVEL_2) || productIds.contains(UserFirebaseHelper.LEVEL_3) || productIds.contains(UserFirebaseHelper.LEVEL_4) || productIds.contains(UserFirebaseHelper.LEVEL_5)) {
                                if (DEBUG) {
                                    MyLog.d(CLS_NAME, "validate: has LEVEL: TRUE");
                                }
                                switch (condition) {
                                    case FLOW:
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "validate: FLOW: TRUE");
                                        }
                                        break;
                                    case AUTOMATIC:
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "validate: AUTOMATIC: TRUE");
                                        }
                                        break;
                                    default:
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "validate: ASYNCHRONOUS: TRUE");
                                        }
                                        break;
                                }
                                if (billingClient != null && !purchase.isAcknowledged()) { //Non-consumable Products
                                    final AcknowledgePurchaseParams acknowledgePurchaseParams =
                                            AcknowledgePurchaseParams.newBuilder()
                                                    .setPurchaseToken(purchase.getPurchaseToken())
                                                    .build();
                                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, BillingValidator.this);
                                }
                                SPH.setPremiumContentVerbose(mContext, true);
                                return;
                            }
                            if (DEBUG) {
                                MyLog.d(CLS_NAME, "validate: has LEVEL: FALSE");
                            }
                            switch (condition) {
                                case FLOW:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "validate: FLOW: FALSE");
                                    }
                                    break;
                                case AUTOMATIC:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "validate: AUTOMATIC: FALSE");
                                    }
                                    break;
                                default:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "validate: ASYNCHRONOUS: FALSE");
                                    }
                                    break;
                            }
                            SPH.setPremiumContentVerbose(mContext, false);
                        } else if (DEBUG) {
                            MyLog.w(CLS_NAME, "validate: product naked");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "validate: GoogleApiAvailability: play services unavailable");
                    }
                }
            }
        });
    }

    @Override
    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onAcknowledgePurchaseResponse:" + billingResult);
        }
    }
}

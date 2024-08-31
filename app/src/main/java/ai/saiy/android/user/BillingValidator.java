package ai.saiy.android.user;

import android.content.Context;

import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.List;

import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;

public class BillingValidator {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = BillingValidator.class.getSimpleName();
    public static final byte FLOW = 3;
    public static final byte AUTOMATIC = 2;
    public static final byte ASYNCHRONOUS = 0;

    private final Context mContext;

    public BillingValidator(Context context) {
        this.mContext = context;
    }

    public void validate(final List<com.android.billingclient.api.PurchaseHistoryRecord> list) {
        new Thread(new Runnable() {
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

                    List<String> skus;
                    for (PurchaseHistoryRecord purchase : list) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "validate: purchase getSkus:" + purchase.getSkus());
                        }
                        skus = purchase.getSkus();
                        if (UtilsList.notNaked(skus)) {
                            if (skus.contains(UserFirebaseHelper.SKU_LEVEL_1) || skus.contains(UserFirebaseHelper.SKU_LEVEL_2) || skus.contains(UserFirebaseHelper.SKU_LEVEL_3) || skus.contains(UserFirebaseHelper.SKU_LEVEL_4) || skus.contains(UserFirebaseHelper.SKU_LEVEL_5)) {
                                if (DEBUG) {
                                    MyLog.d(CLS_NAME, "validate: has SKU_LEVEL: TRUE");
                                }
                                SPH.setPremiumContentVerbose(mContext, true);
                                return;
                            }
                            if (DEBUG) {
                                MyLog.d(CLS_NAME, "validate: has SKU_LEVEL: FALSE");
                            }
                            SPH.setPremiumContentVerbose(mContext, false);
                        } else if (DEBUG) {
                            MyLog.w(CLS_NAME, "validate: sku naked");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "validate: GoogleApiAvailability: play services unavailable");
                    }
                }
            }
        }).start();
    }

    public void validate(final List<com.android.billingclient.api.Purchase> list, final byte condition) {
        new Thread(new Runnable() {
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

                    List<String> skus;
                    for (Purchase purchase : list) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "validate: purchase getSkus:" + purchase.getSkus());
                        }
                        skus = purchase.getSkus();
                        if (UtilsList.notNaked(skus)) {
                            if (skus.contains(UserFirebaseHelper.SKU_LEVEL_1) || skus.contains(UserFirebaseHelper.SKU_LEVEL_2) || skus.contains(UserFirebaseHelper.SKU_LEVEL_3) || skus.contains(UserFirebaseHelper.SKU_LEVEL_4) || skus.contains(UserFirebaseHelper.SKU_LEVEL_5)) {
                                if (DEBUG) {
                                    MyLog.d(CLS_NAME, "validate: has SKU_LEVEL: TRUE");
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
                                SPH.setPremiumContentVerbose(mContext, true);
                                return;
                            }
                            if (DEBUG) {
                                MyLog.d(CLS_NAME, "validate: has SKU_LEVEL: FALSE");
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
                            MyLog.w(CLS_NAME, "validate: sku naked");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "validate: GoogleApiAvailability: play services unavailable");
                    }
                }
            }
        }).start();
    }
}

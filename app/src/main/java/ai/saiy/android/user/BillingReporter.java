package ai.saiy.android.user;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.os.EnvironmentCompat;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.List;

import ai.saiy.android.R;
import ai.saiy.android.firebase.database.reference.IAPCodeReference;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

public class BillingReporter implements PurchaseHistoryResponseListener, PurchasesResponseListener {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = BillingReporter.class.getSimpleName();

    private String content = "";
    private final Context context;
    private final BillingClient billingClient;

    public BillingReporter(Context context, BillingClient billingClient) {
        this.context = context;
        this.billingClient = billingClient;
    }

    private void sendOutput(String content) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sendOutput: " + content);
        }
        if (!UtilsString.notNaked(content)) {
            content = "Operation Failed :(";
        }
        String accountName = SPH.getUserAccount(context);
        if (!UtilsString.notNaked(accountName)) {
            accountName = EnvironmentCompat.MEDIA_UNKNOWN;
        }
        ai.saiy.android.intent.ExecuteIntent.sendEmail(context, new String[]{Constants.SAIY_BILLING_EMAIL}, context.getString(R.string.title_debugging),content + "\n" + ("IAP: " + SPH.getPremiumContentVerbose(context) + "\nAnon: " + (SPH.getFirebaseAnonymousUid(context) != null) + "\nAuth: " + (SPH.getFirebaseUid(context) != null) + "\nMigrate: " + (SPH.getFirebaseMigratedUid(context) != null) + "\nAccount: " + accountName));
    }

    private void queryPurchaseHistoryAsync() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "queryPurchaseHistoryAsync");
        }
        billingClient.queryPurchaseHistoryAsync(QueryPurchaseHistoryParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), this);
    }

    public void report() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "validate: GoogleApiAvailability: SUCCESS");
                    }
                    BillingReporter.this.content = "GoogleApiAvailability: SUCCESS";
                    if (! new IAPCodeReference().getKey(context).first) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "validate: unable to fetch iapKey");
                        }
                        content += "\nValidate: unable to fetch iapKey";
                        sendOutput(content);
                        return;
                    }

                    billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), BillingReporter.this);
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "validate: GoogleApiAvailability: play services unavailable");
                    }
                    content += "\nValidate: GoogleApiAvailability: play services unavailable";
                    sendOutput(content);
                }
            }
        }).start();
    }

    @Override
    public void onPurchaseHistoryResponse(BillingResult billingResult, List<PurchaseHistoryRecord> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onPurchaseHistoryResponse: BillingResponse.OK");
            }
            if (UtilsList.notNaked(list)) {
                boolean isSkuLevel5 = false;
                boolean isSkuLevel4 = false;
                boolean isSkuLevel3 = false;
                boolean isSkuLevel2 = false;
                boolean isSkuLevel1 = false;
                List<String> skus;
                for (PurchaseHistoryRecord purchaseHistoryRecord : list) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "validate: purchase getSkus:" + purchaseHistoryRecord.getSkus());
                    }
                    skus = purchaseHistoryRecord.getSkus();
                    if (UtilsList.notNaked(skus)) {
                        if (skus.contains(UserFirebaseHelper.SKU_LEVEL_1)) {
                            isSkuLevel1 = true;
                        }
                        if (skus.contains(UserFirebaseHelper.SKU_LEVEL_2)) {
                            isSkuLevel2 = true;
                        }
                        if (skus.contains(UserFirebaseHelper.SKU_LEVEL_3)) {
                            isSkuLevel3 = true;
                        }
                        if (skus.contains(UserFirebaseHelper.SKU_LEVEL_4)) {
                            isSkuLevel4 = true;
                        }
                        if (skus.contains(UserFirebaseHelper.SKU_LEVEL_5)) {
                            isSkuLevel5 = true;
                        }
                    }
                }
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "History user: SKU_LEVEL_1: " + isSkuLevel1);
                    MyLog.d(CLS_NAME, "History user: SKU_LEVEL_2: " + isSkuLevel2);
                    MyLog.d(CLS_NAME, "History user: SKU_LEVEL_3: " + isSkuLevel3);
                    MyLog.d(CLS_NAME, "History user: SKU_LEVEL_4: " + isSkuLevel4);
                    MyLog.d(CLS_NAME, "History user: SKU_LEVEL_5: " + isSkuLevel5);
                }
                content += "\nHistory user: SKU_LEVEL_1: " + isSkuLevel1;
                content += "\nHistory user: SKU_LEVEL_2: " + isSkuLevel2;
                content += "\nHistory user: SKU_LEVEL_3: " + isSkuLevel3;
                content += "\nHistory user: SKU_LEVEL_4: " + isSkuLevel4;
                content += "\nHistory user: SKU_LEVEL_5: " + isSkuLevel5;
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onPurchaseHistoryResponse: purchaseList naked");
                }
                content += "\nHistory: purchaseList: EMPTY";
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onPurchaseHistoryResponse: BillingResponse.DEFAULT");
            }
            content += "\nHistory: BillingResponse: BAD";
        }
        sendOutput(content);
    }

    @Override
    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onQueryPurchasesResponse getResponseCode:"  + billingResult.getResponseCode());
        }
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "queryPurchasesSynchronous: BillingResponse.OK");
            }
            if (UtilsList.notNaked(list)) {
                boolean isSkuLevel5 = false;
                boolean isSkuLevel4 = false;
                boolean isSkuLevel3 = false;
                boolean isSkuLevel2 = false;
                boolean isSkuLevel1 = false;
                List<String> skus;
                for (Purchase purchase : list) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "validate: purchase getSkus:" + purchase.getSkus());
                    }
                    skus = purchase.getSkus();
                    if (UtilsList.notNaked(skus)) {
                        if (skus.contains(UserFirebaseHelper.SKU_LEVEL_1)) {
                            isSkuLevel1 = true;
                        }
                        if (skus.contains(UserFirebaseHelper.SKU_LEVEL_2)) {
                            isSkuLevel2 = true;
                        }
                        if (skus.contains(UserFirebaseHelper.SKU_LEVEL_3)) {
                            isSkuLevel3 = true;
                        }
                        if (skus.contains(UserFirebaseHelper.SKU_LEVEL_4)) {
                            isSkuLevel4 = true;
                        }
                        if (skus.contains(UserFirebaseHelper.SKU_LEVEL_5)) {
                            isSkuLevel5 = true;
                        }
                    }
                }
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "user: SKU_LEVEL_1: " + isSkuLevel1);
                    MyLog.d(CLS_NAME, "user: SKU_LEVEL_2: " + isSkuLevel2);
                    MyLog.d(CLS_NAME, "user: SKU_LEVEL_3: " + isSkuLevel3);
                    MyLog.d(CLS_NAME, "user: SKU_LEVEL_4: " + isSkuLevel4);
                    MyLog.d(CLS_NAME, "user: SKU_LEVEL_5: " + isSkuLevel5);
                }
                content += "\nuser: SKU_LEVEL_1: " + isSkuLevel1;
                content += "\nuser: SKU_LEVEL_2: " + isSkuLevel2;
                content += "\nuser: SKU_LEVEL_3: " + isSkuLevel3;
                content += "\nuser: SKU_LEVEL_4: " + isSkuLevel4;
                content += "\nuser: SKU_LEVEL_5: " + isSkuLevel5;
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onQueryPurchasesResponse: purchaseList naked");
                }
                content += "\npurchaseList: EMPTY";
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onQueryPurchasesResponse: BillingResponse.DEFAULT");
            }
            content += "\nBillingResponse: BAD";
        }

        queryPurchaseHistoryAsync();
    }
}

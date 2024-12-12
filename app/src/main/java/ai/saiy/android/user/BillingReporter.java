package ai.saiy.android.user;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

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
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BillingReporter implements PurchaseHistoryResponseListener, PurchasesResponseListener {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = BillingReporter.class.getSimpleName();
    private static final String UNKNOWN = "unknown";

    private final StringBuilder content = new StringBuilder();
    private final Context context;
    private final BillingClient billingClient;

    public BillingReporter(Context context, BillingClient billingClient) {
        this.context = context;
        this.billingClient = billingClient;
    }

    private void sendOutput() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sendOutput: " + content);
        }
        if (content.length() <= 0) {
            content.append("Operation Failed :(");
        }
        final String content = BillingReporter.this.content.toString();
        BillingReporter.this.content.setLength(0);
        String accountName = SPH.getUserAccount(context);
        if (!UtilsString.notNaked(accountName)) {
            accountName = UNKNOWN;
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
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "validate: GoogleApiAvailability: SUCCESS");
                    }
                    content.setLength(0);
                    content.append("GoogleApiAvailability: SUCCESS");
                    if (! new IAPCodeReference().getKey(context).first) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "validate: unable to fetch iapKey");
                        }
                        content.append("\nValidate: unable to fetch iapKey");
                        sendOutput();
                        return;
                    }

                    billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), BillingReporter.this);
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "validate: GoogleApiAvailability: play services unavailable");
                    }
                    content.append("\nValidate: GoogleApiAvailability: play services unavailable");
                    sendOutput();
                }
            }
        });
    }

    @Override
    public void onPurchaseHistoryResponse(BillingResult billingResult, List<PurchaseHistoryRecord> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onPurchaseHistoryResponse: BillingResponse.OK");
            }
            if (UtilsList.notNaked(list)) {
                boolean isLevel5 = false;
                boolean isLevel4 = false;
                boolean isLevel3 = false;
                boolean isLevel2 = false;
                boolean isLevel1 = false;
                List<String> productIds;
                for (PurchaseHistoryRecord purchaseHistoryRecord : list) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "validate: purchase getProducts:" + purchaseHistoryRecord.getProducts());
                    }
                    if (!TextUtils.equals(Constants.SAIY, purchaseHistoryRecord.getDeveloperPayload())) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "validate: purchase payload:" + purchaseHistoryRecord.getDeveloperPayload() + ", " + purchaseHistoryRecord.getPurchaseTime());
                        }
                        content.append("\nHistory payload: ").append(purchaseHistoryRecord.getDeveloperPayload());
                        continue;
                    }
                    productIds = purchaseHistoryRecord.getProducts();
                    if (UtilsList.notNaked(productIds)) {
                        if (productIds.contains(UserFirebaseHelper.LEVEL_1)) {
                            isLevel1 = true;
                        }
                        if (productIds.contains(UserFirebaseHelper.LEVEL_2)) {
                            isLevel2 = true;
                        }
                        if (productIds.contains(UserFirebaseHelper.LEVEL_3)) {
                            isLevel3 = true;
                        }
                        if (productIds.contains(UserFirebaseHelper.LEVEL_4)) {
                            isLevel4 = true;
                        }
                        if (productIds.contains(UserFirebaseHelper.LEVEL_5)) {
                            isLevel5 = true;
                        }
                    }
                }
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "History user: LEVEL_1: " + isLevel1);
                    MyLog.d(CLS_NAME, "History user: LEVEL_2: " + isLevel2);
                    MyLog.d(CLS_NAME, "History user: LEVEL_3: " + isLevel3);
                    MyLog.d(CLS_NAME, "History user: LEVEL_4: " + isLevel4);
                    MyLog.d(CLS_NAME, "History user: LEVEL_5: " + isLevel5);
                }
                content.append("\nHistory user: LEVEL_1: ").append(isLevel1);
                content.append("\nHistory user: LEVEL_2: ").append(isLevel2);
                content.append("\nHistory user: LEVEL_3: ").append(isLevel3);
                content.append("\nHistory user: LEVEL_4: ").append(isLevel4);
                content.append("\nHistory user: LEVEL_5: ").append(isLevel5);
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onPurchaseHistoryResponse: purchaseList naked");
                }
                content.append("\nHistory: purchaseList: EMPTY");
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onPurchaseHistoryResponse: BillingResponse.DEFAULT");
            }
            content.append("\nHistory: BillingResponse: BAD");
        }
        sendOutput();
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
                boolean isLevel5 = false;
                boolean isLevel4 = false;
                boolean isLevel3 = false;
                boolean isLevel2 = false;
                boolean isLevel1 = false;
                List<String> productIds;
                for (Purchase purchase : list) {
                    if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "validate: purchase state:" + purchase.getPurchaseState());
                        }
                        continue;
                    }
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "validate: purchase getProducts:" + purchase.getProducts());
                    }
                    productIds = purchase.getProducts();
                    if (UtilsList.notNaked(productIds)) {
                        if (productIds.contains(UserFirebaseHelper.LEVEL_1)) {
                            isLevel1 = true;
                        }
                        if (productIds.contains(UserFirebaseHelper.LEVEL_2)) {
                            isLevel2 = true;
                        }
                        if (productIds.contains(UserFirebaseHelper.LEVEL_3)) {
                            isLevel3 = true;
                        }
                        if (productIds.contains(UserFirebaseHelper.LEVEL_4)) {
                            isLevel4 = true;
                        }
                        if (productIds.contains(UserFirebaseHelper.LEVEL_5)) {
                            isLevel5 = true;
                        }
                    }
                }
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "user: LEVEL_1: " + isLevel1);
                    MyLog.d(CLS_NAME, "user: LEVEL_2: " + isLevel2);
                    MyLog.d(CLS_NAME, "user: LEVEL_3: " + isLevel3);
                    MyLog.d(CLS_NAME, "user: LEVEL_4: " + isLevel4);
                    MyLog.d(CLS_NAME, "user: LEVEL_5: " + isLevel5);
                }
                content.append("\nuser: LEVEL_1: ").append(isLevel1);
                content.append("\nuser: LEVEL_2: ").append(isLevel2);
                content.append("\nuser: LEVEL_3: ").append(isLevel3);
                content.append("\nuser: LEVEL_4: ").append(isLevel4);
                content.append("\nuser: LEVEL_5: ").append(isLevel5);
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onQueryPurchasesResponse: purchaseList naked");
                }
                content.append("\npurchaseList: EMPTY");
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onQueryPurchasesResponse: BillingResponse.DEFAULT");
            }
            content.append("\nBillingResponse: BAD");
        }

        queryPurchaseHistoryAsync();
    }
}

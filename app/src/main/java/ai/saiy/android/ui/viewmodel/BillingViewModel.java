package ai.saiy.android.ui.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ai.saiy.android.IABUtil.Security;
import ai.saiy.android.R;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.device.DeviceInfo;
import ai.saiy.android.firebase.database.write.IAPPurchase;
import ai.saiy.android.ui.activity.CurrentActivityProvider;
import ai.saiy.android.user.BillingValidator;
import ai.saiy.android.user.UserFirebaseHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public final class BillingViewModel extends AndroidViewModel implements BillingClientStateListener,
        ProductDetailsResponseListener,
        PurchasesResponseListener,
        PurchaseHistoryResponseListener,
        PurchasesUpdatedListener {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = BillingViewModel.class.getSimpleName();

    private final MutableLiveData<Boolean> mIsBillingSuccessful = new MutableLiveData<>();
    private final MutableLiveData<BillingResult> productDetailsResult = new MutableLiveData<>();
    private final MutableLiveData<List<ProductDetails>> productDetailsList = new MutableLiveData<>();
    private BillingClient mBillingClient;
    private volatile BillingValidator billingValidator;
    private volatile boolean isConnectionRetrying = false;
    private volatile boolean isBillingFlowOK = false;
    private volatile String iapKey = "";
    private final CurrentActivityProvider mActivityProvider;

    public @Inject BillingViewModel(@NonNull Application application, CurrentActivityProvider activityProvider) {
        super(application);
        this.mActivityProvider = activityProvider;
        this.mBillingClient = BillingClient.newBuilder(application).setListener(this).enablePendingPurchases().build();
    }

    @Override
    public void onBillingServiceDisconnected() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBillingServiceDisconnected");
        }
        if (isConnectionRetrying) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onBillingServiceDisconnected: already retried");
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onBillingServiceDisconnected: retrying");
            }
            this.isConnectionRetrying = true;
            startBillingConnection();
        }
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBillingSetupFinished: " + billingResult);
            debugBillingResponse("onBillingSetupFinished", billingResult);
        }
        if (BillingClient.BillingResponseCode.OK == billingResult.getResponseCode()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    queryPurchasesAsync();
                }
            }).start();
        }
    }

    @Override
    public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onProductDetailsResponse");
        }
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onProductDetailsResponse: BillingResponse.OK");
            }
            if (!ai.saiy.android.utils.UtilsList.notNaked(list)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onProductDetailsResponse: productDetailsList naked");
                }
            } else if (DEBUG) {
                for (ProductDetails productDetails : list) {
                    MyLog.i(CLS_NAME, "productDetails.getProductId():" + productDetails.getProductId());
                    MyLog.i(CLS_NAME, "productDetails.getTitle():" + productDetails.getTitle());
                    final com.android.billingclient.api.ProductDetails.OneTimePurchaseOfferDetails oneTimePurchaseOfferDetails = productDetails.getOneTimePurchaseOfferDetails();
                    if (oneTimePurchaseOfferDetails != null) {
                        MyLog.i(CLS_NAME, "productDetails.getPrice():" + oneTimePurchaseOfferDetails.getFormattedPrice());
                    }
                    final List<com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetails = productDetails.getSubscriptionOfferDetails();
                    if (subscriptionOfferDetails != null) {
                        for (com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails subscriptionOffer : subscriptionOfferDetails) {
                            MyLog.i(CLS_NAME, "productDetails.getOfferTags():" + subscriptionOffer.getOfferTags());
                            MyLog.i(CLS_NAME, "productDetails.getPricingPhases():" + subscriptionOffer.getPricingPhases());
                        }
                    }
                }
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "onProductDetailsResponse: BillingResponse.DEFAULT");
        }
        productDetailsList.setValue(list);
        productDetailsResult.setValue(billingResult);
    }

    @Override
    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onQueryPurchasesResponse: " + billingResult);
            debugBillingResponse("onQueryPurchasesResponse", billingResult);
        }
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onQueryPurchasesResponse: BillingResponse.OK");
            }
            getBillingValidator().validate(list, BillingValidator.ASYNCHRONOUS);
            if (SPH.getDebugBilling(getApplication())) {
                queryPurchaseHistoryAsync();
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "onQueryPurchasesResponse: BillingResponse.DEFAULT");
        }
    }

    @Override
    public void onPurchaseHistoryResponse(@NonNull BillingResult billingResult, @Nullable List<PurchaseHistoryRecord> list) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPurchaseHistoryResponse: " + billingResult);
            debugBillingResponse("onPurchaseHistoryResponse", billingResult);
        }
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onPurchaseHistoryResponse: BillingResponse.OK");
            }
            getBillingValidator().validate(list);
        }
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPurchasesUpdated: " + billingResult);
            debugBillingResponse("onPurchasesUpdated", billingResult);
        }
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onPurchasesUpdated: BillingResponse.OK");
            }
            getBillingValidator().validate(list, isBillingFlowOK ? BillingValidator.FLOW : BillingValidator.AUTOMATIC);
            if (isBillingFlowOK) {
                final boolean isPending = verifyPurchase(list);
                if (!isPending) {
                    isBillingFlowOK = false;
                }
            } else if (DEBUG) {
                MyLog.i(CLS_NAME, "onPurchasesUpdated ignored");
            }
        } else {
            isBillingFlowOK = false;
        }
    }

    private void debugBillingResponse(String str, BillingResult billingResult) {
        MyLog.i(CLS_NAME, "debugBillingResponse");
        switch (billingResult.getResponseCode()) {
            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                MyLog.i(CLS_NAME, str + ": BillingResponse.FEATURE_NOT_SUPPORTED");
                break;
            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                MyLog.i(CLS_NAME, str + ": BillingResponse.SERVICE_DISCONNECTED");
                break;
            case BillingClient.BillingResponseCode.OK:
                MyLog.i(CLS_NAME, str + ": BillingResponse.OK");
                break;
            case BillingClient.BillingResponseCode.USER_CANCELED:
                MyLog.i(CLS_NAME, str + ": BillingResponse.USER_CANCELED");
                break;
            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                MyLog.i(CLS_NAME, str + ": BillingResponse.SERVICE_UNAVAILABLE");
                break;
            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                MyLog.i(CLS_NAME, str + ": BillingResponse.BILLING_UNAVAILABLE");
                break;
            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
            default:
                MyLog.i(CLS_NAME, str + ": BillingResponse.DEFAULT");
                break;
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                MyLog.i(CLS_NAME, str + ": BillingResponse.DEVELOPER_ERROR");
                break;
            case BillingClient.BillingResponseCode.ERROR:
                MyLog.i(CLS_NAME, str + ": BillingResponse.ERROR");
                break;
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                MyLog.i(CLS_NAME, str + ": BillingResponse.ITEM_ALREADY_OWNED");
                break;
            case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
                MyLog.i(CLS_NAME, str + ": BillingResponse.ITEM_NOT_OWNED");
                break;
        }
    }

    /**
     * To verify the PURCHASED state
     * @param purchaseList the purchase list
     * @return true if there is a pending bill
     */
    private boolean verifyPurchase(List<Purchase> purchaseList) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "Begin verifyPayload");
        }
        if (!ai.saiy.android.utils.UtilsList.notNaked(purchaseList)) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "purchaseList naked");
            }
            mIsBillingSuccessful.setValue(false);
            return false;
        }
        final Purchase purchase = purchaseList.get(0);
        final @Purchase.PurchaseState int purchaseState = purchase.getPurchaseState();
        if (purchaseState != Purchase.PurchaseState.PURCHASED) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getPurchaseState: " + purchaseState);
            }
            return true;
        }
        final String originalJSON = purchase.getOriginalJson();
        final String signature = purchase.getSignature();
        if (DEBUG) {
            MyLog.d(CLS_NAME, "originalJSON: " + originalJSON);
            MyLog.d(CLS_NAME, "signature: " + signature);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendSubmissionIAP(purchase);
            }
        }).start();
        final boolean secure = Security.verifyPurchase(iapKey, originalJSON, signature);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "secure: " + secure);
        }
        mIsBillingSuccessful.setValue(secure);
        return false;
    }

    public @StringRes int startPurchaseFlow(@NonNull Activity activity, @NonNull com.android.billingclient.api.ProductDetails productDetails) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startPurchaseFlow");
        }
        if (!canBillingProceed()) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "startPurchaseFlow: canBillingProceed: false");
            }
            return R.string.iap_error_generic;
        }
        final BillingResult billingResult = mBillingClient.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(
                Collections.singletonList(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).build())).build());
        switch (billingResult.getResponseCode()) {
            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "startPurchaseFlow: BillingResponse.FEATURE_NOT_SUPPORTED");
                }
                return R.string.iap_error_generic;
            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "startPurchaseFlow: BillingResponse.SERVICE_DISCONNECTED");
                }
                return R.string.iap_error_generic;
            case BillingClient.BillingResponseCode.OK:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "startPurchaseFlow: BillingResponse.OK");
                }
                this.isBillingFlowOK = true;
                break;
            case BillingClient.BillingResponseCode.USER_CANCELED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "startPurchaseFlow: BillingResponse.USER_CANCELED");
                }
                return R.string.iap_purchase_cancelled;
            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "startPurchaseFlow: BillingResponse.SERVICE_UNAVAILABLE");
                }
                return R.string.iap_error_generic;
            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "startPurchaseFlow: BillingResponse.BILLING_UNAVAILABLE");
                }
                return R.string.iap_error_generic;
            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "startPurchaseFlow: BillingResponse.DEFAULT");
                }
                return R.string.iap_item_unavailable;
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "startPurchaseFlow: BillingResponse.DEVELOPER_ERROR");
                }
                return R.string.iap_error_generic;
            case BillingClient.BillingResponseCode.ERROR:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "startPurchaseFlow: BillingResponse.ERROR");
                }
                return R.string.iap_error_generic;
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "startPurchaseFlow: BillingResponse.ITEM_ALREADY_OWNED");
                }
                return R.string.iap_owned;
            case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "startPurchaseFlow: BillingResponse.ITEM_NOT_OWNED");
                }
                break;
        }
        return 0;
    }

    public void setIapKey(String key) {
        this.iapKey = key;
    }

    public @Nullable BillingClient getBillingClient() {
        return mBillingClient;
    }

    public void start() {
        if (mBillingClient != null) {
            startBillingConnection();
        } if (DEBUG) {
            MyLog.e(CLS_NAME, "mBillingClient null");
        }
    }

    public void resume() {
        this.isConnectionRetrying = false;
    }

    public @NonNull MutableLiveData<Boolean> mIsBillingSuccessful() {
        return mIsBillingSuccessful;
    }

    public boolean queryProductDetails() {
        if (mBillingClient == null) {
            return false;
        }
        final List<QueryProductDetailsParams.Product> products = new ArrayList<>();
        for (String productId : UserFirebaseHelper.productIds()) {
            products.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.INAPP).build());
        }
        final QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(ImmutableList.copyOf(products))
                .build();
        mBillingClient.queryProductDetailsAsync(queryProductDetailsParams, this);
        return true;
    }

    public @NonNull LiveData<BillingResult> getProductDetailsResult() {
        return productDetailsResult;
    }

    public @NonNull LiveData<List<ProductDetails>> getProductDetailsList() {
        return productDetailsList;
    }

    private void sendSubmissionIAP(@NonNull Purchase purchase) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sendSubmissionIAP");
        }
        final Pair<String, Integer> signaturePair = new DeviceInfo().createKeys(getApplication());
        int hashCode = 0;
        String hexCertificate = "";
        if (signaturePair != null) {
            hexCertificate = signaturePair.first;
            hashCode = signaturePair.second;
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "sendSubmissionIAP: signaturePair null");
        }
        final boolean isFreedomInstalled = Installed.isPackageInstalled(getApplication(), Installed.PACKAGE_FREEDOM);
        final IAPPurchase userIap = new IAPPurchase(purchase.getOrderId(), purchase.getOriginalJson(), purchase.getPackageName(), purchase.getPurchaseTime(), purchase.getSignature(), purchase.getProducts(), purchase.getPurchaseToken(), isFreedomInstalled, hashCode, hexCertificate);
        final Task<Void> task = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("db_write").child("user_iap").getRef().setValue(userIap);
        if (DEBUG) {
            final com.google.android.gms.tasks.OnCompleteListener<Void> onCompleteListener = new com.google.android.gms.tasks.OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    MyLog.i(CLS_NAME, "sendSubmissionIAP: task success: " + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        task.getException().printStackTrace();
                    }
                }
            };
            mActivityProvider.withActivity(activity -> {
                if (activity == null || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) || activity.isFinishing() || activity.isChangingConfigurations()) {
                    task.addOnCompleteListener(onCompleteListener);
                    return;
                }
                task.addOnCompleteListener(activity, onCompleteListener);
            });
        }
    }

    private void queryPurchasesAsync() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "queryPurchasesAsync");
        }
        if (mBillingClient == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "queryPurchasesAsync mBillingClient null");
            }
            return;
        }
        if (!mBillingClient.isReady()) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "queryPurchasesAsync mBillingClient not ready");
            }
            return;
        }
        mBillingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), this);
    }

    private void queryPurchaseHistoryAsync() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "queryPurchaseHistoryAsync");
        }
        if (mBillingClient == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "queryPurchaseHistoryAsync mBillingClient null");
            }
            return;
        }
        if (!mBillingClient.isReady()) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "queryPurchaseHistoryAsync mBillingClient not ready");
            }
            return;
        }
        mBillingClient.queryPurchaseHistoryAsync(QueryPurchaseHistoryParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), this);
    }

    private void startBillingConnection() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startBillingConnection");
        }
        if (mBillingClient != null) {
            mBillingClient.startConnection(this);
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "startBillingConnection mBillingClient null");
        }
    }

    public boolean canBillingProceed() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "canBillingProceed");
        }
        if (mBillingClient == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "canBillingProceed mBillingClient null");
            }
            return false;
        }
        if (mBillingClient.isReady()) {
            return true;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "canBillingProceed mBillingClient not ready");
        }
        return false;
    }

    private @NonNull BillingValidator getBillingValidator() {
        if (billingValidator == null) {
            synchronized (this) {
                if (billingValidator == null) {
                    billingValidator = new BillingValidator(getApplication(), mBillingClient);
                }
            }
        }
        return billingValidator;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mBillingClient != null && mBillingClient.isReady()) {
            mBillingClient.endConnection();
            this.mBillingClient = null;
        }
    }
}

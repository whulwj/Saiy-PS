package ai.saiy.android.firebase.database.write;

import com.google.firebase.database.PropertyName;

import java.util.List;

import ai.saiy.android.utils.Constants;

@com.google.firebase.database.IgnoreExtraProperties
public class IAPPurchase {
    @PropertyName("developer_payload")
    public String developerPayload;
    @PropertyName("order_id")
    public String orderId;
    @PropertyName("original_json")
    public String originalJson;
    @PropertyName("package_name")
    public String packageName;
    @PropertyName("purchase_time")
    public long purchaseTime;

    @PropertyName("signature")
    public String signature;

    @PropertyName("products")
    public List<String> products;
    @PropertyName("token")
    public String token;

    @PropertyName("freedom_installed")
    public boolean isFreedomInstalled;
    @PropertyName("hash")
    public int hash;
    @PropertyName("hex")
    public String hex;

    public IAPPurchase() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public IAPPurchase(String orderId, String originalJson, String packageName, long purchaseTime, String signature, List<String> products, String token, boolean isFreedomInstalled, int hashCode, String hex) {
        this.developerPayload = Constants.SAIY;
        this.orderId = orderId;
        this.originalJson = originalJson;
        this.packageName = packageName;
        this.purchaseTime = purchaseTime;
        this.signature = signature;
        this.products = products;
        this.token = token;
        this.isFreedomInstalled = isFreedomInstalled;
        this.hash = hashCode;
        this.hex = hex;
    }
}

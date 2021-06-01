package com.dooboolab.flutterinapppurchase;

import android.util.Log;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.ProductType;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.RequestId;
import com.amazon.device.iap.model.UserDataResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * AmazonInappPurchasePlugin
 */
public class AmazonInappPurchasePlugin implements MethodCallHandler {
    public static Registrar reg;
    private final String TAG = "InappPurchasePlugin";
    private Result result = null;
    private static MethodChannel channel;
    private String userId;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        channel = new MethodChannel(registrar.messenger(), "flutter_inapp");
        channel.setMethodCallHandler(new FlutterInappPurchasePlugin());
        reg = registrar;
    }

    @Override
    public void onMethodCall(final MethodCall call, final Result result) {
        this.result = result;
        try {
            PurchasingService.registerListener(reg.context(), purchasesUpdatedListener);

        } catch (Exception e) {
            result.error(call.method, "Call endConnection method if you want to start over.", e.getMessage());
        }
        if (call.method.equals("getPlatformVersion")) {
            try {
                result.success("Amazon " + android.os.Build.VERSION.RELEASE);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        } else if (call.method.equals("initConnection")) {
            PurchasingService.getUserData();
            result.success("Billing client ready");
        } else if (call.method.equals("endConnection")) {
            result.success("Billing client has ended.");
        } else if (call.method.equals("consumeAllItems")) {
            // consumable is a separate type in amazon
            result.success("no-ops in amazon");
        } else if (call.method.equals("getItemsByType")) {
            Log.d(TAG, "getItemsByType");
            String type = call.argument("type");
            ArrayList<String> skus = call.argument("skus");

            final Set<String> productSkus = new HashSet<>();
            for (int i = 0; i < skus.size(); i++) {
                Log.d(TAG, "Adding " + skus.get(i));
                productSkus.add(skus.get(i));
            }
            PurchasingService.getProductData(productSkus);

        } else if (call.method.equals("getAvailableItemsByType")) {
            String type = call.argument("type");
            Log.d(TAG, "gaibt=" + type);
            // NOTE: getPurchaseUpdates doesnt return Consumables which are FULFILLED
            if (type.equals("inapp")) {
                PurchasingService.getPurchaseUpdates(true);
            } else if (type.equals("subs")) {
                // Subscriptions are retrieved during inapp, so we just return empty list
                result.success("[]");
            } else {
                result.notImplemented();
            }
        } else if (call.method.equals("getPurchaseHistoryByType")) {
            // No equivalent
            result.success("[]");
        } else if (call.method.equals("buyItemByType")) {
            final String type = call.argument("type");
            final String accountId = call.argument("accountId");
            final String developerId = call.argument("developerId");
            final String sku = call.argument("sku");
            final String oldSku = call.argument("oldSku");
            final int prorationMode = call.argument("prorationMode");
            Log.d(TAG, "type=" + type + "||sku=" + sku + "||oldsku=" + oldSku);
            final RequestId requestId = PurchasingService.purchase(sku);
            Log.d(TAG, "resid=" + requestId.toString());
        } else if (call.method.equals("consumeProduct")) {
            // consumable is a separate type in amazon
            result.success("no-ops in amazon");
        } else {
            result.notImplemented();
        }
    }

    private PurchasingListener purchasesUpdatedListener = new PurchasingListener() {
        @Override
        public void onUserDataResponse(UserDataResponse userDataResponse) {
            Log.d(TAG, "oudr=" + userDataResponse.toString());
            userId = userDataResponse.getUserData().getUserId();
        }

        // getItemsByType
        @Override
        public void onProductDataResponse(ProductDataResponse response) {
            Log.d(TAG, "opdr=" + response.toString());
            final ProductDataResponse.RequestStatus status = response.getRequestStatus();
            Log.d(TAG, "onProductDataResponse: RequestStatus (" + status + ")");

            switch (status) {
                case SUCCESSFUL:
                    Log.d(TAG, "onProductDataResponse: successful.  The item data map in this response includes the valid SKUs");

                    final Map<String, Product> productData = response.getProductData();
                    //Log.d(TAG, "productData="+productData.toString());

                    final Set<String> unavailableSkus = response.getUnavailableSkus();
                    Log.d(TAG, "onProductDataResponse: " + unavailableSkus.size() + " unavailable skus");
                    Log.d(TAG, "unavailableSkus=" + unavailableSkus.toString());
                    JSONArray items = new JSONArray();
                    try {
                        for (Map.Entry<String, Product> skuDetails : productData.entrySet()) {
                            Product product = skuDetails.getValue();
                            NumberFormat format = NumberFormat.getCurrencyInstance();

                            Number number;
                            try {
                                number = format.parse(product.getPrice());
                            } catch (ParseException e) {
                                result.error(TAG, "Price Parsing error", e.getMessage());
                                return;
                            }
                            JSONObject item = new JSONObject();
                            item.put("productId", product.getSku());
                            item.put("price", number.toString());
                            item.put("currency", null);
                            ProductType productType = product.getProductType();
                            switch (productType) {
                                case ENTITLED:
                                case CONSUMABLE:
                                    item.put("type", "inapp");
                                    break;
                                case SUBSCRIPTION:
                                    item.put("type", "subs");
                                    break;
                            }
                            item.put("localizedPrice", product.getPrice());
                            item.put("title", product.getTitle());
                            item.put("description", product.getDescription());
                            item.put("introductoryPrice", "");
                            item.put("subscriptionPeriodAndroid", "");
                            item.put("freeTrialPeriodAndroid", "");
                            item.put("introductoryPriceCyclesAndroid", "");
                            item.put("introductoryPricePeriodAndroid", "");
                            Log.d(TAG, "opdr Putting " + item.toString());
                            items.put(item);
                        }
                        //System.err.println("Sending "+items.toString());
                        result.success(items.toString());
                    } catch (JSONException e) {
                        result.error(TAG, "E_BILLING_RESPONSE_JSON_PARSE_ERROR", e.getMessage());
                    }
                    break;
                case FAILED:
                    result.error(TAG, "FAILED", null);
                case NOT_SUPPORTED:
                    Log.d(TAG, "onProductDataResponse: failed, should retry request");
                    result.error(TAG, "NOT_SUPPORTED", null);
                    break;
            }
        }

        // buyItemByType
        @Override
        public void onPurchaseResponse(PurchaseResponse response) {
            Log.d(TAG, "opr=" + response.toString());
            final PurchaseResponse.RequestStatus status = response.getRequestStatus();
            switch (status) {
                case SUCCESSFUL:
                    Receipt receipt = response.getReceipt();
                    PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.FULFILLED);
                    Date date = receipt.getPurchaseDate();
                    Long transactionDate = date.getTime();
                    try {
                        JSONObject item = getPurchaseData(receipt.getSku(),
                                receipt.getReceiptId(),
                                receipt.getReceiptId(),
                                transactionDate.doubleValue(), response.getUserData().getUserId());
                        Log.d(TAG, "opr Putting " + item.toString());
                        //result.success(item.toString());
                        channel.invokeMethod("purchase-updated", item.toString());
                    } catch (JSONException e) {
                        result.error(TAG, "E_BILLING_RESPONSE_JSON_PARSE_ERROR", e.getMessage());
                        channel.invokeMethod("purchase-error", e.getMessage());

                    }
                    break;
                case FAILED:
                    JSONObject jsonFailed = new JSONObject();
                    try {
                        jsonFailed.put("responseCode", 1);
                        jsonFailed.put("debugMessage", "E_USER_CANCELLED");
                        String[] errorData = DoobooUtils.getInstance().getBillingResponseData(1);
                        jsonFailed.put("code", errorData[0]);
                        jsonFailed.put("message", errorData[1]);
                        channel.invokeMethod("purchase-error", jsonFailed.toString());
                    } catch (JSONException e) {
                        channel.invokeMethod("purchase-error", e.getMessage());
                    }
                    break;
                case ALREADY_PURCHASED:
                    JSONObject json = new JSONObject();
                    try {
                        json.put("responseCode", 7);
                        json.put("debugMessage", "ALREADY_PURCHASED");
                        String[] errorData = DoobooUtils.getInstance().getBillingResponseData(7);
                        json.put("code", errorData[0]);
                        json.put("message", errorData[1]);
                        channel.invokeMethod("purchase-error", json.toString());
                    } catch (JSONException e) {
                        channel.invokeMethod("purchase-error", e.getMessage());
                    }
                    break;
                case INVALID_SKU:
                    JSONObject jsonInvalidSku = new JSONObject();
                    try {
                        jsonInvalidSku.put("responseCode", 4);
                        jsonInvalidSku.put("debugMessage", "E_ITEM_UNAVAILABLE");
                        String[] errorData = DoobooUtils.getInstance().getBillingResponseData(4);
                        jsonInvalidSku.put("code", errorData[0]);
                        jsonInvalidSku.put("message", errorData[1]);
                        channel.invokeMethod("purchase-error", jsonInvalidSku.toString());
                    } catch (JSONException e) {
                        channel.invokeMethod("purchase-error", e.getMessage());
                    }
                    break;
                case NOT_SUPPORTED:
                    JSONObject jsonNotSupported = new JSONObject();
                    try {
                        jsonNotSupported.put("responseCode", 0);
                        jsonNotSupported.put("debugMessage", "E_ITEM_UNAVAILABLE");
                        String[] errorData = DoobooUtils.getInstance().getBillingResponseData(4);
                        jsonNotSupported.put("code", errorData[0]);
                        jsonNotSupported.put("message", errorData[1]);
                        channel.invokeMethod("purchase-error", jsonNotSupported.toString());
                    } catch (JSONException e) {
                        channel.invokeMethod("purchase-error", e.getMessage());
                    }
                    break;

            }
        }

        // getAvailableItemsByType
        @Override
        public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse response) {
            Log.d(TAG, "opudr=" + response.toString());
            final PurchaseUpdatesResponse.RequestStatus status = response.getRequestStatus();

            switch (status) {
                case SUCCESSFUL:
                    JSONArray items = new JSONArray();
                    try {
                        List<Receipt> receipts = response.getReceipts();
                        for (Receipt receipt : receipts) {
                            Date date = receipt.getPurchaseDate();
                            Long transactionDate = date.getTime();
                            JSONObject item = getPurchaseData(receipt.getSku(),
                                    receipt.getReceiptId(),
                                    receipt.getReceiptId(),
                                    transactionDate.doubleValue(), response.getUserData().getUserId());

                            Log.d(TAG, "opudr Putting " + item.toString());
                            items.put(item);
                        }
                        result.success(items.toString());
                    } catch (JSONException e) {
                        result.error(TAG, "E_BILLING_RESPONSE_JSON_PARSE_ERROR", e.getMessage());
                    }
                    break;
                case FAILED:
                    result.error(TAG, "FAILED", null);
                    break;
                case NOT_SUPPORTED:
                    Log.d(TAG, "onPurchaseUpdatesResponse: failed, should retry request");
                    result.error(TAG, "NOT_SUPPORTED", null);
                    break;
            }
        }
    };

    JSONObject getPurchaseData(String productId, String transactionId, String transactionReceipt,
                               Double transactionDate, String userId) throws JSONException {
        JSONObject item = new JSONObject();
        item.put("productId", productId);
        item.put("transactionId", transactionId);
        item.put("transactionReceipt", transactionReceipt);
        item.put("transactionDate", Double.toString(transactionDate));
        item.put("userId", userId);
        item.put("dataAndroid", null);
        item.put("signatureAndroid", null);
        item.put("purchaseToken", null);
        return item;
    }
}

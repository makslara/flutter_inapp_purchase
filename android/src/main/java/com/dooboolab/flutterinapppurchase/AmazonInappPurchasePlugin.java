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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
        if (call.method.equals("getPlatformVersion")) {
            try {
                channel.invokeMethod("log-show", "Method calls: getPlatformVersion, result:\"Amazon \"" + android.os.Build.VERSION.RELEASE + ", date: " + getTime());
                result.success("Amazon " + android.os.Build.VERSION.RELEASE);
                Log.d(TAG, "Method calls: getPlatformVersion, result:\"Amazon \"" + android.os.Build.VERSION.RELEASE);

            } catch (IllegalStateException e) {
                Log.d(TAG, "Method calls: getPlatformVersion, Error result: IllegalStateException");
                channel.invokeMethod("log-show", "Method calls: getPlatformVersion, Error result: IllegalStateException, date: " + getTime());
                e.printStackTrace();
            }
        } else if (call.method.equals("getSandboxMode")) {
            Log.d(TAG, "Method calls: getSandboxMode, result: " + PurchasingService.IS_SANDBOX_MODE);
            channel.invokeMethod("log-show", "Method calls: getSandboxMode, result: " + PurchasingService.IS_SANDBOX_MODE
                    + ", date: " + getTime());
            result.success(PurchasingService.IS_SANDBOX_MODE);
        } else if (call.method.equals("getStore")) {
            try {
                channel.invokeMethod("log-show", "Method calls: getStore, result:\"Amazon \"" + android.os.Build.VERSION.RELEASE + ", date: " + getTime());
                result.success("Amazon " + android.os.Build.VERSION.RELEASE);
                Log.d(TAG, "Method calls: getStore, result:\"Amazon \"" + android.os.Build.VERSION.RELEASE);


            } catch (IllegalStateException e) {
                Log.d(TAG, "Method calls: getStore, result:\"Amazon \"" + android.os.Build.VERSION.RELEASE);
                channel.invokeMethod("log-show", "Method calls: getStore, Error result: IllegalStateException, date: " + getTime());
                e.printStackTrace();
            }
        } else if (call.method.equals("initConnection")) {
            try {
                PurchasingService.registerListener(reg.activity().getApplication(), purchasesUpdatedListener);
                Log.d(TAG, "Method calls: initConnection, result: purchasesUpdatedListener registered");
                channel.invokeMethod("log-show", "Method calls: initConnection, result: purchasesUpdatedListener registered"
                        + "date: "
                        + getTime());

            } catch (Exception e) {
                Log.d(TAG, "Method calls: initConnection, result: purchasesUpdatedListener didn`t registered");
                channel.invokeMethod("log-show",
                        "Method calls: initConnection, result: purchasesUpdatedListener didn`t registered"
                                + "date: "
                                + getTime());

                result.error(call.method, "Call endConnection method if you want to start over.", e.getMessage());
            }
            try {
                PurchasingService.getUserData();
                Log.d(TAG, "Method calls: initConnection, result: Billing client ready");

                channel.invokeMethod("log-show", "Method calls: initConnection, result: Billing client ready"
                        + ", date: " + getTime());
                result.success("Billing client ready");
            } catch (Exception e) {
                Log.d(TAG, "Method calls: initConnection, result: Billing client isn`t ready");
                channel.invokeMethod("log-show", "Method calls: initConnection, result Billing client isn`t ready" + ", date: " + getTime());
                result.error(call.method, "\"Method calls: initConnection, result:Billing client isn`t ready", e.getMessage());
            }
        } else if (call.method.equals("endConnection")) {
            channel.invokeMethod("log-show", "Method calls: endConnection, result: Billing client has ended"
                    + ", date: " + getTime());
            Log.d(TAG, "Method calls: endConnection, result: Billing client ready");
            result.success("Billing client has ended.");
        } else if (call.method.equals("consumeAllItems")) {
            // consumable is a separate type in amazon
            result.success("no-ops in amazon");
        } else if (call.method.equals("getItemsByType")) {
            Log.d(TAG, "Method calls: getItemsByType"
                    + ", result: purchasesUpdatedListener registered");

            String type = call.argument("type");
            ArrayList<String> skus = call.argument("skus");

            final Set<String> productSkus = new HashSet<>();
            for (int i = 0; i < skus.size(); i++) {
                Log.d(TAG, "Method calls: getItemsByType"
                        + ", result: Adding skus: " + skus.get(i));
                channel.invokeMethod("log-show", "Method calls: getItemsByType"
                        + ", result: Adding skus: " + skus.get(i) + ", date: " + getTime());
                productSkus.add(skus.get(i));
            }
            try {
                PurchasingService.getProductData(productSkus);
                Log.d(TAG, "Method calls: getItemsByType, and waiting callback result");
                channel.invokeMethod("log-show", "Method calls: getItemsByType, and waiting callback result" + ", date: " + getTime());

            } catch (Exception e) {
                Log.d(TAG, "Method calls: getItemsByType, result: failed get ProductData");
                channel.invokeMethod("log-show", "Method calls: getItemsByType, result: failed get ProductData " + ", date: " + getTime());
            }

        } else if (call.method.equals("getAvailableItemsByType")) {
            String type = call.argument("type");
            Log.d(TAG, "gaibt=" + type);
            // NOTE: getPurchaseUpdates doesnt return Consumables which are FULFILLED
            if (type.equals("inapp")) {
                try {
                    PurchasingService.getPurchaseUpdates(true);
                    Log.d(TAG, "Method calls: getAvailableItemsByType, and waiting callback result");
                    channel.invokeMethod("log-show", "Method calls: getAvailableItemsByType, and waiting callback result" + ", date: " + getTime());

                } catch (Exception e) {
                    Log.d(TAG, "Method calls: getAvailableItemsByType, result: failed get AvailableItemsByType ");
                    channel.invokeMethod("log-show", "Method calls: getAvailableItemsByType, result: failed get AvailableItemsByType " + ", date: " + getTime());
                }
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
            Log.d(TAG, "Method calls: buyItemByType , type=" + type + "||sku=" + sku + "||oldsku=" + oldSku);
            try {
                PurchasingService.purchase(sku);
                Log.d(TAG, "Method calls: buyItemByType, and waiting callback result" + ",info: " + "accountId - " + accountId + ", sku" + sku);

                channel.invokeMethod("log-show", "Method calls: buyItemByType, and waiting callback result" + ",info: " + "accountId - " + accountId + ", sku" + sku + ", date: " + getTime());

            } catch (Exception e) {
                Log.d(TAG, "Method calls: buyItemByType, result: failed get AvailableItemsByType");
                channel.invokeMethod("log-show", "Method calls: buyItemByType, result: failed get AvailableItemsByType " + ", date: " + getTime());

            }
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
            Log.d(TAG, "Method calls: onProductDataResponse, response: =" + response.toString());
            final ProductDataResponse.RequestStatus status = response.getRequestStatus();
            Log.d(TAG, "Method calls: onProductDataResponse: RequestStatus (" + status + ")");

            switch (status) {
                case SUCCESSFUL:

                    final Map<String, Product> productData = response.getProductData();

                    final Set<String> unavailableSkus = response.getUnavailableSkus();
                    Log.d(TAG, "Method calls: onProductDataResponse, unavailable skus size: " + unavailableSkus.size() + " unavailable skus");
                    Log.d(TAG, "Method calls: onProductDataResponse, unavailable sku : " + unavailableSkus.toString() + " unavailable skus");
                    JSONArray items = new JSONArray();
                    try {
                        for (Map.Entry<String, Product> skuDetails : productData.entrySet()) {
                            Product product = skuDetails.getValue();
                            NumberFormat format = NumberFormat.getCurrencyInstance();

                            Number number;
                            try {
                                number = format.parse(product.getPrice());
                            } catch (ParseException e) {
                                Log.d(TAG, "Method calls: onProductDataResponse, Price Parsing erro");
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
                        Log.d(TAG, "Method calls: onProductDataResponse,  and CallBack result: SUCCESSFUL, item: " + items.toString());

                        channel.invokeMethod("log-show", "Method calls: getProductData, and CallBack result: " + items.toString() + ", date: " + getTime());
                        result.success(items.toString());
                    } catch (JSONException e) {
                        Log.d(TAG, "Method calls: onProductDataResponse,  and CallBack result: E_BILLING_RESPONSE_JSON_PARSE_ERROR");
                        channel.invokeMethod("log-show", "Method calls: getProductData, and CallBack error result : JSONException, date: " + getTime());
                        result.error(TAG, "E_BILLING_RESPONSE_JSON_PARSE_ERROR", e.getMessage());
                    }
                    break;
                case FAILED:
                    Log.d(TAG, "Method calls: onProductDataResponse, result: FAILED, should retry request");
                    channel.invokeMethod("log-show", "Method calls: getProductData, and CallBack error result : FAILED, date: " + getTime());
                    result.error(TAG, "FAILED", null);
                case NOT_SUPPORTED:
                    Log.d(TAG, "Method calls: onProductDataResponse, result: NOT_SUPPORTED, should retry request");
                    channel.invokeMethod("log-show", "Method calls: getProductData, and CallBack error result : NOT_SUPPORTED, date: " + getTime());
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
                        Log.d(TAG, "Method calls: buyItemByType,  and CallBack result: SUCCESSFUL, item: " + item.toString());
                        //result.success(item.toString());
                        channel.invokeMethod("log-show", "Method calls: buyItemByType, and CallBack result: " + item.toString() + ", date: " + getTime());
                        channel.invokeMethod("purchase-updated", item.toString());

                    } catch (JSONException e) {
                        Log.d(TAG, "Method calls: buyItemByType, and CallBack result: E_BILLING_RESPONSE_JSON_PARSE_ERROR");
                        channel.invokeMethod("log-show", "Method calls: buyItemByType, and CallBack error result : JSONException, date: " + getTime());
                        channel.invokeMethod("purchase-error", e.getMessage());
                        result.error(TAG, "E_BILLING_RESPONSE_JSON_PARSE_ERROR", e.getMessage());

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
                    Log.d(TAG, "Method calls: buyItemByType, result: FAILED, should retry request");

                    channel.invokeMethod("log-show", "Method calls: buyItemByType, and CallBack error result : FAILED, date: " + getTime());

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
                        channel.invokeMethod("log-show", "Method calls: buyItemByType, and CallBack result:Already purchase, info: " + json.toString() + ", date: " + getTime());
                        Log.d(TAG, "Method calls: buyItemByType,  and CallBack result: ALREADY_PURCHASED, item: " + json.toString());

                    } catch (JSONException e) {
                        Log.d(TAG, "Method calls: buyItemByType, and CallBack result: E_BILLING_RESPONSE_JSON_PARSE_ERROR");

                        channel.invokeMethod("purchase-error", e.getMessage());
                        channel.invokeMethod("log-show", "Method calls: buyItemByType, and CallBack result: JSONException, date: " + getTime());
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
                    Log.d(TAG, "Method calls: buyItemByType, result: INVALID_SKU, should retry request");
                    channel.invokeMethod("log-show", "Method calls: buyItemByType, and CallBack result: INVALID_SKU, date: " + getTime());

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
                    Log.d(TAG, "Method calls: buyItemByType, result: NOT_SUPPORTED, should retry request");
                    channel.invokeMethod("log-show", "Method calls: buyItemByType, and CallBack result: NOT_SUPPORTED, date: " + getTime());
                    break;

            }
        }

        // getAvailableItemsByType
        @Override
        public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse response) {
            Log.d(TAG, "Method calls:getAvailableItemsByType, response" + response.toString());
            final PurchaseUpdatesResponse.RequestStatus status = response.getRequestStatus();
            Log.d(TAG, "Method calls:getAvailableItemsByType, status: " + status.toString());

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

                            Log.d(TAG, "Method calls:getAvailableItemsByType,, iap Putting: " + item.toString());
                            items.put(item);
                        }
                        Log.d(TAG, "Method calls:getAvailableItemsByType, result: SUCCESSFUL, callback result:" + items.toString());
                        channel.invokeMethod("log-show", "Method calls: getAvailableItemsByType, and CallBack result: " + items.toString() + ", date: " + getTime());
                        result.success(items.toString());
                    } catch (JSONException e) {
                        Log.d(TAG, "Method calls:getAvailableItemsByType, result: E_BILLING_RESPONSE_JSON_PARSE_ERROR, should retry request");
                        channel.invokeMethod("log-show", "Method calls: getAvailableItemsByType, and CallBack result:E_BILLING_RESPONSE_JSON_PARSE_ERROR " + ", date: " + getTime());
                        result.error(TAG, "E_BILLING_RESPONSE_JSON_PARSE_ERROR", e.getMessage());

                    }
                    break;
                case FAILED:
                    Log.d(TAG, "Method calls:getAvailableItemsByType, result: FAILED, should retry request");
                    channel.invokeMethod("log-show", "Method calls: getAvailableItemsByType, and CallBack result:FAILED " + ", date: " + getTime());
                    result.error(TAG, "FAILED", null);
                    break;
                case NOT_SUPPORTED:
                    Log.d(TAG, "Method calls:getAvailableItemsByType, result: NOT_SUPPORTED, should retry request");
                    channel.invokeMethod("log-show", "Method calls: getAvailableItemsByType, and CallBack result:NOT_SUPPORTED " + ", date: " + getTime());
                    result.error(TAG, "NOT_SUPPORTED", null);
                    break;
            }
        }
    };

    private String getTime() {
        SimpleDateFormat dtf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date now = Calendar.getInstance().getTime();
        return dtf.format(now);
    }

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

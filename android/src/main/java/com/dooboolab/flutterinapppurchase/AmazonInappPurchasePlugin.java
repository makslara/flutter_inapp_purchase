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
import java.time.LocalDateTime;
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
                channel.invokeMethod("log-show", "Method calls: getPlatformVersion, result:\"Amazon \"" + android.os.Build.VERSION.RELEASE + ", date: " + Calendar.getInstance().getTime());
                result.success("Amazon " + android.os.Build.VERSION.RELEASE);

            } catch (IllegalStateException e) {
                channel.invokeMethod("log-show", "Method calls: getPlatformVersion, Error result: IllegalStateException, date: " + Calendar.getInstance().getTime());
                e.printStackTrace();
            }
        } else if (call.method.equals("getSandboxMode")) {
            channel.invokeMethod("log-show", "Method calls: getSandboxMode, result: " + PurchasingService.IS_SANDBOX_MODE
                    + ", date: " + Calendar.getInstance().getTime());
            result.success(PurchasingService.IS_SANDBOX_MODE);
        }
        else if (call.method.equals("getStore")) {
            try {
                channel.invokeMethod("log-show", "Method calls: getStore, result:\"Amazon \"" + android.os.Build.VERSION.RELEASE + ", date: " + Calendar.getInstance().getTime());
                result.success("Amazon " + android.os.Build.VERSION.RELEASE);

            } catch (IllegalStateException e) {
                channel.invokeMethod("log-show", "Method calls: getStore, Error result: IllegalStateException, date: " + Calendar.getInstance().getTime());
                e.printStackTrace();
            }
        }else if (call.method.equals("initConnection")) {
            try {
                PurchasingService.registerListener(reg.context(), purchasesUpdatedListener);
                channel.invokeMethod("log-show", "Method calls: initConnection, result: purchasesUpdatedListener registered"
                        + "date: "
                        + Calendar.getInstance().getTime());

            } catch (Exception e) {
                channel.invokeMethod("log-show",
                        "Method calls: initConnection, result: purchasesUpdatedListener didn`t registered"
                                + "date: "
                                + Calendar.getInstance().getTime());

                result.error(call.method, "Call endConnection method if you want to start over.", e.getMessage());
            }
            try {
                PurchasingService.getUserData();
                channel.invokeMethod("log-show", "Method calls: initConnection, result: Billing client ready"
                        + ", date: " + Calendar.getInstance().getTime());

            } catch (Exception e) {
                channel.invokeMethod("log-show", "Method calls: initConnection, result Billing client isn`t ready" + ", date: " + Calendar.getInstance().getTime());

            }
            result.success("Billing client ready");
        } else if (call.method.equals("endConnection")) {
            channel.invokeMethod("log-show", "Method calls: endConnection, result: Billing client has ended"
                    + ", date: " + Calendar.getInstance().getTime());

            result.success("Billing client has ended.");
        } else if (call.method.equals("consumeAllItems")) {
            // consumable is a separate type in amazon
            result.success("no-ops in amazon");
        } else if (call.method.equals("getItemsByType")) {
            Log.d(TAG, "getItemsByType");
            try {
                PurchasingService.registerListener(reg.context(), purchasesUpdatedListener);
                channel.invokeMethod("log-show", "Method calls: getItemsByType"
                        +", result: purchasesUpdatedListener registered, "
                        + "date: "
                        + Calendar.getInstance().getTime());

            } catch (Exception e) {
                channel.invokeMethod("log-show",
                        "Method calls: getItemsByType and registered listener"+", result: repurchasesUpdatedListener didn`t registered"
                                + "date: "
                                + Calendar.getInstance().getTime());

                result.error(call.method, "Call endConnection method if you want to start over.", e.getMessage());
            }
            String type = call.argument("type");
            ArrayList<String> skus = call.argument("skus");

            final Set<String> productSkus = new HashSet<>();
            for (int i = 0; i < skus.size(); i++) {
                Log.d(TAG, "Adding " + skus.get(i));
                channel.invokeMethod("log-show", "Method calls: getItemsByType"
                        + ", result: Adding skus: "+skus.get(i)+", date: " + Calendar.getInstance().getTime());
                productSkus.add(skus.get(i));
            }
            try {
                channel.invokeMethod("log-show", "Method calls: getItemsByType, and waiting callback result" + ", date: " + Calendar.getInstance().getTime());
                PurchasingService.getProductData(productSkus);
            }catch (Exception e){
                channel.invokeMethod("log-show", "Method calls: getItemsByType, result: failed get ProductData " + ", date: " + Calendar.getInstance().getTime());
            }

        } else if (call.method.equals("getAvailableItemsByType")) {
            try {
                PurchasingService.registerListener(reg.context(), purchasesUpdatedListener);
                channel.invokeMethod("log-show", "Method calls: getAvailableItemsByType"
                        +", result: ,purchasesUpdatedListener registered"
                        + "date: "
                        + Calendar.getInstance().getTime());

            } catch (Exception e) {
                channel.invokeMethod("log-show",
                        "Method calls: getAvailableItemsByType"+", result: repurchasesUpdatedListener didn`t registered"
                                + ", date: "
                                + Calendar.getInstance().getTime());

                result.error(call.method, "Call endConnection method if you want to start over.", e.getMessage());
            }
            String type = call.argument("type");
            Log.d(TAG, "gaibt=" + type);
            // NOTE: getPurchaseUpdates doesnt return Consumables which are FULFILLED
            if (type.equals("inapp")) {
                try {
                    channel.invokeMethod("log-show", "Method calls: getAvailableItemsByType, and waiting callback result" + ", date: " + Calendar.getInstance().getTime());
                    PurchasingService.getPurchaseUpdates(true);
                }catch (Exception e){
                    channel.invokeMethod("log-show", "Method calls: getAvailableItemsByType, result: failed get AvailableItemsByType " + ", date: " + Calendar.getInstance().getTime());
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
            try {
                PurchasingService.registerListener(reg.context(), purchasesUpdatedListener);
                channel.invokeMethod("log-show", "Method calls: buyItemByType"
                        +", result:purchasesUpdatedListener registered"
                        + ",date: "
                        + Calendar.getInstance().getTime());

            } catch (Exception e) {
                channel.invokeMethod("log-show",
                        "Method calls: getAvailableItemsByType"+", result: repurchasesUpdatedListener didn`t registered"
                                + ",date: "
                                + Calendar.getInstance().getTime());

                result.error(call.method, "Call endConnection method if you want to start over.", e.getMessage());
            }
            final String type = call.argument("type");
            final String accountId = call.argument("accountId");
            final String developerId = call.argument("developerId");
            final String sku = call.argument("sku");
            final String oldSku = call.argument("oldSku");
            final int prorationMode = call.argument("prorationMode");
            Log.d(TAG, "type=" + type + "||sku=" + sku + "||oldsku=" + oldSku);
            try {
                channel.invokeMethod("log-show", "Method calls: buyItemByType, and waiting callback result"+",info: "+"accountId - "+accountId+", sku" +sku + ", date: " + Calendar.getInstance().getTime());
                final RequestId requestId = PurchasingService.purchase(sku);
                Log.d(TAG, "resid=" + requestId.toString());

            }catch (Exception e){
                channel.invokeMethod("log-show", "Method calls: buyItemByType, result: failed get AvailableItemsByType " + ", date: " + Calendar.getInstance().getTime());

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
                        channel.invokeMethod("log-show", "Method calls: getProductData, and CallBack result: "+items.toString() + ", date: " + Calendar.getInstance().getTime());

                        result.success(items.toString());
                    } catch (JSONException e) {
                        channel.invokeMethod("log-show", "Method calls: getProductData, and CallBack error result : JSONException, date: " + Calendar.getInstance().getTime());

                        result.error(TAG, "E_BILLING_RESPONSE_JSON_PARSE_ERROR", e.getMessage());
                    }
                    break;
                case FAILED:
                    Log.d(TAG, "onProductDataResponse: failed, should retry request");

                    channel.invokeMethod("log-show", "Method calls: getProductData, and CallBack error result : FAILED, date: " + Calendar.getInstance().getTime());

                    result.error(TAG, "FAILED", null);
                case NOT_SUPPORTED:
                    Log.d(TAG, "onProductDataResponse: NOT_SUPPORTED, should retry request");
                    channel.invokeMethod("log-show", "Method calls: getProductData, and CallBack error result : NOT_SUPPORTED, date: " + Calendar.getInstance().getTime());
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
                        channel.invokeMethod("log-show", "Method calls: buyItemByType, and CallBack result: "+item.toString() + ", date: " + Calendar.getInstance().getTime());

                        channel.invokeMethod("purchase-updated", item.toString());
                    } catch (JSONException e) {
                        channel.invokeMethod("log-show", "Method calls: buyItemByType, and CallBack error result : JSONException, date: " + Calendar.getInstance().getTime());
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
                    channel.invokeMethod("log-show", "Method calls: buyItemByType, and CallBack error result : FAILED, date: " + Calendar.getInstance().getTime());

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
                        channel.invokeMethod("log-show", "Method calls: buyItemByType, and CallBack result:Already purchase, info: "+json.toString() + ", date: " + Calendar.getInstance().getTime());

                    } catch (JSONException e) {
                        channel.invokeMethod("purchase-error", e.getMessage());
                        channel.invokeMethod("log-show", "Method calls: buyItemByType, and CallBack result: JSONException, date: " + Calendar.getInstance().getTime());
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
                    channel.invokeMethod("log-show", "Method calls: buyItemByType, and CallBack result: INVALID_SKU, date: " + Calendar.getInstance().getTime());

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
                    channel.invokeMethod("log-show", "Method calls: buyItemByType, and CallBack result: NOT_SUPPORTED, date: " + Calendar.getInstance().getTime());

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
                        channel.invokeMethod("log-show", "Method calls: getAvailableItemsByType, and CallBack result: "+items.toString() + ", date: " + Calendar.getInstance().getTime());

                        result.success(items.toString());
                    } catch (JSONException e) {
                        channel.invokeMethod("log-show", "Method calls: getAvailableItemsByType, and CallBack result:JSONException " + ", date: " + Calendar.getInstance().getTime());

                        result.error(TAG, "E_BILLING_RESPONSE_JSON_PARSE_ERROR", e.getMessage());

                    }
                    break;
                case FAILED:
                    channel.invokeMethod("log-show", "Method calls: getAvailableItemsByType, and CallBack result:FAILED " + ", date: " + Calendar.getInstance().getTime());

                    result.error(TAG, "FAILED", null);
                    break;
                case NOT_SUPPORTED:
                    channel.invokeMethod("log-show", "Method calls: getAvailableItemsByType, and CallBack result:NOT_SUPPORTED " + ", date: " + Calendar.getInstance().getTime());

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

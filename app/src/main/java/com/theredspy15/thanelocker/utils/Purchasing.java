package com.theredspy15.thanelocker.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetailsParams;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Purchasing implements PurchasesUpdatedListener {

    private static final String ITEM_SKU_SUBSCRIBE = "longboardlife_premium";
    public static final String SUBSCRIBE_KEY= "subscribe";

    public static BillingClient billingClient;

    private boolean getSubscribeValueFromPref() {
        return MainActivity.preferences.getBoolean(SUBSCRIBE_KEY,false);
    }

    private void saveSubscribeValueToPref(boolean value) {
        SharedPreferences.Editor prefsEditor = MainActivity.preferences.edit();
        prefsEditor.putBoolean(SUBSCRIBE_KEY,value).apply();
    }

    public static PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
        // To be implemented in a later section.
    };

    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     * <p>Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     * </p>
     */
    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmcmjIwsxTM38kdR/b6DMmaAay/gqTtYPwbMhPRv6iDA1C03T8iucOHgcj3agPLqSNXxBbDNoH6GmA4cvuGTqmBp8p8n31R6c1pTMntCEtVd3X8k1tuTPZp/Qx1ZR5rfSZ9ZnpeY58tD9eOormZkJ3Zy4sHwWMpenfMdZJUf2gloAMnLcRJvYeSQf83+M5LMM+lrGZtBS5vT2X2dY4iI2SdvIHncC4QqX/lJxE+ZgMzs/Whx7BfZl7hc3z+x13Wsl47v21ijT6gqKZNX4wa2qRIe5uw6Rt1hkDiApr+9leV2y8f6guTUv2myDXdzHFwO1YWG+eeUOhEOvfsRaguzjBQIDAQAB";

            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    void handlePurchases(List<Purchase> purchases) {
        for(Purchase purchase:purchases) {
            //if item is purchased
            if (ITEM_SKU_SUBSCRIBE.equals(purchase.getSkus().get(0)) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED)
            {
                if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                    // Invalid purchase
                    System.out.println("e111 invalid");
                    return;
                }
                // else purchase is valid
                //if item is purchased and not acknowledged
                if (!purchase.isAcknowledged()) {
                    System.out.println("e111 needs acknowledged");
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, ackPurchase);
                }
                //else item is purchased and also acknowledged
                else {
                    System.out.println("e111 should be good");
                    // Grant entitlement to the user on item purchase
                    // TODO: restart activity
                    if(!getSubscribeValueFromPref()){
                        saveSubscribeValueToPref(true);
                    }
                }
            }
            //if purchase is pending
            else if (ITEM_SKU_SUBSCRIBE.equals(purchase.getSkus().get(0)) && purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                System.out.println("e111 pending");
            }
            //if purchase is unknown mark false
            else if(ITEM_SKU_SUBSCRIBE.equals(purchase.getSkus().get(0)) && purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                saveSubscribeValueToPref(false);
                System.out.println("e111 unknown");
            } else System.out.println("e111 else");
        }
    }

    AcknowledgePurchaseResponseListener ackPurchase = billingResult -> {
        if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK) {
            //if purchase is acknowledged
            // Grant entitlement to the user. and restart activity
            saveSubscribeValueToPref(true);
            // TODO: restart activity
        }
    };

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        //if item subscribed
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases);
        }
        //if item already subscribed then check and reflect changes
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
            List<Purchase> alreadyPurchases = queryAlreadyPurchasesResult.getPurchasesList();
            if(alreadyPurchases!=null){
                handlePurchases(alreadyPurchases);
            }
        }
        //if Purchase canceled
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {

        }
        // Handle any other error msgs
        else {

        }
    }

    public void subscribe(Context context, Activity activity) {
        //check if service is already connected
        if (billingClient.isReady()) {
            initiatePurchase(activity);
        }
        //else reconnect service
        else {
            billingClient = BillingClient.newBuilder(context).enablePendingPurchases().setListener(this).build();
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        initiatePurchase(activity);
                    } else {

                    }
                }
                @Override
                public void onBillingServiceDisconnected() {

                }
            });
        }
    }

    private void initiatePurchase(Activity activity) {
        List<String> skuList = new ArrayList<>();
        skuList.add(ITEM_SKU_SUBSCRIBE);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        BillingResult billingResult = billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS);
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            billingClient.querySkuDetailsAsync(params.build(),
                    (billingResult1, skuDetailsList) -> {
                        if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (skuDetailsList != null && skuDetailsList.size() > 0) {
                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetailsList.get(0))
                                        .build();
                                billingClient.launchBillingFlow(activity, flowParams);
                            } else {
                                //try to add subscription item "sub_example" in google play console

                            }
                        } else {

                        }
                    });
        } else {

        }
    }
}

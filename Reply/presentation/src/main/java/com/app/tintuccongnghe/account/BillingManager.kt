package com.app.tintuccongnghe.account

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _hasCloudStorageSubscription = MutableStateFlow(false)
    val hasCloudStorageSubscription: StateFlow<Boolean> = _hasCloudStorageSubscription.asStateFlow()

    private val _hasWebsiteLimitSubscription = MutableStateFlow(false)
    val hasWebsiteLimitSubscription: StateFlow<Boolean> = _hasWebsiteLimitSubscription.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection if needed
            }
        })
    }

    fun queryPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val cloudActive = purchases.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                            (purchase.products.contains("cloud_storage_monthly_sub") ||
                             purchase.products.contains("cloud_storage_annual_sub"))
                }
                val limitActive = purchases.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                            (purchase.products.contains("rss_weekly_sub") ||
                             purchase.products.contains("rss_monthly_sub") ||
                             purchase.products.contains("rss_annual_sub") ||
                             purchase.products.contains("rss_limit_monthly_sub") ||
                             purchase.products.contains("rss_limit_annual_sub"))
                }
                _hasCloudStorageSubscription.value = cloudActive
                _hasWebsiteLimitSubscription.value = limitActive
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        updateSubscriptionState(purchase.products)
                    }
                }
            } else {
                updateSubscriptionState(purchase.products)
            }
        }
    }

    private fun updateSubscriptionState(products: List<String>) {
        if (products.any { it.contains("cloud_storage") }) {
            _hasCloudStorageSubscription.value = true
        }
        if (products.any { it.contains("rss") || it.contains("limit") }) {
            _hasWebsiteLimitSubscription.value = true
        }
    }

    suspend fun launchBillingFlow(activity: Activity, productId: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
                )
                .build()

            billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, queryProductDetailsResult ->
                val productDetailsList = queryProductDetailsResult.productDetailsList
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !productDetailsList.isNullOrEmpty()) {
                    val productDetails = productDetailsList[0]
                    val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build()
                    )
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()

                    val result = billingClient.launchBillingFlow(activity, billingFlowParams)
                    continuation.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                } else {
                    continuation.resume(false)
                }
            }
        }
    }
}

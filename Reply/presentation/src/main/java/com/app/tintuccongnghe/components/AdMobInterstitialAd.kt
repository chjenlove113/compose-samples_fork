package com.app.tintuccongnghe.components

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

@Composable
fun AddSiteWithInterstitialAd(
    onNavigateToAddSite: () -> Unit
) {
    val context = LocalContext.current
    var interstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }

    LaunchedEffect(Unit) {
        loadInterstitialAd(context) { ad ->
            interstitialAd = ad
        }
    }

    Button(
        onClick = {
            if (interstitialAd != null) {
                interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("AdMob", "Ad dismissed.")
                        interstitialAd = null
                        loadInterstitialAd(context) { newAd -> interstitialAd = newAd }
                        onNavigateToAddSite()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e("AdMob", "Ad failed to show: ${adError.message}")
                        interstitialAd = null
                        onNavigateToAddSite()
                    }
                }
                interstitialAd?.show(context as Activity)
            } else {
                onNavigateToAddSite()
            }
        },
        modifier = Modifier.padding(8.dp)
    ) {
        Text("Add Site")
    }
}

fun loadInterstitialAd(context: Context, onAdLoaded: (InterstitialAd?) -> Unit) {
    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(
        context,
        "ca-app-pub-3940256099942544/1033173712",
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                onAdLoaded(ad)
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                onAdLoaded(null)
            }
        }
    )
}

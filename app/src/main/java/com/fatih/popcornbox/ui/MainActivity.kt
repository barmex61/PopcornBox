package com.fatih.popcornbox.ui

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.sdk.AppLovinSdk
import com.bumptech.glide.Glide
import com.fatih.popcornbox.R
import com.fatih.popcornbox.other.Constants.isFirstRun
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


@AndroidEntryPoint
class MainActivity : AppCompatActivity() , MaxAdListener {

    private val TAG : String = "MainActivity"
    private  var mInterstitialAd: InterstitialAd ?= null
    var adRequest = AdRequest.Builder().build()
    var currentTime: Long = 0L
    var timeDiff = 0L
    private lateinit var consentInformation: ConsentInformation
    private var navHostFragment : NavHostFragment ?= null
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    private lateinit var interstitialAd: MaxInterstitialAd
    private var retryAttempt = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentTime = savedInstanceState?.getLong("currentTime",0L)?:0L
        WindowCompat.setDecorFitsSystemWindows(window.apply {
            setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                attributes.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }, false)

        setContentView(R.layout.activity_main)
        initializeAppLovinSdk()
	    requestConsentForm()

        navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment?.navController

        loadGoogleAd(navController!!)

    }

    private fun loadGoogleAd(navController:NavController){
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.mainFragment) isFirstRun = false
            if ((Calendar.getInstance().timeInMillis - currentTime > 60000L) && !isFirstRun) {
                interstitialAd.loadAd()
                println("calendar")
                /* InterstitialAd.load(
                    this@MainActivity,
                    "ca-app-pub-7923951045985903/8603110213",
                    adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.d(TAG, adError.toString())
                        }

                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            Log.d(TAG, "Ad was loaded.")
                            mInterstitialAd = interstitialAd
                            /* if (timeDiff == 0L) {
                                timeDiff += 10L
                            } else {
                                timeDiff = 22000L
                            } */

                        }
                    })
                mInterstitialAd?.show(this@MainActivity) */
                if (interstitialAd.isReady){
                    interstitialAd.showAd()
                    currentTime = Calendar.getInstance().timeInMillis
                }
            }
        }
    }

    private fun loadApplovinAd(){
        interstitialAd = MaxInterstitialAd( "41ea2e3178e602ee", this )
        interstitialAd.setListener(this)
        interstitialAd.loadAd()
    }

    private fun requestConsentForm(){
        val params = ConsentRequestParameters
            .Builder()
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(this, params, {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(this@MainActivity)
                { loadAndShowError ->
                    // Consent gathering failed.
                    Log.w(TAG, String.format("%s: %s", loadAndShowError?.errorCode, loadAndShowError?.message))
                    // Consent has been gathered.
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAdsSdk()
                    }
                } },
             { requestConsentError ->
                // Consent gathering failed.
                Log.w(TAG, String.format("%s: %s", requestConsentError.errorCode, requestConsentError.message))
             })

        if (consentInformation.canRequestAds()) {
            initializeMobileAdsSdk()
        }
    }

    private fun initializeAppLovinSdk(){
        AppLovinSdk.getInstance( this ).mediationProvider = "max"
        AppLovinSdk.getInstance( this ).initializeSdk { configuration ->
            println("sdk initialized")
            loadApplovinAd()
            // AppLovin SDK is initialized, start loading ads
        }
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        MobileAds.initialize(this)
    }

    override fun onAdLoaded(maxAd: MaxAd)
    {
        // Interstitial ad is ready to be shown. interstitialAd.isReady() will now return 'true'

        // Reset retry attempt
        println("onadloaded")
        retryAttempt = 0.0
    }

    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
        interstitialAd.loadAd()
    }

    override fun onAdLoadFailed(p0: String, p1: MaxError) {
        // Interstitial ad failed to load
        // AppLovin recommends that you retry with exponentially higher delays up to a maximum delay (in this case 64 seconds)
        println("onloadfailed $p0")
        retryAttempt++
        val delayMillis = TimeUnit.SECONDS.toMillis( Math.pow( 2.0, Math.min( 6.0, retryAttempt ) ).toLong() )

        Handler().postDelayed( { interstitialAd.loadAd() }, delayMillis )
    }

    override fun onAdDisplayed(maxAd: MaxAd) {
        println("displayed")
    }

    override fun onAdClicked(maxAd: MaxAd) {}

    override fun onAdHidden(maxAd: MaxAd)
    {
        // Interstitial ad is hidden. Pre-load the next ad
        interstitialAd.loadAd()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Glide.with(this).onTrimMemory(level)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Glide.with(this).onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.putLong("currentTime",0L)
        super.onSaveInstanceState(outState, outPersistentState)
    }


    override fun onDestroy() {
        navHostFragment?.onDestroy()
        navHostFragment= null
        mInterstitialAd = null
        super.onDestroy()
    }

}
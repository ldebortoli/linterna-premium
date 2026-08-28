package com.linternapremium.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.linternapremium.app.ads.AdsCoordinator
import com.linternapremium.app.billing.BillingEvents
import com.linternapremium.app.billing.BillingGateway
import com.linternapremium.app.billing.GooglePlayBillingGateway
import com.linternapremium.app.domain.LinternaEngine
import com.linternapremium.app.domain.PremiumSequenceRunner
import com.linternapremium.app.model.EngineResult
import com.linternapremium.app.model.LinternaState
import com.linternapremium.app.model.PremiumEffect
import com.linternapremium.app.model.TorchResult
import com.linternapremium.app.platform.AndroidTorchPort
import com.linternapremium.app.platform.PreferencesPremiumStore
import com.linternapremium.app.ports.TorchPort
import com.linternapremium.app.ui.LinternaPremiumScreen
import com.linternapremium.app.ui.theme.LinternaPremiumTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), BillingEvents {
    private lateinit var engine: LinternaEngine
    private lateinit var torchPort: TorchPort
    private lateinit var premiumSequenceRunner: PremiumSequenceRunner
    private var billingGateway: BillingGateway? = null
    private var premiumSequenceJob: Job? = null
    private var uiState by mutableStateOf(LinternaState())
    private var adsReady by mutableStateOf(false)

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        uiState = if (granted) engine.turnOn() else engine.permissionDenied()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        torchPort = AndroidTorchPort(
            context = this,
            simulateWhenUnavailable = BuildConfig.DEMO_BILLING,
        )
        premiumSequenceRunner = PremiumSequenceRunner(torchPort, pause = { delay(it) })
        engine = LinternaEngine(
            torch = torchPort,
            premiumStore = PreferencesPremiumStore(this),
        )
        uiState = engine.state

        if (!BuildConfig.DEMO_BILLING) {
            billingGateway = GooglePlayBillingGateway(
                context = this,
                productId = BuildConfig.PREMIUM_PRODUCT_ID,
                events = this,
            ).also(BillingGateway::start)
        }

        setContent {
            LinternaPremiumTheme {
                LinternaPremiumScreen(
                    state = uiState,
                    adsReady = adsReady,
                    adUnitId = BuildConfig.ADMOB_BANNER_ID,
                    isDemo = BuildConfig.DEMO_BILLING,
                    onTurnOn = ::requestTorch,
                    onPremium = ::pressPremium,
                    onNormalOff = { uiState = engine.turnOffNormally() },
                    onConfirmPurchase = ::confirmPremiumPurchase,
                    onDismissPurchase = { uiState = engine.dismissPurchase() },
                    onDismissOffer = { uiState = engine.dismissPremiumOffer() },
                )
            }
        }

        if (!uiState.isPremiumOwned && BuildConfig.ADMOB_BANNER_ID.isNotBlank()) {
            AdsCoordinator(BuildConfig.DEMO_BILLING).requestPermissionAndInitialize(this) {
                runOnUiThread { adsReady = true }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        billingGateway?.start()
    }

    override fun onPause() {
        premiumSequenceJob?.cancel()
        if (::engine.isInitialized) uiState = engine.backgrounded()
        super.onPause()
    }

    override fun onDestroy() {
        premiumSequenceJob?.cancel()
        billingGateway?.close()
        super.onDestroy()
    }

    override fun onPriceAvailable(formattedPrice: String) {
        runOnUiThread { uiState = engine.updatePrice(formattedPrice) }
    }

    override fun onPremiumPurchased() {
        runOnUiThread {
            uiState = engine.billingPurchased()
            adsReady = false
        }
    }

    override fun onBillingMessage(message: String) {
        runOnUiThread { uiState = engine.billingFailed(message) }
    }

    private fun requestTorch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            uiState = engine.turnOn()
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun pressPremium() {
        applyPremiumResult(engine.pressPremium())
    }

    private fun confirmPremiumPurchase() {
        applyPremiumResult(engine.confirmPremiumPurchase(BuildConfig.DEMO_BILLING))
    }

    private fun applyPremiumResult(result: EngineResult) {
        uiState = result.state
        when (result.effect) {
            PremiumEffect.None -> Unit
            PremiumEffect.LaunchGooglePlay -> {
                billingGateway?.launchPurchase(this)
                    ?: onBillingMessage("Google Play Billing no está configurado en esta compilación.")
            }

            PremiumEffect.RunPremiumSequence -> runPremiumSequence()
        }
    }

    private fun runPremiumSequence() {
        premiumSequenceJob?.cancel()
        premiumSequenceJob = lifecycleScope.launch {
            uiState = when (val result = premiumSequenceRunner.run()) {
                TorchResult.Success -> engine.premiumCelebrationCompleted()
                is TorchResult.Failure -> engine.premiumCelebrationFailed(result.message)
            }
        }
    }
}

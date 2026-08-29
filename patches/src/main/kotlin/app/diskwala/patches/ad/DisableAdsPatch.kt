package app.diskwala.patches.ad

import app.diskwala.patches.shared.Constants.COMPATIBILITY_DISKWALA
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

/**
 * Disables ads by stubbing SDK initializers and React Native bridge ad calls.
 * Design: early return with safe defaults so callers do not crash.
 * - Init providers: return true immediately without calling SDK init
 * - Bridge load/show: return-void without delegating to impl (no ad request)
 * - Bridge isReady: resolve Promise with false (ad not ready) then return
 * All are void methods in the spec; resolving Promise is optional but avoids JS hanging.
 */
@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    name = "Disable ads",
    description = "Stubs AppLovin, AdMob, InMobi and other ad SDKs at the bytecode level to prevent ads from loading without breaking the app.",
    default = true
) {
    compatibleWith(COMPATIBILITY_DISKWALA)

    execute {
        // 1) Stub InitProviders - prevent native SDK init
        AppLovinInitProviderFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )

        InMobiInitProviderFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )

        // MobileAdsInitProvider currently returns 0; keep as is but ensure no crash
        // If future version adds init, this will keep it stubbed to 1 (true)
        try {
            MobileAdsInitProviderFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        } catch (_: Exception) {
            // Optional fingerprint may not match on 24.5 - ignore
        }

        // 2) Stub React Native AppLovin MAX bridge - void methods -> return-void
        //    These delegate to AppLovinMAXModuleImpl; returning early prevents ad network calls.
        AppLovinMAXInitializeFingerprint.method.addInstructions(
            0,
            """
                # Resolve promise as success without initializing SDK
                const-string v0, "stub_initialized"
                invoke-interface {p3, v0}, Lcom/facebook/react/bridge/Promise;->resolve(Ljava/lang/Object;)V
                return-void
            """
        )

        AppLovinMAXLoadInterstitialFingerprint.method.addInstructions(0, "return-void")
        AppLovinMAXShowInterstitialFingerprint.method.addInstructions(0, "return-void")
        AppLovinMAXLoadRewardedAdFingerprint.method.addInstructions(0, "return-void")
        AppLovinMAXShowRewardedAdFingerprint.method.addInstructions(0, "return-void")
        AppLovinMAXLoadAppOpenAdFingerprint.method.addInstructions(0, "return-void")
        AppLovinMAXShowAppOpenAdFingerprint.method.addInstructions(0, "return-void")
        AppLovinMAXCreateBannerFingerprint.method.addInstructions(0, "return-void")
        AppLovinMAXCreateMRecFingerprint.method.addInstructions(0, "return-void")

        // 3) Stub isReady checks - resolve Promise with false, then return
        //    Signature: (Ljava/lang/String;Lcom/facebook/react/bridge/Promise;)V  -> p1=adUnitId, p2=promise
        AppLovinMAXIsInterstitialReadyFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                invoke-interface {p2, v0}, Lcom/facebook/react/bridge/Promise;->resolve(Ljava/lang/Object;)V
                return-void
            """
        )

        AppLovinMAXIsRewardedAdReadyFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                invoke-interface {p2, v0}, Lcom/facebook/react/bridge/Promise;->resolve(Ljava/lang/Object;)V
                return-void
            """
        )

        // 4) Also stub AppOpen ready if fingerprint exists (optional)
        try {
            // isAppOpenAdReady has same signature as isInterstitialReady
            val f = app.morphe.patcher.Fingerprint(
                definingClass = "Lcom/applovin/reactnative/AppLovinMAXModule;",
                name = "isAppOpenAdReady",
                returnType = "V",
                parameters = listOf("Ljava/lang/String;", "Lcom/facebook/react/bridge/Promise;")
            )
            // Only patch if it resolves (won't throw if not found when using try)
            f.method.addInstructions(
                0,
                """
                    sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                    invoke-interface {p2, v0}, Lcom/facebook/react/bridge/Promise;->resolve(Ljava/lang/Object;)V
                    return-void
                """
            )
        } catch (_: Exception) {}

        // 5) Generic safety: patch AppLovinSDK initialization in impl to no-op as well
        //    This handles cases where JS bypasses bridge and calls SDK directly
        try {
            val applovinSdkInitFingerprint = app.morphe.patcher.Fingerprint(
                definingClass = "Lcom/applovin/sdk/AppLovinSdk;",
                name = "initializeSdk",
                returnType = "V"
            )
            applovinSdkInitFingerprint.method.addInstructions(0, "return-void")
        } catch (_: Exception) {}

        // 6) Yandex and other providers - best effort
        try {
            val yandexFingerprint = app.morphe.patcher.Fingerprint(
                definingClass = "Lcom/yandex/mobile/ads/core/initializer/YandexAdsInitializeProvider;",
                name = "onCreate",
                returnType = "Z"
            )
            yandexFingerprint.method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        } catch (_: Exception) {}
    }
}

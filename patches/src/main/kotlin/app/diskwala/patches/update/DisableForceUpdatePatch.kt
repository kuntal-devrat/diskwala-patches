package app.diskwala.patches.update

import app.diskwala.patches.shared.Constants.COMPATIBILITY_DISKWALA
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Removes forced update, anti-tamper, PairIP licensing, content provider crashes, and runtime instability.
 */
@Suppress("unused")
val disableForceUpdatePatch = bytecodePatch(
    name = "Disable forced update",
    description = "Bypasses PairIP license check ('Download from Play Store'), Play Integrity, and signature verification to allow modified and sideloaded app usage.",
    default = true
) {
    compatibleWith(COMPATIBILITY_DISKWALA)

    execute {
        // 1) PairIP Startup VM bypass
        runCatching { StartupLauncherLaunchFingerprint.method.addInstructions(0, "return-void") }

        // 2) Signature checks - always return success
        runCatching { SignatureCheckVerifyIntegrityFingerprint.method.addInstructions(0, "return-void") }
        runCatching {
            SignatureCheckVerifySignatureMatchesFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }

        // 3) PairIP License Content Providers (LicenseContentProvider & LicenseContentProvider1)
        runCatching {
            LicenseContentProviderOnCreateFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }
        runCatching {
            LicenseContentProvider1OnCreateFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }

        // 4) PairIP License Client (checkLicense, initializeLicenseCheck, retryOrThrow, showPaywallOrThrow)
        runCatching { LicenseClientCheckLicenseFingerprint.method.addInstructions(0, "return-void") }
        runCatching { LicenseClientInitLicenseCheck1Fingerprint.method.addInstructions(0, "return-void") }
        runCatching { LicenseClientInitLicenseCheck2Fingerprint.method.addInstructions(0, "return-void") }
        runCatching { LicenseClientRetryOrThrowFingerprint.method.addInstructions(0, "return-void") }
        runCatching { LicenseClientShowPaywallFingerprint.method.addInstructions(0, "return-void") }

        // 5) PairIP License Activity (onCreate, showErrorDialog, showPaywallAndCloseApp)
        runCatching {
            LicenseActivityOnCreateFingerprint.method.addInstructions(
                0,
                """
                    invoke-virtual {p0}, Landroid/app/Activity;->finish()V
                    return-void
                """
            )
        }
        runCatching { LicenseActivityShowErrorDialogFingerprint.method.addInstructions(0, "return-void") }
        runCatching { LicenseActivityShowPaywallFingerprint.method.addInstructions(0, "return-void") }

        // 6) AppMetrica PreloadInfo Content Provider (Prevents crash when PairIP VM is not initialized)
        runCatching {
            PreloadInfoContentProviderOnCreateFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }

        // 7) Firebase & Crashlytics Content Providers
        runCatching {
            RNFBCrashlyticsInitProviderOnCreateFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }
        runCatching {
            FirebaseInitProviderOnCreateFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }

        // 8) Ad and Analytics Startup Providers
        runCatching {
            BigoAdsProviderOnCreateFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }
        runCatching {
            VungleProviderOnCreateFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }
        runCatching {
            IronSourceCrashProviderOnCreateFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }
        runCatching {
            IronSourceLifecycleProviderOnCreateFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }
        runCatching {
            IronSourceLevelPlayLifecycleProviderOnCreateFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }
        runCatching {
            MBComponentLifecycleProviderOnCreateFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }
        runCatching {
            AppMeasurementContentProviderOnCreateFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }

        // 9) PairIP Application & MainApplication clean startup (Bypasses PairIP Method.invoke crashes)
        runCatching {
            PairIPApplicationAttachBaseContextFingerprint.method.addInstructions(
                0,
                """
                    invoke-super {p0, p1}, Landroid/app/Application;->attachBaseContext(Landroid/content/Context;)V
                    return-void
                """
            )
        }
        runCatching {
            MainApplicationOnCreateFingerprint.method.addInstructions(
                0,
                """
                    invoke-super {p0}, Landroid/app/Application;->onCreate()V

                    sget-object v0, Lcom/facebook/react/soloader/OpenSourceMergedSoMapping;->INSTANCE:Lcom/facebook/react/soloader/OpenSourceMergedSoMapping;
                    invoke-static {p0, v0}, Lcom/facebook/soloader/SoLoader;->init(Landroid/content/Context;Lcom/facebook/soloader/ExternalSoMapping;)V

                    :try_start_rn
                    const-string v0, "reactnative"
                    invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
                    :try_end_rn
                    .catch Ljava/lang/Throwable; {:try_start_rn .. :try_end_rn} :catch_rn
                    goto :after_rn
                    :catch_rn
                    :after_rn

                    :try_start_ht
                    const-string v0, "hermestooling"
                    invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
                    :try_end_ht
                    .catch Ljava/lang/Throwable; {:try_start_ht .. :try_end_ht} :catch_ht
                    goto :after_ht
                    :catch_ht
                    :after_ht

                    :try_start_am
                    const-string v0, "appmodules"
                    invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
                    :try_end_am
                    .catch Ljava/lang/Throwable; {:try_start_am .. :try_end_am} :catch_am
                    goto :after_am
                    :catch_am
                    :after_am

                    :try_start_qc
                    const-string v0, "reactnativequickcrypto"
                    invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
                    :try_end_qc
                    .catch Ljava/lang/Throwable; {:try_start_qc .. :try_end_qc} :catch_qc
                    goto :after_qc
                    :catch_qc
                    :after_qc

                    :try_start_init
                    invoke-static {}, Lcom/facebook/react/bridge/ReactNativeJniCommonSoLoader;->staticInit()V
                    invoke-static {}, Lcom/facebook/react/bridge/BridgeSoLoader;->staticInit()V
                    :try_end_init
                    .catch Ljava/lang/Throwable; {:try_start_init .. :try_end_init} :catch_init
                    goto :after_init
                    :catch_init
                    :after_init

                    const/4 v0, 0x0
                    invoke-static {v0, v0, v0}, Lcom/facebook/react/defaults/DefaultNewArchitectureEntryPoint;->load(ZZZ)V

                    return-void
                """
            )
        }
        runCatching {
            MainActivityOnCreateFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    invoke-super {p0, v0}, Lcom/facebook/react/ReactActivity;->onCreate(Landroid/os/Bundle;)V
                    return-void
                """
            )
        }

        // 9b) VMRunner stub - return null instead of executing PairIP VM programs
        runCatching {
            VMRunnerInvokeFingerprint.method.addInstructions(
                0,
                """
                    const/4 p0, 0x0
                    return-object p0
                """
            )
        }
        runCatching { VMRunnerSetContextFingerprint.method.addInstructions(0, "return-void") }

        // 9c) OpenSourceMergedSoMapping.invokeJniOnload stub
        runCatching { OpenSourceMergedSoMappingInvokeJniOnloadFingerprint.method.addInstructions(0, "return-void") }

        // 9d) ReactMarker.notifyNativeMarker stub
        runCatching { ReactMarkerNotifyNativeMarkerFingerprint.method.addInstructions(0, "return-void") }

        // 9e) InspectorFlags stubs
        runCatching {
            InspectorFlagsGetFuseboxEnabledFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """
            )
        }
        runCatching {
            InspectorFlagsGetIsProfilingBuildFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """
            )
        }

        // 9f) Arguments.addEntry null-key safety
        runCatching {
            ArgumentsAddEntryFingerprint.method.addInstructions(
                0,
                """
                    if-nez p1, :cond_args_key_ok
                    return-void
                    :cond_args_key_ok
                """
            )
        }

        // 9g) WritableNativeMap null-key safety
        runCatching {
            WritableNativeMapPutMapFingerprint.method.addInstructions(
                0,
                """
                    if-nez p1, :cond_putmap_ok
                    return-void
                    :cond_putmap_ok
                """
            )
        }
        runCatching {
            WritableNativeMapPutArrayFingerprint.method.addInstructions(
                0,
                """
                    if-nez p1, :cond_putarray_ok
                    return-void
                    :cond_putarray_ok
                """
            )
        }

        // 9h) ReactTextInputShadowNode.createInternalEditText safe fallback
        runCatching {
            ReactTextInputShadowNodeCreateInternalEditTextFingerprint.method.addInstructions(
                0,
                """
                    invoke-virtual {p0}, Lcom/facebook/react/uimanager/ReactShadowNodeImpl;->getThemedContext()Lcom/facebook/react/uimanager/ThemedReactContext;
                    move-result-object v0
                    new-instance v1, Landroid/widget/EditText;
                    const/4 v2, 0x0
                    const/4 v3, 0x0
                    invoke-direct {v1, v0, v2, v3}, Landroid/widget/EditText;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;I)V
                    return-object v1
                """
            )
        }

        // 10) Play Integrity Token bypass
        runCatching {
            PlayIntegrityRequestTokenFingerprint.method.addInstructions(
                0,
                """
                    const-string v0, "diskwala_stub_integrity_token"
                    invoke-interface {p3, v0}, Lcom/facebook/react/bridge/Promise;->resolve(Ljava/lang/Object;)V
                    return-void
                """
            )
        }
        runCatching {
            PlayIntegrityLambdaRejectFingerprint.method.addInstructions(
                0,
                """
                    const-string v0, "diskwala_stub_integrity_token"
                    invoke-interface {p1, v0}, Lcom/facebook/react/bridge/Promise;->resolve(Ljava/lang/Object;)V
                    return-void
                """
            )
        }

        // 11) BlobCollector nativeInstall stub
        runCatching {
            BlobCollectorNativeInstallFingerprint.method.addInstructions(0, "return-void")
        }

        // 12) DefaultNewArchitectureEntryPoint load stub
        runCatching {
            DefaultNewArchitectureEntryPointLoadFingerprint.method.addInstructions(0, "return-void")
        }

        // 13) FreeRASP native modules stub
        runCatching {
            FreeRaspCreateNativeModulesFingerprint.method.addInstructions(
                0,
                """
                    sget-object v0, Ljava/util/Collections;->EMPTY_LIST:Ljava/util/List;
                    return-object v0
                """
            )
        }

        // 14) React Native Shadow Nodes (Switch measurement crash-proofing)
        runCatching {
            ReactSwitchShadowNodeMeasureFingerprint.method.addInstructions(
                0,
                """
                    const/16 v0, 0x80
                    const/16 v1, 0x48
                    invoke-static {v0, v1}, Lcom/facebook/yoga/YogaMeasureOutput;->make(II)J
                    move-result-wide v0
                    return-wide v0
                """
            )
        }

        // 15) SystemProps Null Safety (Prevents "key can't be null" NPE in System.getProperty when PairIP strings are bypassed)
        runCatching {
            SystemPropsGetPropertyGFingerprint.method.addInstructions(
                0,
                """
                    if-nez p0, :cond_g_null
                    const/4 v0, 0x0
                    return-object v0
                    :cond_g_null
                """
            )
        }
        runCatching {
            SystemPropsGetPropertyFFingerprint.method.addInstructions(
                0,
                """
                    if-nez p0, :cond_f_null
                    const/4 v0, 0x0
                    return-object v0
                    :cond_f_null
                """
            )
        }
        runCatching {
            SystemPropsGetPropertyF2Fingerprint.method.addInstructions(
                0,
                """
                    if-nez p0, :cond_f2_null
                    return-object p1
                    :cond_f2_null
                """
            )
        }
        runCatching {
            SystemPropsGetPropertyHFingerprint.method.addInstructions(
                0,
                """
                    if-nez p0, :cond_h_null
                    return-object p1
                    :cond_h_null
                """
            )
        }
    }
}

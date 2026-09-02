package app.diskwala.patches.update

import app.diskwala.patches.shared.Constants.COMPATIBILITY_DISKWALA
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Removes forced update, modded app warnings, and PairIP Play Store license checks.
 * Bypasses:
 * - "Please Download App From App/Play Store."
 * - "Check that Google Play is enabled on your device..."
 * - "Modded APK Detected. Modded App not Allowed."
 * - "newer version is available with improvements and bug fixes. Please update to continue using the app."
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

        // 3) PairIP License Checkers (LicenseContentProvider & LicenseContentProvider1)
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

        // 6) Play Integrity Token bypass
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

        // 7) React Native Shadow Nodes (Switch & TextInput measurement crash-proofing)
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
    }
}

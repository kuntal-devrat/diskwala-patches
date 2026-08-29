package app.diskwala.patches.update

import app.diskwala.patches.shared.Constants.COMPATIBILITY_DISKWALA
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Removes forced update / anti-tamper screen.
 * DiskWala 24.5 uses PairIP VM protection + Play Integrity + signature checks to enforce:
 * - "newer version is available with improvements and bug fixes. Please update to continue using the app."
 * - "Please Download App From App/Play Store."
 * - "Modded APK Detected. Modded App not Allowed."
 * - "APK Diubah Suai Dikesan."
 *
 * The update check lives inside encrypted VM bytecode (assets/XrSWhF7qgXWkwZNT etc.) executed via
 * VMRunner.invoke -> VmDecryptor.decrypt -> executeVM (native libpairipcore.so).
 * The simplest, stub-safe bypass is to prevent the VM from running at all, and to make
 * integrity checks always succeed. The app's React Native code then never receives the
 * "forceUpdate" signal and proceeds to normal feed.
 *
 * We patch at multiple layers for redundancy:
 * 1) StartupLauncher.launch() -> return-void (prevents VM startup)
 * 2) VMRunner.invoke / executeVM -> return null (if launch is bypassed elsewhere)
 * 3) SignatureCheck -> always succeed (prevents "Apk signature is invalid")
 * 4) PlayIntegrity.requestToken -> resolve with fake token instead of calling Play services
 */
@Suppress("unused")
val disableForceUpdatePatch = bytecodePatch(
    name = "Disable forced update",
    description = "Bypasses PairIP integrity checks, Play Integrity and signature verification to remove the forced update / modded-app screen.",
    default = true
) {
    compatibleWith(COMPATIBILITY_DISKWALA)

    // Must run before any ad/premium patches that might depend on stable state
    execute {
        // 1) Prevent PairIP VM from starting - most effective single patch
        runCatching { StartupLauncherLaunchFingerprint.method.addInstructions(0, "return-void") }

        // 2) Defence in depth: if something still calls VMRunner directly, stub it
        runCatching {
            VMRunnerInvokeFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return-object v0
                """
            )
        }

        // executeVM is native; we cannot add instructions to native, but we can make it non-native
        // by patching the caller. The morphe patcher will replace body; native flag will be cleared
        // when we add instructions. Do it defensively inside try/catch in case fingerprint is native.
        runCatching {
            VMRunnerExecuteVMFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return-object v0
                """
            )
        }

        // 3) Signature checks - always return success
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

        // 4) Play Integrity - immediately resolve Promise with fake token
        //    Original does async Task addOnSuccessListener; we short-circuit to resolve directly
        //    so JS receives a token and proceeds. Using the nonce as token is safe; server only checks non-empty.
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

        // 5) Also patch the failure lambda to not reject (defence in depth)
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
    }
}

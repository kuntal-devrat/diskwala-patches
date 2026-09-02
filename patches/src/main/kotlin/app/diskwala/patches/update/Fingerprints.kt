package app.diskwala.patches.update

import app.morphe.patcher.Fingerprint

/**
 * Fingerprints for forced update / integrity / PairIP license check bypass.
 * Direct signatures without brittle methodCall filters to ensure 100% match rate across builds.
 */

// 1. SignatureCheck
internal object SignatureCheckVerifyIntegrityFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/SignatureCheck;",
    name = "verifyIntegrity",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;")
)

internal object SignatureCheckVerifySignatureMatchesFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/SignatureCheck;",
    name = "verifySignatureMatches",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;")
)

// 2. StartupLauncher
internal object StartupLauncherLaunchFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/StartupLauncher;",
    name = "launch",
    returnType = "V"
)

// 3. License Content Providers (LicenseContentProvider & LicenseContentProvider1)
internal object LicenseContentProviderOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseContentProvider;",
    name = "onCreate",
    returnType = "Z"
)

internal object LicenseContentProvider1OnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseContentProvider1;",
    name = "onCreate",
    returnType = "Z"
)

// 4. LicenseClient (checkLicense, initializeLicenseCheck, retryOrThrow, showPaywallOrThrow)
internal object LicenseClientCheckLicenseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;")
)

internal object LicenseClientInitLicenseCheck1Fingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "initializeLicenseCheck",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;")
)

internal object LicenseClientInitLicenseCheck2Fingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "initializeLicenseCheck",
    returnType = "V",
    parameters = listOf("Z")
)

internal object LicenseClientRetryOrThrowFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "retryOrThrow",
    returnType = "V"
)

internal object LicenseClientShowPaywallFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "showPaywallOrThrow",
    returnType = "V"
)

// 5. LicenseActivity (onCreate, showErrorDialog, showPaywallAndCloseApp)
internal object LicenseActivityOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;")
)

internal object LicenseActivityShowErrorDialogFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseActivity;",
    name = "showErrorDialog",
    returnType = "V"
)

internal object LicenseActivityShowPaywallFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseActivity;",
    name = "showPaywallAndCloseApp",
    returnType = "V"
)

// 6. PlayIntegrity
internal object PlayIntegrityRequestTokenFingerprint : Fingerprint(
    definingClass = "Lcom/diskwalaapp/integrity/PlayIntegrityModule;",
    name = "requestToken",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;", "Lcom/facebook/react/bridge/Promise;")
)

internal object PlayIntegrityLambdaRejectFingerprint : Fingerprint(
    definingClass = "Lcom/diskwalaapp/integrity/PlayIntegrityModule;",
    name = "requestToken\$lambda\$2",
    returnType = "V",
    parameters = listOf("Lcom/facebook/react/bridge/Promise;", "Ljava/lang/Exception;")
)

// 7. ReactSwitch & TextInput Shadow Nodes (measure & createInternalEditText)
internal object ReactSwitchShadowNodeMeasureFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/react/views/switchview/ReactSwitchShadowNode;",
    name = "measure",
    returnType = "J"
)

internal object ReactTextInputShadowNodeCreateInternalEditTextFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/react/views/textinput/ReactTextInputShadowNode;",
    name = "createInternalEditText",
    returnType = "Landroid/widget/EditText;"
)

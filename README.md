# ?? DiskWala Patches

Morphe patches for **DiskWala** `com.diskwalaapp` — remove ads and forced update screen. No root required.

## ? About

Patches for DiskWala that stub ad SDKs (AppLovin, InMobi, AdMob) at the bytecode level and bypass PairIP/Play Integrity checks that enforce `Please update to continue using the app` / `Modded APK Detected` screens.

Patches are built with [Morphe Patcher](https://github.com/MorpheApp/morphe-patcher) and work on `24.5` (`334`) and future versions experimentally.

### How to use these patches

Click here to add these patches to Morphe: **https://morphe.software/add-source?github=kuntal-devrat/diskwala-patches**

Or in Morphe Manager ? Sources ? Add ? `https://github.com/kuntal-devrat/diskwala-patches`

## ?? Patches list

<!-- PATCHES_START EXPANDED -->
> **[vunspecified](https://github.com/kuntal-devrat/diskwala-patches/releases/tag/vunspecified)**&nbsp;&nbsp;â€¢&nbsp;&nbsp;`dev`&nbsp;&nbsp;â€¢&nbsp;&nbsp;3 patches total
<details open>
<summary>ðŸ“¦ DiskWala&nbsp;&nbsp;â€¢&nbsp;&nbsp;3 patches</summary>
<br>

**ðŸŽ¯ Supported versions:**

| 24.5 |
| :---: |

| ðŸ’Š&nbsp;Patch | ðŸ“œ&nbsp;Description | âš™ï¸&nbsp;Options |
|----------|----------------|-----------|
| [Disable ads](#disable-ads) | Stubs AppLovin, AdMob, InMobi and other ad SDKs at the bytecode level to prevent ads from loading without breaking the app. |  |
| [Disable forced update](#disable-forced-update) | Bypasses PairIP integrity checks, Play Integrity and signature verification to remove the forced update / modded-app screen. |  |
| [Unlock premium](#unlock-premium) | Makes RevenueCat entitlements appear active so ads-free and premium features are unlocked. |  |

</details>

<!-- PATCHES_END -->

## ? Features

| Patch | Description |
|-------|-------------|
| **Disable ads** | Stubs `AppLovinInitProvider`, `InMobiInitProvider`, `MobileAdsInitProvider` and the React Native `AppLovinMAX` bridge (`loadInterstitial`, `showInterstitial`, `loadRewardedAd`, `showRewardedAd`, `isInterstitialReady` etc.) to return without requesting ads. Promise-based `isReady` calls resolve with `false` so JS does not hang. Safe early-return, no null crashes. |
| **Unlock premium** | Makes `EntitlementInfo.isActive()` always `true` and `EntitlementInfos.getActive()` return `all` map, so `Buy Subscription to enjoy Ads Free Experience` gate passes. |
| **Disable forced update** | Bypasses PairIP VM protection (`StartupLauncher.launch` ? `return-void`, `VMRunner.invoke` ? `null`), signature checks (`SignatureCheck.verifyIntegrity` ? no-op, `verifySignatureMatches` ? `true`) and Play Integrity (`PlayIntegrity.requestToken` ? resolve with `diskwala_stub_integrity_token`). The Hermes JS bundle then never receives `forceUpdate` / `Modded APK Detected` signals. |

## ?? Getting development started

See [Morphe documentation](https://github.com/MorpheApp/morphe-documentation/blob/main/docs/morphe-development/README.md) and [patcher docs](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/2_1_setup.md#-prepare-the-environment) for setup.

1. Add GitHub PAT with `read:packages` to `~/.gradle/gradle.properties`:
   ```
   gpr.user=your_github_username
   gpr.key=ghp_xxxxxxxxxxxxxxxxxxxx
   ```
   or set `GITHUB_ACTOR` / `GITHUB_TOKEN` env.

2. Build locally:
   ```bash
   ./gradlew buildAndroid
   # ? patches/build/libs/patches-*.mpp
   ```
   Test with Morphe Desktop like any other bundle.

3. Workflow mirrors template:
   - **Make all changes to the `dev` branch** ? `fix:` / `feat:` commits create pre-releases.
   - Merge `dev` ? `main` to create stable release (backmerge handled by CI).
   - Never push `patches-list.json`, `patches-bundle.json`, `CHANGELOG.md` manually.

## ??? XAPK handling

`com.diskwalaapp_24.5.xapk` contains base + splits (`config.arm64_v8a`, `config.en`, `config.xxhdpi`). Provide **base APK** `com.diskwalaapp.apk` to Morphe; after patching reinstall with original splits via `adb install-multiple` or repack XAPK.

Patched artifacts from manual `apktool` build:
- `D:\DiskWala\com.diskwalaapp_24.5-patched.apk` (signed, 64MB)
- `D:\DiskWala\com.diskwalaapp_24.5-patched.xapk` (79MB)

## ?? Verify

```bash
adb install-multiple patched_base.apk config.arm64_v8a.apk config.en.apk config.xxhdpi.apk
adb logcat | grep -i -E "AppLovin|PlayIntegrity|SignatureCheck|VMRunner"
# Expect: no AdLoader logs, no INTEGRITY_ERROR, no "Executing XrSWhF7qgXWkwZNT"
```

## ?? License

GPL-3.0 with Morphe Section 7 restrictions — see [LICENSE](LICENSE) and [NOTICE](NOTICE). **Do not use “Morphe” in fork branding**; describe as “compatible with Morphe”.

<!-- The patches end tag intentionally above so first release cleans up dev instructions -->

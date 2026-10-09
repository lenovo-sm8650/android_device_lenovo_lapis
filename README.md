# Lenovo Yoga Tab Plus (lapis) — device tree

<p>
  <a href="https://sourceforge.net/projects/pixelos-unofficial-tb520fu/"><img alt="SourceForge OSS Rising Star" src="https://sourceforge.net/cdn/syndication/badge_img/4143825/oss-rising-star-white?achievement=oss-rising-star" width="125"></a>
</p>

[![Total downloads](https://img.shields.io/sourceforge/dt/pixelos-unofficial-tb520fu.svg?label=downloads)](https://sourceforge.net/projects/pixelos-unofficial-tb520fu/files/seventeen/)
[![Monthly downloads](https://img.shields.io/sourceforge/dm/pixelos-unofficial-tb520fu.svg)](https://sourceforge.net/projects/pixelos-unofficial-tb520fu/files/seventeen/)

Unofficial device tree for the Lenovo Yoga Tab Plus / YOGA Pad Pro (model
TB520FU, codename lapis, Qualcomm Snapdragon 8 Gen 3).

| Branch | Builds |
|---|---|
| `lineage-24.0` | plain LineageOS 24 (`lineage_lapis`), no source patches |
| `seventeen` | PixelOS 17 (`custom_lapis`): `lineage-24.0` plus the PixelOS product, LunarisDolby, video motion smoothing and the maintainer customizations |

| | |
|---|---|
| SoC | Qualcomm SM8650 (pineapple) |
| Kernel | GKI `6.1.138-android14-11`, built from source |
| Display | 2944×1840 dual-DSI, natively landscape, density 340 |
| Codename | lapis (`ro.vendor.config.lgsi.project`) |
| Stock firmware | `ZUI_17.5.10.362_260719_ROW` |
| Shipping API | 34 |


## Features

Highlights of the PixelOS (`seventeen`) build:

### Device features

- **Widevine L1** — Netflix HD playback.
- **HDR, HDR10+ and Dolby Vision** — HDR video playback with the device's display and media codecs.
- **Dolby Atmos** — LunarisDolby settings, sound profiles and equalizer.
- **Play Integrity** — BASIC, DEVICE and STRONG.
- **OTA updates** — full and incremental updates through Settings > System > System update.
- **Lenovo Pencil** — automatic pairing, battery and charging status, writing haptics, pen buttons and the Lenovo pen settings.
- **Lenovo keyboard** — touchpad controls, configurable shortcut keys, backlight, firmware updates and desktop mode integration.
- **Folio case** — close the cover to sleep and lock; open it to wake.
- **Adaptive refresh rate** — 30/60/90/120/144 Hz display modes with adaptive switching.
- **Battery controls** — charging limits, battery protection, bypass charging and standby power saving.
- **Natural colors** — ambient white balance with adjustable strength.
- **Memory extension** — storage-backed virtual memory with a selectable size.
- **Double tap to wake** and a **PC mode Quick Settings tile**.
- **Video motion smoothing** — per-app Qualcomm VPP frame interpolation (MEMC).

HDR and Dolby playback depends on the app, content and streaming subscription.

### Custom features

Available in Settings > System > Custom Tweaks:

- **Per-app CPU/GPU performance** — separate Power saving, Balanced, Default and custom limits for each app, with optional background memory cleanup for games. Profiles apply while the app is on screen and return to normal when you leave it.
- **Device identity spoofing** — choose the brand, manufacturer and model reported to selected apps.
- **Play Store installer reporting** — make selected apps recognize Google Play as their installer.

## Working

- Display, Wi-Fi, Bluetooth and almost all other features tested so far.
- Miracast (wireless display).

## Not working

None found so far.

## Downloads

Latest build: see [Releases](https://github.com/lenovo-sm8650/android_device_lenovo_lapis/releases/),
installation steps in the release notes. Files are on
[SourceForge](https://sourceforge.net/projects/pixelos-unofficial-tb520fu/files/seventeen/).
Pick the region of your device: ROW and PRC only differ in the device tree
(dtb) and the signed images that carry it.

The `.zip` installs from TWRP or the PixelOS recovery; the `ltbox_*.7z` is a
firmware package for LTBox (EDL). Both keep the user data when updating an
installed build; coming from the stock firmware or another ROM, format data.
Installed builds that include the optional customizations also update
themselves (Settings > System > System update).

## Repositories

All repositories are in the [lenovo-sm8650](https://github.com/lenovo-sm8650)
organization, laid out like the LineageOS trees (device, SoC common tree, hardware, blobs,
kernel):

| Path | Repository | Contents |
|---|---|---|
| `device/lenovo/lapis` | `android_device_lenovo_lapis` | this tree |
| `device/lenovo/sm8650-common` | `android_device_lenovo_sm8650-common` | SM8650 (pineapple) platform configuration |
| `device/lenovo/lapis-kernel` | `android_device_lenovo_lapis-kernel` | stock vendor kernel modules, dtb and dtbo |
| `hardware/lenovo` | `android_hardware_lenovo` | TB520FUParts, the `tb520fu-input` system_server bridge; on `seventeen` also LunarisDolby, VideoMotion and the maintainer customizations (`custom/`) |
| `kernel/lenovo/sm8650` | `android_kernel_lenovo_sm8650` | Android common kernel `android14-6.1` at `2ecae636cf9b` (the source of the stock GKI kernel) plus the Qualcomm UAPI headers |
| `vendor/lenovo/lapis` | `proprietary_vendor_lenovo_lapis` | device blobs (Git LFS for files over 50 MB) |
| `vendor/lenovo/sm8650-common` | `proprietary_vendor_lenovo_sm8650-common` | platform blobs |

Every repository has a `lineage-24.0` and a `seventeen` branch;
`lineage.dependencies` lists them for roomservice.

PixelOS (`seventeen`) also needs a few source changes. They are commits in
forks of the PixelOS projects (`seventeen` branch; Aperture from LineageOS,
`lineage-24.0`), which the local manifest below puts in place of the
originals:

| Path | Fork | Changes |
|---|---|---|
| `frameworks/base` | `android_frameworks_base` | Lenovo pen haptic and keyboard managers, white balance strength, keyboard desktop mode opt-out, pen hover pointer, installer report for picked apps |
| `frameworks/av` | `android_frameworks_av` | video motion smoothing (Qualcomm VPP) |
| `frameworks/native` | `android_frameworks_native` | 60 Hz floor for a static screen |
| `packages/apps/Settings` | `android_packages_apps_Settings` | optional white balance switch, Lenovo `PLACE_HOLDER` action |
| `packages/apps/ParanoidSense` | `android_packages_apps_ParanoidSense` | landscape enrollment preview |
| `packages/apps/Updater` | `android_packages_apps_Updater` | SourceForge update server |
| `packages/apps/Aperture` | `android_packages_apps_Aperture` | UI rotation on a landscape display |
| `vendor/lineage` | `android_vendor_lineage` | prebuilt vendor modules, kernel header cleanup |
## Kernel

The stock firmware runs Google's GKI build of `android14-6.1`
(`6.1.138-android14-11-g2ecae636cf9b-ab14676408`). This tree builds the same
source with `gki_defconfig` (clang r547379; GKI uses r487747c, newer clang
fails on this kernel). The 60 GKI modules go to system_dlkm, signed with a
key generated for each build. Lenovo/Qualcomm only ship the vendor modules
(vendor_boot, vendor_dlkm, all unsigned) and the device trees; their source
is not published at this version, so they come from the stock firmware
(`device/lenovo/lapis-kernel/`). Only `android14-6.1` updates keep working
with them (stable KMI).

Source: [kernel/common](https://android.googlesource.com/kernel/common/+/2ecae636cf9be43fdfe04adb25b2c2987838955a),
Lenovo's release: https://support.lenovo.com/us/en/solutions/ht511330-lenovo-open-source-portal

## Getting the source

Git LFS is needed for two blobs in the vendor repository (and, on
`seventeen`, for the APKs in `hardware/lenovo/custom`).

```bash
sudo apt install git-lfs && git lfs install
mkdir pixelos && cd pixelos
repo init -u https://github.com/PixelOS-AOSP/android_manifest -b seventeen --git-lfs
mkdir -p .repo/local_manifests
# save the manifest below as .repo/local_manifests/lapis.xml
repo sync -c -j$(nproc)
```

`.repo/local_manifests/lapis.xml` for PixelOS:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<manifest>
  <remote name="lapis" fetch="https://github.com/lenovo-sm8650" revision="seventeen" />

  <!-- Replaced by the forks below -->
  <remove-project name="PixelOS-AOSP/android_frameworks_av" />
  <remove-project name="PixelOS-AOSP/android_frameworks_base" />
  <remove-project name="PixelOS-AOSP/android_frameworks_native" />
  <remove-project name="PixelOS-AOSP/android_packages_apps_ParanoidSense" />
  <remove-project name="PixelOS-AOSP/android_packages_apps_Settings" />
  <remove-project name="PixelOS-AOSP/android_packages_apps_Updater" />
  <remove-project name="PixelOS-AOSP/android_vendor_lineage" />
  <remove-project name="LineageOS/android_packages_apps_Aperture" />
  <!-- Replaced by LunarisDolby (hardware/lenovo) -->
  <remove-project name="PixelOS-AOSP/android_packages_apps_DolbyAtmos" />

  <project name="android_device_lenovo_lapis" path="device/lenovo/lapis" remote="lapis" />
  <project name="android_device_lenovo_lapis-kernel" path="device/lenovo/lapis-kernel" remote="lapis" />
  <project name="android_device_lenovo_sm8650-common" path="device/lenovo/sm8650-common" remote="lapis" />
  <project name="android_hardware_lenovo" path="hardware/lenovo" remote="lapis" />
  <project name="android_kernel_lenovo_sm8650" path="kernel/lenovo/sm8650" remote="lapis" />
  <project name="proprietary_vendor_lenovo_lapis" path="vendor/lenovo/lapis" remote="lapis" />
  <project name="proprietary_vendor_lenovo_sm8650-common" path="vendor/lenovo/sm8650-common" remote="lapis" />

  <project name="android_frameworks_av" path="frameworks/av" remote="lapis" />
  <project name="android_frameworks_base" path="frameworks/base" remote="lapis" />
  <project name="android_frameworks_native" path="frameworks/native" remote="lapis" />
  <project name="android_packages_apps_Aperture" path="packages/apps/Aperture" remote="lapis" revision="lineage-24.0" />
  <project name="android_packages_apps_ParanoidSense" path="packages/apps/ParanoidSense" remote="lapis" />
  <project name="android_packages_apps_Settings" path="packages/apps/Settings" remote="lapis" />
  <project name="android_packages_apps_Updater" path="packages/apps/Updater" remote="lapis" />
  <project name="android_vendor_lineage" path="vendor/lineage" remote="lapis" />
</manifest>
```

For LineageOS, use `revision="lineage-24.0"` and only the device, hardware,
kernel and vendor projects.
## Building

```bash
source build/envsetup.sh
breakfast lapis user
m pixelos        # LineageOS: m bacon
```

`user` builds keep ADB authentication on and exclude debug tools, ADB root
and the OTA `addon.d` preservation path. `WITH_ADB_INSECURE=true` turns ADB
authentication off. The AVB and app signing keys stay as configured below.
## OTA publishing

Belongs to the customizations (`hardware/lenovo/custom`, `seventeen` only);
see its README for the SourceForge folder layout, its `tools/ota_json.py` and
the incremental OTA steps.
## Installing

Sideload the OTA package (`out/target/product/lapis/PixelOS_lapis-*.zip`)
from the PixelOS recovery: Apply update > Apply from ADB, then
`adb sideload <zip>`. It installs to the other slot, like any A/B update.

If data has to be wiped (first install, or a change of signing keys), format
data **before** sideloading. Formatting after the sideload also wipes the
update snapshot in `/metadata`, and the new slot does not boot.

The pvmfw image is part of the package: with dm-verity on, the bootloader
checks it through vbmeta (stock `pvmfw.img` in the vendor repository, added
with `--include_descriptors_from_image`).

## Layout

Based on the LineageOS OnePlus Pad 2 (`caihong`) and `oneplus/sm8650-common`
trees, merged into one tree with the OnePlus-specific parts removed.

- `init/`, `vintf/`, `sepolicy/`, `overlay/` — from the stock firmware
  (`LapisRowFrameworksOverlay`, `LapisRowWifiResOverlay`,
  `manifest_pineapple.xml` without IMS/DPM, the device is Wi-Fi only).
- `health/` — QTI health HAL copy that ignores the pen charger (`wls_tx`).
- `configs/idc/`, `configs/keylayout/` — the stock Lenovo pen and keyboard
  input configurations, ZUI-only keycodes remapped to AOSP keycodes.
- `lenovo/PenService/` — the stock PenService with a compat dex for APIs that
  changed in Android 17 and the PixelOS look of the pen settings (card groups).
- `lenovo/KeyboardUpdate/` — the stock keyboard firmware updaters with a
  compat dex that gives their page the PixelOS look (no resource overlays).
- `configs/displayconfig/` — display configuration of the panel.
- `system_ext.prop` — besides the stock values: `ro.config.lgsi.device.type=pad`
  (the stock Lenovo apps use the tablet dialog layout with it) and a linear
  brightness slider like stock ZUI.

In `hardware/lenovo`:

- `packages/TB520FUParts/` — "Lenovo features" in Settings > System:
  charging modes, white balance strength, memory extension (zram writeback),
  pen settings, folio case mode, and the physical keyboard page of the stock
  settings with the keyboard firmware update.
- `input/` — `tb520fu-input.jar`, loaded into system_server as a
  DeviceKeyHandler: Lenovo pen (attach, pairing, battery, writing haptics,
  buttons), keyboard keys, charging modes, double tap to wake and the folio
  case mode (the cover is the sensor HAL hall effect sensor, confirmed by the
  light sensor, as on stock), ported from the stock ZUI services (see
  `input/NOTICE`).

## Verified boot

The stock firmware is signed with the public AOSP `testkey_rsa4096` (the flaw
LTBox uses), and this tree signs the same way: recovery is chained at
location 1, vbmeta_system at 2 and boot at 3. The device has no
`vbmeta_vendor`. dm-verity is on, so after checking that a build boots
unlocked, `fastboot flashing lock` (wipes data) gives a locked, green boot.

Apps are signed with the AOSP test keys, so anyone can build compatible
updates. Switching to other keys later needs a data wipe.

## Extracting blobs

```bash
./extract-files.py <dump>
```

`<dump>` is an extracted stock firmware containing `vendor/`, `odm/`,
`system_ext/` and `product/`. Not needed when the vendor repository is
synced.

## Credits
- **[LineageOS Team](https://github.com/LineageOS)** — [android_device_oneplus_caihong](https://github.com/LineageOS/android_device_oneplus_caihong), [android_kernel_oneplus_sm8650](https://github.com/LineageOS/android_kernel_oneplus_sm8650)
- **[PixelOS Team](https://github.com/PixelOS-AOSP)** — [PixelOS-AOSP](https://github.com/orgs/PixelOS-AOSP/repositories)
- **[miner7222](https://github.com/miner7222)** — [Lenovo-SM8850](https://github.com/Lenovo-SM8850/android_hardware_lenovo)
- **[Pong-Development](https://github.com/Pong-Development)** — [hardware_dolby](https://github.com/Pong-Development/hardware_dolby)
- **[sungwon1002](https://github.com/sungwon1002)** — [android_device_lenovo_TB710FU](https://github.com/sungwon1002/android_device_lenovo_TB710FU)

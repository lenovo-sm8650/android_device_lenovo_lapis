# Lenovo Yoga Tab Plus (lapis) — device tree

Device tree for the Lenovo Yoga Tab Plus / YOGA Pad Pro (model TB520FU,
codename lapis, Qualcomm Snapdragon 8 Gen 3). The `lineage-24.0` branch is
plain LineageOS 24 (`lineage_lapis`): no source patches besides the
`vendor/lineage` kernel build change in Building, no Lenovo apps.

| | |
|---|---|
| SoC | Qualcomm SM8650 (pineapple) |
| Kernel | GKI `6.1.138-android14-11`, built from source |
| Display | 2944×1840 dual-DSI, natively landscape, density 340 |
| Codename | lapis (`ro.vendor.config.lgsi.project`) |
| Stock firmware | `18.5.10.037_260929_ROW` |
| Shipping API | 34 |

## Features

- **Hardware** — audio, Wi-Fi, Bluetooth, camera, fingerprint (Goodix),
  sensors, thermal, firmware and the GKI kernel with the stock vendor modules.
- **Widevine L1**, **KeyMint / StrongBox**.
- **HDR, HDR10 and HDR10+** media codecs, and **Dolby Vision** playback
  (stock `DolbyVisionService` with the DVS daemon). HDR and Dolby Vision
  playback depends on the app, content and streaming subscription.
- **WiFi Display** (Miracast) through the QTI session.
- **Landscape by default** — default rotation, boot animation
  (`ro.bootanim.set_orientation_logical_0`), density and tablet characteristics.
- **Charging control** — LineageOS Health HAL
  (`vendor.lineage.health-service.default`) on `charging_enable`
  (1 = charge, 0 = stop charging while the charger keeps powering the tablet);
  configured in `device.mk`, `init/ueventd.lapis.rc` and
  `sepolicy/vendor/hal_lineage_health_default.te`.
- **Stylus** — the NVTCapacitivePen touch controller is a plain AOSP stylus
  (pressure, tilt, `BTN_STYLUS`); no input configuration is needed.
- **Keyboard** — the stock Lenovo keyboard keylayouts and input device
  configurations (`configs/`).
- **Folio cover** — `keyhandler/`, a LineageOS key handler
  (`config_deviceKeyHandlerLibs/Classes` in `overlay-lineage/lineage-sdk`)
  that puts the device to sleep when the cover is closed and wakes it when it
  is opened (hall effect sensor, confirmed by the light sensor).
- **Natural colors** — AOSP display white balance with the ambient color
  temperature of the RGB sensor (`overlay/FrameworksResTarget`).
- **Brightness** — AOSP automatic brightness curve of the panel.
- **Refresh rate** — 120 Hz default and peak. The pen does not work at 90 and
  144 Hz, so the display is kept at 120 Hz (see the comment in
  `overlay/FrameworksResTarget/res/values/config.xml`).

## Working

- Display, Wi-Fi, Bluetooth and almost all other features.
- Miracast (wireless display).

## Repositories

All repositories are in the [lenovo-sm8650](https://github.com/lenovo-sm8650)
organization, laid out like the LineageOS trees (device, SoC common tree,
blobs, kernel):

| Path | Repository | Contents |
|---|---|---|
| `device/lenovo/lapis` | `android_device_lenovo_lapis` | this tree |
| `device/lenovo/sm8650-common` | `android_device_lenovo_sm8650-common` | SM8650 (pineapple) platform configuration |
| `device/lenovo/lapis-kernel` | `android_device_lenovo_lapis-kernel` | stock vendor kernel modules, dtb and dtbo |
| `kernel/lenovo/sm8650` | `android_kernel_lenovo_sm8650` | Android common kernel `android14-6.1` at `2ecae636cf9b` (the source of the stock GKI kernel) plus the Qualcomm UAPI headers |
| `vendor/lenovo/lapis` | `proprietary_vendor_lenovo_lapis` | device blobs (Git LFS for files over 50 MB) |
| `vendor/lenovo/sm8650-common` | `proprietary_vendor_lenovo_sm8650-common` | platform blobs |
| `vendor/lineage` | `android_vendor_lineage` | LineageOS `vendor/lineage` with the kernel build change in Building |

`lineage.dependencies` lists the repositories for roomservice. The rest of the
source is LineageOS 24 as published; the local manifest below puts the
`vendor/lineage` fork in place of the original.

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

Git LFS is needed for two blobs in the vendor repository.

```bash
sudo apt install git-lfs && git lfs install
mkdir lineage && cd lineage
repo init -u https://github.com/LineageOS/android.git -b lineage-24.0 --git-lfs
mkdir -p .repo/local_manifests
# save the manifest below as .repo/local_manifests/lapis.xml
repo sync -c -j$(nproc)
```

`.repo/local_manifests/lapis.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<manifest>
  <remote name="lapis" fetch="https://github.com/lenovo-sm8650" revision="lineage-24.0" />

  <!-- Replaced by the fork below -->
  <remove-project name="LineageOS/android_vendor_lineage" />

  <project name="android_device_lenovo_lapis" path="device/lenovo/lapis" remote="lapis" />
  <project name="android_device_lenovo_lapis-kernel" path="device/lenovo/lapis-kernel" remote="lapis" />
  <project name="android_device_lenovo_sm8650-common" path="device/lenovo/sm8650-common" remote="lapis" />
  <project name="android_kernel_lenovo_sm8650" path="kernel/lenovo/sm8650" remote="lapis" />
  <project name="proprietary_vendor_lenovo_lapis" path="vendor/lenovo/lapis" remote="lapis" />
  <project name="proprietary_vendor_lenovo_sm8650-common" path="vendor/lenovo/sm8650-common" remote="lapis" />
  <project name="android_vendor_lineage" path="vendor/lineage" remote="lapis" />
</manifest>
```

## Building

All GKI modules built from `kernel/lenovo/sm8650` go to system_dlkm and the
vendor modules are the stock prebuilts, so the kernel build of LineageOS
(`vendor/lineage/build/tasks/kernel.mk`) has no vendor_dlkm modules of its
own. The original still installs that empty set, which replaces
`modules.dep` and `modules.load` of the prebuilt vendor modules and fails on
their load list; the `vendor/lineage` fork skips it.

```bash
source build/envsetup.sh
breakfast lapis user
m bacon
```

`user` builds keep ADB authentication on and exclude debug tools, ADB root
and the OTA `addon.d` preservation path. `WITH_ADB_INSECURE=true` turns ADB
authentication off.

## Installing

Sideload the OTA package (`out/target/product/lapis/lineage-24.0-*-lapis.zip`)
from the recovery of this tree: Apply update > Apply from ADB, then
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
- `overlay-lineage/` — LineageOS SDK overlay (key handler registration).
- `health/` — QTI health HAL copy that ignores the pen charger (`wls_tx`).
- `keyhandler/` — folio cover key handler.
- `configs/` — the stock Lenovo keyboard keylayouts and input device
  configurations, vendor-only keycodes remapped to AOSP keycodes.

## Extracting blobs

```bash
./extract-files.py <dump>
```

`<dump>` is an extracted stock firmware containing `vendor/`, `odm/`,
`system_ext/` and `product/`. Not needed when the vendor repository is
synced.

## Credits
- **[LineageOS Team](https://github.com/LineageOS)** — [android_device_oneplus_caihong](https://github.com/LineageOS/android_device_oneplus_caihong), [android_kernel_oneplus_sm8650](https://github.com/LineageOS/android_kernel_oneplus_sm8650)
- **[sungwon1002](https://github.com/sungwon1002)** — [android_device_lenovo_TB710FU](https://github.com/sungwon1002/android_device_lenovo_TB710FU)

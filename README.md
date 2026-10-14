# Lenovo Yoga Tab Plus (lapis) — device tree

Unofficial device tree for the Lenovo Yoga Tab Plus / YOGA Pad Pro (model
TB520FU, codename lapis, Qualcomm Snapdragon 8 Gen 3).

| Branch | Builds |
|---|---|
| `lineage-24.0` | plain LineageOS 24 (`lineage_lapis`), with one local `vendor/lineage` change (see Building) |
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

- **Widevine L1** — Netflix HD playback.
- **HDR, HDR10+ and Dolby Vision** — HDR video playback with the device's display and media codecs.
- **Lenovo Pencil** — automatic pairing, battery and charging status, pen buttons and the Lenovo pen settings.
- **Lenovo keyboard** — touchpad controls, configurable shortcut keys, backlight and firmware updates.
- **Folio case** — close the cover to sleep and lock; open it to wake.
- **Adaptive refresh rate** — 30/60/90/120/144 Hz display modes.
- **Battery controls** — charging limits, battery protection, bypass charging and standby power saving.
- **Natural colors** — ambient white balance.
- **Memory extension** — storage-backed virtual memory with a selectable size.
- **Double tap to wake**.

HDR and Dolby Vision playback depends on the app, content and streaming subscription.

Writing haptics, the white balance strength, the keyboard desktop mode, the
PC mode tile, Dolby Atmos settings, video motion smoothing and Custom Tweaks
need framework changes or apps of the `seventeen` (PixelOS) branch.

## Working

- Display, Wi-Fi, Bluetooth and almost all other features.
- Miracast (wireless display).

## Not working

None

## Repositories

All repositories are in the [lenovo-sm8650](https://github.com/lenovo-sm8650)
organization, laid out like the LineageOS trees (device, SoC common tree, hardware, blobs,
kernel):

| Path | Repository | Contents |
|---|---|---|
| `device/lenovo/lapis` | `android_device_lenovo_lapis` | this tree |
| `device/lenovo/sm8650-common` | `android_device_lenovo_sm8650-common` | SM8650 (pineapple) platform configuration |
| `device/lenovo/lapis-kernel` | `android_device_lenovo_lapis-kernel` | stock vendor kernel modules, dtb and dtbo |
| `hardware/lenovo` | `android_hardware_lenovo` | TB520FUParts and the `tb520fu-input` system_server bridge |
| `kernel/lenovo/sm8650` | `android_kernel_lenovo_sm8650` | Android common kernel `android14-6.1` at `2ecae636cf9b` (the source of the stock GKI kernel) plus the Qualcomm UAPI headers |
| `vendor/lenovo/lapis` | `proprietary_vendor_lenovo_lapis` | device blobs (Git LFS for files over 50 MB) |
| `vendor/lenovo/sm8650-common` | `proprietary_vendor_lenovo_sm8650-common` | platform blobs |
| `vendor/lineage` | `android_vendor_lineage` | LineageOS `vendor/lineage` with the kernel build change in Building |

Every repository has a `lineage-24.0` and a `seventeen` branch;
`lineage.dependencies` lists them for roomservice. The rest of the source is
LineageOS 24 as published; the local manifest below puts the `vendor/lineage`
fork in place of the original.

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
  <project name="android_hardware_lenovo" path="hardware/lenovo" remote="lapis" />
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
authentication off. The AVB and app signing keys stay as configured below.

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
- `health/` — QTI health HAL copy that ignores the pen charger (`wls_tx`).
- `configs/idc/`, `configs/keylayout/` — the stock Lenovo pen and keyboard
  input configurations, ZUI-only keycodes remapped to AOSP keycodes.
- `lenovo/PenService/` — the stock PenService with a compat dex for APIs that
  changed in Android 17 and the Settings look of the pen settings (card groups).
- `lenovo/KeyboardUpdate/` — the stock keyboard firmware updaters with a
  compat dex that gives their page the Settings look (no resource overlays).
- `configs/displayconfig/` — display configuration of the panel.
- `system_ext.prop` — besides the stock values: `ro.config.lgsi.device.type=pad`
  (the stock Lenovo apps use the tablet dialog layout with it) and a linear
  brightness slider like stock ZUI.

In `hardware/lenovo`:

- `packages/TB520FUParts/` — "Lenovo features" in Settings > System:
  charging modes, white balance, memory extension (zram writeback),
  pen settings, folio case mode, and the physical keyboard page of the stock
  settings with the keyboard firmware update.
- `input/` — `tb520fu-input.jar`, loaded into system_server as a
  DeviceKeyHandler: Lenovo pen (attach, pairing, battery, buttons), keyboard
  keys, charging modes, double tap to wake and the folio case mode (the cover
  is the sensor HAL hall effect sensor, confirmed by the light sensor, as on
  stock), ported from the stock ZUI services (see `input/NOTICE`).

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
- **[miner7222](https://github.com/miner7222)** — [Lenovo-SM8850](https://github.com/Lenovo-SM8850/android_hardware_lenovo)
- **[sungwon1002](https://github.com/sungwon1002)** — [android_device_lenovo_TB710FU](https://github.com/sungwon1002/android_device_lenovo_TB710FU)

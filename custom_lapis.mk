#
# SPDX-FileCopyrightText: 2026 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit_only.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base.mk)

# Inherit from lapis device
$(call inherit-product, device/lenovo/lapis/device.mk)

# Inherit some common PixelOS stuff.
$(call inherit-product, vendor/custom/config/common_full_tablet_wifionly.mk)

PRODUCT_NAME := custom_lapis
PRODUCT_DEVICE := lapis
PRODUCT_MANUFACTURER := Lenovo
PRODUCT_BRAND := Lenovo
PRODUCT_MODEL := TB520FU
PRODUCT_CHARACTERISTICS := nosdcard,tablet

PRODUCT_GMS_CLIENTID_BASE := android-lenovo

PRODUCT_BUILD_PROP_OVERRIDES += \
    BuildDesc="TB520FU-user 17 CQ2A.260712.001 37 release-keys" \
    BuildFingerprint=qti/TB520FU/TB520FU:17/CQ2A.260712.001/ZUI_18.5.10.037_260929_ROW:user/release-keys \
    DeviceName=TB520FU \
    DeviceProduct=TB520FU \
    SystemDevice=TB520FU \
    SystemName=TB520FU

# Keep the stock ZUI fingerprint set above; Play Integrity Fix spoofs a Pixel
# profile per process instead of overriding the global build fingerprint.
$(call inherit-product-if-exists, hardware/lenovo/custom/custom.mk)

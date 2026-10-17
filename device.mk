#
# SPDX-FileCopyrightText: 2026 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

TARGET_IS_TABLET := true

# AAPT
PRODUCT_AAPT_CONFIG := normal
PRODUCT_AAPT_PREF_CONFIG := xhdpi

# Boot animation
TARGET_SCREEN_HEIGHT := 1840
TARGET_SCREEN_WIDTH := 2944

# Camera
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.camera.concurrent.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.camera.concurrent.xml \
    frameworks/native/data/etc/android.hardware.camera.flash-autofocus.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.camera.flash-autofocus.xml \
    frameworks/native/data/etc/android.hardware.camera.front.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.camera.front.xml \
    frameworks/native/data/etc/android.hardware.camera.full.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.camera.full.xml \
    frameworks/native/data/etc/android.hardware.camera.raw.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.camera.raw.xml

# Carrier
PRODUCT_VENDOR_PROPERTIES += \
    ro.carrier=wifi-only

# Dalvik
$(call inherit-product, frameworks/native/build/tablet-10in-xhdpi-2048-dalvik-heap.mk)

# Video motion smoothing (Qualcomm VPP frame interpolation, per app;
# hardware/lenovo/packages/VideoMotion and frameworks/av)
PRODUCT_PACKAGES += \
    VideoMotion

# Dolby Atmos UI: LunarisDolby (hardware/lenovo/packages/LunarisDolby),
# set up for the stock DAX3 blobs and tuning of this tablet
PRODUCT_PACKAGES += \
    LunarisDolby

# Device settings (hardware/lenovo/packages/LenovoParts)
PRODUCT_PACKAGES += \
    LenovoParts

# WiFi Display (Miracast): the QTI session of the OnePlus QSSI blobs
# (proprietary-files.txt) with the QTI display config clients built from source
PRODUCT_PACKAGES += \
    libdisplayconfig.system.qti \
    vendor.display.config@2.0 \
    vendor.qti.hardware.display.config-V5-ndk \
    vendor.qti.hardware.display.config-V16-ndk

PRODUCT_SYSTEM_EXT_PROPERTIES += \
    persist.debug.wfd.enable=1 \
    persist.sys.wfd.virtual=0

# Dolby Vision service app with the stock com.dolby.vision_play broadcast
# (lenovo/DolbyVisionService)
PRODUCT_PACKAGES += \
    DolbyVisionService

# Lenovo pen / keyboard / battery bridge (hardware/lenovo/input)
PRODUCT_PACKAGES += \
    PenService \
    lenovo-input \
    ZuiKeyboardUpdate \
    ZuiKeyboardUpdateOlympia

# Stock Lenovo pen/keyboard keylayouts, ZUI-only keycodes remapped
# (convert_keylayouts.py)
PRODUCT_COPY_FILES += \
    $(call find-copy-subdir-files,*.kl,device/lenovo/lapis/configs/keylayout,$(TARGET_COPY_OUT_VENDOR)/usr/keylayout) \
    $(call find-copy-subdir-files,*.idc,device/lenovo/lapis/configs/idc,$(TARGET_COPY_OUT_VENDOR)/usr/idc)

# Display configuration of the stock firmware: the panel has no display_id_<id>.xml (the
# stock vendor one is for the Qualcomm reference panel), so the display service takes the
# brightness arrays of config.xml and the high brightness mode data of display_hdr.xml
# (stock DisplayDeviceConfig.zuiLoadHighBrightnessModeData)
PRODUCT_COPY_FILES += \
    device/lenovo/lapis/configs/displayconfig-system/display_hdr.xml:$(TARGET_COPY_OUT_SYSTEM)/etc/displayconfig/display_hdr.xml

# Fingerprint (Goodix)
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.fingerprint.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.fingerprint.xml

# Health
PRODUCT_PACKAGES += \
    android.hardware.health-service.lenovo \
    android.hardware.health-service.lenovo_recovery

# Init
PRODUCT_PACKAGES += \
    fstab.zram.0 \
    fstab.zram.2 \
    fstab.zram.4 \
    fstab.zram.6 \
    fstab.zram.8 \
    fstab.zram.12 \
    fstab.zram.16 \
    init.lenovo.zram.rc \
    init.lenovo.region.rc \
    init.lenovo.region.sh \
    setwlansardsi0.sh \
    setwlansardsi1.sh \
    setwlansardsi2.sh \
    setwlansardsi3.sh \
    setwlansardsi4.sh

# Kernel
PRODUCT_COPY_FILES += \
    device/lenovo/lapis-kernel/modules/vendor_dlkm/system_dlkm.modules.blocklist:$(TARGET_COPY_OUT_VENDOR_DLKM)/lib/modules/system_dlkm.modules.blocklist

# Overlays
PRODUCT_ENFORCE_RRO_TARGETS := *
PRODUCT_PACKAGES += \
    FrameworksResTarget \
    PenServiceResTarget \
    SettingsProviderResTarget \
    SettingsResTarget \
    SystemUIResTarget \
    WifiResTarget \
    ZuiUDeviceResTarget

# The NT36532 touch IC is flashless: nvt_36xxx downloads this firmware 5 s
# after probe, so recovery needs it in its own /vendor/firmware.
PRODUCT_COPY_FILES += \
    $(foreach p,boe tianma,vendor/lenovo/lapis/proprietary/vendor/firmware/novatek_ts_$(p)_fw.bin:$(TARGET_COPY_OUT_RECOVERY)/root/vendor/firmware/novatek_ts_$(p)_fw.bin)

# ADSP loader modules for recovery (battery/charger). The destination has no
# lib/ component: PRODUCT_COPY_FILES rejects ELF files under bin/ and lib/.
RECOVERY_ADSP_MODULES := \
    q6_pdr_dlkm \
    q6_notifier_dlkm \
    snd_event_dlkm \
    gpr_dlkm \
    spf_core_dlkm \
    adsp_loader_dlkm

PRODUCT_COPY_FILES += \
    $(foreach m,$(RECOVERY_ADSP_MODULES),device/lenovo/lapis-kernel/modules/vendor_dlkm/$(m).ko:$(TARGET_COPY_OUT_RECOVERY)/root/vendor/modules/$(m).ko)

# Sensors
PRODUCT_PACKAGES += \
    android.hardware.sensors-service.multihal \
    sensors.dynamic_sensor_hal

PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.sensor.accelerometer.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/sku_pineapple/android.hardware.sensor.accelerometer.xml \
    frameworks/native/data/etc/android.hardware.sensor.dynamic.head_tracker.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/sku_pineapple/android.hardware.sensor.dynamic.head_tracker.xml \
    frameworks/native/data/etc/android.hardware.sensor.gyroscope.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/sku_pineapple/android.hardware.sensor.gyroscope.xml \
    frameworks/native/data/etc/android.hardware.sensor.light.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/sku_pineapple/android.hardware.sensor.light.xml \
    frameworks/native/data/etc/android.hardware.sensor.stepcounter.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/sku_pineapple/android.hardware.sensor.stepcounter.xml \
    frameworks/native/data/etc/android.hardware.sensor.stepdetector.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/sku_pineapple/android.hardware.sensor.stepdetector.xml

# Shipping API
BOARD_SHIPPING_API_LEVEL := 34
PRODUCT_SHIPPING_API_LEVEL := $(BOARD_SHIPPING_API_LEVEL)

# Soong namespaces
PRODUCT_SOONG_NAMESPACES += \
    device/lenovo/lapis \
    device/lenovo/lapis/lenovo/DolbyVisionService \
    device/lenovo/lapis/lenovo/KeyboardUpdate \
    device/lenovo/lapis/lenovo/PenService \
    hardware/lenovo

# VINTF
DEVICE_FRAMEWORK_COMPATIBILITY_MATRIX_FILE += device/lenovo/lapis/vintf/device_framework_matrix.xml

# Inherit from the common SM8650 configuration.
$(call inherit-product, device/lenovo/sm8650-common/common.mk)

# Inherit from the proprietary files makefile.
$(call inherit-product, vendor/lenovo/lapis/lapis-vendor.mk)

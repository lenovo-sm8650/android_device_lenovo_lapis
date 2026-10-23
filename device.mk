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

# Dolby Vision playback service (stock app, proprietary-files.txt)
PRODUCT_PACKAGES += \
    DolbyVisionService

# Folio cover (hall effect sensor): sleep when closed, wake when opened
PRODUCT_PACKAGES += \
    lapis-keyhandler

# Stock Lenovo keyboard keylayouts and input device configurations
PRODUCT_COPY_FILES += \
    $(call find-copy-subdir-files,*.kl,device/lenovo/lapis/configs/keylayout,$(TARGET_COPY_OUT_VENDOR)/usr/keylayout) \
    $(call find-copy-subdir-files,*.idc,device/lenovo/lapis/configs/idc,$(TARGET_COPY_OUT_VENDOR)/usr/idc)

# Fingerprint (Goodix)
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.fingerprint.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.fingerprint.xml

# Health
PRODUCT_PACKAGES += \
    android.hardware.health-service.lenovo \
    android.hardware.health-service.lenovo_recovery

# Lineage Health: charging control through charging_enable (1 = charge,
# 0 = stop charging; the charger keeps powering the device, i.e. bypass)
PRODUCT_PACKAGES += \
    vendor.lineage.health-service.default

$(call soong_config_set,lineage_health,charging_control_charging_path,/sys/class/power_supply/battery/charging_enable)
$(call soong_config_set,lineage_health,charging_control_charging_enabled,1)
$(call soong_config_set,lineage_health,charging_control_charging_disabled,0)
$(call soong_config_set_bool,lineage_health,charging_control_supports_bypass,true)

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
    setwlansardsi0.sh \
    setwlansardsi1.sh \
    setwlansardsi2.sh \
    setwlansardsi3.sh \
    setwlansardsi4.sh \
    ueventd.lapis.rc

# Kernel
PRODUCT_COPY_FILES += \
    device/lenovo/lapis-kernel/modules/vendor_dlkm/system_dlkm.modules.blocklist:$(TARGET_COPY_OUT_VENDOR_DLKM)/lib/modules/system_dlkm.modules.blocklist

# Overlays
DEVICE_PACKAGE_OVERLAYS += \
    $(LOCAL_PATH)/overlay-lineage

PRODUCT_ENFORCE_RRO_TARGETS := *
PRODUCT_PACKAGES += \
    FrameworksResTarget \
    SettingsProviderResTarget \
    SettingsResTarget \
    WifiResTarget

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
    device/lenovo/lapis

# VINTF
DEVICE_FRAMEWORK_COMPATIBILITY_MATRIX_FILE += device/lenovo/lapis/vintf/device_framework_matrix.xml

# Inherit from the common SM8650 configuration.
$(call inherit-product, device/lenovo/sm8650-common/common.mk)

# Inherit from the proprietary files makefile.
$(call inherit-product, vendor/lenovo/lapis/lapis-vendor.mk)

# Display configuration of the panel
PRODUCT_COPY_FILES += \
    $(call find-copy-subdir-files,*.xml,device/lenovo/lapis/configs/displayconfig,$(TARGET_COPY_OUT_VENDOR)/etc/displayconfig)

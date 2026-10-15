#
# SPDX-FileCopyrightText: 2026 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

DEVICE_PATH := device/lenovo/lapis
KERNEL_PATH := device/lenovo/lapis-kernel

# Inherit from the common SM8650 board configuration.
include device/lenovo/sm8650-common/BoardConfigCommon.mk

# Assert
TARGET_OTA_ASSERT_DEVICE := lapis,TB520FU

# Display
TARGET_SCREEN_DENSITY := 340

# DTB / DTBO
# The dtb comes from the installed vendor_boot, not the ROW firmware: LTBox
# rewrote its region string (ROW -> PRC) so it matches the PRC board.
BOARD_INCLUDE_DTB_IN_BOOTIMG := true
BOARD_PREBUILT_DTBIMAGE_DIR := $(KERNEL_PATH)/dtb
BOARD_PREBUILT_DTBOIMAGE := $(KERNEL_PATH)/dtbo.img

# Kernel modules
KERNEL_MODULES_DIR := $(KERNEL_PATH)/modules

BOARD_VENDOR_RAMDISK_KERNEL_MODULES := $(wildcard $(KERNEL_MODULES_DIR)/vendor_boot/*.ko)
BOARD_VENDOR_RAMDISK_KERNEL_MODULES_BLOCKLIST_FILE := $(KERNEL_MODULES_DIR)/vendor_boot/modules.blocklist
BOARD_VENDOR_RAMDISK_KERNEL_MODULES_LOAD := $(strip $(shell cat $(KERNEL_MODULES_DIR)/vendor_boot/modules.load))
BOARD_VENDOR_RAMDISK_RECOVERY_KERNEL_MODULES_LOAD := $(strip $(shell cat $(KERNEL_MODULES_DIR)/vendor_boot/modules.load.recovery))

BOARD_VENDOR_KERNEL_MODULES := $(wildcard $(KERNEL_MODULES_DIR)/vendor_dlkm/*.ko)
BOARD_VENDOR_KERNEL_MODULES_BLOCKLIST_FILE := $(KERNEL_MODULES_DIR)/vendor_dlkm/modules.blocklist
BOARD_VENDOR_KERNEL_MODULES_LOAD := $(strip $(shell cat $(KERNEL_MODULES_DIR)/vendor_dlkm/modules.load))

# Properties
TARGET_PRODUCT_PROP += $(DEVICE_PATH)/product.prop
TARGET_SYSTEM_EXT_PROP += $(DEVICE_PATH)/system_ext.prop
TARGET_VENDOR_PROP += $(DEVICE_PATH)/vendor.prop

# Recovery
# The panel is natively landscape but the Novatek touch reports portrait
# coordinates (the -twrp tree swaps X/Y and flips Y, which is this rotation).
TARGET_RECOVERY_DEFAULT_TOUCH_ROTATION := ROTATION_RIGHT

# Security
BOOT_SECURITY_PATCH := 2026-07-05
VENDOR_SECURITY_PATCH := $(BOOT_SECURITY_PATCH)

# SEPolicy
BOARD_VENDOR_SEPOLICY_DIRS += $(DEVICE_PATH)/sepolicy/vendor
SYSTEM_EXT_PRIVATE_SEPOLICY_DIRS += $(DEVICE_PATH)/sepolicy/private

# Verified Boot
# pvmfw is a prebuilt (radio/, proprietary-firmware.txt) with its own testkey
# footer; the bootloader looks up its descriptor in the vbmeta chain.
BOARD_AVB_MAKE_VBMETA_IMAGE_ARGS += --include_descriptors_from_image vendor/lenovo/lapis/radio/pvmfw.img

# Include the proprietary files BoardConfig.
include vendor/lenovo/lapis/BoardConfigVendor.mk


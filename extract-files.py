#!/usr/bin/env -S PYTHONPATH=../../../tools/extract-utils python3
#
# SPDX-FileCopyrightText: 2026 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

from extract_utils.fixups_blob import (
    blob_fixup,
    blob_fixups_user_type,
)
from extract_utils.fixups_lib import (
    lib_fixups,
    lib_fixups_user_type,
)
from extract_utils.main import (
    ExtractUtils,
    ExtractUtilsModule,
)

namespace_imports = [
    'device/lenovo/lapis',
    'vendor/lenovo/sm8650-common',
    'hardware/qcom-caf/sm8650',
    'hardware/qcom-caf/wlan',
    'vendor/qcom/opensource/commonsys/display',
    'vendor/qcom/opensource/commonsys-intf/display',
    'vendor/qcom/opensource/dataservices',
]


def lib_fixup_vendor_suffix(lib: str, partition: str, *args, **kwargs):
    return f'{lib}_{partition}' if partition == 'vendor' else None


lib_fixups: lib_fixups_user_type = {
    **lib_fixups,
    (
        'com.qualcomm.qti.dpm.api@1.0',
        'vendor.qti.diaghal@1.0',
        'vendor.qti.hardware.dpmaidlservice-V1-ndk',
        'vendor.qti.hardware.dpmservice@1.0',
        'vendor.qti.hardware.qccsyshal@1.0',
        'vendor.qti.hardware.qccsyshal@1.1',
        'vendor.qti.hardware.qccsyshal@1.2',
        'vendor.qti.hardware.wifidisplaysession@1.0',
        'vendor.qti.qccvndhal_aidl-V1-ndk',
    ): lib_fixup_vendor_suffix,
}


CODEC2_V34_LIBS = (
    'android.hardware.media.c2@1.0',
    'android.hardware.media.c2@1.1',
    'android.hardware.media.c2@1.2',
    'libcodec2',
    'libcodec2_hidl@1.0',
    'libcodec2_hidl@1.1',
    'libcodec2_hidl@1.2',
    'libcodec2_hidl_plugin',
    'libcodec2_soft_common',
    'libcodec2_vndk',
    'libsfplugin_ccodec_utils',
    'libstagefright_aidl_bufferpool2',
    'libstagefright_bufferpool@2.0.1',
    'libstagefright_bufferqueue_helper',
    'libstagefright_foundation',
    'libui',
)


def codec2_v34(fixup):
    # The stock Dolby Codec2 blobs use the stock Codec2 libraries,
    # shipped as <name>-v34.so next to the ones built from source. They were
    # built against the libui of Android 14 (libui-v34, from hardware/lineage/
    # compat): the layout of GraphicBuffer differs from the one built from
    # source, which crashed the Dolby Vision decoder.
    for lib in CODEC2_V34_LIBS:
        fixup = fixup.replace_needed(f'{lib}.so', f'{lib}-v34.so')
    return fixup


def clear_opencl_versions(fixup):
    # Lenovo camera algorithm libs reference OpenCL symbols with OPENCL_x.y
    # versions, but the Adreno libOpenCL exports them unversioned.
    for symbol in OPENCL_SYMBOLS:
        fixup = fixup.clear_symbol_version(symbol)
    return fixup


def clear_nativewindow_versions(fixup):
    # AHardwareBuffer symbols are referenced as @LIBNATIVEWINDOW, but the
    # vendor libnativewindow stub exports them unversioned.
    for symbol in NATIVEWINDOW_SYMBOLS:
        fixup = fixup.clear_symbol_version(symbol)
    return fixup


NATIVEWINDOW_SYMBOLS = (
    'AHardwareBuffer_allocate',
    'AHardwareBuffer_describe',
    'AHardwareBuffer_lock',
    'AHardwareBuffer_lockPlanes',
    'AHardwareBuffer_release',
    'AHardwareBuffer_unlock',
)


OPENCL_SYMBOLS = (
    'clBuildProgram',
    'clCreateBuffer',
    'clCreateCommandQueue',
    'clCreateCommandQueueWithProperties',
    'clCreateContext',
    'clCreateKernel',
    'clCreateProgramWithBinary',
    'clCreateProgramWithSource',
    'clEnqueueCopyBuffer',
    'clEnqueueFillBuffer',
    'clEnqueueMapBuffer',
    'clEnqueueNDRangeKernel',
    'clEnqueueReadBuffer',
    'clEnqueueUnmapMemObject',
    'clFinish',
    'clGetDeviceIDs',
    'clGetDeviceInfo',
    'clGetExtensionFunctionAddressForPlatform',
    'clGetPlatformIDs',
    'clGetPlatformInfo',
    'clGetProgramBuildInfo',
    'clGetProgramInfo',
    'clReleaseCommandQueue',
    'clReleaseContext',
    'clReleaseKernel',
    'clReleaseMemObject',
    'clReleaseProgram',
    'clSetKernelArg',
)

blob_fixups: blob_fixups_user_type = {
    # WiFi Display (OnePlus QSSI of Android 16, see proprietary-files.txt)
    'system_ext/lib64/libwfdcommonutils.so': blob_fixup()
        .remove_needed('libheif.so'),
    'system_ext/lib64/libwfdmmsrc_system.so': blob_fixup()
        .add_needed('libgui_shim.so'),
    'system_ext/lib64/libwfdnative.so': blob_fixup()
        .add_needed('libinput_shim.so'),
    'system_ext/lib64/libwfdservice.so': blob_fixup()
        .replace_needed('android.media.audio.common.types-V4-cpp.so', 'android.media.audio.common.types-V5-cpp.so'),
    (
        'vendor/bin/hw/vendor.dolby.media.c2@1.0-service-vision',
        'vendor/lib64/c2.dolby.client.so',
        'vendor/lib64/c2.dolby.hevc.dec.so',
        'vendor/lib64/c2.dolby.hevc.sec.dec.so',
        'vendor/lib64/c2.dolby.store.so',
        'vendor/lib64/libdolbydecoderprocessor.so',
        'vendor/lib64/libdolbyeglcore.so',
        'vendor/bin/hw/vendor.dolby.media.c2-default-service-dax',
        'vendor/lib64/libcodec2_soft_ac4dec.so',
        'vendor/lib64/libcodec2_soft_ddpdec.so',
        'vendor/lib64/libcodec2_store_dolby.so',
    ): codec2_v34(blob_fixup()),
    (
        'vendor/lib64/android.hardware.media.c2@1.0-v34.so',
        'vendor/lib64/android.hardware.media.c2@1.1-v34.so',
        'vendor/lib64/android.hardware.media.c2@1.2-v34.so',
        'vendor/lib64/libcodec2-v34.so',
        'vendor/lib64/libcodec2_hidl@1.0-v34.so',
        'vendor/lib64/libcodec2_hidl@1.1-v34.so',
        'vendor/lib64/libcodec2_hidl@1.2-v34.so',
        'vendor/lib64/libcodec2_hidl_plugin-v34.so',
        'vendor/lib64/libcodec2_soft_common-v34.so',
        'vendor/lib64/libcodec2_vndk-v34.so',
        'vendor/lib64/libsfplugin_ccodec_utils-v34.so',
        'vendor/lib64/libstagefright_aidl_bufferpool2-v34.so',
        'vendor/lib64/libstagefright_bufferpool@2.0.1-v34.so',
        'vendor/lib64/libstagefright_bufferqueue_helper-v34.so',
        'vendor/lib64/libstagefright_foundation-v34.so',
    ): codec2_v34(blob_fixup().fix_soname()),
    (
        'vendor/lib64/libpandora.algorithm.arcsoft.superportraitvideo.1.so',
        'vendor/lib64/libpandora.algorithm.arcsoft.tablethdrchecker.1.so',
        'vendor/lib64/libpandora.algorithm.arcsoft.tabletnormalhdr.1.so',
        'vendor/lib64/libpandora.algorithm.arcsoft.tabletportraithdr.1.so',
        'vendor/lib64/libpandora.algorithm.bst.filter.1.so',
        'vendor/lib64/libpandora.algorithm.bst.tabletaicapture.1.so',
        'vendor/lib64/libpandora.algorithm.bst.tabletsinglebokehcapture.1.so',
        'vendor/lib64/libpandora.algorithm.camera.AlgorithmsDev.1.so',
        'vendor/lib64/libpandora.algorithm.fotonation.facebeauty.1.so',
        'vendor/lib64/libpandora.algorithm.lenovo.Watermark.1.so',
        'vendor/lib64/libpandora.algorithm.morpho.tabletdccapture.1.so',
        'vendor/lib64/libpandora.algorithm.morpho.tabletdcvideo.1.so',
        'vendor/lib64/libpandora.algorithm.morpho.tabletdenoise.1.so',
        'vendor/lib64/libpandora.algorithm.morpho.tabletsr.1.so',
        'vendor/lib64/libpandora.algorithm.niklas.BmpWatermark.0.so',
        'vendor/lib64/libpandora.algorithm.niklas.YUVRotator.0.so',
        'vendor/lib64/libpandora.algorithm.niklas.YUVScaler.0.so',
        'vendor/lib64/libpandora.so',
    ): clear_opencl_versions(blob_fixup()),
    (
        'vendor/lib64/libFNVfbEngineHAL.so',
        'vendor/lib64/libarcsoft_video_superportrait.so',
    ): clear_nativewindow_versions(blob_fixup()),
    'vendor/lib64/hw/gf_fingerprint.default.so': blob_fixup()
        .fix_soname(),
}  # fmt: skip

module = ExtractUtilsModule(
    'lapis',
    'lenovo',
    blob_fixups=blob_fixups,
    lib_fixups=lib_fixups,
    namespace_imports=namespace_imports,
    add_firmware_proprietary_file=True,
)

if __name__ == '__main__':
    utils = ExtractUtils.device_with_common(
        module, 'sm8650-common', module.vendor
    )
    utils.run()

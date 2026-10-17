/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.lenovo.pen.cap.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.lenovo.pen.cap.R;
import com.lenovo.pen.common.Logger;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Replacement of the stock Kotlin object com.lenovo.pen.cap.util.DeviceUtils (ZUI 18.5.10.037),
 * identical except isZui().
 *
 * Stock isZui() is ro.config.lgsi.ui.type == "ZUI". That property is kept out of our build on
 * purpose (other apps would switch to their ZUI paths), so the stock check failed and the pen name
 * row only copied the model to the clipboard instead of opening the Bluetooth device profile
 * (rename, forget). The only users are BtPrefName.onClick/update, and the profile screen is the
 * Settings BLUETOOTH_DEVICE_DETAIL_SETTINGS activity on this ROM.
 */
public final class DeviceUtils {
    public static final String DEVICE_MODEL_INCEPTION_LTE = "TB570ZU";
    public static final String DEVICE_MODEL_INCEPTION_WIFI = "TB570FU";
    public static final String DEVICE_MODEL_LAPIS = "TB520FU";
    public static final String DEVICE_MODEL_LINDEN = "TB360ZU";
    public static final String DEVICE_MODEL_OLIVINE = "TB138FC";
    public static final String DEVICE_MODEL_ONYX_WIFI = "TB132FU";
    public static final String DEVICE_MODEL_P12_LTE = "Lenovo TB-Q706Z";
    public static final String DEVICE_MODEL_P12_NEC = "LAVIE Tab T12 12QHD1";
    public static final String DEVICE_MODEL_P12_WIFI = "Lenovo TB-Q706F";
    public static final String DEVICE_MODEL_SPINEL = "TB371FC";
    public static final String DEVICE_MODEL_TITANS = "TB571FU";
    public static final DeviceUtils INSTANCE = new DeviceUtils();

    private static final ArrayList<String> SUPPORT_CAP_DEVICE_MODELS = new ArrayList<>(
            Arrays.asList("Lenovo TB-J606F", "Lenovo TB-J606N", "Lenovo TB-J706F",
                    "Lenovo TB-J706N", "Lenovo TB-J716F", "Lenovo TB-J607F", "Lenovo TB-J607Z",
                    "Lenovo TB-9707F", "Lenovo YT-K606F"));

    private DeviceUtils() {}

    public static int getDefaultPenTouchFilmPressure() {
        return 5;
    }

    public final String getVersionName(Context context) {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            Logger.e$default("getVersionName :: error = " + e.getMessage(), false, 2, null);
            return "";
        }
    }

    public static String getVersionName(Context context, String packageName) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (Exception e) {
            Logger.e$default("getVersionName :: error = " + e.getMessage(), false, 2, null);
            return "null";
        }
    }

    public final int getVersionCode(Context context) {
        if (context == null) {
            return -1;
        }
        try {
            if (context.getPackageManager() != null) {
                return context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0).versionCode;
            }
            return -1;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e$default("getVersionCode :: error = " + e.getMessage(), false, 2, null);
            return -1;
        }
    }

    public final long getVersionCode(Context context, String pkg) {
        try {
            return context.getPackageManager().getPackageInfo(pkg, 0).getLongVersionCode();
        } catch (PackageManager.NameNotFoundException e) {
            return -1L;
        }
    }

    public static boolean isModeNight(Context context) {
        return (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isPackageEnabled(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, 0).enabled;
        } catch (Exception e) {
            Logger.e$default("Error while retrieving application info for package "
                    + packageName + ", " + e, false, 2, null);
            return false;
        }
    }

    public static String getSerial() {
        try {
            return Build.getSerial();
        } catch (SecurityException e) {
            e.printStackTrace();
            return "";
        }
    }

    public final void printScreenInfo(Activity activity) {
        Configuration config = activity.getResources().getConfiguration();
        Logger.d$default("printScreenInfo :: " + config, false, 2, null);
        int widthDp = config.screenWidthDp;
        int heightDp = config.screenHeightDp;
        String orientation;
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientation = "port";
        } else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            orientation = "land";
        } else {
            orientation = "";
        }
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        Logger.d$default("printScreenInfo :: Physical size: w" + metrics.widthPixels + "xh"
                + metrics.heightPixels, false, 2, null);
        Logger.d$default(String.format(
                "printScreenInfo :: Current screen: values-w%1$ddp-h%2$ddp-%3$s, inMultiWindowMode = %4$s",
                widthDp, heightDp, orientation, activity.isInMultiWindowMode()), false, 2, null);
    }

    public static boolean isPreinstallZuiNotesByOta() {
        return (Arrays.asList(DEVICE_MODEL_INCEPTION_LTE, DEVICE_MODEL_INCEPTION_WIFI)
                .contains(Build.MODEL) && SystemProps.isRow())
                || Arrays.asList(DEVICE_MODEL_LINDEN).contains(Build.MODEL);
    }

    public static boolean isZui() {
        return true;
    }

    public static void startFreeWriteInSystemSettings(Context context) {
        try {
            context.startActivity(createSettingsIntent(
                    "com.lenovo.pen.bt.ui.freewrite.FreeWriteActivity"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startPenSettingsInSystemSettings(Context context) {
        try {
            Intent intent = createSettingsIntent("com.lenovo.pen.bt.ui.BtSettingsActivity");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Intent createSettingsIntent(String cls) {
        Intent intent = new Intent("com.android.settings.PLACE_HOLDER");
        intent.setPackage("com.android.settings");
        intent.putExtra(":settings:place_holder_package_name", "com.lenovo.penservice");
        intent.putExtra(":settings:place_holder_class_name", cls);
        intent.putExtra(":settings:place_holder_highlight_menu_key", "top_level_pen");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static int getDefaultPenHapticLevel() {
        return Arrays.asList(DEVICE_MODEL_TITANS).contains(Build.MODEL) ? 5 : 3;
    }

    public final boolean isWifiOnly(Context context) {
        return context != null && !context.getApplicationContext()
                .getSystemService(TelephonyManager.class).isDataCapable();
    }

    public final boolean isInception() {
        return SystemProperties.get("ro.product.model").contains("TB570");
    }

    public final boolean isPrcVersion() {
        String region = SystemProperties.get("ro.config.lgsi.region");
        if (!TextUtils.isEmpty(region)) {
            return region.equalsIgnoreCase("PRC");
        }
        return SystemProperties.get("ro.config.zui.region").equalsIgnoreCase("PRC");
    }

    public final boolean isPadCava() {
        return SystemProperties.get("ro.config.lgsi.project").equals("cava");
    }

    public final boolean isSupportTether(Context context) {
        if (!isWifiOnly(context)) {
            return true;
        }
        if (isWifiOnly(context) && isInception() && isPrcVersion()) {
            return true;
        }
        boolean show = ZuiConfig.getBoolean(context, R.bool.zui_config_show_top_level_tether);
        if ((isWifiOnly(context) && isPrcVersion()) || (isPadCava() && SystemProps.isRow())) {
            return show;
        }
        return false;
    }
}

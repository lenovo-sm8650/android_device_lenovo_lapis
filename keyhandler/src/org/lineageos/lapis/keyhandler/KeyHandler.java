/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.lapis.keyhandler;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

import com.android.internal.os.DeviceKeyHandler;

/**
 * Folio cover: puts the device to sleep when the cover is closed and wakes it
 * when the cover is opened.
 *
 * The cover magnet is reported by the wake-up hall effect sensor of the sensor
 * HAL (qti.sensor.hall_effect). The hall_irq input device only advertises
 * KEY 0x2ee-0x2f1 and never reports them: its two hall sensors serve the pen
 * charger. A magnet near the back of the tablet (keyboard folded behind it)
 * also trips the hall sensor, so a closed event is confirmed by the light
 * sensor reading dark before the device goes to sleep.
 */
public class KeyHandler implements DeviceKeyHandler {
    private static final String TAG = "LapisFolio";

    private static final int TYPE_HALL_EFFECT = 33171002;
    private static final float COVERED_MAX_LUX = 20f;

    private final PowerManager mPowerManager;
    private final SensorManager mSensorManager;
    private final Sensor mLightSensor;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean mLightRegistered;
    // The cover was closed on a dark screen, so opening it wakes the device
    private boolean mWakeOnOpen;

    private final SensorEventListener mHallListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // 1: cover away (open), anything else: cover near (closed)
            onCover((int) event.values[0] != 1);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private final SensorEventListener mLightListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.values[0] <= COVERED_MAX_LUX) {
                unregisterLight();
                sleepIfInteractive();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    public KeyHandler(Context context) {
        mPowerManager = context.getSystemService(PowerManager.class);
        mSensorManager = context.getSystemService(SensorManager.class);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // The wake-up variant also reports while the screen is off
        final Sensor hall = mSensorManager.getDefaultSensor(TYPE_HALL_EFFECT, true);
        if (hall == null) {
            Log.w(TAG, "No hall effect sensor, folio cover disabled");
            return;
        }
        mSensorManager.registerListener(mHallListener, hall, SensorManager.SENSOR_DELAY_NORMAL,
                mHandler);
    }

    @Override
    public KeyEvent handleKeyEvent(KeyEvent event) {
        return event;
    }

    private void onCover(boolean closed) {
        if (!closed) {
            unregisterLight();
            if (mWakeOnOpen && !mPowerManager.isInteractive()) {
                mPowerManager.wakeUp(SystemClock.uptimeMillis(), PowerManager.WAKE_REASON_LID,
                        "lapis:folio");
            }
            mWakeOnOpen = false;
        } else if (!mPowerManager.isInteractive()) {
            // Closed on a screen that is already off: opening wakes it
            mWakeOnOpen = true;
        } else if (mLightSensor == null) {
            sleepIfInteractive();
        } else if (!mLightRegistered) {
            mLightRegistered = mSensorManager.registerListener(mLightListener, mLightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL, mHandler);
        }
    }

    private void sleepIfInteractive() {
        if (mPowerManager.isInteractive()) {
            mWakeOnOpen = true;
            mPowerManager.goToSleep(SystemClock.uptimeMillis(),
                    PowerManager.GO_TO_SLEEP_REASON_LID_SWITCH, 0);
        }
    }

    private void unregisterLight() {
        if (mLightRegistered) {
            mSensorManager.unregisterListener(mLightListener);
            mLightRegistered = false;
        }
    }
}

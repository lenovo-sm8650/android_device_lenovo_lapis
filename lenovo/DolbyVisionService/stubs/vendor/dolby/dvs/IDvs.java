/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package vendor.dolby.dvs;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/** Compile-time stub of the AIDL interface in the stock DolbyVisionService. */
public interface IDvs extends IInterface {
    void registerCallback(IDvsCallback callback) throws RemoteException;

    void setParameters(String parameters) throws RemoteException;

    String getParameters(String keys) throws RemoteException;

    abstract class Stub extends Binder implements IDvs {
        public static IDvs asInterface(IBinder binder) {
            throw new RuntimeException("stub");
        }
    }
}

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
public interface IDvsCallback extends IInterface {
    String getInterfaceHash() throws RemoteException;

    int getInterfaceVersion() throws RemoteException;

    boolean onDolbyVisionPlaybackStart() throws RemoteException;

    boolean onDolbyVisionPlaybackStop() throws RemoteException;

    abstract class Stub extends Binder implements IDvsCallback {
        @Override
        public IBinder asBinder() {
            return this;
        }
    }
}

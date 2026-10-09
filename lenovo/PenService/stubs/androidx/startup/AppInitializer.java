/* SPDX-License-Identifier: Apache-2.0 */
package androidx.startup;

/** Compile-time stand-in; the implementation remains in the stock APK. */
public final class AppInitializer {
    public static AppInitializer getInstance(android.content.Context context) { return null; }
    void discoverAndInitialize() {}
}

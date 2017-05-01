/**
 * Copyright Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feedhenry.sdk;

import android.content.Context;
import com.feedhenry.sdk.utils.FHLog;

public class Device {

    private static String mDeviceId;
    private static String mDeviceName;
    private static String mUserAgent;

    private static final String MANUFACTURER_FIELD = "MANUFACTURER";
    private static final String USER_AGENT_TEMP = "Android %s; %s";

    private static final String LOG_TAG = "com.feedhenry.sdk.Device";

    /**
     * Gets the unique device ID.
     *
     * @param context Application context
     * @return the unique device id
     */
    public static String getDeviceId(Context context) {
        if (mDeviceId == null) {
            mDeviceId = android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        }
        return mDeviceId;
    }

    /**
     * Gets the model and manufacturer (if possible) of the device.
     *
     * @return the model and manufacturer (if found) of the device
     */
    public static String getDeviceModelAndManufacturer() {
        if (mDeviceName == null) {
            String model = android.os.Build.MODEL;
            String deviceName = model;
            String manufacturer = android.os.Build.MANUFACTURER;
            if (manufacturer != null && !manufacturer.isEmpty()) {
                deviceName = manufacturer + ' ' + model;
            } else {
                FHLog.w(LOG_TAG, "Could not retrieve device manufacturer info");
            }
            mDeviceName = deviceName;
        }
        return mDeviceName;
    }

    /**
     * Gets the custom user agent string.
     *
     * @return the custom user agent string
     */
    public static String getUserAgent() {
        if (mUserAgent == null) {
            mUserAgent = String.format(USER_AGENT_TEMP, android.os.Build.VERSION.RELEASE, getDeviceModelAndManufacturer());
            FHLog.d(LOG_TAG, "UA = " + mUserAgent);
        }
        return mUserAgent;
    }
}

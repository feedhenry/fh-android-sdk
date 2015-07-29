/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.tests.api;

import android.test.AndroidTestCase;
import com.feedhenry.sdk.Device;

public class DeviceTest extends AndroidTestCase {
    public void testDevice() {
        assertNotNull(Device.getDeviceId(getContext()));
        assertNotNull(Device.getDeviceModelAndManufacturer());
        assertNotNull(Device.getUserAgent());
    }
}

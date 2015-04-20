package com.feedhenry.sdk.tests.api;

import android.test.AndroidTestCase;
import com.feedhenry.sdk.Device;

public class DeviceTest extends AndroidTestCase {
  public void testDevice() {
    assertNotNull(Device.getDeviceId(getContext()));
    assertNotNull(Device.getDeviceName());
    assertNotNull(Device.getUserAgent());
  }
}

package com.feedhenry.sdk.tests.api;

import android.test.AndroidTestCase;
import com.feedhenry.sdk.Device;

/**
 * Created by weili on 15/04/15.
 */
public class DeviceTest extends AndroidTestCase {
  public void testDevice() {
    Device.init(getContext());
    assertNotNull(Device.getInstance().getDeviceId());
    assertNotNull(Device.getInstance().getDeviceName());
    assertNotNull(Device.getInstance().getUserAgent());
  }
}

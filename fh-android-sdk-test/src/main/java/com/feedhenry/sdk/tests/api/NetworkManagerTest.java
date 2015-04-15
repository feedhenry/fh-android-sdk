package com.feedhenry.sdk.tests.api;

import android.test.AndroidTestCase;
import com.feedhenry.sdk.NetworkManager;

/**
 * Created by weili on 15/04/15.
 */
public class NetworkManagerTest extends AndroidTestCase {

  public void testNetworkManager(){
    NetworkManager.init(getContext());
    NetworkManager nm = NetworkManager.getInstance();
    nm.checkNetworkStatus();
    boolean isOnline = nm.isOnline();
    assertTrue(isOnline);
  }
}

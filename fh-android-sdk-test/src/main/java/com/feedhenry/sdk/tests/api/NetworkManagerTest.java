/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.tests.api;

import android.test.AndroidTestCase;
import com.feedhenry.sdk.NetworkManager;

public class NetworkManagerTest extends AndroidTestCase {

    public void testNetworkManager() {
        NetworkManager.init(getContext());
        NetworkManager nm = NetworkManager.getInstance();
        nm.checkNetworkStatus();
        boolean isOnline = nm.isOnline();
        assertTrue(isOnline);
    }
}

/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.tests.api;

import android.test.AndroidTestCase;
import com.feedhenry.sdk.AppProps;

public class AppPropsTest extends AndroidTestCase {
    public void testAppProps() throws Exception {
        AppProps appProps = AppProps.load(getContext());
        assertNotNull(appProps.getHost());
        assertEquals("http://localhost:9000", appProps.getHost());
        assertNull(appProps.getAppApiKey());
        assertNull(appProps.getAppId());
        assertNull(appProps.getProjectId());
        assertNull(appProps.getConnectionTag());
        assertTrue(appProps.isLocalDevelopment());
    }
}

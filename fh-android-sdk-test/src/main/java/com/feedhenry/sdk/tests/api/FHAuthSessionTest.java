/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.tests.api;

import android.test.AndroidTestCase;
import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.api.FHAuthSession;
import com.feedhenry.sdk.utils.DataManager;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

public class FHAuthSessionTest extends AndroidTestCase {

    private MockWebServer mockWebServer = null;
    private boolean valid = false;
    private static final String TEST_TOKEN = "testSessionToken";

    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.play(9000);
        FH.init(getContext(), null);
        DataManager.getInstance().save(FHAuthSession.SESSION_TOKEN_KEY, TEST_TOKEN);
    }

    public void tearDown() throws Exception {
        DataManager.getInstance().remove(FHAuthSession.SESSION_TOKEN_KEY);
        mockWebServer.shutdown();
        // Git a little bit time to allow mockWebServer shutdown properly
        Thread.sleep(100);
    }

    public void testExists() throws Exception {
        assertTrue(FHAuthSession.exists());
    }

    public void testVerify() throws Exception {
        MockResponse cloudSuccessResponse = new MockResponse();
        cloudSuccessResponse.addHeader("Content-Type", "application/json");
        cloudSuccessResponse.setBody("{'status':'ok', 'isValid': true}");
        mockWebServer.enqueue(cloudSuccessResponse);
        FHAuthSession.verify(new FHAuthSession.Callback() {
            @Override
            public void handleSuccess(final boolean isValid) {
                valid = isValid;
            }

            @Override
            public void handleError(FHResponse pRes) {

            }
        }, true);
        assertTrue(valid);
        assertEquals(TEST_TOKEN, FHAuthSession.getToken());
    }

    public void testClear() throws Exception {
        MockResponse cloudSuccessResponse = new MockResponse();
        cloudSuccessResponse.addHeader("Content-Type", "application/json");
        cloudSuccessResponse.setBody("{'status':'ok'}");
        mockWebServer.enqueue(cloudSuccessResponse);
        FHAuthSession.clear(true);
        assertFalse(FHAuthSession.exists());
    }
}

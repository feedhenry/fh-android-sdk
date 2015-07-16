/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.tests.api;

import android.test.AndroidTestCase;
import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.api.FHAuthRequest;
import com.feedhenry.sdk.api.FHAuthSession;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

public class FHAuthRequestTest extends AndroidTestCase {

    private MockWebServer mockWebServer = null;

    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.play(9000);
        FH.init(getContext(), null);
    }

    public void tearDown() throws Exception {
        mockWebServer.shutdown();
        // Git a little bit time to allow mockWebServer shutdown properly
        Thread.sleep(100);
    }

    public void testFHAuthRequest() throws Exception {
        MockResponse cloudSuccessResponse = new MockResponse();
        cloudSuccessResponse.addHeader("Content-Type", "application/json");
        cloudSuccessResponse.setBody("{'status':'ok', 'sessionToken': 'testSessionToken'}");
        mockWebServer.enqueue(cloudSuccessResponse);

        FHAuthRequest authRequest = new FHAuthRequest(getContext());
        authRequest.setPresentingActivity(getContext());
        authRequest.setAuthUser("testAuthPolicy", "test", "test");

        authRequest.execute(new FHActCallback() {
            @Override
            public void success(FHResponse pResponse) {

            }

            @Override
            public void fail(FHResponse pResponse) {

            }
        });
        assertTrue(FHAuthSession.exists());
    }
}

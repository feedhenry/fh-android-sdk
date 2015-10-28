/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.tests.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;
import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.api.FHAuthRequest;
import com.feedhenry.sdk.api.FHAuthSession;
import com.feedhenry.sdk.tests.sync.FHTestUtils;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import java.util.concurrent.atomic.AtomicBoolean;
import org.mockito.Mockito;

public class FHAuthRequestTest extends AndroidTestCase {

    private MockWebServer mockWebServer = null;

    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.play(9000);
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
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
    
    /**
     * Seems like OAuth 2 requests from google have an Anchor after result=success
     */
    public void testOnReceiveLogsInOAuthRequestWhenSuccessIsFollowedByAHash() {
        final String googleResponse = "https://testing.feedhenry.me/box/srv/1.1/arm/authCallback?fh_auth_session=j6wnpwpb2xjn2a7quutrxubz&authResponse={\"authToken\":\"testToken\",\"email\":\"TestEmail\",\"family_name\":\"Henry\",\"gender\":\"male\",\"given_name\":\"Feed\",\"hd\":\"feedhenry.com\",\"id\":\"8675309\",\"link\":\"https://plus.google.com/8675309\",\"name\":\"Feed Henry\",\"picture\":\"http://www.feedhenry.com/wp-content/uploads/2015/01/fh-rh-top-logo-sm.png\",\"verified_email\":true}&status=complete&result=success#";
        FHAuthRequest authRequest = new FHAuthRequest(getContext());
        final AtomicBoolean success = new AtomicBoolean(false);
        FHActCallback callback = new FHActCallback() {

            @Override
            public void success(FHResponse pResponse) {
                success.set(true);
            }

            @Override
            public void fail(FHResponse pResponse) {
                
            }
        };
        authRequest.setPresentingActivity(Mockito.mock(Context.class));
        BroadcastReceiver oauth2Receiver = FHTestUtils.instanciatePrivateInnerClass("OAuthURLRedirectReceiver", authRequest, callback);
        oauth2Receiver.onReceive(null, new Intent().putExtra("url", googleResponse));
        assertTrue(success.get());
    }
    
}

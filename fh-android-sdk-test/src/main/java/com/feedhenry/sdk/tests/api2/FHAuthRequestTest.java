/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license
 * agreement. If you do not have a contract, you do not have a license to use
 * this software.
 */
package com.feedhenry.sdk.tests.api2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.api.FHAuthRequest;
import static com.feedhenry.sdk.api.FHAuthSession.SESSION_TOKEN_KEY;
import com.feedhenry.sdk.api2.FHAuthSession;
import com.feedhenry.sdk.tests.MainActivity;
import com.feedhenry.sdk.tests.sync.FHTestUtils;
import com.feedhenry.sdk.utils.DataManager;
import com.feedhenry.sdk2.FHHttpClient;
import cz.msebera.android.httpclient.Header;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.fh.JSONObject;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class FHAuthRequestTest extends ActivityInstrumentationTestCase2 {

    private FHAuthSession fhAuthSession;
    private Context mContext;
    private DataManager mDataManager;

    public FHAuthRequestTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mContext = getActivity().getApplicationContext();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
        mDataManager = DataManager.init(mContext);
        if (mDataManager.read(SESSION_TOKEN_KEY) != null) {
            mDataManager.remove(SESSION_TOKEN_KEY);
        }
        this.fhAuthSession = new FHAuthSession(mDataManager, new FHHttpClient());
    }

    
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown(); 
        if (mDataManager != null) {
            mDataManager.remove(SESSION_TOKEN_KEY);
        }
    }

    
    
    public void testSuccessfulFHAuthRequestCreatesSessionToken() throws Exception {
        
        FHAuthRequest authRequest = new FHAuthRequest(mContext, fhAuthSession);
        authRequest.setPresentingActivity(getActivity());
        authRequest.setAuthUser("testAuthPolicy", "test", "test");

        FHHttpClient mockClient = Mockito.mock(FHHttpClient.class);
        Mockito.doAnswer(callSuccess()).when(mockClient).post(any(String.class), any(Header[].class), any(JSONObject.class), any(FHActCallback.class), anyBoolean());

        final AtomicBoolean success = new AtomicBoolean(false);
        
        FHTestUtils.injectInto(authRequest, mockClient);
        
        authRequest.execute(new FHActCallback() {
            @Override
            public void success(FHResponse pResponse) {
                success.set(true);
            }

            @Override
            public void fail(FHResponse pResponse) {

            }
        });
        assertTrue(fhAuthSession.exists());
        assertTrue(success.get());
        assertEquals("testToken", mDataManager.read(SESSION_TOKEN_KEY));
    }

    public void testFailingFHAuthRequestCallsFail() throws Exception {
        
        FHAuthRequest authRequest = new FHAuthRequest(mContext, fhAuthSession);
        authRequest.setPresentingActivity(getActivity());
        authRequest.setAuthUser("testAuthPolicy", "test", "test");

        FHHttpClient mockClient = Mockito.mock(FHHttpClient.class);
        Mockito.doAnswer(callFailure()).when(mockClient).post(any(String.class), any(Header[].class), any(JSONObject.class), any(FHActCallback.class), anyBoolean());

        final AtomicBoolean success = new AtomicBoolean(false);
        
        FHTestUtils.injectInto(authRequest, mockClient);
        
        authRequest.execute(new FHActCallback() {
            @Override
            public void success(FHResponse pResponse) {
                success.set(true);
            }

            @Override
            public void fail(FHResponse pResponse) {
                success.set(false);
            }
        });
        assertFalse(fhAuthSession.exists());
        assertFalse(success.get());
        
    }
    
    /**
     * Seems like OAuth 2 requests from google have an Anchor after
     * result=success
     */
    public void testOnReceiveLogsInOAuthRequestWhenSuccessIsFollowedByAHash() {
        final String googleResponse = "https://testing.feedhenry.me/box/srv/1.1/arm/authCallback?fh_auth_session=j6wnpwpb2xjn2a7quutrxubz&authResponse={\"authToken\":\"testToken\",\"email\":\"TestEmail\",\"family_name\":\"Henry\",\"gender\":\"male\",\"given_name\":\"Feed\",\"hd\":\"feedhenry.com\",\"id\":\"8675309\",\"link\":\"https://plus.google.com/8675309\",\"name\":\"Feed Henry\",\"picture\":\"http://www.feedhenry.com/wp-content/uploads/2015/01/fh-rh-top-logo-sm.png\",\"verified_email\":true}&status=complete&result=success#";
        FHAuthRequest authRequest = new FHAuthRequest(mContext, fhAuthSession);
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
        authRequest.setPresentingActivity(mock(MainActivity.class));
        BroadcastReceiver oauth2Receiver = FHTestUtils.instanciatePrivateInnerClass("OAuthURLRedirectReceiver", authRequest, callback);
        oauth2Receiver.onReceive(null, new Intent().putExtra("url", googleResponse));
        assertTrue(success.get());
    }

    private Answer callSuccess() {
        return new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                FHActCallback callback = (FHActCallback) args[3];
                callback.success(successResponse());
                return null;
            }

        };
    }

    
    private Answer callFailure() {
        return new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                FHActCallback callback = (FHActCallback) args[3];
                callback.fail(successResponse());
                return null;
            }

        };
    }
    
    private FHResponse successResponse() {
        JSONObject successJSON = new JSONObject("{\"status\":\"ok\", \"sessionToken\":\"testToken\"}");
        return new FHResponse(successJSON, null, null, null);
    }

}

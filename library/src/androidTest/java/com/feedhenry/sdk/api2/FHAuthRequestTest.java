/**
 * Copyright Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feedhenry.sdk.api2;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.MainActivity;
import com.feedhenry.sdk.api.FHAuthRequest;
import com.feedhenry.sdk.sync.FHTestUtils;
import com.feedhenry.sdk.utils.DataManager;
import com.feedhenry.sdk2.FHHttpClient;

import org.json.fh.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.atomic.AtomicBoolean;

import cz.msebera.android.httpclient.Header;

import static android.support.test.InstrumentationRegistry.getContext;
import static com.feedhenry.sdk.api.FHAuthSession.SESSION_TOKEN_KEY;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class FHAuthRequestTest {

    private static final String TAG = FHAuthRequestTest.class.getName();

    private FHAuthSession fhAuthSession;
    private DataManager mDataManager;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class, false, false);

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
        mDataManager = DataManager.init(mActivityRule.getActivity());
        if (mDataManager.read(SESSION_TOKEN_KEY) != null) {
            mDataManager.remove(SESSION_TOKEN_KEY);
        }
        this.fhAuthSession = new FHAuthSession(mDataManager, new FHHttpClient());
    }

    @After
    public void tearDown() throws Exception {
        if (mDataManager != null) {
            mDataManager.remove(SESSION_TOKEN_KEY);
        }
    }

    @Ignore
    public void testSuccessfulFHAuthRequestCreatesSessionToken() throws Exception {

        FHAuthRequest authRequest = new FHAuthRequest(
                mActivityRule.getActivity(), fhAuthSession);
        authRequest.setPresentingActivity(mActivityRule.getActivity());
        authRequest.setAuthUser("testAuthPolicy", "test", "test");

        FHHttpClient mockClient = Mockito.mock(FHHttpClient.class);
        Mockito.doAnswer(callSuccess())
                .when(mockClient)
                .post(
                        any(String.class),
                        any(Header[].class),
                        any(JSONObject.class),
                        any(FHActCallback.class),
                        anyBoolean()
                );

        final AtomicBoolean success = new AtomicBoolean(false);

        FHTestUtils.injectInto(authRequest, mockClient);

        authRequest.execute(new FHActCallback() {
            @Override
            public void success(FHResponse pResponse) {
                success.set(true);
            }

            @Override
            public void fail(FHResponse pResponse) {
                Log.e(TAG, pResponse.getErrorMessage(), pResponse.getError());
            }
        });
        assertTrue(fhAuthSession.exists());
        assertTrue(success.get());
        assertEquals("testToken", mDataManager.read(SESSION_TOKEN_KEY));
    }

    @Test
    public void testFailingFHAuthRequestCallsFail() throws Exception {

        FHAuthRequest authRequest = new FHAuthRequest(
                mActivityRule.getActivity(), fhAuthSession);
        authRequest.setPresentingActivity(mActivityRule.getActivity());
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
    @Test
    public void testOnReceiveLogsInOAuthRequestWhenSuccessIsFollowedByAHash() {
        final String googleResponse = "https://testing.feedhenry.me/box/srv/1.1/arm/authCallback?fh_auth_session=j6wnpwpb2xjn2a7quutrxubz&authResponse={\"authToken\":\"testToken\",\"email\":\"TestEmail\",\"family_name\":\"Henry\",\"gender\":\"male\",\"given_name\":\"Feed\",\"hd\":\"feedhenry.com\",\"id\":\"8675309\",\"link\":\"https://plus.google.com/8675309\",\"name\":\"Feed Henry\",\"picture\":\"http://www.feedhenry.com/wp-content/uploads/2015/01/fh-rh-top-logo-sm.png\",\"verified_email\":true}&status=complete&result=success#";
        FHAuthRequest authRequest = new FHAuthRequest(
                mActivityRule.getActivity(), fhAuthSession);
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

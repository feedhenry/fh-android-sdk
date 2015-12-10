/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.tests.api2;

import android.test.AndroidTestCase;
import android.util.Log;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHResponse;
import static com.feedhenry.sdk.api.FHAuthSession.SESSION_TOKEN_KEY;
import com.feedhenry.sdk.api2.FHAuthSession;
import com.feedhenry.sdk.utils.DataManager;
import com.feedhenry.sdk2.FHHttpClient;
import cz.msebera.android.httpclient.Header;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.fh.JSONObject;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


public class FHAuthSessionTest extends AndroidTestCase {

    
    private static final String TEST_TOKEN = "testSessionToken";
    private DataManager mDataManager;
    private com.feedhenry.sdk.api2.FHAuthSession session;
    private FHHttpClient mFHHttpClient;
    @Override
    public void setUp() throws Exception {
        this.mDataManager = DataManager.init(getContext());
        mDataManager.save(FHAuthSession.SESSION_TOKEN_KEY, TEST_TOKEN);
        mFHHttpClient = mock(FHHttpClient.class);
        session = new FHAuthSession(mDataManager, mFHHttpClient);
    }

    @Override
    public void tearDown() throws Exception {
        if (mDataManager != null) {
            mDataManager.remove(FHAuthSession.SESSION_TOKEN_KEY);
        }
    }

    public void testExists() throws Exception {
        assertTrue(session.exists());
    }

    public void testVerify() throws Exception {
        final AtomicBoolean valid = new AtomicBoolean(false);
        
        doAnswer(verifyTrue()).when(mFHHttpClient).post(matches("http://localhost:9000/box/srv/1.1/admin/authpolicy/verifysession"), any(Header[].class), eq(new JSONObject().put(SESSION_TOKEN_KEY, TEST_TOKEN)), any(FHActCallback.class), eq(true));
        
        session.verify(new com.feedhenry.sdk.api.FHAuthSession.Callback() {
            private String TAG = "Callback";
            @Override
            public void handleSuccess(final boolean isValid) {
                valid.set(true);
            }

            @Override
            public void handleError(FHResponse pRes) {
                Log.e(TAG, pRes.getErrorMessage());
            }
        }, true);
        
        verify(mFHHttpClient).post(matches("http://localhost:9000/box/srv/1.1/admin/authpolicy/verifysession"), any(Header[].class), eq(new JSONObject().put(SESSION_TOKEN_KEY, TEST_TOKEN)), any(FHActCallback.class), eq(true));
        assertTrue(valid.get());
        assertEquals(TEST_TOKEN, session.getToken());
    }

    public void testClear() throws Exception {
        doAnswer(verifyTrue()).when(mFHHttpClient).post(matches("http://localhost:9000/box/srv/1.1/admin/authpolicy/revokesession"), any(Header[].class), eq(new JSONObject().put(SESSION_TOKEN_KEY, TEST_TOKEN)), any(FHActCallback.class), eq(true));
        
        session.clear(true);
        assertFalse(session.exists());
        verify(mFHHttpClient).post(matches("http://localhost:9000/box/srv/1.1/admin/authpolicy/revokesession"), any(Header[].class), eq(new JSONObject().put(SESSION_TOKEN_KEY, TEST_TOKEN)), any(FHActCallback.class), eq(true));
    }

    private Answer verifyTrue() {
        return new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                FHActCallback callback = (FHActCallback) invocation.getArguments()[3];
                callback.success(successResponse());
                return null;
            }
        };
    }
    
    private FHResponse successResponse() {
        JSONObject successJSON = new JSONObject("{\"status\":\"ok\", \"isValid\":true}");
        return new FHResponse(successJSON, null, null, null);
    }
}

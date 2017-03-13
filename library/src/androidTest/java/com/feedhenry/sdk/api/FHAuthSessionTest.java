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
package com.feedhenry.sdk.api;

import android.support.test.runner.AndroidJUnit4;

import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.utils.DataManager;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class FHAuthSessionTest {

    private MockWebServer mockWebServer = null;
    private boolean valid = false;
    private static final String TEST_TOKEN = "testSessionToken";

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.play(9000);
        FH.init(getContext(), null);
        DataManager.getInstance().save(FHAuthSession.SESSION_TOKEN_KEY, TEST_TOKEN);
    }

    @After
    public void tearDown() throws Exception {
        DataManager.getInstance().remove(FHAuthSession.SESSION_TOKEN_KEY);
        mockWebServer.shutdown();
        // Git a little bit time to allow mockWebServer shutdown properly
        Thread.sleep(100);
    }

    @Test
    public void testExists() throws Exception {
        assertTrue(FHAuthSession.exists());
    }

    @Test
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

    @Test
    public void testClear() throws Exception {
        MockResponse cloudSuccessResponse = new MockResponse();
        cloudSuccessResponse.addHeader("Content-Type", "application/json");
        cloudSuccessResponse.setBody("{'status':'ok'}");
        mockWebServer.enqueue(cloudSuccessResponse);
        FHAuthSession.clear(true);
        assertFalse(FHAuthSession.exists());
    }

}

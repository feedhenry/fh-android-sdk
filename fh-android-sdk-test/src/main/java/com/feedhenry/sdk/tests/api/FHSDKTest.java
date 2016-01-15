/**
 * Copyright Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feedhenry.sdk.tests.api;

import com.feedhenry.sdk.api.FHAuthSession;
import com.feedhenry.sdk.utils.DataManager;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;
import org.json.fh.JSONException;
import org.json.fh.JSONObject;

import android.os.Looper;
import android.test.AndroidTestCase;

import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHAct;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.api.FHCloudRequest;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

public class FHSDKTest extends AndroidTestCase {

    private JSONObject resJson = null;

    private MockWebServer mockWebServer = null;

    public void setUp() throws Exception {
        super.setUp();
        mockWebServer = new MockWebServer();
        mockWebServer.play(9000);
        FH.init(getContext(), null); // this will load fhconfig.local.properties file
        FH.setLogLevel(FH.LOG_LEVEL_VERBOSE);
        DataManager.getInstance().save(FHAuthSession.SESSION_TOKEN_KEY, "testsessiontoken");
    }

    public void tearDown() throws Exception {
        super.tearDown();
        shutdownServer();
        FH.stop();
    }

    public void testFHAct() throws Exception {
        actTest();
    }

    private void actTest() throws Exception {
        String cloudHost = FH.getCloudHost();
        System.out.println("cloud host is " + cloudHost);

        // the fhconfig.local.properties file exists, use the host value from that
        // file as the cloud host
        assertEquals("http://localhost:9000", cloudHost);

        // mock response for act call
        MockResponse actSuccessResponse = new MockResponse();
        actSuccessResponse.addHeader("Content-Type", "application/json");

        actSuccessResponse.setBody("{'status':'ok', 'type': 'act'}");
        mockWebServer.enqueue(actSuccessResponse);
        FHAct actCall = FH.buildActRequest("test", new JSONObject());
        FHActCallback callback = new FHActCallback() {

            @Override
            public void success(FHResponse pResponse) {
                resJson = pResponse.getJson();
            }

            @Override
            public void fail(FHResponse pResponse) {
                resJson = null;
            }
        };

        
        runRequest(actCall, callback);
        

        assertEquals(resJson.getString("type"), "act");
        // verify request object
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod().toUpperCase());
        assertEquals("/cloud/test", request.getPath());

        String requestBody = new String(request.getBody().readUtf8());
        JSONObject requestJson = new JSONObject(requestBody);
        assertTrue(requestJson.has("__fh"));

        JSONObject fhParams = requestJson.getJSONObject("__fh");
        String deviceId = fhParams.optString("cuid", null);
        assertEquals(getDeviceId(), deviceId);
    }

    public void testCloudGet() throws Exception {
        enqueueCloudResponse();
        JSONObject p = new JSONObject();
        p.put("test", "true");
        makeCloudRequest("GET", null, p);
        verifyCloudRequest("/v1/cloud/test?test=true", "GET", null, null);
    }

    public void testCloudDelete() throws Exception {
        enqueueCloudResponse();
        JSONObject p = new JSONObject();
        p.put("test", "true");
        makeCloudRequest("DELETE", null, p);
        verifyCloudRequest("/v1/cloud/test?test=true", "DELETE", null, null);
    }

    public void testCloudPost() throws Exception {
        enqueueCloudResponse();
        JSONObject p = new JSONObject();
        p.put("test", "true");
        Header[] headers = new Header[1];
        headers[0] = new BasicHeader("testHeader", "testValue");

        makeCloudRequest("POST", headers, p);
        verifyCloudRequest("/v1/cloud/test", "POST", headers, p);
    }


    public void testCloudPut() throws Exception {
        enqueueCloudResponse();
        JSONObject p = new JSONObject();
        p.put("test", "true");
        Header[] headers = new Header[1];
        headers[0] = new BasicHeader("testHeader", "testValue");

        makeCloudRequest("PUT", headers, p);
        verifyCloudRequest("/v1/cloud/test", "PUT", headers, p);
    }

    private void shutdownServer() throws Exception {
        mockWebServer.shutdown();
        // Git a little bit time to allow mockWebServer shutdown properly
        Thread.sleep(100);
    }

    private void enqueueCloudResponse() throws Exception {
        MockResponse cloudSuccessResponse = new MockResponse();
        cloudSuccessResponse.addHeader("Content-Type", "application/json");
        cloudSuccessResponse.setBody("{'status':'ok', 'type': 'cloud'}");
        mockWebServer.enqueue(cloudSuccessResponse);
    }

    private void makeCloudRequest(String method, Header[] headers, JSONObject params) throws JSONException, Exception {
        FHCloudRequest cloudRequest = FH.buildCloudRequest("/v1/cloud/test", method, headers, params);
        FHActCallback callback = new FHActCallback() {

            @Override
            public void success(FHResponse pResponse) {
                resJson = pResponse.getJson();
            }

            @Override
            public void fail(FHResponse pResponse) {
                resJson = null;
            }
        };
        
        runRequest(cloudRequest, callback);
        
        assertNotNull(resJson);
        assertEquals("cloud", resJson.getString("type"));
    }

    private void verifyCloudRequest(String path, String method, Header[] headers, JSONObject params) throws Exception {
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals(method.toLowerCase(), request.getMethod().toLowerCase());

        String cuidHeader = request.getHeader("x-fh-cuid");
        assertEquals(getDeviceId(), cuidHeader);

        if (null != headers) {
            for (int i = 0; i < headers.length; i++) {
                String requestHeaderValue = request.getHeader(headers[i].getName());
                assertEquals(requestHeaderValue, headers[i].getValue());
            }
        }

        if (null != params) {
            String requestBody = new String(request.getBody().readUtf8());
            assertEquals(requestBody.toString(), params.toString());
        }

    }

    private String getDeviceId() {
        return android.provider.Settings.Secure.getString(getContext()
                .getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }

    private void runRequest(final FHAct pRequest,
            final FHActCallback pCallback) throws Exception {
        // The AsyncHttpClient uses Looper & Handlers to implement async http calls.
        // It requires the calling thread to have a looper attached to it.
        // When requests are made from the main UI thread, it will work find as the
        // main UI thread contains a main Looper.
        // However, if the app creates another Thread which will be used to invoke
        // the call, it should use the sync mode or attach the looper to the thread
        // as demoed below.
        // The main thread that runs the tests doesn't work with Handlers either.
        Thread testThread = new Thread() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    pRequest.execute(new FHActCallback() {

                        @Override
                        public void success(FHResponse pResponse) {
                            System.out.println("Got response " + pResponse.getRawResponse());
                            pCallback.success(pResponse);
                            Looper.myLooper().quit();
                        }

                        @Override
                        public void fail(FHResponse pResponse) {
                            System.out.println("Got error response : "
                                    + pResponse.getRawResponse());
                            pCallback.fail(pResponse);
                            Looper.myLooper().quit();
                        }
                    });
                    Looper.loop();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        };
        testThread.start();
        testThread.join();
    }

}

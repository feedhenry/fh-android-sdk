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
package com.feedhenry.sdk.tests.api2;

import android.test.AndroidTestCase;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.tests.sync.FHTestUtils;
import com.feedhenry.sdk2.FHHttpClient;
import com.feedhenry.sdk2.FHRemote;
import cz.msebera.android.httpclient.Header;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.json.fh.JSONObject;
import org.mockito.Matchers;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FHRemoteTest extends AndroidTestCase {
    private FHRemote fhRemote;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.fhRemote = new FHRemote(getContext()) {
            
            @Override
            protected String getPath() {
                return "";
            }
            
            @Override
            protected JSONObject getRequestArgs() {
                return new JSONObject();
            }
            
            @Override
            protected Header[] buildHeaders(Header[] pHeaders) {
                return new Header[0];
            }
        };
    }
    
    public void testExecuteAsyncCallsHttpClientPost() throws Exception {
        FHHttpClient httpClient = mock(FHHttpClient.class);
        FHTestUtils.injectInto(fhRemote, httpClient);
        FHActCallback callback = mock(FHActCallback.class);
        
        fhRemote.execute(callback);
        
        verify(httpClient).post(eq("http://localhost:9000/box/srv/1.1/"), any(Header[].class), eq(new JSONObject()), eq(callback), eq(false));
        
    }
    
    public void testExecuteAsyncWithSetCallbackCallsHttpClientPost() throws Exception {
        FHHttpClient httpClient = mock(FHHttpClient.class);
        FHTestUtils.injectInto(fhRemote, httpClient);
        FHActCallback callback = mock(FHActCallback.class);
        fhRemote.setCallback(callback);
        fhRemote.execute();
        
        verify(httpClient).post(eq("http://localhost:9000/box/srv/1.1/"), any(Header[].class), eq(new JSONObject()), eq(callback), eq(false));
        
    }
    
    public void testExecuteThrowsExceptionThrownByHttpClient() throws Exception {
        FHHttpClient httpClient = mock(FHHttpClient.class);
        FHTestUtils.injectInto(fhRemote, httpClient);
        final RuntimeException stubException = new RuntimeException("stubException");
        FHActCallback callback = mock(FHActCallback.class);
        Mockito.doThrow(stubException).when(httpClient).post(eq("http://localhost:9000/box/srv/1.1/"), any(Header[].class), eq(new JSONObject()), eq(callback), eq(false));
        
        fhRemote.setCallback(callback);
        
        
            fhRemote.execute();
        Mockito.verify(callback).fail(Matchers.argThat(new BaseMatcher<FHResponse>(){
            @Override
            public boolean matches(Object item) {
                FHResponse resp = (FHResponse) item;
                if (resp.getArray() != null && resp.getArray().length() > 0) {
                    return false;
                }
                if (resp.getJson() != null && resp.getJson().length() > 0) {
                    return false;
                }
                return resp.getErrorMessage() != null && resp.getError().equals(stubException);
            }

            @Override
            public void describeTo(Description description) {
                
            }
        } ));
    }
    
    public void testExecuteThrowsException2ThrownByHttpClient() throws Exception {
        FHHttpClient httpClient = mock(FHHttpClient.class);
        FHTestUtils.injectInto(fhRemote, httpClient);
        final RuntimeException stubException = new RuntimeException("stubException");
        FHActCallback callback = mock(FHActCallback.class);
        Mockito.doThrow(stubException).when(httpClient).post(eq("http://localhost:9000/box/srv/1.1/"), any(Header[].class), eq(new JSONObject()), eq(callback), eq(false));

        fhRemote.execute(callback);
        Mockito.verify(callback).fail(Matchers.argThat(new BaseMatcher<FHResponse>(){
            @Override
            public boolean matches(Object item) {
                FHResponse resp = (FHResponse) item;
                if (resp.getArray() != null && resp.getArray().length() > 0) {
                    return false;
                }
                if (resp.getJson() != null && resp.getJson().length() > 0) {
                    return false;
                }
                return resp.getErrorMessage() != null && resp.getError().equals(stubException);
            }

            @Override
            public void describeTo(Description description) {
                
            }
        } ));
    }
    
}

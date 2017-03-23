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
package com.feedhenry.sdk.sync;

import android.support.test.runner.AndroidJUnit4;

import com.feedhenry.sdk.FH;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.json.fh.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.InvocationsFinder;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.invocation.Invocation;
import org.mockito.verification.VerificationMode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static android.support.test.InstrumentationRegistry.getContext;

@RunWith(AndroidJUnit4.class)
public class FHSyncDatasetTest {

    private static final String DATASET_ID = "testDataSet";
    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start(9100);
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
        FH.init(getContext(), null);
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
        // Git a little bit time to allow mockWebServer shutdown properly
        Thread.sleep(100);
    }

    @Test
    public void testDataSetStopSyncStopsSync() throws Exception {

        mockWebServer.enqueue(new MockResponse().setBody("{}"));

        FHSyncListener listener = Mockito.mock(FHSyncListener.class);

        FHSyncConfig config = new FHSyncConfig();
        config.setNotifySyncComplete(true);
        config.setSyncFrequency(1);

        FHSyncClient client = new FHSyncClient();
        client.init(getContext(), config, listener);
        client.manage(DATASET_ID, null, new JSONObject());
        Map<String, FHSyncDataset> datasets = (Map<String, FHSyncDataset>) FHTestUtils.getPrivateField(client, "mDataSets");
        FHSyncDataset dataset = datasets.get(DATASET_ID);
        FHSyncDataset spy = Mockito.spy(dataset);
        Mockito.doNothing().when(spy).startSyncLoop();
        Mockito.doReturn(false).when(spy).isSyncRunning();
        Mockito.doReturn(true).when(spy).isSyncPending();
        Mockito.doReturn(null).when(spy).getSyncStart();
        datasets.put(DATASET_ID, spy);

        AtomicInteger invocations = new AtomicInteger(0);
        int runningInvocations = 0;
        Thread.sleep(1500);
        Mockito.verify(spy, countAtleast(invocations, 1)).startSyncLoop();
        runningInvocations = invocations.get();
        client.pauseSync();

        Thread.sleep(2000);
        Mockito.verify(spy, countExactly(invocations, runningInvocations)).startSyncLoop();

        client.resumeSync(listener);
        Thread.sleep(2000);
        Mockito.verify(spy, countAtleast(invocations, runningInvocations + 1)).startSyncLoop();
    }

    private VerificationMode countAtleast(final AtomicInteger invocationsOut, final int numberOfInvocations) {
        return new VerificationMode() {

            @Override
            public void verify(VerificationData data) {
                List<Invocation> invocations = data.getAllInvocations();
                InvocationMatcher wanted = data.getWanted();
                int actualInvocations = new InvocationsFinder().findInvocations(invocations, wanted).size();

                if (actualInvocations < numberOfInvocations) {
                    throw new IllegalStateException("Found " + actualInvocations + " but wanted " + numberOfInvocations);
                }

                invocationsOut.set(actualInvocations);


            }
        };
    }

    private VerificationMode countExactly(final AtomicInteger invocationsOut, final int numberOfInvocations) {
        return new VerificationMode() {

            @Override
            public void verify(VerificationData data) {
                List<Invocation> invocations = data.getAllInvocations();
                InvocationMatcher wanted = data.getWanted();
                int actualInvocations = new InvocationsFinder().findInvocations(invocations, wanted).size();

                if (actualInvocations != numberOfInvocations) {
                    throw new IllegalStateException("Found " + actualInvocations + " but wanted " + numberOfInvocations);
                }

                invocationsOut.set(actualInvocations);


            }
        };
    }

}

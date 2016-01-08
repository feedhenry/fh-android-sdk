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
package com.feedhenry.sdk.tests.sync;

import android.test.AndroidTestCase;
import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.sync.FHSyncClient;
import com.feedhenry.sdk.sync.FHSyncConfig;
import com.feedhenry.sdk.sync.FHSyncListener;
import com.feedhenry.sdk.sync.NotificationMessage;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class FHSyncClientTest extends AndroidTestCase {

    private MockWebServer mockWebServer = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockWebServer = new MockWebServer();
        mockWebServer.start(9000);
        FH.init(getContext(), null); // this will load fhconfig.local.properties file
        FH.setLogLevel(FH.LOG_LEVEL_VERBOSE);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        try {
            mockWebServer.shutdown();
            Thread.sleep(100);
        } catch (IOException | InterruptedException | AssertionError ignore) {
             
        }
        FH.stop();
    }

    public void testInitDestroyInitDestroyDoesNotThrowNPE() throws Exception {
        FHSyncClient client = FHSyncClient.getInstance();
        CountDownLatch latch = new CountDownLatch(1);
        client.init(getContext(), new FHSyncConfig(), new LockingSyncListener(latch));
        client.destroy();

        latch = new CountDownLatch(1);
        client.init(getContext(), new FHSyncConfig(), new LockingSyncListener(latch));
        client.destroy();

    }

    private static class LockingSyncListener implements FHSyncListener {

        final CountDownLatch latch;

        public LockingSyncListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSyncStarted(NotificationMessage pMessage) {
            latch.countDown();
        }

        @Override
        public void onSyncCompleted(NotificationMessage pMessage) {

        }

        @Override
        public void onUpdateOffline(NotificationMessage pMessage) {

        }

        @Override
        public void onCollisionDetected(NotificationMessage pMessage) {

        }

        @Override
        public void onRemoteUpdateFailed(NotificationMessage pMessage) {

        }

        @Override
        public void onRemoteUpdateApplied(NotificationMessage pMessage) {

        }

        @Override
        public void onLocalUpdateApplied(NotificationMessage pMessage) {

        }

        @Override
        public void onDeltaReceived(NotificationMessage pMessage) {

        }

        @Override
        public void onSyncFailed(NotificationMessage pMessage) {

        }

        @Override
        public void onClientStorageFailed(NotificationMessage pMessage) {

        }
    }

}

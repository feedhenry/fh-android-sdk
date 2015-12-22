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

import android.test.AndroidTestCase;
import com.feedhenry.sdk.AppProps;

public class AppPropsTest extends AndroidTestCase {

    public void testAppProps() throws Exception {
        AppProps appProps = AppProps.load(getContext());
        assertNotNull(appProps.getHost());
        assertEquals("http://localhost:9000", appProps.getHost());
        assertNull(appProps.getAppApiKey());
        assertNull(appProps.getAppId());
        assertNull(appProps.getProjectId());
        assertNull(appProps.getConnectionTag());
        assertTrue(appProps.isLocalDevelopment());

        assertNotNull(appProps.getPushServerUrl());
        assertEquals("http://localhost:9000/api/v2/ag-push", appProps.getPushServerUrl());

        assertNotNull(appProps.getPushSenderId());
        assertEquals("MY_SENDER_ID", appProps.getPushSenderId());

        assertNotNull(appProps.getPushVariant());
        assertEquals("123456789", appProps.getPushVariant());

        assertNotNull(appProps.getPushSecret());
        assertEquals("987654321", appProps.getPushSecret());
    }
}

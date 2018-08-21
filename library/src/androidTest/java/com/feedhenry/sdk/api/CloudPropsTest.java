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
package com.feedhenry.sdk.api;

import android.support.test.runner.AndroidJUnit4;

import com.feedhenry.sdk.AppProps;
import com.feedhenry.sdk.CloudProps;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getContext;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class CloudPropsTest {

    @Test
    public void testCloudProps() throws Exception {
        AppProps.load(getContext());
        CloudProps cloudProps = CloudProps.initDev();
        String cloudHost = cloudProps.getCloudHost();
        assertEquals("http://localhost:9000", cloudHost);
    }
}

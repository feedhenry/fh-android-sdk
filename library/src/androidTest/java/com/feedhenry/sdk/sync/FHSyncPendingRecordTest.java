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

import org.json.fh.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class FHSyncPendingRecordTest {

    @Test
    public void testPendingHappy() throws Exception {
        FHSyncPendingRecord pending = FHTestUtils.generateRandomPendingRecord();
        JSONObject json = pending.getJSON();

        System.out.println("penidng obj = " + json.toString());

        FHSyncPendingRecord another = FHSyncPendingRecord.fromJSON(json);
        assertEquals(pending, another);
    }

}

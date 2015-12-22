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

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;
import com.feedhenry.sdk.utils.DataManager;
import org.json.fh.JSONObject;

public class DataManagerTest extends AndroidTestCase {

    private static final String TRACKID = "faketrackid";

    private void removeAll() {
        SharedPreferences prefs = getContext().getSharedPreferences("fhsdkprivatedata", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

    public void setUp() {
        DataManager.init(getContext());
        removeAll();
    }

    public void tearDown() {
        removeAll();
    }

    public void testMigrateData() {
        DataManager dm = DataManager.getInstance();
        String initData = dm.read("init");
        assertNull(initData);
        createLegacyInitData();
        dm.migrateLegacyData();
        initData = dm.read("init");
        assertNotNull(initData);
        JSONObject initObj = new JSONObject(initData);
        assertTrue(initObj.has("trackId"));
        assertEquals(TRACKID, initObj.getString("trackId"));
    }

    public void testDataManager() {
        DataManager dm = DataManager.getInstance();
        String testKey = "testkey";
        String testValue = "testValue";
        String saved = dm.read(testKey);
        assertNull(saved);
        dm.save(testKey, testValue);
        saved = dm.read(testKey);
        assertEquals(saved, testValue);
        dm.remove(testKey);
        saved = dm.read(testKey);
        assertNull(saved);
    }

    private void createLegacyInitData() {
        SharedPreferences prefs = getContext().getSharedPreferences("init", Context.MODE_PRIVATE);
        JSONObject initObj = new JSONObject().put("trackId", TRACKID);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("init", initObj.toString());
        edit.commit();
    }
}

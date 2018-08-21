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

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.runner.AndroidJUnit4;

import com.feedhenry.sdk.utils.DataManager;

import org.json.fh.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DataManagerTest {

    private static final String TRACKID = "faketrackid";

    @Before
    public void setUp() {
        DataManager.init(getContext());
        removeAll();
    }

    @After
    public void tearDown() {
        removeAll();
    }

    private void removeAll() {
        SharedPreferences prefs = getContext().getSharedPreferences("fhsdkprivatedata", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

    @Test
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

    @Test
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

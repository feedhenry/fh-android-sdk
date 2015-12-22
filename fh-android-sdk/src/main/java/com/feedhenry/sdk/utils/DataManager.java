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
package com.feedhenry.sdk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.fh.JSONObject;

/**
 * Internal class for managing data persistence.
 */
public class DataManager {
    private static final String LEGACY_PREF_KEY = "init";
    private static final String PREF_FILE_KEY = "fhsdkprivatedata";
    private static final String PREF_KEY_PREFIX = "fhsdk_";
    private static final String MIGRATED_KEY = "legacyMigrated";
    private static final String TRACKID_KEY = "trackId";
    private static final String LOG_TAG = "com.feedhenry.sdk.utils.DataManager";
    private static DataManager mInstance;
    private Context mContext;
    private SharedPreferences mPrefs;

    private DataManager(Context context) {
        this.mContext = context.getApplicationContext();
        this.mPrefs = this.mContext.getSharedPreferences(PREF_FILE_KEY, Context.MODE_PRIVATE);
    }

    public static synchronized DataManager init(Context context) {
        if (mInstance == null) {
            mInstance = new DataManager(context);
        }
        return mInstance;
    }

    public static synchronized DataManager getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("DataManager is not initialised");
        }
        return mInstance;
    }

    public void save(String key, String value) {
        SharedPreferences.Editor editor = this.mPrefs.edit();
        editor.putString(getKey(key), value);
        editor.apply();
    }

    public String read(String key) {
        return this.mPrefs.getString(getKey(key), null);
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = this.mPrefs.edit();
        editor.remove(getKey(key));
        editor.commit();
    }

    private static String getKey(String key) {
        return PREF_KEY_PREFIX + key;
    }

    /**
     * Move the "init" value from the old pref file to the new pref file.
     * Only do it once.
     */
    public void migrateLegacyData() {
        if (read(MIGRATED_KEY) == null) {
            SharedPreferences legacyPref = this.mContext.getSharedPreferences(LEGACY_PREF_KEY, Context.MODE_PRIVATE);
            if (legacyPref != null) {
                String initValue = legacyPref.getString(LEGACY_PREF_KEY, null);
                if (initValue != null) {
                    if (isFHInitValue(initValue)) {
                        save(LEGACY_PREF_KEY, initValue);
                        FHLog.d(LOG_TAG, "legacy init data has been migrated : " + initValue);
                        // remove old prefs
                        SharedPreferences.Editor editor = legacyPref.edit();
                        editor.remove(LEGACY_PREF_KEY);
                        editor.commit();
                    }
                }
            }
            // at this point, the legacy data has been migrated if it exists.
            // If it does not exist, no need to check again in the future anyway.
            save(MIGRATED_KEY, String.valueOf(true));
        }
    }

    private static boolean isFHInitValue(String value) {
        try {
            return new JSONObject(value).has(TRACKID_KEY);
        } catch (Exception e) {
            return false;
        }
    }
}

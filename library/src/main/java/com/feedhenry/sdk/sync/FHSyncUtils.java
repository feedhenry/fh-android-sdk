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
package com.feedhenry.sdk.sync;

import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.fh.JSONArray;
import org.json.fh.JSONException;
import org.json.fh.JSONObject;

public class FHSyncUtils {

    private static final char[] DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    private static final String TAG = "FHSyncUtils";

    public static JSONArray sortObj(JSONArray pObject) {
        JSONArray results = new JSONArray();
        
        JSONArray castedObj = (JSONArray) pObject;
        for (int i = 0, length = castedObj.length(); i < length; i++) {
            JSONObject obj = new JSONObject();
            obj.put("key", i + "");
            Object value = castedObj.get(i);
            if (value instanceof JSONObject || value instanceof JSONArray) {
                obj.put("value", sortObj(value));
            } else {
                obj.put("value", value);
            }
            results.put(obj);
        }

        return results;
    }

    public static JSONArray sortObj(JSONObject pObject) {
        JSONArray results = new JSONArray();
        
        JSONArray keys = pObject.names();
        List<String> sortedKeys = sortNames(keys);
        for (String sortedKey : sortedKeys) {
            JSONObject obj = new JSONObject();
            String key = sortedKey;
            Object value = ((JSONObject) pObject).get(key);
            obj.put("key", sortedKey);
            if (value instanceof JSONObject || value instanceof JSONArray) {
                obj.put("value", sortObj(value));
            } else {
                obj.put("value", value);
            }
            results.put(obj);
        }
        
        return results;
    }
     
    public static String generateObjectHash(JSONArray pObject) {
        String hashValue = "";
        JSONArray sorted = sortObj(pObject);
        hashValue = generateHash(sorted.toString());

        return hashValue;
    }

       public static String generateObjectHash(JSONObject pObject) {
        String hashValue = "";
        JSONArray sorted = sortObj(pObject);
        hashValue = generateHash(sorted.toString());

        return hashValue;
    }
    
    public static String generateHash(String pText) {
        try {
            String hashValue;
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.reset();
            md.update(pText.getBytes("ASCII"));
            hashValue = encodeHex(md.digest());
            return hashValue;
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Log.e(TAG, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    private static String encodeHex(byte[] pData) {
        int l = pData.length;

        char[] out = new char[l << 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS[(0xF0 & pData[i]) >>> 4];
            out[j++] = DIGITS[0x0F & pData[i]];
        }

        return new String(out);
    }

    private static Object sortObj(Object value) {
        if (value instanceof JSONArray) {
            return sortObj((JSONArray)value);
        } else if (value instanceof JSONObject) {
            return sortObj((JSONObject)value);
        } else {
            throw new IllegalArgumentException(String.format("A object %s was snuck into a JSON tree", value.toString()));
        }
    }
    
    private static List<String> sortNames(JSONArray pNames) throws JSONException {
        if (pNames == null) {
            return Collections.emptyList();
        }
        
        int length = pNames.length();
        ArrayList<String> names = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            names.add(pNames.getString(i));
        }
        Collections.sort(names);
        return names;
    }

    
}

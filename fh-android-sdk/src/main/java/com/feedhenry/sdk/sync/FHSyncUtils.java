/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.sync;

import java.security.MessageDigest;
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

    public static JSONArray sortObj(Object pObject) throws Exception {
        JSONArray results = new JSONArray();
        if (pObject instanceof JSONArray) {
            results = new JSONArray();
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
        } else if (pObject instanceof JSONObject) {
            JSONArray keys = ((JSONObject) pObject).names();
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
        } else {
            throw new Exception("object is not JSONObject or JSONArray");
        }
        return results;
    }

    public static String generateObjectHash(Object pObject) {
        String hashValue = "";
        try {
            JSONArray sorted = sortObj(pObject);
            hashValue = generateHash(sorted.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hashValue;
    }

    public static String generateHash(String pText) throws Exception {
        String hashValue;
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.reset();
        md.update(pText.getBytes("ASCII"));
        hashValue = encodeHex(md.digest());
        return hashValue;
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

    private static List<String> sortNames(JSONArray pNames) throws JSONException {
        if (pNames == null) {
            return Collections.emptyList();
        }
        
        int length = pNames.length();
        ArrayList<String> names = new ArrayList<String>(length);
        for (int i = 0; i < length; i++) {
            names.add(pNames.getString(i));
        }
        Collections.sort(names);
        return names;
    }
}

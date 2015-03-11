package com.feedhenry.sdk.tests.sync;

import java.util.Date;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.feedhenry.sdk.sync.FHSyncDataRecord;
import com.feedhenry.sdk.sync.FHSyncPendingRecord;

public class FHTestUtils {

	public static JSONObject generateJSON() throws Exception{
		JSONObject ret = new JSONObject();
		ret.put("testStringKey", genRandomString(10));
		ret.put("testNumberKey", new Random().nextInt());
		JSONArray arr = new JSONArray();
		arr.put(genRandomString(10));
		arr.put(genRandomString(10));
		ret.put("testArrayKey", arr);
		JSONObject dict = new JSONObject();
		dict.put(genRandomString(10), genRandomString(10));
		dict.put(genRandomString(10), genRandomString(10));
		ret.put("testDictKey", dict);
		return ret;
	}
	
	private static String genRandomString(int pLength){
		Random r = new Random();
		String letter = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<pLength;i++){
		  sb.append(letter.charAt(r.nextInt(1000000)%(letter.length())));
		}
		return sb.toString();
	}
	
	public static FHSyncDataRecord generateRadomDataRecord() throws Exception{
		JSONObject json = generateJSON();
		return new FHSyncDataRecord(json);
	}
	
	public static FHSyncPendingRecord generateRandomPendingRecord() throws Exception{
		FHSyncPendingRecord pending = new FHSyncPendingRecord();
		pending.setInFlightDate(new Date());
		pending.setInFight(true);
		pending.setCrashed(false);
		pending.setAction("create");
		pending.setTimestamp(new Date().getTime());
		pending.setUid(genRandomString(10));
		pending.setPreData(generateRadomDataRecord());
		pending.setPostData(generateRadomDataRecord());
		return pending;
	}
}

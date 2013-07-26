package com.feedhenry.sdk.sync.test;

import org.json.fh.JSONArray;
import org.json.fh.JSONObject;

import com.feedhenry.sdk.sync.FHSyncUtils;

import junit.framework.TestCase;

public class FHSyncUtilsTest extends TestCase {

  public void setUp() {

  }

  public void tearDown() {

  }

  public void testStringHash() throws Exception {
    String text = "test";
    String hashed = FHSyncUtils.generateHash(text);
    String expeted = "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3";
    System.out.println("hashed = " + hashed);
    assertEquals(expeted, hashed);
  }

  public void testGenerateObjHash() throws Exception {
    JSONObject obj = new JSONObject();
    obj.put("testKey", "Test Data");
    obj.put("testBoolKey", true);
    obj.put("testNumKey", 10);
    JSONArray arr = new JSONArray();
    arr.put("obj1");
    arr.put("obj2");
    obj.put("testArrayKey", arr);
    JSONObject jsobj = new JSONObject();
    jsobj.put("obj3key", "obj3");
    jsobj.put("obj4key", "obj4");
    obj.put("testDictKey", jsobj);
    String hash = FHSyncUtils.generateObjectHash(obj);
    System.out.println("Generated hash = " + hash);
    String expected = "5f4675723d658919ede35fac62fade8c6397df1d";
    assertEquals(expected, hash);
  }
}

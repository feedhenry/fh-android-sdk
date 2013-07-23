package com.feedhenry.sdk.sync;

import junit.framework.TestCase;

import org.json.JSONObject;

public class FHSyncDataRecordTest extends TestCase {

  public void testDataHappy() {
    JSONObject obj = FHTestUtils.generateJSON();
    System.out.println("Generated json = " + obj.toString());
    FHSyncDataRecord record = new FHSyncDataRecord(obj);
    assertNotNull(record.getData());
    assertNotNull(record.getHashValue());

    JSONObject json = record.getJSON();
    FHSyncDataRecord another = FHSyncDataRecord.fromJSON(json);
    assertEquals(record, another);
  }

}

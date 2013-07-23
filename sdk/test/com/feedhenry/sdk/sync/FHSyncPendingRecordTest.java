package com.feedhenry.sdk.sync;

import org.json.JSONObject;

import junit.framework.TestCase;

public class FHSyncPendingRecordTest extends TestCase {

  public void testPendingHappy() {
    FHSyncPendingRecord pending = FHTestUtils.generateRandomPendingRecord();
    JSONObject json = pending.getJSON();

    System.out.println("penidng obj = " + json.toString());

    FHSyncPendingRecord another = FHSyncPendingRecord.fromJSON(json);
    assertEquals(pending, another);
  }
}

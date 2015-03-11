package com.feedhenry.sdk.tests.sync;

import org.json.JSONObject;

import com.feedhenry.sdk.sync.FHSyncPendingRecord;

import junit.framework.TestCase;

public class FHSyncPendingRecordTest extends TestCase {

  public void testPendingHappy() throws Exception {
    FHSyncPendingRecord pending = FHTestUtils.generateRandomPendingRecord();
    JSONObject json = pending.getJSON();

    System.out.println("penidng obj = " + json.toString());

    FHSyncPendingRecord another = FHSyncPendingRecord.fromJSON(json);
    assertEquals(pending, another);
  }
}

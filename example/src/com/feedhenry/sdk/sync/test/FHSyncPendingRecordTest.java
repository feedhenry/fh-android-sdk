package com.feedhenry.sdk.sync.test;

import org.json.fh.JSONObject;

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

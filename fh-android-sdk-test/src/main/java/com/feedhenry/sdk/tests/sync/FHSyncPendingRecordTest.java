/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.tests.sync;

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

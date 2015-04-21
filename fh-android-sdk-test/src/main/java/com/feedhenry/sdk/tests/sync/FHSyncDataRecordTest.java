/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.tests.sync;

import junit.framework.TestCase;

import org.json.fh.JSONObject;

import com.feedhenry.sdk.sync.FHSyncDataRecord;

public class FHSyncDataRecordTest extends TestCase {

    public void testDataHappy() throws Exception {
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

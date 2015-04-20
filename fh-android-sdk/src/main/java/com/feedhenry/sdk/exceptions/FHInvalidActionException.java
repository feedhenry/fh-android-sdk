/**
 * Copyright (c) 2014 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.exceptions;

public class FHInvalidActionException extends Exception {

    private static final String mMessage = "Invalid action : ";

    public FHInvalidActionException(String pAction) {
        super(mMessage + pAction);
    }
}

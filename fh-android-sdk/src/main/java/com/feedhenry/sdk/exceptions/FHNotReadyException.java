/**
 * Copyright (c) 2014 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.exceptions;

/**
 * This exception will be thrown if an FH API method is called before FH.init finishes.
 * 
 */
public class FHNotReadyException extends Exception {

    private static final String mMessage = "FH SDK is not ready. You need to ensure FH.init is called.";

    public FHNotReadyException() {
        super(mMessage);
    }
}

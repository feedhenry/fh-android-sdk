/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk;

/**
 * Represents a request call to FeedHenry.
 *
 * When asynchronous mode is used, the request will be executed on a separate thread and the
 * calling thread will be returned immediately.
 * When the request is completed, the callback to process the response will be executed on the
 * calling thread using the Looper class.
 * The calling thread needs to be alive for the callback to be processed properly.
 *
 * If you are making API calls on short-lived threads, consider using synchronous mode instead.
 */

public interface FHAct {
    /**
     * Sets the callback function to be executed when the action is finished.
     *
     * @param pCallback the callback function
     */
    void setCallback(FHActCallback pCallback);

    /**
     * Executes the request asynchronously.
     * Executes the callback function set by {@link #setCallback(FHActCallback pCallback)} when the request finishes.
     *
     * @throws Exception this method is allowed to throw an exception
     */
    void executeAsync() throws Exception;

    /**
     * Executes the request asynchronously.
     * Executes the pCallback function when it finishes.
     *
     * @param pCallback the callback function
     * @throws Exception this method is allowed to throw an exception
     */
    void executeAsync(FHActCallback pCallback) throws Exception;

    /**
     * Executes the request synchronously.
     *
     * @param pCallback the callback function
     * @throws Exception this method is allowed to throw an exception
     */
    void execute(FHActCallback pCallback) throws Exception;
}

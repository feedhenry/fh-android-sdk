/**
 * Copyright Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feedhenry.sdk;

/**
 * Represents a request call to FeedHenry.
 *
 * When asynchronous mode is used, the request will be executed on a separate
 * thread and the calling thread will be returned immediately. When the request
 * is completed, the callback to process the response will be executed on the
 * calling thread using the Looper class. The calling thread needs to be alive
 * for the callback to be processed properly.
 *
 * If you are making API calls on short-lived threads, consider using
 * synchronous mode instead.
 *
 * @deprecated the execute method will become an asynchronous method and the
 * executeAsync methods will be removed in fh-android-sdk:3.0.0/Platform 3.7.0
 */
@Deprecated
public interface FHAct {

    /**
     * Sets the callback function to be executed when the action is finished.
     *
     * @param pCallback the callback function
     */
    void setCallback(FHActCallback pCallback);

    /**
     * Executes the request asynchronously. Executes the callback function set
     * by {@link #setCallback(FHActCallback pCallback)} when the request
     * finishes.
     *
     */
    void executeAsync();

    /**
     * Executes the request asynchronously. Executes the pCallback function when
     * it finishes.
     *
     * @param pCallback the callback function
     */
    void executeAsync(FHActCallback pCallback);

    /**
     * Executes the request synchronously.
     *
     * @param pCallback the callback function
     */
    void execute(FHActCallback pCallback);
}

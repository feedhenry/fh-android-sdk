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
package com.feedhenry.sdk.utils;

import android.util.Log;
import com.feedhenry.sdk.FH;

public class FHLog {

    private static void log(int pLogLevel, String pTag, String pMessage, Throwable pThrowable) {
        if (pLogLevel >= FH.getLogLevel()) {
            if (pLogLevel == FH.LOG_LEVEL_VERBOSE) {
                Log.v(pTag, pMessage);
            } else if (pLogLevel == FH.LOG_LEVEL_DEBUG) {
                Log.d(pTag, pMessage);
            } else if (pLogLevel == FH.LOG_LEVEL_INFO) {
                Log.i(pTag, pMessage);
            } else if (pLogLevel == FH.LOG_LEVEL_WARNING) {
                Log.w(pTag, pMessage);
            } else if (pLogLevel == FH.LOG_LEVEL_ERROR) {
                if (null == pThrowable) {
                    Log.e(pTag, pMessage);
                } else {
                    Log.e(pTag, pMessage, pThrowable);
                }
            }
        }
    }

    public static void v(String pTag, String pMessage) {
        log(FH.LOG_LEVEL_VERBOSE, pTag, pMessage, null);
    }

    public static void d(String pTag, String pMessage) {
        log(FH.LOG_LEVEL_DEBUG, pTag, pMessage, null);
    }

    public static void i(String pTag, String pMessage) {
        log(FH.LOG_LEVEL_INFO, pTag, pMessage, null);
    }

    public static void w(String pTag, String pMessage) {
        log(FH.LOG_LEVEL_WARNING, pTag, pMessage, null);
    }

    public static void e(String pTag, String pMessage, Throwable pThrowable) {
        log(FH.LOG_LEVEL_ERROR, pTag, pMessage, pThrowable);
    }
}

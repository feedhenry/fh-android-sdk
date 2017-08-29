/**
 * Copyright Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feedhenry.sdk.sync.android;

import android.content.Context;
import android.support.annotation.NonNull;
import com.feedhenry.sdk.sync.IFileStorage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

/**
 * Storage in Android local data store
 */

public class AndroidFileStorage implements IFileStorage {

    private final WeakReference<Context> mContextWeak;

    public AndroidFileStorage(Context ctx) {
        mContextWeak = new WeakReference<>(ctx);
    }

    @Override
    public FileInputStream openFileInput(@NonNull String pFilePath) throws FileNotFoundException {
        Context ctx = mContextWeak.get();
        if (ctx != null) {
            return ctx.openFileInput(pFilePath);
        } else {
            throw new IllegalStateException("Con't open file when Context is destroyed");
        }
    }

    @Override
    public FileOutputStream openFileOutput(@NonNull String pFilePath) throws FileNotFoundException {
        Context ctx = mContextWeak.get();
        if (ctx != null) {
            return ctx.openFileOutput(pFilePath, Context.MODE_PRIVATE);
        } else {
            throw new IllegalStateException("Con't open file when Context is destroyed");
        }
    }
}

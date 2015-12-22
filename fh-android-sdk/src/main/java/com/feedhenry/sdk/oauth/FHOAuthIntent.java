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
package com.feedhenry.sdk.oauth;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

/**
 * An intent wrapper for the WebView.
 */
public class FHOAuthIntent extends Activity {

    private FHOAuthWebView mWebview;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        mWebview = new FHOAuthWebView(this, getIntent().getBundleExtra("settings"));
        mWebview.onCreate();
        setContentView(mWebview.getView());
    }

    public boolean onKeyDown(int pkeyCode, KeyEvent pEvent) {
        if (pkeyCode == KeyEvent.KEYCODE_BACK) {
            mWebview.close();
            return true;
        } else {
            return super.onKeyDown(pkeyCode, pEvent);
        }
    }
}

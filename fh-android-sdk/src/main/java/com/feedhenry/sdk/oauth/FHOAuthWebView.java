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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.feedhenry.sdk.utils.FHLog;

/**
 * Construct a WebView window and load a url request.
 * Broadcasts URL change events.
 */
public class FHOAuthWebView {
    private WebView mWebView;
    private Bundle mSettings = null;
    private Activity mContext = null;
    private ViewGroup mMainLayout = null;
    private String mFinishedUrl = "NOT_FINISHED";
    private boolean mFinished = false;
    private static final String LOG_TAG = "com.feedhenry.sdk.oauth.FHOAuthWebView";

    public static final String BROADCAST_ACTION_FILTER = "com.feedhenry.sdk.oauth.urlChanged";

    public FHOAuthWebView(Activity pContext, Bundle pSettings) {
        mContext = pContext;
        mSettings = pSettings;
    }

    public void onCreate() {
        String startUrl = mSettings.getString("url");
        String title = mSettings.getString("title");
        mMainLayout = null;
        mMainLayout = new LinearLayout(this.mContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            0.0F);
        mMainLayout.setLayoutParams(lp);
        ((LinearLayout) mMainLayout).setGravity(Gravity.CENTER_VERTICAL);
        ((LinearLayout) mMainLayout).setOrientation(LinearLayout.VERTICAL);

        mWebView = new WebView(this.mContext);

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        mWebView.setLayoutParams(
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1.0F));

        mWebView.setWebViewClient(
            new WebViewClient() {

                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    FHLog.d(LOG_TAG, "going to load url " + url);
                    return !url.contains("http");
                }

                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    FHLog.d(LOG_TAG, "start to load " + url);
                    if (url.contains("status=complete")) {
                        mFinishedUrl = url;
                        mFinished = true;
                    }
                }

                public void onPageFinished(WebView view, String url) {
                    FHLog.d(LOG_TAG, "finish loading " + url);
                    if (mFinished && !"about:blank".equals(url)) {
                        close();
                    }
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    FHLog.d(LOG_TAG, "error: " + description + "url: " + failingUrl);
                }
            });

        mWebView.requestFocusFromTouch();
        mWebView.setVisibility(View.VISIBLE);
        LinearLayout barLayout = new LinearLayout(this.mContext);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            0.0F);
        barLayout.setLayoutParams(blp);
        barLayout.setGravity(Gravity.CENTER_VERTICAL);

        initHeaderBar(barLayout, title);

        mMainLayout.addView(barLayout);
        mMainLayout.setBackgroundColor(Color.TRANSPARENT);
        mMainLayout.setBackgroundResource(0);
        mMainLayout.addView(this.mWebView);

        mWebView.loadUrl(startUrl);
    }

    private void initHeaderBar(LinearLayout barLayout, String title) {
        barLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        barLayout.setBackgroundColor(Color.BLACK);

        TextView text = new TextView(this.mContext);
        if (!title.equals("undefined")) {
            text.setText(title);
        }
        text.setTextColor(Color.WHITE);
        text.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        text.setTextSize(20);
        text.setTypeface(null, 1);
        text.setLayoutParams(
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0F));

        barLayout.addView(text);
    }

    public void close() {
        mWebView.stopLoading();
        Intent i = new Intent();
        i.setAction(BROADCAST_ACTION_FILTER);
        i.putExtra("url", mFinishedUrl);
        mContext.sendBroadcast(i);
        mContext.finish();
    }

    public ViewGroup getView() {
        return mMainLayout;
    }

    public void destroy() {
        if (mWebView != null) {
            mWebView.destroy();
        }
    }
}

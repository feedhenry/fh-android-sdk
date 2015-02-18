package com.feedhenry.sdk.oauth;

import com.feedhenry.sdk.utils.FHLog;

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

/**
 * Construct a WebView window and load a url request. Broadcast url changes event to anyone who's interested.
 *
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
  
  public FHOAuthWebView(Activity pContext, Bundle pSettings){
    mContext = pContext;
    mSettings = pSettings;
  }
  
  public void onCreate(){
    String startUrl = mSettings.getString("url");
    String title = mSettings.getString("title");
    mMainLayout = null;
    mMainLayout = new LinearLayout(this.mContext);
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 0.0F);
    mMainLayout.setLayoutParams(lp);
    ((LinearLayout) mMainLayout).setGravity(Gravity.CENTER_VERTICAL);
    ((LinearLayout) mMainLayout).setOrientation(LinearLayout.VERTICAL);
   
    mWebView = new WebView(this.mContext);

    WebSettings settings = mWebView.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setBuiltInZoomControls(true);
    settings.setJavaScriptCanOpenWindowsAutomatically(true);
    
    mWebView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 1.0F));
    
    mWebView.setWebViewClient(new WebViewClient() {

      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        FHLog.d(LOG_TAG, "going to load url " + url);
        if (url.contains("http")) {
          return false;
        }
        return true;
      }

      public void onPageStarted(WebView view, String url, Bitmap favicon) {
        FHLog.d(LOG_TAG, "start to load " + url);
        if(url.indexOf("status=complete") > -1){
          mFinishedUrl = url;
          mFinished = true;
        }
      }

      public void onPageFinished(WebView view, String url) {
        FHLog.d(LOG_TAG, "finish loading " + url);
        if(mFinished && !"about:blank".equals(url)){
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
    LinearLayout barlayout = new LinearLayout(this.mContext);
    LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0F);
    barlayout.setLayoutParams(blp);
    barlayout.setGravity(Gravity.CENTER_VERTICAL);

    initHeaderBar(barlayout, title);
    
    mMainLayout.addView(barlayout);
    mMainLayout.setBackgroundColor(Color.TRANSPARENT);
    mMainLayout.setBackgroundResource(0);
    mMainLayout.addView(this.mWebView);
    
    mWebView.loadUrl(startUrl);
  }
  
  private void initHeaderBar(LinearLayout barlayout, String title) {
    barlayout.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
    barlayout.setBackgroundColor(Color.BLACK);

    TextView text = new TextView(this.mContext);
    if (!title.equals("undefined")){
      text.setText(title);
    }
    text.setTextColor(Color.WHITE);
    text.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
    text.setTextSize(20);
    text.setTypeface(null, 1);
    text.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0F));
    
    barlayout.addView(text);
  }
  
  public void close() {
    mWebView.stopLoading();
    Intent i = new Intent();
    i.setAction(BROADCAST_ACTION_FILTER);
    i.putExtra("url", mFinishedUrl);
    mContext.sendBroadcast(i);
    mContext.finish();
  }
  
  public ViewGroup getView(){
    return mMainLayout;
  }
  
  public void destroy(){
    if(null != mWebView){
      mWebView.destroy();
    }
  }

}

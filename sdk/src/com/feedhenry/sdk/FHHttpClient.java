package com.feedhenry.sdk;

import android.util.Log;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * The static HTTP client for use solely by the FeedHenry SDK, implemented as
 * outlined at http://loopj.com/android-async-http/
 */
class FHHttpClient {
	private static AsyncHttpClient mClient = new AsyncHttpClient();
	static {
		mClient.setUserAgent("fh_android_sdk");
	}

	// We only need to declare a POST function; the FeedHenry act calls make no
	// use of GET.
	public static void post(String url, JSONObject params,
			FHActCallback callback) throws Exception {
		
		StringEntity entity;
		if (params != null) {
			entity = new StringEntity(params.toString(), "UTF-8");
		} else {
			entity = new StringEntity(new JSONObject().toString());
		}
		
		mClient.post(null, url, entity, "application/json",
				new FHJsonHttpResponseHandler(callback));
	}

	static class FHJsonHttpResponseHandler extends JsonHttpResponseHandler {
		private FHActCallback callback;

		public FHJsonHttpResponseHandler(FHActCallback pCallback) {
			super();
			callback = pCallback;
		}

		@Override
		public void onSuccess(JSONArray pRes) {
			Log.d(FH.LOG_TAG, "Got response : " + pRes.toString());
			FHResponse fhres = new FHResponse(null, pRes, null, null);
			doCallback("success", fhres);
		}

		@Override
		public void onSuccess(JSONObject pRes) {
			Log.d(FH.LOG_TAG, "Got response : " + pRes.toString());
			FHResponse fhres = new FHResponse(pRes, null, null, null);
			doCallback("success", fhres);
		}

		@Override
		public void onFailure(Throwable e, String content) {
			Log.e(FH.LOG_TAG, e.getMessage(), e);
			FHResponse fhres = new FHResponse(null, null, e, content);
			doCallback("fail", fhres);
		}
		
		/**
		 * Helper method which only calls the callback function providing it
		 * isn't null. Saves us from having to do a null check in every block.
		 * 
		 * @param method Either "success" or "failure".
		 * @param res The response given from the request.
		 */
		private void doCallback(String method, FHResponse res) {
			if (callback != null) {
				if (method == "success") {
					callback.success(res);
				} else if (method == "fail") {
					callback.fail(res);
				}
			}
		}
	}
}

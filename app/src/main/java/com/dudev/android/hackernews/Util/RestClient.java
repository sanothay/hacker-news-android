package com.dudev.android.hackernews.Util;

import android.content.Context;
import android.text.TextUtils;
import com.dudev.android.hackernews.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams
        ;

/**
 * Created by soulivanh on 5/5/15 AD.
 */
public class RestClient {

    private static String BASE_URL;

    private static AsyncHttpClient client;
    private static RestClient restClient;
    public static RestClient getInstance(Context context) {
        if (restClient == null) {
            restClient = new RestClient(context);
        }

        return restClient;
    }

    private RestClient(Context context) {
        if (client == null) {
            client = new AsyncHttpClient(true, 80, 443);
        }

        if (TextUtils.isEmpty(BASE_URL)) {
            BASE_URL = context.getString(R.string.base_url);
        }
    }


    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}

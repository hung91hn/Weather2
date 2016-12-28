package com.hung91hn.weather.utils;


import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class CustomRequest extends Request<String> {
    private int method;
    private String url;
    private Map<String, String> params;
    private Response.Listener listener;


    public CustomRequest(int method, String url, Map<String, String> params, Response.Listener<String> responseListener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        Log.d("hung91hn",url);
        this.method = method;
        this.url = url;
        this.params = params;
        this.listener = responseListener;
        this.setRetryPolicy(new DefaultRetryPolicy(30000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(jsonString, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(String response) {
        if (listener != null) listener.onResponse(response);
    }

    @Override
    public String getUrl() {
        if (method == Request.Method.GET) {
            StringBuilder stringBuilder = new StringBuilder(url);
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            int i = 1;
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                if (i == 1) {
                    stringBuilder.append("?" + entry.getKey() + "=" + entry.getValue());
                } else {
                    stringBuilder.append("&" + entry.getKey() + "=" + entry.getValue());
                }
                iterator.remove(); // avoids a ConcurrentModificationException
                i++;
            }
            url = stringBuilder.toString();
        }
        return url.replaceAll(" ","%20");
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Connection", "close");
        return headers;
    }
}

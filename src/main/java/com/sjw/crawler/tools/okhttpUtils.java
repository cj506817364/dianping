package com.sjw.crawler.tools;

import okhttp3.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/9/25.
 */
public class okhttpUtils {
    private static final OkHttpClient.Builder MOKHTTPCLIENT = new OkHttpClient.Builder();
    private final static Integer WriteTime = 5000;
    private final static Integer ReadTime = 5000;
    private final static Integer ConnectTime = 5000;
//    private final static CookieJar cookieJar = new CookieUtil();

    static {
        MOKHTTPCLIENT.connectTimeout(WriteTime, TimeUnit.SECONDS);
        MOKHTTPCLIENT.readTimeout(ReadTime, TimeUnit.SECONDS);
        MOKHTTPCLIENT.writeTimeout(ConnectTime, TimeUnit.SECONDS);
        MOKHTTPCLIENT.followRedirects(false);
        MOKHTTPCLIENT.followSslRedirects(false);
//        MOKHTTPCLIENT.cookieJar(cookieJar);
    }


    public static Response execute(Request request, Proxy proxy) throws IOException {
        return MOKHTTPCLIENT.proxy(proxy).build().newCall(request).execute();
    }

    public static Response execute(Request request) throws IOException {
        return MOKHTTPCLIENT.build().newCall(request).execute();
    }


    public static void enqueue(Request request, Callback responseCallback) {
        MOKHTTPCLIENT.build().newCall(request).enqueue(responseCallback);
    }

    static int serversLoadTimes;


    public static String getStringFromServer(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = execute(request);
        if (response.isSuccessful()) {
            String responseUrl = response.body().string();
            return responseUrl;
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }

    private static final String CHARSET_NAME = "UTF-8";


    public static String formatParams(List<BasicNameValuePair> params) {
        return URLEncodedUtils.format(params, CHARSET_NAME);
    }

    public static String attachHttpGetParams(String url, List<BasicNameValuePair> params) {
        return url + "?" + formatParams(params);
    }

    public static String attachHttpGetParam(String url, String name, String value) {
        return url + "?" + name + "=" + value;
    }
}

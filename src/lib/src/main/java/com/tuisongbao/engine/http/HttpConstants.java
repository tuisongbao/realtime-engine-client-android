package com.tuisongbao.engine.http;

public class HttpConstants {

    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String ENGINE_SERVER_REQUEST_URL = getRestURL("/v2/sdk/engine/server");
    private static final String HOST_URL = "http://192.168.225.102";

    public static String getRestURL(String path) {
        return HOST_URL + path;
    }
}
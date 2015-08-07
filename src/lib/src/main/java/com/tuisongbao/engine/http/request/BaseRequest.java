package com.tuisongbao.engine.http.request;

import com.tuisongbao.engine.http.HttpsClient;
import com.tuisongbao.engine.http.response.BaseResponse;
import com.tuisongbao.engine.log.LogUtil;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

public class BaseRequest extends HttpEntityEnclosingRequestBase
{
    private static final String TAG = "TSB" + BaseRequest.class.getSimpleName();
    String method;
    DefaultHttpClient httpClient;
    String params;

    public BaseRequest(String method, String url) {
        try {
            this.method = method;
            setURI(URI.create(url));
            this.httpClient = HttpsClient.getDefaultHttpClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BaseRequest(String method, String url, String object)
    {
        try {
            this.method = method;
            setURI(URI.create(url));
            this.params = object;
            this.httpClient = HttpsClient.getDefaultHttpClient();

            if (object != null) {
                StringEntity entity = new StringEntity(object, HTTP.UTF_8);
                entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                setEntity(entity);
            }
            setHeader(HTTP.CONTENT_TYPE, "application/json");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getMethod()
    {
        return this.method;
    }

    @Override
    public String toString() {
        return this.getMethod() + " " + this.getURI() + " " + this.params;
    }

    // TODO: 15-8-7 Set timeout
    public BaseResponse execute()
    {
        BaseResponse localResponse = null;
        try
        {
            LogUtil.info(TAG, this.toString());
            localResponse = new BaseResponse(this.httpClient.execute(this));
        }
        catch (IOException localIOException)
        {
            LogUtil.error(TAG, localIOException.getMessage()
                    + "\n IOException when executing request. Do you have permission to access the internet?");
        }
        return localResponse;
    }

}

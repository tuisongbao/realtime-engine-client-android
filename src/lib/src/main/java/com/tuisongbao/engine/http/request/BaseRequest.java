package com.tuisongbao.engine.http.request;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.http.HttpsClient;
import com.tuisongbao.engine.http.response.BaseResponse;
import com.tuisongbao.engine.log.LogUtil;

public class BaseRequest extends HttpEntityEnclosingRequestBase
{
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
            setAuth(TSBEngine.getTSBEngineOptions().getAppId(), TSBEngine.getTSBEngineOptions().getAppId());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setAuth(String username, String password)
    {
        UsernamePasswordCredentials localUsernamePasswordCredentials = new UsernamePasswordCredentials(username, password);
        this.setHeader(BasicScheme.authenticate(localUsernamePasswordCredentials, HTTP.UTF_8, false));
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

    // TODO: set timeout
    public BaseResponse execute()
    {
        BaseResponse localResponse = null;
        try
        {
            LogUtil.info(LogUtil.LOG_TAG_HTTP, this.toString());
            localResponse = new BaseResponse(this.httpClient.execute(this));
        }
        catch (IOException localIOException)
        {
            LogUtil.error(LogUtil.LOG_TAG_HTTP, localIOException.getMessage()
                    + "\n IOException when executing request. Do you have permission to access the internet?");
        }
        return localResponse;
    }

}
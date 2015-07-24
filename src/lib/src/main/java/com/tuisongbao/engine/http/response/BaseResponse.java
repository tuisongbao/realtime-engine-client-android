package com.tuisongbao.engine.http.response;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.json.JSONObject;

import com.tuisongbao.engine.log.LogUtil;


public class BaseResponse
{
    HttpResponse mRresponse;
    String mBody;

    public BaseResponse(HttpResponse paramHttpResponse)
    {
        this.mRresponse = paramHttpResponse;
        LogUtil.info(LogUtil.LOG_TAG_HTTP, this.toString());
    }

    public int status()
    {
        StatusLine localStatusLine = this.mRresponse.getStatusLine();
        if (localStatusLine != null) return this.mRresponse.getStatusLine().getStatusCode();
        return -1;
    }

    public boolean isStatusOk()
    {
        return this.mRresponse != null && status() == 200;
    }

    public JSONObject getJSONData()
    {
        JSONObject jsonData = null;
        try
        {
            jsonData = new JSONObject(body());
        }
        catch (Exception e) {}
        return jsonData;
    }

    public String body()
    {
        if (mBody != null) {
            return mBody;
        }
        mBody = "";
        try {
            if (this.mRresponse.getEntity() != null) {
                // if Use EntityUtil.toString, the chinese text will be wrong.
                InputStream in = mRresponse.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder str = new StringBuilder();
                String line = null;
                while((line = reader.readLine()) != null)
                {
                    str.append(line);
                }
                in.close();
                mBody = str.toString();
            }
        } catch (Exception e) {}
        return mBody;
    }

    @Override
    public String toString() {
        return status() + " " + body();
    }

}

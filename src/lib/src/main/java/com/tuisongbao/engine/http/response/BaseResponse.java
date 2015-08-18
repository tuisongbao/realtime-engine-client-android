package com.tuisongbao.engine.http.response;

import com.tuisongbao.engine.utils.LogUtils;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class BaseResponse {
    private static final String TAG = "TSB" + BaseResponse.class.getSimpleName();

    private HttpResponse mResponse;
    private String mBody;

    public BaseResponse(HttpResponse paramHttpResponse)
    {
        this.mResponse = paramHttpResponse;
        LogUtils.info(TAG, this.toString());
    }

    public int status()
    {
        StatusLine localStatusLine = this.mResponse.getStatusLine();
        if (localStatusLine != null) return this.mResponse.getStatusLine().getStatusCode();
        return -1;
    }

    public boolean isStatusOk()
    {
        return this.mResponse != null && status() == 200;
    }

    public JSONObject getJSONData()
    {
        JSONObject jsonData = null;
        try
        {
            jsonData = new JSONObject(body());
        }
        catch (Exception e) {
            LogUtils.error(TAG, "Body: " + body(), e);
        }
        return jsonData;
    }

    public String body()
    {
        if (mBody != null) {
            return mBody;
        }
        mBody = "";
        try {
            if (this.mResponse.getEntity() != null) {
                // if Use EntityUtil.toString, the chinese text will be wrong.
                InputStream in = mResponse.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder str = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null)
                {
                    str.append(line);
                }
                in.close();
                mBody = str.toString();
            }
        } catch (Exception e) {
            LogUtils.error(TAG, e);
        }
        return mBody;
    }

    @Override
    public String toString() {
        return status() + " " + body();
    }

}

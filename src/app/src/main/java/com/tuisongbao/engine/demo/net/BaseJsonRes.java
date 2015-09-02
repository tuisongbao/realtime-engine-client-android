package com.tuisongbao.engine.demo.net;

import android.util.Log;

import com.juns.health.net.loopj.android.http.JsonHttpResponseHandler;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.common.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by user on 15-8-31.
 */
public abstract class BaseJsonRes extends JsonHttpResponseHandler {

    public void onSuccess(JSONObject response) {
        try {
            Log.i("respose-------------", response.toString());
            String result = response.getString(Constants.Result);
            // System.out.println("返回的值" + response);
            if (result == null) {
                Utils.showLongToast(App.getInstance(), Constants.NET_ERROR);
            } else if (result.equals("1")) {
                String str_data = response.getString(Constants.Value);
                onMySuccess(str_data);
            } else {
                String str = response.getString(Constants.Info);
                Utils.showLongToast(App.getInstance(), str);
                onMyFailure();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            onMyFailure();
        }
    }

    public abstract void onMySuccess(String data);

    public abstract void onMyFailure();
}

package com.tuisongbao.engine.demo.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;

import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.common.Utils;
import com.tuisongbao.engine.demo.dialog.FlippingLoadingDialog;
import com.tuisongbao.engine.demo.net.NetClient;

import org.apache.http.message.BasicNameValuePair;

/**
 * Created by user on 15-8-31.
 */
public class BaseActivity extends Activity {
    protected Activity context;
    protected NetClient netClient;
    protected FlippingLoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        // 为了关闭时关闭全部的activity
        App.getInstance().addActivity(this);
        netClient = new NetClient(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Utils.finish(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 打开 Activity
     *
     * @param activity
     * @param cls
     * @param name
     */
    public void start_Activity(Activity activity, Class<?> cls,
                               BasicNameValuePair... name) {
        Utils.start_Activity(activity, cls, name);
    }

    /**
     * 关闭 Activity
     *
     * @param activity
     */
    public void finish(Activity activity) {
        Utils.finish(activity);
    }

    /**
     * 判断是否有网络连接
     */
    public boolean isNetworkAvailable(Context context) {
        return Utils.isNetworkAvailable(context);
    }

    public FlippingLoadingDialog getLoadingDialog(String msg) {
        if (mLoadingDialog == null){
            mLoadingDialog = new FlippingLoadingDialog(this, msg);
        }

        return mLoadingDialog;
    }
}
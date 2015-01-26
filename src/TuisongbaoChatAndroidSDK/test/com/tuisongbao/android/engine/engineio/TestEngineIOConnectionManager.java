package com.tuisongbao.android.engine.engineio;

import com.tuisongbao.android.engine.engineio.EngineIOManager;

import android.test.AndroidTestCase;

public class TestEngineIOConnectionManager extends AndroidTestCase {

    public void test() {
        EngineIOManager.getInstance().init(getContext());
//        EngineIOConnectionManager.getInstance().send("2");
        try {
            Thread.sleep(180 * 1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

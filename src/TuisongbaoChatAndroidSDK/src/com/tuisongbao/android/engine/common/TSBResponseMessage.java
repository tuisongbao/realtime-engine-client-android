package com.tuisongbao.android.engine.common;

public class TSBResponseMessage extends BaseTSBResponseMessage<String> {

    @Override
    public String parse() {
        return getData();
    }

}

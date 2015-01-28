package com.tuisongbao.android.engine.channel.message;

import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;

public class TSBSubscribeResponseMessage extends BaseTSBResponseMessage<String> {

    @Override
    public String parse() {
        return getChannel();
    }

}

package com.tuisongbao.android.engine.channel.message;

import com.tuisongbao.android.engine.common.BaseTSBResponseCallbackMessage;

public class TSBSubscribeResponseMessage extends BaseTSBResponseCallbackMessage<String> {

    @Override
    public String parse() {
        return getChannel();
    }

}

package com.tuisongbao.engine.common.entity;

import com.tuisongbao.engine.common.event.BaseEvent;

public class ResponseEvent extends BaseEvent<ResponseEventData> {
    public ResponseEvent() {
        super("engine_response");
    }
}

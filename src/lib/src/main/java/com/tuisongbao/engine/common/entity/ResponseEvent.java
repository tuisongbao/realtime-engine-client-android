package com.tuisongbao.engine.common.entity;

import com.tuisongbao.engine.common.event.BaseEvent;

/**
 * Created by root on 15-8-3.
 */
public class ResponseEvent extends BaseEvent<ResponseEventData> {
    public ResponseEvent() {
        super("engine_response");
    }
}

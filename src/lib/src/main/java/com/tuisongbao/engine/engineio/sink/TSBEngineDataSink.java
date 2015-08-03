package com.tuisongbao.engine.engineio.sink;

import com.github.nkzawa.emitter.Emitter;
import com.google.gson.Gson;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.IEventHandler;
import com.tuisongbao.engine.util.StrUtil;

/**
 * A data sink that sends new messages of specific types to listeners.
 *
 */
public class TSBEngineDataSink extends BaseEngineDataSink {
    private TSBEngine mEngine;

    public TSBEngineDataSink(TSBEngine engine) {
        mEngine = engine;
    }

    public void setHandler(final BaseEvent event, final IEventHandler response) {
        bindOnce(String.valueOf(event.getId()), new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                response.onResponse(event, (RawEvent) args[0]);
            }
        });
    }

    @Override
    protected void propagateEvent(String event) {
        RawEvent rawEvent = new Gson().fromJson(event, RawEvent.class);
        String eventName = rawEvent.getName();
        if (!StrUtil.isEmpty(rawEvent.getChannel()) || Protocol.isChannelEvent(eventName)) {
            mEngine.getChannelManager().trigger(eventName, rawEvent);
        } else if (Protocol.isServerResponseEvent(eventName)) {
            ResponseEventData data = new Gson().fromJson(rawEvent.getData(), ResponseEventData.class);
            trigger(String.valueOf(data.getTo()), rawEvent);
        } else if (Protocol.isChatEvent(eventName)) {
            mEngine.getChatManager().trigger(eventName, rawEvent);
        }
    }
}

package com.tuisongbao.engine.engineio.sink;

import com.github.nkzawa.emitter.Emitter;
import com.google.gson.Gson;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.common.entity.Event;
import com.tuisongbao.engine.common.event.ITSBResponseEvent;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.entity.ResponseEventData;
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

    public void setHandler(final Event event, final ITSBResponseEvent response) {
        bindOnce(String.valueOf(event.getId()), new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                response.callback(event, (ResponseEventData)args[0]);
            }
        });
    }

    @Override
    protected void propagateEvent(Event event) {
        if (event == null) return;

        String eventName = event.getName();
        if (!StrUtil.isEmpty(event.getChannel()) || Protocol.isChannelEvent(eventName)) {
            mEngine.channelManager.trigger(eventName, event);
        } else if (Protocol.isServerResponseEvent(eventName)) {
            ResponseEventData data = new Gson().fromJson(event.getData(), ResponseEventData.class);
            trigger(String.valueOf(data.getTo()), data);
        } else if (Protocol.isChatEvent(eventName)) {
            mEngine.chatManager.trigger(eventName, event);
        }
    }
}

package com.tuisongbao.android.engine.common;

import org.json.JSONObject;

import com.tuisongbao.android.engine.common.TSBEngineResponseToServerRequestMessage.ResponseToServerData;

/**
 * I‘s used to response server request message, such as "engine_chat:message:get", "engine_chat:message:new" and so on
 *
 */
public class TSBEngineResponseToServerRequestMessage extends BaseTSBRequestMessage<ResponseToServerData> {
    
    public static final String NAME = "engine_response";

    public TSBEngineResponseToServerRequestMessage(long serverRequestId, boolean isOk) {
        super(NAME);
        ResponseToServerData data = new ResponseToServerData();
        data.setTo(serverRequestId);
        data.setOk(isOk);
        setData(data);
    }
    
    public void setResult(JSONObject result) {
        getData().setResult(result);
    }

    public class ResponseToServerData {
        private long to;
        private boolean ok;
        private JSONObject result;

        public long getTo() {
            return to;
        }

        public void setTo(long to) {
            this.to = to;
        }

        public boolean getOk() {
            return ok;
        }

        public void setOk(boolean ok) {
            this.ok = ok;
        }

        public JSONObject getResult() {
            return result;
        }

        public void setResult(JSONObject result) {
            this.result = result;
        }
    }
}

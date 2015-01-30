package com.tuisongbao.android.engine.common;

/**
 * It's used to not need response data request.
 *
 */
public class TSBResponseMessage extends BaseTSBResponseMessage<String> {

    @Override
    public String parse() {
        return getData();
    }

}

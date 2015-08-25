package com.tuisongbao.engine.common.entity;

import com.tuisongbao.engine.common.EventEmitter;

public class Entity extends EventEmitter {
    /**
     * 拓展项，用于挂载你需要的字段，比如会话相关联的群组的名称。
     * 不支持序列化，开发者需自行维护。如果有 {@code serialize} 方法， 序列化过程中时会忽略该字段。
     */
    transient public Object extension;
}

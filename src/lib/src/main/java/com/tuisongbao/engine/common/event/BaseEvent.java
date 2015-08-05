package com.tuisongbao.engine.common.event;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseEvent<T>{
    protected long id;
    protected String channel;
    protected String name;
    protected T data;

    transient protected List<String> serializeFields;

    public BaseEvent() {};

    public BaseEvent(String name) {
        this.name = name;

        serializeFields = new ArrayList<>();
        serializeFields.add("id");
        serializeFields.add("channel");
        serializeFields.add("name");
        serializeFields.add("data");
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public T getData() {
        return this.data;
    }

    protected GsonBuilder getSerializerWithExclusionStrategy() {
        return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                if (serializeFields.indexOf(f.getName()) >= 0) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        });
    }

    protected Gson getSerializer() {
        // FIXME: 15-8-5 If event wants customizing the serialized fields, the size of serializeFields is over 4, This is a trick, may fix later.
        if (serializeFields.size() > 4) {
            return getSerializerWithExclusionStrategy().create();
        } else {
            return new Gson();
        }
    }

    public String serialize() {
        return getSerializer().toJson(this);
    }
}

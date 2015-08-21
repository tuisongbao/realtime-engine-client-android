package com.tuisongbao.engine.chat.message.content;

import android.location.Location;

import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageContent;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageLocationEntity;

public class ChatMessageLocationContent extends ChatMessageContent {
    public ChatMessageLocationContent() {

    }

    public ChatMessageLocationContent(Location location) {
        setType(ChatMessage.TYPE.LOCATION);

        ChatMessageLocationEntity entity = new ChatMessageLocationEntity(location);
        setLocation(entity);
    }

    public ChatMessageLocationContent(double latitude, double longitude, String pointOfInterest) {
        setType(ChatMessage.TYPE.LOCATION);

        ChatMessageLocationEntity entity = new ChatMessageLocationEntity();
        entity.setLat(latitude);
        entity.setLng(longitude);
        entity.setPoi(pointOfInterest);
        setLocation(entity);
    }

    public double getLatitude() {
        return getLocation().getLat();
    }

    public double getLongitude() {
        return getLocation().getLng();
    }

    public String getPointOfInterest() {
        return getLocation().getPoi();
    }
}

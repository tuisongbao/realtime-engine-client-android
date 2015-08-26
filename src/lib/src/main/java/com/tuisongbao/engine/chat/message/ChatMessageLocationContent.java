package com.tuisongbao.engine.chat.message;

import android.location.Location;

/**
 * <STRONG>地理位置消息内容</STRONG>
 */
public class ChatMessageLocationContent extends ChatMessageContent {
    public ChatMessageLocationContent() {

    }

    /**
     * 根据给定的地理位置初始化消息内容
     *
     * @param location  地理位置
     */
    public ChatMessageLocationContent(Location location) {
        setType(ChatMessage.TYPE.LOCATION);

        ChatMessageLocationEntity entity = new ChatMessageLocationEntity(location);
        setLocation(entity);
    }

    /**
     * 根据给定的地理位置的具体信息初始化消息内容
     *
     * @param latitude          纬度
     * @param longitude         经度
     * @param pointOfInterest   兴趣点
     */
    public ChatMessageLocationContent(double latitude, double longitude, String pointOfInterest) {
        setType(ChatMessage.TYPE.LOCATION);

        ChatMessageLocationEntity entity = new ChatMessageLocationEntity();
        entity.setLat(latitude);
        entity.setLng(longitude);
        entity.setPoi(pointOfInterest);
        setLocation(entity);
    }

    /**
     * 获取纬度信息
     *
     * @return  纬度
     */
    public double getLatitude() {
        return getLocation().getLat();
    }

    /**
     * 获取经度信息
     *
     * @return  经度
     */
    public double getLongitude() {
        return getLocation().getLng();
    }

    /**
     * 获取兴趣点
     *
     * @return  兴趣点
     */
    public String getPointOfInterest() {
        return getLocation().getPoi();
    }
}

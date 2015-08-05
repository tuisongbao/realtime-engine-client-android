package com.tuisongbao.engine.chat.message.entity.content;

/**
 * Created by root on 15-8-5.
 */
public class ChatMessageFileContent {
    private String key;
    private String url;
    private String thumbUrl;
    private String name;
    private String mimeType;
    private String etag;
    private double size;

    private int width;
    private int height;

    private double duration;

    /**
     * file's absolute path in device, for image, voice and video
     */
    transient String originFilePath;
    transient String thumbnailPath;

    public void setFilePath(String path) {
        originFilePath = path;
    }

    public String getFilePath() {
        return originFilePath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public ChatMessageFileContent setKey(String key) {
        this.key = key;
        return this;
    }

    public String getKey() {
        return key;
    }

    public ChatMessageFileContent setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public ChatMessageFileContent setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public String getMimeType() {
        return mimeType;
    }

    public ChatMessageFileContent setEtag(String etag) {
        this.etag = etag;
        return this;
    }

    public String getEtag() {
        return etag;
    }

    public ChatMessageFileContent setSize(double size) {
        this.size = size;
        return this;
    }

    public double getSize() {
        return size;
    }

    public ChatMessageFileContent setDuration(double duration) {
        this.duration = duration;
        return this;
    }

    public double getDuration() {
        return duration;
    }

    public ChatMessageFileContent setFrame(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public ChatMessageFileContent setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public ChatMessageFileContent setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
        return this;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }
}

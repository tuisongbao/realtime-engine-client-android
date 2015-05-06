package com.tuisongbao.android.engine.chat.entity;

import android.os.Parcel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TSBImageMessageBody extends TSBMessageBody {
    public static final String KEY = "key";
    public static final String DOWNLOAD_URL = "downloadUrl";
    public static final String LOCAL_PATH = "localPath";
    public static final String ETAG = "etag";
    public static final String NAME = "name";
    public static final String SIZE = "size";
    public static final String MIME_TYPE = "mimeType";
    public static final String IMAGE_INFO = "imageInfo";

    public static final String IMAGE_INFO_WIDTH = "width";
    public static final String IMAGE_INFO_HEIGHT = "height";

    private JsonObject file;

    public TSBImageMessageBody() {
        super(TSBMessage.TYPE.IMAGE);
        file = new JsonObject();
    }

    public JsonObject getFile() {
        return file;
    }

    public void setFile(JsonObject file) {
        this.file = file;
    }

    public void setLocalPath(String path) {
        file.addProperty(LOCAL_PATH, path);
    }

    public String getLocalPath() {
        JsonElement localPathElement = file.get(LOCAL_PATH);
        if (localPathElement != null) {
            return localPathElement.getAsString();
        }
        return "";
    }

    public String getDownloadUrl() {
        JsonElement downloadUrlElement = file.get(DOWNLOAD_URL);
        if (downloadUrlElement != null) {
            return downloadUrlElement.getAsString();
        }
        return "";
    }

    public void setDownloadUrl(String downloadUrl) {
        file.addProperty(DOWNLOAD_URL, downloadUrl);
    }

    public String getName() {
        return file.get(NAME).getAsString();
    }

    public String getKey() {
        return file.get(KEY).getAsString();
    }

    public void setKey(String key) {
        file.addProperty(KEY, key);
    }

    public String getEtag() {
        return file.get(ETAG).getAsString();
    }

    public String getSize() {
        return file.get(SIZE).getAsString();
    }

    public void setSize(String size) {
        file.addProperty(SIZE, size);
    }

    public String getMimeType() {
        return file.get(MIME_TYPE).getAsString();
    }

    public void setMimeType(String mimeType) {
        file.addProperty(MIME_TYPE, mimeType);
    }

    public JsonObject getImageInfo() {
        return file.get(IMAGE_INFO).getAsJsonObject();
    }

    public void setImageInfo(JsonObject imageInfo) {
        file.add(IMAGE_INFO, imageInfo);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        // empty
    }

    @Override
    public String toString() {
        return String.format("TSBTextMessageBody[file: %s, type: %s]", file.toString(), type.getName());
    }
}

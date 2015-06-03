package com.tuisongbao.android.engine.chat.entity;

import android.os.Parcel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tuisongbao.android.engine.chat.entity.TSBMessage.TYPE;

public abstract class TSBMediaMessageBody extends TSBMessageBody {

    public static final String KEY = "key";
    public static final String DOWNLOAD_URL = "downloadUrl";
    public static final String LOCAL_PATH = "localPath";
    public static final String ETAG = "etag";
    public static final String NAME = "name";
    public static final String SIZE = "size";
    public static final String MIME_TYPE = "mimeType";

    protected JsonObject file;

    public TSBMediaMessageBody(TYPE type) {
        super(type);
        file = new JsonObject();
    }

    abstract public JsonObject getMediaInfo();
    abstract public void setMediaInfo(JsonObject infoObject);

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

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    public void setFile(JsonObject file) {
        this.file = file;
    }

}

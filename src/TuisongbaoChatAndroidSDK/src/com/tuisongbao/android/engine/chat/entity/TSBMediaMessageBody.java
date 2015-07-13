package com.tuisongbao.android.engine.chat.entity;

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

    // According to the rule of Gson, this will be serialized to
    // file:
    //     width:
    //     height:
    //     ....
    // this field is used to generate the `file` key
    /**
     * According to the rule of Gson, this will be serialized to
     * file:
     *     width:
     *     height:
     *     ....
     * this field is used by generating the key `file`
     *
     * Why use JsonObject instead of JSONObject, see also TSBMessageBody
     */
    protected JsonObject file;

    public TSBMediaMessageBody(TYPE type) {
        super(type);
        file = new JsonObject();
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

    public void setFile(JsonObject file) {
        this.file = file;
    }

    public JsonObject getFile() {
        return file;
    }
}

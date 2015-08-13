package com.tuisongbao.engine.chat.message.entity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageEventContent;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageFileContent;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.utils.DownloadUtils;
import com.tuisongbao.engine.utils.StrUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * 消息主体内容
 *
 * <P>
 *     提供了多媒体消息的资源下载 {@link #downloadingOriginal}
 */
public class ChatMessageContent {
    transient private final String TAG = "TSB" + ChatMessage.class.getSimpleName();

    private ChatMessage.TYPE type;
    private String text;
    private ChatMessageFileContent file;
    private ChatMessageEventContent event;
    private JsonObject extra;

    transient private boolean downloadingThumbnail = false;
    transient private boolean downloadingOriginal = false;
    transient private Engine mEngine;

    public ChatMessageContent() {
        type = ChatMessage.TYPE.TEXT;
    }

    public void setEngine(Engine engine) {
        mEngine = engine;
    }

    public ChatMessage.TYPE getType() {
        return type;
    }

    public void setType(ChatMessage.TYPE type) {
        this.type = type;
    }

    public void setFile(ChatMessageFileContent file) {
        this.file = file;
    }

    public ChatMessageFileContent getFile() {
        if (file == null) {
            file = new ChatMessageFileContent();
        }
        return file;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setExtra(JSONObject extra) {
        this.extra = new Gson().fromJson(extra.toString(), JsonObject.class);
    }

    public void setExtra(JsonObject extra) {
        this.extra = extra;
    }

    public JsonElement getExtra() {
        return extra;
    }

    public ChatMessageEventContent getEvent() {
        if (event == null) {
            event = new ChatMessageEventContent();
        }
        return event;
    }

    public void setEvent(ChatMessageEventContent event) {
        this.event = event;
    }

    public void setFilePath(String path) {
        if (file == null) {
            file = new ChatMessageFileContent();
        }
        file.setFilePath(path);
    }

    /**
     * 下载资源文件并存储在本地
     * <P>
     *     该方法<STRONG>可以</STRONG>重复调用 ，SDK 会自行检测是否有缓存，不存在时会重新下载。
     *
     * @param filePathCallback 路径回调处理方法，该方法接收一个参数，表示文件的绝对路径
     * @param progressCallback 进度回调处理方法，该方法接收一个参数，类型为 {@code int}， 表示下载进度
     */
    public void download(final EngineCallback<String> filePathCallback, final ProgressCallback progressCallback) {
        downloadResource(true, filePathCallback, progressCallback);
    }

    /**
     * 下载缩略图，适用于图片和视频消息
     *
     * <P>
     *     该方法<STRONG>可以</STRONG>重复调用 ，SDK 会自行检测是否有缓存，不存在时会重新下载。
     *
     * @param filePathCallback 路径回调处理方法，该方法接收一个参数，表示文件的绝对路径
     * @param progressCallback 进度回调处理方法，该方法接收一个参数，类型为 {@code int}， 表示下载进度
     */
    public void downloadThumb(final EngineCallback<String> filePathCallback, final ProgressCallback progressCallback) {
        downloadResource(false, filePathCallback, progressCallback);
    }

    public boolean generateThumbnail(int maxWidth) {
        if (getType() != ChatMessage.TYPE.IMAGE) {
            return false;
        }

        String thumbnailPath = getFile().getThumbnailPath();
        if (isFileExists(thumbnailPath)) {
            return false;
        }

        // Create thumbnail bitmap
        String filePath = getFile().getFilePath();
        Bitmap image = BitmapFactory.decodeFile(filePath);
        float bitmapRatio = (float)image.getWidth() / (float) image.getHeight();

        int width = Math.min(image.getWidth(), maxWidth);
        int height = (int) (width / bitmapRatio);
        Bitmap thumbnail = Bitmap.createScaledBitmap(image, width, height, true);

        // Save thumbnail
        String fileName = StrUtils.getTimestampStringOnlyContainNumber(new Date()) + ".jpg";
        FileOutputStream out = null;
        try {
            File file = DownloadUtils.getOutputFile(fileName, getType().getName());
            out = new FileOutputStream(file.getAbsolutePath());
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, out);

            // Update thumbnail path in message
            getFile().setThumbnailPath(file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                LogUtil.error(TAG, e);
            }
        }
        return false;
    }

    private ResponseError permissionCheck() {
        if (!isMediaMessage()) {
            ResponseError error = new ResponseError();
            error.setMessage("No resource to download, this is not a media message.");
            return error;
        }
        return null;
    }

    private boolean isFileExists(String filePath) {
        if (StrUtils.isEmpty(filePath)) {
            return false;
        }
        File fileTest = new File(filePath);
        if (!fileTest.exists()) {
            LogUtil.warn(TAG, "Local file at " + filePath + " is no longer exists, need to download again");
            return false;
        }
        return true;
    }

    private void downloadResource(final boolean isOriginal, final EngineCallback callback, final ProgressCallback progressCallback) {
        ResponseError error = permissionCheck();
        if (error != null) {
            callback.onError(error);
            return;
        }

        String filePath;
        String downloadUrl;
        final boolean isDownloading;
        ChatMessageFileContent file = getFile();
        if (isOriginal) {
            filePath = file.getFilePath();
            downloadUrl = file.getUrl();
            isDownloading = downloadingOriginal;
        } else {
            filePath = file.getThumbnailPath();
            downloadUrl = file.getThumbUrl();
            isDownloading = downloadingThumbnail;
        }

        if (isDownloading) {
            error = new ResponseError();
            error.setMessage("Download is in process...");
            callback.onError(error);
            return;
        }

        if (isFileExists(filePath)) {
            callback.onSuccess(filePath);
            return;
        }

        ChatMessage.TYPE type = getType();
        // Download thumbnail of video
        if (getType() == ChatMessage.TYPE.VIDEO && !isOriginal) {
            type = ChatMessage.TYPE.IMAGE;
        }
        DownloadUtils.downloadResourceIntoLocal(downloadUrl, type, new EngineCallback<String>() {

            @Override
            public void onSuccess(String filePath) {
                updateFilePath(isOriginal, filePath);
                callback.onSuccess(filePath);
            }

            @Override
            public void onError(ResponseError error) {
                callback.onError(error);
            }
        }, progressCallback);
    }

    @Override
    public String toString() {
        return String.format("ChatMessageContent[url:%s, thumbUrl: %s, path:%s, thumbPath: %s]"
                , getFile().getUrl(), getFile().getThumbUrl(), getFile().getFilePath(), getFile().getThumbnailPath());
    }


    private void updateFilePath(boolean isOriginal, String filePath) {
        String url;
        if (isOriginal) {
            downloadingOriginal = false;
            getFile().setFilePath(filePath);
            url = getFile().getUrl();
        } else {
            downloadingThumbnail = false;
            getFile().setThumbnailPath(filePath);
            url = getFile().getThumbUrl();
        }
        if (mEngine.getChatManager().isCacheEnabled()) {
            ChatConversationDataSource dataSource = new ChatConversationDataSource(Engine.getContext(), mEngine);
            dataSource.open();
            dataSource.updateMessageFilePath(isOriginal, url, filePath);
            dataSource.close();
        }
    }

    private boolean isMediaMessage() {
        ChatMessage.TYPE contentType = getType();
        return contentType == ChatMessage.TYPE.IMAGE || contentType == ChatMessage.TYPE.VOICE || contentType == ChatMessage.TYPE.VIDEO;
    }
}

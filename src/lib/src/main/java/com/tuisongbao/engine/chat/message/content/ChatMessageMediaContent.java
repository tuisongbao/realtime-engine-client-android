package com.tuisongbao.engine.chat.message.content;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageContent;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageFileEntity;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.download.DownloadManager;
import com.tuisongbao.engine.download.DownloadTask;
import com.tuisongbao.engine.utils.FileUtils;
import com.tuisongbao.engine.utils.LogUtils;
import com.tuisongbao.engine.utils.StrUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class ChatMessageMediaContent extends ChatMessageContent {
    private static final String TAG = "TSB" + ChatMessageMediaContent.class.getSimpleName();

    transient private boolean downloadingThumbnail = false;
    transient private boolean downloadingOriginal = false;

    public void setFilePath(String path) {
        if (file == null) {
            file = new ChatMessageFileEntity();
        }
        file.setFilePath(path);
    }

    public String getFilePath() {
        if (file == null) {
            return null;
        } else {
            return file.getFilePath();
        }
    }

    public String getFileUrl() {
        if (file == null) {
            return null;
        } else {
            return file.getUrl();
        }
    }

    public String getThumbnailPath() {
        if (file == null) {
            return null;
        } else {
            return file.getThumbnailPath();
        }
    }

    public String getThumbnailUrl() {
        if (file == null) {
            return null;
        } else {
            return file.getThumbUrl();
        }
    }

    /**
     * 下载源文件并存储在本地
     *
     * <P>
     *     该方法<STRONG>可以</STRONG>重复调用 ，SDK 会自行检测是否有缓存，不存在时会重新下载。
     *
     * @param filePathCallback 路径回调处理方法，该方法接收一个参数，表示文件的绝对路径
     * @param progressCallback 进度回调处理方法，该方法接收一个参数，类型为 {@code int}， 表示下载进度
     */
    public DownloadTask download(final EngineCallback<String> filePathCallback, final ProgressCallback progressCallback) {
        return downloadResource(true, filePathCallback, progressCallback);
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
    public DownloadTask downloadThumb(final EngineCallback<String> filePathCallback, final ProgressCallback progressCallback) {
        return downloadResource(false, filePathCallback, progressCallback);
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
            File file = FileUtils.getOutputFile("/tuisongbao/" + getType().getName() + "/" + fileName);
            if (file == null) {
                // If thumbnail can not be created, only can count on the downloading thumbnail.....
                return false;
            }
            out = new FileOutputStream(file.getAbsolutePath());
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, out);

            // Update thumbnail path in message
            getFile().setThumbnailPath(file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            LogUtils.error(TAG, e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                LogUtils.error(TAG, e);
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
            LogUtils.warn(TAG, "Local file at " + filePath + " is no longer exists, need to download again");
            return false;
        }
        return true;
    }

    private DownloadTask downloadResource(final boolean isOriginal, final EngineCallback<String> callback,
                                          final ProgressCallback progressCallback) {
        ResponseError error = permissionCheck();
        if (error != null) {
            callback.onError(error);
            return null;
        }

        String filePath;
        String downloadUrl;
        final boolean isDownloading;
        if (isOriginal) {
            filePath = getFilePath();
            downloadUrl = getFileUrl();
            isDownloading = downloadingOriginal;
        } else {
            filePath = getThumbnailPath();
            downloadUrl = getThumbnailUrl();
            isDownloading = downloadingThumbnail;
        }

        if (isDownloading) {
            error = new ResponseError();
            error.setMessage("Download is in process...");
            callback.onError(error);
            return null;
        }

        if (isFileExists(filePath)) {
            callback.onSuccess(filePath);
            return null;
        }

        final String newFilePath = getFilePathToStore(isOriginal);
        DownloadTask task = new DownloadTask(downloadUrl, newFilePath, new EngineCallback<String>() {
            @Override
            public void onSuccess(String absolutePath) {
                updateFilePath(isOriginal, absolutePath);
                callback.onSuccess(absolutePath);
            }

            @Override
            public void onError(ResponseError error) {
                callback.onError(error);
            }
        }, progressCallback);
        DownloadManager.getInstance().start(task);

        return task;
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

    private String getFilePathToStore(boolean isOriginal) {
        String outputFileName = StrUtils.getTimestampStringOnlyContainNumber(new Date());
        String fileSuffix = "";
        if (type == ChatMessage.TYPE.IMAGE) {
            fileSuffix = ".png";
        } else if (type == ChatMessage.TYPE.VOICE) {
            fileSuffix = ".wav";
        } else if (type == ChatMessage.TYPE.VIDEO && !isOriginal) {
            // Thumbnail of video
            fileSuffix = ".png";
        } else if (type == ChatMessage.TYPE.VIDEO) {
            fileSuffix = ".mp4";
        }

        String folderName = type.getName();
        // Store video's thumbnail under image directory
        if (type == ChatMessage.TYPE.VOICE && !isOriginal) {
            folderName = ChatMessage.TYPE.IMAGE.getName();
        }
        return "/tuisongbao/" + folderName + "/" + outputFileName + fileSuffix;
    }
}

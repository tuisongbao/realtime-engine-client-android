package com.tuisongbao.engine.chat.message;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.utils.FileUtils;
import com.tuisongbao.engine.utils.LogUtils;
import com.tuisongbao.engine.utils.StrUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * <STRONG>图片消息内容</STRONG>
 */
public class ChatMessageImageContent extends ChatMessageMediaContent {
    private static final String TAG = "TSB" + ChatMessageImageContent.class.getSimpleName();
    public ChatMessageImageContent() {
        setType(ChatMessage.TYPE.IMAGE);
    }

    /**
     * 获取图片的宽度，单位是像素
     *
     * @return  图片的宽度
     */
    public int getWidth() {
        return getFile().getWidth();
    }

    /**
     * 获取图片的高度，单位是像素
     *
     * @return  图片的高度
     */
    public int getHeight() {
        return getFile().getHeight();
    }

    /**
     * 根据 {@link #getFilePath()} 提供的路径，生成缩略图，并更新缩略图路径
     *
     * <P>
     *     在调用 {@link com.tuisongbao.engine.chat.conversation.ChatConversation#sendMessage(ChatMessageContent, EngineCallback, ProgressCallback)}
     *     发送图片时，会默认调用该方法，宽度为 200。
     * </P>
     *
     * @param maxWidth  缩略图的宽度
     * @return  成功时，返回缩略图的绝对路径，否则返回 {@code null}
     */
    public String generateThumbnail(int maxWidth) {
        if (getType() != ChatMessage.TYPE.IMAGE) {
            return null;
        }

        String thumbnailPath = getFile().getThumbnailPath();
        if (FileUtils.isFileExists(thumbnailPath)) {
            return null;
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
            File file = FileUtils.load("/tuisongbao/" + getType().getName() + "/" + fileName);
            if (file == null) {
                // If thumbnail can not be created, only can count on the downloading thumbnail.....
                return null;
            }
            String thumbnailFilePath = file.getAbsolutePath();
            out = new FileOutputStream(thumbnailFilePath);
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, out);

            // Update thumbnail path in message
            getFile().setThumbnailPath(thumbnailFilePath);
            return thumbnailFilePath;
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
        return null;
    }
}

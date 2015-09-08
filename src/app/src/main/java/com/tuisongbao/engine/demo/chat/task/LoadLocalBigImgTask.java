package com.tuisongbao.engine.demo.chat.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.chat.utils.ImageCache;
import com.tuisongbao.engine.demo.widght.TouchImageView.TouchImageView;

/**
 * Created by user on 15-9-1.
 */
public class LoadLocalBigImgTask extends AsyncTask<Void, Void, Bitmap> {

    private ProgressBar pb;
    private TouchImageView photoView;
    private String path;
    private int width;
    private int height;
    private Context context;

    public LoadLocalBigImgTask(Context context, String path,
                               TouchImageView photoView, ProgressBar pb, int width, int height) {
        this.context = context;
        this.path = path;
        this.photoView = photoView;
        this.pb = pb;
        this.width = width;
        this.height = height;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        int degree = 0;
        if (degree != 0) {
            pb.setVisibility(View.VISIBLE);
            photoView.setVisibility(View.INVISIBLE);
        } else {
            pb.setVisibility(View.INVISIBLE);
            photoView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        pb.setVisibility(View.INVISIBLE);
        photoView.setVisibility(View.VISIBLE);
        if (result != null)
            ImageCache.getInstance().put(path, result);
        else
            result = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.default_image);
        photoView.setImageBitmap(result);
    }
}

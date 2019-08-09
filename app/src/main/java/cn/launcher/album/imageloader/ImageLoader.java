package cn.launcher.album.imageloader;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

/**
 * Created by jiaojie.jia on 2017/6/25.
 */

public class ImageLoader {

    private ImageLoaderProxy mProxy;

    private static ImageLoader mInstance;

    private ImageLoader() {
    }

    public static ImageLoader getInstance() {
        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader();
                }
            }
        }
        return mInstance;
    }

    public void setProxy(ImageLoaderProxy proxy) {
        mProxy = proxy;
    }

    public void loadImage(Context context, String url, ImageView imageView) {
        // android.util.Log.d("wlDebug", "url = " + url);
//        mProxy.loadImage(context, url, imageView);
        loadGalleryImage(context,url,imageView);
    }

    public static final String FILE_PROTOCAL = "file://";

    public void loadGalleryImage(Context context, String url, final ImageView imageView) {
        Uri uri = Uri.parse(FILE_PROTOCAL + url);
        Glide.with(context)
                .load(uri)
                .into(imageView);
    }
}

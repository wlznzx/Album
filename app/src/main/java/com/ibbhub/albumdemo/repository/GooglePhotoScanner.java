package com.ibbhub.albumdemo.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.SparseArray;


import com.ibbhub.albumdemo.application.MyApplication;
import com.ibbhub.albumdemo.bean.AlbumEntry;
import com.ibbhub.albumdemo.bean.PhotoEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Google相册扫描器
 * Created by jiaojie.jia on 2017/3/15.
 */

public class GooglePhotoScanner {

    private static final int MIN_SIZE = 1024 * 10;              // 最小文件限制

    private static List<PhotoEntry> mAllPhotos = new ArrayList<>();
    private static List<PhotoEntry> mCameraPhotos = new ArrayList<>();
    private static LinkedHashMap<String, List<PhotoEntry>> mSectionsOfMonth = new LinkedHashMap<>();
    private static LinkedHashMap<String, List<PhotoEntry>> mSectionsOfDay = new LinkedHashMap<>();

    private static List<AlbumEntry> albumsSorted = new ArrayList<>();

    private static final String cameraFolder = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            .getAbsolutePath() + "/" + "Camera/";

    private static Calendar calendar = Calendar.getInstance();

    private static String[] WEEKS = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
    private static String[] MONTHS = {"01月", "02月", "03月", "04月", "05月", "06月", "07月", "08月", "09月", "10月", "11月", "12月"};

    //查询字段
    private static String[] projection = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.ORIENTATION,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.LONGITUDE,
            MediaStore.Images.Media.LATITUDE};

    public static void loadGalleryPhotosAlbums() {
        albumsSorted.clear();
        //获取ContentResolver
        ContentResolver contentResolver = MyApplication.getContext().getContentResolver();
        // 查询范围
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        // 条件
        String selection = MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=?";
        // 条件值
        String[] selectionArgs = {"image/jpeg", "image/png", "image/webp,image/pjpeg"};
        // 排序
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";

        Cursor cursor = null;
        try {
            cursor = MediaStore.Images.Media.query(contentResolver,
                    uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null) {

                Integer cameraAlbumId = null;
                long currentTime = System.currentTimeMillis();
                SparseArray<AlbumEntry> albums = new SparseArray<>();

                int imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                int bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int dateColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                int orientationColumn = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
                int imageSizeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);
                int widthColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.WIDTH);
                int heightColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.HEIGHT);
                int dateModifiedColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED);
                int longitudeColumn = cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE);
                int latitudeColumn = cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE);

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(imageIdColumn);
                    int bucketId = cursor.getInt(bucketIdColumn);
                    String bucketName = cursor.getString(bucketNameColumn);
                    String path = cursor.getString(dataColumn);

                    long takendate = cursor.getLong(dateColumn);
                    int orientation = cursor.getInt(orientationColumn);
                    int size = cursor.getInt(imageSizeColumn);
                    int width = cursor.getInt(widthColumn);
                    int height = cursor.getInt(heightColumn);
                    long modified = cursor.getInt(dateModifiedColumn);
                    double longitude = cursor.getDouble(longitudeColumn);
                    double latitude = cursor.getDouble(latitudeColumn);

                    if (TextUtils.isEmpty(path) || size < MIN_SIZE) {
                        continue;
                    }

                    // android.util.Log.d("wlDebug", "path = " + path + " bucketId = " + bucketId);
                    PhotoEntry photoEntry = new PhotoEntry(id, bucketId, bucketName, path, width, height, size, latitude, longitude, 0, orientation, takendate, modified);
                    mAllPhotos.add(photoEntry);
                    AlbumEntry albumEntry = albums.get(bucketId);
                    if (albumEntry == null) {
                        albumEntry = new AlbumEntry(bucketId, bucketName, photoEntry);
                        albums.put(bucketId, albumEntry);
                        if (cameraAlbumId == null && path.startsWith(cameraFolder)) {
                            albumEntry.setCamera(true);
                            if (albumsSorted.size() >= 2) {
                                albumsSorted.add(0, albumEntry);
                            } else {
                                albumsSorted.add(albumEntry);
                            }
                            cameraAlbumId = bucketId;
                        } else {
                            albumsSorted.add(albumEntry);
                        }
                    }
                    if (cameraAlbumId != null && bucketId == cameraAlbumId) {
                        mCameraPhotos.add(photoEntry);
                    }
                    albumEntry.addPhoto(photoEntry);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<PhotoEntry> getAllPhotos() {
        return mAllPhotos;
    }

    public static List<PhotoEntry> getCameraPhotos() {
        return mCameraPhotos;
    }


    public static LinkedHashMap<String, List<PhotoEntry>> getOtherSection(int folderPosition) {
        LinkedHashMap<String, List<PhotoEntry>> data = new LinkedHashMap<>();
        data.put(albumsSorted.get(folderPosition).getBucketName(), albumsSorted.get(folderPosition).getList());
        return data;
    }

    /**
     * 获取文件夹列表
     *
     * @return
     */
    public static List<AlbumEntry> getImageFloders() {
        return albumsSorted;
    }


    /**
     * 清理数据
     */
    public static void clear() {
        if (albumsSorted != null)
            albumsSorted.clear();
        if (mSectionsOfMonth != null)
            mSectionsOfMonth.clear();
        if (mSectionsOfDay != null)
            mSectionsOfDay.clear();
        if (mAllPhotos != null)
            mAllPhotos.clear();
        if (mCameraPhotos != null)
            mCameraPhotos.clear();
    }
}

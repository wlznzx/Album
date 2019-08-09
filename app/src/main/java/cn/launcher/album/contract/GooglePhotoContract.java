package cn.launcher.album.contract;


import cn.launcher.album.bean.AlbumEntry;
import cn.launcher.album.bean.PhotoEntry;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by jiaojie.jia on 2017/3/15.
 */

public interface GooglePhotoContract {

    interface View {

        void fullPreviewData(List<PhotoEntry> allPhotos);

        void fullFolders(List<AlbumEntry> folders);

        void updateSelectedSize(int size, int min, int max);

        void updateBtnStatus(boolean enable);

        void onSelectFull();

        void setSelectResult(List<PhotoEntry> photoItems);

        void checkFolderListStatus();

        void showProgressDialog(int max);

        void showContinueDialog();
        
        void dismissProgressDialog();

        void incrementProgress();

        void photoPreviewAdd();

        void photoPreviewRemove(int position);

        void scrollToImage(int dataPosition);

    }
}

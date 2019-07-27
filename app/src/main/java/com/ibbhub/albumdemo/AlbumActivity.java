package com.ibbhub.albumdemo;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.ibbhub.album.AlbumFragment;
import com.ibbhub.albumdemo.adapter.PhotoFoldersAdapter;
import com.ibbhub.albumdemo.application.MyApplication;
import com.ibbhub.albumdemo.bean.AlbumEntry;
import com.ibbhub.albumdemo.bean.PhotoEntry;
import com.ibbhub.albumdemo.contract.GooglePhotoContract;
import com.ibbhub.albumdemo.repository.GooglePhotoScanner;

import java.util.List;

public class AlbumActivity extends AppCompatActivity implements GooglePhotoContract.View {

    private LinearLayout mDesignBottomSheet;
    private BottomSheetBehavior mBottomSheetBehavior;
    private PhotoFoldersAdapter mFoldersAdapter;

    private RecyclerView mRvFiledir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        MyApplication.setApplication(getApplication());
        initView();

        new Thread(new Runnable() {
            @Override
            public void run() {
                android.util.Log.d("wlDebug", "GooglePhotoScanner.");
                GooglePhotoScanner.loadGalleryPhotosAlbums();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFoldersAdapter.setData(GooglePhotoScanner.getImageFloders());
                        mFoldersAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private MenuItem chooseMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        chooseMenu = menu.findItem(R.id.action_choose);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_choose) {
            if (item.getTitle().equals("选择")) {
                albumFragment.enterChoose();
            } else {
                albumFragment.cancelChoose();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private AlbumFragment albumFragment;

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        albumFragment = (AlbumFragment) getSupportFragmentManager().findFragmentByTag("album");
        if (albumFragment == null) {
            albumFragment = new MyAlbumFragment();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.flParent, albumFragment);
        ft.commit();
        mRvFiledir = findViewById(R.id.rv_filedir);
        mRvFiledir.setLayoutManager(new LinearLayoutManager(this));
        mDesignBottomSheet = findViewById(R.id.design_bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(mDesignBottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mFoldersAdapter = new PhotoFoldersAdapter();
        mRvFiledir.setAdapter(mFoldersAdapter);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void onChooseModeChange(boolean isChoose) {
        chooseMenu.setTitle(isChoose ? "取消" : "选择");
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, AlbumActivity.class);
        context.startActivity(starter);
    }

    @Override
    public void fullPreviewData(List<PhotoEntry> allPhotos) {

    }

    @Override
    public void fullFolders(List<AlbumEntry> folders) {
        mFoldersAdapter.setData(folders);
    }

    @Override
    public void updateSelectedSize(int size, int min, int max) {

    }

    @Override
    public void updateBtnStatus(boolean enable) {

    }

    @Override
    public void onSelectFull() {

    }

    @Override
    public void setSelectResult(List<PhotoEntry> photoItems) {

    }

    @Override
    public void checkFolderListStatus() {

    }

    @Override
    public void showProgressDialog(int max) {

    }

    @Override
    public void showContinueDialog() {

    }

    @Override
    public void dismissProgressDialog() {

    }

    @Override
    public void incrementProgress() {

    }

    @Override
    public void photoPreviewAdd() {

    }

    @Override
    public void photoPreviewRemove(int position) {

    }

    @Override
    public void scrollToImage(int dataPosition) {

    }
}

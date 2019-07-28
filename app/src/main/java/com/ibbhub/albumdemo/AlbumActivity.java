package com.ibbhub.albumdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.ibbhub.album.AlbumFragment;
import com.ibbhub.album.OnChooseModeListener;
import com.ibbhub.album.TaHelper;
import com.ibbhub.albumdemo.adapter.PhotoFoldersAdapter;
import com.ibbhub.albumdemo.application.MyApplication;
import com.ibbhub.albumdemo.bean.AlbumEntry;
import com.ibbhub.albumdemo.bean.PhotoEntry;
import com.ibbhub.albumdemo.callback.OnEditItemClickListener;
import com.ibbhub.albumdemo.contract.GooglePhotoContract;
import com.ibbhub.albumdemo.repository.GooglePhotoScanner;

import java.util.List;

public class AlbumActivity extends AppCompatActivity implements GooglePhotoContract.View, OnChooseModeListener {

    private LinearLayout mDesignBottomSheet;
    private BottomSheetBehavior mBottomSheetBehavior;
    private PhotoFoldersAdapter mFoldersAdapter;

    private RecyclerView mRvFiledir;

    private View floderBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        MyApplication.setApplication(getApplication());
        initView();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // android.util.Log.d("wlDebug", "GooglePhotoScanner.");
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

        floderBtn = findViewById(R.id.folder_btn);
        floderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
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
        albumFragment.setOnChooseModeListener(this);
        mRvFiledir = findViewById(R.id.rv_filedir);
        mRvFiledir.setLayoutManager(new LinearLayoutManager(this));
        mDesignBottomSheet = findViewById(R.id.design_bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(mDesignBottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    if (!albumFragment.isChooseMode) {
                        floderBtn.setVisibility(View.VISIBLE);
                    }
                } else {
                    floderBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }


        });
        mFoldersAdapter = new PhotoFoldersAdapter();
        mRvFiledir.setAdapter(mFoldersAdapter);
        mFoldersAdapter.setOnItemClickListener(new OnEditItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                AlbumEntry bean = mFoldersAdapter.getItem(position);
                String _path = bean.getAlbumCover().subSequence(0, bean.getAlbumCover().lastIndexOf("/")).toString();
                android.util.Log.d("wlDebug", "" + _path);
                ((MyAlbumFragment) albumFragment).setAlbumSrc(_path);
                albumFragment.initData();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            return;
        }
        if (albumFragment.isChooseMode) {
            albumFragment.cancelChoose();
            return;
        }
        super.onBackPressed();
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


    @Override
    public void onChooseMode(boolean isChoose) {
        if (isChoose) {
            floderBtn.setVisibility(View.GONE);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            floderBtn.setVisibility(View.VISIBLE);
        }
    }
}

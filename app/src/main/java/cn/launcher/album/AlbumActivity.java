package cn.launcher.album;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.widget.TextView;
import android.widget.Toast;

import com.ibbhub.album.AlbumFragment;
import com.ibbhub.album.OnChooseModeListener;

import cn.launcher.album.adapter.PhotoFoldersAdapter;
import cn.launcher.album.application.MyApplication;
import cn.launcher.album.bean.AlbumEntry;
import cn.launcher.album.bean.PhotoEntry;
import cn.launcher.album.callback.OnEditItemClickListener;
import cn.launcher.album.contract.GooglePhotoContract;
import cn.launcher.album.repository.GooglePhotoScanner;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.List;

public class AlbumActivity extends AppCompatActivity implements GooglePhotoContract.View, OnChooseModeListener {

    private LinearLayout mDesignBottomSheet;
    private BottomSheetBehavior mBottomSheetBehavior;
    private PhotoFoldersAdapter mFoldersAdapter;

    private RecyclerView mRvFiledir;

    private View floderBtn;

    private TextView cameraPhotoTV;

    private TextView albumPhotoTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        MyApplication.setApplication(getApplication());

        requestPermission();
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

    private void requestPermission() {
        //获取storage权限
        AndPermission.with(this)
                .runtime()
                .permission(Permission.Group.STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        initView();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {

                    }
                })
                .start();
    }

    private void initView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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

        cameraPhotoTV = findViewById(R.id.camera_photo_tv);
        cameraPhotoTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraPhotoTV.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                albumPhotoTV.setTextColor(getResources().getColor(android.R.color.black));
                String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/DCIM/Camera";
                ((MyAlbumFragment) albumFragment).setAlbumSrc(path);
                albumFragment.initData();
            }
        });
        albumPhotoTV = findViewById(R.id.album_photo_tv);

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
                cameraPhotoTV.setTextColor(getResources().getColor(android.R.color.black));
                albumPhotoTV.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        floderBtn = findViewById(R.id.folder_btn);
        findViewById(R.id.album_photo_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        findViewById(R.id.to_camera_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCamera();
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

    private void showCamera() {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName("com.mediatek.camera", "com.mediatek.camera.CameraActivity");
            intent.setComponent(cn);
            startActivity(intent);
//            startActivity(cameraIntent);
        } else {
//            Toast.makeText(getApplicationContext(), R.string.msg_no_camera, Toast.LENGTH_SHORT).show();
        }

    }

}

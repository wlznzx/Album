package chuangyuan.ycj.videolibrary.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.ExoPlayerView;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import chuangyuan.ycj.videolibrary.R;
import chuangyuan.ycj.videolibrary.listener.ExoPlayerListener;
import chuangyuan.ycj.videolibrary.listener.OnEndGestureListener;
import chuangyuan.ycj.videolibrary.utils.VideoPlayUtils;

/**
 * author  yangc
 * date 2017/11/24
 * E-Mail:yangchaojiang@outlook.com
 * Deprecated: 父类view 存放控件方法
 */
abstract class BaseView extends FrameLayout {
    private static final int ANIM_DURATION = 600;
    /*** The constant TAG.***/
    public static final String TAG = VideoPlayerView.class.getName();
    /*** 记录视频进度缓存map  **/
    protected static WeakHashMap<String, Long> tags = new WeakHashMap<>();
    /*** 记录视频当前窗口缓存map **/
    protected static WeakHashMap<String, Integer> tags2 = new WeakHashMap<>();
    final Activity activity;
    /***播放view*/
    protected final ExoPlayerView playerView;
    /*** 加载速度显示*/
    protected TextView videoLoadingShowText;
    /***错误页,进度控件,锁屏按布局,自定义预览布局,提示布局,播放按钮*/
    protected View exoLoadingLayout, exoPlayPreviewLayout, exoPreviewPlayBtn, exoBarrageLayout;
    /***水印,封面图占位,显示音频和亮度布图,返回按钮*/
    protected ImageView exoPlayWatermark, exoPreviewImage, exoBottomPreviewImage, exoControlsBack;
    /***手势管理布局view***/
    protected final GestureControlView mGestureControlView;
    /***视频加载页***/
    protected final ActionControlView mActionControlView;
    /*** 锁屏管理布局***/
    protected final LockControlView mLockControlView;
    /***锁屏管理布局***/
    protected final PlayerControlView controllerView;
    /***切换***/
    protected BelowView belowView;
    /***流量提示框***/
    protected AlertDialog alertDialog;
    protected ExoPlayerListener mExoPlayerListener;
    /***标题左间距,多分辨率,默认Ui布局样式横屏后还原处理***/
    protected int getPaddingLeft, switchIndex, setSystemUiVisibility = 0;
    /*** The Ic back image.***/
    @DrawableRes
    private int icBackImage = R.drawable.ic_exo_back;
    private OnEndGestureListener mOnEndGestureListener;
    private View.OnClickListener onClickListener;
    /***是否显示返回按钮,是否在上面,是否横屏,是否列表播放 默认false,是否切换按钮,是否自动切换视频宽高*/
    private boolean isShowBack = true, isLand, isListPlayer, isShowVideoSwitch, isWGh, controllerHideOnTouch = true, isVerticalFullScreen;
    private ArrayList<String> nameSwitch;

    /**
     * Instantiates a new Base vie
     *
     * @param context the context
     */
    public BaseView(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Instantiates a new Base view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public BaseView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Instantiates a new Base view.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public BaseView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        activity = VideoPlayUtils.scanForActivity(context);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        playerView = new ExoPlayerView(getContext(), attrs, defStyleAttr);
        controllerView = playerView.getControllerView();
        mGestureControlView = new GestureControlView(getContext(), attrs, defStyleAttr);
        mActionControlView = new ActionControlView(getContext(), attrs, defStyleAttr);
        mLockControlView = new LockControlView(getContext(), attrs, defStyleAttr, this);
        addView(playerView, params);
        int userWatermark = 0;
        int defaultArtworkId = 0;
        int loadId = R.layout.simple_exo_play_load;
        int preViewLayoutId = 0;
        int barrageLayoutId = 0;
        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.VideoPlayerView, 0, 0);
            try {
                icBackImage = a.getResourceId(R.styleable.VideoPlayerView_player_back_image, icBackImage);
                userWatermark = a.getResourceId(R.styleable.VideoPlayerView_user_watermark, 0);
                isListPlayer = a.getBoolean(R.styleable.VideoPlayerView_player_list, false);
                defaultArtworkId = a.getResourceId(R.styleable.VideoPlayerView_default_artwork, defaultArtworkId);
                loadId = a.getResourceId(R.styleable.VideoPlayerView_player_load_layout_id, loadId);
                preViewLayoutId = a.getResourceId(R.styleable.VideoPlayerView_player_preview_layout_id, preViewLayoutId);
                barrageLayoutId = a.getResourceId(R.styleable.VideoPlayerView_player_custom_layout_id, barrageLayoutId);
                int playerViewId = a.getResourceId(R.styleable.VideoPlayerView_controller_layout_id, R.layout.simple_exo_playback_control_view);
                if (preViewLayoutId == 0 && (playerViewId == R.layout.simple_exo_playback_list_view || playerViewId == R.layout.simple_exo_playback_top_view)) {
                    preViewLayoutId = R.layout.exo_default_preview_layout;
                }
            } finally {
                a.recycle();
            }
        }
        if (barrageLayoutId != 0) {
            exoBarrageLayout = inflate(context, barrageLayoutId, null);
        }
        exoLoadingLayout = inflate(context, loadId, null);
        if (preViewLayoutId != 0) {
            exoPlayPreviewLayout = inflate(context, preViewLayoutId, null);
        }
        intiView();
        initWatermark(userWatermark, defaultArtworkId);
    }


    /**
     * Inti view.
     */
    private void intiView() {
        exoControlsBack = new ImageView(getContext());
        exoControlsBack.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int ss = VideoPlayUtils.dip2px(getContext(), 7f);
        exoControlsBack.setId(R.id.exo_controls_back);
        exoControlsBack.setImageDrawable(ContextCompat.getDrawable(getContext(), icBackImage));
        exoControlsBack.setPadding(ss, ss, ss, ss);
        FrameLayout frameLayout = playerView.getContentFrameLayout();
        frameLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.exo_player_background_color));
        exoLoadingLayout.setBackgroundColor(Color.TRANSPARENT);
        exoLoadingLayout.setVisibility(GONE);
        exoLoadingLayout.setClickable(true);
        frameLayout.addView(mGestureControlView, frameLayout.getChildCount());
        frameLayout.addView(mActionControlView, frameLayout.getChildCount());
        frameLayout.addView(mLockControlView, frameLayout.getChildCount());
        if (null != exoPlayPreviewLayout) {
            frameLayout.addView(exoPlayPreviewLayout, frameLayout.getChildCount());
        }
        frameLayout.addView(exoLoadingLayout, frameLayout.getChildCount());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(VideoPlayUtils.dip2px(getContext(), 35f), VideoPlayUtils.dip2px(getContext(), 35f));
        frameLayout.addView(exoControlsBack, frameLayout.getChildCount(), layoutParams);
        int index = frameLayout.indexOfChild(findViewById(R.id.exo_controller_barrage));
        if (exoBarrageLayout != null) {
            frameLayout.removeViewAt(index);
            exoBarrageLayout.setBackgroundColor(Color.TRANSPARENT);
            frameLayout.addView(exoBarrageLayout, index);
        }
        exoPlayWatermark = playerView.findViewById(R.id.exo_player_watermark);
        videoLoadingShowText = playerView.findViewById(R.id.exo_loading_show_text);
        exoBottomPreviewImage = playerView.findViewById(R.id.exo_preview_image_bottom);
        if (playerView.findViewById(R.id.exo_preview_image) != null) {
            exoPreviewImage = playerView.findViewById(R.id.exo_preview_image);
            exoPreviewImage.setBackgroundResource(android.R.color.transparent);
        } else {
            exoPreviewImage = exoBottomPreviewImage;
        }
        setSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
        exoPreviewPlayBtn = playerView.findViewById(R.id.exo_preview_play);

    }

    /****
     * 重置
     * ***/
    public void resets() {
        if (getTag() != null) {
            tags.put(getTag().toString(), getPlayerView().getPlayer().getCurrentPosition());
            tags2.put(getTag().toString(), getPlayerView().getPlayer().getCurrentWindowIndex());
        }
        mLockControlView.removeCallback();
        if (exoLoadingLayout != null) {
            exoLoadingLayout.setVisibility(GONE);
        }
        if (mActionControlView != null) {
            mActionControlView.hideAllView();
        }
        getPlaybackControlView().showNo();
       // showPreViewLayout(VISIBLE);
    }

    /**
     * On destroy.
     */
    public void onDestroy() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        if (exoControlsBack != null && exoControlsBack.animate() != null) {
            exoControlsBack.animate().cancel();
        }
        if (mLockControlView != null) {
            mLockControlView.onDestroy();
        }
        if (activity != null && activity.isDestroyed()) {
            tags.clear();
            tags2.clear();
            belowView = null;
            alertDialog = null;
        }
        nameSwitch = null;
    }


    /***
     * 设置水印图和封面图
     * @param userWatermark userWatermark  水印图
     * @param defaultArtworkId defaultArtworkId   封面图
     */
    protected void initWatermark(int userWatermark, int defaultArtworkId) {
        if (userWatermark != 0) {
            exoPlayWatermark.setImageResource(userWatermark);
        }
        if (defaultArtworkId != 0) {
            setPreviewImage(BitmapFactory.decodeResource(getResources(), defaultArtworkId));
        }
    }

    /***
     * 显示网络提示框
     */
    protected void showDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            return;
        }
        alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle(getContext().getString(R.string.exo_play_reminder));
        alertDialog.setMessage(getContext().getString(R.string.exo_play_wifi_hint_no));
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getContext().
                getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showBtnContinueHint(View.VISIBLE);

            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showBtnContinueHint(View.GONE);
                if (mExoPlayerListener != null) {
                    mExoPlayerListener.playVideoUri();
                }

            }
        });
        alertDialog.show();
    }

    /***
     * 设置内容横竖屏内容
     *
     */
    protected void scaleLayout() {
        if (isVerticalFullScreen()) {
            scaleVerticalLayout();
            return;
        }
        ViewGroup contentView = activity.findViewById(android.R.id.content);
        ViewGroup parent = (ViewGroup) playerView.getParent();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        if (parent != null) {
            parent.removeView(playerView);
        }
        if (isLand) {
            contentView.addView(playerView, params);
        } else {
            addView(playerView, params);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mExoPlayerListener.land();
        }
    }

    /***
     * 设置内容竖屏全屏
     *
     */
    private void scaleVerticalLayout() {
        ViewGroup contentView = activity.findViewById(android.R.id.content);
        final ViewGroup parent = (ViewGroup) playerView.getParent();
        if (isLand) {
            if (parent != null) {
                parent.removeView(playerView);
            }
            LayoutParams params;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                contentView.addView(playerView, params);
            } else {
                params = new LayoutParams(getWidth(), getHeight());
                contentView.addView(playerView, params);
                ChangeBounds changeBounds = new ChangeBounds();
                //开启延迟动画，在这里会记录当前视图树的状态
                changeBounds.setDuration(ANIM_DURATION);
                TransitionManager.beginDelayedTransition(contentView, changeBounds);
                ViewGroup.LayoutParams layoutParams = playerView.getLayoutParams();
                layoutParams.height = LayoutParams.MATCH_PARENT;
                layoutParams.width = LayoutParams.MATCH_PARENT;
                playerView.setLayoutParams(layoutParams);
            }

        } else {
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ChangeBounds changeBounds = new ChangeBounds();
                //开启延迟动画，在这里会记录当前视图树的状态
                changeBounds.setDuration(ANIM_DURATION);
                TransitionManager.beginDelayedTransition(contentView, changeBounds);
                ViewGroup.LayoutParams layoutParams2 = playerView.getLayoutParams();
                layoutParams2.width = getWidth();
                layoutParams2.height = getHeight();
                playerView.setLayoutParams(layoutParams2);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (parent != null) {
                            parent.removeView(playerView);
                        }
                        BaseView.this.addView(playerView);
                    }
                }, ANIM_DURATION);
            } else {
                if (parent != null) {
                    parent.removeView(playerView);
                }
                addView(playerView, params);
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
         //   mExoPlayerListener.land();
        }
    }

    /***
     * 列表显示返回按钮
     * @param visibility visibility
     * **/
    protected void showListBack(int visibility) {
        if (isListPlayer()) {
            if (visibility == VISIBLE) {
                exoControlsBack.setVisibility(VISIBLE);
                getPaddingLeft = controllerView.getExoControllerTop().getPaddingLeft();
                controllerView.getExoControllerTop().
                        setPadding(VideoPlayUtils.dip2px(getContext(), 35), 0, 0, 0);
            } else {
                controllerView.getExoControllerTop().setPadding(getPaddingLeft, 0, 0, 0);
            }
            showBackView(visibility, false);
        }
    }

    /***
     * 为了播放完毕后，旋转屏幕，导致播放图像消失处理
     * @param visibility 状态
     */
    protected void showBottomView(int visibility) {
        exoBottomPreviewImage.setVisibility(visibility);
        if (visibility == VISIBLE) {
            exoBottomPreviewImage.setImageDrawable(exoPreviewImage.getDrawable());
        }
    }

    /***
     * 显示隐藏加载页
     *
     * @param visibility 状态
     */
    protected void showLockState(int visibility) {
        mLockControlView.showLockState(visibility);
    }

    /***
     * 显示隐藏加载页
     *
     * @param visibility 状态
     */
    protected void showLoadState(int visibility) {
        if (visibility == View.VISIBLE) {
            showErrorState(GONE);
            showReplay(GONE);
            showLockState(GONE);
        }
        if (exoLoadingLayout != null) {
            exoLoadingLayout.setVisibility(visibility);
        }
    }

    /***
     * 显示隐藏错误页
     *
     * @param visibility 状态
     */
    protected void showErrorState(int visibility) {
        if (visibility == View.VISIBLE) {
            playerView.hideController();
            showReplay(GONE);
            showBackView(VISIBLE, true);
            showLockState(GONE);
            showLoadState(GONE);
            showPreViewLayout(GONE);
        }
        mActionControlView.showErrorState(visibility);
    }

    /***
     * 显示按钮提示页
     *
     * @param visibility 状态
     */
    protected void showBtnContinueHint(int visibility) {
        if (visibility == View.VISIBLE) {
            showReplay(GONE);
            showErrorState(GONE);
            showPreViewLayout(GONE);
            showLoadState(GONE);
            showBackView(VISIBLE, true);
        }
        mActionControlView.showBtnContinueHint(visibility);
    }

    /***
     * 显示隐藏重播页
     *
     * @param visibility 状态
     */
    protected void showReplay(int visibility) {
        if (visibility == View.VISIBLE) {
            controllerView.hideNo();
            showErrorState(GONE);
            showBtnContinueHint(GONE);
            showPreViewLayout(GONE);
            showLockState(GONE);
            showLoadState(GONE);
            showBottomView(VISIBLE);
            showBackView(VISIBLE, true);
        }
        mActionControlView.showReplay(visibility);
    }

    /***
     * 显示隐藏自定义预览布局
     *
     * @param visibility 状态
     */
    protected void showPreViewLayout(int visibility) {
        if (exoPlayPreviewLayout != null) {
            if (exoPlayPreviewLayout.getVisibility() == visibility) {
                return;
            }
            exoPlayPreviewLayout.setVisibility(visibility);
            if (playerView.findViewById(R.id.exo_preview_play) != null) {
                playerView.findViewById(R.id.exo_preview_play).setVisibility(visibility);
            }
        }
    }

    /***
     * 显示隐藏返回键
     *
     * @param visibility 状态
     * @param is is
     */
    protected void showBackView(int visibility, boolean is) {
        if (exoControlsBack != null) {
            //如果是竖屏和且不显示返回按钮，就隐藏
            if ((!isShowBack && !isLand)) {
                exoControlsBack.setVisibility(GONE);
                return;
            }
            if (isListPlayer() && !isLand) {
                exoControlsBack.setVisibility(GONE);
            } else {
                if (visibility == VISIBLE && is) {
                    exoControlsBack.setTranslationY(0);
                    exoControlsBack.setAlpha(1f);
                }
                exoControlsBack.setVisibility(visibility);
            }
        }
    }

    /**
     * 播放监听事件
     ***/
    final View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (mExoPlayerListener == null) {
                    return false;
                }
                if (onClickListener != null) {
                    onClickListener.onClick(v);
                } else {
                    mExoPlayerListener.startPlayers();
                }
            }
            return false;
        }
    };

    /****
     * 设置点击播放按钮回调, 交给用户处理
     * @param onClickListener 回调实例
     */
    public void setOnPlayClickListener(@Nullable View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /***
     * 设置设置手势回调
     * @param mOnEndGestureListener updateProgressListener
     */
    public void setOnEndGestureListener(OnEndGestureListener mOnEndGestureListener) {
        this.mOnEndGestureListener = mOnEndGestureListener;
    }

    /***
     * 设置手势touch 事件
     * @param controllerHideOnTouch true 启用  false 关闭
     */
    public void setPlayerGestureOnTouch(boolean controllerHideOnTouch) {
        this.controllerHideOnTouch = controllerHideOnTouch;
    }

    /**
     * 设置返回返回按钮
     *
     * @param showBack true 显示返回  false 反之
     */
    public void setShowBack(boolean showBack) {
        this.isShowBack = showBack;
    }

    /**
     * 设置标题
     *
     * @param title 内容
     */
    public void setTitle(@NonNull String title) {
        controllerView.setTitle(title);
    }

    /***
     * 显示水印图
     *
     * @param res 资源
     */
    public void setExoPlayWatermarkImg(int res) {
        if (exoPlayWatermark != null) {
            exoPlayWatermark.setImageResource(res);
        }
    }

    /**
     * 设置占位预览图
     *
     * @param previewImage 预览图
     */
    public void setPreviewImage(Bitmap previewImage) {
        this.exoPreviewImage.setImageBitmap(previewImage);
    }

    /***
     * 设置播放的状态回调 .,此方法不是外部使用，请不要调用
     *
     * @param mExoPlayerListener 回调
     */
    public void setExoPlayerListener(ExoPlayerListener mExoPlayerListener) {
        this.mExoPlayerListener = mExoPlayerListener;
    }

    /***
     * 设置开启线路切换按钮
     *
     * @param showVideoSwitch true 显示  false 不现实
     */
    public void setShowVideoSwitch(boolean showVideoSwitch) {
        isShowVideoSwitch = showVideoSwitch;
    }

    /**
     * 设置全屏按钮样式
     *
     * @param icFullscreenStyle 全屏按钮样式
     */
    public void setFullscreenStyle(@DrawableRes int icFullscreenStyle) {
        controllerView.setFullscreenStyle(icFullscreenStyle);
    }

    /**
     * 设置开启开启锁屏功能
     *
     * @param openLock 默认 true 开启   false 不开启
     */
    public void setOpenLock(boolean openLock) {
        mLockControlView.setOpenLock(openLock);
    }

    /**
     * 设置开启开启锁屏功能
     *
     * @param openLock 默认 false 不开启   true 开启
     */
    public void setOpenProgress2(boolean openLock) {
        mLockControlView.setProgress(openLock);
    }

    /**
     * Gets name switch.
     *
     * @return the name switch
     */
    protected ArrayList<String> getNameSwitch() {
        return nameSwitch == null ? nameSwitch = new ArrayList<>() : nameSwitch;
    }

    protected void setNameSwitch(ArrayList<String> nameSwitch) {
        this.nameSwitch = nameSwitch;
    }

    /**
     * Gets name switch.
     *
     * @return the name switch
     */
    protected int getSwitchIndex() {
        return switchIndex;
    }

    /**
     * 设置多分辨显示文字
     *
     * @param name        name
     * @param switchIndex switchIndex
     */
    public void setSwitchName(@NonNull List<String> name, @Size(min = 0) int switchIndex) {
        this.nameSwitch = new ArrayList<>(name);
        this.switchIndex = switchIndex;
    }

    /**
     * 设置是否横屏
     *
     * @param land land  默认 false  true  横屏
     */
    protected void setLand(boolean land) {
        isLand = land;
    }


    /**
     * 设置视频宽度小于视频高度是否旋转(渲染是texture_view 有效)
     *
     * @param isWGh isWGh  默认 false  true 开启
     */
    public void setWGh(boolean isWGh) {
        this.isWGh = isWGh;
    }

    /**
     * 是否开启竖屏全屏
     *
     * @param verticalFullScreen isWGh  默认 false  true 开启
     */
    public void setVerticalFullScreen(boolean verticalFullScreen) {
        isVerticalFullScreen = verticalFullScreen;
    }

    public boolean isVerticalFullScreen() {
        return isVerticalFullScreen;
    }

    public boolean isShowBack() {
        return isShowBack;
    }

    protected boolean isWGh() {
        return isWGh;
    }

    public boolean isLand() {
        return isLand;
    }


    protected boolean isShowVideoSwitch() {
        return isShowVideoSwitch;
    }

    /****
     * 获取锁频view
     *
     * @return PlaybackControlView playback control view
     */
    @NonNull
    public LockControlView getLockControlView() {
        return mLockControlView;
    }

    /****
     * 获取控制view
     *
     * @return PlaybackControlView playback control view
     */
    @NonNull
    public PlayerControlView getPlaybackControlView() {
        return controllerView;
    }

    /***
     * 获取当前加载布局
     *
     * @return boolean
     */
    public boolean isLoadingLayoutShow() {
        return exoLoadingLayout.getVisibility() == VISIBLE;
    }

    /***
     * 获取视频加载view
     *
     * @return View load layout
     */
    @NonNull
    public View getLoadLayout() {
        return exoLoadingLayout;
    }

    /***
     * 流量播放提示view
     *
     * @return View play hint layout
     */
    @NonNull
    public View getPlayHintLayout() {
        return mActionControlView.getPlayBtnHintLayout();
    }

    /***
     * 重播展示view
     *
     * @return View replay layout
     */
    @NonNull
    public View getReplayLayout() {
        return mActionControlView.getPlayReplayLayout();
    }

    /***
     * 错误展示view
     *
     * @return View error layout
     */
    @NonNull
    public View getErrorLayout() {
        return mActionControlView.getExoPlayErrorLayout();
    }

    /***
     * 获取手势音频view
     *
     * @return View 手势
     */
    @NonNull
    public View getGestureAudioLayout() {
        return mGestureControlView.getExoAudioLayout();
    }

    /***
     * 获取手势亮度view
     *
     * @return View gesture brightness layout
     */
    @NonNull
    public View getGestureBrightnessLayout() {
        return mGestureControlView.getExoBrightnessLayout();
    }

    /***
     * 获取手势视频进度调节view
     *
     * @return View gesture progress layout
     */
    @NonNull
    public View getGestureProgressLayout() {
        return mGestureControlView.getDialogProLayout();
    }

    /***
     * 是否属于列表播放
     *
     * @return boolean boolean
     */
    public boolean isListPlayer() {
        return isListPlayer;
    }

    /***
     * 获取全屏按钮
     * @return boolean exo fullscreen
     */
    public AppCompatCheckBox getExoFullscreen() {
        return controllerView.getExoFullscreen();
    }

    /**
     * Gets switch text.
     *
     * @return the switch text
     */
    @NonNull
    public TextView getSwitchText() {
        return controllerView.getSwitchText();
    }


    /***
     * 获取预览图
     *
     * @return ImageView preview image
     */
    @NonNull
    public ImageView getPreviewImage() {
        return exoPreviewImage;
    }

    /***
     * 获取内核播放view
     *
     * @return SimpleExoPlayerView player view
     */
    @NonNull
    public PlayerView getPlayerView() {
        return playerView;
    }

    /**
     * 获取进度条
     *
     * @return ExoDefaultTimeBar time bar
     */
    @NonNull
    public ExoDefaultTimeBar getTimeBar() {
        return (ExoDefaultTimeBar) controllerView.getTimeBar();
    }


    protected final View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!controllerHideOnTouch) {
                return false;
            } else if (mLockControlView.isLock()) {
                return false;
            } else if (!isLand) {
                //竖屏不执行手势
                return false;
            }
            if (mOnEndGestureListener != null) {
                mOnEndGestureListener.onTouchEvent(event);
            }
            // 处理手势结束
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    if (mOnEndGestureListener != null) {
                        mOnEndGestureListener.onEndGesture();
                    }
                    break;
                default:
            }
            return false;
        }
    };

}

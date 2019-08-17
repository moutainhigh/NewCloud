package com.bokecc.ccsskt.example.activity;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.bluetooth.BluetoothHeadset;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.ccsskt.example.CCApplication;
import com.bokecc.ccsskt.example.R;
import com.bokecc.ccsskt.example.R2;
import com.bokecc.ccsskt.example.adapter.ChatAdapter;
import com.bokecc.ccsskt.example.adapter.ColorAdapter;
import com.bokecc.ccsskt.example.adapter.VideoAdapter;
import com.bokecc.ccsskt.example.base.BaseActivity;
import com.bokecc.ccsskt.example.bridge.OnDisplayInteractionListener;
import com.bokecc.ccsskt.example.bridge.OnDocInteractionListener;
import com.bokecc.ccsskt.example.bridge.OnTeacherLectureListener;
import com.bokecc.ccsskt.example.bridge.OnVideoInteractionListener;
import com.bokecc.ccsskt.example.entity.ChatEntity;
import com.bokecc.ccsskt.example.entity.ColorStatus;
import com.bokecc.ccsskt.example.entity.MyEBEvent;
import com.bokecc.ccsskt.example.entity.VideoStreamView;
import com.bokecc.ccsskt.example.fragment.BaseFragment;
import com.bokecc.ccsskt.example.fragment.LectureFragment;
import com.bokecc.ccsskt.example.fragment.MainVideoFragment;
import com.bokecc.ccsskt.example.fragment.TilingFragment;
import com.bokecc.ccsskt.example.global.Config;
import com.bokecc.ccsskt.example.interact.MyBroadcastReceiver;
import com.bokecc.ccsskt.example.popup.BottomCancelPopup;
import com.bokecc.ccsskt.example.popup.CommonPopup;
import com.bokecc.ccsskt.example.recycle.BaseOnItemTouch;
import com.bokecc.ccsskt.example.recycle.DividerGridItemDecoration;
import com.bokecc.ccsskt.example.recycle.OnClickListener;
import com.bokecc.ccsskt.example.util.AndroidBug5497Workaround;
import com.bokecc.ccsskt.example.util.DensityUtil;
import com.bokecc.ccsskt.example.util.SoftKeyboardUtil;
import com.bokecc.ccsskt.example.util.TimeUtil;
import com.bokecc.ccsskt.example.util.UserComparator;
import com.bokecc.ccsskt.example.view.DocView;
import com.bokecc.sskt.CCInteractSession;
import com.bokecc.sskt.SubscribeRemoteStream;
import com.bokecc.sskt.base.CCStream;
import com.bokecc.sskt.base.ConnectionStatsWrapper;
import com.bokecc.sskt.base.exception.StreamException;
import com.bokecc.sskt.base.renderer.CCSurfaceRenderer;
import com.bokecc.sskt.bean.PicToken;
import com.bokecc.sskt.bean.User;
import com.bokecc.sskt.bean.Vote;
import com.bokecc.sskt.bean.VoteResult;
import com.bokecc.sskt.doc.DocInfo;
import com.bokecc.sskt.net.OKHttpStatusListener;
import com.bokecc.sskt.net.OKHttpUtil;
import com.bumptech.glide.Glide;
import com.cpiz.android.bubbleview.BubblePopupWindow;
import com.cpiz.android.bubbleview.BubbleRelativeLayout;
import com.cpiz.android.bubbleview.BubbleStyle;
import com.intel.webrtc.base.LocalCameraStreamParameters;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.compress.CompressImage;
import com.jph.takephoto.compress.CompressImageImpl;
import com.jph.takephoto.model.TImage;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static com.bokecc.ccsskt.example.R.id.id_student_chat_open_img;
import static com.bokecc.ccsskt.example.R.id.id_student_class_user_list;
import static com.bokecc.ccsskt.example.global.Config.mRoomDes;
import static com.bokecc.ccsskt.example.global.Config.mRoomId;
import static com.bokecc.ccsskt.example.global.Config.mUserId;
import static com.bokecc.sskt.CCInteractSession.LIANMAI_MODE_AUTO;
import static com.bokecc.sskt.CCInteractSession.LIANMAI_MODE_FREE;

@RuntimePermissions
public class StudentActivity extends BaseActivity implements OnDocInteractionListener, OnVideoInteractionListener, OnDisplayInteractionListener,View.OnClickListener,OnTeacherLectureListener {

    private static final String TAG = StudentActivity.class.getSimpleName();

    @BindView(R2.id.id_student_other_layout)
    RelativeLayout mOtherScenes;
    @BindView(R2.id.id_student_noclass_layout)
    LinearLayout mNoClassLayout;
    @BindView(R2.id.id_student_teacher_gone_layout)
    RelativeLayout mTeacherGoneLayout;
    @BindView(R2.id.id_student_class_user_list)
    View mClassMsg;
    TextView mClassName;
    TextView mClassUserNum;
    ImageView mClassHandIcon;
    ImageView mClassArrowIcon;
    @BindView(R2.id.id_student_top_layout)
    RelativeLayout mTopLayout;
    @BindView(R2.id.id_student_video_controller)
    ImageButton mVideoController;
    @BindView(R2.id.id_student_timer)
    RelativeLayout mRoomTimerLayout;
    @BindView(R2.id.id_student_timer_value)
    TextView mTimerValue;
    @BindView(R2.id.id_student_bottom_layout)
    RelativeLayout mBottomLayout;
    @BindView(R2.id.id_student_lianmaistyle)
    Button mLianmaiStyle;
    @BindView(R2.id.id_student_click_dismiss_chat)
    FrameLayout mClickDismissChatLayout;
    @BindView(R2.id.id_student_chat_layout)
    RelativeLayout mChatLayout;
    @BindView(R2.id.id_student_chat_list)
    RecyclerView mChatList;
    @BindView(R2.id.id_student_chat_input)
    EditText mChatInput;
    @BindView(R2.id.id_student_chat)
    Button mChatBtn;
    @BindView(R2.id.id_student_chat_img_fullscreen_layout)
    RelativeLayout mChatImageLayout;
    @BindView(R2.id.id_student_chat_img)
    ImageView mChatImage;
    @BindView(R2.id.id_student_drag_child)
    LinearLayout mDrawLayout;
    @BindView(R2.id.id_student_draw_paint)
    ImageButton mDrawPaint;
    @BindView(R2.id.id_student_draw_clear)
    ImageButton mDrawClear;
    @BindView(R2.id.id_student_draw_tbc)
    ImageButton mDrawTBC;
    @BindView(R2.id.id_student_handup)
    Button mHandup;
    @BindView(R2.id.id_student_share_screen_container)
    FrameLayout mShareScreenContainer;
    @BindView(R2.id.id_student_share_screen)
    CCSurfaceRenderer mShareScreen;
    @BindView(R2.id.id_student_share_screen_exit)
    ImageView mShareScreenExit;
    @BindView(R2.id.id_student_remote_video_container)
    FrameLayout mRemoteVideoContainer;
    @BindView(R2.id.id_student_remote_video)
    SurfaceView mRemoteVideo;
    @BindView(R2.id.id_student_remote_video_exit)
    ImageView mRemoteVideoExit;

    @BindView(R2.id.id_student_page_change_layout)
    LinearLayout mPageChangeLayout;
    @BindView(R2.id.id_student_doc_index)
    TextView mDocIndex;
    @BindView(R2.id.id_student_doc_back)
    ImageButton mDocBack;
    @BindView(R2.id.id_student_doc_forward)
    ImageButton mDocForward;

    private boolean hasShareScreen = false, isRemoteVideoFullScreen = false, isShareScreenFullScreen = false, isRemoveShareScreen = false;
    private int mShareScreenLeft = 0, mShareScreenTop = 0;
    private int mRemoteVideoLeft = 0, mRemoteVideoTop = 0;

    private View mPopupView;
    private ImageButton mSmallSize, mMidSize, mLargeSize;
    private RecyclerView mColors;
    private BubbleRelativeLayout mBubbleLayout;
    private BubblePopupWindow mPopupWindow;
    private final int[] mColorResIds = new int[]{
            R.drawable.black_selector, R.drawable.orange_selector, R.drawable.green_selector,
            R.drawable.blue_selector, R.drawable.gray_selector, R.drawable.red_selector
    };
    private final String[] mColorStr = new String[]{
            "000000", "f27a1a", "70c75e", "78a7f5", "7b797a", "e33423"
    };
    private final int[] mColorValues = new int[]{
            Color.parseColor("#000000"), Color.parseColor("#f27a1a"), Color.parseColor("#70c75e"),
            Color.parseColor("#78a7f5"), Color.parseColor("#7b797a"), Color.parseColor("#e33423")
    };

    private int mCurPosition = 0;
    private boolean isAuthDraw = false;

    private static final int REQUEST_SYSTEM_PICTURE = 0;

    private Handler mRoomTimerHandler;
    private final Runnable mRoomTimerTask = new Runnable() {
        @Override
        public void run() {
            if (!isStartTimer) {
                return;
            }
            mRoomTime -= 1;
            if (mRoomTime <= 0) {
                updateTimeTip();
                stopCountDown();
                startAnimTip();
                return;
            }
            updateTimeTip();
            mRoomTimerHandler.postDelayed(this, 1000);
        }
    };
    private boolean isStartTimer = false;
    private long mRoomTime;
    private AnimatorSet mAnimatorSet;

    // 连麦状态
    private int mMaiStatus = 0;
    private boolean isNamedHandup = false; // 点名连麦模式 举手
    private boolean isAutoHandup = false; //自动连麦模式 举手

    private boolean needWait = true;

    // 软键盘监听
    private SoftKeyboardUtil mSoftKeyBoardUtil;

    private CommonPopup mExitPopup, mCancelMaiPopup;
    private BottomCancelPopup mCancelPopup;
    private boolean haveDownMai = true;
    // 麦序
    private int mQueueIndex;
    private static final int MAI_STATUS_NORMAL = 0;
    private static final int MAI_STATUS_QUEUE = 1;
    private static final int MAI_STATUS_ING = 2;

    private ArrayList<BaseFragment> mFragments;
    private SparseIntArray mTemplatePosition = new SparseIntArray();
    private BaseFragment mCurFragment;

    private ArrayList<ChatEntity> mChatEntities;
    private ChatAdapter mChatAdapter;
    private boolean isScroll = true;
    private boolean isStateIDLE = true;
    private boolean isClickChat = false;

    private int mTopDistance = -1, mBottomDistance = -1;
    private boolean isTopDismiss = false, isBottomDismiss = false;
    private boolean isCancelTask = false;
    private boolean isVideoShow = true;

    private VideoAdapter mVideoAdapter;
    private VideoStreamView mSelfStreamView;
    private CCSurfaceRenderer mSelfRenderer,surfaceRenderer;
    private CopyOnWriteArrayList<VideoStreamView> mVideoStreamViews = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<CCSurfaceRenderer> mViewPool = new CopyOnWriteArrayList<>();

    private int mCount;// 成员个数

    private HashMap<String, IMediaPlayer> mPlayerMap;
    private HashMap<IMediaPlayer, Boolean> mPlayerType;
    private HashMap<IMediaPlayer, Integer> mPlayerStatus;
    private boolean isMiss = false, isVideoPlay = false, isAudioPlay = false, needRestore = false, needInitVideoPlayer = false;
    private JSONObject mPauseMedia;
    private int mRestorePosition = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_student;
    }

    @Override
    protected void beforeSetContentView() {
        if (CCApplication.sClassDirection == 1) {
            //取消标题
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            //取消状态栏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private int lastX;
    private int lastY;

    @Override
    protected void onViewCreated() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (CCApplication.sClassDirection == 1) { // 横屏全屏软键盘bug 应该判断是FULLSCREEN
            AndroidBug5497Workaround.assistActivity(this);
        }
        mPlayerMap = new HashMap<>();
        mPlayerType = new HashMap<>();
        mPlayerStatus = new HashMap<>();

        doDrawLayoutTouch();
        doShareScreenLayoutTouch();
        doRemoteVideoLayoutTouch();
        mRemoteVideo.setZOrderOnTop(true);
        mRemoteVideo.setZOrderMediaOverlay(true);
        mShareScreen.setZOrderOnTop(true);
        mShareScreen.setZOrderMediaOverlay(true);
        mShareScreen.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (isRemoveShareScreen) {
                    mShareScreen.setVisibility(View.GONE);
                    mShareScreenContainer.setVisibility(View.GONE);
                    hasShareScreen = false;
                }
            }
        });

        mRemoteVideo.setVisibility(View.GONE);
        mShareScreen.setVisibility(View.GONE);

        mClassName = (TextView) mClassMsg.findViewById(R.id.id_top_class_name);
        mClassUserNum = (TextView) mClassMsg.findViewById(R.id.id_top_class_users);
        mClassHandIcon = (ImageView) mClassMsg.findViewById(R.id.id_top_class_handup_flag);
        mClassArrowIcon = (ImageView) mClassMsg.findViewById(R.id.id_top_class_arrow);
        mClassName.setText(mInteractSession.getRoom().getRoomName());

        mVideoController.setVisibility(View.GONE); // 默认视频控制按钮隐藏

        mVideoAdapter = new VideoAdapter(this);
        mFragments = new ArrayList<>();
        mFragments.add(LectureFragment.newInstance(CCInteractSession.TALKER));
        mFragments.add(MainVideoFragment.newInstance(CCInteractSession.TALKER));
        mFragments.add(TilingFragment.newInstance(CCInteractSession.TALKER));
        mFragments.add(MainVideoFragment.newInstance(CCInteractSession.TALKER));
        mTemplatePosition.put(CCInteractSession.TEMPLATE_SPEAK, 0);
        mTemplatePosition.put(CCInteractSession.TEMPLATE_SINGLE, 1);
        mTemplatePosition.put(CCInteractSession.TEMPLATE_TILE, 2);
        mTemplatePosition.put(CCInteractSession.TEMPLATE_DOUBLE_TEACHER, 3);
        for (BaseFragment fragment :
                mFragments) {
            fragment.setVideoAdapter(mVideoAdapter);
        }
        setSelected(mInteractSession.getTemplate());

        // 展示屏幕共享流
        mShareScreen.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        mInteractSession.initSurfaceContext(mShareScreen, new RendererCommon.RendererEvents() {
            @Override
            public void onFirstFrameRendered() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mShareScreenContainer.getLayoutParams();
                        params.width = mShareScreen.getWidth();
                        params.height = mShareScreen.getHeight();
                        mShareScreenContainer.setLayoutParams(params);
                    }
                });
            }

            @Override
            public void onFrameResolutionChanged(int i, int i1, int i2) {

            }
        });

        mSelfStreamView = new VideoStreamView();
        mSelfRenderer = new CCSurfaceRenderer(this);
        mSelfRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        mInteractSession.initSurfaceContext(mSelfRenderer);

        mSelfStreamView.setRenderer(mSelfRenderer);
        SubscribeRemoteStream selfStream = new SubscribeRemoteStream();
        selfStream.setUserName(mInteractSession.getUserName());
        selfStream.setUserId(mInteractSession.getUserIdInPusher());
        selfStream.setUserRole(CCInteractSession.TALKER);
        mSelfStreamView.setStream(selfStream);

        // 设置禁言状态
        mLianmaiStyle.setVisibility(View.VISIBLE);
        if (mInteractSession.isRoomGag() || mInteractSession.isGag()) {
            mChatBtn.setBackgroundResource(R.drawable.student_mute_selector);
        }
        if (mInteractSession.getLianmaiMode() == LIANMAI_MODE_FREE) {
            mLianmaiStyle.setBackgroundResource(R.drawable.queue_mai_selector);
        } else if (mInteractSession.getLianmaiMode() == CCInteractSession.LIANMAI_MODE_NAMED) {
            mLianmaiStyle.setBackgroundResource(R.drawable.handup_selector);
            boolean flag = false;

            if (mInteractSession.getUserList() != null) {
                for (User user :
                        mInteractSession.getUserList()) {
                    if (user.getLianmaiStatus() == CCInteractSession.LIANMAI_STATUS_IN_MAI) {
                        flag = true;
                        break;
                    }
                }
            }
            if (flag != isNamedHandup)
                updateHandUpFlag(flag);
        } else if (mInteractSession.getLianmaiMode() == LIANMAI_MODE_AUTO) {
            mLianmaiStyle.setVisibility(View.GONE);
        }

        mChatList.setLayoutManager(new LinearLayoutManager(this));
        mChatAdapter = new ChatAdapter(this, CCInteractSession.TALKER);
        mChatEntities = new ArrayList<>();
        mChatAdapter.bindDatas(mChatEntities);
        mChatList.setAdapter(mChatAdapter);
//        mChatList.addItemDecoration(new RecycleViewDivider(this,
//                LinearLayoutManager.HORIZONTAL, DensityUtil.dp2px(this, 2), Color.TRANSPARENT,
//                0, 0, RecycleViewDivider.TYPE_BETWEEN));
//        canScrollHorizontally();//能否横向滚动
//        canScrollVertically();//能否纵向滚动
//        scrollToPosition(int position);//滚动到指定位置
//
//        findViewByPosition(int position);//获取指定位置的Item View
//        findFirstCompletelyVisibleItemPosition();//获取第一个完全可见的Item位置
//        findFirstVisibleItemPosition();//获取第一个可见Item的位置
//        findLastCompletelyVisibleItemPosition();//获取最后一个完全可见的Item位置
//        findLastVisibleItemPosition();//获取最后一个可见Item的位置
        mChatList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                Log.i(TAG, "onScrollStateChanged: " + newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    isStateIDLE = true;
                    if (!recyclerView.canScrollVertically(1)) {
                        Log.i(TAG, "onScrollStateChanged: bottom");
                    }
                    if (!recyclerView.canScrollVertically(-1)) {
                        Log.i(TAG, "onScrollStateChanged: top");
                    }
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                    if (lastVisibleItemPosition == recyclerView.getLayoutManager().getItemCount() - 1) {
                        Log.i(TAG, "onScrollStateChanged: last visible");
                        if (!isScroll) {
                            isScroll = true;
                        }
                    }
                } else {
                    isStateIDLE = false;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { // 手指向上滑 内容显示下面的
                    Log.i(TAG, "onScrolled: down");
                } else { // 手指向下滑 内容显示上面的
                    if (!isStateIDLE && isScroll) {
                        isScroll = false;
                    }
                    Log.i(TAG, "onScrolled: up");
                }
            }
        });

        if (mInteractSession.getLianmaiMode() == LIANMAI_MODE_AUTO) {
            mHandup.setVisibility(View.VISIBLE);
            if (mInteractSession.isRoomLive() && CCApplication.isConnect) {
                showProgress();
                needWait = false;
                mInteractSession.requestLianMai(new CCInteractSession.AtlasCallBack<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissProgress();
                    }

                    @Override
                    public void onFailure(String err) {
                        dismissProgress();
                        toastOnUiThread(err);
                    }
                });
            }
        } else {
            mHandup.setVisibility(View.GONE);
        }
        mMyBroadcastReceiver = MyBroadcastReceiver.getInstance();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver(mMyBroadcastReceiver, intentFilter);

        onSoftInputChange();
        initExitPopup();
        initCancelPopup();
        initCancelMaiPopup();

        surfaceRenderer = new CCSurfaceRenderer(this);
        mInteractSession.initSurfaceContext(surfaceRenderer);

        // 是否开播
        if (!mInteractSession.isRoomLive()) {
            mOtherScenes.setVisibility(View.VISIBLE);
            mNoClassLayout.setVisibility(View.VISIBLE);
        } else {
            mOtherScenes.setVisibility(View.GONE);
            CopyOnWriteArrayList<SubscribeRemoteStream> streams = mInteractSession.getSubscribeRemoteStreams();
            for (SubscribeRemoteStream stream :
                    streams) {
                addStreamView(stream);
            }
        }

        mRoomTimerHandler = new Handler();
        mAnimatorSet = new AnimatorSet();
        if (mInteractSession.getInitLastTime() >= 0) { // 计时器
            showTimer(mInteractSession.getInitStartTime(), mInteractSession.getInitLastTime());
        }

        mRemoteVideo.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    Log.e(TAG, "surfaceCreated: ");
                    if (needRestore) {
                        IMediaPlayer player = mPlayerMap.get("videoMedia");
                        if (player != null) {
                            player.setDisplay(holder);
                            if (mRestorePosition != 0) {
                                player.seekTo(mRestorePosition);
                            } else {
                                if (isVideoPlay) {
                                    player.start();
                                }
                            }
                        }
                        needRestore = false;
                    } else if (needInitVideoPlayer) {
                        if (mPauseMedia != null) {
                            String type = mPauseMedia.getString("type");
                            initMediaPlayer(type.equals("videoMedia"), mPauseMedia.getJSONObject("msg").getString("src"));
                            mPauseMedia = null;
                        } else {
                            isVideoPlay = mInteractSession.getInteractBean().getVideo().getString("status").equals("1");
                            initMediaPlayer(true, mInteractSession.getInteractBean().getVideo().getString("src"));
                        }
                        needInitVideoPlayer = false;
                    }
                } catch (Exception ignored) {
                    Log.e(TAG, "surfaceCreated: [ " + ignored.toString() + " ]");
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.e(TAG, "surfaceDestroyed: ");
                if (mPlayerMap.containsKey("videoMedia")) {
                    IMediaPlayer player = mPlayerMap.get("videoMedia");
                    if (player != null) {
                        player.pause();
                        player.setDisplay(null);
                        mRestorePosition = (int) player.getCurrentPosition();
                        needRestore = true;
                    }
                }
            }
        });
        try {
            if (mInteractSession.getInteractBean().hasMedia()) { // 如果有插播的多媒体 进行播放器初始化
                isMiss = true;
                if (mInteractSession.getInteractBean().getAudio() != null) {
                    isAudioPlay = mInteractSession.getInteractBean().getAudio().getString("status").equals("1");
                    initMediaPlayer(false, mInteractSession.getInteractBean().getAudio().getString("src"));
                }
                if (mInteractSession.getInteractBean().getVideo() != null) {
                    needInitVideoPlayer = true;
                    mRemoteVideoContainer.setVisibility(View.VISIBLE);
                    mRemoteVideo.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception ignored) {
        }

        initDrawPopup();

        loopUserCount();
    }

    private void doRemoteVideoLayoutTouch() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mRemoteVideoContainer.getLayoutParams();
        final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                mRemoteVideoLeft = params.leftMargin;
                mRemoteVideoTop = params.topMargin;
                RelativeLayout.LayoutParams temp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                temp.leftMargin = 0;
                temp.topMargin = 0;
                mRemoteVideoContainer.setLayoutParams(temp);
                int height = DensityUtil.getWidth(StudentActivity.this) * mPlayerMap.get("videoMedia").getVideoHeight() / mPlayerMap.get("videoMedia").getVideoWidth();
                FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
                videoParams.gravity = Gravity.CENTER;
                mRemoteVideo.setLayoutParams(videoParams);
                mRemoteVideoExit.setVisibility(View.VISIBLE);
                isRemoteVideoFullScreen = true;
                if (hasShareScreen) {
                    mShareScreenContainer.setVisibility(View.GONE);
                    mShareScreen.setVisibility(View.GONE);
                }
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                //计算移动的距离
                int offX = (int) (e2.getX() - e1.getX());
                int offY = (int) (e2.getY() - e1.getY());
                params.topMargin = params.topMargin + offY;
                params.leftMargin = params.leftMargin + offX;
                if (params.topMargin < 0) {
                    params.topMargin = 0;
                }
                if (params.leftMargin < 0) {
                    params.leftMargin = 0;
                }
                if (params.topMargin > (DensityUtil.getHeight(StudentActivity.this) - mRemoteVideo.getHeight()
                        - DensityUtil.dp2px(StudentActivity.this, 30))) {
                    params.topMargin = DensityUtil.getHeight(StudentActivity.this) - mRemoteVideo.getHeight() - DensityUtil.dp2px(StudentActivity.this, 30);
                }
                if (params.leftMargin > (DensityUtil.getWidth(StudentActivity.this) - mRemoteVideo.getWidth())) {
                    params.leftMargin = DensityUtil.getWidth(StudentActivity.this) - mRemoteVideo.getWidth();
                }
                mRemoteVideoContainer.setLayoutParams(params);
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });
        mRemoteVideoContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return !isRemoteVideoFullScreen && gestureDetector.onTouchEvent(event);
            }
        });
    }

    private void doShareScreenLayoutTouch() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mShareScreenContainer.getLayoutParams();
        final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                mShareScreenLeft = params.leftMargin;
                mShareScreenTop = params.topMargin;
                RelativeLayout.LayoutParams temp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                temp.leftMargin = 0;
                temp.topMargin = 0;
                mShareScreenContainer.setLayoutParams(temp);
                FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                videoParams.gravity = Gravity.CENTER;
                mShareScreen.setLayoutParams(videoParams);
                mShareScreenExit.setVisibility(View.VISIBLE);
                isShareScreenFullScreen = true;
                dismissRemoteVideoByAnim();
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                //计算移动的距离
                int offX = (int) (e2.getX() - e1.getX());
                int offY = (int) (e2.getY() - e1.getY());
                params.topMargin = params.topMargin + offY;
                params.leftMargin = params.leftMargin + offX;
                if (params.topMargin < 0) {
                    params.topMargin = 0;
                }
                if (params.leftMargin < 0) {
                    params.leftMargin = 0;
                }
                if (params.topMargin > (DensityUtil.getHeight(StudentActivity.this) - mShareScreenContainer.getHeight()
                        - DensityUtil.dp2px(StudentActivity.this, 30))) {
                    params.topMargin = DensityUtil.getHeight(StudentActivity.this) - mShareScreenContainer.getHeight() - DensityUtil.dp2px(StudentActivity.this, 30);
                }
                if (params.leftMargin > (DensityUtil.getWidth(StudentActivity.this) - mShareScreenContainer.getWidth())) {
                    params.leftMargin = DensityUtil.getWidth(StudentActivity.this) - mShareScreenContainer.getWidth();
                }
                mShareScreenContainer.setLayoutParams(params);
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });
        mShareScreenContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return !isShareScreenFullScreen && gestureDetector.onTouchEvent(event);
            }
        });
    }

    private void doDrawLayoutTouch() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mDrawLayout.getLayoutParams();
        mDrawLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //获取到手指处的横坐标和纵坐标
                int x = (int) event.getX();
                int y = (int) event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN://手指按下时
                        lastX = x;
                        lastY = y;
                        break;
                    case MotionEvent.ACTION_MOVE://手指移动时
                        //计算移动的距离
                        int offX = x - lastX;
                        int offY = y - lastY;
                        params.removeRule(RelativeLayout.CENTER_HORIZONTAL);
                        params.topMargin = params.topMargin + offY;
                        params.leftMargin = params.leftMargin + offX;
                        if (params.topMargin < 0) {
                            params.topMargin = 0;
                        }
                        if (params.leftMargin < 0) {
                            params.leftMargin = 0;
                        }
                        if (CCApplication.sClassDirection == 1) {
                            if (params.topMargin > (DensityUtil.getHeight(StudentActivity.this) - mDrawLayout.getHeight())) {
                                params.topMargin = DensityUtil.getHeight(StudentActivity.this) - mDrawLayout.getHeight();
                            }
                            if (params.leftMargin > (DensityUtil.getWidth(StudentActivity.this) - mDrawLayout.getWidth())) {
                                params.leftMargin = DensityUtil.getWidth(StudentActivity.this) - mDrawLayout.getWidth();
                            }
                        } else { // 竖屏 文档全屏
                            if (params.topMargin > (DensityUtil.getWidth(StudentActivity.this) - mDrawLayout.getHeight())) {
                                params.topMargin = DensityUtil.getWidth(StudentActivity.this) - mDrawLayout.getHeight();
                            }
                            if (params.leftMargin > (DensityUtil.getHeight(StudentActivity.this) - mDrawLayout.getWidth())) {
                                params.leftMargin = DensityUtil.getHeight(StudentActivity.this) - mDrawLayout.getWidth();
                            }
                        }
                        mDrawLayout.setLayoutParams(params);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;

                }
                return true;
            }
        });
    }

    private void initDrawPopup() {
        mPopupView = LayoutInflater.from(this).inflate(R.layout.draw_bubble_layout, null);
        mBubbleLayout = (BubbleRelativeLayout) mPopupView.findViewById(R.id.id_bubble_layout);
        mPopupWindow = new BubblePopupWindow(mPopupView, mBubbleLayout);
        mPopupWindow.setCancelOnTouch(false);
        mPopupWindow.setCancelOnTouchOutside(true);
        mSmallSize = (ImageButton) mPopupView.findViewById(R.id.id_small_size);
        mMidSize = (ImageButton) mPopupView.findViewById(R.id.id_mid_size);
        mLargeSize = (ImageButton) mPopupView.findViewById(R.id.id_large_size);
        mColors = (RecyclerView) mPopupView.findViewById(R.id.id_draw_bubble_colors);
        mSmallSize.setOnClickListener(this);
        mMidSize.setOnClickListener(this);
        mLargeSize.setOnClickListener(this);
        mSmallSize.setSelected(true);
        mMidSize.setSelected(false);
        mLargeSize.setSelected(false);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mColors.setLayoutManager(layoutManager);
        final ColorAdapter colorAdapter = new ColorAdapter(this);
        ArrayList<ColorStatus> colorStatuses = new ArrayList<>();
        ColorStatus colorStatus;
        for (int i = 0; i < mColorResIds.length; i++) {
            colorStatus = new ColorStatus();
            if (i == 4) {
                colorStatus.setSelected(true);
            } else {
                colorStatus.setSelected(false);
            }
            colorStatus.setResId(mColorResIds[i]);
            colorStatuses.add(colorStatus);
        }
        colorAdapter.bindDatas(colorStatuses);
        mColors.addItemDecoration(new DividerGridItemDecoration(this));
        mColors.setAdapter(colorAdapter);
        mColors.addOnItemTouchListener(new BaseOnItemTouch(mColors, new OnClickListener() {
            @Override
            public void onClick(RecyclerView.ViewHolder viewHolder) {
                final int position = mColors.getChildAdapterPosition(viewHolder.itemView);
                if (mCurPosition == position) {
                    return;
                }
                ColorStatus pre = new ColorStatus();
                pre.setSelected(false);
                pre.setResId(colorAdapter.getDatas().get(mCurPosition).getResId());
                colorAdapter.update(mCurPosition, pre);
                ColorStatus cur = new ColorStatus();
                cur.setSelected(true);
                cur.setResId(colorAdapter.getDatas().get(position).getResId());
                colorAdapter.update(position, cur);
                mCurPosition = position;
                // 设置颜色
                if (mCurFragment instanceof LectureFragment) {
                    ((LectureFragment) mCurFragment).setColor(mColorValues[position], Integer.parseInt(mColorStr[position], 16));
                }
            }
        }));
    }

    @Override
    public void onClick(View v) {
        clearSizesStatus();
        v.setSelected(!v.isSelected());
        float size = 1.5f;
        int i = v.getId();
        if (i == R.id.id_small_size) {
            size = 1.5f;

        } else if (i == R.id.id_mid_size) {
            size = 4.5f;

        } else if (i == R.id.id_large_size) {
            size = 7.5f;

        }
        // 设置尺寸
        if (mCurFragment instanceof LectureFragment) {
            ((LectureFragment) mCurFragment).setStrokeWidth(size);
        }
    }

    private void clearSizesStatus() {
        mSmallSize.setSelected(false);
        mMidSize.setSelected(false);
        mLargeSize.setSelected(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mChatInput.setFocusableInTouchMode(false);
        mChatInput.setFocusable(false);
        mChatInput.clearFocus();
        if (needInitVideoPlayer) {
            mRemoteVideoContainer.setVisibility(View.VISIBLE);
            mRemoteVideo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isExit && mInteractSession.getTemplate() != CCInteractSession.TEMPLATE_SPEAK) {
            if (isBottomDismiss) {
                animateTopAndBottom(mTopDistance, -mBottomDistance, false);
            } else {
                cancelDismissTopAndBottom();
            }
        } else {
            if (CCApplication.sClassDirection == 0) {
                if (isTopDismiss) {
                    animateTop(mTopDistance);
                }
            } else {
                if (isBottomDismiss) {
                    animateTopAndBottom(mTopDistance, -mBottomDistance, false);
                    if (mCurFragment instanceof LectureFragment) {
                        ((LectureFragment) mCurFragment).setOnlyDoc(false);
                    }
                } else {
                    cancelDismissTopAndBottom();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        mVideoStreamViews = null;
        stopCountDown();
        super.onDestroy();
        unregisterReceiver(mMyBroadcastReceiver);
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }
        DocView.drawingData.clear(); // FIXME 待优化
        // 关闭播放器
        if (!mPlayerMap.isEmpty()) {
            for (Map.Entry<String, IMediaPlayer> player : mPlayerMap.entrySet()) {
                if (player.getValue() != null) {
                    if (mPlayerStatus.get(player.getValue()) == 1 && player.getValue().isPlaying()) {
                        player.getValue().stop();
                    }
                    if (mPlayerType.get(player.getValue())) {
                        player.getValue().setDisplay(null);
                    }
                    player.getValue().release();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode != RESULT_OK) {
//            return;
//        }
        switch (requestCode) {
            case REQUEST_SYSTEM_PICTURE:
                if (data == null) {
                    showToast("图片加载失败");
                    return;
                }
                Uri imageUri = data.getData();// 获得图片的uri
                if (imageUri == null) {
                    showToast("图片加载失败");
                    return;
                }
                String imgPath = getImageAbsolutePath(imageUri);
                if (!TextUtils.isEmpty(imgPath)) {
                    try {
                        compressBitmap(imgPath, readPictureDegree(imgPath));
                    } catch (IOException e) {
                        showToast("图片加载失败");
                    }
                } else {
                    showToast("图片加载失败");
                }
                break;
        }
    }

    @Override
    public void onLecture(int cur, int total) {
        if(total == 0&&CCApplication.sClassDirection == 1){
            mDocBack.setVisibility(View.GONE);
            mDocForward.setVisibility(View.GONE);
            mPageChangeLayout.setVisibility(View.GONE);
        }
        if(((LectureFragment) mCurFragment).getDocInfo() != null && ((LectureFragment) mCurFragment).getDocInfo().isSetupTeacher()
                && CCApplication.sClassDirection == 1){
            mDrawClear.setVisibility(View.VISIBLE);
            if (total > 0 && mPageChangeLayout.getVisibility() != View.VISIBLE) {
                mPageChangeLayout.setVisibility(View.VISIBLE);
            }
            mDocIndex.setText(cur + "/" + total);
            if (total == 1) {
                mDocBack.setEnabled(false);
                mDocForward.setEnabled(false);
                mPageChangeLayout.setVisibility(View.GONE);
            } else if(total > 1){
                mDocBack.setVisibility(View.VISIBLE);
                mDocForward.setVisibility(View.VISIBLE);
                mDocIndex.setVisibility(View.VISIBLE);
                if (cur == 1) {
                    mDocBack.setEnabled(false);
                    mDocForward.setEnabled(true);
                } else if (cur == total) {
                    mDocForward.setEnabled(true);
                    mDocBack.setEnabled(true);
                } else {
                    mDocBack.setEnabled(true);
                    mDocForward.setEnabled(true);
                }
            }
            if (((LectureFragment) mCurFragment).isWhitboard()) {
                mPageChangeLayout.setVisibility(View.GONE);
            }
        } else {
            mDocBack.setVisibility(View.GONE);
            mDocForward.setVisibility(View.GONE);
            mDrawClear.setVisibility(View.GONE);
        }
    }

    private class MyCompressListener implements CompressImage.CompressListener {

        private int degree;

        MyCompressListener(int degree) {
            this.degree = degree;
        }

        @Override
        public void onCompressSuccess(ArrayList<TImage> images) {
            //图片压缩成功
            BufferedOutputStream bos = null;
            try {
                File file = new File(images.get(0).getCompressPath());
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (degree != 0 && bitmap != null) {
                    Matrix m = new Matrix();
                    m.setRotate(degree, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
                    Bitmap temp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                    file.deleteOnExit();
                    bos = new BufferedOutputStream(new FileOutputStream(file));
                    temp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    bitmap.recycle();
                }
                updatePic1(file);
            } catch (Exception e) {
            } finally {
                images.clear();
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        @Override
        public void onCompressFailed(ArrayList<TImage> images, String msg) {
            //图片压缩失败
            showToast("图片压缩失败,停止上传");
        }
    }

    ArrayList<TImage> images = new ArrayList<>();
    CompressConfig config = new CompressConfig.Builder()
            .enableQualityCompress(false)
            .setMaxSize(5 * 1024)
            .create();

    private void compressBitmap(String imgPath, int degree) {
        images.clear();
        File file = new File(imgPath);
        TImage image = TImage.of(file.getAbsolutePath(), TImage.FromType.OTHER);
        images.add(image);
        CompressImage compressImage = CompressImageImpl.of(this, config, images, new MyCompressListener(degree));
        compressImage.compress();
    }

    /**
     * 读取照片exif信息中的旋转角度
     *
     * @param path 照片路径
     * @return角度 获取从相册中选中图片的角度
     */
    private int readPictureDegree(String path) throws IOException {
        if (TextUtils.isEmpty(path)) {
            return 0;
        }
        int degree = 0;
        ExifInterface exifInterface = new ExifInterface(path);
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
        }
        return degree;
    }

    /**
     * 获取token
     */
    private void updatePic1(final File file) {
        mInteractSession.getPicUploadToken(new CCInteractSession.AtlasCallBack<PicToken>() {
            @Override
            public void onSuccess(PicToken picToken) {
                // 开始上传 demo 仅提供思路
                updatePic2(file, picToken);
            }

            @Override
            public void onFailure(String err) {
                toastOnUiThread(err);
            }
        });
    }

    /**
     * 上传
     */
    private void updatePic2(File file, final PicToken picToken) {
        Map<String, String> parms = new HashMap<>();
        parms.put("OSSAccessKeyId", picToken.getAccessid());
        parms.put("policy", picToken.getPolicy());
        parms.put("signature", picToken.getSignature());
        long time = System.currentTimeMillis();
        final String key = picToken.getDir() + "/" + time + "_android.png";
        parms.put("key", key);
        parms.put("success_action_status", "200");
        OKHttpUtil.updateFile(this, picToken.getHost(), file, parms, new OKHttpStatusListener() {
            @Override
            public void onFailed(int code, String errorMsg) {
                toastOnUiThread(errorMsg);
            }

            @Override
            public void onSuccessed(String result) {
                mInteractSession.sendPic(picToken.getHost() + "/" + key);
            }
        });
    }

    public String getImageAbsolutePath(Uri imageUri) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(this, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(contentUri, null, null);
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri))
                return imageUri.getLastPathSegment();
            return getDataColumn(imageUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return imageUri.getPath();
        }
        return null;
    }

    public String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private void updateHandUpFlag(boolean flag) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mClassArrowIcon.getLayoutParams();
        params.removeRule(RelativeLayout.END_OF);
        if (flag) {
            mClassHandIcon.setVisibility(View.VISIBLE);
            params.addRule(RelativeLayout.END_OF, R.id.id_top_class_handup_flag);
            params.setMarginStart(DensityUtil.dp2px(this, 5));
        } else {
            mClassHandIcon.setVisibility(View.GONE);
            params.addRule(RelativeLayout.END_OF, R.id.id_top_class_msg);
            params.setMarginStart(DensityUtil.dp2px(this, 10));
        }
        mClassArrowIcon.setLayoutParams(params);
        isNamedHandup = flag;
    }

    private void updateChatList(final ChatEntity chatEntity) {
        mChatEntities.add(chatEntity);
        mChatAdapter.notifyItemInserted(mChatEntities.size() - 1);
        if (isScroll) {
            mChatList.smoothScrollToPosition(mChatAdapter.getItemCount() - 1);// 进行定位
        }
    }

    private void updateUserCount(final int count) {
        mClassUserNum.setText(String.valueOf(count) + "个成员");
    }

    private void updateChatStatus(String userid, final boolean isAllowChat) {
        if (TextUtils.isEmpty(userid)) {
            Toast.makeText(this, isAllowChat ? "老师关闭全体禁言" : "老师开启全体禁言", Toast.LENGTH_SHORT).show();
            if (!isAllowChat) {
                mChatBtn.setBackgroundResource(R.drawable.student_mute_selector);
            } else {
                if (mInteractSession.isGag()) {
                    mChatBtn.setBackgroundResource(R.drawable.student_mute_selector);
                } else {
                    mChatBtn.setBackgroundResource(R.drawable.student_chat_selector);
                }
            }
            return;
        }
        if (mInteractSession.getUserIdInPusher().equals(userid)) {
            Toast.makeText(this, isAllowChat ? "您被老师关闭禁言" : "您被老师开启禁言", Toast.LENGTH_SHORT).show();
            if (!isAllowChat) {
                mChatBtn.setBackgroundResource(R.drawable.student_mute_selector);
            } else {
                if (mInteractSession.isRoomGag()) {
                    mChatBtn.setBackgroundResource(R.drawable.student_mute_selector);
                } else {
                    mChatBtn.setBackgroundResource(R.drawable.student_chat_selector);
                }
            }
        }
    }
    private boolean authDrawFlag = false;
    private boolean authDrawFlag1 = false;
    private void updateVideos(String userid, boolean flag, boolean isSelf, int changed) {
        CopyOnWriteArrayList<VideoStreamView> videoStreamViews;
        if (mCurFragment instanceof MainVideoFragment) {
            videoStreamViews = ((MainVideoFragment) mCurFragment).getDatas();
        } else {
            videoStreamViews = (CopyOnWriteArrayList<VideoStreamView>) mVideoAdapter.getDatas();
        }
        if (changed == 2) {
            if (mInteractSession.getUserIdInPusher().equals(userid)) { // 如果是当前用户进行授权标注的处理
                isAuthDraw = flag;
                if (!flag) {
                    if (mPopupWindow != null && mPopupWindow.isShowing()) {
                        mPopupWindow.dismiss();
                    }
                }
                if (mCurFragment instanceof LectureFragment) {
                    authDrawFlag1 = flag;
                    if(!authDrawFlag){
                        ((LectureFragment) mCurFragment).authDraw(flag,changed);
                    }
                    if (CCApplication.sClassDirection == 1) {
                        if(!authDrawFlag){
                            mDrawLayout.setVisibility(flag ? View.VISIBLE : View.GONE);
                            mDrawClear.setVisibility(View.GONE);
                            mDocBack.setVisibility(View.GONE);
                            mDocForward.setVisibility(View.GONE);
                            mDocIndex.setVisibility(View.GONE);
                        }
                    }
                }
                Toast.makeText(this, flag ? "您被老师授权标注" : "您被老师取消授权标注", Toast.LENGTH_SHORT).show();
            }
        }
        if (changed == 4) {
            if (mInteractSession.getUserIdInPusher().equals(userid)) { // 如果是当前用户进行授权标注的处理
                isAuthDraw = flag;
                if (!flag) {
                    if (mPopupWindow != null && mPopupWindow.isShowing()) {
                        mPopupWindow.dismiss();
                    }
                }
                if (mCurFragment instanceof LectureFragment) {
                    if(flag){
                        authDrawFlag = true;
                        ((LectureFragment) mCurFragment).authDraw(flag,changed);
                    } else {
                        if(authDrawFlag1) {
                            ((LectureFragment) mCurFragment).authDraw(true,2);
                        } else {
                            ((LectureFragment) mCurFragment).authDraw(flag,changed);
                        }
                        authDrawFlag = false;
                    }
                    if (CCApplication.sClassDirection == 1) {
                        if(flag){
                            authDrawFlag = true;
                            mDrawLayout.setVisibility(View.VISIBLE);
                        } else {
                            if(authDrawFlag1) {
                                mDrawLayout.setVisibility(View.VISIBLE);
                                mDrawClear.setVisibility(View.GONE);
                                mDocBack.setVisibility(View.GONE);
                                mDocForward.setVisibility(View.GONE);
                                mDocIndex.setVisibility(View.GONE);
                            } else {
                                mDrawLayout.setVisibility(View.GONE);
                            }
                            authDrawFlag = false;
                        }
//                        mDrawLayout.setVisibility(flag ? View.VISIBLE : View.GONE);
                    }
                }
                Toast.makeText(this, flag ? "您被老师设为讲师" : "您被老师取消设为讲师", Toast.LENGTH_SHORT).show();
            }
        }
        for (int i = 0; i < videoStreamViews.size(); i++) {
            VideoStreamView videoStreamView = videoStreamViews.get(i);
            if (videoStreamView.getStream().getUserId().equals(userid)) {
                if (changed == 0) {
                    videoStreamView.getStream().setAllowAudio(flag);
                    if (mInteractSession.getUserIdInPusher().equals(userid) && !isSelf) {
                        Toast.makeText(this, flag ? "您被老师开启麦克风" : "您被老师关闭麦克风", Toast.LENGTH_SHORT).show();
                    }
                } else if (changed == 1) {
                    videoStreamView.getStream().setAllowVideo(flag);
                } else if (changed == 2) {
                    videoStreamView.getStream().setAllowDraw(flag);
                } else if (changed == 3) {
                    videoStreamView.getStream().setLock(flag);
                }else if (changed == 4) {
                    videoStreamView.getStream().setSetupTeacher(flag);
                }
                mCurFragment.notifyLayoutManagerRefresh();
                mVideoAdapter.update(i, videoStreamView, changed);
                if (mCurFragment instanceof LectureFragment) { // 如果是讲课模式 判断大屏
                    ((LectureFragment) mCurFragment).updateFullScreenVideoIfShow(flag, changed);
                }
                return;
            }
        }
        if (mCurFragment instanceof MainVideoFragment) { // 如果没有找到对应的数据 判断是否是主视频模式，对主视频进行修改
            ((MainVideoFragment) mCurFragment).updateMainVideo(userid, flag, changed);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInteractEvent(MyEBEvent event) {
        switch (event.what) {
            case Config.INTERACT_EVENT_WHAT_SERVER_CONNECT:
                if (needWait && mInteractSession.getLianmaiMode() == LIANMAI_MODE_AUTO &&
                        mInteractSession.isRoomLive()) {
                    showLoading();
                    mInteractSession.requestLianMai(new CCInteractSession.AtlasCallBack<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dismissLoading();
                        }

                        @Override
                        public void onFailure(String err) {
                            dismissLoading();
                            toastOnUiThread(err);
                        }
                    });
                }
                break;
            case Config.CHAT_IMG:
                mChatImageLayout.setVisibility(View.VISIBLE);
                //TODO glide 无法asBitmap override
                Glide.with(this).load(event.obj)
                        /*.asBitmap()*/
                        //.override(DensityUtil.getWidth(this), DensityUtil.getHeight(this)).
                        //fitCenter()
                        .into(mChatImage);
                break;
            case Config.INTERACT_EVENT_WHAT_CHAT:
                updateChatList((ChatEntity) event.obj);
                break;
            case Config.INTERACT_EVENT_WHAT_ERROR:
                showToast((String) event.obj);
            case Config.INTERACT_EVENT_WHAT_USER_COUNT:
                mCount = (Integer) event.obj + (Integer) event.obj2;
                updateUserCount(mCount);
                break;
            case Config.INTERACT_EVENT_WHAT_USER_GAG:
                updateChatStatus((String) event.obj, (Boolean) event.obj2);
                break;
            case Config.INTERACT_EVENT_WHAT_ALL_GAG:
                updateChatStatus("", (Boolean) event.obj);
                break;
            case Config.INTERACT_EVENT_WHAT_INVITE:
                if (isStop)
                    return;
                showInvite();
                break;
            case Config.INTERACT_EVENT_WHAT_INVITE_CANCEL:
                dismissInvite();
                break;
            case Config.INTERACT_EVENT_WHAT_ROOM_TIMER_START:
                showTimer((long) event.obj, (long) event.obj2);
                break;
            case Config.INTERACT_EVENT_WHAT_ROOM_TIMER_STOP:
                dismissTimer();
                break;
            case Config.INTERACT_EVENT_WHAT_CLASS_STATUS_START:
                mOtherScenes.setVisibility(View.GONE);
                if (CCApplication.sClassDirection == 1 && mCurFragment instanceof LectureFragment) {
                    //  更新图标
                    mVideoController.setBackgroundResource(R.drawable.draw_hide);
                    mVideoController.setVisibility(View.VISIBLE);
                    isVideoShow = true;
                }
                mCurFragment.getRecyclerView().setVisibility(View.VISIBLE);
                if (mCurFragment instanceof LectureFragment) {
                    ((LectureFragment) mCurFragment).restoreNormal();
                }
                if (isAuthDraw && mCurFragment instanceof LectureFragment
                        && (CCApplication.sClassDirection == 1 || ((LectureFragment) mCurFragment).isDocFullScreen())) {
                    mDrawLayout.setVisibility(View.VISIBLE);
                }
                if (mInteractSession.getLianmaiMode() == CCInteractSession.LIANMAI_MODE_AUTO) {
                    showLoading();
                    mInteractSession.requestLianMai(new CCInteractSession.AtlasCallBack<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dismissLoading();
                        }

                        @Override
                        public void onFailure(String err) {
                            dismissLoading();
                            toastOnUiThread(err);
                        }
                    });
                }
                break;
            case Config.INTERACT_EVENT_WHAT_CLASS_STATUS_STOP:
                dismissVote(null);
                dismissVoteResult();
                dismissNamed();
                mDrawLayout.setVisibility(View.GONE);
                mTeacherGoneLayout.setVisibility(View.GONE);
                mOtherScenes.setVisibility(View.VISIBLE);
                mNoClassLayout.setVisibility(View.VISIBLE);
                mVideoController.setVisibility(View.GONE);
                if (isBottomDismiss) {
                    animateTopAndBottom(mTopDistance, -mBottomDistance, false);
                } else {
                    cancelDismissTopAndBottom();
                }
                mVideoStreamViews.clear();
                for (BaseFragment fragment :
                        mFragments) {
                    fragment.clearDatas();
                }
                updateMaiButton(MAI_STATUS_NORMAL);
                mCurFragment.classStop();
                if (mHandup.getVisibility() == View.VISIBLE) {
                    isAutoHandup = false;
                    mHandup.setBackgroundResource(R.drawable.handup_selector);
                }
                break;
            case Config.INTERACT_EVENT_WHAT_UP_MAI:
                publish();
                break;
            case Config.INTERACT_EVENT_WHAT_DOWN_MAI:
                unpublish();
                break;
            case Config.INTERACT_EVENT_WHAT_UPDATE_MEDIA_MODE:
                if (mMaiStatus == 3) { // 当前用户在连麦中
                    if ((int) event.obj == CCInteractSession.MEDIA_MODE_BOTH) {
                        mInteractSession.enableVideo(true);
                    } else {
                        mInteractSession.disableVideo(true);
                    }
                }
                if (mCurFragment instanceof TilingFragment) {
                    mCurFragment.notifyLayoutManagerRefresh();
                }
                mVideoAdapter.notifyDataSetChanged();
                break;
            case Config.INTERACT_EVENT_WHAT_UPDATE_LIANMAI_MODE:
                int mode = (int) (event).obj;
                mLianmaiStyle.setVisibility(View.VISIBLE);
                if (mode == LIANMAI_MODE_AUTO) {
                    mHandup.setVisibility(View.VISIBLE);
                    if (mMaiStatus == CCInteractSession.LIANMAI_STATUS_MAI_ING) {
                        updateMaiButton(CCInteractSession.LIANMAI_STATUS_MAI_ING);
                    } else {
                        mLianmaiStyle.setVisibility(View.GONE);
                    }
                } else {
                    mHandup.setVisibility(View.GONE);
                    if (mode == CCInteractSession.LIANMAI_MODE_FREE) { // 切换到自由连麦 更新左上角举手标识
                        if (isNamedHandup) {
                            // 更新连麦按钮
                            sortUser(mInteractSession.getUserList());
                        }
                        updateHandUpFlag(false);
                    }
                    // 更新连麦按钮
                    updateMaiButton(mMaiStatus);
                }

                break;
            case Config.INTERACT_EVENT_WHAT_QUEUE_MAI:
                ArrayList<User> users = (ArrayList<User>) event.obj; // 重新赋值
                if (mInteractSession.getLianmaiMode() == CCInteractSession.LIANMAI_MODE_NAMED) { // 点名连麦模式
                    boolean flag = false;
                    for (User user :
                            users) {
                        if (user.getLianmaiStatus() == CCInteractSession.LIANMAI_STATUS_IN_MAI) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag != isNamedHandup)
                        updateHandUpFlag(flag);
                } else { // 自由连麦模式
                    if (mMaiStatus == 1) { // 如果麦序发生变化 并且当前用户在麦序中 进行麦序计算
                        sortUser(users);
                    }
                }
                break;
            case Config.INTERACT_EVENT_WHAT_HANDUP:
                String userid = (String) event.obj;
                if (userid.equals(mInteractSession.getUserIdInPusher())) {
                    isAutoHandup = (boolean) event.obj2;
                    mHandup.setBackgroundResource(isAutoHandup ? R.drawable.handup_cancel_selector : R.drawable.handup_selector);
                }
                break;
            case Config.INTERACT_EVENT_WHAT_STREAM_ADDED:
                addStreamView((SubscribeRemoteStream) event.obj);
                break;
            case Config.INTERACT_EVENT_WHAT_STREAM_REMOVED:
                removeStreamView((SubscribeRemoteStream) event.obj);
                break;
            case Config.INTERACT_EVENT_WHAT_STREAM_ERROR:
                unpublish();
                break;
            case Config.INTERACT_EVENT_WHAT_MAIN_VIDEO_FOLLOW:
                userid = (String) event.obj;
                ((MainVideoFragment) mCurFragment).findMainVideoByUseridToDisplay(userid);
                break;
            case Config.INTERACT_EVENT_WHAT_TEMPLATE:
                setSelected((Integer) event.obj);
                break;
            case Config.INTERACT_EVENT_WHAT_START_NAMED:
                if (isStop)
                    return;
                showNamed((Integer) event.obj);
                break;
            case Config.INTERACT_EVENT_WHAT_ROOM_ROLL_CALL_START:
                if (isStop)
                    return;
                showVote((Vote) event.obj);
                break;
            case Config.INTERACT_EVENT_WHAT_ROOM_ROLL_CALL_STOP:
                dismissVote((String) event.obj);
                break;
            case Config.INTERACT_EVENT_WHAT_ROOM_ROLL_CALL_RESULT:
                if (isStop)
                    return;
                showVoteResult((VoteResult) event.obj);
                break;
            case Config.INTERACT_EVENT_WHAT_SERVER_DISCONNECT:
                isExit = true;
                mNotifyPopup.show(mRoot);
                break;
            case Config.INTERACT_EVENT_WHAT_USER_AUDIO:
                updateVideos((String) event.obj, (Boolean) event.obj2, (Boolean) event.obj3, 0);
                break;
            case Config.INTERACT_EVENT_WHAT_USER_VIDEO:
                updateVideos((String) event.obj, (Boolean) event.obj2, (Boolean) event.obj3, 1);
                break;
            case Config.INTERACT_EVENT_WHAT_AUTH_DRAW:
                updateVideos((String) event.obj, (Boolean) event.obj2, false, 2);
                break;
            case Config.INTERACT_EVENT_WHAT_LOCK:
                updateVideos((String) event.obj, (Boolean) event.obj2, mInteractSession.getUserIdInPusher().equals(event.obj), 3);
                break;
            case Config.INTERACT_EVENT_WHAT_SETUP_THEACHER:
                updateVideos((String) event.obj, (Boolean) event.obj2, false, 4);
                break;
            case Config.INTERACT_EVENT_WHAT_SETUP_THEACHER_PAGE:
                ((LectureFragment) mCurFragment).setDocInfo((DocInfo) event.obj, (int) event.obj2, 0);
                break;
            case Config.INTERACT_EVENT_WHAT_DOC_CHANGE:
                ((LectureFragment) mCurFragment).setupTeacherFlag((DocInfo) event.obj, (int) event.obj2);
                break;
            case Config.INTERACT_EVENT_WHAT_INTERLUDE_MEDIA:
                try {
                    JSONObject media = (JSONObject) event.obj;
                    String handler = media.getString("handler");
                    String type = media.getString("type");
                    if (handler.equals("init")) {
                        isMiss = false;
                        if (type.equals("videoMedia")) {
                            isVideoPlay = false;
                        } else {
                            isAudioPlay = false;
                        }
                        if (!isPause) {
                            initMediaPlayer(type.equals("videoMedia"), media.getJSONObject("msg").getString("src"));
                        } else {
                            mPauseMedia = media;
                            needInitVideoPlayer = true;
                        }
                    } else {
                        IMediaPlayer mediaPlayer = mPlayerMap.get(type);
                        switch (handler) {
                            case "play":
                                if (type.equals("videoMedia")) {
                                    isVideoPlay = true;
                                } else {
                                    isAudioPlay = true;
                                }
                                if (mediaPlayer == null) {
                                    showToast("播放器未初始化");
                                    return;
                                }
                                if (mPlayerStatus.get(mediaPlayer) == 1 && !mediaPlayer.isPlaying()) {
                                    if (type.equals("videoMedia") && isPause) {
                                        return;
                                    }
                                    Log.e(TAG, "start: ");
                                    mediaPlayer.start();
                                } else {
                                    Log.e(TAG, "player: [ " + mPlayerStatus.get(mediaPlayer) + "] [ " + mediaPlayer.isPlaying() + " ]");
//                                    showToast(mPlayerStatus.get(mediaPlayer) + "-" + mediaPlayer.isPlaying());
                                }
                                break;
                            case "pause":
                                if (type.equals("videoMedia")) {
                                    isVideoPlay = false;
                                } else {
                                    isAudioPlay = false;
                                }
                                if (mediaPlayer == null) {
                                    showToast("播放器未初始化");
                                    return;
                                }
                                if (mPlayerStatus.get(mediaPlayer) == 1 && mediaPlayer.isPlaying()) {
                                    if (type.equals("videoMedia") && isPause) {
                                        return;
                                    }
                                    Log.e(TAG, "pause: ");
                                    mediaPlayer.pause();
                                } else {
                                    Log.e(TAG, "player: [ " + mPlayerStatus.get(mediaPlayer) + "] [ " + mediaPlayer.isPlaying() + " ]");
//                                    showToast(mPlayerStatus.get(mediaPlayer) + "-" + mediaPlayer.isPlaying());
                                }
                                break;
                            case "timeupdate":
                                if (mediaPlayer == null) {
                                    showToast("播放器未初始化");
                                    return;
                                }
                                if (mPlayerStatus.get(mediaPlayer) == 1) {
                                    if (type.equals("videoMedia") && isPause) {
                                        return;
                                    }
                                    mediaPlayer.seekTo((int) (media.getJSONObject("msg").getDouble("time") * 1000));
                                } else {
                                    Log.e(TAG, "player: [ " + mPlayerStatus.get(mediaPlayer) + "] [ " + mediaPlayer.isPlaying() + " ]");
//                                    showToast(mPlayerStatus.get(mediaPlayer) + "-" + mediaPlayer.isPlaying());
                                }
                                break;
                            case "close":
                                mPlayerMap.remove(type); // 移除对应的播放器
                                // 视频去掉播放框
                                if (type.equals("videoMedia") && mRemoteVideoContainer.getVisibility() == View.VISIBLE) {
                                    mRemoteVideoContainer.setVisibility(View.GONE);
                                    mRemoteVideo.setVisibility(View.GONE);
                                }
                                if (mediaPlayer == null) {
                                    showToast("播放器未初始化");
                                    return;
                                }
                                if (mPlayerStatus.get(mediaPlayer) == 1) {
                                    mediaPlayer.stop();
                                }
                                mPlayerStatus.remove(mediaPlayer);
                                mPlayerType.remove(mediaPlayer);
                                mediaPlayer.release();
                                break;
                            default:
                                showToast("unkonw handler");
                                break;
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "插播音视频数据解析异常: [ " + e.getMessage() + " ]");
                    showToast("插播音视频数据解析异常 [ " + e.getMessage() + " ]");
                    // TODO: 2017/11/13  是否需要释放播放器资源和处理播放框（如果显示）
                }
                break;
            case Config.INTERACT_EVENT_WHAT_TEACHER_DOWN:
                break;
//            case Config.INTERACT_EVENT_WHAT_NOTIFY_PUBLISH:
//                Log.i(TAG, "onInteractEvent: " + (String)event.obj + "----->" + (String)event
//                        .obj2 + "----->2" + mSelfStreamView.getStream().getUserId());
//                mInteractSession.rePublishEvent((String)event.obj,(String)event
//                        .obj2,mSelfStreamView.getStream().getUserId());
//                break;
//            case Config.INTERACT_EVENT_WHAT_INTERRUPT_PUBLISH:
//                try {
//                    JSONObject republish = (JSONObject) event.obj;
//                    String SelfUserid = republish.getString("userid");
//                    String SelfRoomid = republish.getString("streamid");
//                    if(mSelfStreamView.getStream().getUserId().equals(SelfUserid)){
//                        Log.i(TAG, "wdh----->1onInteractEvent: " + SelfUserid + "----" +
//                                SelfRoomid);
//                        publish();
//                    }
//                }catch (JSONException e) {
//                    Log.e(TAG, "中断推流数据解析异常: [ " + e.getMessage() + " ]");
//                }
//                break;
            case Config.INTERACT_EVENT_WHAT_VIDEO_CONTROL:
                String  VideoUserid = (String) event.obj;
                String VideoType = (String) event.obj2;
                if (CCApplication.sClassDirection == 0) {
                    ((LectureFragment) mCurFragment).setVideoScale(VideoUserid, VideoType);
                } else {
                    ((LectureFragment) mCurFragment).setVideoScale(VideoUserid, VideoType);
                }
                break;
            case Config.INTERACT_EVENT_WHAT_KICK_OUT:
                isExit = true;
                isKick = true;
                showToast("对不起，您已经被踢出该直播间");
                CCApplication.mAreaCode = mInteractSession.isAreaCode();
                ValidateActivity.startSelf(this, mRoomDes.getName(), mRoomDes.getDesc(), mRoomId, mUserId, CCInteractSession.TALKER, mRoomDes.getAuthtype() == 2);
                finish();
        }
    }

    private void initMediaPlayer(final boolean isVideo, String mediaPath) {
        IMediaPlayer player = mPlayerMap.get(isVideo ? "videoMedia" : "audioMedia");
        try {
            if (player != null) {
                mPlayerMap.remove(isVideo ? "videoMedia" : "audioMedia");
                if (isVideo) {
                    player.setDisplay(null);
                    if (mRemoteVideoContainer.getVisibility() == View.VISIBLE) { // 视频
                        //  去除上一个界面
                        mRemoteVideoContainer.setVisibility(View.GONE);
                        mRemoteVideo.setVisibility(View.GONE);
                        mRemoteVideoExit.setVisibility(View.GONE);
                    }
                }
                if (mPlayerStatus.get(player) == 1) {
                    player.stop();
                }
                mPlayerType.remove(player);
                mPlayerStatus.remove(player);
                player.release();
                player = null;
            }
            player = new IjkMediaPlayer();
            mPlayerMap.put(isVideo ? "videoMedia" : "audioMedia", player);
            mPlayerType.put(player, isVideo);
            mPlayerStatus.put(player, 0);
            if (isVideo && mRemoteVideoContainer.getVisibility() != View.VISIBLE) { // 视频
                //  显示视频区域
                mRemoteVideoContainer.setVisibility(View.VISIBLE);
                FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(DensityUtil.dp2px(this, 160),
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                videoParams.gravity = Gravity.CENTER;
                mRemoteVideo.setLayoutParams(videoParams);
                mRemoteVideo.setVisibility(View.VISIBLE);
                isRemoteVideoFullScreen = false;
            }
            player.setLooping(true); // 设置循环播放
            player.setScreenOnWhilePlaying(true);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            ((IjkMediaPlayer)player).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
            Log.e(TAG, "initMediaPlayer: ");
            player.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer mp) {
                    if (isPause) {
                        mPlayerMap.remove(isVideo ? "videoMedia" : "audioMedia");
                        mPlayerType.remove(mp);
                        mPlayerStatus.remove(mp);
                        mp.stop();
                        mp.setDisplay(null);
                        mp.release();
                        return;
                    }
                    Log.e(TAG, "onPrepared: ");
                    if (mPlayerType.get(mp)) {
                        mp.setDisplay(mRemoteVideo.getHolder());
                        //  重新进行尺寸调整
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mRemoteVideoContainer.getLayoutParams();
                        params.width = DensityUtil.dp2px(StudentActivity.this, 160);
                        params.height = DensityUtil.dp2px(StudentActivity.this, 90);
                        mRemoteVideoContainer.setLayoutParams(params);
                    }
                    mPlayerStatus.put(mp, 1);
                    if (isMiss) {
                        try {
                            int msec;
                            if (mPlayerType.get(mp)) {
                                msec = (int) (mInteractSession.getInteractBean().getVideo().getDouble("current_time") * 1000);
                            } else {
                                msec = (int) (mInteractSession.getInteractBean().getAudio().getDouble("current_time") * 1000);
                            }
                            if (msec > 0) {
                                mp.seekTo(msec);
                            }
                        } catch (JSONException ignored) {
                        }
                    } else {
                        if (mPlayerType.get(mp)) { // 视频
                            if (isVideoPlay) {
                                mp.start();
                            }
                        } else { // 音频
                            if (isAudioPlay) {
                                mp.start();
                            }
                        }
                    }
                }
            });
            player.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(IMediaPlayer mp) {
                    if (mPlayerType.get(mp)) { // 视频
                        if (!isVideoPlay && mp.isPlaying()) {
                            mp.pause();
                        }
                        if (isVideoPlay && !mp.isPlaying()) {
                            mp.start();
                        }
                    } else { // 音频
                        if (!isAudioPlay && mp.isPlaying()) {
                            mp.pause();
                        }
                        if (isAudioPlay && !mp.isPlaying()) {
                            mp.start();
                        }
                    }
                }
            });
            player.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer mp, int what, int extra) {
                    mPlayerStatus.put(mp, -1);
                    mp.reset();
                    mp.release(); // 释放播放器资源
                    Log.e(TAG, "onError: [ " + what + "-" + extra + " ]");
                    toastOnUiThread("播放出错 [ " + what + "-" + extra + " ]");
                    // 移除播放器
                    if (mPlayerType.get(mp)) {
                        mPlayerMap.remove("videoMedia");
                    } else {
                        mPlayerMap.remove("audioMedia");
                    }
                    // 视频去掉播放框
                    if (mPlayerType.get(mp) && mRemoteVideoContainer.getVisibility() == View.VISIBLE) {
                        mRemoteVideoContainer.setVisibility(View.GONE);
                        mRemoteVideo.setVisibility(View.GONE);
                    }
                    return false;
                }
            });
            player.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer iMediaPlayer) {

                }
            });
            player.setDataSource(mediaPath);
            player.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, "initMediaPlayer: [ " + e.toString() + " ]");
            showToast("初始化播放器失败 [ " + e.toString() + " ]");
            if (player != null) {
                mPlayerMap.remove(isVideo ? "videoMedia" : "audioMedia");
                mPlayerType.remove(player);
                mPlayerStatus.remove(player);
                player.stop();
                player.setDisplay(null);
                player.release();
                return;
            }
        }
    }

    private void dismissTimer() {
        if (mRoomTimerLayout.getVisibility() == View.GONE) {
            return;
        }
        mAnimatorSet.cancel();
        stopCountDown();
        mRoomTimerLayout.setVisibility(View.GONE);
    }

    private void showTimer(long startTime, long lastTime) {
        if (mRoomTimerLayout.getVisibility() == View.VISIBLE) {
            stopCountDown();
        }
        long endTime = startTime + lastTime;
        mRoomTime = (endTime - System.currentTimeMillis()) / 1000;
        if (mRoomTime <= 0) {
            mRoomTime = 0;
            updateTimeTip();
            startAnimTip();
            return;
        }
        mRoomTimerLayout.setVisibility(View.VISIBLE);
        mTimerValue.setTextColor(Color.parseColor("#FFFFFF"));
        updateTimeTip();
        startCountDown();
    }

    private void updateTimeTip() {
        if (mTimerValue != null) {
            mTimerValue.setText(TimeUtil.formatNamed99(mRoomTime));
        }
    }

    /**
     * 开始倒计时
     */
    private void startCountDown() {
        if (!isStartTimer) {
            isStartTimer = true;
            mRoomTimerHandler.postDelayed(mRoomTimerTask, 1000);
        }
    }

    /**
     * 结束倒计时
     */
    private void stopCountDown() {
        if (isStartTimer) {
            isStartTimer = false;
            mRoomTimerHandler.removeCallbacks(mRoomTimerTask);
        }
    }

    /**
     * 开始动画提示
     */
    private void startAnimTip() {
        mTimerValue.setTextColor(Color.parseColor("#ffd72113"));
        ValueAnimator alphaAnimator = ObjectAnimator.ofInt(1, 100).
                setDuration(1000);
        alphaAnimator.setRepeatCount(-1);
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                int diff = 255 - (int) (255 * fraction);
                String alpha;
                if (diff < 10) {
                    alpha = "0" + diff;
                } else {
                    alpha = Integer.toHexString(diff);
                    if (alpha.length() == 1) {
                        alpha = "0" + alpha;
                    }
                }
                String color = "#" + alpha + "f93930";
                if (mTimerValue != null) {
                    mTimerValue.setTextColor(Color.parseColor(color));
                }
            }
        });
        alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mAnimatorSet.playTogether(alphaAnimator);
        mAnimatorSet.start();
    }

    private final class SubCallBack implements CCInteractSession.AtlasCallBack<SubscribeRemoteStream> {

        private VideoStreamView videoStreamView;

        SubCallBack(VideoStreamView videoStreamView) {
            this.videoStreamView = videoStreamView;
        }

        @Override
        public void onSuccess(SubscribeRemoteStream stream) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        videoStreamView.getStream().attach(videoStreamView.getRenderer());
                        int position;
                        if (videoStreamView.getStream().getUserRole() == CCInteractSession.PRESENTER) {
                            position = 0;
                            mTeacherGoneLayout.setVisibility(View.GONE);
                        } else {
                            position = mVideoStreamViews.size();
                        }
                        mVideoStreamViews.add(position, videoStreamView);
                        mCurFragment.notifyItemChanged(videoStreamView, position, true);

                        String videocontrol = mInteractSession.getVideoZoom();
                        if(videocontrol!=null) {
                            if (mCurFragment instanceof LectureFragment) {
                                ((LectureFragment) mCurFragment).setVideoScale(videocontrol, "big");
                            }
                        } else {
                            if (mCurFragment instanceof LectureFragment) {
                                ((LectureFragment) mCurFragment).isVideSale(mSelfStreamView);
                            }
                        }

                    } catch (StreamException e) {
                        showToast(e.getMessage());
                    }
                }
            });
        }

        @Override
        public void onFailure(String err) {
            toastOnUiThread(err);
            // TODO: 2017/11/8 关键缓冲池
        }
    }

    /**
     * 展示屏幕共享流
     */
    private void showShareScreen(SubscribeRemoteStream stream) {
        try {
            isRemoveShareScreen = false;
            mInteractSession.subscribe(stream, new CCInteractSession.AtlasCallBack<SubscribeRemoteStream>() {
                @Override
                public void onSuccess(final SubscribeRemoteStream stream) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                stream.attach(mShareScreen);
                                mShareScreen.setVisibility(View.VISIBLE);
                                hasShareScreen = true;
                                mShareScreenContainer.setVisibility(View.VISIBLE);
                            } catch (StreamException e) {
                                showToast(e.getMessage());
                            }
                        }
                    });
                }

                @Override
                public void onFailure(String err) {
                    toastOnUiThread(err);
                }
            });
        } catch (Exception e) {
            showToast(e.getMessage());
        }
    }

    private synchronized void addStreamView(final SubscribeRemoteStream stream) {
        if (stream.getRemoteStream().getStreamType() == CCStream.REMOTE_SCREEN) {
            showShareScreen(stream);
            return;
        }
        CCSurfaceRenderer surfaceRenderer;
        if (!mViewPool.isEmpty()) {
            surfaceRenderer = mViewPool.remove(0); // 从池子中获取
        } else {
            surfaceRenderer = new CCSurfaceRenderer(this);
            mInteractSession.initSurfaceContext(surfaceRenderer);
        }
        surfaceRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        final VideoStreamView videoStreamView = new VideoStreamView();
        videoStreamView.setRenderer(surfaceRenderer);
        videoStreamView.setStream(stream);
        final Timer timer = new Timer();
        try {
            mInteractSession.subscribe(stream, new SubCallBack(videoStreamView));
        } catch (StreamException e) {
            showToast(e.getMessage());
        }

        timer.schedule(new TimerTask() {
            public void run() {
                mInteractSession.getConnectionStats(stream,new CCInteractSession.AtlasCallBack<ConnectionStatsWrapper>(){

                    @Override
                    public void onSuccess(ConnectionStatsWrapper connectionStats) {

                        if(connectionStats.bytesReceived == 0 || connectionStats.frameWidthReceived ==0
                                && connectionStats.frameHeightReceived == 0) {
                            try {
                                stream.detach();
                                mInteractSession.unsubscribe(stream, new CCInteractSession.AtlasCallBack<Void>() {

                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        try {
                                            mInteractSession.subscribe(stream,new SubCallBack(videoStreamView));
                                        } catch (StreamException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(String err) {

                                    }
                                });
                            } catch (StreamException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(String err) {
                        Log.i(TAG, "onFailure: " + err);
                    }
                });
            }
        }, 10000);
    }

    private synchronized void removeStreamView(SubscribeRemoteStream stream) {
        if (stream.getRemoteStream().getStreamType() == CCStream.REMOTE_SCREEN) {
            dismissShareScreen(stream);
            return;
        }
        VideoStreamView tempView = null;
        try {
            if (mCurFragment instanceof LectureFragment) {
                ((LectureFragment) mCurFragment).exitVideoFullScreenIfShow(stream);
            } else {
                if (stream.getUserRole() == CCInteractSession.PRESENTER) {
                    if (mInteractSession.isRoomLive()) {
                        mTeacherGoneLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
            int position = -1;
            for (int i = 0; i < mVideoStreamViews.size(); i++) {
                VideoStreamView streamView = mVideoStreamViews.get(i);
                if (streamView.getStream().getUserId().equals(stream.getUserId())) {
                    tempView = streamView;
                    position = i;
                    break;
                }
            }
            if (tempView == null)
                return;

            if(position > 0){
                if (mCurFragment instanceof LectureFragment) {
                    ((LectureFragment) mCurFragment).videoFlah(position);
                }
            }

            mVideoStreamViews.remove(tempView);
            mViewPool.add(tempView.getRenderer());// 空闲view添加到池子中
            mCurFragment.notifyItemChanged(tempView, position, false);
            stream.detach();
            mInteractSession.unsubscribe(stream, null);
        } catch (StreamException ignore) {
        } finally {
            if (tempView != null) {
                tempView.getRenderer().cleanFrame();
            }
        }
    }

    private void dismissShareScreen(SubscribeRemoteStream stream) {
        try {
            isRemoveShareScreen = true;
            stream.detach();
            mInteractSession.unsubscribe(stream, null);
        } catch (StreamException ignored) {
        } finally {
            mShareScreen.cleanFrame();
            RelativeLayout.LayoutParams temp = new RelativeLayout.LayoutParams(DensityUtil.dp2px(StudentActivity.this, 160), DensityUtil.dp2px(StudentActivity.this, 90));
            temp.leftMargin = 0;
            temp.topMargin = 0;
            mShareScreenContainer.setLayoutParams(temp);
            FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(DensityUtil.dp2px(StudentActivity.this, 160), DensityUtil.dp2px(StudentActivity.this, 90));
            videoParams.gravity = Gravity.CENTER;
            mShareScreen.setLayoutParams(videoParams);
        }
    }

    private synchronized void setSelected(int template) {
        if (mCurFragment != null) {
            mCurFragment.clearDatas();
            cancelDismissTopAndBottom();
        }
        if (mCurFragment != null && mCurFragment instanceof LectureFragment) {
            ((LectureFragment) mCurFragment).dealWithFullscreen();
        }
        mCurFragment = mFragments.get(mTemplatePosition.get(template));
        mTeacherGoneLayout.setVisibility(View.GONE);
        if (!(mCurFragment instanceof LectureFragment)) {
            if (mVideoStreamViews.size() > 0 &&
                    mVideoStreamViews.get(0).getStream().getUserRole() != CCInteractSession.PRESENTER) {
                mTeacherGoneLayout.setVisibility(View.VISIBLE);
            }
        }
        mVideoAdapter.setType(template);
        mCurFragment.addDatas(mVideoStreamViews);
        getSupportFragmentManager().beginTransaction().
                replace(R.id.id_student_content, mCurFragment)
                .commitAllowingStateLoss();
        if (mCurFragment instanceof LectureFragment) {
            if (isAuthDraw && CCApplication.sClassDirection == 1) {
                mDrawLayout.setVisibility(View.VISIBLE);
            }
            ((LectureFragment) mCurFragment).authDraw(isAuthDraw,0);
        } else {
            mDrawLayout.setVisibility(View.GONE);
            mChatList.setVisibility(View.VISIBLE);
            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            }
        }
        if (mInteractSession.isRoomLive()) { //直播开始进行界面操作区域的动画
            if (CCApplication.sClassDirection == 1 && mCurFragment instanceof LectureFragment) {
                mVideoController.setVisibility(View.VISIBLE);
                isVideoShow = true;
                mVideoController.setBackgroundResource(R.drawable.draw_hide);
            } else {
                mVideoController.setVisibility(View.GONE);
            }
            if (isBottomDismiss) {
                if (template == CCInteractSession.TEMPLATE_SPEAK) {
                    animateTopAndBottom(mTopDistance, -mBottomDistance, false);
                } else {
                    animateTopAndBottom(mTopDistance, -mBottomDistance, true);
                }
            } else {
                if (mInteractSession.getTemplate() != CCInteractSession.TEMPLATE_SPEAK) {
                    executeDismissTopAndBottom();
                } else {
                    if (isTopDismiss) {
                        animateTop(mTopDistance);
                    }
                }
            }
        } else {
            mVideoController.setVisibility(View.GONE);
        }
    }

    private void animateTop(int top) {
        if (mTopLayout != null) {
            mTopLayout.animate().cancel();
            mTopLayout.animate().translationYBy(top).setDuration(100).withEndAction(new Runnable() {
                @Override
                public void run() {
                    isTopDismiss = !isTopDismiss;
                }
            }).start();
            if (mCurFragment instanceof MainVideoFragment) {
                ((MainVideoFragment) mCurFragment).animateTop(top);
            } else if (mCurFragment instanceof LectureFragment) {
                ((LectureFragment) mCurFragment).animateTop(top);
            }
        }
    }

    private void animateBottom(int bottom, final boolean isDoDismiss) {
        if (mBottomLayout != null) {

            if (mCurFragment instanceof LectureFragment && CCApplication.sClassDirection == 1 /*&& isAuthDraw*/) { // 文档模式 并且是横屏 是否需要判断是否授权
                mChatList.setVisibility(isDoDismiss ? View.VISIBLE : View.GONE);
            }
            mBottomLayout.animate().cancel();
            mBottomLayout.animate().translationYBy(bottom).setDuration(100).withEndAction(new Runnable() {
                @Override
                public void run() {
                    mCurFragment.restoreClick();
                    isBottomDismiss = !isBottomDismiss;
                    if (isDoDismiss) {
                        executeDismissTopAndBottom();
                    }
                }
            }).start();
        }
    }

    private void animateTopAndBottom(int top, int bottom, boolean isDoDismissTask) {
        animateTop(top);
        animateBottom(bottom, isDoDismissTask);
    }

    private Runnable dismissTopAndBottomTask = new Runnable() {
        @Override
        public void run() {
            if (mTopDistance == -1) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTopLayout.getLayoutParams();
                mTopDistance = params.topMargin + mTopLayout.getHeight();
            }
            if (mBottomDistance == -1) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBottomLayout.getLayoutParams();
                mBottomDistance = params.bottomMargin + mBottomLayout.getHeight();
            }
            if (isCancelTask) {
                return;
            }
            animateTopAndBottom(-mTopDistance, mBottomDistance, false);
        }
    };

    private void executeDismissTopAndBottom() {
        isCancelTask = false;
        mHandler.postDelayed(dismissTopAndBottomTask, 3000);
    }

    private void cancelDismissTopAndBottom() {
        isCancelTask = true;
        mHandler.removeCallbacks(dismissTopAndBottomTask);
    }

    private void initExitPopup() {
        mExitPopup = new CommonPopup(this);
        mExitPopup.setOutsideCancel(true);
        mExitPopup.setKeyBackCancel(true);
        mExitPopup.setTip("是否确认离开课堂？");
        mExitPopup.setOKClickListener(new CommonPopup.OnOKClickListener() {
            @Override
            public void onClick() {
                exit();
            }
        });
    }

    private void initCancelPopup() {
        mCancelPopup = new BottomCancelPopup(this);
        mCancelPopup.setOutsideCancel(true);
        mCancelPopup.setKeyBackCancel(true);
        ArrayList<String> datas = new ArrayList<>();
        datas.add("切换摄像头");
        datas.add("关闭摄像头");
        datas.add("关闭麦克风");
        if(mInteractSession.getshowExit() == 1){
            datas.add("下麦");
        }
        mCancelPopup.setChooseDatas(datas);
        mCancelPopup.setIndexColor(3, Color.parseColor("#D32F2F"));
        mCancelPopup.setOnChooseClickListener(new BottomCancelPopup.OnChooseClickListener() {
            @Override
            public void onClick(int index) {
                if (index == 0) {
                    if (mInteractSession.isAllowVideo()) {
                        mInteractSession.switchCamera(mSelfRenderer, new CCInteractSession.AtlasCallBack<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }

                            @Override
                            public void onFailure(String err) {
                                toastOnUiThread(err);
                            }
                        });
                    } else {
                        showToast("不支持当前操作，摄像头被关闭了");
                    }
                } else if (index == 1) {
                    if (mInteractSession.getMediaMode() == CCInteractSession.MEDIA_MODE_BOTH) {
                        if (mInteractSession.isAllowVideo()) {
                            mInteractSession.disableVideo(true);
                            mSelfRenderer.cleanFrame();
                        } else {
                            mInteractSession.enableVideo(true);
                        }
                    } else {
                        showToast("老师已经设置当前直播间的连麦模式为仅音频");
                    }
                } else if (index == 2) {
                    if (mInteractSession.isAllowAudio()) {
                        mInteractSession.disableAudio(true);
                    } else {
                        if (mInteractSession.isAllAllowAudio()) {
                            mInteractSession.enableAudio(true);
                        } else {
                            showToast("老师已经设置当前直播间的麦克风关闭");
                        }
                    }
                } else {
                    showLoading();
                    mInteractSession.stopLianMai(new CCInteractSession.AtlasCallBack<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dismissLoading();
                        }

                        @Override
                        public void onFailure(String err) {
                            dismissLoading();
                            toastOnUiThread(err);
                        }
                    });

                }
            }
        });
    }

    private void initCancelMaiPopup() {
        mCancelMaiPopup = new CommonPopup(this);
        mCancelMaiPopup.setOutsideCancel(true);
        mCancelMaiPopup.setKeyBackCancel(true);
        mCancelMaiPopup.setOKClickListener(new CommonPopup.OnOKClickListener() {
            @Override
            public void onClick() {
                cancelQueueMai();
            }
        });
    }

    /**
     * 退出
     */
    private void exit() {
        if (mMaiStatus == 3) {
            showLoading();
            mInteractSession.unpublish(new CCInteractSession.AtlasCallBack() {
                @Override
                public void onSuccess(Object o) {
                    dismissLoading();
                    finishGoHome();
                }

                @Override
                public void onFailure(String err) {
                    dismissLoading();
                    toastOnUiThread(err);
                }
            });
        } else {
            finishGoHome();
        }
    }

    private void finishGoHome() {
        isExit = true;
        cancelUserCount();
        CCApplication.mAreaCode = mInteractSession.isAreaCode();
        mInteractSession.closeLocalCameraStream();
        mInteractSession.leaveRoom(null);
        if (mRoomDes == null || TextUtils.isEmpty(mRoomId) || TextUtils.isEmpty(mUserId)) {
            go(HomeActivity.class);
            finish();
            return;
        }
//        ValidateActivity.startSelf(this, mRoomDes.getName(), mRoomDes.getDesc(), mRoomId, mUserId, CCInteractSession.TALKER, mRoomDes.getAuthtype() == 2);
        finish();
    }

    private void onSoftInputChange() {
        mSoftKeyBoardUtil = new SoftKeyboardUtil(this);
        mSoftKeyBoardUtil.observeSoftKeyboard(this, new SoftKeyboardUtil.OnSoftKeyboardChangeListener() {
            @Override
            public void onSoftKeyBoardChange(int softKeybardHeight, boolean isShow, int changeHeight) {
                if (!isClickChat) { // 防止强制横屏引起的问题
                    return;
                }
                if (isShow) {
//                    if (changeHeight == 0) {
                        mChatLayout.setVisibility(View.VISIBLE);
//                    } else {
//                        if (changeHeight < 0) {
//                            changeHeight = (int) -(mChatInput.getPaint().descent() + DensityUtil.dp2px(StudentActivity.this, 7.5f));
//                        }
//                        mChatLayout.animate().translationYBy(changeHeight).setDuration(100).start();
//                    }
                } else {
                    mChatList.setVisibility(View.VISIBLE);
                    mClickDismissChatLayout.setVisibility(View.GONE);
                    mChatLayout.setVisibility(View.GONE);
                }
            }
        });
    }

   @OnClick(R2.id.id_student_share_screen_exit)
    void exitShareScreen() {
        showRemoteVideoByAnim();
        isShareScreenFullScreen = false;
        mShareScreenExit.setVisibility(View.GONE);
        RelativeLayout.LayoutParams temp = new RelativeLayout.LayoutParams(DensityUtil.dp2px(StudentActivity.this, 160), DensityUtil.dp2px(StudentActivity.this, 90));
        temp.leftMargin = mShareScreenLeft;
        temp.topMargin = mShareScreenTop;
        mShareScreenContainer.setLayoutParams(temp);
        FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(DensityUtil.dp2px(StudentActivity.this, 160), DensityUtil.dp2px(StudentActivity.this, 90));
        videoParams.gravity = Gravity.CENTER;
        mShareScreen.setLayoutParams(videoParams);
    }

   @OnClick(R2.id.id_student_remote_video_exit)
    void exitRemoteVideo() {
        isRemoteVideoFullScreen = false;
        mRemoteVideoExit.setVisibility(View.GONE);
        RelativeLayout.LayoutParams temp = new RelativeLayout.LayoutParams(DensityUtil.dp2px(StudentActivity.this, 160), DensityUtil.dp2px(StudentActivity.this, 90));
        temp.leftMargin = mRemoteVideoLeft;
        temp.topMargin = mRemoteVideoTop;
        mRemoteVideoContainer.setLayoutParams(temp);
        FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(DensityUtil.dp2px(StudentActivity.this, 160), DensityUtil.dp2px(StudentActivity.this, 90));
        videoParams.gravity = Gravity.CENTER;
        mRemoteVideo.setLayoutParams(videoParams);
        if (hasShareScreen) {
            mShareScreenContainer.setVisibility(View.VISIBLE);
            mShareScreen.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R2.id.id_student_class_user_list)
    void classUserList() {
        ListActivity.startSelf(this, CCInteractSession.TALKER, mCount);
    }

   @OnClick(R2.id.id_student_close)
    void close() {
        mExitPopup.show(mRoot);
    }

   @OnClick(R2.id.id_student_video_controller)
    void showOrDismissVideos() {
        mCurFragment.getRecyclerView().setVisibility(isVideoShow ? View.GONE : View.VISIBLE);
        isVideoShow = !isVideoShow;
        mVideoController.setBackgroundResource(isVideoShow ? R.drawable.draw_hide : R.drawable.draw_hide_on);
    }

   @OnClick(R2.id.id_student_draw_paint)
    void showDrawStyle() {
        if ((mDrawLayout.getY() + mDrawLayout.getHeight() + 20 + mPopupView.getHeight()) >
                DensityUtil.getHeight(this)) {
            mPopupWindow.showArrowTo(mDrawPaint, BubbleStyle.ArrowDirection.Down, 20);
        } else {
            mPopupWindow.showArrowTo(mDrawPaint, BubbleStyle.ArrowDirection.Up, 20);
        }
    }

   @OnClick(R2.id.id_student_draw_undo)
    void undo() {
        // 设置颜色
        if (mCurFragment instanceof LectureFragment) {
            ((LectureFragment) mCurFragment).undo();
        }
    }

   @OnClick(R2.id.id_student_draw_clear)
    void drawClear() {
        if (mCurFragment instanceof LectureFragment) {
            ((LectureFragment) mCurFragment).clear();
        }
    }

   @OnClick(R2.id.id_student_doc_back)
    void docBack() {
        if (mCurFragment instanceof LectureFragment) {
            ((LectureFragment) mCurFragment).docBack();
        }
    }

   @OnClick(R2.id.id_student_doc_forward)
    void docForward() {
        if (mCurFragment instanceof LectureFragment) {
            ((LectureFragment) mCurFragment).docForward();
        }
    }

   @OnClick(R2.id.id_student_draw_tbc)
    void showActionBar() {
        if (isTopDismiss) {
            toggleTopAndBottom();
        }
    }

    @Override
    public void onBackPressed() {
        if (isShareScreenFullScreen) {
            exitShareScreen();
            return;
        }
        if (isRemoteVideoFullScreen) {
            exitRemoteVideo();
            return;
        }
        if (mChatImageLayout.getVisibility() == View.VISIBLE) {
            mChatImageLayout.setVisibility(View.GONE);
            return;
        }
        if (mCurFragment instanceof LectureFragment && ((LectureFragment) mCurFragment).dealWithFullscreen()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return;
        }
        mExitPopup.show(mRoot);
    }

   @OnClick(R2.id.id_student_chat_img_fullscreen_layout)
    void dismissChatImage() {
        mChatImageLayout.setVisibility(View.GONE);
    }

   @OnClick(R2.id.id_student_click_dismiss_chat)
    void clickDismissChatLayout() {
        mSoftKeyBoardUtil.hideKeyboard(mChatInput);
    }

   @OnClick(R2.id.id_student_chat)
    void chat() {
        if (mInteractSession.isRoomGag() || mInteractSession.isGag()) {
            showToast("禁言中");
            return;
        }
        isClickChat = true;
        mChatList.setVisibility(View.GONE);
        mClickDismissChatLayout.setVisibility(View.VISIBLE);
        mSoftKeyBoardUtil.showKeyboard(mChatInput);
    }

   @OnClick(R2.id.id_student_chat_send)
    void chatSend() {
        String content = mChatInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            toastOnUiThread("禁止发送空消息");
            return;
        }
        content = transformMsg(content);
        mInteractSession.sendMsg(content);
        mChatInput.setText("");
        mSoftKeyBoardUtil.hideKeyboard(mChatInput);
    }

    // 来自网络正则匹配url 如果不符合需求，请自行修改
//    Pattern pattern = Pattern.compile("(([hH][tT]{2}[pP]|[hH][tT]{2}[pP][sS])://|(www))(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?");

    Pattern pattern = Pattern.compile("(([hH][tT]{2}[pP]|[hH][tT]{2}[pP][sS]|[fF][tT][pP])://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?");


    //            Matcher matcher = Patterns.WEB_URL.matcher(data); // 系统

    private String transformMsg(String content) {
        // fixme 兼容我们直播客户端 如果没有需求可以直接不调用这个方法
        String datas[] = content.split(" ");
        StringBuilder sb = new StringBuilder();
        boolean flag = false;
        for (String data :
                datas) {
            if (!flag) {
                flag = true;
            } else {
                sb.append(" ");
            }
            Matcher matcher = pattern.matcher(data);
            int preEnd = 0;
            int start, end;
            while (matcher.find()) {
                start = matcher.start();
                end = matcher.end();
                if (preEnd != start) {
                    sb.append(" ");
                    sb.append(data.substring(preEnd, start));
                }
                if (preEnd == 0 && start == 0) {
                    sb.append("[uri_");
                } else {
                    sb.append(" [uri_");
                }
                sb.append(data.substring(start, end));
                sb.append("]");
                preEnd = end;
            }
            if (preEnd != data.length()) {
                if (preEnd != 0) {
                    sb.append(" ");
                }
                sb.append(data.substring(preEnd, data.length()));
            }
        }
        return sb.toString();
    }

    /**
     * 开启系统相册
     */
    private void openSystemAlbum() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_SYSTEM_PICTURE);
    }

    @OnClick(R2.id.id_student_chat_open_img)
    void openImg() {
        /*
         * 这个地方可以自己实现
         */
        openSystemAlbum();
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO})
    void doRequestMai() {
        showLoading();
        mInteractSession.requestLianMai(new CCInteractSession.AtlasCallBack<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                dismissLoading();
                mQueueIndex = -1;
                updateMaiButton(MAI_STATUS_QUEUE);
            }

            @Override
            public void onFailure(String err) {
                dismissLoading();
                toastOnUiThread(err);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        StudentActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale({Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO})
    void showRationale(PermissionRequest request) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(request);
    }

    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO})
    void onNeverAskAgain() {
        Toast.makeText(this, "相机或录音权限被拒绝，并且不会再次询问", Toast.LENGTH_SHORT).show();
    }

    private void showRationaleDialog(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton("允许", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("禁止", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage("当前应用需要开启相机和录音进行推流")
                .show();
    }

   @OnClick(R2.id.id_student_handup)
    void autoHandup() {
        mInteractSession.handup(!isAutoHandup, new CCInteractSession.AtlasCallBack<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                isAutoHandup = !isAutoHandup;
                mHandup.setBackgroundResource(isAutoHandup ? R.drawable.handup_cancel_selector : R.drawable.handup_selector);
            }

            @Override
            public void onFailure(String err) {
                showToast(err);
            }
        });
    }

   @OnClick(R2.id.id_student_lianmaistyle)
    void requestMai() {
        if (mMaiStatus == MAI_STATUS_ING) {
            if (mInteractSession.isAllowVideo()) {
                mCancelPopup.update(1, "关闭摄像头");
            } else {
                mCancelPopup.update(1, "开启摄像头");
            }
            if (mInteractSession.isAllowAudio()) {
                mCancelPopup.update(2, "关闭麦克风");
            } else {
                mCancelPopup.update(2, "开启麦克风");
            }
            if (mInteractSession.getLianmaiMode() == LIANMAI_MODE_AUTO) {
                if (haveDownMai) {
                    mCancelPopup.removeChoose(3);
                    haveDownMai = false;
                }
            } else {
                //针对对啊客户需求，去掉下麦按钮
                if(mInteractSession.getshowExit() == 1){
                    if (!haveDownMai) {
                        mCancelPopup.add(3, "下麦");
                        haveDownMai = true;
                    }
                }
            }
            mCancelPopup.show(mRoot);
        } else if (mMaiStatus == MAI_STATUS_NORMAL) {
            StudentActivityPermissionsDispatcher.doRequestMaiWithCheck(this);
        } else {
            if (mInteractSession.getLianmaiMode() == CCInteractSession.LIANMAI_MODE_NAMED) {
                mCancelMaiPopup.setTip("取消举手");
            } else {
                mCancelMaiPopup.setTip("取消排麦");
            }
            mCancelMaiPopup.show(mRoot);
        }
    }

    @Override
    public void toggleTopLayout(boolean isOnlyDoc) {
        if (mTopDistance == -1) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTopLayout.getLayoutParams();
            mTopDistance = params.topMargin + mTopLayout.getHeight();
        }
        if (isOnlyDoc) {
            animateTop(mTopDistance);
        } else {
            animateTop(-mTopDistance);
        }
    }

    @Override
    public void docFullScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mChatList.setVisibility(View.GONE);
        mTopLayout.setVisibility(View.GONE);
        mBottomLayout.setVisibility(View.GONE);
        if (isAuthDraw) { // 授权绘制
            mDrawTBC.setVisibility(View.GONE);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        dismissRemoteVideoByAnim(); // 隐藏插播多媒体的界面
        if (hasShareScreen) { // 隐藏共享桌面流的界面
            mShareScreenContainer.setVisibility(View.GONE);
            mShareScreen.setVisibility(View.GONE);
        }
    }

    @Override
    public void exitDocFullScreen() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mChatList.setVisibility(View.VISIBLE);
        mTopLayout.setVisibility(View.VISIBLE);
        mBottomLayout.setVisibility(View.VISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        showRemoteVideoByAnim(); // 显示插播多媒体的界面
        if (hasShareScreen) { // 显示共享桌面流的界面
            mShareScreenContainer.setVisibility(View.VISIBLE);
            mShareScreen.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 通过动画把视图移除屏幕实现视图的可见性
     */

    private void showRemoteVideoByAnim() {
        if (mRemoteVideoContainer.getVisibility() == View.VISIBLE) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mRemoteVideoContainer, "translationX", 0);
            animator.setDuration(50);
            animator.start();
        }
    }

    private void dismissRemoteVideoByAnim() {
        if (mRemoteVideoContainer.getVisibility() == View.VISIBLE) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mRemoteVideoContainer.getLayoutParams();
            ObjectAnimator animator = ObjectAnimator.ofFloat(mRemoteVideoContainer, "translationX", -(params.leftMargin + mRemoteVideoContainer.getWidth()));
            animator.setDuration(50);
            animator.start();
        }
    }

    @Override
    public void videoFullScreen() {
        // 隐藏共享流界面
        if (hasShareScreen) {
            mShareScreen.setVisibility(View.GONE);
            mShareScreenContainer.setVisibility(View.GONE);
        }
        dismissRemoteVideoByAnim();
        mChatList.setVisibility(View.GONE);
        mTopLayout.setVisibility(View.GONE);
        mBottomLayout.setVisibility(View.GONE);
        if (isAuthDraw) {
            mDrawLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void exitVideoFullScreen() {
        showRemoteVideoByAnim();
        if (mCurFragment instanceof LectureFragment && CCApplication.sClassDirection == 1) {
            mChatList.setVisibility(View.GONE);
        } else {
            mChatList.setVisibility(View.VISIBLE);
        }
        if (isAuthDraw && mInteractSession.isRoomLive()
                && (CCApplication.sClassDirection == 1 || ((LectureFragment) mCurFragment).isDocFullScreen())) {
            mDrawLayout.setVisibility(View.VISIBLE);
        } else {
            mDrawLayout.setVisibility(View.GONE);
        }
        mTopLayout.setVisibility(View.VISIBLE);
        mBottomLayout.setVisibility(View.VISIBLE);
        if (hasShareScreen) {
            mShareScreenContainer.setVisibility(View.VISIBLE);
            mShareScreen.setVisibility(View.VISIBLE);
        }
    }

    private synchronized void publish() {
        try {
            mInteractSession.setCameraType(LocalCameraStreamParameters.CameraType.FRONT);
            mInteractSession.initCameraStream(mInteractSession.getMediaMode());
            mInteractSession.attachLocalCameraStram(mSelfRenderer);
            SubscribeRemoteStream selfStream = new SubscribeRemoteStream();
            selfStream.setUserName(mInteractSession.getUserName());
            selfStream.setUserId(mInteractSession.getUserIdInPusher());
            selfStream.setUserRole(CCInteractSession.TALKER);
            mSelfStreamView.setStream(selfStream);
            mSelfStreamView.getStream().setAllowAudio(mInteractSession.isAllowAudio());
            mSelfStreamView.getStream().setAllowVideo(mInteractSession.isAllowVideo());
            mSelfStreamView.getStream().setAllowDraw(mInteractSession.isAllowDraw());
            mSelfStreamView.getStream().setSetupTeacher(mInteractSession.isSetupTeacher());
            mSelfStreamView.getStream().setLock(mInteractSession.isLock());
            mVideoStreamViews.add(mSelfStreamView);
            mCurFragment.notifyItemChanged(mSelfStreamView, mVideoStreamViews.size() - 1, true);
            mInteractSession.publish(new CCInteractSession.AtlasCallBack<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    updateMaiButton(MAI_STATUS_ING);
                }

                @Override
                public void onFailure(final String err) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mInteractSession.getLianmaiMode() == LIANMAI_MODE_AUTO) {
                                mLianmaiStyle.setVisibility(View.GONE);
                            }
                            mVideoStreamViews.remove(mSelfStreamView);
                            mCurFragment.notifySelfRemove(mSelfStreamView);
                            showToast(err);
                            mInteractSession.closeLocalCameraStream();
                        }
                    });
                }
            });
        } catch (StreamException e) {
            showToast(e.getMessage());
//            mVideoStreamViews.remove(mSelfStreamView);
//            mCurFragment.notifySelfRemove(mSelfStreamView);
        }
    }

    private synchronized void unpublish() {
        mInteractSession.unpublish(new CCInteractSession.AtlasCallBack<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                updateList4Unpublish();
            }

            @Override
            public void onFailure(final String err) {
                toastOnUiThread(err);
                updateList4Unpublish();
            }
        });
    }

    private void updateList4Unpublish() {
        mInteractSession.closeLocalCameraStream();
        updateMaiButton(StudentActivity.MAI_STATUS_NORMAL);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSelfRenderer.cleanFrame();
                    mInteractSession.detachLocalCameraStram(mSelfRenderer);
                } catch (Exception ignored) {
                } finally {
                    if (!isExit) {
                        int position = mVideoStreamViews.indexOf(mSelfStreamView);
                        mVideoStreamViews.remove(mSelfStreamView);
                        mCurFragment.notifyItemChanged(mSelfStreamView, position, false);
                        if (mCurFragment instanceof LectureFragment) {
                            ((LectureFragment) mCurFragment).exitVideoFullScreenIfShow(mSelfStreamView.getStream());
                        }
                        if(position > 0){
                            if (mCurFragment instanceof LectureFragment) {
                                ((LectureFragment) mCurFragment).videoFlah(position);
                            }
                        }
                    }
                }
            }
        });
    }

    private void sortUser(ArrayList<User> users) {
        ArrayList<User> compareUsers = new ArrayList<>();
        for (User user :
                users) {
            if (user.getLianmaiStatus() == CCInteractSession.LIANMAI_STATUS_IN_MAI ||
                    user.getLianmaiStatus() == CCInteractSession.LIANMAI_STATUS_UP_MAI) {
                compareUsers.add(user);
            }
        }
        Collections.sort(compareUsers, new UserComparator());
        mQueueIndex = 1;
        for (User user :
                compareUsers) {
            if (user.getUserId().equals(mInteractSession.getUserIdInPusher())) {
                updateMaiButton(MAI_STATUS_QUEUE);
                break;
            }
            mQueueIndex++;
        }
    }

    private void cancelQueueMai() {
        showLoading();
        mInteractSession.cancleLianMai(new CCInteractSession.AtlasCallBack<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                dismissLoading();
                updateMaiButton(MAI_STATUS_NORMAL);
            }

            @Override
            public void onFailure(String err) {
                dismissLoading();
                toastOnUiThread(err);
            }
        });
    }

    private void updateMaiButton(final int status) {
        if (isExit) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLianmaiStyle.setVisibility(View.VISIBLE);
                mLianmaiStyle.setText("");
                mMaiStatus = status;
                switch (status) {
                    case MAI_STATUS_ING:
                        showToast("连麦成功");
                        mLianmaiStyle.setBackgroundResource(R.drawable.maiing_selector);
                        break;
                    case MAI_STATUS_NORMAL:
                        if (mInteractSession.getLianmaiMode() == LIANMAI_MODE_FREE) {
                            mLianmaiStyle.setBackgroundResource(R.drawable.queue_mai_selector);
                        } else if (mInteractSession.getLianmaiMode() == LIANMAI_MODE_AUTO) {
                            mLianmaiStyle.setVisibility(View.GONE);
                        } else {
                            mLianmaiStyle.setBackgroundResource(R.drawable.handup_selector);
                        }
                        break;
                    case MAI_STATUS_QUEUE:
                        if (mInteractSession.getLianmaiMode() == LIANMAI_MODE_FREE) {
                            mLianmaiStyle.setTextColor(Color.WHITE);
                            if (mQueueIndex == -1) {
                                mLianmaiStyle.setText("    排麦中");
                            } else {
                                SpannableString maiStr = new SpannableString("    排麦中\n    第" + mQueueIndex + "位");
                                mLianmaiStyle.setText(maiStr);
                            }
                            mLianmaiStyle.setBackgroundResource(R.drawable.queuing_selector);
                        } else {
                            mLianmaiStyle.setBackgroundResource(R.drawable.handup_cancel_selector);
                        }
                        break;
                }
            }
        });
    }

    @Override
    public SubscribeRemoteStream getStream(int position) {
        return mVideoStreamViews.get(position).getStream();
    }

    @Override
    public void toggleTopAndBottom() {
        if (mTopDistance == -1) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTopLayout.getLayoutParams();
            mTopDistance = params.topMargin + mTopLayout.getHeight();
        }
        if (mBottomDistance == -1) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBottomLayout.getLayoutParams();
            mBottomDistance = params.bottomMargin + mBottomLayout.getHeight();
        }
        if (isBottomDismiss) {
            animateTopAndBottom(mTopDistance, -mBottomDistance, true);
        } else {
            cancelDismissTopAndBottom();
            animateTopAndBottom(-mTopDistance, mBottomDistance, false);
        }
    }
}

package com.bokecc.ccsskt.example.activity;

import android.Manifest;
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
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
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
import com.bokecc.ccsskt.example.bridge.OnTeacherInteractionListener;
import com.bokecc.ccsskt.example.bridge.OnTeacherLectureListener;
import com.bokecc.ccsskt.example.bridge.OnVideoClickListener;
import com.bokecc.ccsskt.example.bridge.OnVideoInteractionListener;
import com.bokecc.ccsskt.example.entity.ChatEntity;
import com.bokecc.ccsskt.example.entity.ColorStatus;
import com.bokecc.ccsskt.example.entity.MoreItem;
import com.bokecc.ccsskt.example.entity.MyEBEvent;
import com.bokecc.ccsskt.example.entity.VideoStreamView;
import com.bokecc.ccsskt.example.fragment.BaseFragment;
import com.bokecc.ccsskt.example.fragment.LectureFragment;
import com.bokecc.ccsskt.example.fragment.MainVideoFragment;
import com.bokecc.ccsskt.example.fragment.TilingFragment;
import com.bokecc.ccsskt.example.global.Config;
import com.bokecc.ccsskt.example.interact.MyBroadcastReceiver;
import com.bokecc.ccsskt.example.popup.BottomCancelPopup;
import com.bokecc.ccsskt.example.popup.BottomIconPopup;
import com.bokecc.ccsskt.example.popup.CommonPopup;
import com.bokecc.ccsskt.example.popup.MorePopup;
import com.bokecc.ccsskt.example.recycle.BaseOnItemTouch;
import com.bokecc.ccsskt.example.recycle.DividerGridItemDecoration;
import com.bokecc.ccsskt.example.recycle.OnClickListener;
import com.bokecc.ccsskt.example.util.AndroidBug5497Workaround;
import com.bokecc.ccsskt.example.util.DensityUtil;
import com.bokecc.ccsskt.example.util.SoftKeyboardUtil;
import com.bokecc.ccsskt.example.view.DocView;
import com.bokecc.sskt.CCInteractSession;
import com.bokecc.sskt.SubscribeRemoteStream;
import com.bokecc.sskt.base.ConnectionStatsWrapper;
import com.bokecc.sskt.base.exception.StreamException;
import com.bokecc.sskt.base.renderer.CCSurfaceRenderer;
import com.bokecc.sskt.bean.PicToken;
import com.bokecc.sskt.bean.User;
import com.bokecc.sskt.doc.DocInfo;
import com.bokecc.sskt.net.OKHttpStatusListener;
import com.bokecc.sskt.net.OKHttpUtil;
import com.bokecc.sskt.net.ProgressListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cpiz.android.bubbleview.BubblePopupWindow;
import com.cpiz.android.bubbleview.BubbleRelativeLayout;
import com.cpiz.android.bubbleview.BubbleStyle;
import com.eightbitlab.supportrenderscriptblur.SupportRenderScriptBlur;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import eightbitlab.com.blurview.BlurView;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static com.bokecc.ccsskt.example.CCApplication.mNamedTimeEnd;
import static com.bokecc.ccsskt.example.R.id.id_teacher_class_user_list;
import static com.bokecc.ccsskt.example.global.Config.KEY_DOC_POSITION;
import static com.bokecc.ccsskt.example.global.Config.mRoomDes;
import static com.bokecc.ccsskt.example.global.Config.mRoomId;
import static com.bokecc.ccsskt.example.global.Config.mUserId;

@RuntimePermissions
public class TeacherActivity extends BaseActivity implements OnTeacherInteractionListener,
        OnDocInteractionListener, OnVideoInteractionListener, OnDisplayInteractionListener, OnVideoClickListener, View.OnClickListener, OnTeacherLectureListener {

    private static final String TAG = TeacherActivity.class.getSimpleName();

    private ArrayList<BaseFragment> mFragments;
    private SparseIntArray mTemplatePosition = new SparseIntArray();
    private BaseFragment mCurFragment;

    @BindView(R2.id.id_teacher_top_layout)
    RelativeLayout mTopLayout;
    @BindView(R2.id.id_teacher_bottom_layout)
    LinearLayout mBottomLayout;
    @BindView(R2.id.id_teacher_class_user_list)
    View mClassMsg;
    TextView mClassName;
    TextView mClassUserNum;
    ImageView mClassHandIcon;
    ImageView mClassArrowIcon;
    @BindView(R2.id.id_teacher_follow)
    ImageButton mFollow;
    @BindView(R2.id.id_teacher_camera)
    ImageButton mCamera;
    @BindView(R2.id.id_teacher_mic)
    ImageButton mMic;
    @BindView(R2.id.id_teacher_click_dismiss_chat)
    FrameLayout mClickDismissChatLayout;
    @BindView(R2.id.id_teacher_chat_layout)
    RelativeLayout mChatLayout;
    @BindView(R2.id.id_teacher_chat_list)
    RecyclerView mChatList;
    @BindView(R2.id.id_teacher_chat_input)
    EditText mChatInput;
    @BindView(R2.id.id_teacher_start_layout)
    RelativeLayout mStartLayout;
    @BindView(R2.id.id_teacher_stop_layout)
    LinearLayout mStopLayout;
    @BindView(R2.id.id_teacher_chat_img_fullscreen_layout)
    RelativeLayout mChatImageLayout;
    @BindView(R2.id.id_teacher_chat_img)
    ImageView mChatImage;
    @BindView(R2.id.id_teacher_blur_view)
    BlurView mBlurView;
    @BindView(R2.id.id_teacher_blur_root)
    RelativeLayout mBlurRoot;
    @BindView(R2.id.id_teacher_blur_doc)
    ImageButton mBlurDoc;
    @BindView(R2.id.id_teacher_blur_update_img)
    ImageButton mBlurUpdateImg;
    @BindView(R2.id.id_teacher_blur_named)
    ImageButton mBlurNamed;
    @BindView(R2.id.id_teacher_blur_layout)
    ImageButton mBlurLayout;
    @BindView(R2.id.id_teacher_blur_setting)
    ImageButton mBlurSetting;
    @BindView(R2.id.id_teacher_more)
    ImageButton mMoreBtn;
    @BindView(R2.id.id_teacher_drag_child)
    LinearLayout mDrawLayout;
    @BindView(R2.id.id_teacher_page_change_layout)
    LinearLayout mPageChangeLayout;
    @BindView(R2.id.id_teacher_doc_index)
    TextView mDocIndex;
    @BindView(R2.id.id_teacher_draw_paint)
    ImageButton mDrawPaint;
    @BindView(R2.id.id_teacher_doc_back)
    ImageButton mDocBack;
    @BindView(R2.id.id_teacher_doc_forward)
    ImageButton mDocForward;
    @BindView(R2.id.id_teacher_video_controller)
    ImageButton mVideoController;
    ImageButton[] mBlurAnimViews;
    ImageButton[] mBlurNoDocAnimViews;
    @BindView(R2.id.id_teacher_draw_tbc)
    ImageButton mDrawTBC;

    private Animation mBlurOutAnim;
    private Animation mBlurInAnim;

    private static final int REQUEST_SYSTEM_PICTURE_CHAT = 0;
    private static final int REQUEST_SYSTEM_PICTURE_UPDATE = 1;

    private BottomCancelPopup mCameraPopup;
    private BottomCancelPopup mAuditorPopup;
    private BottomIconPopup mIconPopup;
    private CommonPopup mLivePopup, mExitPopup;
    private MorePopup mMorePopup;
    private boolean isMoreItemHasDoc = false;

    private BubblePopupWindow mPopupWindow; // 选择画笔颜色
    private View mPopupView;
    private ImageButton mSmallSize, mMidSize, mLargeSize;
    private RecyclerView mColors;
    private BubbleRelativeLayout mBubbleLayout;
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

    private boolean isHandUp = false; // 是否有人举手
    // 是否发布流
    private boolean isPublish = false;
    // 是否跟随
    private boolean isFollow = false;
    // 退出时候的操作
    private int mExitAction = -1;
    private static final int EXIT_ACTION_STOP = 0;
    private static final int EXIT_ACTION_CLOSE = 1;

    private ArrayList<ChatEntity> mChatEntities;
    private ChatAdapter mChatAdapter;
    private boolean isScroll = true;
    private boolean isStateIDLE = true;
    // 软键盘监听
    private SoftKeyboardUtil mSoftKeyBoardUtil;

    private boolean isTopDismiss = false, isBottomDismiss = false;
    private int mTopDistance = -1, mBottomDistance = -1;
    private boolean isCancelTask = false;
    private boolean isVideoShow = true;

    private VideoAdapter mVideoAdapter;
    private VideoStreamView mSelfStreamView;
    private CCSurfaceRenderer mSelfRenderer;
    protected CopyOnWriteArrayList<VideoStreamView> mVideoStreamViews = new CopyOnWriteArrayList<>();
    private Map<SubscribeRemoteStream, VideoStreamView> mStreamViewMap = new ConcurrentHashMap<>();

    private int mCount;
    private String mClickAuditorId; //点击旁听的id

    private AtomicInteger mHandupCount = new AtomicInteger(0);

    @Override
    protected int getLayoutId() {
        return R.layout.activity_teacher;
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
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    protected void onViewCreated() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (CCApplication.sClassDirection == 1) { // 横屏全屏软键盘bug 应该判断是FULLSCREEN
            AndroidBug5497Workaround.assistActivity(this);
        }

        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }

        onSoftInputChange();
        initExitPopup();
        initLiveActionPopup();
        initCameraPopup();
        initMorePopup();
        initAuditorPopup();
        initIconPopup();
        initDrawPopup();

        mMyBroadcastReceiver = MyBroadcastReceiver.getInstance();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver(mMyBroadcastReceiver, intentFilter);

        mBlurAnimViews = new ImageButton[]{
                mBlurDoc, mBlurUpdateImg, mBlurNamed, mBlurLayout, mBlurSetting
        };
        mBlurNoDocAnimViews = new ImageButton[]{
                mBlurNamed, mBlurLayout, mBlurSetting
        };
        initBlurAnim();

        mClassName = (TextView) mClassMsg.findViewById(R.id.id_top_class_name);
        mClassUserNum = (TextView) mClassMsg.findViewById(R.id.id_top_class_users);
        mClassHandIcon = (ImageView) mClassMsg.findViewById(R.id.id_top_class_handup_flag);
        mClassArrowIcon = (ImageView) mClassMsg.findViewById(R.id.id_top_class_arrow);
        mClassName.setText(mInteractSession.getRoom().getRoomName());

        mVideoAdapter = new VideoAdapter(this);
        mFragments = new ArrayList<>();
        mFragments.add(LectureFragment.newInstance(CCInteractSession.PRESENTER));
        mFragments.add(MainVideoFragment.newInstance(CCInteractSession.PRESENTER));
        mFragments.add(TilingFragment.newInstance(CCInteractSession.PRESENTER));
        mTemplatePosition.put(CCInteractSession.TEMPLATE_SPEAK, 0);
        mTemplatePosition.put(CCInteractSession.TEMPLATE_SINGLE, 1);
        mTemplatePosition.put(CCInteractSession.TEMPLATE_TILE, 2);
        for (BaseFragment fragment :
                mFragments) {
            fragment.setVideoAdapter(mVideoAdapter);
        }

        // 双师模式兼容为大屏模式
        int template = mInteractSession.getTemplate();
        if (template == CCInteractSession.TEMPLATE_DOUBLE_TEACHER) {
            template = CCInteractSession.TEMPLATE_SINGLE;
        }

        // 设置当前的教师布局模版
        setSelected(template);

        mSelfStreamView = new VideoStreamView();
        mSelfRenderer = new CCSurfaceRenderer(this);
        mSelfRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        mInteractSession.initSurfaceContext(mSelfRenderer);
        mSelfStreamView.setRenderer(mSelfRenderer);
        SubscribeRemoteStream selfStream = new SubscribeRemoteStream();
        selfStream.setUserName(mInteractSession.getUserName());
        selfStream.setUserId(mInteractSession.getUserIdInPusher());
        selfStream.setAllowAudio(mInteractSession.isAllowAudio());
        selfStream.setAllowVideo(mInteractSession.isAllowVideo());
        selfStream.setLock(mInteractSession.isLock());
        selfStream.setUserRole(CCInteractSession.PRESENTER);
        mSelfStreamView.setStream(selfStream);

        mChatList.setLayoutManager(new LinearLayoutManager(this));
        mChatAdapter = new ChatAdapter(this, CCInteractSession.PRESENTER);
        mChatEntities = new ArrayList<>();
        mChatAdapter.bindDatas(mChatEntities);
        mChatList.setAdapter(mChatAdapter);
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

//        startPreview();
        TeacherActivityPermissionsDispatcher.startPreviewWithCheck(this);

        if (mInteractSession.isRoomLive()) { // 首次开启直播页，如果当前直播间处于直播状态（上次直播异常退出导致），进行界面提示
            CopyOnWriteArrayList<SubscribeRemoteStream> streams = mInteractSession.getSubscribeRemoteStreams();
            for (SubscribeRemoteStream stream :
                    streams) {
                addStreamView(stream);
            }
            mHandler.postDelayed(new Runnable() { // 解决问题 ：popupwindow 不可以在onCreate的时候添加到窗口
                @Override
                public void run() {
                    mLivePopup.show(mRoot);
                }
            }, 100);
        }
        loopUserCount();

        doDrawLayoutTouch();
    }

    private void initBlurAnim() {
        mBlurInAnim = AnimationUtils.loadAnimation(this, R.anim.blur_in);
        mBlurOutAnim = AnimationUtils.loadAnimation(this, R.anim.blur_out);
    }

    private int lastX;
    private int lastY;

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
                            if (params.topMargin > (DensityUtil.getHeight(TeacherActivity.this) - mDrawLayout.getHeight())) {
                                params.topMargin = DensityUtil.getHeight(TeacherActivity.this) - mDrawLayout.getHeight();
                            }
                            if (params.leftMargin > (DensityUtil.getWidth(TeacherActivity.this) - mDrawLayout.getWidth())) {
                                params.leftMargin = DensityUtil.getWidth(TeacherActivity.this) - mDrawLayout.getWidth();
                            }
                        } else { // 竖屏 文档全屏
                            if (params.topMargin > (DensityUtil.getWidth(TeacherActivity.this) - mDrawLayout.getHeight())) {
                                params.topMargin = DensityUtil.getWidth(TeacherActivity.this) - mDrawLayout.getHeight();
                            }
                            if (params.leftMargin > (DensityUtil.getHeight(TeacherActivity.this) - mDrawLayout.getWidth())) {
                                params.leftMargin = DensityUtil.getHeight(TeacherActivity.this) - mDrawLayout.getWidth();
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

    @Override
    protected void onStart() {
        super.onStart();
        // TODO: 2017/10/27  取消动画
//        if (mInteractSession.getTemplate() != CCInteractSession.TEMPLATE_SPEAK) {
//            executeDismissTopAndBottom();
//        }
    }

    @Override
    protected void onResume() {
//        if (isPause && !isGo) {
//            mInteractSession.switchCamera(mSelfRenderer, new CCInteractSession.AtlasCallBack<Void>() {
//                @Override
//                public void onSuccess(Void aVoid) {
//                    mInteractSession.enableVideo(false);
//                }
//
//                @Override
//                public void onFailure(String err) {
//
//                }
//            });
//        }
        super.onResume();
        // 设置直播间名称
        mClassName.setText(mInteractSession.getRoom().getRoomName());
        mChatInput.setFocusableInTouchMode(false);
        mChatInput.setFocusable(false);
        mChatInput.clearFocus();
    }

    @Override
    protected void onPause() {
//        if (!isGo) {
//            Log.e(TAG, "onPause: ");
//            mInteractSession.disableVideo(false);
//            mInteractSession.switchCamera(mSelfRenderer, new CCInteractSession.AtlasCallBack<Void>() {
//                @Override
//                public void onSuccess(Void aVoid) {
//                }
//
//                @Override
//                public void onFailure(String err) {
//                }
//            });
//        }
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
        super.onDestroy();
        unregisterReceiver(mMyBroadcastReceiver);
        if (mSoftKeyBoardUtil != null) {
            mSoftKeyBoardUtil.removeGlobalOnLayoutListener(this);
            mSoftKeyBoardUtil = null;
        }
        DocView.drawingData.clear();

        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (resultCode == Config.DOC_LIST_RESULT_CODE) {
            DocInfo docInfo = (DocInfo) data.getSerializableExtra("selected_doc");
            ((LectureFragment) mCurFragment).setDocInfo(docInfo, 0, 0);
        } else if (resultCode == Config.LAYOUT_RESULT_CODE) {
            int template = data.getExtras().getInt("template", -1);
            if (template == -1) {
                showToast("布局选择出错");
                return;
            }
            if (template == CCInteractSession.TEMPLATE_SPEAK) {
                if ((CCApplication.sClassDirection == 1) && mInteractSession.isRoomLive()) {
                    mDrawLayout.setVisibility(View.VISIBLE);
                }
            } else {
                mDrawLayout.setVisibility(View.GONE);
            }
            setSelected(template);
        }
        if (requestCode == REQUEST_SYSTEM_PICTURE_CHAT ||
                requestCode == REQUEST_SYSTEM_PICTURE_UPDATE) {
            String imgPath = checkImg(data.getData());
            if (!TextUtils.isEmpty(imgPath)) {
                try {
                    compressBitmap(imgPath, requestCode, readPictureDegree(imgPath));
                } catch (IOException e) {
                    showToast("图片加载失败");
                }
            } else {
                showToast("图片加载失败");
            }
        }
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

    private String checkImg(Uri imageUri) {
        if (imageUri == null) {
            showToast("图片加载失败");
            return null;
        }
        String imgPath = getImageAbsolutePath(imageUri);
        if (imgPath == null) {
            showToast("图片加载失败");
            return null;
        }
        return imgPath;
    }

    private void clearSizesStatus() {
        mSmallSize.setSelected(false);
        mMidSize.setSelected(false);
        mLargeSize.setSelected(false);
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

    @Override
    public void onLecture(int cur, int total) {
        if (mPageChangeLayout.getVisibility() != View.VISIBLE) {
            mPageChangeLayout.setVisibility(View.VISIBLE);
        }
        mDocIndex.setText(cur + "/" + total);
        if (total == 1) {
            mDocBack.setEnabled(false);
            mDocForward.setEnabled(false);
            mPageChangeLayout.setVisibility(View.GONE);
        } else {
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
    }

    private class MyCompressListener implements CompressImage.CompressListener {

        private int type;
        private int degree;

        MyCompressListener(int type, int degree) {
            this.type = type;
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
                if (type == REQUEST_SYSTEM_PICTURE_CHAT) {
                    updatePic1(file);
                } else if (type == REQUEST_SYSTEM_PICTURE_UPDATE) {
                    updateImage2Doc(file);
                }
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

    /**
     * 进行图片压缩
     */
    ArrayList<TImage> images = new ArrayList<>();
    CompressConfig config = new CompressConfig.Builder()
            .enableQualityCompress(false)
            .setMaxSize(5 * 1024)
            .create();

    private void compressBitmap(String imgPath, int type, int degree) {
        images.clear();
        File file = new File(imgPath);
        TImage image = TImage.of(file.getAbsolutePath(), TImage.FromType.OTHER);
        images.add(image);
        CompressImage compressImage = CompressImageImpl.of(this, config, images, new MyCompressListener(type, degree));
        compressImage.compress();
    }

    final String DOC_IMG_PATH = "http://document.csslcloud.net/servlet/image/upload";

    private void updateImage2Doc(File file) {
        String url = DOC_IMG_PATH + "?roomid=" + mInteractSession.getRoom().getRoomId();
        OKHttpUtil.updateFileWithProgress(this, url, file, null, new OKHttpStatusListener() {
            @Override
            public void onFailed(int code, String errorMsg) {
                toastOnUiThread(errorMsg);
                Log.e(TAG, "onFailed: " + code + "-" + errorMsg);
                if (mCurFragment instanceof LectureFragment) {
                    ((LectureFragment) mCurFragment).dismissDocLoading(null, false);
                }
            }

            @Override
            public void onSuccessed(String result) {
                Log.e(TAG, result);
                try {
                    JSONObject object = new JSONObject(result);
                    String res = object.getString("result");
                    if (res.equals("OK")) {
                        String docId = object.getString("docId");
                        if (mCurFragment instanceof LectureFragment) {
                            ((LectureFragment) mCurFragment).dismissDocLoading(docId, true);
                        }
                    } else {
                        toastOnUiThread("上传文档失败");
                        if (mCurFragment instanceof LectureFragment) {
                            ((LectureFragment) mCurFragment).dismissDocLoading(null, false);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new ProgressListener() {
            @Override
            public void onProgressChanged(long numBytes, long totalBytes) {

            }

            @Override
            public void onProgressStart(long totalBytes) {
                if (mCurFragment instanceof LectureFragment) {
                    ((LectureFragment) mCurFragment).showDocLoading(totalBytes);
                }
            }

            @Override
            public void onProgressFinish() {
            }
        });
    }

    /**
     * 获取token
     */
    private void updatePic1(final File file) {
        mInteractSession.getPicUploadToken(new CCInteractSession.AtlasCallBack<PicToken>() {
            @Override
            public void onSuccess(PicToken picToken) {
                // 开始上传 demo 仅提供思路
                Log.e(TAG, "onSuccessed: token");
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
                Log.e(TAG, "onSuccessed: send pic url");
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

    private void showAuditor() {
        mAuditorPopup.clear();
        mAuditorPopup.add(0, mInteractSession.isAuditorGag(mClickAuditorId) ? "取消禁言" : "禁言");
        mAuditorPopup.add(1, "踢出房间");
        mAuditorPopup.show(mRoot);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInteractEvent(User user) {
        if (user.getUserRole() == CCInteractSession.AUDITOR) {
            mClickAuditorId = user.getUserId();
            showAuditor();
        } else {
            updateOrShowUserPopup(user);
        }
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO})
    public void startPreview() {
        try {
            mInteractSession.setCameraType(LocalCameraStreamParameters.CameraType.FRONT);
            mInteractSession.initCameraStream(mInteractSession.getMediaMode());
            mInteractSession.attachLocalCameraStram(mSelfRenderer);
            mVideoStreamViews.add(mSelfStreamView);
            mCurFragment.notifyItemChanged(mSelfStreamView, 0, true);
        } catch (StreamException e) {
            showToast(e.getMessage());
            mVideoStreamViews.remove(mSelfStreamView);
            mCurFragment.notifySelfRemove(mSelfStreamView);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        TeacherActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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

    @Override
    public SubscribeRemoteStream getStream(int position) {
        return mVideoStreamViews.get(position).getStream();
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
        isHandUp = flag;
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

    private void updateVideos(String userid, boolean flag, int changed) {
        CopyOnWriteArrayList<VideoStreamView> videoStreamViews;
        if (mCurFragment instanceof MainVideoFragment) {
            videoStreamViews = ((MainVideoFragment) mCurFragment).getDatas();
        } else {
            videoStreamViews = (CopyOnWriteArrayList<VideoStreamView>) mVideoAdapter.getDatas();
        }
        for (int i = 0; i < videoStreamViews.size(); i++) {
            VideoStreamView videoStreamView = videoStreamViews.get(i);
            if (videoStreamView.getStream().getUserId().equals(userid)) {
                if (changed == 0) {
                    videoStreamView.getStream().setAllowAudio(flag);
                } else if (changed == 1) {
                    videoStreamView.getStream().setAllowVideo(flag);
                } else if (changed == 2) {
                    videoStreamView.getStream().setAllowDraw(flag);
                } else if (changed == 3) {
                    videoStreamView.getStream().setLock(flag);
                } else if (changed == 4) {
                    videoStreamView.getStream().setSetupTeacher(flag);
                }
                if (mCurFragment instanceof TilingFragment) {
                    mCurFragment.notifyLayoutManagerRefresh();
                }
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
            case Config.CHAT_IMG:
                mChatImageLayout.setVisibility(View.VISIBLE);
                Glide.with(this).asBitmap().load(event.obj).
                        apply(new RequestOptions()
                                .override(DensityUtil.getWidth(this), DensityUtil.getHeight(this))
                                .fitCenter()).
//                        override(DensityUtil.getWidth(this), DensityUtil.getHeight(this)).
//                        fitCenter().
        into(mChatImage);
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
            case Config.INTERACT_EVENT_WHAT_HANDUP:
                boolean isHandup = (boolean) event.obj2;
                if (isHandup) {
                    mHandupCount.incrementAndGet();
                } else {
                    mHandupCount.decrementAndGet();
                }
                updateHandUpFlag(mHandupCount.get() > 0);
                break;
            case Config.INTERACT_EVENT_WHAT_UPDATE_MEDIA_MODE:
                if (mCurFragment instanceof TilingFragment) {
                    mCurFragment.notifyLayoutManagerRefresh();
                }
                mVideoAdapter.notifyDataSetChanged();
                break;
            case Config.INTERACT_EVENT_WHAT_QUEUE_MAI:
                ArrayList<User> users = (ArrayList<User>) event.obj; // 重新赋值
                if (mUserPopup.isShowing()) { // 如果弹出框显示，更新当前选中的用户状态
                    for (User user :
                            users) { // 刷新
                        if (mCurUser != null && user.getUserId().equals(mCurUser.getUserId())) {
                            updateOrShowUserPopup(user);
                        }
                    }
                }
                if (mInteractSession.getLianmaiMode() == CCInteractSession.LIANMAI_MODE_NAMED) { // 点名连麦模式
                    boolean flag = false;
                    for (User user :
                            users) {
                        if (user.getLianmaiStatus() == CCInteractSession.LIANMAI_STATUS_IN_MAI) {
                            flag = true;
                            break;
                        }
                        if (mUserPopup.isShowing()) { // 如果弹出框显示，更新当前选中的用户状态
                            if (mCurUser != null && user.getUserId().equals(mCurUser.getUserId())) {
                                updateOrShowUserPopup(user);
                            }
                        }
                    }
                    if (flag != isHandUp)
                        updateHandUpFlag(flag);
                    if (flag && isTopDismiss) {
                        mCurFragment.notifyHandUp();
                    }
                }
                break;
            case Config.INTERACT_EVENT_WHAT_STREAM_ADDED:
                addStreamView((SubscribeRemoteStream) event.obj);
                break;
            case Config.INTERACT_EVENT_WHAT_STREAM_REMOVED:
                removeStreamView((SubscribeRemoteStream) event.obj);
                break;
            case Config.INTERACT_EVENT_WHAT_STREAM_ERROR:
                finishGoHome();
                break;
            case Config.INTERACT_EVENT_WHAT_ANSWER_NAMED:
                if (CCApplication.mNamedTimeEnd != 0L) {
                    CCApplication.mNamedCount++;
                }
                break;
            case Config.INTERACT_EVENT_WHAT_USER_AUDIO:
                updateVideos((String) event.obj, (Boolean) event.obj2, 0);
                break;
            case Config.INTERACT_EVENT_WHAT_USER_VIDEO:
                updateVideos((String) event.obj, (Boolean) event.obj2, 1);
                break;
            case Config.INTERACT_EVENT_WHAT_AUTH_DRAW:
                updateVideos((String) event.obj, (Boolean) event.obj2, 2);
                break;
            case Config.INTERACT_EVENT_WHAT_LOCK:
                updateVideos((String) event.obj, (Boolean) event.obj2, 3);
                break;
            case Config.INTERACT_EVENT_WHAT_SETUP_THEACHER:
                updateVideos((String) event.obj, (Boolean) event.obj2, 4);
                break;
            case Config.INTERACT_EVENT_WHAT_TEACHER_DOWN:
                showToast("老师流异常remove");
                finishGoHome();
                break;
            case Config.INTERACT_EVENT_WHAT_DEVICE_FAIL:
                showToast(event.obj2 + " 连麦设备不可用，上麦失败");
                break;
            case Config.INTERACT_EVENT_WHAT_TEACHER_SETUPTHEACHER_FLAG:
                ((LectureFragment) mCurFragment).TeacherSetupTeacherPage((int) event.obj);
                break;
            case Config.INTERACT_EVENT_WHAT_KICK_OUT:
                isExit = true;
                isKick = true;
//                showToast("对不起，您已经被其他老师挤出该直播间");
                showToast("对不起，您已经被踢出该直播间");
                CCApplication.mAreaCode = mInteractSession.isAreaCode();
                ValidateActivity.startSelf(this, mRoomDes.getName(), mRoomDes.getDesc(), mRoomId, mUserId, CCInteractSession.PRESENTER, false);
                finish();
            case Config.INTERACT_EVENT_WHAT_SERVER_DISCONNECT:
                mNotifyPopup.show(mRoot);
                break;
            case Config.INTERACT_EVENT_WHAT_CLASS_STATUS_START:

                if (CCApplication.sClassDirection == 1 && mCurFragment instanceof LectureFragment) {
                    //  更新图标
                    mVideoController.setBackgroundResource(R.drawable.draw_hide);
                    mVideoController.setVisibility(View.VISIBLE);
                    isVideoShow = true;
                }
                if (mCurFragment instanceof LectureFragment
                        && (CCApplication.sClassDirection == 1 || ((LectureFragment) mCurFragment).isDocFullScreen())) {
                    mDrawLayout.setVisibility(View.VISIBLE);
                }
                break;
            case Config.INTERACT_EVENT_WHAT_CLASS_STATUS_STOP:

                mVideoController.setVisibility(View.GONE);
                mDrawLayout.setVisibility(View.GONE);
//                ((LectureFragment) mCurFragment).authDraw(false);//直播结束，停止画笔
                break;
//            case Config.INTERACT_EVENT_WHAT_NOTIFY_PUBLISH:
//                NotifyRouter((String)event.obj,(String)event.obj2);
//                break;
//            case Config.INTERACT_EVENT_WHAT_INTERRUPT_PUBLISH:
//                publish();
//                break;
        }
    }

//    private void NotifyRouter(String streamid, String userid) {
//        mInteractSession.rePublishEvent(streamid,userid,mSelfStreamView.getStream().getUserId());
//    }

    private synchronized void addStreamView(final SubscribeRemoteStream stream) {
        final CCSurfaceRenderer surfaceRenderer = new CCSurfaceRenderer(this);
        mInteractSession.initSurfaceContext(surfaceRenderer);
        surfaceRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        VideoStreamView videoStreamView = new VideoStreamView();
        videoStreamView.setRenderer(surfaceRenderer);
        videoStreamView.setStream(stream);
        mStreamViewMap.put(stream, videoStreamView);
        final Timer timer = new Timer();
        try {
            mInteractSession.subscribe(stream, new CCInteractSession.AtlasCallBack<SubscribeRemoteStream>() {
                @Override
                public void onSuccess(final SubscribeRemoteStream stream) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final VideoStreamView tempView = mStreamViewMap.get(stream);
                                if (tempView == null) {
                                    return;
                                }
                                tempView.getStream().attach(tempView.getRenderer());
                                mVideoStreamViews.add(tempView);
                                mCurFragment.notifyItemChanged(tempView, mVideoStreamViews.size() - 1, true);
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
        } catch (StreamException e) {
            showToast(e.getMessage());
        }

        timer.schedule(new TimerTask() {
            public void run() {
                mInteractSession.getConnectionStats(stream, new CCInteractSession.AtlasCallBack<ConnectionStatsWrapper>() {

                    @Override
                    public void onSuccess(ConnectionStatsWrapper connectionStats) {
                        if (connectionStats.bytesReceived == 0 || connectionStats.frameWidthReceived == 0
                                && connectionStats.frameHeightReceived == 0) {
                            try {
                                stream.detach();
                                mInteractSession.unsubscribe(stream, new CCInteractSession.AtlasCallBack<Void>() {

                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        timer.schedule(new TimerTask() {
                                            public void run() {
                                                try {
                                                    mInteractSession.subscribe(stream, new CCInteractSession.AtlasCallBack<SubscribeRemoteStream>() {
                                                        @Override
                                                        public void onSuccess(final SubscribeRemoteStream stream) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    try {
                                                                        final VideoStreamView tempView = mStreamViewMap.get(stream);
                                                                        if (tempView == null) {
                                                                            return;
                                                                        }
                                                                        tempView.getStream().attach(tempView.getRenderer());
                                                                        mVideoStreamViews.add(tempView);
                                                                        mCurFragment.notifyItemChanged(tempView, mVideoStreamViews.size() - 1, true);
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
                                                } catch (StreamException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }, 2000);
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
        try {
            if (mCurFragment instanceof LectureFragment) {
                ((LectureFragment) mCurFragment).exitVideoFullScreenIfShow(stream);
            }
            int position = -1;
            VideoStreamView tempView = null;
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
            mStreamViewMap.remove(stream);
            mVideoStreamViews.remove(tempView);
            mCurFragment.notifyItemChanged(tempView, position, false);
            stream.detach();
            mInteractSession.unsubscribe(stream, null);
        } catch (StreamException ignore) {

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

            mChatList.animate().cancel();
            mBottomLayout.animate().cancel();
            mChatList.animate().translationYBy(bottom).setDuration(100).start();
            mBottomLayout.animate().translationYBy(bottom).setDuration(100).withEndAction(new Runnable() {
                @Override
                public void run() {
                    if (!(mCurFragment instanceof LectureFragment)) {
                        mCurFragment.restoreClick();
                    }
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
            if (mBottomLayout == null || mTopLayout == null) {
                return;
            }
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

    private synchronized void setSelected(int template) {
        if (template == CCInteractSession.TEMPLATE_SPEAK) {
            if (!isMoreItemHasDoc) {
                mMorePopup.addItem(0, new MoreItem(0, R.drawable.doc_icon, "提取文档"));
                isMoreItemHasDoc = true;
            }
        } else {
            if (isMoreItemHasDoc) {
                mMorePopup.removeItem(0);
                isMoreItemHasDoc = false;
            }
        }
        mVideoAdapter.setType(template);
        if (mCurFragment != null) {
            mCurFragment.clearDatas();
            cancelDismissTopAndBottom();
        }
        mCurFragment = mFragments.get(mTemplatePosition.get(template));
        mCurFragment.addDatas(mVideoStreamViews);
        getSupportFragmentManager().beginTransaction().
                replace(R.id.id_teacher_content, mCurFragment)
                .commitAllowingStateLoss();
        if (mCurFragment instanceof LectureFragment) {
            if (CCApplication.sClassDirection == 1) {
//                mDrawLayout.setVisibility(View.GONE);
            } else {
//                ((LectureFragment) mCurFragment).authDraw(true);
                mDrawLayout.setVisibility(View.GONE);
            }
        }
        if (isBottomDismiss) {
            animateTopAndBottom(mTopDistance, -mBottomDistance, false);
        } else {
            if (isTopDismiss) {
                animateTop(mTopDistance);
            }
        }
    }

    private void onSoftInputChange() {
        mSoftKeyBoardUtil = new SoftKeyboardUtil(this);
        mSoftKeyBoardUtil.observeSoftKeyboard(this, new SoftKeyboardUtil.OnSoftKeyboardChangeListener() {
            @Override
            public void onSoftKeyBoardChange(int softKeybardHeight, boolean isShow, int changeHeight) {
                if (isShow) {
//                    if (changeHeight == 0) {
                    mChatLayout.setVisibility(View.VISIBLE);
//                    } else {
//                        if (changeHeight < 0) {
//                            changeHeight = (int) -(mChatInput.getPaint().descent() + DensityUtil.dp2px(TeacherActivity.this, 7.5f));
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

    private void initExitPopup() {
        mExitPopup = new CommonPopup(this);
        mExitPopup.setOutsideCancel(true);
        mExitPopup.setKeyBackCancel(true);
        mExitPopup.setOKClickListener(new CommonPopup.OnOKClickListener() {
            @Override
            public void onClick() {
                switch (mExitAction) {
                    case EXIT_ACTION_CLOSE:
                        exit();
                        break;
                    case EXIT_ACTION_STOP:
                        unpublish(false);
                        break;
                }
            }
        });
    }

    private void exit() {
        if (isPublish) {
            showLoading();
            unpublish(true);
        } else {
            finishGoHome();
        }
    }

    private void finishGoHome() {
        isExit = true;
        cancelUserCount();
        CCApplication.mAreaCode = mInteractSession.isAreaCode();
        mInteractSession.closeLocalCameraStream();
        mSelfRenderer.cleanFrame();
        mInteractSession.leaveRoom(null);
        mSPUtil.put(Config.KEY_DOC_ID + Config.mRoomId, "WhiteBorad"); // 文档恢复白板
        mSPUtil.put(KEY_DOC_POSITION, -1);
        if (mRoomDes == null || TextUtils.isEmpty(mRoomId) || TextUtils.isEmpty(mUserId)) {
            go(HomeActivity.class);
            finish();
            return;
        }
        ValidateActivity.startSelf(this, mRoomDes.getName(), mRoomDes.getDesc(), mRoomId, mUserId, CCInteractSession.PRESENTER, false);
        finish();
    }

    private void initLiveActionPopup() {
        mLivePopup = new CommonPopup(this);
        mLivePopup.setTip("是否继续上场直播");
        mLivePopup.setOKValue("继续");
        mLivePopup.setCancelValue("终止");
        mLivePopup.setOutsideCancel(false);
        mLivePopup.setKeyBackCancel(false);
        mLivePopup.setOKClickListener(new CommonPopup.OnOKClickListener() {
            @Override
            public void onClick() {
                continueLastLive();
            }
        });
        mLivePopup.setCancelClickListener(new CommonPopup.OnCancelClickListener() {
            @Override
            public void onClick() {
                stopLastLive();
            }
        });
    }

    /**
     * 继续上一场直播
     */
    private void continueLastLive() {
        // 订阅已经存在的流
//        CopyOnWriteArrayList<SubscribeRemoteStream> streams = mInteractSession.getSubscribeRemoteStreams();
//        if (streams != null && streams.size() > 0) {
//            SubscribeRemoteStream stream = streams.get(0);
//            if (stream.getUserRole() != CCInteractSession.PRESENTER) {
//                addStreamView(stream);
//            }
//        }
        // 发布本地流
        publish();
    }

    /**
     * 停止上一场直播
     */
    private void stopLastLive() {
        showLoading();
        mInteractSession.sendStopCommand(new CCInteractSession.AtlasCallBack<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                dismissLoading();
                toastOnUiThread("成功终止上一场异常停止直播");
            }

            @Override
            public void onFailure(String err) {
                dismissLoading();
                toastOnUiThread(err);
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

    private void initCameraPopup() {
        mCameraPopup = new BottomCancelPopup(this);
        mCameraPopup.setOutsideCancel(true);
        mCameraPopup.setKeyBackCancel(true);
        ArrayList<String> datas = new ArrayList<>();
        datas.add("切换摄像头");
        datas.add("关闭摄像头");
        mCameraPopup.setChooseDatas(datas);
        mCameraPopup.setOnChooseClickListener(new BottomCancelPopup.OnChooseClickListener() {
            @Override
            public void onClick(int index) {
                if (index == 1) {
                    closeCamera();
                } else {
                    switchCamera();
                }
            }
        });
    }

    private void initAuditorPopup() {
        mAuditorPopup = new BottomCancelPopup(this);
        mAuditorPopup.setOutsideCancel(true);
        mAuditorPopup.setKeyBackCancel(true);
        ArrayList<String> datas = new ArrayList<>();
        mAuditorPopup.setChooseDatas(datas);
        mAuditorPopup.setOnChooseClickListener(new BottomCancelPopup.OnChooseClickListener() {
            @Override
            public void onClick(int index) {
                switch (index) {
                    case 0:
                        mInteractSession.gagOne(!mInteractSession.isAuditorGag(mClickAuditorId), mClickAuditorId);
                        break;
                    case 1:
                        mInteractSession.kickUserFromRoom(mClickAuditorId);
                        break;
                }
            }
        });
    }

    private void initIconPopup() {
        mIconPopup = new BottomIconPopup(this);
        mIconPopup.setOutsideCancel(true);
        mIconPopup.setKeyBackCancel(true);
        mIconPopup.setOnChooseClickListener(new BottomIconPopup.OnChooseClickListener() {
            @Override
            public void onClick(int index, int tag, VideoStreamView videoStreamView) {
                if (tag == R.drawable.auth_draw_normal) {
                    mInteractSession.authUserDraw(videoStreamView.getStream().getUserId());

                } else if (tag == R.drawable.auth_draw_cancel_normal) {
                    mInteractSession.cancleAuthUserDraw(videoStreamView.getStream().getUserId());

                } else if (tag == R.drawable.setup_teacher) {
                    mInteractSession.SetupTeacher(videoStreamView.getStream().getUserId());

                } else if (tag == R.drawable.setup_teacher_canclenormal) {
                    mInteractSession.cancleSetupTeacher(videoStreamView.getStream().getUserId());

                } else if (tag == R.drawable.close_camera_normal) {
                    mInteractSession.toggleVideo(false, videoStreamView.getStream().getUserId());

                } else if (tag == R.drawable.open_camera_normal) {
                    mInteractSession.toggleVideo(true, videoStreamView.getStream().getUserId());

                } else if (tag == R.drawable.close_mic_normal) {
                    mInteractSession.toggleAudio(false, videoStreamView.getStream().getUserId());

                } else if (tag == R.drawable.open_mic_normal) {
                    mInteractSession.toggleAudio(true, videoStreamView.getStream().getUserId());

                } else if (tag == R.drawable.video_fullscreen_normal) {
                    if (mCurFragment instanceof LectureFragment) {
                        ((LectureFragment) mCurFragment).videoFullScreen(mIconPopup.getPopupStreamPosition());
                    } else if (mCurFragment instanceof MainVideoFragment) {
                        ((MainVideoFragment) mCurFragment).updateFollowId(videoStreamView, mIconPopup.getPopupStreamPosition());
                    } else if (mCurFragment instanceof TilingFragment) {
                    }

                } else if (tag == R.drawable.kickout_normal) {
                    showLoading();
                    mInteractSession.kickUserFromLianmai(videoStreamView.getStream().getUserId(), new CCInteractSession.AtlasCallBack<Void>() {
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

    /**
     * 添加第三方推流地址
     */
    private void addRtmpPath() {
        mInteractSession.addExternalOutput(mInteractSession.getPushUrl(), new CCInteractSession.AtlasCallBack<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailure(String err) {
                toastOnUiThread("addRtmpPath failure -" + err);
            }
        });
    }

    /**
     * 移除第三方推流地址
     */
    private void removeRtmpPath() {
        mInteractSession.removeExternalOutput(mInteractSession.getPushUrl(), new CCInteractSession.AtlasCallBack<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e(TAG, "remove onSuccess: ");
            }

            @Override
            public void onFailure(String err) {
                Log.e(TAG, "remove onFailure: " + err);
                toastOnUiThread("removeRtmpPath failure -" + err);
            }
        });
    }

    /**
     * 发布本地流
     */
    private synchronized void publish() {
        showLoading();
        mInteractSession.publish(new CCInteractSession.AtlasCallBack<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e(TAG, "onSuccess: [ " + mInteractSession.getLocalStreamId() + " ]");
                mInteractSession.setRegion(mInteractSession.getLocalStreamId());
                addRtmpPath();
                dismissLoading();
                if (mCurFragment instanceof LectureFragment) {
                    if (CCApplication.sClassDirection == 1 && mInteractSession.getTemplate() == CCInteractSession.TEMPLATE_SPEAK) {
                        ((LectureFragment) mCurFragment).authDraw(true, 0);
                        mDrawLayout.setVisibility(View.VISIBLE);
                        if (((LectureFragment) mCurFragment).isWhitboard()) {
                            mPageChangeLayout.setVisibility(View.GONE);
                        } else {
                            mPageChangeLayout.setVisibility(View.VISIBLE);
                        }
                    } else if (CCApplication.sClassDirection == 0 && mInteractSession.getTemplate() == CCInteractSession.TEMPLATE_SPEAK) {
                        ((LectureFragment) mCurFragment).authDraw(true, 0);
                    }
                    ((LectureFragment) mCurFragment).sendCurrentDocPage();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStartLayout.setVisibility(View.GONE);
                        mStopLayout.setVisibility(View.VISIBLE);
                        mMic.setBackgroundResource(R.drawable.mic_selector);
                        mCamera.setBackgroundResource(R.drawable.camera_selector);
                        isPublish = true;
                    }
                });
            }

            @Override
            public void onFailure(String err) {
                dismissLoading();
                toastOnUiThread(err);
            }
        });
    }

    private synchronized void unpublish(final boolean flag) {
        showLoading();
        if (mCurFragment instanceof LectureFragment) {
            mCurFragment.classStop();
        }
        mInteractSession.unpublish(new CCInteractSession.AtlasCallBack() {
            @Override
            public void onSuccess(Object o) {
                removeRtmpPath();
                dismissLoading();
                isPublish = false;
                mNamedTimeEnd = 0L; // 更新点名结束时间
                if (CCApplication.sClassDirection == 1 && mInteractSession.getTemplate() == CCInteractSession.TEMPLATE_SPEAK) {
                    ((LectureFragment) mCurFragment).authDraw(false, 0);//直播结束，停止画笔
                    mDrawLayout.setVisibility(View.GONE);
                } else if (CCApplication.sClassDirection == 0 && mInteractSession.getTemplate() == CCInteractSession.TEMPLATE_SPEAK) {
                    ((LectureFragment) mCurFragment).authDraw(false, 0);//直播结束，停止画笔
                }
                if (flag) {
                    finishGoHome();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mStartLayout.setVisibility(View.VISIBLE);
                            mStopLayout.setVisibility(View.GONE);
                            updateHandUpFlag(false);
                        }
                    });
                }
            }

            @Override
            public void onFailure(String err) {
                dismissLoading();
                toastOnUiThread(err);
                if (flag) {
                    finishGoHome();
                }
            }
        });
    }

    /**
     * 关闭摄像头
     */
    private void closeCamera() {
        mInteractSession.disableVideo(true);
        mSelfRenderer.cleanFrame();
        mCamera.setBackgroundResource(R.drawable.camera_close_selector);
    }

    /**
     * 开启摄像头
     */
    private void openCamera() {
        mInteractSession.enableVideo(true);
        mCamera.setBackgroundResource(R.drawable.camera_selector);
    }

    /**
     * 切换摄像头
     */
    private void switchCamera() {
        mInteractSession.switchCamera(mSelfRenderer, new CCInteractSession.AtlasCallBack<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailure(String err) {
                toastOnUiThread(err);
            }
        });
    }

    private void initMorePopup() {
        mMorePopup = new MorePopup(this);
        ArrayList<MoreItem> moreItems = new ArrayList<>();
        moreItems.add(new MoreItem(0, R.drawable.doc_icon, "提取文档"));
        moreItems.add(new MoreItem(1, R.drawable.named_icon, "点名"));
        moreItems.add(new MoreItem(2, R.drawable.layout_icon, "布局切换"));
        isMoreItemHasDoc = true;
        mMorePopup.setMoreItems(moreItems);
        mMorePopup.setOutsideCancel(true);
        mMorePopup.setKeyBackCancel(true);
        mMorePopup.setOnMoreItemClickListener(new MorePopup.OnMoreItemClickListener() {
            @Override
            public void onClick(int position) {
                switch (position) {
                    case 0:
                        go(DocListActivity.class, Config.TEACHER_REQUEST_CODE);
                        break;
                    case 1:
                        if (!mInteractSession.isRoomLive()) {
                            showToast("直播未开始");
                            return;
                        }
                        if (CCApplication.mNamedTimeEnd != 0) {
                            NamedCountActivity.startSelf(TeacherActivity.this,
                                    (int) (CCApplication.mNamedTimeEnd - System.currentTimeMillis()) / 1000);
                        } else {
                            go(NamedActivity.class);
                        }
                        break;
                    case 2:
                        go(LayoutChooseActivity.class, Config.TEACHER_REQUEST_CODE);
                        break;
                }
            }
        });
    }

    @OnClick(R2.id.id_teacher_class_user_list)
    void classUserList() {
        isGo = true;
        ListActivity.startSelf(this, CCInteractSession.PRESENTER, mCount);
    }

    @OnClick(R2.id.id_teacher_close)
    void close() {
        mExitAction = EXIT_ACTION_CLOSE;
        if (isPublish) {
            mExitPopup.setTip("是否确认离开课堂？离开后将结束直播");
        } else {
            mExitPopup.setTip("是否确认离开课堂？");
        }
        mExitPopup.show(mRoot);
    }

    @Override
    public void onBackPressed() {
        if (mChatImageLayout.getVisibility() == View.VISIBLE) {
            mChatImageLayout.setVisibility(View.GONE);
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
        if (mBlurRoot.getVisibility() == View.VISIBLE) {
            dismissBlur();
            return;
        }
        mExitAction = EXIT_ACTION_CLOSE;
        if (isPublish) {
            mExitPopup.setTip("是否确认离开课堂？离开后将结束直播");
        } else {
            mExitPopup.setTip("是否确认离开课堂？");
        }
        mExitPopup.show(mRoot);
    }

    @OnClick(R2.id.id_teacher_chat_img_fullscreen_layout)
    void dismissChatImage() {
        mChatImageLayout.setVisibility(View.GONE);
    }

//    @OnClick(R2.id.id_teacher_setting)
//    void setting() {
//        go(SettingActivity.class);
//    }

    @OnClick(R2.id.id_teacher_follow)
    void follow() {
        showLoading();
        String userid;
        if (isFollow) {
            userid = "";
        } else {
            userid = ((MainVideoFragment) mCurFragment).getMainVideoUserid();
        }
        mInteractSession.changeMainStreamInSigleTemplate(
                userid, new CCInteractSession.AtlasCallBack<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateFollow();
                        dismissLoading();
                    }

                    @Override
                    public void onFailure(String err) {
                        dismissLoading();
                        toastOnUiThread(err);
                    }
                });
    }

    @OnClick(R2.id.id_teacher_video_controller)
    void showOrDismissVideos() {
        mCurFragment.getRecyclerView().setVisibility(isVideoShow ? View.GONE : View.VISIBLE);
        isVideoShow = !isVideoShow;
        mVideoController.setBackgroundResource(isVideoShow ? R.drawable.draw_hide : R.drawable.draw_hide_on);
    }

    @OnClick(R2.id.id_teacher_start)
    void start() {
        // 判断直播间的状态
        if (mInteractSession.isRoomLive()) {
            // 当前直播间正在直播 进行弹窗提示
            mLivePopup.show(mRoot);
        } else {
            // 当前直播间没有直播 正常开始直播
            publish();
        }
    }

    @OnClick(R2.id.id_teacher_stop)
    void stop() {
        mExitAction = EXIT_ACTION_STOP;
        mExitPopup.setTip("是否确认结束直播？");
        mExitPopup.show(mRoot);
    }

    @OnClick(R2.id.id_teacher_draw_paint)
    void showDrawStyle() {
        if ((mDrawLayout.getY() + mDrawLayout.getHeight() + 20 + mPopupView.getHeight()) >
                DensityUtil.getHeight(this)) {
            mPopupWindow.showArrowTo(mDrawPaint, BubbleStyle.ArrowDirection.Down, 20);
        } else {
            mPopupWindow.showArrowTo(mDrawPaint, BubbleStyle.ArrowDirection.Up, 20);
        }
    }

    @OnClick(R2.id.id_teacher_draw_undo)
    void drawUndo() {
        if (mCurFragment instanceof LectureFragment) {
            ((LectureFragment) mCurFragment).undo();
        }
    }


    @OnClick(R2.id.id_teacher_draw_clear)
    void drawClear() {
        if (mCurFragment instanceof LectureFragment) {
            ((LectureFragment) mCurFragment).clear();
        }
    }

    @OnClick(R2.id.id_teacher_doc_back)
    void docBack() {
        if (mCurFragment instanceof LectureFragment) {
            ((LectureFragment) mCurFragment).docBack();
        }
    }

    @OnClick(R2.id.id_teacher_doc_forward)
    void docForward() {
        if (mCurFragment instanceof LectureFragment) {
            ((LectureFragment) mCurFragment).docForward();
        }
    }

//    @OnClick(R2.id.id_lecture_doc_img_grid)
//    void showDocImgGrid() {
//        Bundle bundle = new Bundle();
//        if (mCurDocInfo == null) {
//            bundle.putStringArrayList("doc_img_list", null);
//        } else {
//            bundle.putStringArrayList("doc_img_list", mCurDocInfo.getAllImgUrls());
//        }
//        ((BaseActivity) mActivity).isGo = true;
//        go(DocImgGridActivity.class, bundle, Config.LECTURE_REQUEST_CODE);
//    }

    boolean isShowMore = false;

    private void setTopMargin(ImageButton item, int value) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) item.getLayoutParams();
        params.topMargin = DensityUtil.dp2px(this, value);
        item.setLayoutParams(params);
    }

    private void setStartMargin(ImageButton item, int value) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) item.getLayoutParams();
        params.leftMargin = DensityUtil.dp2px(this, value);
        item.setLayoutParams(params);
    }

    private void setEndMargin(ImageButton item, int value) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) item.getLayoutParams();
        params.rightMargin = DensityUtil.dp2px(this, value);
        item.setLayoutParams(params);
    }

    @OnClick(R2.id.id_teacher_more)
    void more() {
        if (isShowMore) {
            return;
        }
//        mMorePopup.show(mRoot);
        isShowMore = true;
        RelativeLayout.LayoutParams params;
        if (isMoreItemHasDoc) {
            if (CCApplication.sClassDirection == 0) {
                setTopMargin(mBlurDoc, 120);
                setTopMargin(mBlurUpdateImg, 120);
                setTopMargin(mBlurNamed, 250);
                setTopMargin(mBlurLayout, 250);
                setTopMargin(mBlurSetting, 380);
                setStartMargin(mBlurDoc, 60);
                setEndMargin(mBlurUpdateImg, 60);
                setStartMargin(mBlurNamed, 60);
                setEndMargin(mBlurLayout, 60);
                setStartMargin(mBlurSetting, 60);
            } else {
                setTopMargin(mBlurDoc, 10);
                setTopMargin(mBlurUpdateImg, 10);
                setTopMargin(mBlurNamed, 125);
                setTopMargin(mBlurLayout, 125);
                setTopMargin(mBlurSetting, 240);
                setStartMargin(mBlurDoc, 160);
                setEndMargin(mBlurUpdateImg, 160);
                setStartMargin(mBlurNamed, 160);
                setEndMargin(mBlurLayout, 160);
                setStartMargin(mBlurSetting, 160);
            }
        } else {
            mBlurDoc.setVisibility(View.GONE);
            mBlurUpdateImg.setVisibility(View.GONE);
            if (CCApplication.sClassDirection == 0) {
                setTopMargin(mBlurNamed, 120);
                setTopMargin(mBlurLayout, 120);
                setTopMargin(mBlurSetting, 250);
                setStartMargin(mBlurNamed, 60);
                setEndMargin(mBlurLayout, 60);
                setStartMargin(mBlurSetting, 60);
            } else {
                setTopMargin(mBlurNamed, 60);
                setTopMargin(mBlurLayout, 60);
                setTopMargin(mBlurSetting, 200);
                setStartMargin(mBlurNamed, 160);
                setEndMargin(mBlurLayout, 160);
                setStartMargin(mBlurSetting, 160);
            }
        }
        childEnterAnim();
        final float radius = 20;
        final View decorView = getWindow().getDecorView();
        //Activity's root View. Can also be root View of your layout (preferably)
        final ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        //set background, if your root layout doesn't have one
        final Drawable windowBackground = decorView.getBackground();
        mBlurView.setupWith(rootView)
                .windowBackground(windowBackground)
                .blurAlgorithm(new SupportRenderScriptBlur(this))
                .blurRadius(radius);
    }

    private void childEnterAnim() {
        final ImageButton[] imageButtons;
        if (isMoreItemHasDoc) {
            imageButtons = mBlurAnimViews;
        } else {
            imageButtons = mBlurNoDocAnimViews;
        }
        for (int i = 0; i < imageButtons.length; i++) {
            final int finalI = i;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Animation translateAnim = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                            Animation.RELATIVE_TO_PARENT, 1f, Animation.RELATIVE_TO_SELF, 0f
                    );
                    translateAnim.setDuration(200);
                    mBlurRoot.setVisibility(View.VISIBLE);
                    mBlurView.setVisibility(View.VISIBLE);
                    mBlurView.startAnimation(mBlurInAnim);
                    imageButtons[finalI].setVisibility(View.VISIBLE);
                    imageButtons[finalI].setAnimation(translateAnim);
                    imageButtons[finalI].startAnimation(translateAnim);
                }
            }, 200 + i * 50);
        }
    }

    @OnClick(R2.id.id_teacher_blur_clickable)
    void dismissBlur() {
        childExitAnim();
    }

    private void dismissBlurWithAnim() {
        mBlurView.startAnimation(mBlurOutAnim);
        mBlurOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mBlurRoot.setVisibility(View.GONE);
                mBlurView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void childExitAnim() {
        final ImageButton[] imageButtons;
        if (isMoreItemHasDoc) {
            imageButtons = mBlurAnimViews;
        } else {
            imageButtons = mBlurNoDocAnimViews;
        }
        for (int i = imageButtons.length - 1; i >= 0; i--) {
            final int finalI = i;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Animation translateAnim = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_PARENT, 1f
                    );
                    translateAnim.setDuration(150);
                    translateAnim.setAnimationListener(new MyAnimListener(imageButtons.length, finalI, imageButtons[finalI]));
                    imageButtons[finalI].setAnimation(translateAnim);
                    imageButtons[finalI].startAnimation(translateAnim);
                }
            }, (imageButtons.length - 1 - i) * 50);
        }
    }

    private class MyAnimListener implements
            Animation.AnimationListener {

        private int total;
        private int index;
        private View child;

        MyAnimListener(int total, int index, View child) {
            this.total = total;
            this.index = index;
            this.child = child;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            child.setVisibility(View.GONE);
            if (index == total - 3) {
                dismissBlurWithAnim();
            }
            if (index == 0) {
                isShowMore = false;
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private void dismissBlurAtOnce() {
        isShowMore = false;
        mBlurRoot.setVisibility(View.GONE);
        mBlurView.setVisibility(View.GONE);
        for (ImageButton view :
                mBlurAnimViews) {
            view.setVisibility(View.GONE);
        }
    }

    @OnClick(R2.id.id_teacher_blur_doc)
    void goDoc() {
        dismissBlurAtOnce();
        go(DocListActivity.class, Config.TEACHER_REQUEST_CODE);
    }

    @OnClick(R2.id.id_teacher_blur_update_img)
    void updateImg() {
        isGo = true;
        dismissBlurAtOnce();
        openSystemAlbum(REQUEST_SYSTEM_PICTURE_UPDATE);
    }

    @OnClick(R2.id.id_teacher_blur_named)
    void goRoll() {
        dismissBlurAtOnce();
        if (!mInteractSession.isRoomLive()) {
            showToast("直播未开始");
            return;
        }
        if (CCApplication.mNamedTimeEnd != 0) {
            isGo = true;
            NamedCountActivity.startSelf(TeacherActivity.this,
                    (int) (CCApplication.mNamedTimeEnd - System.currentTimeMillis()) / 1000);
        } else {
            go(NamedActivity.class);
        }
    }

    @OnClick(R2.id.id_teacher_blur_layout)
    void goLayout() {
        dismissBlurAtOnce();
        go(LayoutChooseActivity.class, Config.TEACHER_REQUEST_CODE);
    }

    @OnClick(R2.id.id_teacher_blur_setting)
    void goSetting() {
        dismissBlurAtOnce();
        go(SettingActivity.class);
    }

    @OnClick(R2.id.id_teacher_mic)
    void toggleMic() {
        if (!mInteractSession.isAllowAudio()) {
            mInteractSession.enableAudio(true);
            mMic.setBackgroundResource(R.drawable.mic_selector);
        } else {
            mInteractSession.disableAudio(true);
            mMic.setBackgroundResource(R.drawable.mic_close_selector);
        }
    }

    @OnClick(R2.id.id_teacher_camera)
    void camera() {
        if (mInteractSession.isAllowVideo()) {
            mCameraPopup.show(mRoot);
        } else {
            openCamera();
        }
    }

    @OnClick(R2.id.id_teacher_draw_tbc)
    void showActionBar() {

        toggleTopAndBottom();
    }

    @OnClick(R2.id.id_teacher_click_dismiss_chat)
    void clickDismissChatLayout() {
        mSoftKeyBoardUtil.hideKeyboard(mChatInput);
    }

    @OnClick(R2.id.id_teacher_chat)
    void chat() {
        mChatList.setVisibility(View.GONE);
        mClickDismissChatLayout.setVisibility(View.VISIBLE);
        mSoftKeyBoardUtil.showKeyboard(mChatInput);
    }

    @OnClick(R2.id.id_teacher_chat_send)
    void chatSend() {
        String content = mChatInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            toastOnUiThread("禁止发送空消息");
            return;
        }
        content = transformMsg(content);
        mInteractSession.sendMsg(content);
        mChatInput.setText("");
        mChatInput.setFocusableInTouchMode(false);
        mChatInput.setFocusable(false);
        mSoftKeyBoardUtil.hideKeyboard(mChatInput);
    }

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
    private void openSystemAlbum(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, requestCode);
    }

    @OnClick(R2.id.id_teacher_chat_open_img)
    void openImg() {
        /*
         * 这个地方可以自己实现
         */
        openSystemAlbum(REQUEST_SYSTEM_PICTURE_CHAT);
    }

    @Override
    public void showFollow() {
        mFollow.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissFollow() {
        if (mFollow != null) {
            mFollow.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateFollow() {
        if (TextUtils.isEmpty(mInteractSession.teacherFollowUserID())) {
            isFollow = false;
            mFollow.setBackgroundResource(R.drawable.follow_selector);
        } else {
            isFollow = true;
            mFollow.setBackgroundResource(R.drawable.follow_on);
        }
    }

    @Override
    public void dismissVideoController() {
        mVideoController.setVisibility(View.GONE);
    }

    @Override
    public void showVideoController() {
        if (!isVideoShow) {
            mVideoController.setVisibility(View.VISIBLE);
            mVideoController.setBackgroundResource(R.drawable.draw_hide);
        } else {
            mVideoController.setVisibility(View.VISIBLE);
        }
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

    @Override
    public void toggleTopLayout(boolean isOnlyDoc) {
        if (mTopDistance == -1) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTopLayout.getLayoutParams();
            mTopDistance = params.topMargin + mTopLayout.getHeight();
        }
        if (isOnlyDoc) {

            if (CCApplication.sClassDirection == 1) {
//                animateTop(mTopDistance);
            } else {
                animateTop(mTopDistance);
            }
        } else {

            if (CCApplication.sClassDirection == 1) {
//                animateTop(-mTopDistance);
            } else {
                animateTop(-mTopDistance);
            }
        }
    }

    @Override
    public void docFullScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mChatList.setVisibility(View.GONE);
        mTopLayout.setVisibility(View.GONE);
        mBottomLayout.setVisibility(View.GONE);
        mDrawTBC.setVisibility(View.GONE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void exitDocFullScreen() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mChatList.setVisibility(View.VISIBLE);
        mTopLayout.setVisibility(View.VISIBLE);
        mBottomLayout.setVisibility(View.VISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void videoFullScreen() {
        mChatList.setVisibility(View.GONE);
        mTopLayout.setVisibility(View.GONE);
        mBottomLayout.setVisibility(View.GONE);
        mDrawLayout.setVisibility(View.GONE);
    }

    @Override
    public void exitVideoFullScreen() {

        if (mCurFragment instanceof LectureFragment && CCApplication.sClassDirection == 1) {
            mChatList.setVisibility(View.GONE);
        } else {
            mChatList.setVisibility(View.VISIBLE);
        }
        if (mInteractSession.isRoomLive()
                && (CCApplication.sClassDirection == 1 || ((LectureFragment) mCurFragment).isDocFullScreen())) {
            mDrawLayout.setVisibility(View.VISIBLE);
        } else {
            mDrawLayout.setVisibility(View.GONE);
        }
        mChatList.setVisibility(View.VISIBLE);
        mTopLayout.setVisibility(View.VISIBLE);
        mBottomLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onVideoClick(int position, VideoStreamView videoStreamView) {
        mIconPopup.show(mRoot, position, videoStreamView);
    }

}

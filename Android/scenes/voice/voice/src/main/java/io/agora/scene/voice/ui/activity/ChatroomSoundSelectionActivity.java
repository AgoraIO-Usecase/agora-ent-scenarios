package io.agora.scene.voice.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;

import java.util.List;

import io.agora.CallBack;
import io.agora.scene.voice.VoiceConfigManager;
import io.agora.scene.voice.bean.SoundSelectionBean;
import io.agora.scene.voice.ui.soundselection.RoomSoundSelectionConstructor;
import io.agora.voice.baseui.BaseActivity;
import io.agora.voice.baseui.general.callback.OnResourceParseCallback;
import io.agora.voice.buddy.tool.FastClickTools;
import io.agora.voice.buddy.tool.LogTools;
import io.agora.voice.buddy.tool.ThreadManager;
import io.agora.voice.buddy.tool.ToastTools;
import io.agora.scene.voice.R;
import io.agora.scene.voice.general.interfaceOrImplement.UserActivityLifecycleCallbacks;
import io.agora.scene.voice.general.repositories.ProfileManager;
import io.agora.scene.voice.model.RoomViewModel;
import io.agora.scene.voice.ui.adapter.ChatroomSoundSelectionAdapter;
import io.agora.voice.buddy.config.ConfigConstants;
import io.agora.voice.buddy.config.RouterParams;
import io.agora.voice.buddy.config.RouterPath;
import io.agora.scene.voice.ui.widget.titlebar.RoomTitleBar;
import io.agora.util.EMLog;
import io.agora.voice.imkit.manager.ChatroomHelper;
import io.agora.voice.network.tools.bean.VRUserBean;
import io.agora.voice.network.tools.bean.VRoomInfoBean;

public class ChatroomSoundSelectionActivity extends BaseActivity implements ChatroomSoundSelectionAdapter.OnItemClickListener, RoomTitleBar.OnBackPressListener, View.OnClickListener {
    private RoomTitleBar titleBar;
    private RecyclerView recyclerView;
    private ConstraintLayout goLive;
    private ChatroomSoundSelectionAdapter adapter;
    private RoomViewModel chatroomViewModel;
    private boolean isPublic = true;
    private String roomName;
    private String encryption;
    private String soundEffect;
    private int roomType;
    private int sound_effect = ConfigConstants.SoundSelection.Social_Chat;

    private final String TAG = ChatroomSoundSelectionActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        setNavAndStatusBarTransparent(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.voice_activity_sound_selection_layout;
    }

    @Override
    protected void initSystemFit() {
        setFitSystemForTheme(false, "#00000000");
        setStatusBarTextColor(false);
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        if (null != intent) {
            roomName = intent.getStringExtra(RouterParams.KEY_CHATROOM_CREATE_NAME);
            isPublic = intent.getBooleanExtra(RouterParams.KEY_CHATROOM_CREATE_IS_PUBLIC, true);
            encryption = intent.getStringExtra(RouterParams.KEY_CHATROOM_CREATE_ENCRYPTION);
            roomType = intent.getIntExtra(RouterParams.KEY_CHATROOM_CREATE_ROOM_TYPE, 0);
        }
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        recyclerView = findViewById(R.id.list);
        goLive = findViewById(R.id.bottom_layout);
        adapter = new ChatroomSoundSelectionAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        float offsetPx = getResources().getDimension(R.dimen.space_84dp);
        BottomOffsetDecoration bottomOffsetDecoration = new BottomOffsetDecoration((int) offsetPx);
        recyclerView.addItemDecoration(bottomOffsetDecoration);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void initListener() {
        super.initListener();
        adapter.SetOnItemClickListener(this);
        titleBar.setOnBackPressListener(this);
        goLive.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        getSoundSelectionData(this);
        adapter.setSelectedPosition(0);
        soundEffect = ConfigConstants.SoundSelectionText.Social_Chat;
        chatroomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);
        chatroomViewModel.getCreateObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<VRoomInfoBean>() {
                @Override
                public void onSuccess(@Nullable VRoomInfoBean data) {
                    if (null != data && null != data.getRoom()) {
                        VRUserBean userinfo = ProfileManager.getInstance().getProfile();
                        LogTools.logD("chat_uid: " + userinfo.getChat_uid(), TAG);
                        LogTools.logD("im_token: " + userinfo.getIm_token(), TAG);
                        ChatroomHelper.getInstance().login(userinfo.getChat_uid(), userinfo.getIm_token(), new CallBack() {
                            @Override
                            public void onSuccess() {
                                joinRoom(data);
                            }

                            @Override
                            public void onError(int code, String desc) {
                                EMLog.e("ChatroomSoundSelectionActivity", "Login Fail code: " + code + " desc: " + desc);
                                dismissLoading();
                            }
                        });
                    }
                }
            });
        });
    }

    public void createNormalRoom(boolean allow_free_join_mic, String sound_effect) {
        showLoading(false);
        if (isPublic) {
            chatroomViewModel.createNormalRoom(this, roomName, false, allow_free_join_mic, sound_effect);
        } else {
            if (!TextUtils.isEmpty(encryption) && encryption.length() == 4) {
                chatroomViewModel.createNormalRoom(this, roomName, true, encryption, allow_free_join_mic, sound_effect);
            } else {
                ToastTools.show(this, getString(R.string.voice_room_create_tips), Toast.LENGTH_LONG);
            }
        }
    }

    public void createSpatialRoom() {
        showLoading(false);
        if (isPublic) {
            chatroomViewModel.createSpatial(this, roomName, false);
        } else {
            if (!TextUtils.isEmpty(encryption) && encryption.length() == 4) {
                chatroomViewModel.createSpatial(this, roomName, true, encryption);
            } else {
                ToastTools.show(this, getString(R.string.voice_room_create_tips), Toast.LENGTH_LONG);
            }
        }
    }

    public void joinRoom(VRoomInfoBean data) {
        ThreadManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                dismissLoading();
                ARouter.getInstance()
                        .build(RouterPath.ChatroomPath)
                        .withSerializable(RouterParams.KEY_CHATROOM_DETAILS_INFO, data)
                        .navigation();
                finishCreateActivity();
                finish();
            }
        });
    }


    @Override
    public void OnItemClick(int position, SoundSelectionBean bean) {
        adapter.setSelectedPosition(position);
        sound_effect = bean.getSoundSelectionType();
    }

    public void getSoundSelectionData(Context context) {
        List<SoundSelectionBean> soundSelectionList = RoomSoundSelectionConstructor.builderSoundSelectionList(context, ConfigConstants.SoundSelection.Social_Chat);
        LogTools.logD("getData:" + soundSelectionList.size(), TAG);
        adapter.setData(soundSelectionList);
    }


    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }

    @Override
    public void onClick(View view) {
        if (roomType == 0) {
            switch (sound_effect) {
                case ConfigConstants.SoundSelection.Karaoke:
                    soundEffect = ConfigConstants.SoundSelectionText.Karaoke;
                    break;
                case ConfigConstants.SoundSelection.Gaming_Buddy:
                    soundEffect = ConfigConstants.SoundSelectionText.Gaming_Buddy;
                    break;
                case ConfigConstants.SoundSelection.Professional_Broadcaster:
                    soundEffect = ConfigConstants.SoundSelectionText.Professional_Broadcaster;
                    break;
                default:
                    soundEffect = ConfigConstants.SoundSelectionText.Social_Chat;
                    break;
            }
            if (!FastClickTools.isFastClick(view, 1000))
                createNormalRoom(false, soundEffect);
        }
//      else {
//         createSpatialRoom();
//      }

    }

    /**
     * 结束创建activity
     */
    protected void finishCreateActivity() {
        UserActivityLifecycleCallbacks lifecycleCallbacks = VoiceConfigManager.getLifecycleCallbacks();
        if (lifecycleCallbacks == null) {
            finish();
            return;
        }
        List<Activity> activities = lifecycleCallbacks.getActivityList();
        if (activities == null || activities.isEmpty()) {
            finish();
            return;
        }
        for (Activity activity : activities) {
            if (activity != lifecycleCallbacks.current() && activity instanceof ChatroomCreateActivity) {
                activity.finish();
            }
        }
    }

    static class BottomOffsetDecoration extends RecyclerView.ItemDecoration {
        private int mBottomOffset;

        public BottomOffsetDecoration(int bottomOffset) {
            mBottomOffset = bottomOffset;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int dataSize = state.getItemCount();
            int position = parent.getChildAdapterPosition(view);
            if (dataSize > 0 && position == dataSize - 1) {
                outRect.set(0, 0, 0, mBottomOffset);
            } else {
                outRect.set(0, 0, 0, 0);
            }

        }
    }
}

package io.agora.scene.ktv.live;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.Arrays;
import java.util.List;

import io.agora.lrcview.PitchView;
import io.agora.lrcview.bean.LrcData;
import io.agora.rtc2.Constants;
import io.agora.scene.base.KtvConstant;
import io.agora.scene.base.PagePathConstant;
import io.agora.scene.base.bean.MemberMusicModel;
import io.agora.scene.base.bean.room.RTMMessageBean;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.component.BaseViewBindingActivity;
import io.agora.scene.base.component.OnButtonClickListener;
import io.agora.scene.base.component.OnItemClickListener;
import io.agora.scene.base.data.model.AgoraMember;
import io.agora.scene.base.event.NetWorkEvent;
import io.agora.scene.base.manager.RTCManager;
import io.agora.scene.base.manager.RTMManager;
import io.agora.scene.base.manager.RoomManager;
import io.agora.scene.base.utils.ThreadManager;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.ActivityRoomLivingBinding;
import io.agora.scene.ktv.databinding.KtvItemRoomSpeakerBinding;
import io.agora.scene.ktv.dialog.MoreDialog;
import io.agora.scene.ktv.dialog.MusicSettingDialog;
import io.agora.scene.ktv.dialog.RoomChooseSongDialog;
import io.agora.scene.ktv.dialog.UserLeaveSeatMenuDialog;
import io.agora.scene.ktv.live.fragment.dialog.MVFragment;
import io.agora.scene.ktv.live.holder.RoomPeopleHolder;
import io.agora.scene.ktv.widget.LrcControlView;
import io.agora.scene.widget.DividerDecoration;
import io.agora.scene.widget.dialog.CloseRoomDialog;
import io.agora.scene.widget.dialog.CommonDialog;
import io.agora.scene.widget.utils.UiUtils;

/**
 * 房间主页
 */
@Route(path = PagePathConstant.pageRoomLiving)
public class RoomLivingActivity extends BaseViewBindingActivity<ActivityRoomLivingBinding> implements OnItemClickListener<AgoraMember> {
    private RoomLivingViewModel roomLivingViewModel;
    private MoreDialog moreDialog;
    private MusicSettingDialog musicSettingDialog;
    private BaseRecyclerViewAdapter<KtvItemRoomSpeakerBinding, AgoraMember, RoomPeopleHolder> mRoomSpeakerAdapter;
    private boolean isInitList = false;
    private CloseRoomDialog creatorExitDialog;

    private CommonDialog exitDialog;
    private UserLeaveSeatMenuDialog mUserLeaveSeatMenuDialog;
    private AgoraMember mAgoraMember = null;

    @Override
    protected ActivityRoomLivingBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return ActivityRoomLivingBinding.inflate(inflater);
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        roomLivingViewModel = new ViewModelProvider(this).get(RoomLivingViewModel.class);
        roomLivingViewModel.setLifecycleOwner(this);
        roomLivingViewModel.initData();
        mRoomSpeakerAdapter = new BaseRecyclerViewAdapter<>(Arrays.asList(new AgoraMember[8]),
                this, RoomPeopleHolder.class);
        getBinding().rvUserMember.addItemDecoration(new DividerDecoration(4, 24, 8));
        getBinding().rvUserMember.setAdapter(mRoomSpeakerAdapter);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            getBinding().rvUserMember.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        getBinding().lrcControlView.setRole(LrcControlView.Role.Listener);
        getBinding().lrcControlView.post(() -> {
            roomLivingViewModel.joinRoom();
            roomLivingViewModel.requestRTMToken();
        });
        if (RoomManager.mMine.userNo.equals(RoomManager.mRoom.creatorNo)) {
            requestRecordPermission();
        }
    }


    private void setNetWorkStatus(int txQuality, int rxQuality) {
        if (txQuality == Constants.QUALITY_BAD || txQuality == Constants.QUALITY_POOR
                || rxQuality == Constants.QUALITY_BAD || rxQuality == Constants.QUALITY_POOR) {
            getBinding().ivNetStatus.setImageResource(R.drawable.bg_round_yellow);
            getBinding().tvNetStatus.setText(R.string.net_status_m);
        } else if (txQuality == Constants.QUALITY_VBAD || txQuality == Constants.QUALITY_DOWN
                || rxQuality == Constants.QUALITY_VBAD || rxQuality == Constants.QUALITY_VBAD) {
            getBinding().ivNetStatus.setImageResource(R.drawable.bg_round_red);
            getBinding().tvNetStatus.setText(R.string.net_status_low);
        } else if (txQuality == Constants.QUALITY_EXCELLENT || txQuality == Constants.QUALITY_GOOD
                || rxQuality == Constants.QUALITY_EXCELLENT || rxQuality == Constants.QUALITY_GOOD) {
            getBinding().ivNetStatus.setImageResource(R.drawable.bg_round_green);
            getBinding().tvNetStatus.setText(R.string.net_status_good);
        } else if (txQuality == Constants.QUALITY_UNKNOWN || rxQuality == Constants.QUALITY_UNKNOWN) {
            getBinding().ivNetStatus.setImageResource(R.drawable.bg_round_red);
            getBinding().tvNetStatus.setText(R.string.net_status_un_know);
        } else {
            getBinding().ivNetStatus.setImageResource(R.drawable.bg_round_green);
            getBinding().tvNetStatus.setText(R.string.net_status_good);
        }
    }

    @Override
    public void onItemClick(@NonNull AgoraMember agoraMember, View view, int position, long viewType) {
        mAgoraMember = agoraMember;
        if (!TextUtils.isEmpty(agoraMember.userNo)) {
            if (RoomManager.mMine.isMaster) {
                if (!mRoomSpeakerAdapter.dataList.get(position).userNo.equals(RoomManager.mMine.userNo)) {
                    showUserLeaveSeatMenuDialog();
                }
            } else if (mRoomSpeakerAdapter.dataList.get(position).userNo.equals(RoomManager.mMine.userNo)) {
                showUserLeaveSeatMenuDialog();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("cwtsw", "onResume() " + isBlackDarkStatus());
        setDarkStatusIcon(isBlackDarkStatus());
    }

    /**
     * 下麦提示
     */
    private void showUserLeaveSeatMenuDialog() {
        if (mUserLeaveSeatMenuDialog == null) {
            mUserLeaveSeatMenuDialog = new UserLeaveSeatMenuDialog(this);
            mUserLeaveSeatMenuDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                    setDarkStatusIcon(isBlackDarkStatus());
                }

                @Override
                public void onRightButtonClick() {
                    setDarkStatusIcon(isBlackDarkStatus());
                    roomLivingViewModel.leaveSeat(mAgoraMember);
                }
            });
        }
        mUserLeaveSeatMenuDialog.setAgoraMember(mAgoraMember);
        mUserLeaveSeatMenuDialog.show();
    }

    @Override
    public void onItemClick(View view, int position, long viewType) {
        if (position == -1) return;
        //点击坐位 上麦克
        AgoraMember agoraMember = mRoomSpeakerAdapter.dataList.get(position);
        if (agoraMember == null) {
            if (RoomManager.getInstance().getMine().role == AgoraMember.Role.Listener) {
                roomLivingViewModel.haveSeat(position);
                requestRecordPermission();
            }
        }
    }

    @Override
    public void initListener() {
        getBinding().ivExit.setOnClickListener(view -> {
            showExitDialog();
        });
        getBinding().superLayout.setOnClickListener(view -> {
            setDarkStatusIcon(isBlackDarkStatus());
        });
        getBinding().cbMic.setOnCheckedChangeListener((compoundButton, b) -> {
            if (mRoomSpeakerAdapter.getItemData(RoomManager.mMine.onSeat) == null) {
                return;
            }
            roomLivingViewModel.toggleMic(b ? 0 : 1);
            mRoomSpeakerAdapter.getItemData(RoomManager.mMine.onSeat).isSelfMuted = b ? 0 : 1;
            mRoomSpeakerAdapter.notifyItemChanged(RoomManager.mMine.onSeat);
        });
        getBinding().iBtnChorus.setOnClickListener(this::showChooseSongDialog);
        getBinding().iBtnChooseSong.setOnClickListener(this::showChooseSongDialog);
        getBinding().btnMenu.setOnClickListener(this::showMoreDialog);
        getBinding().btnOK.setOnClickListener(view -> {
            getBinding().groupResult.setVisibility(View.GONE);
        });
        roomLivingViewModel.setOnLrcActionListener(getBinding().lrcControlView);
        getBinding().lrcControlView.setPitchViewOnActionListener(new PitchView.OnActionListener() {
            @Override
            public void onOriginalPitch(float pitch, int totalCount) {
            }

            @Override
            public void onScore(double score, double cumulativeScore, double totalScore) {
                getBinding().lrcControlView.updateScore(score);
            }
        });
        getBinding().cbVideo.setOnCheckedChangeListener((compoundButton, b) -> toggleSelfVideo(b));
        roomLivingViewModel.setISingleCallback((type, o) -> {
            ThreadManager.getMainHandler().post(() -> {
                if (isFinishing() || getBinding() == null) {
                    return;
                }
                hideLoadingView();
                if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_LOCAL_PITCH) {
                    getBinding().lrcControlView.getPitchView().updateLocalPitch((float) o);
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_BG_CHANGE) {
                    //修改背景
                    setPlayerBgFromMsg(Integer.parseInt((String) o));
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_MEMBER_COUNT_UPDATE) {
                    getBinding().tvRoomMCount.setText(getString(R.string.room_count, String.valueOf(RTMManager.getInstance().getMemberCount())));
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_JOIN_SUCCESS) {
                    if (!TextUtils.isEmpty(roomLivingViewModel.agoraRoom.bgOption)) {
                        setPlayerBgFromMsg(Integer.parseInt(roomLivingViewModel.agoraRoom.bgOption));
                    } else {
                        setPlayerBgFromMsg(0);
                    }
                    getBinding().tvRoomName.setText(roomLivingViewModel.agoraRoom.name);
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_EXIT) {
                    finish();
                } else if (type == KtvConstant.TYPE_CONTROL_VIEW_STATUS_ON_CREATOR_EXIT) {
                    showCreatorExitDialog();
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_ROOM_INFO_CHANGED) {
                    //修改背景
                    getBinding().lrcControlView.setLrcViewBackground(R.mipmap.portrait02);
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_EXIT) {
                    finish();
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_MEMBER_LEAVE) {
                    onMemberLeave((AgoraMember) o);
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_CONTROL_VIEW_ENABLED) {
                    getBinding().lrcControlView.setEnabled(((boolean) o));
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_MEMBER_JOIN) {
                    onMemberJoin((AgoraMember) o);
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_PLAY_COMPLETED) {
                    Log.d("cwtsw", "得分回调 userNo = " + RoomManager.mMine.userNo + " o = " + o);
                    if (RoomManager.mMine.userNo.equals(o)) {
                        Log.d("cwtsw", "计算得分");
                        int score = (int) getBinding().lrcControlView.getPitchView().getAverageScore();
                        getBinding().tvResultScore.setText(String.valueOf(score));
                        if (score >= 90) {
                            getBinding().ivResultLevel.setImageResource(R.mipmap.ic_s);
                        } else if (score >= 80) {
                            getBinding().ivResultLevel.setImageResource(R.mipmap.ic_a);
                        } else if (score >= 60) {
                            getBinding().ivResultLevel.setImageResource(R.mipmap.ic_b);
                        } else {
                            getBinding().ivResultLevel.setImageResource(R.mipmap.ic_c);
                        }
                        if (RoomManager.mMine.userNo.equals(RoomManager.getInstance().mMusicModel.userNo)) {
                            getBinding().groupResult.setVisibility(View.VISIBLE);
                            Log.d("cwtsw", "显示得分");
                        }
                    }
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_SEAT_CHANGE) {
                    mRoomSpeakerAdapter.notifyDataSetChanged();
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_SEAT_STATUS) {
                    Integer visible = (Integer) o;
                    getBinding().groupBottomView.setVisibility(visible);
                    getBinding().groupEmptyPrompt.setVisibility((visible == View.VISIBLE ? View.GONE : View.VISIBLE));
                } else if (type == KtvConstant.TYPE_CONTROL_VIEW_STATUS_ON_VIDEO) {
                    RTMMessageBean bean = (RTMMessageBean) o;
                    //头像打开摄像头
                    for (int i = 0; i < mRoomSpeakerAdapter.dataList.size(); i++) {
                        if (mRoomSpeakerAdapter.dataList.get(i) != null && mRoomSpeakerAdapter.dataList.get(i).userNo.equals(bean.userNo)) {
                            mRoomSpeakerAdapter.dataList.get(i).isVideoMuted = bean.isVideoMuted;
//                            mRoomSpeakerAdapter.dataList.get(i).setStreamId(bean.streamId.intValue());
                            mRoomSpeakerAdapter.notifyItemChanged(i);
                        }
                    }
                } else if (type == KtvConstant.TYPE_CONTROL_VIEW_STATUS_ON_MIC_MUTE) {
                    RTMMessageBean bean = (RTMMessageBean) o;
                    for (int i = 0; i < mRoomSpeakerAdapter.dataList.size(); i++) {
                        if (mRoomSpeakerAdapter.dataList.get(i) != null &&
                                mRoomSpeakerAdapter.dataList.get(i).userNo.equals(bean.userNo)) {
                            mRoomSpeakerAdapter.dataList.get(i).isSelfMuted = bean.isSelfMuted;
//                            mRoomSpeakerAdapter.dataList.get(i).setStreamId(bean.streamId.intValue());
                            mRoomSpeakerAdapter.notifyItemChanged(i);
                        }
                    }
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_MIC_STATUS) {
                    getBinding().cbMic.setChecked((boolean) o);
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_VIDEO_STATUS_CHANGED) {
                    onVideoStatusChange((AgoraMember) o);
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_MUSIC_DEL) {
//                onMusicDelete((MemberMusicModel) o);
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_MUSIC_CHANGED) {
                    onMusicChanged((MemberMusicModel) o);
                    getBinding().lrcControlView.setScoreControlView();
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_CONTROL_VIEW_PITCH_LRC_DATA) {
                    getBinding().lrcControlView.getLrcView().setLrcData((LrcData) o);
                    getBinding().lrcControlView.getPitchView().setLrcData((LrcData) o);
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_CONTROL_VIEW_TOTAL_DURATION) {
                    getBinding().lrcControlView.getLrcView().setTotalDuration((Long) o);
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_CONTROL_VIEW_UPDATE_TIME) {
                    getBinding().lrcControlView.getLrcView().updateTime((Long) o);
                    getBinding().lrcControlView.getPitchView().updateTime((Long) o);
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_COUNT_DOWN) {
                    getBinding().lrcControlView.setCountDown((Integer) o);
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_NETWORK_STATUS) {
                    NetWorkEvent event = (NetWorkEvent) (o);
                    setNetWorkStatus(event.txQuality, event.rxQuality);
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_JOINED_CHORUS) {
                    getBinding().lrcControlView.onMemberJoinedChorus();
                    mRoomSpeakerAdapter.notifyDataSetChanged();
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_SHOW_MEMBER_STATUS) {
                    if (!isInitList) {
                        for (AgoraMember member : (List<AgoraMember>) o) {
                            onMemberJoin(member);
                        }
                        isInitList = true;
                    }
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_ON_SEAT) {
                    RTMMessageBean msg = (RTMMessageBean) o;
                    AgoraMember member = new AgoraMember();
                    member.onSeat = msg.onSeat;
                    member.userNo = msg.userNo;
                    member.headUrl = msg.headUrl;
                    member.name = msg.name;
                    member.id = msg.id;
                    member.isSelfMuted = 0;
                    member.isVideoMuted = 0;
                    onMemberJoin(member);
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LEAVE_SEAT) {
                    RTMMessageBean msg = (RTMMessageBean) o;
                    AgoraMember member = new AgoraMember();
                    member.onSeat = msg.onSeat;
                    member.userNo = msg.userNo;
                    member.headUrl = msg.headUrl;
                    member.name = msg.name;
                    member.isSelfMuted = 0;
                    member.isVideoMuted = 0;
                    onMemberLeave(member);
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_MUSICEMPTY) {
                    getBinding().lrcControlView.setRole(LrcControlView.Role.Listener);
                    getBinding().lrcControlView.onIdleStatus();
                    mRoomSpeakerAdapter.notifyDataSetChanged();
                } else if (type == KtvConstant.CALLBACK_TYPE_SHOW_MUSIC_MENU_DIALOG) {
                    showMusicSettingDialog();
                } else if (type == KtvConstant.CALLBACK_TYPE_SHOW_CHANGE_MUSIC_DIALOG) {
                    showChangeMusicDialog();
                } else if (type == KtvConstant.CALLBACK_TYPE_TOGGLE_MIC) {
                    getBinding().cbMic.setEnabled((Boolean) o);
                } else if (type == KtvConstant.CALLBACK_TYPE_ROOM_LIVING_ON_CONTROL_VIEW_STATUS) {
                    if ((int) o == KtvConstant.TYPE_CONTROL_VIEW_STATUS_ON_PREPARE) {
                        getBinding().lrcControlView.onPrepareStatus();
                    } else if ((int) o == KtvConstant.TYPE_CONTROL_VIEW_STATUS_ON_WAIT_CHORUS) {
                        getBinding().lrcControlView.onWaitChorusStatus();
                    } else if ((int) o == KtvConstant.TYPE_CONTROL_VIEW_STATUS_ON_PLAY_STATUS) {
                        getBinding().lrcControlView.onPlayStatus();
                    } else if ((int) o == KtvConstant.TYPE_CONTROL_VIEW_STATUS_ON_PAUSE_STATUS) {
                        getBinding().lrcControlView.onPauseStatus();
                    } else if ((int) o == KtvConstant.TYPE_CONTROL_VIEW_STATUS_ON_LRC_RESET) {
                        getBinding().lrcControlView.getLrcView().reset();
                    }
                }
            });
        });

    }

    private void showExitDialog() {
        if (exitDialog == null) {
            exitDialog = new CommonDialog(this);

            if (RoomManager.mMine.isMaster) {
                exitDialog.setDialogTitle(getString(R.string.dismiss_room));
                exitDialog.setDescText(getString(R.string.confirm_to_dismiss_room));
            } else {
                exitDialog.setDialogTitle(getString(R.string.exit_room));
                exitDialog.setDescText(getString(R.string.confirm_to_exit_room));
            }
            exitDialog.setDialogBtnText(getString(R.string.ktv_cancel), getString(R.string.ktv_confirm));
            exitDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                    setDarkStatusIcon(isBlackDarkStatus());
                }

                @Override
                public void onRightButtonClick() {
                    setDarkStatusIcon(isBlackDarkStatus());
                    roomLivingViewModel.exitRoom();
                }
            });
        }
        exitDialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onMusicChanged(@NonNull MemberMusicModel music) {
        getBinding().lrcControlView.setMusic(music);
        if (music.userNo.equals(RoomManager.mMine.userNo)) {
//            RoomManager.mMine.role = AgoraMember.Role.Speaker;
            getBinding().lrcControlView.setRole(LrcControlView.Role.Singer);
        } else {
//            RoomManager.mMine.role = AgoraMember.Role.Listener;
            getBinding().lrcControlView.setRole(LrcControlView.Role.Listener);
        }
        roomLivingViewModel.onMusicStaticChanged(this, music);
        mRoomSpeakerAdapter.notifyDataSetChanged();
    }


    private void onVideoStatusChange(AgoraMember member) {
        for (int i = 0; i < mRoomSpeakerAdapter.dataList.size(); i++) {
            AgoraMember currentMember = mRoomSpeakerAdapter.dataList.get(i);
            if (currentMember != null && currentMember.userNo.equals(member.userNo)) {
                mRoomSpeakerAdapter.dataList.set(i, member);
                mRoomSpeakerAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    public void closeMenuDialog() {
        setDarkStatusIcon(isBlackDarkStatus());
        moreDialog.dismiss();
    }

    private void showChooseSongDialog(View view) {
        boolean isChorus = false;
        if (view.getId() == R.id.iBtnChorus) {
            isChorus = true;
        }
        new RoomChooseSongDialog(isChorus)
                .show(getSupportFragmentManager(), RoomChooseSongDialog.TAG);
    }

    private void showMoreDialog(View view) {
        if (moreDialog == null) {
            moreDialog = new MoreDialog(roomLivingViewModel.mSetting);
        }
        moreDialog.show(getSupportFragmentManager(), MoreDialog.TAG);
    }

    private void showMusicSettingDialog() {
        if (musicSettingDialog == null) {
            musicSettingDialog = new MusicSettingDialog(roomLivingViewModel.mSetting);
        }
        musicSettingDialog.show(getSupportFragmentManager(), MusicSettingDialog.TAG);
    }

    private CommonDialog changeMusicDialog;

    private void showChangeMusicDialog() {
        if (UiUtils.isFastClick(2000)) return;
        if (changeMusicDialog == null) {
            changeMusicDialog = new CommonDialog(this);
            changeMusicDialog.setDialogTitle(getString(R.string.ktv_room_change_music_title));
            changeMusicDialog.setDescText(getString(R.string.ktv_room_change_music_msg));
            changeMusicDialog.setDialogBtnText(getString(R.string.ktv_cancel), getString(R.string.ktv_confirm));
            changeMusicDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                    setDarkStatusIcon(isBlackDarkStatus());
                }

                @Override
                public void onRightButtonClick() {
                    setDarkStatusIcon(isBlackDarkStatus());
                    roomLivingViewModel.changeMusic();
                }
            });
        }
        changeMusicDialog.show();
    }

    public void setPlayerBgFromMsg(int position) {
        getBinding().lrcControlView.setLrcViewBackground(MVFragment.exampleBackgrounds.get(position));
    }

    public void setPlayerBg(int position) {
        roomLivingViewModel.setMV_BG(position);
        getBinding().lrcControlView.setLrcViewBackground(MVFragment.exampleBackgrounds.get(position));
    }

    @Override
    protected void onStart() {
        super.onStart();
        roomLivingViewModel.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        roomLivingViewModel.onStop();
    }

    @Override
    public boolean isBlackDarkStatus() {
        return false;
    }

    private boolean isOpenSelfVideo = false;

    //开启 关闭摄像头
    private void toggleSelfVideo(boolean isOpen) {
        isOpenSelfVideo = true;
        if (isOpen) {
            RoomManager.mMine.isVideoMuted = 1;
        } else {
            RoomManager.mMine.isVideoMuted = 0;
        }
        requestCameraPermission();
    }

    @Override
    public void getPermissions() {
        if (isOpenSelfVideo) {
            mRoomSpeakerAdapter.getItemData(RoomManager.mMine.onSeat).isVideoMuted = RoomManager.mMine.isVideoMuted;
            mRoomSpeakerAdapter.notifyItemChanged(RoomManager.mMine.onSeat);
            roomLivingViewModel.toggleSelfVideo(RoomManager.mMine.isVideoMuted);
            isOpenSelfVideo = false;
        }
        if (RoomManager.mMine.isSelfMuted == 0) {
            RTCManager.getInstance().getRtcEngine().disableAudio();
            RTCManager.getInstance().getRtcEngine().enableAudio();
        }
    }

    private void onMemberLeave(@NonNull AgoraMember member) {
        if (member.userNo.equals(RoomManager.mMine.userNo)) {
            getBinding().groupBottomView.setVisibility(View.GONE);
            getBinding().groupEmptyPrompt.setVisibility(View.VISIBLE);
            RoomManager.mMine.role = AgoraMember.Role.Listener;
        }
        AgoraMember temp = mRoomSpeakerAdapter.getItemData(member.onSeat);
        if (temp != null) {
            mRoomSpeakerAdapter.dataList.set(member.onSeat, null);
            mRoomSpeakerAdapter.notifyItemChanged(member.onSeat);
        }
        RoomManager.getInstance().onMemberLeave(member);
    }

    private void onMemberJoin(@NonNull AgoraMember member) {
        if (mRoomSpeakerAdapter.getItemData(member.onSeat) == null) {
            mRoomSpeakerAdapter.dataList.set(member.onSeat, member);
            if (member.userNo.equals(RoomManager.getInstance().getMine().userNo)) {
                RoomManager.getInstance().getMine().onSeat = member.onSeat;
                if (member.isMaster) {
                    RoomManager.getInstance().getMine().isMaster = true;
                    RoomManager.getInstance().getMine().role = AgoraMember.Role.Owner;
                } else {
                    RoomManager.getInstance().getMine().role = AgoraMember.Role.Speaker;
                }
                getBinding().groupBottomView.setVisibility(View.VISIBLE);
                getBinding().groupEmptyPrompt.setVisibility(View.GONE);
            }
            mRoomSpeakerAdapter.notifyItemChanged(member.onSeat);
            RoomManager.getInstance().onMemberJoin(member);
        }
    }

    private void showCreatorExitDialog() {
        if (creatorExitDialog == null) {
            creatorExitDialog = new CloseRoomDialog(this);
            creatorExitDialog.setCanceledOnTouchOutside(false);
            creatorExitDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                    setDarkStatusIcon(isBlackDarkStatus());
                    finish();
                }

                @Override
                public void onRightButtonClick() {
                    setDarkStatusIcon(isBlackDarkStatus());
                    finish();

                }
            });
        }
        creatorExitDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RTCManager.getInstance().getRtcEngine().enableInEarMonitoring(false, Constants.EAR_MONITORING_FILTER_NONE);
        roomLivingViewModel.release();
        RoomManager.getInstance().leaveRoom();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showExitDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

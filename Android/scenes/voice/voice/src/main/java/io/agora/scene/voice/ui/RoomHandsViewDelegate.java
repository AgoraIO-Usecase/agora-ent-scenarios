package io.agora.scene.voice.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.util.Map;

import io.agora.scene.voice.model.VoiceRoomLivingViewModel;
import io.agora.scene.voice.service.VoiceBuddyFactory;
import io.agora.scene.voice.ui.common.CommonSheetAlertDialog;
import io.agora.scene.voice.ui.fragment.ChatroomHandsDialog;
import io.agora.voice.baseui.general.callback.OnResourceParseCallback;
import io.agora.voice.baseui.interfaces.IParserSource;
import io.agora.voice.buddy.tool.ToastTools;
import io.agora.scene.voice.R;
import io.agora.scene.voice.ui.widget.primary.ChatPrimaryMenuView;

public class RoomHandsViewDelegate implements IParserSource {
    private FragmentActivity activity;
    private ChatroomHandsDialog dialog;
    private String roomId;
    private ChatPrimaryMenuView chatPrimaryMenuView;
    private String owner;
    private boolean isRequest;

    private VoiceRoomLivingViewModel roomLivingViewModel;

    RoomHandsViewDelegate(FragmentActivity activity, VoiceRoomLivingViewModel roomLivingViewModel, ChatPrimaryMenuView view) {
        this.activity = activity;
        this.roomLivingViewModel = roomLivingViewModel;
        this.chatPrimaryMenuView = view;
        onViewModelObservable();
    }

    private void onViewModelObservable() {
        roomLivingViewModel.cancelMicSeatApplyObservable().observe(activity, result -> {
            parseResource(result, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_cancel_apply_success), Toast.LENGTH_SHORT);
                    chatPrimaryMenuView.setShowHandStatus(false, false);
                    isRequest = false;
                }

                @Override
                public void onError(int code, String message) {
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_cancel_apply_fail), Toast.LENGTH_SHORT);
                }
            });
        });
        roomLivingViewModel.cancelMicSeatApplyObservable().observe(activity, result -> {
            parseResource(result, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_apply_success), Toast.LENGTH_SHORT);
                    chatPrimaryMenuView.setShowHandStatus(false, true);
                    isRequest = true;
                }

                @Override
                public void onError(int code, String message) {
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_apply_fail), Toast.LENGTH_SHORT);
                }
            });
        });
    }

    public static RoomHandsViewDelegate getInstance(FragmentActivity activity, VoiceRoomLivingViewModel roomLivingViewModel, ChatPrimaryMenuView view) {
        return new RoomHandsViewDelegate(activity, roomLivingViewModel, view);
    }

    public void onRoomDetails(String roomId, String owner) {
        this.roomId = roomId;
        this.owner = owner;
        Log.e("onRoomDetails", "owner: " + owner);
        Log.e("onRoomDetails", "getUid: " + VoiceBuddyFactory.get().getVoiceBuddy().userId());
    }

    public boolean isOwner() {
        return VoiceBuddyFactory.get().getVoiceBuddy().equals(owner);
    }

    public void showOwnerHandsDialog() {
        activity.getSupportFragmentManager();
        dialog = (ChatroomHandsDialog) activity.getSupportFragmentManager().findFragmentByTag("room_hands");
        if (dialog == null) {
            dialog = ChatroomHandsDialog.getNewInstance();
        }
        Bundle bundle = new Bundle();
        bundle.putString("roomId", roomId);
        dialog.setArguments(bundle);
        dialog.show(activity.getSupportFragmentManager(), "room_hands");
        chatPrimaryMenuView.setShowHandStatus(false, false);
    }

    public void update(int index) {
        if (dialog != null) {
            dialog.update(index);
        }
    }

    // 用户点击上台
    public void onUserClickOnStage(int micIndex) {
        if (isRequest) {
            ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_submit_sent), Toast.LENGTH_SHORT);
        } else {
            showMemberHandsDialog(micIndex);
        }
    }

    public void resetRequest() {
        isRequest = false;
    }

    public void showMemberHandsDialog(int micIndex) {
        new CommonSheetAlertDialog()
                .contentText(isRequest ? activity.getString(R.string.voice_chatroom_cancel_request_speak) : activity.getString(R.string.voice_chatroom_request_speak))
                .rightText(activity.getString(R.string.voice_room_confirm))
                .leftText(activity.getString(R.string.voice_room_cancel))
                .setOnClickListener(new CommonSheetAlertDialog.OnClickBottomListener() {
                    @Override
                    public void onConfirmClick() {
                        if (isRequest) {
                            roomLivingViewModel.cancelMicSeatApply(VoiceBuddyFactory.get().getVoiceBuddy().chatUid());
                        } else {
                            roomLivingViewModel.startMicSeatApply(-1);
                        }
                    }

                    @Override
                    public void onCancelClick() {
                    }
                })
                .show(activity.getSupportFragmentManager(), "room_hands_apply");
    }

    public void check(Map<String, String> map) {
        if (dialog != null)
            dialog.check(map);
    }

}

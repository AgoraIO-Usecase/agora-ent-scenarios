package io.agora.scene.voice.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import java.util.Map;

import io.agora.scene.voice.ui.common.CommonSheetAlertDialog;
import io.agora.scene.voice.ui.fragment.ChatroomHandsDialog;
import io.agora.voice.buddy.tool.ToastTools;
import io.agora.scene.voice.R;
import io.agora.scene.voice.general.net.ChatroomHttpManager;
import io.agora.scene.voice.general.repositories.ProfileManager;
import io.agora.scene.voice.ui.widget.primary.ChatPrimaryMenuView;
import io.agora.voice.network.tools.VRValueCallBack;

public class RoomHandsViewDelegate {
    private FragmentActivity activity;
    private ChatroomHandsDialog dialog;
    private String roomId;
    private ChatPrimaryMenuView chatPrimaryMenuView;
    private String owner;
    private boolean isRequest;

    RoomHandsViewDelegate(FragmentActivity activity, ChatPrimaryMenuView view) {
        this.activity = activity;
        this.chatPrimaryMenuView = view;
    }

    public static RoomHandsViewDelegate getInstance(FragmentActivity activity, ChatPrimaryMenuView view) {
        return new RoomHandsViewDelegate(activity, view);
    }

    public void onRoomDetails(String roomId, String owner) {
        this.roomId = roomId;
        this.owner = owner;
        Log.e("onRoomDetails", "owner: " + owner);
        Log.e("onRoomDetails", "getUid: " + ProfileManager.getInstance().getProfile().getUid());
    }

    public boolean isOwner() {
        return ProfileManager.getInstance().getProfile().getUid().equals(owner);
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
                            ChatroomHttpManager.getInstance(activity).cancelSubmitMic(roomId, new VRValueCallBack<Boolean>() {
                                @Override
                                public void onSuccess(Boolean var1) {
                                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_cancel_apply_success), Toast.LENGTH_SHORT);
                                    chatPrimaryMenuView.setShowHandStatus(false, false);
                                    isRequest = false;
                                }

                                @Override
                                public void onError(int code, String desc) {
                                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_cancel_apply_fail), Toast.LENGTH_SHORT);
                                }
                            });
                        } else {
                            ChatroomHttpManager.getInstance(activity).submitMic(roomId, micIndex, new VRValueCallBack<Boolean>() {
                                @Override
                                public void onSuccess(Boolean var1) {
                                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_apply_success), Toast.LENGTH_SHORT);
                                    chatPrimaryMenuView.setShowHandStatus(false, true);
                                    isRequest = true;
                                }

                                @Override
                                public void onError(int code, String desc) {
                                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_apply_fail), Toast.LENGTH_SHORT);
                                }
                            });
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

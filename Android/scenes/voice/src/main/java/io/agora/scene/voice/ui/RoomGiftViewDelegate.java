package io.agora.scene.voice.ui;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

import io.agora.scene.base.utils.ThreadManager;
import io.agora.scene.voice.R;
import io.agora.scene.voice.VoiceLogger;
import io.agora.scene.voice.global.VoiceCenter;
import io.agora.scene.voice.imkit.bean.ChatMessageData;
import io.agora.scene.voice.imkit.custorm.CustomMsgHelper;
import io.agora.scene.voice.imkit.custorm.OnMsgCallBack;
import io.agora.scene.voice.model.GiftBean;
import io.agora.scene.voice.ui.widget.gift.ChatroomGiftView;
import io.agora.scene.voice.ui.widget.gift.GiftBottomDialog;
import io.agora.scene.voice.ui.widget.gift.OnSendClickListener;
import io.agora.scene.voice.viewmodel.VoiceRoomLivingViewModel;

public class RoomGiftViewDelegate {
    private FragmentActivity activity;
    private GiftBottomDialog dialog;
    private int time = 2;
    private int Animation_time = 3;
    private TextView send;
    private ChatroomGiftView giftView;
    private String roomId;
    private String owner;
    private SVGAImageView svgaImageView;

    private VoiceRoomLivingViewModel roomLivingViewModel;

    RoomGiftViewDelegate(FragmentActivity activity, VoiceRoomLivingViewModel roomLivingViewModel, ChatroomGiftView giftView, SVGAImageView svgaImageView) {
        this.activity = activity;
        this.roomLivingViewModel = roomLivingViewModel;
        this.giftView = giftView;
        this.svgaImageView = svgaImageView;
    }

    public static RoomGiftViewDelegate getInstance(FragmentActivity activity, VoiceRoomLivingViewModel roomLivingViewModel, ChatroomGiftView giftView, SVGAImageView svgaImageView) {
        return new RoomGiftViewDelegate(activity, roomLivingViewModel, giftView, svgaImageView);
    }

    public void onRoomDetails(String roomId, String owner) {
        this.roomId = roomId;
        this.owner = owner;
        VoiceLogger.d("onRoomDetails", "owner: " + owner);
        VoiceLogger.d("onRoomDetails", "getUid: " + VoiceCenter.getUserId());
    }


    public void showGiftDialog(OnMsgCallBack msgCallBack) {
        if (activity != null) {
            dialog = (GiftBottomDialog) activity.getSupportFragmentManager().findFragmentByTag("live_gift");
            if (dialog == null) {
                dialog = GiftBottomDialog.getNewInstance();
            }
            dialog.show(activity.getSupportFragmentManager(), "live_gift");
            dialog.setOnConfirmClickListener(new OnSendClickListener() {
                @Override
                public void SendGift(View view, Object bean) {
                    dialog.setSendEnable(false);
                    GiftBean giftBean = (GiftBean) bean;
                    onSendGiftSuccess(view,giftBean,msgCallBack);
                }
            });
        }
    }

    private void onSendGiftSuccess(View view, GiftBean giftBean,OnMsgCallBack msgCallBack) {
        VoiceLogger.d("sendGift", "Successfully reported");
        CustomMsgHelper.getInstance().sendGiftMsg(
                VoiceCenter.getNickname(),
                VoiceCenter.getHeadUrl(),
                giftBean.getId(), giftBean.getNum(), giftBean.getPrice(), giftBean.getName(),
                new OnMsgCallBack() {
                    @Override
                    public void onSuccess(ChatMessageData message) {
                        VoiceLogger.d("MenuItemClick", "item_gift_onSuccess");
                        ThreadManager.getInstance().runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                if (activity.isDestroyed()) {
                                    return;
                                }
                                giftView.refresh();
                                if (view instanceof TextView) {
                                    send = ((TextView) view);
                                    send.setText(time + "s");
                                    send.setEnabled(false);
                                    startTask();
                                }
                                if (giftBean.getId().equals("VoiceRoomGift9")) {
                                    showGiftAction();
                                    dialog.dismiss();
                                }
                            }
                        });
                        if (msgCallBack != null) {
                            msgCallBack.onSuccess(message);
                        }
                    }

                    @Override
                    public void onError(String messageId, int code, String error) {
                        super.onError(messageId, code, error);
                        dialog.dismiss();
                        if (msgCallBack != null) {
                            msgCallBack.onError(messageId, code, error);
                        }
                    }
                });
    }

    private Handler handler = new Handler();
    private Runnable task;
    private Runnable showTask;

    private void startTask() {
        handler.postDelayed(task = new Runnable() {
            @Override
            public void run() {
                time--;
                send.setText(time + "s");
                if (time == 0) {
                    stopTask();
                    send.setEnabled(true);
                    send.setText(activity.getString(R.string.voice_chatroom_gift_dialog_send));
                } else {
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private void stopTask() {
        if (task != null) {
            handler.removeCallbacks(task);
            task = null;
            time = 2;
            dialog.setSendEnable(true);
        }
    }

    public void showGiftAction() {
        String name = "animation_of_rocket.svga";
        SVGAParser svgaParser = SVGAParser.Companion.shareParser();
//      svgaParser.setFrameSize(100, 100);
        svgaParser.decodeFromAssets(name, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                svgaImageView.setVideoItem(videoItem);
                svgaImageView.stepToFrame(0, true);
                startAnimationTask();
            }

            @Override
            public void onError() {
            }

        }, null);
    }

    public void startAnimationTask() {
        handler.postDelayed(showTask = new Runnable() {
            @Override
            public void run() {
                Animation_time--;
                VoiceLogger.d("startActionTask", "Animation_time: " + Animation_time);
                if (Animation_time == 0) {
                    stopActionTask();
                    VoiceLogger.d("startActionTask", "isAnimating: " + svgaImageView.isAnimating());
                    if (svgaImageView.isAnimating()) {
                        svgaImageView.stopAnimation(true);
                    }
                } else {
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private void stopActionTask() {
        if (showTask != null) {
            handler.removeCallbacks(showTask);
            showTask = null;
            Animation_time = 3;
        }
    }

}

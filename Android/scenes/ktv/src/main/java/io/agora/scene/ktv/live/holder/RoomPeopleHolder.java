package io.agora.scene.ktv.live.holder;

import android.content.Context;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.card.MaterialCardView;

import io.agora.rtc2.Constants;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.scene.base.GlideApp;
import io.agora.scene.base.api.model.User;
import io.agora.scene.base.bean.MemberMusicModel;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvItemRoomSpeakerBinding;
import io.agora.scene.ktv.manager.RTCManager;
import io.agora.scene.ktv.manager.RoomManager;
import io.agora.scene.ktv.service.VLRoomSeatModel;
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform;

public class RoomPeopleHolder extends BaseRecyclerViewAdapter.BaseViewHolder<KtvItemRoomSpeakerBinding, VLRoomSeatModel> {
    public RoomPeopleHolder(@NonNull KtvItemRoomSpeakerBinding mBinding) {
        super(mBinding);
    }

    @Override
    public void binding(VLRoomSeatModel member, int selectedIndex) {
        mBinding.tvUserName.setText(String.valueOf(getAdapterPosition() + 1));
        mBinding.avatarItemRoomSpeaker.setImageResource(R.mipmap.ktv_ic_seat);
        mBinding.tvZC.setVisibility(View.GONE);
        mBinding.tvRoomOwner.setVisibility(View.GONE);
        mBinding.ivMute.setVisibility(View.GONE);
        if (member == null) {
            mBinding.avatarItemRoomSpeaker.setVisibility(View.VISIBLE);
            if (mBinding.superLayout.getChildAt(0) instanceof CardView) {
                mBinding.superLayout.removeViewAt(0);
            }
            return;
        }
        if (member.isMaster() && getAdapterPosition() == 0) {
            mBinding.tvRoomOwner.setVisibility(View.VISIBLE);
        }
        mBinding.tvUserName.setText(member.getName());
        if (member.isSelfMuted() == 1) {
            mBinding.ivMute.setVisibility(View.VISIBLE);
        } else {
            mBinding.ivMute.setVisibility(View.GONE);
        }
        GlideApp.with(itemView).load(member.getHeadUrl()).error(R.mipmap.userimage)
                .transform(new CenterCropRoundCornerTransform(100)).into(mBinding.avatarItemRoomSpeaker);

        MemberMusicModel mMusicModel = RoomManager.getInstance().getMusicModel();
        if (mMusicModel != null) {
            if (member.getUserNo().equals(mMusicModel.userNo)) {
                mBinding.tvZC.setText("主唱");
                mBinding.tvZC.setVisibility(View.VISIBLE);
            } else if (member.getUserNo().equals(mMusicModel.userId) || member.getUserNo().equals(mMusicModel.chorusNo)) {
                mBinding.tvZC.setText("合唱");
                mBinding.tvZC.setVisibility(View.VISIBLE);
            } else {
                mBinding.tvZC.setVisibility(View.GONE);
            }
        }
        showAvatarOrCameraView(member);
    }

    private void showAvatarOrCameraView(VLRoomSeatModel member) {
        Context mContext = itemView.getContext();
        User mUser = UserManager.getInstance().getUser();
        RtcEngine engine = RTCManager.getInstance().getRtcEngine();
        if (mUser != null) {
            if (member.isVideoMuted() == 0) { // 未开启摄像头 《==》 移除存在的SurfaceView，显示头像
                mBinding.avatarItemRoomSpeaker.setVisibility(View.VISIBLE);
                if (mBinding.superLayout.getChildAt(0) instanceof CardView) {
                    mBinding.superLayout.removeViewAt(0);
                }
            } else { // 开启了摄像头
                mBinding.avatarItemRoomSpeaker.setVisibility(View.INVISIBLE);
                if (mBinding.superLayout.getChildAt(0) instanceof CardView) { // SurfaceView 已存在 《==》 No-OP
                    ((CardView) mBinding.superLayout.getChildAt(0)).removeAllViews();
                    mBinding.superLayout.removeViewAt(0);
                }
//                } else {
                SurfaceView surfaceView = loadRenderView(mContext);
                if (member.getUserNo().equals(UserManager.getInstance().getUser().userNo)) { // 是本人
                    engine.startPreview();
                    engine.setupLocalVideo(new VideoCanvas(surfaceView, Constants.RENDER_MODE_HIDDEN, 0));
                } else {
                    int id = Integer.parseInt(member.getId());
                    RTCManager.getInstance().getRtcEngine().setupRemoteVideo(new VideoCanvas(surfaceView, Constants.RENDER_MODE_HIDDEN, id));
                }
//            }
            }
        }

    }

    @NonNull
    private SurfaceView loadRenderView(@NonNull Context mContext) {
        MaterialCardView cardView = new MaterialCardView(mContext, null, R.attr.materialCardViewStyle);
        cardView.setCardElevation(0);
        cardView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> cardView.setRadius((right - left) / 2f));

        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 0);
        lp.dimensionRatio = "1:1";
        cardView.setLayoutParams(lp);

        SurfaceView surfaceView = new SurfaceView(mContext);
        surfaceView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        cardView.addView(surfaceView);

        mBinding.superLayout.addView(cardView, 0);
        return surfaceView;
    }

}
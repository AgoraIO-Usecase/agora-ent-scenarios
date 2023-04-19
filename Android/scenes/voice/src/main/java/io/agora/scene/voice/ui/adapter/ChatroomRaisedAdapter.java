package io.agora.scene.voice.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import io.agora.scene.voice.model.VoiceMemberModel;
import io.agora.voice.common.ui.adapter.RoomBaseRecyclerViewAdapter;
import io.agora.scene.voice.R;
import io.agora.voice.common.utils.ImageTools;

public class ChatroomRaisedAdapter extends RoomBaseRecyclerViewAdapter<VoiceMemberModel> {
    private onActionListener listener;
    private boolean isAccepted;
    private String selectUid;

    @Override
    public RoomBaseRecyclerViewAdapter.ViewHolder<VoiceMemberModel> getViewHolder(ViewGroup parent, int viewType) {
        return new raisedViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.voice_item_hands_raised, parent, false));
    }


    public class raisedViewHolder extends ViewHolder<VoiceMemberModel> {
        private ShapeableImageView avatar;
        private MaterialTextView name;
        private MaterialTextView action;

        @Override
        public void initView(View itemView) {
            super.initView(itemView);
            avatar = itemView.findViewById(R.id.ivAudienceAvatar);
            name = itemView.findViewById(R.id.mtAudienceUsername);
            action = itemView.findViewById(R.id.mtAudienceAction);
        }

        public raisedViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void setData(VoiceMemberModel item, int position) {
            ImageTools.loadImage(avatar, item.getPortrait());
            name.setText(item.getNickName());
            action.setText(mContext.getString(R.string.voice_room_accept));
            action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null)
                        listener.onItemActionClick(view, item.getMicIndex(), item.getChatUid());
                }
            });
            action.setText(mContext.getString(R.string.voice_room_accept));
            if (item.getChatUid().equals(selectUid)) {
                if (isAccepted) {
                    action.setText(mContext.getString(R.string.voice_room_accepted));
                    action.setBackgroundResource(R.drawable.voice_bg_rect_radius20_grey);
                    action.setEnabled(false);
                } else {
                    action.setText(mContext.getString(R.string.voice_room_accept));
                    action.setBackgroundResource(R.drawable.voice_bg_rect_radius20_gradient_blue);
                    action.setEnabled(true);
                }
            }else {

            }
        }
    }

    public void setAccepted(String uid, boolean isAccepted) {
        this.isAccepted = isAccepted;
        this.selectUid = uid;
//        notifyDataSetChanged();
    }

    public void setOnActionListener(onActionListener listener) {
        this.listener = listener;
    }

    public interface onActionListener {
        void onItemActionClick(View view, int index, String uid);
    }
}

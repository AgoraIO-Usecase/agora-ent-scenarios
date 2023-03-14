package io.agora.scene.voice.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.HashMap;
import java.util.Map;

import io.agora.scene.voice.R;
import io.agora.scene.voice.model.VoiceMemberModel;
import io.agora.voice.common.ui.adapter.RoomBaseRecyclerViewAdapter;
import io.agora.voice.common.utils.ImageTools;

public class ChatroomInviteAdapter extends RoomBaseRecyclerViewAdapter<VoiceMemberModel> {
    private onActionListener listener;
    private Map<String,Boolean> checkMap = new HashMap<>();

    @Override
    public RoomBaseRecyclerViewAdapter.ViewHolder<VoiceMemberModel> getViewHolder(ViewGroup parent, int viewType) {
        return new inviteViewHolder(LayoutInflater.from (parent.getContext()).inflate (R.layout.voice_item_hands_raised, parent, false));
    }

    public class inviteViewHolder extends ViewHolder<VoiceMemberModel> {
        private ShapeableImageView avatar;
        private MaterialTextView name;
        private MaterialTextView action;

        public inviteViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void setData(VoiceMemberModel item, int position) {
            ImageTools.loadImage(avatar, item.getPortrait());
            name.setText(item.getNickName());
            action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null)
                        listener.onItemActionClick(view,position,item.getChatUid());
                }
            });
            if (checkMap.containsKey(item.getChatUid())){
                action.setText(mContext.getString(R.string.voice_room_invited));
                action.setBackgroundResource(R.drawable.voice_bg_rect_radius20_grey);
                action.setEnabled(false);
            }else {
                action.setText(mContext.getString(R.string.voice_room_invite));
                action.setBackgroundResource(R.drawable.voice_bg_rect_radius20_gradient_blue);
                action.setEnabled(true);
            }
        }

        @Override
        public void initView(View itemView) {
            super.initView(itemView);
            avatar = itemView.findViewById(R.id.ivAudienceAvatar);
            name = itemView.findViewById(R.id.mtAudienceUsername);
            action = itemView.findViewById(R.id.mtAudienceAction);
        }
    }

    public void setOnActionListener(onActionListener listener){
        this.listener = listener;
    }

    public interface onActionListener{
        void onItemActionClick(View view,int position,String uid);
    }

    public void setInvited(Map<String,Boolean> inviteData){
        checkMap.putAll(inviteData);
        notifyDataSetChanged();
    }

    public void removeInvited(String chatUid){
        checkMap.remove(chatUid);
        notifyDataSetChanged();
    }

}

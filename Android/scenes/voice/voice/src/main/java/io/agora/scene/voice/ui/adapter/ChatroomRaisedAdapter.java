package io.agora.scene.voice.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import io.agora.voice.baseui.adapter.RoomBaseRecyclerViewAdapter;
import io.agora.scene.voice.R;
import io.agora.voice.network.tools.bean.VRMicListBean;

public class ChatroomRaisedAdapter extends RoomBaseRecyclerViewAdapter<VRMicListBean.ApplyListBean> {
    private onActionListener listener;
    private boolean isAccepted;
    private String selectUid;

    @Override
    public RoomBaseRecyclerViewAdapter.ViewHolder<VRMicListBean.ApplyListBean> getViewHolder(ViewGroup parent, int viewType) {
        return new raisedViewHolder(LayoutInflater.from (parent.getContext()).inflate (R.layout.voice_item_hands_raised, parent, false));
    }


    public class raisedViewHolder extends ViewHolder<VRMicListBean.ApplyListBean> {
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
        public void setData(VRMicListBean.ApplyListBean item, int position) {
            int resId = 0;
                try {
                    resId = mContext.getResources().getIdentifier(item.getMember().getPortrait(), "drawable", mContext.getPackageName());
                }catch (Exception e){
                    Log.e("getResources()", e.getMessage());
                }
                if (resId != 0){
                    avatar.setImageResource(resId);
                }
                name.setText(item.getMember().getName());
                action.setText(mContext.getString(R.string.voice_room_accept));
                action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (listener != null)
                            listener.onItemActionClick(view,item.getMic_index(),item.getMember().getUid());
                    }
                });
                action.setText(mContext.getString(R.string.voice_room_accept));
                if (item.getMember().getUid().equals(selectUid)){
                    if (isAccepted){
                        action.setText(mContext.getString(R.string.voice_room_accepted));
                        action.setBackgroundResource(R.drawable.voice_bg_rect_radius20_grey);
                        action.setEnabled(false);
                    }else {
                        action.setText(mContext.getString(R.string.voice_room_accept));
                        action.setBackgroundResource(R.drawable.voice_bg_rect_radius20_gradient_blue);
                        action.setEnabled(true);
                    }
                }
            }
        }

    public void setAccepted(String uid,boolean isAccepted){
        this.isAccepted = isAccepted;
        this.selectUid = uid;
        notifyDataSetChanged();
    }

    public void setOnActionListener(onActionListener listener){
        this.listener = listener;
    }

    public interface onActionListener{
        void onItemActionClick(View view,int index,String uid);
    }
}

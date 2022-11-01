package io.agora.scene.ktv.create.holder;

import android.view.View;

import androidx.annotation.NonNull;

import io.agora.scene.base.GlideApp;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.data.model.AgoraRoom;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.ItemRoomListBinding;
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform;

public class RoomHolder extends BaseRecyclerViewAdapter.BaseViewHolder<ItemRoomListBinding, AgoraRoom> {

    public RoomHolder(@NonNull ItemRoomListBinding mBinding) {
        super(mBinding);
    }

    @Override
    public void binding(AgoraRoom data, int selectedIndex) {
        if (data != null) {
            GlideApp.with(mBinding.ivRoomCover.getContext()).load(data.getCoverRes())
                    .transform(new CenterCropRoundCornerTransform(40)).into(mBinding.ivRoomCover);
            mBinding.tvRoomName.setText(data.name);
            mBinding.tvPersonNum.setText(String.format("%d%s", data.roomPeopleNum, itemView.getContext().getString(R.string.people)));
            if (data.isPrivate == 1){
                mBinding.ivLock.setVisibility(View.VISIBLE);
            } else{
                mBinding.ivLock.setVisibility(View.GONE);
            }
        }
    }
}
package io.agora.scene.ktv.singbattle.create.holder;

import android.view.View;

import androidx.annotation.NonNull;

import io.agora.scene.base.GlideApp;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.ktv.singbattle.R;
import io.agora.scene.ktv.singbattle.databinding.ItemRoomListBinding;
import io.agora.scene.ktv.singbattle.service.RoomListModel;
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform;

public class RoomHolder extends BaseRecyclerViewAdapter.BaseViewHolder<ItemRoomListBinding, RoomListModel> {

    public RoomHolder(@NonNull ItemRoomListBinding mBinding) {
        super(mBinding);
    }

    @Override
    public void binding(RoomListModel data, int selectedIndex) {
        if (data != null) {
            GlideApp.with(mBinding.ivRoomCover.getContext()).load(data.getCoverRes())
                    .transform(new CenterCropRoundCornerTransform(40)).into(mBinding.ivRoomCover);
            mBinding.tvRoomName.setText(data.getName());
            mBinding.tvPersonNum.setText(String.format("%d%s", data.getRoomPeopleNum(), itemView.getContext().getString(R.string.ktv_people)));
            if (data.isPrivate()){
                mBinding.ivLock.setVisibility(View.VISIBLE);
            } else{
                mBinding.ivLock.setVisibility(View.GONE);
            }
        }
    }
}
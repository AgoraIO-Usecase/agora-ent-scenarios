package io.agora.scene.grandchorus.ui.holder

import android.view.View
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseRecyclerViewAdapter.BaseViewHolder
import io.agora.scene.grandchorus.R
import io.agora.scene.grandchorus.databinding.GrandchorusItemRoomListBinding
import io.agora.scene.grandchorus.service.RoomListModel
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform

class RoomHolder constructor(mBinding: GrandchorusItemRoomListBinding) :
    BaseViewHolder<GrandchorusItemRoomListBinding, RoomListModel?>(mBinding) {
    override fun binding(data: RoomListModel?, selectedIndex: Int) {
        if (data != null) {
            GlideApp.with(mBinding.ivRoomCover.context).load(data.getCoverRes())
                .transform(CenterCropRoundCornerTransform(40)).into(mBinding.ivRoomCover)
            mBinding.tvRoomName.text = data.name
            mBinding.tvPersonNum.text =
                String.format("%d%s", data.roomPeopleNum, itemView.context.getString(R.string.grandchorus_people))
            if (data.isPrivate) {
                mBinding.ivLock.visibility = View.VISIBLE
            } else {
                mBinding.ivLock.visibility = View.GONE
            }
        }
    }
}
package io.agora.scene.cantata.ui.holder

import android.view.View
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseRecyclerViewAdapter.BaseViewHolder
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataItemRoomListBinding
import io.agora.scene.cantata.service.RoomListModel
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform

class RoomHolder constructor(mBinding: CantataItemRoomListBinding) :
    BaseViewHolder<CantataItemRoomListBinding, RoomListModel?>(mBinding) {
    override fun binding(data: RoomListModel?, selectedIndex: Int) {
        if (data != null) {
            GlideApp.with(mBinding.ivRoomCover.context).load(data.getCoverRes())
                .transform(CenterCropRoundCornerTransform(40)).into(mBinding.ivRoomCover)
            mBinding.tvRoomName.text = data.name
            mBinding.tvPersonNum.text =
                String.format("%d%s", data.roomPeopleNum, itemView.context.getString(R.string.cantata_people))
            if (data.isPrivate) {
                mBinding.ivLock.visibility = View.VISIBLE
            } else {
                mBinding.ivLock.visibility = View.GONE
            }
        }
    }
}
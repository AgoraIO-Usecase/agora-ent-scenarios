package io.agora.scene.cantata.ui.dialog

import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.cantata.databinding.CantataDialogSingingUserBinding
import io.agora.scene.cantata.databinding.CantataItemSingingUserBinding
import io.agora.scene.cantata.service.RoomSeatModel
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder

class ChorusSingerDialog : BaseBottomSheetDialogFragment<CantataDialogSingingUserBinding>() {

    companion object {
        const val TAG = "ChorusSingerDialog"
    }

    private val mAdapter: BindingSingleAdapter<RoomSeatModel, CantataItemSingingUserBinding> =
        object : BindingSingleAdapter<RoomSeatModel, CantataItemSingingUserBinding>() {
            override fun onBindViewHolder(holder: BindingViewHolder<CantataItemSingingUserBinding>, position: Int) {
                val item: RoomSeatModel = getItem(position) ?: return
                val binding: CantataItemSingingUserBinding = holder.binding

            }
        }
}
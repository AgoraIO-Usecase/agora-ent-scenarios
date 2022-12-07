package io.agora.scene.show.widget.pk

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.agora.scene.show.databinding.ShowLivePkDialogBinding
import io.agora.scene.show.service.ShowRoomDetailModel

class LivePKDialog : BottomSheetDialogFragment() {
    private var mBinding : ShowLivePkDialogBinding? = null
    private val binding get() = mBinding!!
    private lateinit var pkDialogListener : OnPKDialogActionListener;
    private val pkFragment : LivePKRequestMessageFragment = LivePKRequestMessageFragment()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = ShowLivePkDialogBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置背景透明
        WindowCompat.setDecorFitsSystemWindows(requireDialog().window!!, false)
        requireDialog().setOnShowListener { dialog: DialogInterface? ->
            (view.parent as ViewGroup).setBackgroundColor(
                Color.TRANSPARENT
            )
        }
        ViewCompat.setOnApplyWindowInsetsListener(
            requireDialog().window!!.decorView
        ) { v: View?, insets: WindowInsetsCompat ->
            val inset =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.pager.setPadding(0, 0, 0, inset.bottom)
            WindowInsetsCompat.CONSUMED
        }

        binding.pager.getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER

        pkFragment.setListener(object : LivePKRequestMessageFragment.Listener {
            override fun onAcceptMicSeatItemChosen(roomItem: ShowRoomDetailModel) {
                pkDialogListener.onInviteButtonChosen(this@LivePKDialog, roomItem)
            }

            override fun onRequestRefreshing() {
                pkDialogListener.onRequestMessageRefreshing(this@LivePKDialog)
            }
        })

        binding.pager.isSaveEnabled = false
        binding.pager.adapter =
            object : FragmentStateAdapter(childFragmentManager, viewLifecycleOwner.lifecycle) {
                override fun getItemCount(): Int {
                    return 1
                }

                override fun createFragment(position: Int): Fragment {
                    return pkFragment
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    /**
     * 接受连麦-更新连麦Dialog
     */
    fun setLinkDialogActionListener(listener : OnPKDialogActionListener) {
        pkDialogListener = listener
    }

    /**
     * 设置连麦申请列表
     */
    fun setOnlineBoardcasterList(roomItem : List<ShowRoomDetailModel>) {
        pkFragment.setOnlineBoardcasterList(roomItem)
    }

    /**
     * pk-更新item选中状态
     */
    fun setPKInvitationItemStatus(roomItem: ShowRoomDetailModel, isInvited: Boolean) {
        pkFragment.setPKInvitationItemStatus(roomItem, isInvited)
    }
}
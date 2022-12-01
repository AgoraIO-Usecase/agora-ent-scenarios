package io.agora.scene.show.widget.pk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import io.agora.scene.show.databinding.ShowLiveDetailVideoPkBinding
import io.agora.scene.show.service.ShowMicSeatApply
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.scene.show.widget.UserItem

class LivePKDialog : AppCompatDialogFragment() {
    private val mBinding by lazy { ShowLiveDetailVideoPkBinding.inflate(LayoutInflater.from(context)) }
    private val mService by lazy { ShowServiceProtocol.getImplInstance() }

    private lateinit var pkDialogListener : OnPKDialogActionListener;
    private val pkFragment : LivePKRequestMessageFragment = LivePKRequestMessageFragment()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(
            requireDialog().window!!.decorView
        ) { v: View?, insets: WindowInsetsCompat ->
            val inset =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            mBinding.pager.setPadding(0, 0, 0, inset.bottom)
            WindowInsetsCompat.CONSUMED
        }
        mBinding.rBtnRequestMessage.setChecked(true)
        mBinding.pager.getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER

        pkFragment.setListener(object : LivePKRequestMessageFragment.Listener {
            override fun onAcceptMicSeatItemChosen(userItem: UserItem) {
                if (pkDialogListener != null) {
                    pkDialogListener.onInviteButtonChosen(this@LivePKDialog, userItem)
                }
            }

            override fun onRequestRefreshing(tagIndex: Int) {
                if (pkDialogListener != null) {
                    pkDialogListener.onRequestMessageRefreshing(this@LivePKDialog, tagIndex)
                }
            }
        })

        setChosenItemCount(0)
    }

    override fun onStart() {
        super.onStart()
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
    fun setOnlineBoardcasterList(userList : List<UserItem>) {
        pkFragment.setOnlineBoardcasterList(userList)
    }

    /**
     * pk-更新item选中状态
     */
    fun setPKInvitationItemStatus(userItem: UserItem, isInvited: Boolean) {
        pkFragment.setPKInvitationItemStatus(userItem, isInvited)
    }

    private fun setChosenItemCount(count: Int) {
        var count = count
        if (mBinding == null) {
            return
        }
        if (count > 0) {
            mBinding.rBtnRequestMessage.setVisibility(View.VISIBLE)
            if (count > 99) {
                count = 99
            }
            mBinding.rBtnRequestMessage.setText(count.toString())
        } else {
            mBinding.rBtnRequestMessage.setVisibility(View.GONE)
        }
    }
}
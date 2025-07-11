package io.agora.scene.show.widget.pk

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
import io.agora.scene.show.service.ShowInteractionInfo

class LivePKDialog : BottomSheetDialogFragment() {
    private var mBinding : ShowLivePkDialogBinding? = null
    private val binding get() = mBinding!!
    private lateinit var pkDialogListener : OnPKDialogActionListener
    private val pkFragment : LivePKRequestMessageFragment = LivePKRequestMessageFragment()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = ShowLivePkDialogBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set background transparent
        WindowCompat.setDecorFitsSystemWindows(requireDialog().window!!, false)
        requireDialog().setOnShowListener {
            (view.parent as ViewGroup).setBackgroundColor(
                Color.TRANSPARENT
            )
        }
        ViewCompat.setOnApplyWindowInsetsListener(
            requireDialog().window!!.decorView
        ) { _: View?, insets: WindowInsetsCompat ->
            val inset =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.pager.setPadding(0, 0, 0, inset.bottom)
            WindowInsetsCompat.CONSUMED
        }

        binding.pager.getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER

        pkFragment.setListener(object : LivePKRequestMessageFragment.Listener {
            override fun onAcceptMicSeatItemChosen(view: View, roomItem: LiveRoomConfig) {
                pkDialogListener.onInviteButtonChosen(this@LivePKDialog, view, roomItem)
            }

            override fun onRequestRefreshing() {
                pkDialogListener.onRequestMessageRefreshing(this@LivePKDialog)
            }

            override fun onStopPKingChosen() {
                pkDialogListener.onStopPKingChosen(this@LivePKDialog)
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
     * Accept linking - Update PK Dialog
     */
    fun setPKDialogActionListener(listener : OnPKDialogActionListener) {
        pkDialogListener = listener
    }

    /**
     * Set PK request list
     */
    fun setOnlineBroadcasterList(interactionInfo: ShowInteractionInfo?,
                                 roomList: List<LiveRoomConfig>) {
        pkFragment.setOnlineBroadcasterList(interactionInfo, roomList)
    }

    /**
     * PK - Update item selection status
     */
    fun setPKInvitationItemStatus(userName: String, status: Int?) {
        pkFragment.setPKInvitationItemStatus(userName, status)
    }
}
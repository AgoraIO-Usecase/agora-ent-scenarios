package io.agora.scene.joy.live.fragmentdialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.joy.R
import io.agora.scene.joy.databinding.JoyDialogChooseGameLayoutBinding
import io.agora.scene.joy.databinding.JoyItemGameChooseLayoutBinding
import io.agora.scene.joy.service.api.JoyGameListResult

class JoyChooseGameDialog : BaseBottomSheetDialogFragment<JoyDialogChooseGameLayoutBinding>() {

    companion object {
        const val TAG = "Joy_JoyChooseGameDialog"
        const val Key_Games = "key_games"
    }

    var mSelectedCompletion: ((game: JoyGameListResult) -> Unit)? = null

    // 点击界面外透传点击位置
    var mTouchEventCompletion: ((x: Int, y: Int) -> Unit)? = null

    private val mGamList by lazy {
        arguments?.getSerializable(Key_Games) as List<JoyGameListResult>
    }

    private val mChooseGameAdapter: JoyChooseGameAdapter by lazy {
        JoyChooseGameAdapter(mGamList, 0)
    }

    val mSelectGame: JoyGameListResult
        get() = mGamList[mChooseGameAdapter.selectedIndex]

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : BottomSheetDialog(requireContext(), theme) {
            override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
                // 检查触摸事件位置是否在 BottomSheetDialogFragment 视图之外
                if (ev.action == MotionEvent.ACTION_UP) {
                    val dialogView = findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

                    dialogView?.let {
                        val location = IntArray(2)
                        it.getLocationOnScreen(location)
                        val dialogLeft = location[0]
                        val dialogTop = location[1]
                        val dialogRight = dialogLeft + it.width
                        val dialogBottom = dialogTop + it.height

                        val x = ev.rawX
                        val y = ev.rawY

                        // 如果触摸事件不在对话框内，返回 false 以允许事件向下传递
                        if (x < dialogLeft || x > dialogRight || y < dialogTop || y > dialogBottom) {
                            mTouchEventCompletion?.invoke(x.toInt(), y.toInt())
                            Log.d(TAG, "${ev.action} x:$x y:$y")
                            return false
                        }
                    }
                }

                // 其他情况，正常处理触摸事件
                return super.dispatchTouchEvent(ev)
            }

            override fun onTouchEvent(event: MotionEvent): Boolean {
                return super.onTouchEvent(event)
            }
        }.apply {
            setCancelable(false)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            it.setCancelable(false)
            it.setCanceledOnTouchOutside(false)
            val bottomSheet =
                (it as BottomSheetDialog).findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
                behavior.setHideable(false)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEnableConfirm(true)
        mBinding.btnConfirm.setOnClickListener {
            setEnableConfirm(false)
            val game = mGamList[mChooseGameAdapter.selectedIndex]
            mSelectedCompletion?.invoke(game)

        }
        mBinding.rvGame.adapter = mChooseGameAdapter
    }

    fun setEnableConfirm(isEnable: Boolean) {
        mBinding.btnConfirm.isEnabled = isEnable
        mBinding.btnConfirm.alpha = if (isEnable) 1.0f else 0.5f
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }

    }

}

private class JoyChooseGameAdapter constructor(
    var mList: List<JoyGameListResult>,
    var selectedIndex: Int,
) : RecyclerView.Adapter<JoyChooseGameAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: JoyItemGameChooseLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            JoyItemGameChooseLayoutBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    fun setDataList(list: List<JoyGameListResult>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    // 将数据绑定到视图项
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mList[position]
        holder.binding.ivGame.setImageResource(R.mipmap.joy_img_room_item_bg_1)
        holder.binding.tvGameName.text = data.name
        val context = holder.binding.root.context
        if (selectedIndex == position) {
            holder.binding.tvGameName.setTextColor(
                ResourcesCompat.getColor(context.resources, R.color.white, null)
            )
            holder.binding.layoutGame.setBackgroundResource(R.drawable.joy_bg_item_choose_game)
        } else {
            holder.binding.tvGameName.setTextColor(
                ResourcesCompat.getColor(context.resources, R.color.def_text_color_040, null)
            )
            holder.binding.layoutGame.setBackgroundResource(R.color.white)
        }
        GlideApp.with(holder.binding.ivGame)
            .load(data.thumbnail)
            .error(R.drawable.joy_banner_pkzb)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(holder.binding.ivGame)
        holder.itemView.setOnClickListener {
            selectedIndex = position
            notifyDataSetChanged()
        }
    }
}
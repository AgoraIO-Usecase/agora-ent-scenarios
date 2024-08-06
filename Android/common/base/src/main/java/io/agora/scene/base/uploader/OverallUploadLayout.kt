package io.agora.scene.base.uploader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import io.agora.scene.base.R
import io.agora.scene.base.databinding.ViewOverallUploadBinding
import io.agora.scene.base.utils.ToastUtils

enum class UploadStatus {
    Upload_loading,
    Upload_Complete,
    Upload_Failed,
}

class OverallUploadLayout : ConstraintLayout {

    private lateinit var binding: ViewOverallUploadBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context, attrs, defStyleAttr, defStyleRes
    ) {
        init(context)
    }

    private var uploadStatus: UploadStatus = UploadStatus.Upload_loading

    private var onRepeatUploadListener: (() -> Unit)? = null

    fun setOnRepeatUploadListener(listener: () -> Unit) {
        this.onRepeatUploadListener = listener
    }

    private fun init(context: Context) {
        val root = View.inflate(context, R.layout.view_overall_upload, this)
        binding = ViewOverallUploadBinding.bind(root)

        binding.btnCancel.setOnClickListener {
            OverallLayoutController.hide()
        }
        binding.btnUploadRepeat.setOnClickListener {
            onRepeatUploadListener?.invoke()
        }
        binding.btnClose.setOnClickListener {
            if (uploadStatus == UploadStatus.Upload_Complete) {
                OverallLayoutController.hide()
            }
        }
        binding.tvTaskUuid.setOnClickListener {
            //获取剪切板管理器
            val cm: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            //设置内容到剪切板
            cm.setPrimaryClip(ClipData.newPlainText(null, binding.tvTaskUuid.text?.toString()))
            ToastUtils.showToast("复制成功")
        }
    }

    fun uploadStatus(uploadStatus: UploadStatus, uuid: String) {
        this.uploadStatus = uploadStatus
        when (uploadStatus) {
            UploadStatus.Upload_loading -> {
                binding.ivUploading.setImageResource(R.drawable.icon_upload_loading)
                binding.tvContent.setText(R.string.comm_upload_logging)
                binding.tvTaskUuid.visibility = View.INVISIBLE
                binding.btnClose.visibility = View.VISIBLE
                binding.btnUploadRepeat.visibility = View.INVISIBLE
                binding.btnCancel.visibility = View.INVISIBLE
            }

            UploadStatus.Upload_Complete -> {
                binding.ivUploading.setImageResource(R.drawable.icon_upload_success)
                binding.tvContent.setText(R.string.comm_upload_logging_complete)
                binding.tvTaskUuid.text = uuid
                binding.tvTaskUuid.visibility = View.VISIBLE
                binding.btnClose.visibility = View.VISIBLE
                binding.btnUploadRepeat.visibility = View.INVISIBLE
                binding.btnCancel.visibility = View.INVISIBLE
            }

            UploadStatus.Upload_Failed -> {
                binding.ivUploading.setImageResource(R.drawable.icon_upload_error)
                binding.tvContent.setText(R.string.comm_upload_logging_error)
                binding.tvTaskUuid.visibility = View.INVISIBLE
                binding.btnClose.visibility = View.INVISIBLE
                binding.btnUploadRepeat.visibility = View.VISIBLE
                binding.btnCancel.visibility = View.VISIBLE
            }
        }
    }
}

class ItemViewTouchListener(val wl: WindowManager.LayoutParams, val windowManager: WindowManager) :
    View.OnTouchListener {
    private var x = 0
    private var y = 0
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                x = motionEvent.rawX.toInt()
                y = motionEvent.rawY.toInt()

            }

            MotionEvent.ACTION_MOVE -> {
                val nowX = motionEvent.rawX.toInt()
                val nowY = motionEvent.rawY.toInt()
                val movedX = nowX - x
                val movedY = nowY - y
                x = nowX
                y = nowY
                wl.apply {
                    x += movedX
                    y += movedY
                }
                Log.d("FloatMonkService", "x:$x, y: $y")
                //更新悬浮球控件位置
                windowManager?.updateViewLayout(view, wl)
            }

            else -> {

            }
        }
        return false
    }
}
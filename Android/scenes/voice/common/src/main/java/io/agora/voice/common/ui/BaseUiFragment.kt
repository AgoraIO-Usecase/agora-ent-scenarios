package io.agora.voice.common.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import io.agora.voice.common.R

abstract class BaseUiFragment<B : ViewBinding> : Fragment(),
    IParserSource {

    var binding: B? = null

    private var loadingDialog: AlertDialog? = null

    open fun showLoading(cancelable: Boolean) {
        if (loadingDialog == null) {
            loadingDialog = AlertDialog.Builder(requireActivity()).setView(R.layout.voice_view_base_loading).create().apply {
                // 背景修改成透明
                window?.decorView?.setBackgroundColor(Color.TRANSPARENT)
            }
        }
        loadingDialog?.setCancelable(cancelable)
        loadingDialog?.show()
    }

    open fun dismissLoading() {
        loadingDialog?.dismiss()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = getViewBinding(inflater, container)
        return this.binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    val parentActivity: BaseUiActivity<*>
        get() = requireActivity() as BaseUiActivity<*>

    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): B?
}
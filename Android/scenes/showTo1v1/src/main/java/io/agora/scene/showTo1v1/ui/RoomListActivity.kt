package io.agora.scene.showTo1v1.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.showTo1v1.databinding.ShowTo1v1RoomListActivityBinding

class RoomListActivity : BaseViewBindingActivity<ShowTo1v1RoomListActivityBinding>() {

    override fun getViewBinding(inflater: LayoutInflater): ShowTo1v1RoomListActivityBinding {
        return ShowTo1v1RoomListActivityBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnApplyWindowInsetsListener(binding.emptyInclude.emptyRoot)
    }


    override fun setOnApplyWindowInsetsListener(view: View?) {
        ViewCompat.setOnApplyWindowInsetsListener(view!!) { v: View?, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view!!.setPaddingRelative(0, 0, 0, inset.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}
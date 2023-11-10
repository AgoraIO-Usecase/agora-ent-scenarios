package io.agora.voice.common.utils

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.voice.common.R

/**
 * @author create by zhangwei03
 */
object ImageTools {

    @JvmStatic
    fun loadImage(view: ImageView, url: String?) {
        Glide.with(view)
            .load(url)
            .error(R.drawable.vocie_user_image)
            .apply(RequestOptions.circleCropTransform())
            .into(view)
    }

    @JvmStatic
    fun loadImage(view: ImageView, @DrawableRes res: Int) {
        Glide.with(view)
            .load(res)
            .error(R.drawable.vocie_user_image)
            .apply(RequestOptions.circleCropTransform())
            .into(view)
    }
}
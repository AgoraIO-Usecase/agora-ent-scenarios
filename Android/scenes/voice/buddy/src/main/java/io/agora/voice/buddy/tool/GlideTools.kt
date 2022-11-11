package io.agora.voice.buddy.tool

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.load.engine.GlideException
import io.agora.voice.buddy.R
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.target.Target
import java.io.File

/**
 * Glide工具类
 */
object GlideTools {

    /*** 占位图  */
    private val placeholderImage = R.drawable.voice_ic_default_image_32

    /*** 错误图  */
    private val errorImage = R.drawable.voice_ic_broken_image_32

    /**隐私合规处理*/
    private fun convertUrl(url: String): GlideUrl {
        return GlideUrl(url, LazyHeaders.Builder().addHeader("User-Agent", "android").build())
    }

    /**加载图片(默认)*/
    fun loadImage(context: Context, url: String, imageView: ImageView, skipMemoryCache: Boolean = false) {
        val options = RequestOptions()
            .placeholder(placeholderImage) //占位图
            .error(errorImage) //错误图
            .skipMemoryCache(skipMemoryCache)
        Glide.with(context)
            .load(convertUrl(url))
            .apply(options)
            .into(imageView)
    }

    /**加载gif */
    private fun loadGif(context: Context, url: String, imageView: ImageView) {
        val options = RequestOptions()
            .placeholder(placeholderImage) //占位图
            .error(errorImage) //错误图
        Glide.with(context)
            .load(url)
            .apply(options)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(imageView)
    }

    /**下载图片*/
    fun downloadImage(context: Context, url: String?, requestListener: RequestListener<File>?) {
        Glide.with(context)
            .downloadOnly()
            .load(url)
            .addListener(requestListener).preload()
    }
}
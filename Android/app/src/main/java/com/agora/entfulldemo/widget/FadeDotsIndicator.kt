package com.agora.entfulldemo.widget

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.agora.entfulldemo.R

class FadeDotsIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val dots = ArrayList<ImageView>()
    private var dotsClickable: Boolean = false
    private var currentDot: Int = -1

    private val dotsSize: Float
    private val dotsSpacing: Float
    private val dotsCornerRadius: Float

    private val linearLayout = LinearLayout(context)

    init {
        linearLayout.orientation = LinearLayout.HORIZONTAL
        addView(
            linearLayout,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dotsSize = 7.dp
        dotsSpacing = 9.dp
        dotsCornerRadius = dotsSize / 2
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        refreshDots()
    }

    private fun refreshDotsCount() {
        pager?.let {
            if (dots.size < it.count) {
                addDots(it.count - dots.size)
            } else if (dots.size > it.count) {
                removeDots(dots.size - it.count)
            }
        }
    }

    private fun addDots(count: Int) {
        for (i in 0 until count) {
            addDot(i)
        }
    }

    private fun removeDots(count: Int) {
        for (i in 0 until count) {
            removeDot(i)
        }
    }

    fun addDot(index: Int) {
        val dot = LayoutInflater.from(context).inflate(R.layout.app_view_fade_dots_indicator, this, false)
        dot.layoutDirection = View.LAYOUT_DIRECTION_LTR
        val strokeView = dot.findViewById<ImageView>(R.id.dot_stroke)
        setUpDotCornerRadiusView(strokeView)

        val imageView = dot.findViewById<ImageView>(R.id.dot)
        setUpDotCornerRadiusView(imageView)
        setUpDotAlpha(index, imageView)

        dot.setOnClickListener {
            pager?.let { pager ->
                if (dotsClickable && index < pager.count) {
                    pager.setCurrentItem(index, true)
                }
            }

        }

        dots.add(imageView)
        linearLayout.addView(dot)
    }

    private fun setUpDotCornerRadiusView(imageView: ImageView) {
        val params = imageView.layoutParams as RelativeLayout.LayoutParams
        params.height = dotsSize.toInt()
        params.width = params.height
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
        params.setMargins((dotsSpacing / 2).toInt(), 0, (dotsSpacing / 2).toInt(), 0)

        val background = imageView.background as GradientDrawable
        background.cornerRadius = dotsSize / 2
    }

    private fun setUpDotAlpha(index: Int, imageView: ImageView) {
        pager?.let {
            if (it.currentItem == index) {
                currentDot = it.currentItem
                imageView.alpha = 1f
            } else {
                imageView.alpha = 0f
            }
        }
    }

    fun removeDot(index: Int) {
        linearLayout.removeViewAt(childCount - 1)
        dots.removeAt(dots.size - 1)
    }

    fun refreshDots() {
        pager?.let {
            post {
                // Check if we need to refresh the dots count
                refreshDotsCount()
                refreshDotsColors()
                refreshOnPageChangedListener()
            }
        }
    }

    private fun refreshOnPageChangedListener() {
        pager?.let {
            if (it.isNotEmpty) {
                it.removeOnPageChangeListener()
                it.addOnPageChangeListener()
            }
        }
    }

    private fun refreshDotsColors() {
        for (i in dots.indices) {
            setUpDotAlpha(i, dots[i])
        }
    }

    var pager: Pager? = null

    interface Pager {
        val isNotEmpty: Boolean
        val currentItem: Int
        val isEmpty: Boolean
        val count: Int
        fun setCurrentItem(item: Int, smoothScroll: Boolean)
        fun removeOnPageChangeListener()
        fun addOnPageChangeListener()
    }

    // PUBLIC METHODS
    fun setViewPager2(viewPager2: ViewPager2) {
        viewPager2.adapter?.let { adapter ->
            adapter.registerAdapterDataObserver(object :
                RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    super.onChanged()
                    refreshDots()
                }
            })

            pager = object : Pager {
                var onPageChangeCallback: ViewPager2.OnPageChangeCallback? = null

                override val isNotEmpty: Boolean
                    get() = !(viewPager2.isEmpty)
                override val currentItem: Int
                    get() = viewPager2.currentItem
                override val isEmpty: Boolean
                    get() = viewPager2.isEmpty
                override val count: Int
                    get() = adapter.itemCount

                override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
                    viewPager2.setCurrentItem(item, smoothScroll)
                }

                override fun removeOnPageChangeListener() {
                    onPageChangeCallback?.let { viewPager2.unregisterOnPageChangeCallback(it) }
                }

                override fun addOnPageChangeListener() {
                    onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageScrolled(
                            position: Int,
                            positionOffset: Float,
                            positionOffsetPixels: Int
                        ) {
                            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                            if (position + 1 >= count || position == -1) {
                                return
                            }
                            dots[position].alpha = 1 - positionOffset
                            dots[position + 1].alpha = positionOffset
                        }

                        override fun onPageScrollStateChanged(state: Int) {
                            super.onPageScrollStateChanged(state)
                            if (state == ViewPager2.SCROLL_STATE_IDLE && currentItem != currentDot) {
                                refreshDots()
                            }
                        }

                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            if (currentItem != currentDot) {
                                refreshDots()
                            }
                        }
                    }
                    onPageChangeCallback?.let { viewPager2.registerOnPageChangeCallback(it) }
                }
            }
        }
    }

    private val ViewPager2?.isEmpty: Boolean
        get() = this != null && adapter != null && adapter?.itemCount == 0
}
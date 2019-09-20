package com.kelmer.android.fabmenu.linear

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.animation.*
import com.kelmer.android.fabmenu.R

class LinearFabMenu @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val openInterpolator: Interpolator = OvershootInterpolator()
    private val closeInterpolator: Interpolator = AnticipateInterpolator()


    lateinit var menuButtonShowAnimation: Animation
        set(value) {
            field = value
            menuButton.setShowAnimation(showAnimation)
        }
    lateinit var menuButtonHideAnimation: Animation
    lateinit var imageToggleShowAnimation: Animation
    lateinit var imageToggleHideAnmation: Animation


    private val fabSize: Int


    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LinearFabMenu, 0, 0)

        fabSize = a.getInt(R.styleable.FloatingActionButton_fab_size, SIZE_NORMAL)

        initBackgroundDimAnimation()
        createMenuButton()
        initMenuButtonAnimations()

        a.recycle()
    }


    private fun initMenuButtonAnimations() {
        setMenuButtonShowAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_scale_up))
        imageToggleShowAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_up)
    }

//    private fun setMenuButtonShowAnimation(showAnimation: Animation) {
//        menuButtonHideAnimation = showAnimation
//        menuButton.set
//    }

    private fun createMenuButton() {


    }

    private fun initBackgroundDimAnimation() {


    }


    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
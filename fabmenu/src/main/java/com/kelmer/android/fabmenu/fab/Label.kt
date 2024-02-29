package com.kelmer.android.fabmenu.fab

import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.Animation
import com.kelmer.android.fabmenu.Util
import kotlin.math.abs

class Label @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    private val PORTER_DUFF_CLEAR = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

    private var shadowRadius: Int = 0
    private var shadowXOffset: Int = 0
    private var shadowYOffset: Int = 0
    private var mShadowColor: Int = 0
    private var mBackgroundDrawable: Drawable? = null
    private var mShowShadow = true
    private var mRawWidth: Int = 0
    private var mRawHeight: Int = 0
    private var mColorNormal: Int = 0
    private var mColorPressed: Int = 0
    private var mColorRipple: Int = 0
    private var mCornerRadius: Int = 0
    private var mFab: FloatingActionButton? = null
    private var mShowAnimation: Animation? = null
    private var mHideAnimation: Animation? = null
    private var mUsingStyle: Boolean = false
    private var mHandleVisibilityChanges = true


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(calculateMeasuredWidth(), calculateMeasuredHeight())
    }

    private fun calculateMeasuredWidth(): Int {
        if (mRawWidth == 0) {
            mRawWidth = measuredWidth
        }
        return measuredWidth + calculateShadowWidth()
    }

    private fun calculateMeasuredHeight(): Int {
        if (mRawHeight == 0) {
            mRawHeight = measuredHeight
        }
        return measuredHeight + calculateShadowHeight()
    }

    internal fun calculateShadowWidth(): Int {
        return if (mShowShadow) shadowRadius + Math.abs(shadowXOffset) else 0
    }

    internal fun calculateShadowHeight(): Int {
        return if (mShowShadow) shadowRadius + Math.abs(shadowYOffset) else 0
    }

    internal fun updateBackground() {
        val layerDrawable: LayerDrawable
        if (mShowShadow) {
            layerDrawable = LayerDrawable(arrayOf(Shadow(), createFillDrawable()))

            val leftInset = shadowRadius + abs(shadowXOffset)
            val topInset = shadowRadius + abs(shadowYOffset)
            val rightInset = shadowRadius + abs(shadowXOffset)
            val bottomInset = shadowRadius + abs(shadowYOffset)

            layerDrawable.setLayerInset(
                1,
                leftInset,
                topInset,
                rightInset,
                bottomInset
            )
        } else {
            layerDrawable = LayerDrawable(arrayOf(createFillDrawable()))
        }

        setBackgroundCompat(layerDrawable)
    }

    private fun createFillDrawable(): Drawable {
        val drawable = StateListDrawable()
        drawable.addState(
            intArrayOf(android.R.attr.state_pressed),
            createRectDrawable(mColorPressed)
        )
        drawable.addState(intArrayOf(), createRectDrawable(mColorNormal))

        if (Util.isLollipop()) {
            val ripple = RippleDrawable(
                ColorStateList(
                    arrayOf(intArrayOf()),
                    intArrayOf(mColorRipple)
                ), drawable, null
            )
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
            clipToOutline = true
            mBackgroundDrawable = ripple
            return ripple
        }

        mBackgroundDrawable = drawable
        return drawable
    }

    private fun createRectDrawable(color: Int): Drawable {
        val shape = RoundRectShape(
            floatArrayOf(
                mCornerRadius.toFloat(),
                mCornerRadius.toFloat(),
                mCornerRadius.toFloat(),
                mCornerRadius.toFloat(),
                mCornerRadius.toFloat(),
                mCornerRadius.toFloat(),
                mCornerRadius.toFloat(),
                mCornerRadius.toFloat()
            ),
            null, null
        )
        val shapeDrawable = ShapeDrawable(shape)
        shapeDrawable.paint.color = color
        return shapeDrawable
    }

    private fun setShadow(fab: FloatingActionButton) {
        mShadowColor = fab.shadowColor
        shadowRadius = fab.shadowRadius
        shadowXOffset = fab.shadowXOffset
        shadowYOffset = fab.shadowYOffset
        mShowShadow = fab.hasShadow()
    }

    private fun setBackgroundCompat(drawable: Drawable) {
        if (Util.isJellyBean()) {
            background = drawable
        } else {
            setBackgroundDrawable(drawable)
        }
    }

    private fun playShowAnimation() {
        if (mShowAnimation != null) {
            mHideAnimation?.cancel()
            startAnimation(mShowAnimation)
        }
    }

    private fun playHideAnimation() {
        if (mHideAnimation != null) {
            mShowAnimation?.cancel()
            startAnimation(mHideAnimation)
        }
    }

    internal fun onActionDown() {
        if (mUsingStyle) {
            mBackgroundDrawable = background
        }

        if (mBackgroundDrawable is StateListDrawable) {
            val drawable = mBackgroundDrawable as StateListDrawable
            drawable.state = intArrayOf(android.R.attr.state_pressed)
        } else if (Util.isLollipop() && mBackgroundDrawable is RippleDrawable) {
            val ripple = mBackgroundDrawable as RippleDrawable
            ripple.state = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_pressed)
            ripple.setHotspot((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat())
            ripple.setVisible(true, true)
        }
        //        setPressed(true);
    }

    internal fun onActionUp() {
        if (mUsingStyle) {
            mBackgroundDrawable = background
        }

        if (mBackgroundDrawable is StateListDrawable) {
            val drawable = mBackgroundDrawable as StateListDrawable
            drawable.state = intArrayOf()
        } else if (Util.isLollipop() && mBackgroundDrawable is RippleDrawable) {
            val ripple = mBackgroundDrawable as RippleDrawable
            ripple.state = intArrayOf()
            ripple.setHotspot((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat())
            ripple.setVisible(true, true)
        }
        //        setPressed(false);
    }

    internal fun setFab(fab: FloatingActionButton) {
        mFab = fab
        setShadow(fab)
    }

    internal fun setShowShadow(show: Boolean) {
        mShowShadow = show
    }

    internal fun setCornerRadius(cornerRadius: Int) {
        mCornerRadius = cornerRadius
    }

    internal fun setColors(colorNormal: Int, colorPressed: Int, colorRipple: Int) {
        mColorNormal = colorNormal
        mColorPressed = colorPressed
        mColorRipple = colorRipple
    }

    internal fun show(animate: Boolean) {
        if (animate) {
            playShowAnimation()
        }
        visibility = View.VISIBLE
    }

    internal fun hide(animate: Boolean) {
        if (animate) {
            playHideAnimation()
        }
        visibility = View.INVISIBLE
    }

    internal fun setShowAnimation(showAnimation: Animation) {
        mShowAnimation = showAnimation
    }

    internal fun setHideAnimation(hideAnimation: Animation) {
        mHideAnimation = hideAnimation
    }

    internal fun setUsingStyle(usingStyle: Boolean) {
        mUsingStyle = usingStyle
    }

    internal fun setHandleVisibilityChanges(handle: Boolean) {
        mHandleVisibilityChanges = handle
    }

    internal fun isHandleVisibilityChanges(): Boolean {
        return mHandleVisibilityChanges
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val fabb = mFab

        if (fabb?.getOnClickListener() == null || !fabb.isEnabled) {
            return super.onTouchEvent(event)
        }

        val action = event.action
        when (action) {
            MotionEvent.ACTION_UP -> {
                onActionUp()
//                fabb.onActionUp()
            }

            MotionEvent.ACTION_CANCEL -> {
                onActionUp()
//                fabb.onActionUp()
            }
        }

        mGestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    internal var mGestureDetector =
        GestureDetector(getContext(), object : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                onActionDown()
//                mFab?.onActionDown()
                return super.onDown(e)
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                onActionUp()
//                mFab?.onActionUp()
                return super.onSingleTapUp(e)
            }
        })

    private inner class Shadow() : Drawable() {

        private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val mErase = Paint(Paint.ANTI_ALIAS_FLAG)

        init {
            this.init()
        }

        private fun init() {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            mPaint.style = Paint.Style.FILL
            mPaint.color = mColorNormal

            mErase.xfermode = PORTER_DUFF_CLEAR

            if (!isInEditMode) {
                mPaint.setShadowLayer(
                    shadowRadius.toFloat(),
                    shadowXOffset.toFloat(),
                    shadowYOffset.toFloat(),
                    mShadowColor
                )
            }
        }

        override fun draw(canvas: Canvas) {
            val shadowRect = RectF(
                (shadowRadius + abs(shadowXOffset)).toFloat(),
                (shadowRadius + abs(shadowYOffset)).toFloat(),
                mRawWidth.toFloat(),
                mRawHeight.toFloat()
            )

            canvas.drawRoundRect(
                shadowRect,
                mCornerRadius.toFloat(),
                mCornerRadius.toFloat(),
                mPaint
            )
            canvas.drawRoundRect(
                shadowRect,
                mCornerRadius.toFloat(),
                mCornerRadius.toFloat(),
                mErase
            )
        }

        override fun setAlpha(alpha: Int) {

        }

        override fun setColorFilter(cf: ColorFilter?) {

        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSLUCENT
        }
    }


}
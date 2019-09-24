package com.kelmer.android.fabmenu.fab

import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.*
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.Shape
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import com.kelmer.android.fabmenu.R
import com.kelmer.android.fabmenu.Util
import com.kelmer.android.fabmenu.Util.dpToPx
import com.kelmer.android.fabmenu.Util.getColor
import kotlin.math.abs
import kotlin.math.max

class FloatingActionButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageButton(context, attrs, defStyleAttr) {


    var fabSize: Int

    var showAnimation: Animation
    var hideAnimation: Animation


    var showShadow: Boolean
    var shadowColor: Int
    private val usingElevation: Boolean = false
    var shadowRadius = dpToPx(4f)
    var shadowXOffset = dpToPx(1f)
    var shadowYOffset = dpToPx(3f)

    private val iconSize = dpToPx(24f).toInt()
    private val icon: Drawable? = null


    private var colorNormal: Int
    private var colorPressed: Int
    private var colorDisabled: Int
    private var colorRipple: Int


    private var bgDrawable: Drawable? = null
    private var labelText: String = ""



    private var clickListener: View.OnClickListener? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, 0, 0)

        colorNormal = a.getColor(R.styleable.FloatingActionButton_fab_colorNormal, getColor(R.color.fab_color_normal))
        colorPressed = a.getColor(R.styleable.FloatingActionButton_fab_colorPressed, getColor(R.color.fab_color_pressed))
        colorDisabled = a.getColor(R.styleable.FloatingActionButton_fab_colorDisabled, getColor(R.color.fab_color_disabled))
        colorRipple = a.getColor(R.styleable.FloatingActionButton_fab_colorRipple, getColor(R.color.fab_color_ripple))

        showShadow = a.getBoolean(R.styleable.FloatingActionButton_fab_showShadow, true)
        shadowColor = a.getColor(R.styleable.FloatingActionButton_fab_shadowColor, getColor(R.color.fab_shadow_color))

        fabSize = a.getInt(R.styleable.FloatingActionButton_fab_size, SIZE_NORMAL)

        var text = a.getString(R.styleable.FloatingActionButton_fab_label)
        if (!text.isNullOrBlank()) {
            labelText = text
        }

        a.recycle()

        showAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_up)
        hideAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_down)

        isClickable = true
    }


    private fun getCircleSize(): Int =
        resources.getDimensionPixelSize(if (fabSize == SIZE_NORMAL) R.dimen.fab_size_normal else R.dimen.fab_size_mini)


    private fun calculateMeasuredWidth(): Int = getCircleSize() + calculateShadowWidth()
    private fun calculateMeasuredHeight(): Int = getCircleSize() + calculateShadowHeight()


    private fun calculateShadowWidth(): Int = if (hasShadow()) getShadowX() * 2 else 0
    private fun calculateShadowHeight(): Int = if (hasShadow()) getShadowY() * 2 else 0

    private fun getShadowX() = (shadowRadius + abs(shadowXOffset)).toInt()
    private fun getShadowY() = (shadowRadius + abs(shadowYOffset)).toInt()

    private fun calculateCenterX(): Float = measuredWidth / 2f
    private fun calculateCenterY(): Float = measuredHeight / 2f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(calculateMeasuredWidth(), calculateMeasuredHeight())
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateBackground()
    }


    fun updateBackground() {
        val layerDrawable = if (hasShadow()) {
            LayerDrawable(arrayOf(Shadow(), createFillDrawable(), getIconDrawable()))
        } else {
            LayerDrawable(arrayOf(createFillDrawable(), getIconDrawable()))
        }

        val iconDrawable = getIconDrawable()
        var iconSize = max(iconDrawable.intrinsicWidth, iconDrawable.intrinsicHeight)

        val iconOffset = (getCircleSize() - (if (iconSize > 0) iconSize else this.iconSize)) / 2
        val circleInsetHorizontal: Int =
            if (hasShadow()) (shadowRadius + abs(shadowXOffset)).toInt() else 0
        val circleInsetVertical: Int =
            if (hasShadow()) (shadowRadius + abs(shadowYOffset)).toInt() else 0

        layerDrawable.setLayerInset(
            if (hasShadow()) 2 else 1,
            circleInsetHorizontal + iconOffset,
            circleInsetVertical + iconOffset,
            circleInsetHorizontal + iconOffset,
            circleInsetVertical + iconOffset
        )

        setBackgroundCompat(layerDrawable)
    }


    private fun getIconDrawable(): Drawable = icon ?: ColorDrawable(Color.TRANSPARENT)


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun createFillDrawable(): Drawable {
        val drawable = StateListDrawable()
        drawable.addState(
            intArrayOf(-android.R.attr.state_enabled),
            createCircleDrawable(colorDisabled)
        )
        drawable.addState(
            intArrayOf(android.R.attr.state_pressed),
            createCircleDrawable(colorPressed)
        )
        drawable.addState(intArrayOf(), createCircleDrawable(colorNormal))

        if (Util.isLollipop()) {
            val ripple: RippleDrawable = RippleDrawable(
                ColorStateList(
                    arrayOf(intArrayOf()),
                    intArrayOf(colorRipple)
                ), drawable, null
            )
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }

            clipToOutline = true
            bgDrawable = ripple
            return ripple
        }

        bgDrawable = drawable
        return drawable
    }

    private fun createCircleDrawable(color: Int): Drawable {
        val shapeDrawable = CircleDrawable(OvalShape())
        shapeDrawable.paint.color = color
        return shapeDrawable
    }

    @Suppress("DEPRECATION")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun setBackgroundCompat(drawable: Drawable) {
        if (Util.isJellyBean()) {
            background = drawable
        } else {
            setBackgroundDrawable(drawable)
        }
    }


    fun playShowAnimation() {
        hideAnimation.cancel()
        startAnimation(showAnimation)
    }

    fun playHideAnimation() {
        showAnimation.cancel()
        startAnimation(hideAnimation)
    }


    private fun getLabelView(): TextView? = getTag(R.id.fab_label) as? Label


    internal fun setColors(colorNormal: Int, colorPressed: Int, colorRipple: Int) {
        this.colorNormal = colorNormal
        this.colorPressed = colorPressed
        this.colorRipple = colorRipple
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun onActionDown() {

        if (bgDrawable is StateListDrawable) {
            val drawable = bgDrawable as StateListDrawable
            drawable.state = intArrayOf(
                android.R.attr.state_enabled,
                android.R.attr.state_pressed
            )
        } else if (Util.isLollipop()) {

            val ripple = bgDrawable as RippleDrawable
            ripple.state = intArrayOf(
                android.R.attr.state_enabled,
                android.R.attr.state_pressed
            )
            ripple.setHotspot(calculateCenterX(), calculateCenterY())
            ripple.setVisible(true, true)
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun onActionUp() {
        if (bgDrawable is StateListDrawable) {
            val drawable = bgDrawable as StateListDrawable
            drawable.state = intArrayOf(
                android.R.attr.state_enabled
            )
        } else if (Util.isLollipop()) {
            val ripple = bgDrawable as RippleDrawable
            ripple.state = intArrayOf(android.R.attr.state_enabled)
            ripple.setHotspot(calculateCenterX(), calculateCenterY())
            ripple.setVisible(true, true)
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (clickListener!=null && isEnabled) {
            val label = getTag(R.id.fab_label) as? Label ?: return super.onTouchEvent(event)

            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    label.onActionUp()
                    onActionUp()
                }
                MotionEvent.ACTION_CANCEL -> {
                    label.onActionUp()
                    onActionUp()
                }
            }
            gestureDetector.onTouchEvent(event)

        }
        return super.onTouchEvent(event)
    }


    private val gestureDetector = object : GestureDetector(context, object : SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?): Boolean {
            val label = getTag(R.id.fab_label) as? Label
            label?.onActionDown()
            onActionDown()
            return super.onDown(e)
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            val label = getTag(R.id.fab_label) as? Label
            label?.onActionUp()
            onActionUp()
            return super.onSingleTapUp(e)
        }


    }){}


    fun hasShadow() = !usingElevation && showShadow

    companion object {

        val PORTER_DUFF_CLEAR = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        const val SIZE_NORMAL = 0
        const val SIZE_MINI = 1

    }


    fun setLabelText(text: String) {
        labelText = text
        getLabelView()?.text = text
    }


    inner class CircleDrawable(s: Shape) : ShapeDrawable(s) {

        private var circleInsetHorizontal: Int = 0
        private var circleInsetVertical: Int = 0


        init {
            circleInsetHorizontal =
                if (hasShadow()) (shadowColor + abs(shadowXOffset)).toInt() else 0
            circleInsetVertical = if (hasShadow()) (shadowColor + abs(shadowYOffset)).toInt() else 0
        }

        override fun draw(canvas: Canvas) {
            setBounds(
                circleInsetHorizontal,
                circleInsetVertical,
                calculateMeasuredWidth() - circleInsetHorizontal,
                calculateMeasuredHeight() - circleInsetVertical
            )
            super.draw(canvas)
        }
    }

    inner class Shadow : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val erase = Paint(Paint.ANTI_ALIAS_FLAG)

        private val radius: Float

        init {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            paint.style = Paint.Style.FILL
            paint.color = colorNormal

            erase.xfermode = PORTER_DUFF_CLEAR

            if (!isInEditMode) {
                paint.setShadowLayer(shadowRadius, shadowXOffset, shadowYOffset, shadowColor)
            }

            radius = getCircleSize() / 2f

        }

        override fun draw(canvas: Canvas) {
            canvas.drawCircle(calculateCenterX(), calculateCenterY(), radius, paint)
            canvas.drawCircle(calculateCenterX(), calculateCenterY(), radius, erase)
        }

        override fun setAlpha(alpha: Int) {
        }

        override fun getOpacity(): Int = PixelFormat.UNKNOWN

        override fun setColorFilter(colorFilter: ColorFilter?) {
        }
    }

    /**
     * Makes FAB disappear setting its visibility to INVISIBLE
     */
    fun hide(animate: Boolean) {
        if (!isHidden()) {
            if (animate) {
                playHideAnimation()
            }
            super.setVisibility(View.INVISIBLE)
        }
    }

    fun show(animate: Boolean) {
        if (isHidden()) {
            if (animate) {
                playShowAnimation()
            }
            super.setVisibility(View.VISIBLE)
        }
    }

    fun isHidden() = visibility == View.INVISIBLE


    fun getLabelText(): String {
        return labelText
    }

    fun getOnClickListener(): OnClickListener? = clickListener

    override fun setOnClickListener(l: OnClickListener?){
        super.setOnClickListener(l)
        clickListener = l
        (getTag(R.id.fab_label) as? View)?.setOnClickListener {
            clickListener?.onClick(this@FloatingActionButton)
        }
    }


    fun setButtonSize(size: Int) {
        if (size != SIZE_NORMAL && size != SIZE_MINI) {
            throw IllegalArgumentException("Use @FabSize constants only!")
        }

        if (fabSize != size) {
            fabSize = size
            updateBackground()
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        val label = getTag(R.id.fab_label) as? Label
        if (label != null) {
            label.isEnabled = enabled
        }
    }
}
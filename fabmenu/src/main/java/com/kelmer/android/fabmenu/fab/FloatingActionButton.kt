package com.kelmer.android.fabmenu.fab

import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.*
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.Shape
import android.os.Build
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Checkable
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.kelmer.android.fabmenu.R
import com.kelmer.android.fabmenu.Util
import com.kelmer.android.fabmenu.Util.dpToPx
import com.kelmer.android.fabmenu.Util.getColor
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max

class FloatingActionButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageButton(context, attrs, defStyleAttr), Checkable {


    var fabSize: Int

    var showAnimation: Animation
    var hideAnimation: Animation


    var showShadow: Boolean
    var shadowColor: Int
    private var usingElevation: Boolean = false
    var shadowRadius: Int = dpToPx(4f).toInt()
    var shadowXOffset: Int = dpToPx(1f).toInt()
    var shadowYOffset: Int = dpToPx(3f).toInt()

    private val iconSize = dpToPx(24f).toInt()
    private var icon: Drawable? = null


    private var colorNormal: Int
    private var colorPressed: Int
    private var colorDisabled: Int
    private var colorRipple: Int
    private var colorReveal: Int


    private var currentColor: Int

    private var bgDrawable: Drawable? = null
    private var labelText: String = ""

    private var clickListener: View.OnClickListener? = null


    // Progress
    private var progressBarEnabled: Boolean = false
    private var progressWidth: Int = dpToPx(6f).toInt()
    private var progressColor: Int
    private var progressBackgroundColor: Int
    private var showProgressBackground: Boolean = false
    private var progressCircleBounds = RectF()

    private var lastTimeAnimated: Long = 0
    private val spinSpeed = 195f //Degrees per second

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var currentProgress: Float = 0f

    private val barLength = 16
    private var barExtraLength: Float = 0f

    private var pausedTimeWithoutGrowing: Long = 0
    private var timeStartGrowing: Float = 0.0f
    private var barGrowingFromFront = true


    private var shouldUpdateButtonPosition: Boolean = false

    private var originalX = -1f
    private var originalY = -1f
    private var buttonPositionSaved: Boolean = false

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, 0, 0)

        colorNormal = a.getColor(
            R.styleable.FloatingActionButton_fab_colorNormal,
            getColor(R.color.fab_color_normal)
        )
        currentColor = colorNormal
        colorPressed = a.getColor(
            R.styleable.FloatingActionButton_fab_colorPressed,
            getColor(R.color.fab_color_pressed)
        )
        colorDisabled = a.getColor(
            R.styleable.FloatingActionButton_fab_colorDisabled,
            getColor(R.color.fab_color_disabled)
        )
        colorRipple = a.getColor(
            R.styleable.FloatingActionButton_fab_colorRipple,
            getColor(R.color.fab_color_ripple)
        )

        colorReveal = a.getColor(
            R.styleable.FloatingActionButton_fab_colorReveal,
            getColor(R.color.fab_reveal_color)
        )

        val checked = a.getBoolean(R.styleable.FloatingActionButton_fab_checked, false)
        isChecked = checked


        showShadow = a.getBoolean(R.styleable.FloatingActionButton_fab_showShadow, true)
        shadowColor = a.getColor(
            R.styleable.FloatingActionButton_fab_shadowColor,
            getColor(R.color.fab_shadow_color)
        )

        fabSize = a.getInt(R.styleable.FloatingActionButton_fab_size, SIZE_NORMAL)

        var text = a.getString(R.styleable.FloatingActionButton_fab_label)
        if (!text.isNullOrBlank()) {
            labelText = text
        }


        progressColor = a.getColor(
            R.styleable.FloatingActionButton_fab_progress_color,
            getColor(R.color.fab_progress_color)
        )
        progressBackgroundColor = a.getColor(
            R.styleable.FloatingActionButton_fab_progress_backgroundColor,
            getColor(R.color.fab_background_color)
        )
        showProgressBackground =
            a.getBoolean(R.styleable.FloatingActionButton_fab_progress_showBackground, true)

        progressWidth = a.getDimension(
            R.styleable.FloatingActionButton_fab_progress_width,
            resources.getDimension(R.dimen.fab_progress_width)
        ).toInt()


        a.recycle()

        showAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_up)
        hideAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_down)

        isClickable = true
        refreshDrawableState()
    }


    private fun getCircleSize(): Int =
        resources.getDimensionPixelSize(if (fabSize == SIZE_NORMAL) R.dimen.fab_size_normal else R.dimen.fab_size_mini)


    private fun calculateMeasuredWidth(): Int {
        var width = getCircleSize() + calculateShadowWidth()
//        if (progressBarEnabled) {
//            width += progressWidth * 2
//        }
        return width
    }

    private fun calculateMeasuredHeight(): Int {
        var height = getCircleSize() + calculateShadowHeight()
//        if (progressBarEnabled) {
//            height += progressWidth * 2
//        }
        return height
    }


    private fun calculateShadowWidth(): Int = if (hasShadow()) getShadowX() * 2 else 0
    private fun calculateShadowHeight(): Int = if (hasShadow()) getShadowY() * 2 else 0

    private fun getShadowX() = (shadowRadius + abs(shadowXOffset)).toInt()
    private fun getShadowY() = (shadowRadius + abs(shadowYOffset)).toInt()

    private fun calculateCenterX(): Float = measuredWidth / 2f
    private fun calculateCenterY(): Float = measuredHeight / 2f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(calculateMeasuredWidth(), calculateMeasuredHeight())
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (progressBarEnabled) {
            if (showProgressBackground) {
                canvas.drawArc(progressCircleBounds, 360f, 360f, false, backgroundPaint)
            }

            val deltaTime = SystemClock.uptimeMillis() - lastTimeAnimated
            val deltaNormalized = deltaTime * spinSpeed / 1000f

            updateProgressLength(deltaTime)

            currentProgress += deltaNormalized
            if (currentProgress > 360f) {
                currentProgress -= 360f
            }

            lastTimeAnimated = SystemClock.uptimeMillis()
            val from: Float = if (isInEditMode) 0f else currentProgress - 90
            val to = if (isInEditMode) 135f else barLength + barExtraLength

            canvas.drawArc(progressCircleBounds, from, to, false, progressPaint)

            invalidate()
        }
    }

    private fun updateProgressLength(deltaTime: Long) {
        if (pausedTimeWithoutGrowing >= PAUSE_GROWING_TIME) {
            timeStartGrowing += deltaTime

            if (timeStartGrowing > BAR_SPIN_CYCLE_TIME) {
                timeStartGrowing -= BAR_SPIN_CYCLE_TIME
                pausedTimeWithoutGrowing = 0
                barGrowingFromFront = !barGrowingFromFront
            }

            val distance: Float =
                cos((timeStartGrowing / BAR_SPIN_CYCLE_TIME + 1) * Math.PI.toFloat()) / 2 + 0.5f
            val length: Float = (BAR_MAX_LENGTH - barLength).toFloat()

            if (barGrowingFromFront) {
                barExtraLength = distance * length
            } else {
                val newLength = length * (1 - distance)
                currentProgress += (barExtraLength - newLength)
                barExtraLength = newLength
            }
        } else {
            pausedTimeWithoutGrowing += deltaTime
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
//        saveButtonOriginalPosition()
//
//        if(shouldUpdateButtonPosition){
//            updateButtonPosition()
//            shouldUpdateButtonPosition = false
//        }
        super.onSizeChanged(w, h, oldw, oldh)

        setupProgressBounds()
        setupProgressBarPaints()
        updateBackground()
    }
//
//    private fun saveButtonOriginalPosition() {
//        if (!buttonPositionSaved) {
//            if (originalX == -1f) {
//                originalX = x
//            } else {
//                originalY = y
//            }
//            buttonPositionSaved = true
//        }
//
//    }
//
//    private fun updateButtonPosition() {
//        var x: Float = 0f
//        var y: Float = 0f
//
//        if (progressBarEnabled) {
//            x = if (originalX > x) x + progressWidth else x - progressWidth
//            y = if (originalY > y) y + progressWidth else y - progressWidth
//        } else {
//            x = originalX
//            y = originalY
//        }
//        setX(x)
//        setY(y)
//    }

    private fun setupProgressBarPaints() {
        backgroundPaint.color = progressBackgroundColor
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeWidth = progressWidth.toFloat()

        progressPaint.color = progressColor
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeWidth = progressWidth.toFloat()

    }

    private fun setupProgressBounds() {
        val circleInsetHorizontal = if (hasShadow()) getShadowX() else 0
        val circleInsetVertical = if (hasShadow()) getShadowY() else 0
        progressCircleBounds = RectF(
            (circleInsetHorizontal + progressWidth / 2).toFloat(),
            (circleInsetVertical + progressWidth / 2).toFloat(),
            (calculateMeasuredWidth() - circleInsetHorizontal - progressWidth / 2).toFloat(),
            (calculateMeasuredHeight() - circleInsetVertical - progressWidth / 2).toFloat()
        )

    }


    fun updateBackground() {
        val layerDrawable = if (hasShadow()) {
            LayerDrawable(arrayOf<Drawable>(Shadow(), createFillDrawable(), getIconDrawable()))
        } else {
            LayerDrawable(arrayOf<Drawable>(createFillDrawable(), getIconDrawable()))
        }


        var iconSize = -1

        if (getIconDrawable() != null) {
            iconSize = max(getIconDrawable().intrinsicWidth, getIconDrawable().intrinsicHeight)
        }
        val iconOffset = (getCircleSize() - (if (iconSize > 0) iconSize else this.iconSize)) / 2
        var circleInsetHorizontal: Int =
            if (hasShadow()) (shadowRadius + abs(shadowXOffset)) else 0
        var circleInsetVertical: Int =
            if (hasShadow()) (shadowRadius + abs(shadowYOffset)) else 0

//
//
//        if (progressBarEnabled) {
//            circleInsetHorizontal += progressWidth
//            circleInsetVertical += progressWidth
//        }


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

        drawable.addState(intArrayOf(), createCircleDrawable(currentColor))

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
            setBackground(drawable)
        } else {
            setBackgroundDrawable(drawable)
        }
    }

    override fun setElevation(elevation: Float) {
        if (Util.isLollipop() && elevation > 0) {
            super.setElevation(elevation)
            if (!isInEditMode) {
                usingElevation = true
                showShadow = false
            }
            updateBackground()
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
        this.currentColor = colorNormal
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

//
//    fun setChecked(checked: Boolean) {
//        val bg = getIconDrawable()
//        if (bg != null) {
//            if (checked) {
//                bg.state = intArrayOf(
//                    android.R.attr.state_enabled, android.R.attr.state_checked
//                )
//            } else {
//                bg.state = intArrayOf(
//                    android.R.attr.state_enabled
//                )
//            }
//        }
//    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (clickListener != null && isEnabled) {
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


    private val gestureDetector =
        object : GestureDetector(context, object : SimpleOnGestureListener() {

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


        }) {}


    fun hasShadow() = !usingElevation && showShadow

    companion object {

        val PORTER_DUFF_CLEAR = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        const val SIZE_NORMAL = 0
        const val SIZE_MINI = 1
        const val PAUSE_GROWING_TIME: Long = 200
        const val BAR_SPIN_CYCLE_TIME = 500f
        const val BAR_MAX_LENGTH = 270
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

        private var radius: Float = getCircleSize() / 2f

        init {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            paint.style = Paint.Style.FILL

            paint.color = currentColor

            erase.xfermode = PORTER_DUFF_CLEAR

            if (!isInEditMode) {
                paint.setShadowLayer(
                    shadowRadius.toFloat(),
                    shadowXOffset.toFloat(),
                    shadowYOffset.toFloat(),
                    shadowColor
                )
            }
            if (progressBarEnabled && showProgressBackground) {
                radius += progressWidth
            }

        }

        override fun draw(canvas: Canvas) {
            canvas.drawCircle(calculateCenterX(), calculateCenterY(), radius, paint)
            canvas.drawCircle(calculateCenterX(), calculateCenterY(), radius, erase)
        }

        override fun setAlpha(alpha: Int) {
        }

        override fun getOpacity(): Int = PixelFormat.OPAQUE

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

    override fun setOnClickListener(l: OnClickListener?) {
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

    override fun setImageDrawable(drawable: Drawable?) {
        if (icon !== drawable) {
            icon = drawable
            updateBackground()
        }
    }

    override fun setImageResource(resId: Int) {
        val drawable = ContextCompat.getDrawable(context, resId)
        if (icon !== drawable) {
            icon = drawable
            updateBackground()
        }
    }


    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        val label = getTag(R.id.fab_label) as? Label
        if (label != null) {
            label.visibility = visibility
        }
    }


    private var mChecked: Boolean = false
    override fun isChecked(): Boolean = mChecked

    override fun toggle() {
        isChecked = !mChecked
        refreshDrawableState()
    }

    override fun setChecked(checked: Boolean) {
        if (mChecked == checked)
            return
        mChecked = checked
        refreshDrawableState()
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked)
            AppCompatImageView.mergeDrawableStates(
                drawableState,
                intArrayOf(android.R.attr.state_checked)
            )
        return drawableState
    }

    @Synchronized
    fun hideProgress() {
        progressBarEnabled = false
        updateBackground()
    }

    @Synchronized
    fun setShowProgressBackground(show: Boolean) {
        showProgressBackground = show
    }

    @Synchronized
    fun showProgressBar() {
        progressBarEnabled = true
        shouldUpdateButtonPosition = true
        lastTimeAnimated = SystemClock.uptimeMillis()
        setupProgressBounds()
        updateBackground()
    }

    fun setProgressColors(progressColor: Int, progressBackgroundColor: Int) {
        this.progressBackgroundColor = progressBackgroundColor
        this.progressColor = progressColor
    }

    fun setProgressWidth(progressWidth: Int) {
        this.progressWidth = progressWidth
    }

    fun doReveal(color: Int) {
        currentColor = color
        updateBackground()
    }

    fun undoReveal() {
        currentColor = colorNormal
        updateBackground()

    }

}
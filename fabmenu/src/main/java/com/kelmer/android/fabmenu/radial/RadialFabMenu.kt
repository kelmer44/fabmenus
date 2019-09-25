package com.kelmer.android.fabmenu.radial

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import androidx.core.content.ContextCompat
import android.util.TypedValue
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.kelmer.android.fabmenu.MenuInterface
import com.kelmer.android.fabmenu.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


class RadialFabMenu @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    private val circlePaint: Paint
    private val circleBorder: Paint
    private var rotationAnimation: ValueAnimator
    private var rotationReverseAnimation: ValueAnimator

    private val fabButtonRadius: Int
    private val menuButtonRadius: Int
    private val gap: Int
    private val numberOfItems: Int = 2

    private var centerX: Int = 0
    private var centerY: Int = 0


    private val menuItemPoints = mutableListOf<CirclePoint>()

    private var updateListener = ValueAnimator.AnimatorUpdateListener { invalidate() }
    private var bezierUpdateListener = ValueAnimator.AnimatorUpdateListener { valueAnimator ->
        bezierConstant = valueAnimator.animatedValue as Float
        invalidate()
    }
    private var rotationUpdateListener = ValueAnimator.AnimatorUpdateListener { valueAnimator ->
        rotationAngle = valueAnimator.animatedValue as Float
        invalidate()
    }

    private var rotationAngle: Float = 0f
    private var isMenuVisible = false


    private val showAnimations = mutableListOf<ObjectAnimator>()
    private val hideAnimations = mutableListOf<ObjectAnimator>()
    private var drawables = mutableListOf<Drawable>()


    lateinit var plusBitmap: Bitmap
    private var bezierConstant: Float =
        BEZIER_CONSTANT


    var listener: MenuInterface? = null


    init {
        val a = context.theme.obtainStyledAttributes(attrs,
            R.styleable.RadialFabMenu, 0, 0)
        try {
            fabButtonRadius = a.getDimension(
                R.styleable.RadialFabMenu_fab_radius,
                resources.getDimension(R.dimen.big_circle_radius)
            ).toInt()
            menuButtonRadius = a.getDimension(
                R.styleable.RadialFabMenu_menu_radius,
                resources.getDimension(R.dimen.small_circle_radius)
            ).toInt()

            val outValue = TypedValue()
            // Read array of target drawables
            if (a.getValue(R.styleable.RadialFabMenu_menu_drawable, outValue)) {
                val res = getContext().resources
                val array = res.obtainTypedArray(outValue.resourceId)
                drawables = mutableListOf()
                for (i in 0 until array.length()) {
                    val value = array.peekValue(i)
                    val drawable = ContextCompat.getDrawable(context, value.resourceId)
                    if (drawable != null) {
                        drawables.add(drawable)
                    }
                }
                array.recycle()
            }

        } finally {
            a.recycle()
        }

        gap = resources.getDimensionPixelSize(R.dimen.default_gap)

        circlePaint = Paint()
        circlePaint.color = ContextCompat.getColor(context,
            R.color.default_color
        )
        circlePaint.style = Paint.Style.FILL_AND_STROKE


        circleBorder = Paint(circlePaint)
        circleBorder.style = Paint.Style.STROKE
        circleBorder.strokeWidth = 1f
        circleBorder.color = ContextCompat.getColor(context,
            R.color.default_color_dark
        )


        rotationAnimation = ValueAnimator.ofFloat(
            START_ANGLE,
            END_ANGLE
        )
        rotationAnimation.apply {
            duration = ANIMATION_DURATION / 4
            interpolator = AccelerateInterpolator()
            addUpdateListener(rotationUpdateListener)
        }

        rotationReverseAnimation = ValueAnimator.ofFloat(
            END_ANGLE,
            START_ANGLE
        )
        rotationReverseAnimation.apply {
            duration = ANIMATION_DURATION / 4
            interpolator = AccelerateInterpolator()
            addUpdateListener(rotationUpdateListener)
        }

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val desiredWidth = measuredWidth
        val desiredHeight = context.resources.getDimensionPixelSize(R.dimen.min_height)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredWidth, widthSize)
            else -> //can be whatever
                desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2
        centerY = h - fabButtonRadius
        for (i in 0 until numberOfItems) {

            menuItemPoints.add(
                CirclePoint(
                    radius = gap.toFloat(),
                    angle = ((Math.PI / (numberOfItems + 1)) * (i + 1))
                )
            )

            val animShow = ObjectAnimator.ofFloat(menuItemPoints[i], "Radius", 0f, gap.toFloat())
            animShow.apply {
                duration =
                    ANIMATION_DURATION
                interpolator = AnticipateOvershootInterpolator()
                startDelay = (ANIMATION_DURATION * (numberOfItems - i)) / 10
                addUpdateListener(updateListener)
            }
            showAnimations.add(animShow)
            val animHide = animShow.clone()
            animHide.apply {
                setFloatValues(gap.toFloat(), 0f)
                startDelay = ANIMATION_DURATION * i / 10
            }
            hideAnimations.add(animHide)
            drawables.forEach { drawable ->
                drawable.setBounds(0, 0, menuButtonRadius, menuButtonRadius)
            }
        }


    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val toBitmap = AppCompatResources.getDrawable(context,
            R.drawable.ic_add
        )?.toBitmap()
        if (toBitmap != null) {
            plusBitmap = toBitmap
        }

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        hideAnimations.clear()
        showAnimations.clear()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in 0 until numberOfItems) {
            val circlePoint = menuItemPoints[i]
            val x = circlePoint.radius * Math.cos(circlePoint.angle).toFloat()
            val y = circlePoint.radius * Math.sin(circlePoint.angle).toFloat()
            canvas.drawCircle(
                x + centerX,
                centerY - y,
                menuButtonRadius.toFloat(),
                circlePaint
            )
            if (i < drawables.size) {
                canvas.save()
                canvas.translate(
                    x + centerX - menuButtonRadius / 2,
                    centerY - y - menuButtonRadius / 2
                )
                drawables[i].draw(canvas)
                canvas.restore()
            }
        }

        canvas.save()
        canvas.translate(centerX.toFloat(), centerY.toFloat())
        val path: Path = createPath()
        canvas.drawPath(path, circlePaint)
        canvas.drawPath(path, circleBorder)
        canvas.rotate(rotationAngle)
        canvas.drawBitmap(
            plusBitmap,
            -plusBitmap.width / 2f,
            -plusBitmap.height / 2f,
            circlePaint
        )
        canvas.restore()

    }

    private fun createPath(): Path {
        val path = Path()
        val c = bezierConstant * fabButtonRadius

        path.moveTo(0f, fabButtonRadius.toFloat())
        path.cubicTo(
            bezierConstant * fabButtonRadius,
            fabButtonRadius.toFloat(),
            fabButtonRadius.toFloat(),
            BEZIER_CONSTANT * fabButtonRadius,
            fabButtonRadius.toFloat(),
            0f
        )
        path.cubicTo(
            fabButtonRadius.toFloat(),
            BEZIER_CONSTANT * fabButtonRadius.toFloat() * (-1).toFloat(),
            c,
            (-1 * fabButtonRadius).toFloat(),
            0f,
            (-1 * fabButtonRadius).toFloat()
        )
        path.cubicTo(
            -1 * c,
            (-1 * fabButtonRadius).toFloat(),
            (-1 * fabButtonRadius).toFloat(),
            (-1).toFloat() * BEZIER_CONSTANT * fabButtonRadius.toFloat(),
            (-1 * fabButtonRadius).toFloat(),
            0f
        )
        path.cubicTo(
            (-1 * fabButtonRadius).toFloat(),
            BEZIER_CONSTANT * fabButtonRadius,
            (-1).toFloat() * bezierConstant * fabButtonRadius.toFloat(),
            fabButtonRadius.toFloat(),
            0f,
            fabButtonRadius.toFloat()
        )

        return path

    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isMenuTouch(event)) {
                    return true
                }
                val menuItem: Int = isMenuItemTouched(event)
                if (isMenuVisible && menuItem > 0) {
                    if (menuItem <= drawables.size) {
                        drawables[menuItemPoints.size - menuItem].state =
                            STATE_PRESSED
                        invalidate()
                    }
                    return true
                }
                return false
            }
            MotionEvent.ACTION_UP -> {
                if (isMenuTouch(event)) {
                    cancelAnimations()
                    if (isMenuVisible) {
                        startHideAnimation()
                        listener?.menuClose()
                    } else {
                        startShowAnimation()
                        listener?.menuOpen()
                    }

                    isMenuVisible = !isMenuVisible
                    Log.i("FabMenu", "Just changed menu visibility to $isMenuVisible")
                    return true
                }

                if (isMenuVisible) {
                    val menuItem = isMenuItemTouched(event)
                    invalidate()
                    if (menuItem > 0) {
                        if (menuItem <= drawables.size) {
                            drawables[menuItemPoints.size - menuItem].state =
                                STATE_ACTIVE
                            postInvalidateDelayed(1000)
                        }
                        listener?.menuItemClicked(menuItem)
                    }
                    return true
                }
                return false
            }
        }
        return true
    }

    private fun isMenuItemTouched(event: MotionEvent): Int {
        if (!isMenuVisible) {
            return -1
        }
        menuItemPoints.forEachIndexed { index, circlePoint ->
            val x = gap * cos(circlePoint.angle) + centerX
            val y = centerY - (gap * sin(circlePoint.angle))
            if (event.x >= x - menuButtonRadius && event.y <= x + menuButtonRadius) {
                if (event.y >= y - menuButtonRadius && event.y <= y + menuButtonRadius) {
                    return menuItemPoints.size - index
                }
            }
        }

        return -1
    }

    private fun startShowAnimation() {
        rotationAnimation.start()
        for (objectAnimator in showAnimations) {
            objectAnimator.start()
        }
    }

    private fun startHideAnimation() {
        Log.w("FabMenu", "Start hiding animation!")
        rotationReverseAnimation.start()
        for (objectAnimator in hideAnimations) {
            objectAnimator.start()
        }
    }


    private fun isMenuTouch(event: MotionEvent): Boolean {
        if (event.x >= centerX - fabButtonRadius && event.x <= centerX + fabButtonRadius) {
            if (event.y >= centerY - fabButtonRadius && event.y <= centerY + fabButtonRadius) {
                return true
            }
        }
        return false
    }

    private fun cancelAnimations() {
        hideAnimations.forEach {
            it.cancel()
        }
        showAnimations.forEach {
            it.cancel()
        }
    }


    companion object {
        private const val ANIMATION_DURATION = 1000L
        private const val BEZIER_CONSTANT = 0.551915024494f;// pre-calculated value
        private const val START_ANGLE = 0f
        private const val END_ANGLE = 45f

        val STATE_ACTIVE = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_active)
        val STATE_PRESSED = intArrayOf(
            android.R.attr.state_enabled,
            -android.R.attr.state_active,
            android.R.attr.state_pressed
        )
    }
}
package com.kelmer.android.fabmenu.fab

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import com.kelmer.android.fabmenu.R
import com.kelmer.android.fabmenu.Util.dpToPx
import kotlin.math.abs

class FloatingActionButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageButton(context, attrs, defStyleAttr) {


    private val fabSize: Int

    private val showAnimation: Animation
    private val hideAnimation: Animation


    private val shadowColor: Int
    private val showShadow: Boolean
    private val usingElevation: Boolean = false
    private val shadowRadius = dpToPx(4f)
    private val shadowXOffset = dpToPx(1f)
    private val shadowYOffset = dpToPx(3f)


    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, 0, 0)


        fabSize = a.getInt(R.styleable.FloatingActionButton_fab_size, SIZE_NORMAL)
        showShadow = a.getBoolean(R.styleable.FloatingActionButton_fab_showShadow, true)
        shadowColor = a.getColor(R.styleable.FloatingActionButton_fab_shadowColor, 0x66000000)
        a.recycle()


        showAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_up)
        hideAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_down)

        isClickable = true
    }


    private fun getCircleSize(): Int =
        resources.getDimensionPixelSize(if (fabSize == SIZE_NORMAL) R.dimen.fab_size_normal else R.dimen.fab_size_mini)


    private fun calculateMeasuredWidth(): Int {
        val width = getCircleSize() + calculateShadowWidth()
        //FIXME: Account for progressbar
        return width
    }

    private fun calculateMeasuredHeight(): Int {
        val height = getCircleSize() + calculateShadowHeight()
        //FIXME: Account for progressbar
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
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        super.setLayoutParams(params)
    }

    fun updateBackground() {
        val layerDrawable = if(hasShadow()){
            LayerDrawable([Shadow])
        }
        else {
            LayerDrawable()
        }
    }


    private fun hasShadow() = !usingElevation && showShadow

    companion object {

        val PORTER_DUFF_CLEAR = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        const val SIZE_NORMAL = 0
        const val SIZE_MINI = 1

    }

    inner class Shadow : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val erase = Paint(Paint.ANTI_ALIAS_FLAG)

        private val radius : Float


        init {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            paint.style = Paint.Style.FILL

            erase.xfermode = PORTER_DUFF_CLEAR

            if(!isInEditMode){
                paint.setShadowLayer(shadowRadius, shadowXOffset, shadowYOffset, shadowColor)
            }

            radius = getCircleSize() / 2f

            //FIXME: STuff for progress bar
        }

        override fun draw(canvas: Canvas) {
            canvas.drawCircle(calculateCenterX(), calculateCenterY(), radius, paint)
            canvas.drawCircle(calculateCenterX(), calculateCenterY(), radius, erase)
        }

        override fun setAlpha(alpha: Int) {
        }

        override fun getOpacity(): Int = PixelFormat.OPAQUE

        override fun setColorFilter(colorFilter: ColorFilter) {
        }


    }
}
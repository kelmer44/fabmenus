package com.kelmer.android.fabmenu.linear

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.TextViewCompat
import com.kelmer.android.fabmenu.MenuInterface
import com.kelmer.android.fabmenu.R
import com.kelmer.android.fabmenu.Util.dpToPx
import com.kelmer.android.fabmenu.Util.getColor
import com.kelmer.android.fabmenu.fab.FloatingActionButton
import com.kelmer.android.fabmenu.fab.Label
import kotlin.math.*


open class LinearFabMenu @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val openInterpolator: Interpolator = OvershootInterpolator()
    private val closeInterpolator: Interpolator = AnticipateInterpolator()

    lateinit var menuButton: FloatingActionButton


    lateinit var menuButtonShowAnimation: Animation
    lateinit var menuButtonHideAnimation: Animation
    lateinit var imageToggleShowAnimation: Animation
    lateinit var imageToggleHideAnmation: Animation


    private var menuShowShadow: Boolean = false
    private var menuShadowColor: Int = 0
    private var menuShadowRadius = dpToPx(4f)
    private var menuShadowXOffset = dpToPx(1f)
    private var menuShadowYOffset = dpToPx(3f)

    private var menuColorNormal: Int
    private var menuColorPressed: Int
    private var menuColorRipple: Int


    private var menuRevealColor: Int
    private var showReveal: Boolean = false


    private var menuLabelText: String = ""
    private var usingMenuLabel: Boolean = false
    private var menuFabSize: Int


    private var isMenuOpening: Boolean = false
    private var menuOpened: Boolean = false

    lateinit var imageToggle: ImageView
    private var icon: Drawable? = null
    private var iconAnimated = true

    private var iconToggleSet: AnimatorSet? = null


    private var openType: Int = 0
    private var openDirection: Int = 0

    private var labelsPosition: Int
    private var labelsMargin = dpToPx(8f).toInt()
    private var labelsVerticalOffset = dpToPx(0f).toInt()

    private val labelsStyle: Int
    private val labelsContext: Context
    private val labelsShowAnimation: Int
    private val labelsHideAnimation: Int
    private var labelsShowShadow: Boolean = false
    private var labelsColorNormal: Int
    private var labelsColorPressed: Int
    private var labelsColorRipple: Int
    private var labelsCornerRadius = dpToPx(3f).toInt()
    private var labelsTextColor: ColorStateList
    private var labelsTextSize: Float

    private var labelsPaddingTop = dpToPx(4f).toInt()
    private var labelsPaddingRight = dpToPx(8f).toInt()
    private var labelsPaddingBottom = dpToPx(4f).toInt()
    private var labelsPaddingLeft = dpToPx(8f).toInt()


    private val openAnimatorSet = AnimatorSet()
    private val closeAnimatorSet = AnimatorSet()

    private var buttonCount: Int = 0
    private var buttonSpacing = dpToPx(8f).toInt()

    private var isAnimated = true


    private var closeOnTouchOutside: Boolean = true

    private var bgColor: Int


    private lateinit var showBackgroundAnimator: ValueAnimator
    private lateinit var hideBackgroundAnimator: ValueAnimator


    private val mUiHandler = Handler()
    private val animationDelayPerItem: Long = 50L


    var toggleListener: MenuInterface? = null
    private var maxButtonWidth: Int = 0


    // Progress
    private var progressWidth: Int = dpToPx(6f).toInt()
    private var progressColor: Int
    private var progressBackgroundColor: Int
    private var showProgressBackground: Boolean = false


    var angleOffset: Float = 0f

    init {


        val a = context.obtainStyledAttributes(attrs, R.styleable.LinearFabMenu, 0, 0)


        labelsPosition =
            a.getInt(R.styleable.LinearFabMenu_menu_labels_position, LABEL_POSITION_LEFT)
        val labelShowAnim =
            if (labelsPosition == LABEL_POSITION_LEFT) R.anim.fab_slide_in_from_right else R.anim.fab_slide_in_from_left
        val labelHideAnim =
            if (labelsPosition == LABEL_POSITION_LEFT) R.anim.fab_slide_out_to_right else R.anim.fab_slide_out_to_left

        labelsShowAnimation =
            a.getResourceId(R.styleable.LinearFabMenu_menu_labels_showAnimation, labelShowAnim)
        labelsHideAnimation =
            a.getResourceId(R.styleable.LinearFabMenu_menu_labels_hideAnimation, labelHideAnim)
        labelsTextColor = a.getColorStateList(R.styleable.LinearFabMenu_menu_labels_textColor)
            ?: ColorStateList.valueOf(Color.WHITE)
        labelsTextSize = a.getDimension(
            R.styleable.LinearFabMenu_menu_labels_textSize,
            resources.getDimension(R.dimen.labels_text_size)
        )

        labelsColorNormal = a.getColor(
            R.styleable.LinearFabMenu_menu_labels_colorNormal,
            getColor(R.color.fab_label_normal)
        )
        labelsColorPressed = a.getColor(
            R.styleable.LinearFabMenu_menu_labels_colorPressed,
            getColor(R.color.fab_label_pressed)
        )
        labelsColorRipple = a.getColor(
            R.styleable.LinearFabMenu_menu_labels_colorRipple,
            getColor(R.color.fab_label_ripple)
        )


        progressColor = a.getColor(
            R.styleable.LinearFabMenu_menu_progress_color,
            getColor(R.color.fab_progress_color)
        )
        progressBackgroundColor = a.getColor(
            R.styleable.LinearFabMenu_menu_progress_backgroundColor,
            getColor(R.color.fab_background_color)
        )
        showProgressBackground =
            a.getBoolean(R.styleable.LinearFabMenu_menu_progress_showBackground, true)

        progressWidth = a.getDimension(
            R.styleable.LinearFabMenu_menu_progress_width,
            resources.getDimension(R.dimen.fab_progress_width)
        ).toInt()


        menuRevealColor = a.getColor(
            R.styleable.LinearFabMenu_menu_reveal_color,
            getColor(R.color.fab_reveal_color)
        )
        showReveal = a.getBoolean(R.styleable.LinearFabMenu_menu_do_reveal, false)

        menuShowShadow = a.getBoolean(R.styleable.LinearFabMenu_menu_showShadow, true)
        menuShadowColor = a.getColor(
            R.styleable.LinearFabMenu_menu_shadowColor,
            getColor(R.color.fab_shadow_color)
        )

        menuShadowRadius =
            a.getDimension(R.styleable.LinearFabMenu_menu_shadowRadius, menuShadowRadius)
        menuShadowXOffset =
            a.getDimension(R.styleable.LinearFabMenu_menu_shadowXOffset, menuShadowXOffset)
        menuShadowYOffset =
            a.getDimension(R.styleable.LinearFabMenu_menu_shadowYOffset, menuShadowYOffset)

        menuColorNormal = a.getColor(
            R.styleable.LinearFabMenu_menu_colorNormal,
            getColor(R.color.fab_color_normal)
        )
        menuColorPressed = a.getColor(
            R.styleable.LinearFabMenu_menu_colorPressed,
            getColor(R.color.fab_color_pressed)
        )
        menuColorRipple = a.getColor(
            R.styleable.LinearFabMenu_menu_colorRipple,
            getColor(R.color.fab_color_ripple)
        )

        icon = a.getDrawable(R.styleable.LinearFabMenu_menu_icon)
        if (icon == null) {
            icon = ContextCompat.getDrawable(context, R.drawable.ic_add)
        }

        menuFabSize =
            a.getInt(R.styleable.LinearFabMenu_menu_fab_size, FloatingActionButton.SIZE_NORMAL)
        labelsStyle = a.getResourceId(R.styleable.LinearFabMenu_menu_labels_style, 0)

        openDirection = a.getInt(R.styleable.LinearFabMenu_menu_openDirection, OPEN_UP)
        openType = a.getInt(R.styleable.LinearFabMenu_menu_type, TYPE_LINEAR)
        bgColor = a.getColor(R.styleable.LinearFabMenu_menu_backgroundColor, Color.TRANSPARENT)



        angleOffset = (a.getFloat(
            R.styleable.LinearFabMenu_menu_radial_angleOffset,
            0f
        ) * Math.PI / 180f).toFloat()

        if (a.hasValue(R.styleable.LinearFabMenu_menu_fab_label)) {
            val label = a.getString(R.styleable.LinearFabMenu_menu_fab_label)
            if (label != null) {
                usingMenuLabel = true
                menuLabelText = label
            }
        }


        labelsContext = ContextThemeWrapper(context, labelsStyle)

        initBackgroundDimAnimation()
        createMenuButton()
        initMenuButtonAnimations()

        a.recycle()
    }

    private fun initMenuButtonAnimations() {
        setShowAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_scale_up))
        imageToggleShowAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_up)

        setHideAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_scale_down))
        imageToggleHideAnmation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_down)
    }

    private fun initBackgroundDimAnimation() {
        val maxAlpha = Color.alpha(bgColor)
        val red = Color.red(bgColor)
        val green = Color.green(bgColor)
        val blue = Color.blue(bgColor)

        showBackgroundAnimator = ValueAnimator.ofInt(0, maxAlpha)
        showBackgroundAnimator.duration = ANIMATION_DURATION
        showBackgroundAnimator.addUpdateListener {
            val alpha = it.animatedValue as Int
            setBackgroundColor(Color.argb(alpha, red, green, blue))
        }
        hideBackgroundAnimator = ValueAnimator.ofInt(maxAlpha, 0)
        hideBackgroundAnimator.duration = ANIMATION_DURATION
        hideBackgroundAnimator.addUpdateListener {
            val alpha = it.animatedValue as Int
            setBackgroundColor(Color.argb(alpha, red, green, blue))
        }
    }

    private fun isBackgroundEnabled(): Boolean = bgColor != Color.TRANSPARENT

    private fun createMenuButton() {
        menuButton = FloatingActionButton(context)

        menuButton.showShadow = menuShowShadow
        if (menuShowShadow) {
            menuButton.shadowRadius = menuShadowRadius.toInt()
            menuButton.shadowXOffset = menuShadowXOffset.toInt()
            menuButton.shadowYOffset = menuShadowYOffset.toInt()
        }
        menuButton.setColors(menuColorNormal, menuColorPressed, menuColorRipple)
        menuButton.shadowColor = menuShadowColor
        menuButton.fabSize = menuFabSize
        menuButton.updateBackground()
        menuButton.setLabelText(menuLabelText)

        menuButton.setProgressColors(progressColor, progressBackgroundColor)
        menuButton.setProgressWidth(progressWidth)
        menuButton.setShowProgressBackground(showProgressBackground)


        //Menu button is actually two views, a FAB and an image on top
        imageToggle = ImageView(context)
        imageToggle.setImageDrawable(icon)

        addView(menuButton, generateDefaultLayoutParams())
        addView(imageToggle)

        createDefaultIconAnimation()

    }

    private fun createDefaultIconAnimation() {
        val collapseAngle: Float
        val expandAngle: Float
        if (openDirection == OPEN_UP) {
            collapseAngle =
                if (labelsPosition == LABEL_POSITION_LEFT) OPENED_PLUS_ROTATION_LEFT else OPENED_PLUS_ROTATION_RIGHT
            expandAngle =
                if (labelsPosition == LABEL_POSITION_LEFT) OPENED_PLUS_ROTATION_LEFT else OPENED_PLUS_ROTATION_RIGHT
        } else {
            collapseAngle =
                if (labelsPosition == LABEL_POSITION_LEFT) OPENED_PLUS_ROTATION_RIGHT else OPENED_PLUS_ROTATION_LEFT
            expandAngle =
                if (labelsPosition == LABEL_POSITION_LEFT) OPENED_PLUS_ROTATION_RIGHT else OPENED_PLUS_ROTATION_LEFT
        }

        val collapseAnimator = ObjectAnimator.ofFloat(
            imageToggle,
            "rotation",
            collapseAngle,
            CLOSED_PLUS_ROTATION
        )


        val expandAnimator = ObjectAnimator.ofFloat(
            imageToggle,
            "rotation",
            CLOSED_PLUS_ROTATION,
            expandAngle
        )

        openAnimatorSet.play(expandAnimator)
        closeAnimatorSet.play(collapseAnimator)

        openAnimatorSet.interpolator = openInterpolator
        closeAnimatorSet.interpolator = closeInterpolator

        openAnimatorSet.duration = ANIMATION_DURATION
        closeAnimatorSet.duration = ANIMATION_DURATION

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        var width: Int
        var height: Int
        maxButtonWidth = 0

        measureChildWithMargins(imageToggle, widthMeasureSpec, 0, heightMeasureSpec, 0)
        /**
         * we do one pass to calculate the max width of those buttons so that all adjust to it
         */
        for (i in 0 until buttonCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE || child == imageToggle) continue
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
            maxButtonWidth = max(maxButtonWidth, child.measuredWidth)
        }


        val dimen =
            if (openType == TYPE_LINEAR) linearDimension(widthMeasureSpec, heightMeasureSpec)
            else radialDimension(widthMeasureSpec, heightMeasureSpec)

        width = dimen.width
        height = dimen.height

        if (layoutParams.width == LayoutParams.MATCH_PARENT) {
            width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        }
        if (layoutParams.height == LayoutParams.MATCH_PARENT) {
            height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        }


        setMeasuredDimension(width, height)
    }

    data class Dimen(val width: Int, val height: Int)

    private fun radialDimension(widthMeasureSpec: Int, heightMeasureSpec: Int): Dimen {
        var width: Int
        var height: Int

        val buttonsHorizontalCenter = 0
        val verticalCenter = 0

        measureChildWithMargins(menuButton, widthMeasureSpec, 0, heightMeasureSpec, 0)

        var minX: Int = -(menuButton.measuredWidth / 2)
        var maxX: Int = (menuButton.measuredWidth / 2)
        var minY: Int = -(menuButton.measuredHeight / 2)
        var maxY: Int = (menuButton.measuredHeight / 2)




        for (i in 0 until buttonCount) {
            val child = getChildAt(i)
            //gone elements or imagetoggle do not count for the total width/height
            if (child.visibility == View.GONE || child == imageToggle || child == menuButton) continue

            val fab = child as? FloatingActionButton
            if (fab != null) {
                measureChildWithMargins(fab, widthMeasureSpec, 0, heightMeasureSpec, 0)
                val childPosForRadial = getChildPosForRadial(
                    fab,
                    buttonsHorizontalCenter,
                    verticalCenter,
                    i
                )


                minX = min(minX, childPosForRadial.x - fab.measuredWidth / 2)
                maxX = max(maxX, childPosForRadial.x + fab.measuredWidth / 2)
                minY = min(minY, childPosForRadial.y - fab.measuredHeight / 2)
                maxY = max(maxY, childPosForRadial.y + fab.measuredHeight / 2)
            }
        }

        width = maxX - minX
        height = maxY - minY
        return Dimen(adjustForOvershoot(width), adjustForOvershoot(height))
    }

    private fun linearDimension(widthMeasureSpec: Int, heightMeasureSpec: Int): Dimen {
        var width: Int
        var height = 0
        var maxLabelWidth = 0
        for (i in 0 until buttonCount) {
            var usedWidth = 0
            val child = getChildAt(i)

            if (child.visibility == View.GONE || child == imageToggle) continue

            usedWidth += child.measuredWidth
            height += child.measuredHeight

            val label = child.getTag(R.id.fab_label) as? Label
            if (label != null) {
                val labelOffset =
                    (maxButtonWidth - child.measuredWidth) / (if (usingMenuLabel) 1 else 2)
                val labelUsedWidth =
                    child.measuredWidth + label.calculateShadowWidth() + labelsMargin + labelOffset
                measureChildWithMargins(
                    label,
                    widthMeasureSpec,
                    labelUsedWidth,
                    heightMeasureSpec,
                    0
                )
                usedWidth += label.measuredWidth
                maxLabelWidth = max(maxLabelWidth, usedWidth + labelOffset)
            }
        }


        width = max(maxButtonWidth, maxLabelWidth + labelsMargin) + paddingLeft + paddingRight

        height += buttonSpacing * (buttonCount - 1) + paddingTop + paddingBottom
        height = adjustForOvershoot(height)

        if (layoutParams.width == LayoutParams.MATCH_PARENT) {
            width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        }
        if (layoutParams.height == LayoutParams.MATCH_PARENT) {
            height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        }

        return Dimen(width, height)
    }

    fun isRadial() = openType == TYPE_RADIAL
    fun isLinear() = openType == TYPE_LINEAR

    fun isLabelsLeft() = labelsPosition == LABEL_POSITION_LEFT
    fun isLabelsRight() = labelsPosition == LABEL_POSITION_RIGHT

    fun isOpenUp() = openDirection == OPEN_UP
    fun isOpenDown() = openDirection == OPEN_DOWN

    /**
     * Determines the position of the FAB button.
     * If labels are to the right and its a linear FAB, then FAB is on the left edge
     * If labels are to the left and its a linear FAB, then FAB is on the right edge
     * If it's a radial FAB, then FAB is on the center
     */
    fun calculateButtonsCenter(): Int {
        return if (isRadial()) {
            (right - left) / 2
        } else {
            if (isLabelsLeft()) {
                right - left - maxButtonWidth / 2 - paddingRight
            } else {
                maxButtonWidth / 2 + paddingLeft
            }
        }
    }


    fun getChildPosForLinear(
        fab: FloatingActionButton,
        horizontalCenter: Int,
        nextY: Int
    ): Point {
        val childX = horizontalCenter - fab.measuredWidth / 2
        val childY = if (isOpenUp()) nextY - fab.measuredHeight - buttonSpacing else nextY
        return Point(childX, childY)
    }


    private fun getCircleRadius() = menuButton.measuredWidth + buttonSpacing

    fun getChildPosForRadial(
        fab: FloatingActionButton,
        horizontalCenter: Int,
        verticalCenter: Int,
        itemPos: Int
    ): Point {

        //Count items that are FABs and substract 1 (because of the main menu)
        val submenuItems = children.filter { it is FloatingActionButton }.count() - 1
        val circleRadius = getCircleRadius()
        val angle = ((Math.PI / submenuItems) * (itemPos)) + angleOffset
        val childX =
            (horizontalCenter) - (circleRadius * cos(angle)).toInt() - fab.measuredWidth / 2
        val childY =
            (verticalCenter) - (circleRadius * sin(angle)).toInt() - fab.measuredHeight / 2
        return Point(childX, childY)
    }


    private fun layoutLinear(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val areLabelsToTheLeft = labelsPosition == LABEL_POSITION_LEFT
        val openUp = openDirection == OPEN_UP

        val buttonsHorizontalCenter = calculateButtonsCenter()

        val menuButtonTop = positionMenuButtonForLinear(openUp, top, bottom)
        val menuButtonLeft = buttonsHorizontalCenter - menuButton.measuredWidth / 2

        menuButton.layout(
            menuButtonLeft,
            menuButtonTop,
            menuButtonLeft + menuButton.measuredWidth,
            menuButtonTop + menuButton.measuredHeight
        )

        val imageLeft = buttonsHorizontalCenter - imageToggle.measuredWidth / 2
        val imageTop =
            menuButtonTop + menuButton.measuredHeight / 2 - imageToggle.measuredHeight / 2


        imageToggle.layout(
            imageLeft,
            imageTop,
            imageLeft + imageToggle.measuredWidth,
            imageTop + imageToggle.measuredHeight
        )


        var nextY =
            if (openUp) menuButtonTop + menuButton.measuredHeight + buttonSpacing else menuButtonTop
        menuButtonTop + menuButton.measuredHeight / 2


        for (i in buttonCount - 1 downTo 0) {
            val child = getChildAt(i)

            if (child == imageToggle) continue

            val fab = child as FloatingActionButton
            if (fab.visibility == View.GONE) continue

            var childX: Int
            var childY: Int
            val pos = getChildPosForLinear(fab, buttonsHorizontalCenter, nextY)
            childX = pos.x
            childY = pos.y

            if (fab != menuButton) {
                fab.layout(
                    childX, childY, childX + fab.measuredWidth,
                    childY + fab.measuredHeight
                )

                if (!isMenuOpening) {
                    fab.hide(false)
                }
            }

            /**
             * Lays out the labels.
             */

            val label = fab.getTag(R.id.fab_label) as? Label
            if (label != null) {
                val labelsOffset =
                    (if (usingMenuLabel) maxButtonWidth / 2 else fab.measuredWidth / 2) + labelsMargin
                val labelXNearButton =
                    if (areLabelsToTheLeft) buttonsHorizontalCenter - labelsOffset else buttonsHorizontalCenter + labelsOffset
                val labelXAwayFromButton =
                    if (areLabelsToTheLeft) labelXNearButton - label.measuredWidth else labelXNearButton + label.measuredWidth

                var labelLeft: Int
                var labelRight: Int

                if (openType == TYPE_LINEAR) {
                    labelLeft = if (areLabelsToTheLeft) labelXAwayFromButton else labelXNearButton
                    labelRight = if (areLabelsToTheLeft) labelXNearButton else labelXAwayFromButton
                } else {
                    labelLeft =
                        if (areLabelsToTheLeft) childX - label.measuredWidth - labelsMargin else childX + fab.measuredWidth + labelsMargin
                    labelRight = labelLeft + label.measuredWidth
                }

                val labelTop =
                    childY - labelsVerticalOffset + (fab.measuredHeight - label.measuredHeight) / 2

                label.layout(labelLeft, labelTop, labelRight, labelTop + label.measuredHeight)

                if (!isMenuOpening) {
                    label.visibility = View.INVISIBLE
                }
            }

            nextY =
                if (openUp) childY - buttonSpacing else childY + child.measuredHeight + buttonSpacing
        }
    }

    private fun layoutRadial(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        //Center of the view
        val menuButtonX = (right - left) / 2
        val menuButtonY = (bottom - top) / 2

        val imageLeft = menuButtonX - imageToggle.measuredWidth / 2
        val imageTop = menuButtonY - imageToggle.measuredHeight / 2


        val childPositions = mutableMapOf<Int, Point>()


        var minX = menuButtonX - menuButton.measuredWidth / 2
        var maxX = menuButtonX + menuButton.measuredWidth / 2
        var minY = menuButtonY - menuButton.measuredHeight / 2
        var maxY = menuButtonY + menuButton.measuredHeight / 2
        for (i in buttonCount - 1 downTo 0) {
            val child = getChildAt(i)
            if (child == imageToggle || child.visibility == View.GONE || child == menuButton) continue
            val fab = child as FloatingActionButton
            val pos = getChildPosForRadial(fab, menuButtonX, menuButtonY, i)


            minX = min(minX, pos.x)
            maxX = max(maxX, pos.x)
            minY = min(minY, pos.y)
            maxY = max(maxY, pos.y)


            childPositions[i] = pos
            if (!isMenuOpening) {
                fab.hide(false)
            }
        }

        var offsetMaxX = (menuButtonX + measuredWidth / 2) + maxX
        var offsetMinX = (menuButtonX - measuredWidth / 2) - minX
//
        var offsetMaxY = (menuButtonY + measuredHeight / 2) + maxY
        var offsetMinY = (menuButtonY - measuredHeight / 2) - minY
        val offsetY = (offsetMinY * 1.3).toInt()
        val offsetX = (offsetMinX * 1.3).toInt()
        for (i in buttonCount - 1 downTo 0) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE || child == menuButton || child == imageToggle) continue
            val fab = child as FloatingActionButton
            val point = childPositions[i]
            if (point != null) {
                fab.layout(
                    point.x + offsetX, point.y + offsetY, point.x + offsetX + fab.measuredWidth,
                    point.y + offsetY + fab.measuredHeight
                )
            }

        }

        menuButton.layout(
            menuButtonX - menuButton.measuredWidth / 2 + offsetX,
            menuButtonY - menuButton.measuredHeight / 2 + offsetY,
            menuButtonX + menuButton.measuredWidth / 2 + offsetX,
            menuButtonY + menuButton.measuredHeight / 2 + offsetY
        )

        imageToggle.layout(
            imageLeft + offsetX,
            imageTop + offsetY,
            imageLeft + imageToggle.measuredWidth + offsetX,
            imageTop + imageToggle.measuredHeight + offsetY
        )


    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (isLinear()) {
            layoutLinear(changed, left, top, right, bottom)
        } else {
            layoutRadial(changed, left, top, right, bottom)
        }

    }

    private fun positionMenuButtonForRadial(
        top: Int,
        bottom: Int
    ): Int {
        return (bottom - top)
    }

    private fun positionMenuButtonForLinear(
        openUp: Boolean,
        top: Int,
        bottom: Int
    ) = if (openUp) bottom - top - menuButton.measuredHeight - paddingBottom else paddingTop

    private fun adjustForOvershoot(dimension: Int): Int = (dimension * 1.1).toInt()

    override fun onFinishInflate() {
        super.onFinishInflate()
        bringChildToFront(menuButton)
        bringChildToFront(imageToggle)
        buttonCount = childCount
        createLabels()
    }

    private fun createLabels() {
        for (i in 0 until buttonCount) {

            val child = getChildAt(i)
            if (child == imageToggle) continue

            val fab = child as FloatingActionButton

            if (fab.getTag(R.id.fab_label) != null) continue

            addLabel(fab)

            if (fab == menuButton) {
                menuButton.setOnClickListener {
                    toggle(isAnimated)
                }
            }

        }

    }

    private fun addLabel(fab: FloatingActionButton) {
        val text = fab.getLabelText()

        if (text.isEmpty()) return

        val label = Label(labelsContext)
        label.isClickable = true
        label.setFab(fab)
        label.setShowAnimation(AnimationUtils.loadAnimation(context, labelsShowAnimation))
        label.setHideAnimation(AnimationUtils.loadAnimation(context, labelsHideAnimation))

        if (labelsStyle > 0) {
            TextViewCompat.setTextAppearance(label, labelsStyle)
            label.setShowShadow(false)
            label.setUsingStyle(false)
        } else {
            label.setColors(labelsColorNormal, labelsColorPressed, labelsColorRipple)
            label.setShowShadow(labelsShowShadow)
            label.setCornerRadius(labelsCornerRadius)
            label.updateBackground()

            label.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelsTextSize)
            label.setTextColor(labelsTextColor)

            var left = labelsPaddingLeft
            var top = labelsPaddingTop
            if (labelsShowShadow) {
                left += fab.shadowRadius + abs(fab.shadowXOffset)
                top += fab.shadowRadius + abs(fab.shadowYOffset)
            }

            label.setPadding(left, top, labelsPaddingLeft, labelsPaddingTop)


        }

        label.text = text
        label.setOnClickListener(fab.getOnClickListener())

        addView(label)
        fab.setTag(R.id.fab_label, label)

    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams =
        MarginLayoutParams(context, attrs)

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams = MarginLayoutParams(p)

    override fun generateDefaultLayoutParams(): LayoutParams =
        MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT, MarginLayoutParams.WRAP_CONTENT)

    override fun checkLayoutParams(p: LayoutParams?): Boolean = p is MarginLayoutParams

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (closeOnTouchOutside) {
            var handled = false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handled = isOpened()
                }
                MotionEvent.ACTION_UP -> {
                    close(isAnimated)
                    handled = true
                }
            }
            return handled
        }

        return super.onTouchEvent(event)
    }

    private fun toggle(animated: Boolean) {
        if (isOpened()) {
            close(animated)
        } else {
            open(animated)
        }
    }

    fun isOpened(): Boolean = menuOpened

    public fun open(animated: Boolean) {
        if (!isOpened()) {

            if (showReveal) {
                menuButton.doReveal(menuRevealColor)
            }
            if (isBackgroundEnabled()) {
                showBackgroundAnimator.start()
            }

            if (iconAnimated) {
                if (iconToggleSet != null) {
                    iconToggleSet?.start()
                } else {
                    closeAnimatorSet.cancel()
                    openAnimatorSet.start()
                }
            }

            var delay = 0L
            var counter = 0
            isMenuOpening = true
            for (i in childCount - 1 downTo 0) {
                val child = getChildAt(i)
                if (child is FloatingActionButton && child.visibility != View.GONE) {
                    counter++

                    mUiHandler.postDelayed(object : Runnable {
                        override fun run() {
                            if (isOpened()) return

                            if (child != menuButton) {
                                child.show(animated)
                            }

                            val label = child.getTag(R.id.fab_label) as? Label
                            if (label != null && label.isHandleVisibilityChanges()) {
                                label.show(animated)
                            }
                        }
                    }, delay)
                    delay += animationDelayPerItem
                }
            }

            mUiHandler.postDelayed({
                menuOpened = true
                toggleListener?.menuOpen()

            }, ++counter * animationDelayPerItem)

        }
    }

    public fun close(animated: Boolean) {
        if (isOpened()) {

            if (showReveal) {
                menuButton.undoReveal()
            }


            if (isBackgroundEnabled()) {
                hideBackgroundAnimator.start()
            }

            if (iconAnimated) {
                if (iconToggleSet != null) {
                    iconToggleSet?.start()
                } else {
                    closeAnimatorSet.start()
                    openAnimatorSet.cancel()
                }
            }

            var delay = 0L
            var counter = 0
            isMenuOpening = false
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child is FloatingActionButton && child.visibility != View.GONE) {
                    counter++

                    mUiHandler.postDelayed(object : Runnable {
                        override fun run() {
                            if (!isOpened()) return

                            if (child != menuButton) {
                                child.hide(animated)
                            }

                            val label = child.getTag(R.id.fab_label) as? Label
                            if (label != null && label.isHandleVisibilityChanges()) {
                                label.hide(animated)
                            }
                        }
                    }, delay)
                    delay += animationDelayPerItem
                }
            }

            mUiHandler.postDelayed({
                menuOpened = false
                toggleListener?.menuClose()
            }, ++counter * animationDelayPerItem)

        }
    }

    private fun setShowAnimation(showAnimation: Animation) {
        menuButtonShowAnimation = showAnimation
        menuButton.showAnimation = showAnimation
    }

    private fun setHideAnimation(hideAnimation: Animation) {
        menuButtonHideAnimation = hideAnimation
        menuButton.hideAnimation = hideAnimation
    }

    fun setClosedOnTouchOutside(close: Boolean) {
        closeOnTouchOutside = close
    }

    fun showProgressBar() {
        menuButton.showProgressBar()
    }

    fun hideProgress() {
        menuButton.hideProgress()
    }


    companion object {

        const val TYPE_LINEAR = 0
        const val TYPE_RADIAL = 1

        const val OPEN_UP = 0
        const val OPEN_DOWN = 1


        const val LABEL_POSITION_LEFT = 0
        const val LABEL_POSITION_RIGHT = 1


        const val OPENED_PLUS_ROTATION_LEFT = -90f - 45f
        const val OPENED_PLUS_ROTATION_RIGHT = 90f + 45f


        const val CLOSED_PLUS_ROTATION = 0f

        const val ANIMATION_DURATION = 300L
    }


}
package com.kelmer.android.fabmenu

import android.os.Build
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import kotlin.math.round

object Util {
    fun View.dpToPx(dp: Float) = round(dp * context.resources.displayMetrics.density)

    fun isJellyBean() =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
    fun isLollipop() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    fun View.getColor(@ColorRes color: Int) = ContextCompat.getColor(context, color)
}
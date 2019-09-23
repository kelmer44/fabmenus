package com.kelmer.android.fabmenu.fab

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Animation
import android.widget.TextView

class Label @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {


    fun calculateShadowWidth(): Int = 0
    fun setFab(fab: FloatingActionButton) {

    }

    fun setShowAnimation(loadAnimation: Animation) {

    }

    fun setHideAnimation(loadAnimation: Animation) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setShowShadow(b: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setUsingStyle(b: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun updateBackground() {


    }

    fun setColors(labelsColorNormal: Int, labelsColorPressed: Int, labelsColorRipple: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setCornerRadius(labelCornerRadius: Any) {

    }


}
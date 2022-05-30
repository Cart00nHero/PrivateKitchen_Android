package com.cartoonhero.privatekitchen_android.props.inlineTools

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.annotation.RequiresApi
import kotlin.math.roundToInt

// Extension method to convert pixels to dp
fun Int.toDp(context: Context):Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,this.toFloat(),context.resources.displayMetrics
).toInt()
fun Float.toSp(context: Context):Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP, this,context.resources.displayMetrics
)
// extension function to convert dp to equivalent pixels
fun Int.dpToPixels(context: Context):Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics
).toInt()

// extension property to get display metrics instance
val Activity.displayMetrics: DisplayMetrics
    get() {
        // display metrics is a structure describing general information
        // about a display, such as its size, density, and font scaling
        val displayMetrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= 30) {
            display.apply {
                this?.getRealMetrics(displayMetrics)
            }
        } else {
            // getMetrics() method was deprecated in api level 30
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        return displayMetrics
    }
val Activity.currentWindowMetrics: WindowMetrics
    @RequiresApi(Build.VERSION_CODES.R)
    get() {
        return getSystemService(WindowManager::class.java).currentWindowMetrics
    }

// extension property to get screen width and height in dp
val Activity.screenSizeInDp: Point
    get() {
        val point = Point()
        displayMetrics.apply {
            // screen width in dp
            point.x = (widthPixels / density).roundToInt()

            // screen height in dp
            point.y = (heightPixels / density).roundToInt()
        }
        return point
    }
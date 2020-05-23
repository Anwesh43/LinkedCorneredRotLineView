package com.anwesh.uiprojects.corneredrotlineview

/**
 * Created by anweshmishra on 24/05/20.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Canvas
import android.app.Activity
import android.content.Context

val nodes : Int = 5
val lines : Int = 2
val upLines : Int = 2
val scGap : Float = 0.02f
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#4CAF50")
val backColor : Int = Color.parseColor("#BDBDBD")
val rotDeg : Float = 90f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawCorneredRotLine(i : Int, scale : Float, size : Float, paint : Paint) {
    val sci : Float = scale.divideScale(i, lines)
    save()
    translate(size, size)
    rotate(rotDeg * sci)
    drawLine(0f, 0f, -size, 0f, paint)
    restore()
}

fun Canvas.drawCorneredRotLines(scale : Float, size : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    for (i in 0..(upLines - 1)) {
        val sfi : Float = sf.divideScale(i, upLines)
        for (j in 0..(lines - 1)) {
            drawCorneredRotLine(i, sfi, size, paint)
        }
    }
}

fun Canvas.drawCRLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(w / 2, gap * (i + 1))
    drawCorneredRotLines(scale, size, paint)
    restore()
}

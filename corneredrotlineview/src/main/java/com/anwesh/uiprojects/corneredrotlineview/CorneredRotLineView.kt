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
val scGap : Float = 0.04f / (2 * upLines * lines)
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#4CAF50")
val backColor : Int = Color.parseColor("#BDBDBD")
val rotDeg : Float = 90f
val delay : Long = 10

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawCorneredRotLine(i : Int, scale : Float, size : Float, paint : Paint) {
    val sci : Float = scale.divideScale(i, lines)
    save()
    scale(1f - 2 * i, 1f)
    translate(size, size)
    rotate(rotDeg * sci)
    drawLine(0f, 0f, -size, 0f, paint)
    restore()
}

fun Canvas.drawCorneredRotLines(scale : Float, size : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, 2)
    val sf2 : Float = sf.divideScale(1, 2)
    save()
    rotate(rotDeg * sf2)
    for (i in 0..(upLines - 1)) {
        save()
        scale(1f, 1f - 2 * i)
        val sfi : Float = sf1.divideScale(i, upLines)
        for (j in 0..(lines - 1)) {
            drawCorneredRotLine(j, sfi, size, paint)
        }
        restore()
    }
    restore()
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
    translate(w / 2, gap * (i + 1) - gap / 2)
    drawLine(-w / 2, gap / 2, w / 2, gap / 2, paint)
    drawCorneredRotLines(scale, size, paint)
    restore()
}

class CorneredRotLineView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += dir * scGap
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class CRLNode(var i : Int, val state : State = State()) {

        private var next : CRLNode? = null
        private var prev : CRLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = CRLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawCRLNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CRLNode {
            var curr : CRLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class CorneredRotLine(var i : Int) {

        private val root : CRLNode = CRLNode(0)
        private var curr : CRLNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : CorneredRotLineView) {

        private val animator : Animator = Animator(view)
        private val crl : CorneredRotLine = CorneredRotLine(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            crl.draw(canvas, paint)
            animator.animate {
                crl.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            crl.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : CorneredRotLineView {
            val view : CorneredRotLineView = CorneredRotLineView(activity)
            activity.setContentView(view)
            return view
        }
    }
}
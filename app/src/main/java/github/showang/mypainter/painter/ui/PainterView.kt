package github.showang.mypainter.painter.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View

class PainterView(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    private var mStrategy: PaintingStrategy? = null

    fun update(strategy: PaintingStrategy) {
        mStrategy = strategy
    }

    fun updateBackground(bitmap: Bitmap?) {
        bitmap ?: return
        background = BitmapDrawable(context.resources, bitmap)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.run {
            when (action) {
                ACTION_DOWN -> mStrategy?.onActionDown(x, y)
                ACTION_MOVE -> mStrategy?.onActionMove(x, y)
                ACTION_UP, ACTION_CANCEL -> mStrategy?.onActionUp(x, y)
                else -> null
            }?.run {
                invalidate()
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val mode = mStrategy ?: return
        canvas?.let(mode::onDraw)
    }
}
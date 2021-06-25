package github.showang.mypainter.painter.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.annotation.ColorInt
import github.showang.mypainter.painter.model.PathShape

class PencilStrategy(
    private val store: PaintingStrategy.ShapeStore,
    @ColorInt inColor: Int = Color.BLACK
) : PaintingStrategy {

    private var paint: Paint = Paint().apply {
        color = inColor
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    private lateinit var currentPath: Path

    fun updateColor(inColor: Int) {
        paint = Paint().apply {
            color = inColor
            style = Paint.Style.STROKE
            strokeWidth = 10f
        }
    }

    override fun onDraw(canvas: Canvas) {
        store.availableShapes.forEach { it.draw(canvas) }
    }

    override fun onActionDown(x: Float, y: Float) {
        currentPath = Path()
        currentPath.moveTo(x, y)
        store.queueShape(PathShape(currentPath, paint))
    }

    override fun onActionMove(x: Float, y: Float) {
        currentPath.lineTo(x, y)
    }

    override fun onActionUp(x: Float, y: Float) {
        currentPath.lineTo(x, y)
    }
}
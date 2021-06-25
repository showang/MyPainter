package github.showang.mypainter.painter.ui

import android.graphics.Canvas
import github.showang.mypainter.painter.model.Shape

interface PaintingStrategy {

    fun onDraw(canvas: Canvas)

    fun onActionDown(x: Float, y: Float)

    fun onActionMove(x: Float, y: Float)

    fun onActionUp(x: Float, y: Float)

    interface ShapeStore {
        val availableShapes: List<Shape>

        fun queueShape(shape: Shape)
    }
}
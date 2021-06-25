package github.showang.mypainter.painter.ui

import android.graphics.*
import androidx.annotation.ColorInt
import github.showang.mypainter.painter.model.PathShape
import github.showang.mypainter.painter.presenter.PaintingPresenter

sealed class PaintingMode {

    abstract fun onDraw(canvas: Canvas)

    abstract fun onActionDown(x: Float, y: Float)

    abstract fun onActionMove(x: Float, y: Float)

    abstract fun onActionUp(x: Float, y: Float)

    class Pencil(
        private val presenter: PaintingPresenter,
        @ColorInt inColor: Int = Color.BLACK
    ) : PaintingMode() {

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
            presenter.currentShapes.forEach { it.draw(canvas) }
        }

        override fun onActionDown(x: Float, y: Float) {
            currentPath = Path()
            currentPath.moveTo(x, y)
            presenter.onShapeDrawn(PathShape(currentPath, paint))
        }

        override fun onActionMove(x: Float, y: Float) {
            currentPath.lineTo(x, y)
        }

        override fun onActionUp(x: Float, y: Float) {
            currentPath.lineTo(x, y)
        }
    }

    class Eraser(private val presenter: PaintingPresenter, private val densityRatio: Float = 3f) :
        PaintingMode() {

        private var size = 30f * densityRatio
        private var halfSize = size / 2
        private var eraseArea: RectF? = null

        private lateinit var currentPath: Path

        private var paint: Paint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            style = Paint.Style.STROKE
            color = Color.TRANSPARENT
            strokeWidth = size
        }

        private val areaPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.BLACK
            strokeWidth = 2f * densityRatio
        }

        override fun onDraw(canvas: Canvas) {
            presenter.currentShapes.forEach { it.draw(canvas) }
            eraseArea?.let {
                canvas.drawRect(it, areaPaint)
            }

        }

        override fun onActionDown(x: Float, y: Float) {
            currentPath = Path()
            currentPath.moveTo(x, y)
            eraseArea = RectF(x - halfSize, y - halfSize, x + halfSize, y + halfSize)
            presenter.onShapeDrawn(PathShape(currentPath, paint))
        }

        override fun onActionMove(x: Float, y: Float) {
            currentPath.lineTo(x, y)
            eraseArea = RectF(x - halfSize, y - halfSize, x + halfSize, y + halfSize)
        }

        override fun onActionUp(x: Float, y: Float) {
            eraseArea = null
        }

    }

}
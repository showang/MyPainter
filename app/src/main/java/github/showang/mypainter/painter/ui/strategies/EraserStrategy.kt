package github.showang.mypainter.painter.ui.strategies

import android.graphics.*
import github.showang.mypainter.painter.model.PathShape
import github.showang.mypainter.painter.ui.PaintingStrategy

class EraserStrategy(
    private val store: PaintingStrategy.ShapeStore,
    private val densityRatio: Float = 3f
) : PaintingStrategy {
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
        store.availableShapes.forEach { it.draw(canvas) }
        eraseArea?.let {
            canvas.drawRect(it, areaPaint)
        }

    }

    override fun onActionDown(x: Float, y: Float) {
        currentPath = Path()
        currentPath.moveTo(x, y)
        eraseArea = RectF(x - halfSize, y - halfSize, x + halfSize, y + halfSize)
        store.queueShape(PathShape(currentPath, paint))
    }

    override fun onActionMove(x: Float, y: Float) {
        currentPath.lineTo(x, y)
        eraseArea = RectF(x - halfSize, y - halfSize, x + halfSize, y + halfSize)
    }

    override fun onActionUp(x: Float, y: Float) {
        eraseArea = null
    }

}
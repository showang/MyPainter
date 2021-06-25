package github.showang.mypainter.painter.presenter

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.view.drawToBitmap
import github.showang.mypainter.painter.ImageRepository
import github.showang.mypainter.painter.model.Shape
import github.showang.mypainter.painter.ui.PainterView
import github.showang.mypainter.painter.ui.PaintingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaintingPresenter(
    private val imageRepository: ImageRepository
) {

    private var currentDrawingPosition = -1
    private val shapes: MutableList<Shape> = mutableListOf()

    private var mSelectedColor: Int = Color.BLACK
        set(value) {
            field = value
            (mPaintingMode as? PaintingMode.Pencil)?.updateColor(value)
            delegate?.onColorUpdated()
        }
    private var mPaintingMode: PaintingMode = PaintingMode.Pencil(this)
        set(value) {
            field = value
            delegate?.onPaintingModeUpdated(value)
        }

    var delegate: ViewDelegate? = null
        set(value) {
            field = value
            value?.onPaintingModeUpdated(mPaintingMode)
            CoroutineScope(IO).launch {
                val bitmap = loadBasedBitmap()
                withContext(Main) {
                    delegate?.onLoadSnapshotCompleted(bitmap)
                }
            }

        }

    val currentShapes: List<Shape>
        get() = if (currentDrawingPosition >= 0)
            shapes.subList(0, currentDrawingPosition + 1)
        else emptyList()
    val selectedColor: Int get() = mSelectedColor

    fun onShapeDrawn(newShape: Shape) {
        if (currentDrawingPosition != shapes.lastIndex) {
            shapes.removeAll(shapes.subList(currentDrawingPosition + 1, shapes.lastIndex + 1))
        }
        shapes.add(newShape)
        currentDrawingPosition++
        onShapeQueueChanged()
    }

    fun updateMode(mode: Mode) {
        mPaintingMode = when (mode) {
            Mode.Pencil -> PaintingMode.Pencil(this, selectedColor)
            Mode.Eraser -> PaintingMode.Eraser(this)
        }
    }

    fun undo() {
        if (currentDrawingPosition == -1) return
        currentDrawingPosition--
        delegate?.onShapesUpdated(currentShapes)
        onShapeQueueChanged()
    }

    fun redo() {
        if (currentDrawingPosition < shapes.lastIndex) {
            currentDrawingPosition++
        }
        delegate?.onShapesUpdated(currentShapes)
        onShapeQueueChanged()
    }


    fun selectColor(colorInt: Int) {
        mSelectedColor = colorInt
    }

    fun saveSnapshot(view: PainterView) = CoroutineScope(IO).launch {
        val bitmap = withContext(Main) { view.drawToBitmap() }
        try {
            imageRepository.saveImage(bitmap)
            withContext(Main) { delegate?.onSaveCompleted(true) }
        } catch (e: Throwable) {
            withContext(Main) { delegate?.onSaveCompleted(false) }
        }
    }

    private fun onShapeQueueChanged() {
        delegate?.onShapeStackChanged(
            currentDrawingPosition == shapes.lastIndex,
            currentDrawingPosition < 0
        )
    }

    private fun loadBasedBitmap(): Bitmap? {
        return imageRepository.loadSnapshot()
    }

    interface ViewDelegate {
        fun onPaintingModeUpdated(mode: PaintingMode)

        fun onShapesUpdated(shapes: List<Shape>)

        fun onShapeStackChanged(isTopMost: Boolean, isBottom: Boolean)

        fun onLoadSnapshotCompleted(bitmap: Bitmap?)

        fun onColorUpdated()

        fun onSaveCompleted(isSuccess: Boolean)
    }

    enum class Mode {
        Pencil, Eraser
    }
}
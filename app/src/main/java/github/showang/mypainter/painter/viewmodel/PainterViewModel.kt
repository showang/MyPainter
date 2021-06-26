package github.showang.mypainter.painter.viewmodel

import android.graphics.Color
import androidx.core.view.drawToBitmap
import github.showang.mypainter.painter.model.ShapeStoreManager
import github.showang.mypainter.painter.repo.ImageRepository
import github.showang.mypainter.painter.ui.PainterView
import github.showang.mypainter.transtate.TranstateViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PainterViewModel(
    private val imageRepository: ImageRepository,
    private val shapeStoreManager: ShapeStoreManager
) : TranstateViewModel<PainterViewState>(), ShapeStoreManager.Listener {

    private val supportedColorList = listOf(
        Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE
    )

    override val initState: PainterViewState =
        Mode.Pencil(supportedColorList.first()).let { initMode ->
            val selectedColor: Int = initMode.colorInt
            PainterViewState.Initializing(
                initMode,
                supportedColorList,
                selectedColor
            )
        }

    override var lastState: PainterViewState = initState

    init {
        CoroutineScope(IO).launch {
            val bitmap = imageRepository.loadSnapshot()
            startTransform(PainterViewEvent.SnapshotLoaded(bitmap))
        }
        shapeStoreManager.addListener(this)
    }

    fun pencilMode(selectedColor: Int) = updateMode(Mode.Pencil(selectedColor))
    fun eraserMode() = updateMode(Mode.Eraser())

    private fun updateMode(mode: Mode) {
        startTransform(PainterViewEvent.ChangeMode(mode))
    }

    fun undo() {
        if (shapeStoreManager.undo()) {
            redrawView()
        }
    }

    fun redo() {
        if (shapeStoreManager.redo()) {
            redrawView()
        }
    }

    private fun redrawView() {
        startTransform(
            PainterViewEvent.ShapeQueueChanged(
                !shapeStoreManager.isBottom,
                !shapeStoreManager.isTopMost
            )
        )
        startTransform(PainterViewEvent.PerformRedraw())
    }

    fun changeColor(colorInt: Int, preColor: Int) {
        val unSelectedIndex = supportedColorList.indexOf(preColor)
        val selectedIndex = supportedColorList.indexOf(colorInt)

        startTransform(PainterViewEvent.ColorChanged(colorInt, selectedIndex, unSelectedIndex))
    }

    fun saveSnapshot(view: PainterView) = CoroutineScope(IO).launch {
        val bitmap = withContext(Main) { view.drawToBitmap() }
        startTransform(
            PainterViewEvent.SaveSnapshotCompleted(
                try {
                    imageRepository.saveImage(bitmap)
                    true
                } catch (e: Throwable) {
                    false
                }
            )
        )
    }

    override fun onQueueChanged() {
        startTransform(
            PainterViewEvent.ShapeQueueChanged(
                !shapeStoreManager.isBottom,
                !shapeStoreManager.isTopMost
            )
        )
    }

    sealed class Mode {
        class Pencil(color: Int, val size: Float = 30f) : Mode() {
            private var mColor = color
            val colorInt get() = mColor
            fun update(color: Int) {
                mColor = color
            }
        }

        class Eraser(val size: Float = 30f) : Mode()
    }
}
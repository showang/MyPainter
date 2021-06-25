package github.showang.mypainter.painter.viewmodel

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.view.drawToBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import github.showang.mypainter.painter.ImageRepository
import github.showang.mypainter.painter.model.ShapeStoreManager
import github.showang.mypainter.painter.ui.PainterView
import github.showang.mypainter.painter.ui.PaintingStrategy
import github.showang.mypainter.painter.ui.PencilStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaintingViewModel(
    private val imageRepository: ImageRepository,
    private val paintingModeFactory: PaintingModeFactory,
    private val shapeStoreManager: ShapeStoreManager

) : ViewModel(), ShapeStoreManager.Listener {

    val strategyLiveData: LiveData<PaintingStrategy> get() = mStrategyLiveData
    val modeLiveData: LiveData<Mode> get() = mModeLiveData
    val baseBitmapLiveData: LiveData<Bitmap?> get() = mBaseBitmapLiveData
    val supportedColorList = listOf(
        Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE
    )

    var selectedColor: Int = supportedColorList.first()
        private set
    private val mModeLiveData = MutableLiveData<Mode>(Mode.Pencil(selectedColor))
    private val mStrategyLiveData =
        MutableLiveData(paintingModeFactory.create(mModeLiveData.value!!))
    private val mBaseBitmapLiveData = MutableLiveData<Bitmap?>(null)

    var delegate: ViewDelegate? = null

    init {
        CoroutineScope(IO).launch {
            val bitmap = loadBasedBitmap()
            mBaseBitmapLiveData.postValue(bitmap)
        }
        shapeStoreManager.addListener(this)
    }

    fun pencilMode() = updateMode(Mode.Pencil(selectedColor))
    fun eraserMode() = updateMode(Mode.Eraser)

    private fun updateMode(mode: Mode) {
        mStrategyLiveData.postValue(paintingModeFactory.create(mode))
        mModeLiveData.postValue(mode)
    }

    fun undo() {
        if (shapeStoreManager.undo()) {
            delegate?.requirePainterDraw()
        }
    }

    fun redo() {
        if (shapeStoreManager.redo()) {
            delegate?.requirePainterDraw()
        }
    }

    fun selectColor(colorInt: Int) {
        val unSelectedIndex = supportedColorList.indexOf(selectedColor)
        val selectedIndex = supportedColorList.indexOf(colorInt)
        selectedColor = colorInt
        (mStrategyLiveData.value as? PencilStrategy)?.updateColor(colorInt)
        delegate?.onColorUpdated(selectedIndex, unSelectedIndex)
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

    override fun onQueueChanged() {
        delegate?.onShapeQueueChanged(shapeStoreManager.isTopMost, shapeStoreManager.isBottom)
    }

    private fun loadBasedBitmap(): Bitmap? {
        return imageRepository.loadSnapshot()
    }

    interface ViewDelegate { //Note: something I feel not suitable with live data
        fun requirePainterDraw()
        fun onShapeQueueChanged(isTopMost: Boolean, isBottom: Boolean)
        fun onColorUpdated(selectedIndex: Int, unSelectedIndex: Int)
        fun onSaveCompleted(isSuccess: Boolean)
    }

    sealed class Mode {
        class Pencil(val colorInt: Int) : Mode()

        object Eraser : Mode()
    }
}
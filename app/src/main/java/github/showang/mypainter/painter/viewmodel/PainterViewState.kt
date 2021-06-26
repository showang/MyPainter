package github.showang.mypainter.painter.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import github.showang.mypainter.transtate.core.Transform
import github.showang.mypainter.transtate.core.ViewEvent
import github.showang.mypainter.transtate.core.ViewState

sealed class PainterViewState : ViewState() {

    class Initializing(
        val currentMode: PainterViewModel.Mode,
        val supportedColors: List<Int>,
        val selectedColor: Int
    ) : PainterViewState() {

        override fun transform(
            byEvent: ViewEvent,
            liveData: MutableLiveData<Transform>
        ): ViewState {
            return when (byEvent) {
                is PainterViewEvent.SnapshotLoaded -> {
                    Log.e("Initializing", "SnapshotLoaded")
                    Drawing(currentMode, supportedColors, selectedColor, byEvent.bitmap)
                }
                else -> throw IllegalStateException("Should load snapshot first.")
            }
        }

    }

    class Drawing(
        val currentMode: PainterViewModel.Mode,
        val supportedColors: List<Int>,
        val selectedColor: Int,
        val baseBitmap: Bitmap?,
        val canUndo: Boolean = false,
        val canRedo: Boolean = false
    ) : PainterViewState() {

        override fun transform(
            byEvent: ViewEvent,
            liveData: MutableLiveData<Transform>
        ): ViewState {
            return when (byEvent) {
                is PainterViewEvent.ChangeMode -> Drawing(
                    byEvent.newMode,
                    supportedColors,
                    selectedColor,
                    baseBitmap,
                    canUndo,
                    canRedo
                )
                is PainterViewEvent.ColorChanged -> Drawing(
                    currentMode.apply {
                        if (this is PainterViewModel.Mode.Pencil) update(byEvent.selectedColor)
                    },
                    supportedColors,
                    byEvent.selectedColor,
                    baseBitmap,
                    canUndo,
                    canRedo
                )
                is PainterViewEvent.ShapeQueueChanged -> Drawing(
                    currentMode,
                    supportedColors,
                    selectedColor,
                    baseBitmap,
                    byEvent.canUndo,
                    byEvent.canRedo
                )
                is PainterViewEvent.PerformRedraw,
                is PainterViewEvent.SaveSnapshotCompleted -> this
                else -> throw IllegalStateException("Unknown state")
            }
        }
    }
}

sealed class PainterViewEvent : ViewEvent() {

    class ColorChanged(val selectedColor: Int, val selectedIndex: Int, val unSelectedIndex: Int) :
        PainterViewEvent()

    class SnapshotLoaded(val bitmap: Bitmap?) : PainterViewEvent()

    class SaveSnapshotCompleted(val isSuccess: Boolean) : PainterViewEvent()

    class PerformRedraw() : PainterViewEvent()

    class ShapeQueueChanged(val canUndo: Boolean, val canRedo: Boolean) : PainterViewEvent()

    class ChangeMode(val newMode: PainterViewModel.Mode) :
        PainterViewEvent()
}
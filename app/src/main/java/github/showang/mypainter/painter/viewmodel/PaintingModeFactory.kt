package github.showang.mypainter.painter.viewmodel

import github.showang.mypainter.painter.model.ShapeStoreManager
import github.showang.mypainter.painter.ui.EraserStrategy
import github.showang.mypainter.painter.ui.PaintingStrategy
import github.showang.mypainter.painter.ui.PencilStrategy

class PaintingModeFactory(private val shapeStoreManager: ShapeStoreManager) {

    fun create(mode: PaintingViewModel.Mode): PaintingStrategy {
        return when (mode) {
            is PaintingViewModel.Mode.Pencil -> PencilStrategy(shapeStoreManager, mode.colorInt)
            is PaintingViewModel.Mode.Eraser -> EraserStrategy(shapeStoreManager)
        }

    }

}
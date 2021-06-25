package github.showang.mypainter.painter.ui

import github.showang.mypainter.painter.model.ShapeStoreManager
import github.showang.mypainter.painter.ui.strategies.EraserStrategy
import github.showang.mypainter.painter.ui.strategies.PencilStrategy
import github.showang.mypainter.painter.viewmodel.PainterViewModel

class PaintingStrategyFactory(private val shapeStoreManager: ShapeStoreManager) {

    fun create(mode: PainterViewModel.Mode): PaintingStrategy {
        return when (mode) {
            is PainterViewModel.Mode.Pencil -> PencilStrategy(shapeStoreManager, mode.colorInt)
            is PainterViewModel.Mode.Eraser -> EraserStrategy(shapeStoreManager)
        }

    }

}
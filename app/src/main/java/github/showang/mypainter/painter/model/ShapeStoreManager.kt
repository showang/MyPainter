package github.showang.mypainter.painter.model

import github.showang.mypainter.painter.ui.PaintingStrategy

class ShapeStoreManager : PaintingStrategy.ShapeStore {

    private val shapes: MutableList<Shape> = mutableListOf()
    private var currentDrawingPosition = -1

    override val availableShapes: List<Shape>
        get() = if (currentDrawingPosition >= 0)
            shapes.subList(0, currentDrawingPosition + 1)
        else emptyList()

    private val listeners: MutableList<Listener> = mutableListOf()

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    override fun queueShape(shape: Shape) {
        if (currentDrawingPosition != shapes.lastIndex) {
            shapes.removeAll(shapes.subList(currentDrawingPosition + 1, shapes.lastIndex + 1))
        }
        shapes.add(shape)
        currentDrawingPosition++
        onChanged()
    }

    fun undo(): Boolean {
        if (currentDrawingPosition == -1) return false
        currentDrawingPosition--
        onChanged()
        return true
    }

    fun redo(): Boolean {
        return if (currentDrawingPosition < shapes.lastIndex) {
            currentDrawingPosition++
            onChanged()
            true
        } else false
    }

    val isTopMost: Boolean get() = currentDrawingPosition == shapes.lastIndex
    val isBottom: Boolean get() = currentDrawingPosition < 0

    private fun onChanged() {
        listeners.forEach { it.onQueueChanged() }
    }

    interface Listener {
        fun onQueueChanged()
    }

}
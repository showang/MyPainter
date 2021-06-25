package github.showang.mypainter.painter.model

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ShapeStoreManagerTest {

    private lateinit var manager: ShapeStoreManager
    private lateinit var mockListener: ShapeStoreManager.Listener

    private val shapeA: Shape = mockk()
    private val shapeB: Shape = mockk()

    @Before
    fun setup() {
        mockListener = mockk()
        manager = ShapeStoreManager()
        manager.addListener(mockListener)
    }

    @Test
    fun testUndo_initState() {
        assert(!manager.undo())
        assert(manager.isBottom)
    }

    @Test
    fun testUndo() {
        every { mockListener.onQueueChanged() } returns Unit

        for (i in 0 until 10) {
            manager.queueShape(if (i % 2 == 0) shapeA else shapeB)
        }
        verify(exactly = 10) { mockListener.onQueueChanged() }

        assert(manager.undo())

        verify(exactly = 11) { mockListener.onQueueChanged() }
        assert(manager.availableShapes.size == 9)
        assert(manager.availableShapes.last() == shapeA)
        assert(!manager.isBottom)

        assert(manager.undo())

        verify(exactly = 12) { mockListener.onQueueChanged() }
        assert(manager.availableShapes.size == 8)
        assert(manager.availableShapes.last() == shapeB)

        (0 until 8).forEach { _ ->
            assert(manager.undo())
        }

        assert(!manager.undo())

        verify(exactly = 20) { mockListener.onQueueChanged() }
        assert(manager.availableShapes.isEmpty())
        assert(manager.isBottom)

    }

    @Test
    fun testRedo_initState() {
        assert(!manager.redo())
        assert(manager.isTopMost)
    }

    @Test
    fun testRedo() {
        every { mockListener.onQueueChanged() } returns Unit
        for (i in 0 until 10) {
            manager.queueShape(if (i % 2 == 0) shapeA else shapeB)
        }
        verify(exactly = 10) { mockListener.onQueueChanged() }
        assert(manager.isTopMost)

        (0 until 5).forEach { _ ->
            assert(manager.undo())
        }
        assert(!manager.isTopMost)

        assert(manager.redo())

        verify(exactly = 16) { mockListener.onQueueChanged() }
        assert(manager.availableShapes.size == 6)
        assert(manager.availableShapes.last() == shapeB)
        assert(!manager.isTopMost)

        (0 until 4).forEach { _ ->
            assert(manager.redo())
        }
        assert(!manager.redo())

        verify(exactly = 20) { mockListener.onQueueChanged() }
        assert(manager.availableShapes.size == 10)
        assert(manager.availableShapes.last() == shapeB)
        assert(manager.isTopMost)
    }

}
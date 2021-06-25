package github.showang.mypainter

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import github.showang.mypainter.databinding.ActivityMainBinding
import github.showang.mypainter.painter.model.Shape
import github.showang.mypainter.painter.presenter.PaintingPresenter
import github.showang.mypainter.painter.ui.PaintingMode
import me.showang.recyct.RecyctAdapter
import me.showang.recyct.RecyctViewHolder
import me.showang.recyct.items.RecyctItemBase
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity(), PaintingPresenter.ViewDelegate {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val presenter: PaintingPresenter by inject()
    private val modeButtons: MutableList<ImageView> = mutableListOf()
    private var adapter: RecyctAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.apply {
            initPainterView()
            initColorRecyclerView()
            initModeSelectedButtons()
            initActionButtons()
        }.root)
        presenter.delegate = this
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_actions, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        presenter.saveSnapshot(binding.painterView)
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        presenter.delegate = null
        super.onDestroy()
    }

    private fun ActivityMainBinding.initPainterView() {
        painterView.init(PaintingMode.Pencil(presenter))
    }

    private fun ActivityMainBinding.initColorRecyclerView() {
        colorRecycler.adapter = RecyctAdapter(supportedColorList).apply {
            register(ColorItem(presenter::selectedColor)) { data, _ ->
                val colorInt = (data as? Int) ?: return@register
                presenter.selectColor(colorInt)
            }
            adapter = this
        }
    }

    private fun ActivityMainBinding.initModeSelectedButtons() {
        listOf(
            pencilModeButton.apply {
                setOnClickListener {
                    presenter.updateMode(PaintingPresenter.Mode.Pencil)
                }
            },
            eraserModeButton.apply {
                setOnClickListener {
                    presenter.updateMode(PaintingPresenter.Mode.Eraser)
                }
            }
        ).let(modeButtons::addAll)
    }

    private fun ActivityMainBinding.initActionButtons() {
        undoButton.setOnClickListener { presenter.undo() }
        redoButton.setOnClickListener { presenter.redo() }
        updateActionButtons()
    }

    private fun ActivityMainBinding.updateActionButtons(
        isTopMost: Boolean = true,
        isBottom: Boolean = true
    ) {
        redoButton.run { setColorFilter(if (isTopMost) disableColor else enableColor) }
        undoButton.run { setColorFilter(if (isBottom) disableColor else enableColor) }
    }

    override fun onShapesUpdated(shapes: List<Shape>) = binding.run {
        painterView.invalidate()
    }

    override fun onShapeStackChanged(isTopMost: Boolean, isBottom: Boolean) = binding.run {
        updateActionButtons(isTopMost, isBottom)
    }

    override fun onLoadSnapshotCompleted(bitmap: Bitmap?) = binding.run {
        painterView.updateBackground(bitmap)
    }

    override fun onPaintingModeUpdated(mode: PaintingMode) = binding.run {
        painterView.init(mode)
        updateModeButtons(mode)
    }

    override fun onColorUpdated() {
        adapter?.notifyDataSetChanged()
    }

    override fun onSaveCompleted(isSuccess: Boolean) {
        Toast.makeText(
            this,
            if (isSuccess) "Save snapshot success." else "Save snapshot fail.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun ActivityMainBinding.updateModeButtons(currentMode: PaintingMode) {
        modeButtons.forEach {
            it.setColorFilter(disableColor)
        }
        when (currentMode) {
            is PaintingMode.Eraser -> eraserModeButton.clearColorFilter()
            is PaintingMode.Pencil -> pencilModeButton.clearColorFilter()
        }
    }

    class ColorItem(private val selectedColor: () -> Int) : RecyctItemBase() {
        override fun create(inflater: LayoutInflater, parent: ViewGroup) =
            object : RecyctViewHolder(inflater, parent, R.layout.item_color_selector) {
                private val normalBg =
                    ColorDrawable(ContextCompat.getColor(context, android.R.color.darker_gray))
                private val selectedBg = ColorDrawable(Color.YELLOW)
                private val colorImage: ImageView by id(R.id.colorImage)
                override fun bind(data: Any, atIndex: Int) {
                    val colorInt = (data as? Int) ?: return
                    colorInt.let(::ColorDrawable).apply(colorImage::setImageDrawable)
                    colorImage.background =
                        normalBg.takeIf { selectedColor() != colorInt } ?: selectedBg
                }
            }
    }

    private val enableColor by lazy {
        ContextCompat.getColor(this, android.R.color.holo_green_light)
    }
    private val disableColor by lazy {
        ContextCompat.getColor(this, android.R.color.darker_gray)
    }

    companion object {
        private val supportedColorList = listOf(
            Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE
        )
    }
}
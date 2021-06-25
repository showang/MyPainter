package github.showang.mypainter

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
import github.showang.mypainter.painter.viewmodel.PaintingViewModel
import me.showang.recyct.RecyctAdapter
import me.showang.recyct.RecyctViewHolder
import me.showang.recyct.items.RecyctItemBase
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), PaintingViewModel.ViewDelegate {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val lifecycleOwner by lazy { { lifecycle } }
    private val viewModel: PaintingViewModel by viewModel()
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
        viewModel.delegate = this
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_actions, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.saveSnapshot(binding.painterView)
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        viewModel.delegate = null
        super.onDestroy()
    }

    private fun ActivityMainBinding.initPainterView() {
        viewModel.strategyLiveData.observe(lifecycleOwner, painterView::update)
        viewModel.baseBitmapLiveData.observe(lifecycleOwner, painterView::updateBackground)
    }

    private fun ActivityMainBinding.initColorRecyclerView() {
        colorRecycler.adapter = RecyctAdapter(viewModel.supportedColorList).apply {
            register(ColorItem(viewModel::selectedColor)) { data, _ ->
                val colorInt = (data as? Int) ?: return@register
                viewModel.selectColor(colorInt)
            }
            adapter = this
        }
    }

    private fun ActivityMainBinding.initModeSelectedButtons() {
        listOf(
            pencilModeButton.apply {
                setOnClickListener { viewModel.pencilMode() }
            },
            eraserModeButton.apply {
                setOnClickListener { viewModel.eraserMode() }
            }
        ).let(modeButtons::addAll)
        viewModel.modeLiveData.observe(lifecycleOwner, ::updateModeButtons)
    }

    private fun ActivityMainBinding.initActionButtons() {
        undoButton.setOnClickListener { viewModel.undo() }
        redoButton.setOnClickListener { viewModel.redo() }
        updateActionButtons()
    }

    private fun ActivityMainBinding.updateActionButtons(
        isTopMost: Boolean = true,
        isBottom: Boolean = true
    ) {
        redoButton.run { setColorFilter(if (isTopMost) disableColor else enableColor) }
        undoButton.run { setColorFilter(if (isBottom) disableColor else enableColor) }
    }

    override fun requirePainterDraw() {
        binding.painterView.invalidate()
    }

    override fun onShapeQueueChanged(isTopMost: Boolean, isBottom: Boolean) = binding.run {
        updateActionButtons(isTopMost, isBottom)
    }

    override fun onColorUpdated(selectedIndex: Int, unSelectedIndex: Int) {
        adapter?.notifyItemChanged(selectedIndex)
        adapter?.notifyItemChanged(unSelectedIndex)
    }

    override fun onSaveCompleted(isSuccess: Boolean) {
        Toast.makeText(
            this,
            if (isSuccess) "Save snapshot success." else "Save snapshot fail.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun updateModeButtons(mode: PaintingViewModel.Mode) = binding.run {
        modeButtons.forEach {
            it.setColorFilter(disableColor)
        }
        when (mode) {
            is PaintingViewModel.Mode.Eraser -> eraserModeButton.clearColorFilter()
            is PaintingViewModel.Mode.Pencil -> pencilModeButton.clearColorFilter()
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
}
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
import github.showang.mypainter.painter.ui.PaintingStrategyFactory
import github.showang.mypainter.painter.viewmodel.PainterViewEvent
import github.showang.mypainter.painter.viewmodel.PainterViewModel
import github.showang.mypainter.painter.viewmodel.PainterViewState
import github.showang.mypainter.transtate.core.Transform
import github.showang.mypainter.transtate.core.ViewState
import me.showang.recyct.RecyctAdapter
import me.showang.recyct.RecyctViewHolder
import me.showang.recyct.items.RecyctItemBase
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val lifecycleOwner by lazy { { lifecycle } }
    private val strategyFactory: PaintingStrategyFactory by inject()
    private val viewModel: PainterViewModel by viewModel()
    private val modeButtons: MutableList<ImageView> = mutableListOf()
    private var adapter: RecyctAdapter? = null

    private var selectedColor: Int = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewModel.observeTransformation(lifecycleOwner, ::initViewBy, ::updateViewBy)
    }

    private fun initViewBy(state: ViewState) {
        when (state) {
            is PainterViewState.Initializing -> {
                selectedColor = state.selectedColor
                binding.initAll(state.currentMode, state.supportedColors)
            }
            is PainterViewState.Drawing -> { //Note: This case can't be reached, just works when using Fragment (ViewDestroyed).
                selectedColor = state.selectedColor
                binding.run {
                    initAll(state.currentMode, state.supportedColors)
                    painterView.updateBackground(state.baseBitmap)
                }
            }
        }
    }

    private fun updateViewBy(transform: Transform) {
        when (val event = transform.byEvent) {
            is PainterViewEvent.ChangeMode -> event.run {
                binding.painterView.update(newMode)
                updateModeButtons(newMode)
            }
            is PainterViewEvent.ShapeQueueChanged ->
                updateActionButtons(!event.canRedo, !event.canUndo)
            is PainterViewEvent.PerformRedraw -> binding.painterView.invalidate()
            is PainterViewEvent.ColorChanged -> {
                selectedColor = event.selectedColor
                (transform.newState as? PainterViewState.Drawing)?.run {
                    binding.painterView.update(currentMode)
                }
                adapter?.run {
                    notifyItemChanged(event.selectedIndex)
                    notifyItemChanged(event.unSelectedIndex)
                }
            }
            is PainterViewEvent.SaveSnapshotCompleted -> onSaveCompleted(event.isSuccess)
            is PainterViewEvent.SnapshotLoaded -> binding.painterView.updateBackground(event.bitmap)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_actions, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.saveSnapshot(binding.painterView)
        return super.onOptionsItemSelected(item)
    }

    private fun ActivityMainBinding.initAll(
        mode: PainterViewModel.Mode,
        supportedColors: List<Int>
    ) {
        initPainterView(mode)
        initColorRecyclerView(supportedColors)
        initModeSelectedButtons()
        initActionButtons()
        updateModeButtons(mode)
    }

    private fun ActivityMainBinding.initPainterView(mode: PainterViewModel.Mode?) {
        painterView.init(strategyFactory)
        mode?.let(painterView::update)
    }

    private fun ActivityMainBinding.initColorRecyclerView(colors: List<Int>) {
        colorRecycler.adapter = RecyctAdapter(colors).apply {
            register(ColorItem { selectedColor }) { data, _ ->
                val colorInt = (data as? Int) ?: return@register
                viewModel.changeColor(colorInt, selectedColor)
            }
            adapter = this
        }
    }

    private fun ActivityMainBinding.initModeSelectedButtons() {
        listOf(
            pencilModeButton.apply {
                setOnClickListener { viewModel.pencilMode(selectedColor) }
            },
            eraserModeButton.apply {
                setOnClickListener { viewModel.eraserMode() }
            }
        ).let(modeButtons::addAll)
    }

    private fun ActivityMainBinding.initActionButtons() {
        undoButton.setOnClickListener { viewModel.undo() }
        redoButton.setOnClickListener { viewModel.redo() }
        updateActionButtons(isTopMost = true, isBottom = true)
    }

    private fun updateActionButtons(isTopMost: Boolean, isBottom: Boolean) = binding.run {
        redoButton.run { setColorFilter(if (isTopMost) disableColor else enableColor) }
        undoButton.run { setColorFilter(if (isBottom) disableColor else enableColor) }
    }

    private fun onSaveCompleted(isSuccess: Boolean) {
        Toast.makeText(
            this,
            if (isSuccess) "Save snapshot success." else "Save snapshot fail.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun updateModeButtons(mode: PainterViewModel.Mode) = binding.run {
        modeButtons.forEach { it.setColorFilter(disableColor) }
        when (mode) {
            is PainterViewModel.Mode.Eraser -> eraserModeButton.clearColorFilter()
            is PainterViewModel.Mode.Pencil -> pencilModeButton.clearColorFilter()
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
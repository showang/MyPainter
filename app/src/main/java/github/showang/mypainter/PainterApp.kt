package github.showang.mypainter

import android.app.Application
import github.showang.mypainter.painter.repo.ImageRepository
import github.showang.mypainter.painter.model.ShapeStoreManager
import github.showang.mypainter.painter.ui.PaintingStrategyFactory
import github.showang.mypainter.painter.viewmodel.PainterViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class PainterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PainterApp)
            modules(listOf(
                module {
                    single { ImageRepository(get()) }
                    single { PaintingStrategyFactory(get()) }
                    single { ShapeStoreManager() }

                    viewModel { PainterViewModel(get(), get()) }
                }
            ))
        }
    }
}
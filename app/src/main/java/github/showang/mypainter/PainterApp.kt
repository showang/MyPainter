package github.showang.mypainter

import android.app.Application
import github.showang.mypainter.painter.ImageRepository
import github.showang.mypainter.painter.model.ShapeStoreManager
import github.showang.mypainter.painter.viewmodel.PaintingModeFactory
import github.showang.mypainter.painter.viewmodel.PaintingViewModel
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
                    single { PaintingModeFactory(get()) }
                    single { ShapeStoreManager() }

                    viewModel { PaintingViewModel(get(), get(), get()) }
                }
            ))
        }
    }
}
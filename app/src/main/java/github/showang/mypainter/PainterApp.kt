package github.showang.mypainter

import android.app.Application
import github.showang.mypainter.painter.ImageRepository
import github.showang.mypainter.painter.presenter.PaintingPresenter
import org.koin.android.ext.koin.androidContext
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
                    single { PaintingPresenter(get()) }
                }
            ))
        }
    }
}
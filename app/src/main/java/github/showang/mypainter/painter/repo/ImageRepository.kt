package github.showang.mypainter.painter.repo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

class ImageRepository(context: Context) {

    private val imageFolder = context.filesDir
    private val cacheFile by lazy { File(imageFolder.path + "/lastSnapshot.png") }

    fun saveImage(bitmap: Bitmap) {
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
        cacheFile.createNewFile()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(cacheFile))
    }

    fun loadSnapshot(): Bitmap? {
        return cacheFile.path.let(BitmapFactory::decodeFile)
    }
}
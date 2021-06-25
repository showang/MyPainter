package github.showang.mypainter.painter.model

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

class PathShape(
    private var path: Path,
    private var paint: Paint
) : Shape {

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }
}
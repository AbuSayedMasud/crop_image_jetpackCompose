package com.fintechhub.crop.libraryCore.crop

import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.fintechhub.crop.libraryCore.images.getDecodeParams
import com.fintechhub.crop.libraryCore.utils.atOrigin
import com.fintechhub.crop.libraryCore.utils.coerceAtMost
import com.fintechhub.crop.libraryCore.utils.roundUp
import com.fintechhub.crop.libraryCore.utils.times
import com.fintechhub.crop.libraryCore.utils.viewMat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Creates an [ImageBitmap] using the parameters in [cropState].
 * If [maxSize] is not null, the result will be scaled down to match it.
 * Returns null if the image could not be created.
 */
suspend fun CropState.createResult(
    maxSize: IntSize?
): ImageBitmap? = withContext(Dispatchers.Default) {
    runCatching { doCreateResult(maxSize) }
        .onFailure { it.printStackTrace() }
        .getOrNull()
}

suspend fun CropState.doCreateResult(maxSize: IntSize?): ImageBitmap? {
    val finalSize = region.size
        .coerceAtMost(maxSize?.toSize())
        .roundUp()
    val result = ImageBitmap(finalSize.width, finalSize.height)
    val canvas = Canvas(result)
    val viewMat = viewMat()
    viewMat.snapFit(region, finalSize.toSize().toRect())
    val imgMat = transform.asMatrix(src.size)
    val totalMat = imgMat * viewMat.matrix

    canvas.clipPath(shape.asPath(region.atOrigin(maxSize)))
    canvas.concat(totalMat)
    val inParams = getDecodeParams(view = finalSize, img = src.size, totalMat)
        ?: return null
    val decoded = src.open(inParams) ?: return null
    val paint = Paint().apply { filterQuality = FilterQuality.High }
    canvas.drawImageRect(
        image = decoded.bmp, paint = paint,
        dstOffset = decoded.params.subset.topLeft,
        dstSize = decoded.params.subset.size,
    )
    return result
}

package com.fintechhub.crop.libraryCore.utils

import androidx.compose.ui.unit.IntSize

class ZoomLimits(
    originalImageSize: IntSize,
    view: IntSize,
    val minCropSize: Int = 50, // TODO make this a CropperStyle parameter
) {
    val maxFactor: Float

    init {
        val viewAspectRatio = view.width.toFloat() / view.height
        val imageAspectRatio = originalImageSize.width.toFloat() / originalImageSize.height

        val fullImageSize = if (viewAspectRatio > imageAspectRatio) {
            originalImageSize.height.coerceAtLeast(view.height)
        } else {
            originalImageSize.width.coerceAtLeast(view.width)
        }.toFloat()

        maxFactor = fullImageSize / minCropSize
    }
}
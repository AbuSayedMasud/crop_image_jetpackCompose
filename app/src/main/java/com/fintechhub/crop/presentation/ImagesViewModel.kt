package com.fintechhub.crop.presentation

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintechhub.crop.libraryCore.crop.CropError
import com.fintechhub.crop.libraryCore.crop.CropResult
import com.fintechhub.crop.libraryCore.crop.imageCropper
import com.fintechhub.crop.libraryCore.crop.cropSrc
import com.fintechhub.crop.libraryCore.images.ImageSrc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImagesViewModel : ViewModel() {
    val imageCropper = imageCropper()
    private val _selectedImage = MutableStateFlow<ImageBitmap?>(null)
    val selectedImage = _selectedImage.asStateFlow()
    private val _cropError = MutableStateFlow<CropError?>(null)
    val cropError = _cropError.asStateFlow()

    fun cropErrorShown() {
        _cropError.value = null
    }

    fun setSelectedImage(imageSrc: ImageSrc) {
        viewModelScope.launch {
            when(val result = imageCropper.cropSrc(imageSrc)) {
                CropResult.Cancelled -> {}
                is CropError -> _cropError.value = result
                is CropResult.Success -> {
                    _selectedImage.value = result.bitmap
                }
            }
        }
    }
}
package com.fintechhub.crop.ui.t
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.fintechhub.crop.libraryCore.crop.CropError
import com.fintechhub.crop.libraryCore.crop.CropResult
import com.fintechhub.crop.libraryCore.crop.cropSrc
import com.fintechhub.crop.libraryCore.crop.rememberImageCropper
import com.fintechhub.crop.rememberImagePicker
import kotlinx.coroutines.launch

@Composable
fun SimpleDemo(modifier: Modifier = Modifier) {
    val imageCropper = rememberImageCropper()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var error by remember { mutableStateOf<CropError?>(null) }
    val imagePicker = rememberImagePicker(onImage = { imageSrc ->
        scope.launch {
            when (val result = imageCropper.cropSrc(imageSrc)) {
                CropResult.Cancelled -> {}
                is CropError -> error = result
                is CropResult.Success -> {
                    selectedImage = result.bitmap
                }
            }
        }
    })
    DemoContent(
        cropState = imageCropper.cropState,
        loadingStatus = imageCropper.loadingStatus,
        selectedImage = selectedImage,
        onPick = { imagePicker.pick() },
        modifier = modifier
    )
    error?.let { CropErrorDialog(it, onDismiss = { error = null }) }
}


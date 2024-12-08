package com.fintechhub.crop.ui.t

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.fintechhub.crop.libraryCore.crop.CropperLoading
import com.fintechhub.crop.libraryCore.crop.CropState
import com.fintechhub.crop.libraryUi.ImageCropperDialog
import com.fintechhub.crop.ui.theme.CropTheme


@Composable
fun DemoContent(
    cropState: CropState?,
    loadingStatus: CropperLoading?,
    selectedImage: ImageBitmap?,
    onPick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (cropState != null) {
        CropTheme(darkTheme = true) {
            ImageCropperDialog(state = cropState)
        }
    }
    if (cropState == null && loadingStatus != null) {
        LoadingDialog(status = loadingStatus)
    }
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (selectedImage != null)
            Image(
                bitmap = selectedImage, contentDescription = null,
                modifier = Modifier.weight(1f)
            ) else Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
            Text("No image selected !")
        }
        Button(onClick = onPick) { Text("Choose Image") }
    }
}

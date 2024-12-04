package com.fintechhub.crop

import androidx.compose.runtime.Composable
import com.fintechhub.crop.libraryCore.images.ImageSrc

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.fintechhub.crop.libraryCore.images.toImageSrc
import kotlinx.coroutines.launch


interface ImagePicker {
    /** Pick an image with [mimetype] */
    fun pick(mimetype: String = "image/*")
}

@Composable
fun rememberImagePicker(onImage: (uri: ImageSrc) -> Unit): ImagePicker {
    val context = LocalContext.current
    val contract = remember { ActivityResultContracts.GetContent() }
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = contract,
        onResult = { uri ->
            coroutineScope.launch {
                val imageSrc = uri?.toImageSrc(context) ?: return@launch
                onImage(imageSrc)
            }
        }
    )

    return remember {
        object : ImagePicker {
            override fun pick(mimetype: String) = launcher.launch(mimetype)
        }
    }
}

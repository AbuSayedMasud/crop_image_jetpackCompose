package com.fintechhub.crop.ui.t
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.fintechhub.crop.presentation.ImagesViewModel
import com.fintechhub.crop.rememberImagePicker

@Composable
fun ViewModelDemo(viewModel: ImagesViewModel, modifier: Modifier = Modifier) {
    val imagePicker = rememberImagePicker(onImage = { uri -> viewModel.setSelectedImage(uri) })
    Column (modifier = Modifier.fillMaxSize()){

        DemoContent(
            cropState = viewModel.imageCropper.cropState,
            loadingStatus = viewModel.imageCropper.loadingStatus,
            selectedImage = viewModel.selectedImage.collectAsState().value,
            onPick = { imagePicker.pick() },
            modifier = modifier
        )
        viewModel.cropError.collectAsState().value?.let { error ->
            CropErrorDialog(error, onDismiss = { viewModel.cropErrorShown() })
        }
    }

}
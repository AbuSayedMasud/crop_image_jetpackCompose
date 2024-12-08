package com.fintechhub.crop.ui.t

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toIntRect
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.fintechhub.crop.CameraPreviewScreen
import com.fintechhub.crop.libraryCore.images.DecodeParams
import com.fintechhub.crop.libraryCore.images.DecodeResult
import com.fintechhub.crop.libraryCore.images.ImageSrc
import com.fintechhub.crop.presentation.ImagesViewModel
import com.fintechhub.crop.rememberImagePicker
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ViewModelDemo(viewModel: ImagesViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isCameraPreviewVisible by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val cameraPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            isCameraPreviewVisible = true
        }
    }

    val imagePicker = rememberImagePicker(onImage = { imageSrc ->
        viewModel.setSelectedImage(imageSrc)
    })

    if (showDialog) {
        // Dialog for choosing camera or gallery
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Select Option") },
            text = { Text("Choose to take a picture from camera or select from gallery.") },
            confirmButton = {
                Button(onClick = {
                    if (hasCameraPermission) {
                        isCameraPreviewVisible = true
                        showDialog = false
                    } else {
                        cameraPermissionRequest.launch(android.Manifest.permission.CAMERA)
                    }
                }) {
                    Text(text = "Take Picture")
                }
            },
            dismissButton = {
                Button(onClick = {
                    imagePicker.pick()
                }) {
                    Text(text = "Choose from Gallery")
                }
            }
        )
    }
    // Main UI
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        // Main UI with DemoContent and CropErrorDialog
        Column(modifier = Modifier.fillMaxSize()) {
            DemoContent(
                cropState = viewModel.imageCropper.cropState,
                loadingStatus = viewModel.imageCropper.loadingStatus,
                selectedImage = viewModel.selectedImage.collectAsState().value,
                onPick = {
                    showDialog = true // Show the dialog to pick an image
                },
                modifier = modifier
            )
            viewModel.cropError.collectAsState().value?.let { error ->
                CropErrorDialog(error, onDismiss = { viewModel.cropErrorShown() })
            }
        }
    }
    // Display Camera Preview if no image captured
    if (isCameraPreviewVisible) {
        CameraPreviewScreen { uri ->
            capturedImageUri = uri
            val image = capturedImageUri?.let { UriImageSrc(it, context) }
            if (image != null) {
                viewModel.setSelectedImage(image)
            }
            isCameraPreviewVisible = false // Hide camera preview after image capture
        }
    }

}



fun createImageUri(context: Context): Uri {
    val contentValues = ContentValues().apply {
        put(
            MediaStore.Images.Media.DISPLAY_NAME,
            "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}.jpg"
        )
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${context.packageName}")
    }

    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ) ?: Uri.EMPTY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraGalleryDialog(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        content = {
            Column {
                Button(modifier = Modifier.fillMaxWidth(), onClick = onCameraClick) {
                    Text("Take Photo")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = onGalleryClick) {
                    Text("Choose from Gallery")
                }
            }
        }
    )
}

class BitmapImageSrc(private val bitmap: Bitmap) : ImageSrc {
    private val scaleFactor = 2  // Increase resolution factor

    override val size: IntSize
        get() = IntSize(bitmap.width * scaleFactor, bitmap.height * scaleFactor)

    override suspend fun open(params: DecodeParams): DecodeResult? {
        return try {
            val newWidth = bitmap.width * scaleFactor
            val newHeight = bitmap.height * scaleFactor
            val scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

            val canvas = android.graphics.Canvas(scaledBitmap)
            val matrix = android.graphics.Matrix()
                .apply { setScale(scaleFactor.toFloat(), scaleFactor.toFloat()) }

            canvas.drawBitmap(bitmap, matrix, null)
            DecodeResult(
                DecodeParams(params.sampleSize, size.toIntRect()),
                scaledBitmap.asImageBitmap()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

class UriImageSrc(private val uri: Uri, private val context: Context) : ImageSrc {
    override val size: IntSize
        get() {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                return IntSize(bitmap.width, bitmap.height)
            } catch (e: Exception) {
                return IntSize(1, 1)
            }
        }

    override suspend fun open(params: DecodeParams): DecodeResult? {
        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            DecodeResult(DecodeParams(params.sampleSize, size.toIntRect()), bitmap.asImageBitmap())
        } catch (e: Exception) {
            null
        }
    }
}

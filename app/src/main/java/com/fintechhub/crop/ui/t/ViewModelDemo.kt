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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toIntRect
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.rememberImagePainter
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
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Create URI for storing the image
    val imageUri = createImageUri(context)

    // Camera intent launcher
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                capturedImageUri = imageUri
                Log.d("image url",capturedImageUri.toString())
                val image = UriImageSrc(capturedImageUri!!, context)
                viewModel.setSelectedImage(image)

            } else {
                Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }

    fun requestPermissions(context: Context, permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>) {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        // Check if permissions are already granted
        if (permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
            // Launch camera or gallery as permissions are granted
            permissionLauncher.launch(permissions)
        } else {
            // Request permissions
            permissionLauncher.launch(permissions)
        }
    }

    // Permission launcher for camera and storage
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val isCameraGranted = permissions[Manifest.permission.CAMERA] ?: false
            val isMediaGranted =
                permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: false || // For Android 13+
                        permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false // For pre-Android 13

            if (isCameraGranted && isMediaGranted) {
                cameraLauncher.launch(imageUri)
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }


    // Image picker to choose an image from the gallery
    val imagePicker = rememberImagePicker(onImage = { imageSrc ->
        viewModel.setSelectedImage(imageSrc)
    })

    // State to manage the visibility of the dialog
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        DemoContent(
            cropState = viewModel.imageCropper.cropState,
            loadingStatus = viewModel.imageCropper.loadingStatus,
            selectedImage = viewModel.selectedImage.collectAsState().value,
            onPick = { showDialog = true },
            modifier = modifier
        )
    }

    // Show Camera/Gallery selection dialog
    if (showDialog) {
        CameraGalleryDialog(
            onCameraClick = {
                requestPermissions(context, permissionLauncher)
                showDialog = false
            },
            onGalleryClick = {
                imagePicker.pick()
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
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

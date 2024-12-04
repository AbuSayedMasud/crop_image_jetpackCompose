package com.fintechhub.crop.libraryUi

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fintechhub.crop.R
import com.fintechhub.crop.libraryCore.crop.CropState
import com.fintechhub.crop.libraryCore.crop.CropperStyle
import com.fintechhub.crop.libraryCore.crop.DefaultCropperStyle
import com.fintechhub.crop.libraryCore.crop.LocalCropperStyle


val CropperDialogProperties = (DialogProperties(
    usePlatformDefaultWidth = false,
    dismissOnBackPress = false,
    dismissOnClickOutside = false
))

@Composable
fun ImageCropperDialog(
    state: CropState,
    style: CropperStyle = DefaultCropperStyle,
    dialogPadding: PaddingValues = PaddingValues(0.dp), // No padding for full-screen
    topBar: @Composable (CropState) -> Unit = { DefaultTopBar(it) },
    cropControls: @Composable BoxScope.(CropState) -> Unit = { DefaultControls(it) }
) {
    // Initialize the state when the screen is launched
    LaunchedEffect(Unit) {
        state.setInitialState(style) // Could still have potential threading issues; test thoroughly
    }

    // Provide the cropper style locally
    CompositionLocalProvider(LocalCropperStyle provides style) {
        // Use a full-screen layout
        Surface(
            modifier = Modifier
                .fillMaxSize() // Full-screen surface
                .padding(dialogPadding), // Padding for the screen if needed
        ) {
            Column {
                // Render the top bar
                topBar(state)

                // Render the cropper preview and controls
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clipToBounds() // Ensures content respects bounds
                ) {
                    // Preview area
                    CropperPreview(
                        state = state,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Customizable crop controls overlay
                    cropControls(state)
                }
            }
        }
    }
}


@Composable
fun BoxScope.DefaultControls(state: CropState) {
    val verticalControls =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    CropperControls(
        isVertical = verticalControls,
        state = state,
        modifier = Modifier
            .align(if (!verticalControls) Alignment.BottomCenter else Alignment.CenterEnd)
            .padding(12.dp)

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopBar(state: CropState) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(), // Ensures the text aligns to the start
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Crop Image",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f) // Takes up available horizontal space
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { state.done(accept = false) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
        },
        actions = {
            IconButton(onClick = { state.reset() }) {
                Icon(
                    painter = painterResource(id = R.drawable.restore),
                    contentDescription = "Reset",
                    tint = Color.Black

                )
            }
            IconButton(
                onClick = { state.done(accept = true) },
                enabled = !state.accepted // Disable if already accepted
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Done",
                    tint = if (!state.accepted) Color.Black else Color.Black
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.shadow(4.dp) // Optional shadow for modern design
    )
}

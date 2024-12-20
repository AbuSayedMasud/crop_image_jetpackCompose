package com.fintechhub.crop.libraryUi

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import com.fintechhub.crop.R
import com.fintechhub.crop.libraryCore.crop.AspectRatio
import com.fintechhub.crop.libraryCore.crop.CropShape
import com.fintechhub.crop.libraryCore.crop.CropState
import com.fintechhub.crop.libraryCore.crop.LocalCropperStyle
import com.fintechhub.crop.libraryCore.crop.flipHorizontal
import com.fintechhub.crop.libraryCore.crop.flipVertical
import com.fintechhub.crop.libraryCore.crop.rotLeft
import com.fintechhub.crop.libraryCore.crop.rotRight
import com.fintechhub.crop.libraryCore.utils.eq0
import com.fintechhub.crop.libraryCore.utils.setAspect

fun Size.isAspect(aspect: AspectRatio): Boolean {
    return ((width / height) - (aspect.x.toFloat() / aspect.y)).eq0()
}

val LocalVerticalControls = staticCompositionLocalOf { false }

@Composable
fun CropperControls(
    isVertical: Boolean,
    state: CropState,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalVerticalControls provides isVertical) {
        ButtonsBar(modifier = modifier.fillMaxWidth()) {
            IconButton(onClick = { state.rotLeft() }) {
                Icon(
                    painterResource(R.drawable.rot_left),
                    null,
                    modifier = Modifier.size(25.dp),
                    tint = Color.Black
                )
            }
            IconButton(onClick = { state.rotRight() }) {
                Icon(
                    painterResource(R.drawable.rot_right),
                    null,
                    modifier = Modifier.size(25.dp),
                    tint = Color.Black
                )
            }
            IconButton(onClick = { state.flipHorizontal() }) {
                Icon(
                    painterResource(R.drawable.flip_hor),
                    null,
                    modifier = Modifier.size(25.dp),
                    tint = Color.Black
                )
            }
            IconButton(onClick = { state.flipVertical() }) {
                Icon(
                    painterResource(R.drawable.flip_ver),
                    null,
                    modifier = Modifier.size(25.dp),
                    tint = Color.Black
                )
            }
            LocalCropperStyle.current.aspects.let { aspects ->
                if (aspects.size > 1) {
                    Box {
                        var menu by remember { mutableStateOf(false) }
                        IconButton(onClick = { menu = !menu }) {
                            Icon(
                                painterResource(R.drawable.resize),
                                null,
                                modifier = Modifier.size(25.dp),
                                tint = Color.Black
                            )
                        }
                        if (menu) AspectSelectionMenu(
                            onDismiss = { menu = false },
                            region = state.region,
                            onRegion = { state.region = it },
                            lock = state.aspectLock,
                            onLock = { state.aspectLock = it }
                        )
                    }
                }
            }
            LocalCropperStyle.current.shapes.let { shapes ->
                if (shapes.size > 1) {
                    Box {
                        var menu by remember { mutableStateOf(false) }
                        IconButton(onClick = { menu = !menu }) {
                            Icon(
                                Icons.Default.Star,
                                null,
                                modifier = Modifier.size(25.dp),
                                tint = Color.Black
                            )
                        }
                        if (menu) ShapeSelectionMenu(
                            onDismiss = { menu = false },
                            selected = state.shape,
                            onSelect = { state.shape = it },
                            shapes = shapes
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ButtonsBar(
    modifier: Modifier = Modifier,
    buttons: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shadowElevation = 4.dp,
        color = Color.White,
        contentColor = contentColorFor(MaterialTheme.colorScheme.surface)
    ) {
        if (LocalVerticalControls.current) Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
            buttons()
        } else Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            buttons()
        }
    }
}


@Composable
fun ShapeSelectionMenu(
    onDismiss: () -> Unit,
    shapes: List<CropShape>,
    selected: CropShape,
    onSelect: (CropShape) -> Unit,
) {
    OptionsPopup(onDismiss = onDismiss, optionCount = shapes.size) { i ->
        val shape = shapes[i]
        ShapeItem(shape = shape, selected = selected == shape,
            onSelect = { onSelect(shape) })
    }
}


@Composable
fun ShapeItem(
    shape: CropShape, selected: Boolean, onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(
        targetValue = if (!selected) LocalContentColor.current
        else MaterialTheme.colorScheme.secondary
    )
    IconButton(
        modifier = modifier,
        onClick = onSelect
    ) {
        val shapeState by rememberUpdatedState(newValue = shape)
        Box(modifier = Modifier
            .size(20.dp)
            .drawWithCache {
                val path = shapeState.asPath(size.toRect())
                val strokeWidth = 2.dp.toPx()
                onDrawWithContent {
                    drawPath(path = path, color = color, style = Stroke(strokeWidth))
                }
            })
    }
}


@Composable
fun AspectSelectionMenu(
    onDismiss: () -> Unit,
    region: Rect,
    onRegion: (Rect) -> Unit,
    lock: Boolean,
    onLock: (Boolean) -> Unit,
) {
    val aspects = LocalCropperStyle.current.aspects
    OptionsPopup(onDismiss = onDismiss, optionCount = 1 + aspects.size) { i ->
        val unselectedTint = LocalContentColor.current
        val selectedTint = MaterialTheme.colorScheme.secondary
        if (i == 0) IconButton(onClick = { onLock(!lock) }) {
            Icon(
                Icons.Default.Lock, null,
                tint = if (lock) selectedTint else unselectedTint
            )
        } else {
            val aspect = aspects[i - 1]
            val isSelected = region.size.isAspect(aspect)
            IconButton(onClick = { onRegion(region.setAspect(aspect)) }) {
                Text(
                    "${aspect.x}:${aspect.y}",
                    color = if (isSelected) selectedTint else unselectedTint
                )
            }
        }
    }
}
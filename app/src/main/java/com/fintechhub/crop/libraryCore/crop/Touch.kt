package com.fintechhub.crop.libraryCore.crop

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
import com.fintechhub.crop.libraryCore.utils.ViewMat
import com.fintechhub.crop.libraryCore.utils.ZoomLimits
import com.fintechhub.crop.libraryCore.utils.abs
import com.fintechhub.crop.libraryCore.utils.dragState
import com.fintechhub.crop.libraryCore.utils.onGestures
import com.fintechhub.crop.libraryCore.utils.rememberGestureState
import com.fintechhub.crop.libraryCore.utils.resize
import com.fintechhub.crop.libraryCore.utils.zoomState

private val MoveHandle = Offset(.5f, .5f)

class DragHandle(
    val handle: Offset,
    val initialPos: Offset,
    val initialRegion: Rect
)

fun Modifier.cropperTouch(
    region: Rect,
    onRegion: (Rect) -> Unit,
    touchRad: Dp,
    handles: List<Offset>,
    viewMat: ViewMat,
    pending: DragHandle?,
    onPending: (DragHandle?) -> Unit,
    zooming: MutableState<Boolean>,
    zoomLimits: ZoomLimits,
): Modifier = composed {
    val touchRadPx2 = LocalDensity.current.run {
        remember(touchRad, viewMat.scale) { touchRad.toPx() / viewMat.scale }.let { it * it }
    }

    onGestures(
        rememberGestureState(
            zoom = zoomState(
                begin = { c ->
                    viewMat.zoomStart(c)
                    zooming.value = true
                },
                next = { s, c -> viewMat.zoom(c, s, zoomLimits) },
                done = { zooming.value = false }
            ),
            drag = dragState(
                begin = { pos ->
                    val localPos = viewMat.invMatrix.map(pos)
                    handles.findHandle(
                        region, localPos,
                        touchRadPx2
                    )?.let { handle ->
                        onPending(DragHandle(handle, localPos, region))
                    }
                },
                next = { _, pos, _ ->
                    pending?.let {
                        val localPos = viewMat.invMatrix.map(pos)
                        val delta = (localPos - pending.initialPos).round().toOffset()
                        val newRegion = if (pending.handle != MoveHandle) {
                            pending.initialRegion
                                .resize(pending.handle, delta, zoomLimits.minCropSize)
                        } else {
                            pending.initialRegion.translate(delta)
                        }
                        onRegion(newRegion)
                    }
                },
                done = {
                    onPending(null)
                })
        )
    )
}

fun List<Offset>.findHandle(
    region: Rect,
    pos: Offset,
    touchRadPx2: Float
): Offset? {
    firstOrNull { (region.abs(it) - pos).getDistanceSquared() <= touchRadPx2 }?.let { return it }
    if (region.contains(pos)) return MoveHandle
    return null
}
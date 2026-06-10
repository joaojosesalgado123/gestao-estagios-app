package pt.ligix.app.ui.common

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

class DropdownDismissController {
    var activeId by mutableStateOf<String?>(null)
        private set

    private var activeBounds by mutableStateOf<Map<String, Rect>>(emptyMap())

    fun show(id: String) {
        activeId = id
        activeBounds = emptyMap()
    }

    fun hide(id: String? = null) {
        if (id == null || activeId == id) {
            activeId = null
            activeBounds = emptyMap()
        }
    }

    fun updateBounds(id: String, boundsId: String, bounds: Rect) {
        if (activeId == id) {
            activeBounds = activeBounds + (boundsId to bounds)
        }
    }

    fun shouldDismissAt(position: androidx.compose.ui.geometry.Offset): Boolean {
        if (activeId == null || activeBounds.isEmpty()) return false
        return activeBounds.values.none { bounds -> bounds.contains(position) }
    }
}

@Composable
fun rememberDropdownDismissController(): DropdownDismissController =
    remember { DropdownDismissController() }

fun Modifier.dismissDropdownsOnOutsideTap(
    controller: DropdownDismissController
): Modifier = pointerInput(controller) {
    awaitEachGesture {
        val down = awaitFirstDown(pass = PointerEventPass.Initial)
        if (controller.shouldDismissAt(down.position)) {
            controller.hide()
        }
    }
}

fun Modifier.dropdownDismissBounds(
    controller: DropdownDismissController,
    dropdownId: String,
    expanded: Boolean,
    boundsId: String = "content"
): Modifier = onGloballyPositioned { coordinates ->
    if (expanded) {
        controller.updateBounds(dropdownId, boundsId, coordinates.boundsInRoot())
    }
}

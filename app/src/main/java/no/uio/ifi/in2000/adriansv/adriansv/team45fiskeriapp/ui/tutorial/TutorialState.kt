package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
data class TutorialState(
    val currentStep: Int = 0,
    val isVisible: Boolean = false,
    val steps: List<TutorialStep> = emptyList(),
    val highlightedBounds: Rect? = null
) {
    val isCompleted: Boolean
        get() = currentStep >= steps.size

    val currentStepData: TutorialStep?
        get() = steps.getOrNull(currentStep)
}

@Immutable
data class TutorialStep(
    val title: String,
    val description: String,
    val targetTag: String,
    val mascotResourceId: Int,
    val mascotPosition: MascotPosition = MascotPosition.RIGHT,
    val mascotOffset: androidx.compose.ui.unit.DpOffset = androidx.compose.ui.unit.DpOffset(0.dp, 0.dp)
)

enum class MascotPosition {
    LEFT, RIGHT, TOP, BOTTOM
}

data class HighlightConfig(
    val padding: Dp = 8.dp,
    val cornerRadius: Dp = 8.dp,
    val blurRadius: Dp = 4.dp
) 
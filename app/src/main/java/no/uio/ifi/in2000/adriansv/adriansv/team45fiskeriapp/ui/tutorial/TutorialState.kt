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
    val highlightedBounds: Rect? = null,
    val isCompleted: Boolean = false
) {
    val isLastStep: Boolean
        get() = currentStep >= steps.size - 1

    val currentStepData: TutorialStep?
        get() = steps.getOrNull(currentStep)
}

@Immutable
data class TutorialStep(
    val title: String,
    val description: String,
    val targetTag: String,
    val mascotResourceId: Int? = null,
    val mascotPlacement: MascotPlacement = MascotPlacement.AUTO,
    val mascotOffset: androidx.compose.ui.unit.DpOffset = androidx.compose.ui.unit.DpOffset(0.dp, 0.dp),
    val isLastStep: Boolean = false,
    val highlightConfig: HighlightConfig = HighlightConfig()
)

enum class MascotPlacement {
    AUTO,   // Automatisk plassering basert på targetTag-elementets posisjon
    LEFT,   // Til venstre for target
    RIGHT,  // Til høyre for target
    TOP,    // Over target
    BOTTOM, // Under target
    CENTER  // Sentrert (for skjermer uten spesifikt target)
}

data class HighlightConfig(
    val padding: Dp = 8.dp,
    val cornerRadius: Dp = 8.dp,
    val blurRadius: Dp = 4.dp,
    val highlightShape: HighlightShape = HighlightShape.CIRCLE
)

enum class HighlightShape {
    CIRCLE,
    RECTANGLE,
    OVAL
} 
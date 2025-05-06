package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial

import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

data class TutorialState(
    val isVisible: Boolean = false,
    val isCompleted: Boolean = false,
    val currentStepIndex: Int = 0,
    val currentStepData: TutorialStep? = null
)

data class TutorialStep(
    val title: String,
    val description: String,
    val targetTag: String = "",
    val mascotResourceId: Int? = null,
    val mascotPlacement: MascotPlacement = MascotPlacement.AUTO,
    val mascotOffset: DpOffset = DpOffset(0.dp, 0.dp),
    val isLastStep: Boolean = false
)

enum class MascotPlacement {
    CENTER, LEFT, RIGHT, TOP, BOTTOM, AUTO
} 
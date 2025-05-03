package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp

class TutorialManager {
    private val _state = mutableStateOf(TutorialState())
    val state: TutorialState
        get() = _state.value

    fun startTutorial(steps: List<TutorialStep>) {
        if (_state.value.isCompleted) return
        
        _state.value = TutorialState(
            currentStep = 0,
            isVisible = true,
            steps = steps
        )
    }

    fun nextStep() {
        val currentState = _state.value
        if (currentState.currentStep < currentState.steps.size - 1) {
            _state.value = currentState.copy(
                currentStep = currentState.currentStep + 1
            )
        } else {
            completeTutorial()
        }
    }

    fun skipTutorial() {
        completeTutorial()
    }

    private fun completeTutorial() {
        _state.value = _state.value.copy(
            isVisible = false,
            isCompleted = true,
            currentStep = _state.value.steps.size
        )
    }

    fun updateHighlightBounds(bounds: Rect?) {
        _state.value = _state.value.copy(
            highlightedBounds = bounds
        )
    }
}

@Composable
fun rememberTutorialManager(): TutorialManager {
    return remember { TutorialManager() }
}

@Composable
fun Modifier.tutorialTarget(
    tag: String,
    tutorialManager: TutorialManager
): Modifier {
    val view = LocalView.current
    return this.then(
        Modifier.onGloballyPositioned { coordinates ->
            val bounds = coordinates.boundsInWindow()
            if (tutorialManager.state.currentStepData?.targetTag == tag) {
                tutorialManager.updateHighlightBounds(bounds)
            }
        }
    )
} 
package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R

class TutorialManager {
    var highlightedBounds by mutableStateOf<Rect?>(null)
        private set
    
    var currentStep by mutableStateOf(0)
        private set
    
    var isVisible by mutableStateOf(false)
        private set
    
    var isCompleted by mutableStateOf(false)
        private set
    
    val steps = listOf(
        TutorialStep(
            title = "Velkommen til appen!",
            description = "Dette er en rask introduksjon som hjelper deg å forstå appen.",
            targetTag = "welcome",
            mascotResourceId = R.drawable.presenting,
            mascotPlacement = MascotPlacement.CENTER
        ),
        TutorialStep(
            title = "Kartvisning",
            description = "Her på kartet kan du utforske fiskesteder, interagere med andre fartøy, og bli varslet om fare- og værvarsel.",
            targetTag = "map_view",
            mascotResourceId = R.drawable.nedvenstre,
            mascotPlacement = MascotPlacement.BOTTOM
        ),
        TutorialStep(
            title = "Profilsiden",
            description = "Her kan du lage din profil, endre dine innstillinger og loggføre dine favorittfangster!",
            targetTag = "",
            mascotResourceId = R.drawable.nedhoyre,
            mascotPlacement = MascotPlacement.BOTTOM
        ),
        TutorialStep(
            title = "Søkefunksjonen",
            description = "Lyst til å planlegge området først? Søkefunksjonen hjelper deg å finne fram til der du ønsker å dra!",
            targetTag = "search_button",
            mascotResourceId = R.drawable.pekopp,
            mascotPlacement = MascotPlacement.TOP
        ),
        TutorialStep(
            title = "Lyst til å sjekke værmeldingen?",
            description = "Bruk værvarselssiden for 10 dagers varsel",
            targetTag = "weather_button",
            mascotResourceId = R.drawable.nedhoyre,
            mascotPlacement = MascotPlacement.BOTTOM
        ),
        TutorialStep(
            title = "Båtvettregler",
            description = "Før du ferder på sjøen, er det viktig å vite om reglene!",
            targetTag = "boat_rules_button",
            mascotResourceId = R.drawable.tilhoyre,
            mascotPlacement = MascotPlacement.RIGHT
        ),
        TutorialStep(
            title = "Fiskeloggen",
            description = "I Fiskeloggen kan du lagre dine fisker og plassere de på kartet der du fikk de!",
            targetTag = "fish_log_button",
            mascotResourceId = R.drawable.tilhoyre,
            mascotPlacement = MascotPlacement.RIGHT
        ),
        TutorialStep(
            title = "Fisketuren",
            description = "Trykk her for å starte en fisketur. Appen holder styr på tiden og fangstene dine, som du kan finne igjen i Min Profil!",
            targetTag = "fishing_trip_button",
            mascotResourceId = R.drawable.tilhoyre,
            mascotPlacement = MascotPlacement.RIGHT
        ),
        TutorialStep(
            title = "Da er du klar !",
            description = "Nå har du lært det grunnleggende i appen, og du er klar til å utforske norske farvann. God fisketur!",
            targetTag = "complete",
            mascotResourceId = R.drawable.presenting,
            isLastStep = true,
            mascotPlacement = MascotPlacement.CENTER
        )
    )
    
    fun startTutorial() {
        isVisible = true
        isCompleted = false
        currentStep = 0
        updateHighlightedBounds()
    }
    
    fun nextStep() {
        if (currentStep < steps.size - 1) {
            currentStep++
            updateHighlightedBounds()
        } else {
            completeTutorial()
        }
    }
    
    fun skipTutorial() {
        completeTutorial()
    }
    
    private fun completeTutorial() {
        isVisible = false
        isCompleted = true
        highlightedBounds = null
    }
    
    fun updateButtonPosition(targetTag: String, bounds: Rect) {
        if (steps.getOrNull(currentStep)?.targetTag == targetTag) {
            highlightedBounds = bounds
        }
    }
    
    private fun updateHighlightedBounds() {
        // Reset highlighted bounds when changing steps
        highlightedBounds = null
    }
    
    fun getCurrentStep(): TutorialStep? = steps.getOrNull(currentStep)
    
    fun isLastStep(): Boolean = currentStep >= steps.size - 1
}

@Composable
fun rememberTutorialManager(): TutorialManager {
    return remember { TutorialManager() }
}

/**
 * Modifier-utvidelse som legger til posisjonssporing for tutorialTarget-elementer.
 * Dette gjør at vi kan fremheve UI-elementer i tutorialen.
 */
@Composable
fun Modifier.tutorialTarget(
    tag: String,
    tutorialManager: TutorialManager
): Modifier {
    return this.then(
        Modifier.onGloballyPositioned { coordinates ->
            val bounds = coordinates.boundsInWindow()
            if (tutorialManager.steps.getOrNull(tutorialManager.currentStep)?.targetTag == tag) {
                tutorialManager.updateButtonPosition(tag, bounds)
            }
        }
    )
} 
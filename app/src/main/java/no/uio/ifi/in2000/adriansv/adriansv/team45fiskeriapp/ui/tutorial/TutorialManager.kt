package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R

class TutorialManager {
    var tutorialState by mutableStateOf(TutorialState())
        private set

    val isCompleted: Boolean
        get() = tutorialState.isCompleted

    private val privateSteps = listOf(
        TutorialStep(
            title = "Velkommen til Sjøspor!",
            description = "Her er en rask innføring, så du enkelt kommer i gang med appen!",
            targetTag = "",
            mascotResourceId = R.drawable.presenting,
            mascotPlacement = MascotPlacement.RIGHT
        ),
        TutorialStep(
            title = "Utforsk kartet",
            description = "På kartet kan du utforske steder å fiske, få en oversikt over fartøy, og se informasjon om fare- og værvarsel.",
            targetTag = "kart_button",
            mascotResourceId = R.drawable.nedvenstre,
            mascotPlacement = MascotPlacement.LEFT
        ),
        TutorialStep(
            title = "Profilsiden din",
            description = "Her kan du tilpasse profilen din, endre dine innstillinger og loggføre dine fangster.",
            targetTag = "profile_button",
            mascotResourceId = R.drawable.nedhoyre,
            mascotPlacement = MascotPlacement.RIGHT
        ),
        TutorialStep(
            title = "Finn et sted",
            description = "Bruk søkefeltet til å finne steder du vil fiske eller utforske.",
            targetTag = "search_bar",
            mascotResourceId = R.drawable.pekopp,
            mascotPlacement = MascotPlacement.BOTTOM
        ),
        TutorialStep(
            title = "Lyst til å sjekke værmeldingen?",
            description = "Hold deg oppdatert med 10-dagers varsel for området du er i.",
            targetTag = "weather_button",
            mascotResourceId = R.drawable.nedhoyre,
            mascotPlacement = MascotPlacement.LEFT
        ),
        TutorialStep(
            title = "Båtvettregler",
            description = "Før du ferder på sjøen, er det viktig å vite om båtvettreglene!",
            targetTag = "boat_rules_button",
            mascotResourceId = R.drawable.tilhoyre,
            mascotPlacement = MascotPlacement.BOTTOM
        ),
        TutorialStep(
            title = "Fiskeloggen",
            description = "I Fiskeloggen kan du lagre dine fangster og markere de på kartet der du fikk de.",
            targetTag = "fish_log_button",
            mascotResourceId = R.drawable.tilhoyre,
            mascotPlacement = MascotPlacement.BOTTOM
        ),
        TutorialStep(
            title = "Start en fisketur",
            description = "Trykk her for å starte en ny tur! Appen logger ruten og fangstene automatisk for deg!",
            targetTag = "fishing_trip_button",
            mascotResourceId = R.drawable.tilhoyre,
            mascotPlacement = MascotPlacement.BOTTOM
        ),
        TutorialStep(
            title = "Klar for å bruke Sjøspor!",
            description = "Nå har du lært det viktigste - god tur!",
            targetTag = "",
            mascotResourceId = R.drawable.waving_mascot,
            mascotPlacement = MascotPlacement.RIGHT,
            isLastStep = true
        )
    )

    fun startTutorial() {
        tutorialState = TutorialState(
            isVisible = true,
            isCompleted = false,
            currentStepIndex = 0,
            currentStepData = privateSteps.firstOrNull()
        )
    }

    fun nextStep() {
        val currentIndex = tutorialState.currentStepIndex
        
        if (currentIndex >= privateSteps.size - 1) {
            completeTutorial()
            return
        }
        
        val nextIndex = currentIndex + 1
        tutorialState = tutorialState.copy(
            currentStepIndex = nextIndex,
            currentStepData = privateSteps.getOrNull(nextIndex)
        )
    }
    
    fun skipTutorial() {
        completeTutorial()
    }
    
    private fun completeTutorial() {
        tutorialState = tutorialState.copy(
            isVisible = false,
            isCompleted = true
        )
    }
}

@Composable
fun rememberTutorialManager(): TutorialManager {
    return remember { TutorialManager() }
}

@Composable
fun Modifier.tutorialTarget(): Modifier {
    return this
} 
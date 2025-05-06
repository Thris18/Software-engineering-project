package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R

class TutorialManager {
    var tutorialState by mutableStateOf(TutorialState())
        private set

    // For bakoverkompatibilitet
    val isCompleted: Boolean
        get() = tutorialState.isCompleted

    val isVisible: Boolean
        get() = tutorialState.isVisible

    val currentStep: Int
        get() = tutorialState.currentStepIndex

    val steps: List<TutorialStep>
        get() = privateSteps.toList()

    fun getCurrentStep(): TutorialStep? = tutorialState.currentStepData

    fun isLastStep(): Boolean = tutorialState.currentStepIndex >= privateSteps.size - 1

    private val privateSteps = listOf(
        TutorialStep(
            title = "Velkommen til appen!",
            description = "Dette er en rask introduksjon som hjelper deg å forstå appen.",
            targetTag = "",
            mascotResourceId = R.drawable.presenting,
            mascotPlacement = MascotPlacement.RIGHT
        ),
        TutorialStep(
            title = "Kartvisning",
            description = "Her på kartet kan du utforske fiskesteder, interagere med andre fartøy, og bli varslet om fare- og værvarsel.",
            targetTag = "kart_button",
            mascotResourceId = R.drawable.nedvenstre,
            mascotPlacement = MascotPlacement.LEFT
        ),
        TutorialStep(
            title = "Profilsiden",
            description = "Her kan du lage din profil, endre dine innstillinger og loggføre dine favorittfangster!",
            targetTag = "profile_button",
            mascotResourceId = R.drawable.nedhoyre,
            mascotPlacement = MascotPlacement.RIGHT
        ),
        TutorialStep(
            title = "Søkefunksjonen",
            description = "Bruk søkefunksjonen for å finne steder og andre fiskere!",
            targetTag = "search_bar",
            mascotResourceId = R.drawable.pekopp,
            mascotPlacement = MascotPlacement.BOTTOM
        ),
        TutorialStep(
            title = "Lyst til å sjekke værmeldingen?",
            description = "Bruk værvarselssiden for 10 dagers varsel",
            targetTag = "weather_button",
            mascotResourceId = R.drawable.nedhoyre,
            mascotPlacement = MascotPlacement.LEFT
        ),
        TutorialStep(
            title = "Båtvettregler",
            description = "Før du ferder på sjøen, er det viktig å vite om reglene!",
            targetTag = "boat_rules_button",
            mascotResourceId = R.drawable.tilhoyre,
            mascotPlacement = MascotPlacement.BOTTOM
        ),
        TutorialStep(
            title = "Fiskeloggen",
            description = "I Fiske oggen kan du lagre dine fisker og plassere de på kartet der du fikk de!",
            targetTag = "fish_log_button",
            mascotResourceId = R.drawable.tilhoyre,
            mascotPlacement = MascotPlacement.BOTTOM
        ),
        TutorialStep(
            title = "Fisketuren",
            description = "Trykk her for å starte en ny tur! Appen holder styr på tid og fangstene dine, som du ser igjen i Min Profil!",
            targetTag = "fishing_trip_button",
            mascotResourceId = R.drawable.tilhoyre,
            mascotPlacement = MascotPlacement.BOTTOM
        ),
        TutorialStep(
            title = "Da er du klar !",
            description = "Nå vet du det mest grunnleggende for Fiske-appen!",
            targetTag = "",
            mascotResourceId = R.drawable.presenting,
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
    
    fun completeTutorial() {
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

/**
 * En dummy modifier for tutorialTarget for å unngå kompileringsfeil i filer som bruker denne.
 * Gjør ingenting funksjonelt men bevarer API-kompatibilitet.
 */
@Composable
fun Modifier.tutorialTarget(
    tag: String,
    tutorialManager: TutorialManager
): Modifier {
    // Denne versjonen gjør ingenting, men holder API-et kompatibelt
    return this
} 
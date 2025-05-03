package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R

@Composable
fun TutorialExample() {
    val tutorialManager = rememberTutorialManager()
    
    // Define tutorial steps
    val tutorialSteps = remember {
        listOf(
            TutorialStep(
                title = "Velkommen til appen!",
                description = "Dette er hovedskjermen hvor du kan se viktig informasjon.",
                targetTag = "welcome",
                mascotResourceId = R.drawable.presenting
            ),
            TutorialStep(
                title = "Kartvisning",
                description = "Trykk her for å se kartet med vær- og fiskedata.",
                targetTag = "map_button",
                mascotResourceId = R.drawable.presenting
            ),
            TutorialStep(
                title = "Fiskelogg",
                description = "Her kan du logge dine fangster og se historikk.",
                targetTag = "fish_log_button",
                mascotResourceId = R.drawable.presenting
            )
        )
    }

    // Start tutorial when screen is first shown
    LaunchedEffect(Unit) {
        tutorialManager.startTutorial(tutorialSteps)
    }

    Box {
        // Your screen content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Welcome section
            Text(
                text = "Velkommen",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.tutorialTarget("welcome", tutorialManager)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { /* Navigate to map */ },
                    modifier = Modifier.tutorialTarget("map_button", tutorialManager)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.map_marker),
                        contentDescription = "Kart"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kart")
                }

                Button(
                    onClick = { /* Navigate to fish log */ },
                    modifier = Modifier.tutorialTarget("fish_log_button", tutorialManager)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.presenting),
                        contentDescription = "Fiskelogg"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fiskelogg")
                }
            }
        }

        // Tutorial overlay
        TutorialOverlay(
            state = tutorialManager.state,
            onNext = { tutorialManager.nextStep() },
            onSkip = { tutorialManager.skipTutorial() }
        )
    }
} 
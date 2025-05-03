package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R


@Composable
fun TutorialOverlay(
    state: TutorialState,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.isVisible || state.isCompleted) return

    val currentStep = state.currentStepData ?: return
    
    // Dialog som alltid vises
    Dialog(onDismissRequest = onSkip) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(align = Alignment.Center)
                    .offset(x = 0.dp)
            ) {
                // Bestem størrelse basert på tittel/trinn
                val (minHeight, maxHeight) = when (currentStep.title) {
                    "Velkommen til appen!" -> Pair(170.dp, 220.dp)
                    else -> Pair(190.dp, 250.dp)
                }

                Surface(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .heightIn(min = minHeight, max = maxHeight),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp, 20.dp),
                    ) {
                        Text(
                            text = currentStep.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = currentStep.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = onSkip) {
                                Text("Hopp over")
                            }
                            
                            TextButton(onClick = onNext) {
                                Text("Neste")
                            }
                        }
                    }
                }
                
                if (currentStep.mascotResourceId != null) {
                    when (currentStep.title) {
                        "Profilsiden" -> {
                            // For profilsiden: Plasser maskotten nederst i midten og gjør den mindre
                            Image(
                                painter = painterResource(id = currentStep.mascotResourceId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(175.dp)  // Mindre størrelse for profilsiden
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 210.dp, x = 50.dp),  // Plassert under dialogen
                                contentScale = ContentScale.Fit
                            )
                        }
                        "Kartvisning" -> {
                            // For kartvisningstrinnet: Høyre side med spesifikk offset
                            Image(
                                painter = painterResource(id = currentStep.mascotResourceId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(175.dp)
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 215.dp, x = (-35).dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        "Båtvettregler" -> {
                            // For båtvettregler: Plassert litt til høyre for midten, litt opp fra bunnen
                            Image(
                                painter = painterResource(id = currentStep.mascotResourceId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(180.dp)
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 225.dp, x = 40.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        "Søkefunksjonen" -> {
                            // For søkefunksjonen: Plassert til venstre for å peke på søkefeltet
                            Image(
                                painter = painterResource(id = currentStep.mascotResourceId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(210.dp)
                                    .align(Alignment.TopCenter)
                                    .offset(y = (-210).dp, x = (80).dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        "Fiskeloggen" -> {
                            // For fiskeloggen: Plassert nederst til høyre for å peke på fiskelogg-knappen
                            Image(
                                painter = painterResource(id = currentStep.mascotResourceId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(180.dp)
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 180.dp, x = 40.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        else -> {
                            // For andre trinn: Standard plassering til høyre
                            Image(
                                painter = painterResource(id = currentStep.mascotResourceId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(200.dp)
                                    .align(Alignment.CenterEnd)
                                    .offset(x = 80.dp, y = 0.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                if (currentStep.title == "Kartvisning") {
                    // Skip-ikon på toppen
                    Image(
                        painter = painterResource(id = R.drawable.skip),
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)  // 10% mindre enn 80dp
                            .align(Alignment.CenterStart)
                            .offset(x = (-50).dp, y = (-60).dp),
                        contentScale = ContentScale.Fit
                    )
                    
                    // Farevarsel-ikon på bunnen
                    Image(
                        painter = painterResource(id = R.drawable.farevarsel),
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.CenterStart)
                            .offset(x = (-40).dp, y = 60.dp),
                        contentScale = ContentScale.Fit
                    )
                    
                    // Circular highlight for Kartvisning
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.Center)
                            .offset(y = 355.dp, x = (-113).dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
                    )
                }
                
                // Circular highlight for Profilsiden
                if (currentStep.title == "Profilsiden") {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.Center) 
                            .offset(y = 355.dp, x = 113.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
                    )
                }
                
                // Circular highlight for Båtvettregler
                if (currentStep.title == "Båtvettregler") {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.Center)
                            .offset(y = 265.dp, x = 147.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
                    )
                }
                
                // Circular highlight for Fiskeloggen
                if (currentStep.title == "Fiskeloggen") {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.Center)
                            .offset(y = 210.dp, x = 147.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
                    )
                }
            }
        }
    }
}

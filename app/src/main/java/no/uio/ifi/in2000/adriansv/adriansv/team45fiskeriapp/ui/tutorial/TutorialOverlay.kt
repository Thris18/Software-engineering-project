package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial

import android.graphics.drawable.AnimationDrawable
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.viewinterop.AndroidView

/**
 * VEILEDNINGSSYSTEM FOR FISKERIAPPLIKASJONEN
 * 
 * Denne filen implementerer applikasjonens interaktive veiledningssystem (tutorial) som
 * hjelper nye brukere med å lære hvordan applikasjonen fungerer.
 * 
 * Nøkkelfunksjonalitet:
 * - Steg-for-steg veiledning med instruksjoner og forklaringer
 * - Visuell overlay som fokuserer på viktige elementer i brukergrensesnittet
 * - Interaktiv maskot som følger brukeren gjennom veiledningen
 * - Tilpasset plassering av elementer basert på skjermstørrelse og innhold
 * 
 * Systemet består av:
 * - TutorialOverlay: Hovedkomponenten som viser veiledningen over applikasjonen
 * - MascotPosition: Dataklasse for håndtering av maskotens plassering
 * - getMascotPosition: Funksjon som beregner optimal plassering basert på gjeldende steg
 * 
 * Veiledningen er designet for å være intuitiv og engasjerende, med en vennlig
 * maskot som guider brukeren gjennom applikasjonens hovedfunksjoner.
 * Dette bidrar til å redusere læringskurven og gjøre applikasjonen mer
 * tilgjengelig for nye brukere.
 */

@Composable
fun TutorialOverlay(
    state: TutorialState,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    if (!state.isVisible || state.isCompleted) return

    val currentStep = state.currentStepData ?: return
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                .widthIn(max = min(250.dp, screenWidth * 0.65f))
                .padding(16.dp)
                .align(Alignment.Center),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
                ) {
                    Column(
                    modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = currentStep.title,
                        style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                    Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = currentStep.description,
                            style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                    Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (currentStep.isLastStep) {
                                Spacer(Modifier.weight(1f))
                                Button(
                                    onClick = onNext,
                                modifier = Modifier.padding(horizontal = 4.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                Text("Kom i gang!", style = MaterialTheme.typography.labelMedium)
                                }
                                Spacer(Modifier.weight(1f))
                            } else {
                            TextButton(
                                onClick = onSkip,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Hopp over", style = MaterialTheme.typography.labelMedium)
                            }

                            TextButton(
                                onClick = onNext,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Neste", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                if (currentStep.mascotResourceId != null) {
                    val mascotPosition = getMascotPosition(
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        step = currentStep,
                        defaultSize = 140.dp
                    )

                    if (currentStep.isLastStep) {
                        AndroidView(
                            factory = { context ->
                                ImageView(context).apply {
                                    setBackgroundResource(currentStep.mascotResourceId)
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                    adjustViewBounds = true
                                    (background as? AnimationDrawable)?.start()
                                }
                            },
                            modifier = Modifier
                                .size(mascotPosition.size)
                                .align(mascotPosition.alignment)
                                .offset(x = mascotPosition.offset.x, y = mascotPosition.offset.y)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = currentStep.mascotResourceId),
                            contentDescription = null,
                            modifier = Modifier
                                .size(mascotPosition.size)
                                .align(mascotPosition.alignment)
                                .offset(x = mascotPosition.offset.x, y = mascotPosition.offset.y),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }


data class MascotPosition(
    val alignment: Alignment,
    val offset: androidx.compose.ui.unit.DpOffset,
    val size: androidx.compose.ui.unit.Dp
)

@Composable
fun getMascotPosition(
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp,
    step: TutorialStep,
    defaultSize: androidx.compose.ui.unit.Dp = 140.dp
): MascotPosition {
    val widthFactor = screenWidth / 360.dp
    val heightFactor = screenHeight / 720.dp

    val kartPos = androidx.compose.ui.unit.DpOffset(
        x = screenWidth * 0.125f,
        y = screenHeight * 0.95f
    )

    when (step.title) {
        "Velkommen til Sjøspor!" -> {
            return MascotPosition(
                alignment = Alignment.Center,
                offset = androidx.compose.ui.unit.DpOffset(screenWidth * 0.35f, 0.dp),
                size = 160.dp * widthFactor
            )
        }

        "Utforsk kartet" -> {
            return MascotPosition(
                alignment = Alignment.BottomStart,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = kartPos.x * 1.6f,
                    y = ((-5).dp) * heightFactor
                ),
                size = 140.dp * widthFactor
            )
        }

        "Profilsiden din" -> {
            return MascotPosition(
                alignment = Alignment.BottomEnd,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = ((-70).dp) * widthFactor,
                    y = ((-5).dp) * heightFactor
                ),
                size = 140.dp * widthFactor
            )
        }

        "Finn et sted" -> {
            return MascotPosition(
                alignment = Alignment.TopCenter,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = 0.dp,
                    y = 60.dp * heightFactor
                ),
                size = 150.dp * widthFactor
            )
        }

        "Lyst til å sjekke værmeldingen?" -> {
            return MascotPosition(
                alignment = Alignment.BottomCenter,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = (-50).dp * widthFactor,
                    y = (-5).dp * heightFactor
                ),
                size = 140.dp * widthFactor
            )
        }

        "Båtvettregler" -> {
            return MascotPosition(
                alignment = Alignment.BottomEnd,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = (-65).dp * widthFactor,
                    y = ((-20).dp * heightFactor) * 0.05f
                ),
                size = 140.dp * widthFactor
            )
        }

        "Fiskeloggen" -> {
            return MascotPosition(
                alignment = Alignment.BottomEnd,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = (-65).dp * widthFactor,
                    y = ((-85).dp * heightFactor) * 0.5f
                ),
                size = 140.dp * widthFactor
            )
        }

        "Start en fisketur" -> {
            return MascotPosition(
                alignment = Alignment.BottomEnd,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = (-65).dp * widthFactor,
                    y = ((-140).dp * heightFactor) * 0.6f
                ),
                size = 140.dp * widthFactor
            )
        }

        "Klar for å bruke Sjøspor!" -> {
            return MascotPosition(
                alignment = Alignment.Center,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = screenWidth * 0.4f,
                    y = 0.dp
                ),
                size = 160.dp * widthFactor
            )
        }

        else -> {
            return when (step.mascotPlacement) {
                MascotPlacement.CENTER -> MascotPosition(
                    alignment = Alignment.Center,
                    offset = step.mascotOffset,
                    size = defaultSize * widthFactor
                )

                MascotPlacement.LEFT -> MascotPosition(
                    alignment = Alignment.CenterStart,
                    offset = androidx.compose.ui.unit.DpOffset(30.dp * widthFactor, 0.dp),
                    size = defaultSize * widthFactor
                )

                MascotPlacement.RIGHT -> MascotPosition(
                    alignment = Alignment.CenterEnd,
                    offset = androidx.compose.ui.unit.DpOffset((-30).dp * widthFactor, 0.dp),
                    size = defaultSize * widthFactor
                )

                MascotPlacement.TOP -> MascotPosition(
                    alignment = Alignment.TopCenter,
                    offset = androidx.compose.ui.unit.DpOffset(0.dp, 30.dp * heightFactor),
                    size = defaultSize * widthFactor
                )

                MascotPlacement.BOTTOM -> MascotPosition(
                    alignment = Alignment.BottomCenter,
                    offset = androidx.compose.ui.unit.DpOffset(0.dp, (-30).dp * heightFactor),
                    size = defaultSize * widthFactor
                )

                MascotPlacement.AUTO -> MascotPosition(
                    alignment = Alignment.Center,
                    offset = androidx.compose.ui.unit.DpOffset(screenWidth * 0.3f, 0.dp),
                    size = defaultSize * widthFactor
                )
            }
        }
    }
}

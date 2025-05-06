package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial

import android.graphics.drawable.AnimationDrawable
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.viewinterop.AndroidView
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import kotlin.math.max

@Composable
fun TutorialOverlay(
    state: TutorialState,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.isVisible || state.isCompleted) return

    val currentStep = state.currentStepData ?: return
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        
        // Semi-transparent overlay without highlight cutout
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
                
                if (currentStep.title == "Da er du klar !") {
                            AndroidView(
                                factory = { context ->
                                    ImageView(context).apply {
                                        setBackgroundResource(R.drawable.waving_mascot)
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

private data class MascotPosition(
    val alignment: Alignment,
    val offset: androidx.compose.ui.unit.DpOffset,
    val size: androidx.compose.ui.unit.Dp
)

@Composable
private fun getMascotPosition(
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
    val værPos = androidx.compose.ui.unit.DpOffset(
        x = screenWidth * 0.5f,
        y = screenHeight * 0.95f
    )
    val profilPos = androidx.compose.ui.unit.DpOffset(
        x = screenWidth * 0.875f,
        y = screenHeight * 0.95f
    )
    val fisketurPos = androidx.compose.ui.unit.DpOffset(
        x = screenWidth * 0.93f,
        y = screenHeight * 0.75f
    )
    val fiskeloggPos = androidx.compose.ui.unit.DpOffset(
        x = screenWidth * 0.93f,
        y = screenHeight * 0.82f
    )
    val båtvettPos = androidx.compose.ui.unit.DpOffset(
        x = screenWidth * 0.93f,
        y = screenHeight * 0.89f
    )
    val søkPos = androidx.compose.ui.unit.DpOffset(
        x = screenWidth * 0.5f,
        y = screenHeight * 0.1f
    )
    
    when (step.title) {
        "Velkommen til appen!" -> {
            return MascotPosition(
                alignment = Alignment.Center,
                offset = androidx.compose.ui.unit.DpOffset(screenWidth * 0.35f, 0.dp),
                size = 160.dp * widthFactor
            )
        }
        "Kartvisning" -> {
            return MascotPosition(
                alignment = Alignment.BottomStart,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = kartPos.x * 1.6f,
                    y = -5.dp * heightFactor
                ),
                size = 140.dp * widthFactor
            )
        }
        "Profilsiden" -> {
            return MascotPosition(
                alignment = Alignment.BottomEnd,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = -70.dp * widthFactor,
                    y = -5.dp * heightFactor
                ),
                size = 140.dp * widthFactor
            )
        }
        "Søkefunksjonen" -> {
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
                    x = -50.dp * widthFactor,
                    y = -5.dp * heightFactor
                ),
                size = 140.dp * widthFactor
            )
        }
        "Båtvettregler" -> {
            return MascotPosition(
                alignment = Alignment.BottomEnd,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = -65.dp * widthFactor,
                    y = (-20.dp * heightFactor) * 0.05f
                ),
                size = 140.dp * widthFactor
            )
        }
        "Fiskeloggen" -> {
            return MascotPosition(
                alignment = Alignment.BottomEnd,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = -65.dp * widthFactor,
                    y = (-85.dp * heightFactor) * 0.5f
                ),
                size = 140.dp * widthFactor
            )
        }
        "Fisketuren" -> {
            return MascotPosition(
                alignment = Alignment.BottomEnd,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = -65.dp * widthFactor,
                    y = (-140.dp * heightFactor) * 0.6f
                ),
                size = 140.dp * widthFactor
            )
        }
        "Da er du klar !" -> {
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
                    offset = androidx.compose.ui.unit.DpOffset(-30.dp * widthFactor, 0.dp),
                    size = defaultSize * widthFactor
                )
                MascotPlacement.TOP -> MascotPosition(
                    alignment = Alignment.TopCenter,
                    offset = androidx.compose.ui.unit.DpOffset(0.dp, 30.dp * heightFactor),
                    size = defaultSize * widthFactor
                )
                MascotPlacement.BOTTOM -> MascotPosition(
                    alignment = Alignment.BottomCenter,
                    offset = androidx.compose.ui.unit.DpOffset(0.dp, -30.dp * heightFactor),
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

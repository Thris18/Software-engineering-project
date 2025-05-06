package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial

import android.graphics.drawable.AnimationDrawable
import android.widget.ImageView
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

@Composable
fun TutorialOverlay(
    state: TutorialState,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.isVisible || state.isCompleted) return

    val currentStep = state.currentStepData ?: return
    val highlightedBounds = state.highlightedBounds
    
    // Adaptive layout for tutorial overlay
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Tilgang til skjermens dimensjoner
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        
        // Semi-transparent overlay with highlight cutout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .drawWithContent {
                    drawContent()
                    
                    // Draw highlight if we have bounds
                    highlightedBounds?.let { bounds ->
                        drawHighlight(
                            bounds = bounds,
                            config = currentStep.highlightConfig
                        )
                    }
                }
        )
        
        // Tutorial dialog with responsive positioning
        Box(modifier = Modifier.fillMaxSize()) {
            // Position dialog at center of screen regardless of highlight position
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
                            // Kun "Kom i gang" for siste steg
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
                            // "Hopp over" og "Neste" for andre steg
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
                
            // Mascot with adaptive positioning
                if (currentStep.mascotResourceId != null) {
                // Plasser maskotten basert på adaptive regler og highlight position
                val mascotPosition = getMascotPosition(
                    targetBounds = highlightedBounds,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    step = currentStep,
                    defaultSize = 140.dp
                )
                
                if (currentStep.title == "Da er du klar !") {
                    // Vingende maskot for siste steg
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
                    // Standard maskot for andre steg
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

// Helper-funksjon for å tegne highlight med ønsket form og stil
private fun DrawScope.drawHighlight(bounds: Rect, config: HighlightConfig) {
    // Legg til litt padding rundt det fremhevede elementet
    val highlightBounds = Rect(
        left = bounds.left - config.padding.toPx(),
        top = bounds.top - config.padding.toPx(),
        right = bounds.right + config.padding.toPx(),
        bottom = bounds.bottom + config.padding.toPx()
    )
    
    // Variabler for highlight-tegning
    val center = Offset(highlightBounds.center.x, highlightBounds.center.y)
    val radius = max(highlightBounds.width, highlightBounds.height) * 0.55f
    
    when (config.highlightShape) {
        HighlightShape.CIRCLE -> {
            // Tegn en sirkulær "hull" med BlendMode.Clear
            drawCircle(
                color = Color.White,
                radius = radius,
                center = center,
                blendMode = BlendMode.Clear
            )
            
            // Tegn en kontur rundt highlight-sirkelen
            drawCircle(
                color = Color(0xFF2196F3),
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        HighlightShape.RECTANGLE -> {
            // Tegn et rektangulært "hull"
            drawRect(
                color = Color.White,
                topLeft = Offset(highlightBounds.left, highlightBounds.top),
                size = androidx.compose.ui.geometry.Size(
                    width = highlightBounds.width,
                    height = highlightBounds.height
                ),
                blendMode = BlendMode.Clear
            )
            
            // Tegn en kontur rundt highlight-rektangelet
            drawRect(
                color = Color(0xFF2196F3),
                topLeft = Offset(highlightBounds.left, highlightBounds.top),
                size = androidx.compose.ui.geometry.Size(
                    width = highlightBounds.width,
                    height = highlightBounds.height
                ),
                style = Stroke(width = 2.dp.toPx())
            )
        }
        HighlightShape.OVAL -> {
            // Tegn en oval highlight (samme som sirkel for nå)
            drawCircle(
                color = Color.White,
                radius = radius,
                center = center,
                blendMode = BlendMode.Clear
            )
            
            drawCircle(
                color = Color(0xFF2196F3),
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

// Helper-funksjon for å bestemme hvor dialogen skal plasseres
@Composable
private fun getDialogAlignment(
    bounds: Rect?,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp
): Alignment {
    if (bounds == null) return Alignment.Center
    
    val screenWidthPx = with(LocalDensity.current) { screenWidth.toPx() }
    val screenHeightPx = with(LocalDensity.current) { screenHeight.toPx() }
    
    // Beregn hvor på skjermen elementet befinner seg
    val centerX = bounds.center.x / screenWidthPx
    val centerY = bounds.center.y / screenHeightPx
    
    return when {
        // Element i øvre del av skjermen -> dialog nederst
        centerY < 0.3f -> Alignment.BottomCenter
        
        // Element i nedre del -> dialog øverst
        centerY > 0.7f -> Alignment.TopCenter
        
        // Element i venstre side -> dialog til høyre
        centerX < 0.3f -> Alignment.CenterEnd
        
        // Element i høyre side -> dialog til venstre
        centerX > 0.7f -> Alignment.CenterStart
        
        // Default: sentrert dialog
        else -> Alignment.Center
    }
}

// Data class for maskot-posisjonering
private data class MascotPosition(
    val alignment: Alignment,
    val offset: androidx.compose.ui.unit.DpOffset,
    val size: androidx.compose.ui.unit.Dp
)

// Helper-funksjon for å bestemme maskot-posisjon
@Composable
private fun getMascotPosition(
    targetBounds: Rect?,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp,
    step: TutorialStep,
    defaultSize: androidx.compose.ui.unit.Dp = 140.dp
): MascotPosition {
    // Beregn proporsjoner for adaptiv posisjonering
    val widthFactor = screenWidth / 360.dp  // Standard referansebredde
    val heightFactor = screenHeight / 720.dp // Standard referansehøyde
    
    // Definerer punkt der knappene er plassert (relativt til skjermstørrelse)
    val kartPos = androidx.compose.ui.unit.DpOffset(
        x = screenWidth * 0.125f, 
        y = screenHeight * 0.9f
    )
    val værPos = androidx.compose.ui.unit.DpOffset(
        x = screenWidth * 0.5f,
        y = screenHeight * 0.9f
    )
    val profilPos = androidx.compose.ui.unit.DpOffset(
        x = screenWidth * 0.875f,
        y = screenHeight * 0.9f
    )
    val fisketurPos = androidx.compose.ui.unit.DpOffset(
        x = screenWidth * 0.92f,
        y = screenHeight * 0.65f
    )
    val fiskeloggPos = androidx.compose.ui.unit.DpOffset(
        x = screenWidth * 0.92f,
        y = screenHeight * 0.75f
    )
    val båtvettPos = androidx.compose.ui.unit.DpOffset(
        x = screenWidth * 0.92f,
        y = screenHeight * 0.85f
    )
    val søkPos = androidx.compose.ui.unit.DpOffset(
        x = screenWidth * 0.5f,
        y = screenHeight * 0.1f
    )
    
    // Velg posisjon basert på hvilket steg vi er på
    when (step.title) {
        "Velkommen til appen!" -> {
            return MascotPosition(
                alignment = Alignment.Center,
                offset = androidx.compose.ui.unit.DpOffset(screenWidth * 0.25f, 0.dp),
                size = 160.dp * widthFactor
            )
        }
        "Kartvisning" -> {
            // Peker på kartknappen (nederst til venstre)
            return MascotPosition(
                alignment = Alignment.BottomStart,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = kartPos.x * 0.8f,
                    y = -30.dp * heightFactor
                ),
                size = 140.dp * widthFactor
            )
        }
        "Profilsiden" -> {
            // Peker på profilknappen (nederst til høyre)
            return MascotPosition(
                alignment = Alignment.BottomEnd,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = -30.dp * widthFactor,
                    y = -30.dp * heightFactor
                ),
                size = 140.dp * widthFactor
            )
        }
        "Søkefunksjonen" -> {
            // Peker på søkeknappen (øverst)
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
            // Peker på værknappen (nederst i midten)
            return MascotPosition(
                alignment = Alignment.BottomCenter,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = 0.dp,
                    y = -30.dp * heightFactor
                ),
                size = 140.dp * widthFactor
            )
        }
        "Båtvettregler" -> {
            // Peker på båtvettknappen (nederst til høyre)
            return MascotPosition(
                alignment = Alignment.BottomEnd,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = -30.dp * widthFactor,
                    y = -100.dp * heightFactor
                ),
                size = 140.dp * widthFactor
            )
        }
        "Fiskeloggen" -> {
            // Peker på fiskeloggknappen (midten til høyre)
            return MascotPosition(
                alignment = Alignment.CenterEnd,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = -30.dp * widthFactor,
                    y = 50.dp * heightFactor
                ),
                size = 140.dp * widthFactor
            )
        }
        "Fisketuren" -> {
            // Peker på fisketurknappen (øverst til høyre)
            return MascotPosition(
                alignment = Alignment.CenterEnd,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = -30.dp * widthFactor,
                    y = -50.dp * heightFactor
                ),
                size = 140.dp * widthFactor
            )
        }
        "Da er du klar !" -> {
            // Avsluttende maskot
            return MascotPosition(
                alignment = Alignment.Center,
                offset = androidx.compose.ui.unit.DpOffset(
                    x = screenWidth * 0.25f,
                    y = 0.dp
                ),
                size = 160.dp * widthFactor
            )
        }
        else -> {
            // Standard plassering basert på mascotPlacement
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
                    offset = androidx.compose.ui.unit.DpOffset(screenWidth * 0.25f, 0.dp),
                    size = defaultSize * widthFactor
                )
            }
        }
    }
}

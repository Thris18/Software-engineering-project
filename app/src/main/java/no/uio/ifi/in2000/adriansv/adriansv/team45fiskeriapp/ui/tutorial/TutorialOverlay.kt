package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun TutorialOverlay(
    state: TutorialState,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    highlightConfig: HighlightConfig = HighlightConfig()
) {
    if (!state.isVisible || state.isCompleted) return

    val currentStep = state.currentStepData ?: return
    val density = LocalDensity.current

    Dialog(
        onDismissRequest = onSkip,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .drawWithContent {
                    drawContent()
                    state.highlightedBounds?.let { bounds ->
                        drawHighlight(
                            bounds = bounds,
                            config = highlightConfig,
                            density = density
                        )
                    }
                }
        ) {
            TutorialPopup(
                step = currentStep,
                onNext = onNext,
                onSkip = onSkip,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun TutorialPopup(
    step: TutorialStep,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .widthIn(max = 320.dp)
            .padding(16.dp)
    ) {
        // Popup-innhold
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp)) // plass til maskot
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onSkip) { Text("Hopp over") }
                    Button(onClick = onNext) { Text("Neste") }
                }
            }
        }
        // Maskot, plassert med offset og posisjon
        Icon(
            painter = painterResource(id = step.mascotResourceId),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .align(
                    when (step.mascotPosition) {
                        MascotPosition.LEFT -> Alignment.CenterStart
                        MascotPosition.RIGHT -> Alignment.CenterEnd
                        MascotPosition.TOP -> Alignment.TopCenter
                        MascotPosition.BOTTOM -> Alignment.BottomCenter
                    }
                )
                .offset(step.mascotOffset.x, step.mascotOffset.y)
        )
    }
}

private fun DrawScope.drawHighlight(
    bounds: androidx.compose.ui.geometry.Rect,
    config: HighlightConfig,
    density: androidx.compose.ui.unit.Density
) {
    val padding = with(density) { config.padding.toPx() }
    val cornerRadius = with(density) { config.cornerRadius.toPx() }
    val blurRadius = with(density) { config.blurRadius.toPx() }

    // Draw blurred background
    drawRect(
        color = Color.Black.copy(alpha = 0.5f),
        topLeft = Offset.Zero,
        size = Size(size.width, size.height)
    )

    // Draw highlight area
    drawRoundRect(
        color = Color.Transparent,
        topLeft = Offset(bounds.left - padding, bounds.top - padding),
        size = Size(
            bounds.width + (padding * 2),
            bounds.height + (padding * 2)
        ),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
    )
} 
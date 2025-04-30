package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components

import android.graphics.drawable.AnimationDrawable
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import kotlin.math.min

@Composable
fun WelcomeScreen(onNavigateToHome: () -> Unit) {
    val pirateFont = FontFamily(Font(R.font.pirataone_regular))
    
    // Get screen dimensions for responsive sizing
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    // Calculate appropriate sizes based on screen dimensions
    val titleSize = min(screenWidth.value * 0.15f, 72f).sp
    val imageSize = min(screenWidth.value * 0.8f, 350f).dp
    val buttonWidth = min(screenWidth.value * 0.8f, 350f).dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFe6f0ff))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Responsive spacing at top (smaller on small screens)
            Spacer(modifier = Modifier.height((screenHeight.value * 0.05f).dp))

            // Appnavn med responsiv fontstørrelse
            Text(
                text = "AppNavn",
                style = TextStyle(
                    fontFamily = pirateFont,
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00334d)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height((screenHeight.value * 0.02f).dp))

            // Maskotbilde med responsiv størrelse
            Image(
                painter = painterResource(id = R.drawable.waving_mascot),
                contentDescription = "Vinkende sjømannsmaskott",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .widthIn(max = imageSize)
                    .padding(8.dp)
            )

            // Flexible space that grows or shrinks based on screen size
            Spacer(modifier = Modifier.weight(1f))

            // Fiskeanimasjon - større størrelse
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.fish_jump)
                    .decoderFactory(GifDecoder.Factory())
                    .build(),
                contentDescription = "Hoppende fisk",
                modifier = Modifier
                    .widthIn(max = (screenWidth.value * 0.3f).dp)
                    .padding(4.dp)
            )

            // Responsiv velkomstknapp - større
            Button(
                onClick = { onNavigateToHome() },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                modifier = Modifier
                    .widthIn(max = buttonWidth.coerceAtMost(320.dp))
                    .padding(vertical = 12.dp, horizontal = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = stringResource(id = R.string.start_button),
                    style = TextStyle(
                        fontSize = min(screenWidth.value * 0.045f, 22f).sp,
                        fontFamily = pirateFont,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF00334d)
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Responsive bottom spacing (smaller on small screens)
            Spacer(modifier = Modifier.height((screenHeight.value * 0.03f).dp))
        }
    }
}
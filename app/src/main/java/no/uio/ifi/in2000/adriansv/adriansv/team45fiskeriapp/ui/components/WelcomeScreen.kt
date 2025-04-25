package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R

@Composable
fun WelcomeScreen(onNavigateToHome: () -> Unit) {
    val pirateFont = FontFamily(Font(R.font.pirataone_regular))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFe6f0ff))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Appnavn med font
            Text(
                text = "AppNavn",
                style = TextStyle(
                    fontFamily = pirateFont,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00334d)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Maskotbilde
            Image(
                painter = painterResource(id = R.drawable.sailor_mascot),
                contentDescription = "Sjømannsmaskott",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(410.dp)
            )

            Spacer(modifier = Modifier.weight(2f))

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.fish_jump)
                    .decoderFactory(GifDecoder.Factory())
                    .build(),
                contentDescription = "Hoppende fisk",
                modifier = Modifier
                    .size(100.dp)
                    .offset(y = (-20).dp) // litt opp så den ser ut som den hopper på knappen
            )

            // Velkomst-knapp
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable { onNavigateToHome() }
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Start fisketuren!",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = pirateFont,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF00334d)
                    )
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R

/**
 * BÅTVETTREGLER FOR FISKERIAPPLIKASJONEN
 * 
 * Denne filen implementerer komponenter for å presentere viktige båtvettregler
 * for brukerne av appen, noe som bidrar til å fremme sikkerhet på sjøen.
 * 
 * Nøkkelfunksjonalitet:
 * - Presentasjon av de syv offisielle båtvettreglene i en oversiktlig dialog
 * - Visning av illustrative bilder for hver regel
 * - Flytende handlingsknapp for å åpne båtvettreglene fra kartvisningen
 * - Responsiv layout som tilpasser seg ulike skjermstørrelser
 * - Alterneringsmønster for visning av tekst og bilder (veksler mellom høyre og venstre)
 * 
 * Filen inneholder:
 * - BaatvettButton: Flytende knapp som gir tilgang til båtvettreglene
 * - BaatvettOverlay: Hovedkomponenten som viser båtvettreglene i en overlay
 * - BaatvettRule: Hjelpefunksjon for å vise individuelle regler
 * - Støttefunksjoner for layout og design
 * 
 * Denne komponenten bidrar til å øke sikkerhetsbevisstheten hos fiskere
 * ved å gjøre viktig sikkerhetsinformasjon lett tilgjengelig i appen.
 * Båtvettreglene er utformet av Sjøfartsdirektoratet og er et viktig
 * sikkerhetsbidrag for alle som ferdes på sjøen.
 */

@Composable
fun BaatvettButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.baatvettknapp),
            contentDescription = "Båtvett regler",
            modifier = Modifier.size(60.dp)
                .offset(x = (-3).dp, y = 0.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun BaatvettOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        // Main card
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 80.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Båtvettregler",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-8).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Lukk",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f, false)
                ) {
                    BaatvettRule(
                        number = 1,
                        text = "Tenk sikkerhet. Kunnskap og planlegging reduserer risikoen og øker trivselen.",
                        imageRes = R.drawable.regel1
                    )
                    BaatvettRule(
                        number = 2,
                        text = "Ta med nødvendig utstyr. Utstyret må holdes i orden og være lett tilgjengelig.",
                        imageRes = R.drawable.regel2
                    )
                    BaatvettRule(
                        number = 3,
                        text = "Respekter vær og farvann. Båten må bare benyttes under egnede forhold.",
                        imageRes = R.drawable.regel3
                    )
                    BaatvettRule(
                        number = 4,
                        text = "Følg Sjøveisreglene. Bestemmelsene om vikeplikt, hastighet og lanterneføring må overholdes.",
                        imageRes = R.drawable.regel4
                    )
                    BaatvettRule(
                        number = 5,
                        text = "Bruk redningsvest eller flyteplagg. Det er påbudt å ha på seg flyteutstyr om bord i fritidsbåter under 8 meter. Fritidsbåter f.o.m. 8 meter skal ha egnet flyteutstyr til alle om bord.",
                        imageRes = R.drawable.regel5
                    )
                    BaatvettRule(
                        number = 6,
                        text = "Vær uthvilt og edru. Promillegrensen er 0,8 når du fører båt.",
                        imageRes = R.drawable.regel6
                    )
                    BaatvettRule(
                        number = 7,
                        text = "Vis hensyn. Sikkerhet, miljø og trivsel er et felles ansvar.",
                        imageRes = R.drawable.regel7
                    )
                }
            }
        }
    }
}

@Composable
private fun BaatvettRule(
    modifier: Modifier = Modifier,
    number: Int,
    text: String,
    imageRes: Int? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Rule number with accent background
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Sjøvettregel nr. $number",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        // Content row with alternating layout
        if (imageRes != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (number % 2 == 1) {
                    // Odd numbered rules: Text left, Image right
                    TextContent(
                        text = text,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp)
                    )
                    RuleImage(
                        imageRes = imageRes,
                        modifier = Modifier.size(120.dp)
                    )
                } else {
                    // Even numbered rules: Image left, Text right
                    RuleImage(
                        imageRes = imageRes,
                        modifier = Modifier.size(120.dp)
                    )
                    TextContent(
                        text = text,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    )
                }
            }
        } else {
            // If no image, just show text
            TextContent(text = text)
        }

        if (number < 7) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun TextContent(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

@Composable
private fun RuleImage(
    imageRes: Int,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = null,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Fit
    )
}
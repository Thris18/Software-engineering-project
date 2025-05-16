package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri

/**
 * FISKETUR-OVERSIKT FOR FISKERIAPPLIKASJONEN
 * 
 * Denne filen implementerer skjermbildet som viser oversikten over brukerens 
 * tidligere fisketurer. Den fungerer som et arkiv over alle gjennomførte turer.
 * 
 * Nøkkelfunksjonalitet:
 * - Visning av alle lagrede fisketurer i kronologisk rekkefølge
 * - Sortering av fisketurer etter dato, antall fangster eller varighet
 * - Visning av skjermdumper/bilder fra hver fisketur
 * - Detaljert informasjon om hver tur (navn, varighet, dato, antall fangster)
 * - Mulighet for å slette tidligere fisketurer
 * - Lagring og lasting av fisketurdata fra lokal JSON-fil
 * 
 * Filen inneholder:
 * - FishingTrip: Datamodell for lagring av fisketur-informasjon
 * - FishingTripStorage: Hjelpeobjekt for lagring og lesing av fisketur-data
 * - FishingTripsScreen: Hovedkomponenten som viser oversikt over fisketurer
 * 
 * Denne skjermen gir fiskeren en verdifull historisk oversikt over sine 
 * fisketurer, som både fungerer som en logg og kan hjelpe med planlegging 
 * av fremtidige turer basert på tidligere erfaringer.
 */

// Datamodell for fishingtrip
data class FishingTrip(
    val name: String,
    val screenshotUri: String,
    val startTime: Long,
    val endTime: Long,
    val catchCount: Int = 0
)

// Help functions for saving and loading trips
object FishingTripStorage {
    private const val FILE_NAME = "fishing_trips.json"

    fun saveTrip(context: Context, trip: FishingTrip) {
        val trips = loadTrips(context).toMutableList()
        trips.add(trip)
        val file = File(context.filesDir, FILE_NAME)
        FileWriter(file).use { writer ->
            writer.write(Gson().toJson(trips))
        }
    }

    fun loadTrips(context: Context): List<FishingTrip> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        return try {
            FileReader(file).use { reader ->
                val type = object : TypeToken<List<FishingTrip>>() {}.type
                Gson().fromJson<List<FishingTrip>>(reader, type) ?: emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteTrip(context: Context, trip: FishingTrip) {
        val trips = loadTrips(context).toMutableList()
        trips.removeAll { it.name == trip.name && it.startTime == trip.startTime && it.endTime == trip.endTime }
        val file = File(context.filesDir, FILE_NAME)
        FileWriter(file).use { writer ->
            writer.write(Gson().toJson(trips))
        }
        try {
            val uri = trip.screenshotUri.toUri()
            val imageFile = File(uri.path ?: "")
            if (imageFile.exists()) imageFile.delete()
        } catch (_: Exception) {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishingTripsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var trips by remember { mutableStateOf(listOf<FishingTrip>()) }
    var sortType by remember { mutableStateOf("Dato") }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    fun reloadTrips() {
        trips = FishingTripStorage.loadTrips(context)
    }

    LaunchedEffect(Unit) {
        reloadTrips()
    }

    val sortedTrips = when (sortType) {
        "Dato" -> trips.sortedByDescending { it.endTime }
        "Antall fangst" -> trips.sortedByDescending { it.catchCount }
        "Varighet" -> trips.sortedByDescending { it.endTime - it.startTime }
        else -> trips
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mine fisketurer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tilbake")
                    }
                },
                actions = {
                    IconButton(onClick = { sortMenuExpanded = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sorter")
                    }
                    DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Dato") }, onClick = { sortType = "Dato"; sortMenuExpanded = false })
                        DropdownMenuItem(text = { Text("Antall fangst") }, onClick = { sortType = "Antall fangst"; sortMenuExpanded = false })
                        DropdownMenuItem(text = { Text("Varighet") }, onClick = { sortType = "Varighet"; sortMenuExpanded = false })
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(sortedTrips) { trip ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(trip.screenshotUri.toUri()),
                            contentDescription = "Bilde av fisketur",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedImageUri = trip.screenshotUri },
                            alignment = Alignment.Center
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = trip.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val duration = trip.endTime - trip.startTime
                            val hours = duration / 3600000
                            val minutes = (duration / 60000) % 60
                            val seconds = (duration / 1000) % 60
                            Text(
                                text = "Varighet: %02d:%02d:%02d".format(hours, minutes, seconds),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            val date = Date(trip.endTime)
                            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                            Text(
                                text = dateFormat.format(date),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Antall fangster: ${trip.catchCount}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = {
                            FishingTripStorage.deleteTrip(context, trip)
                            reloadTrips()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Slett fisketur")
                        }
                    }
                }
            }
        }
        if (selectedImageUri != null) {
            Dialog(onDismissRequest = { selectedImageUri = null }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri!!.toUri()),
                            contentDescription = "Stor visning av bilde",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 600.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Lukk", tint = Color.Black)
                        }
                    }
                }
            }
        }
    }
} 
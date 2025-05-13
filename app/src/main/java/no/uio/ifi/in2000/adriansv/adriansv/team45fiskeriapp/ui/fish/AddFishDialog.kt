package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish.FishLog
import java.util.Date
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import android.widget.Toast
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.PaddingValues

@Composable
fun AddFishDialog(
    onDismiss: () -> Unit,
    onAddFish: (FishLog) -> Unit,
    latitude: Double,
    longitude: Double,
    onSelectLocation: () -> Unit,
    initialFishType: String = "",
    initialLocation: String = "",
    initialArea: String = "",
    initialDescription: String = "",
    initialWeight: String = "",
    initialImageUri: Uri? = null,
    onFishTypeChange: (String) -> Unit = {},
    onLocationChange: (String) -> Unit = {},
    onAreaChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    onWeightChange: (String) -> Unit = {},
    onImageUriChange: (Uri?) -> Unit = {},
    selectedLocationForFish: String? = null
) {
    var fishType by remember { mutableStateOf(initialFishType) }
    var area by remember { mutableStateOf(initialArea) }
    var description by remember { mutableStateOf(initialDescription) }
    var weight by remember { mutableStateOf(initialWeight) }
    var imageUri by remember { mutableStateOf(initialImageUri) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    
    // Håndter bildeopptak
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                photoUri?.let { uri ->
                    imageUri = uri
                    onImageUriChange(uri)
                }
            }
        }
    )

    // Tillatelseshåndtering for kamera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Tillatelse gitt, start kamera
            photoUri?.let { uri ->
                launcher.launch(uri)
            }
        } else {
            // Tillatelse ikke gitt, vis melding til bruker
            Toast.makeText(
                context,
                "Kameratillatelse er nødvendig for å ta bilder",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Håndter bildevalg fra galleri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it
            onImageUriChange(it)
        }
    }

    // Opprett midlertidig fil for bilde
    val photoFile = remember {
        try {
            File.createTempFile(
                "PHOTO_${System.currentTimeMillis()}_",
                ".jpg",
                context.cacheDir
            )
        } catch (e: IOException) {
            null
        }
    }

    // Opprett URI for midlertidig fil
    LaunchedEffect(photoFile) {
        photoFile?.let { file ->
            photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        }
    }

    // Håndter bildeopptak med tillatelsessjekk
    fun takePicture() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Vi har allerede tillatelse, start kamera
                photoUri?.let { uri ->
                    launcher.launch(uri)
                }
            }
            else -> {
                // Be om tillatelse
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Håndter bildevalg fra galleri
    fun pickImage() {
        galleryLauncher.launch("image/*")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Legg til fisk") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Vis bilde hvis det er valgt
                if (imageUri != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Valgt bilde",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                OutlinedTextField(
                    value = fishType,
                    onValueChange = { 
                        fishType = it
                        onFishTypeChange(it)
                    },
                    label = { Text("Fisketype") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = area,
                    onValueChange = { 
                        area = it
                        onAreaChange(it)
                    },
                    label = { Text("Område") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { 
                        description = it
                        onDescriptionChange(it)
                    },
                    label = { Text("Beskrivelse") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = weight,
                    onValueChange = { newValue ->
                        // Tillat kun tall og punktum
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            weight = newValue
                            onWeightChange(newValue)
                        }
                    },
                    label = { Text("Vekt (kg)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Posisjonsknapp
                Button(
                    onClick = onSelectLocation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Velg posisjon",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Velg posisjon",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }
                }

                // Vis koordinater kun hvis brukeren har valgt et punkt gjennom kartet
                if (latitude != 0.0 && longitude != 0.0 && selectedLocationForFish != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "Valgt posisjon: ${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bildeknapper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { takePicture() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Camera, contentDescription = "Ta bilde")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ta bilde")
                    }

                    Button(
                        onClick = { pickImage() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = "Velg bilde")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Velg bilde")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (fishType.isNotBlank() && area.isNotBlank()) {
                        onAddFish(
                            FishLog(
                                fishType = fishType,
                                area = area,
                                description = description,
                                weight = weight.toFloatOrNull() ?: 0.0f,
                                imageUri = imageUri?.toString(),
                                latitude = latitude,
                                longitude = longitude,
                                timestamp = Date(System.currentTimeMillis())
                            )
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Legg til")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Avbryt")
            }
        }
    )
} 
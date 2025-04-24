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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

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
    onImageUriChange: (Uri?) -> Unit = {}
) {
    var fishType by remember { mutableStateOf(initialFishType) }
    var location by remember { mutableStateOf(initialLocation) }
    var area by remember { mutableStateOf(initialArea) }
    var description by remember { mutableStateOf(initialDescription) }
    var weight by remember { mutableStateOf(initialWeight) }
    var imageUri by remember { mutableStateOf(initialImageUri) }
    var photoFile by remember { mutableStateOf<File?>(null) }

    val context = LocalContext.current
    
    // Launcher for velge bilde fra galleri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        onImageUriChange(uri)
    }
    
    // Launcher for ta bilde med kamera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = Uri.fromFile(photoFile)
            onImageUriChange(imageUri)
        } else {
            imageUri = null
            onImageUriChange(null)
            photoFile = null
        }
    }

    // Launcher for kamera-tillatelse
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Opprett en midlertidig fil for bildet
            photoFile = File.createTempFile(
                "JPEG_${System.currentTimeMillis()}_",
                ".jpg",
                context.cacheDir
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile!!
            )
            cameraLauncher.launch(uri)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Legg til fisk") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fishType,
                    onValueChange = { 
                        fishType = it
                        onFishTypeChange(it)
                    },
                    label = { Text("Type fisk") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { 
                        location = it
                        onLocationChange(it)
                    },
                    label = { Text("Sted") },
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
                    value = weight,
                    onValueChange = { 
                        weight = it
                        onWeightChange(it)
                    },
                    label = { Text("Vekt (kg)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { 
                        description = it
                        onDescriptionChange(it)
                    },
                    label = { Text("Beskrivelse") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Button(
                    onClick = onSelectLocation,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Velg posisjon")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Velg fangststed på kartet")
                }

                if (latitude != 59.9139 || longitude != 10.7522) {
                    Text(
                        "Valgt posisjon: ${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            when {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    // Opprett en midlertidig fil for bildet
                                    photoFile = File.createTempFile(
                                        "JPEG_${System.currentTimeMillis()}_",
                                        ".jpg",
                                        context.cacheDir
                                    )
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        photoFile!!
                                    )
                                    cameraLauncher.launch(uri)
                                }
                                else -> {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Camera, contentDescription = "Ta bilde")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ta bilde")
                    }

                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = "Velg bilde")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Velg bilde")
                    }
                }

                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Valgt bilde",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (fishType.isNotBlank() && location.isNotBlank() && area.isNotBlank()) {
                        onAddFish(
                            FishLog(
                                fishType = fishType,
                                location = location,
                                area = area,
                                description = description,
                                weight = weight.toFloatOrNull(),
                                imageUri = imageUri?.toString(),
                                latitude = latitude,
                                longitude = longitude
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
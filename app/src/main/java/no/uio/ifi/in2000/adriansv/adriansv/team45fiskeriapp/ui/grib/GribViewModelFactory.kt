package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib.GribRepository

class GribViewModelFactory(
    private val gribRepository: GribRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GribViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GribViewModel(gribRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 
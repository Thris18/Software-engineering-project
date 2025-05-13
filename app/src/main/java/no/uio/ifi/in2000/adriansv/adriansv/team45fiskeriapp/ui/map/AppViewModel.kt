package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel : ViewModel() {
    private val _currentRoute = MutableStateFlow("welcome")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _showGrib = MutableStateFlow(true)
    val showGrib: StateFlow<Boolean> = _showGrib.asStateFlow()

    private val _showAlerts = MutableStateFlow(true)
    val showAlerts: StateFlow<Boolean> = _showAlerts.asStateFlow()

    private val _showShips = MutableStateFlow(true)
    val showShips: StateFlow<Boolean> = _showShips.asStateFlow()

    private val _isComingFromWelcome = MutableStateFlow(false)
    val isComingFromWelcome: StateFlow<Boolean> = _isComingFromWelcome.asStateFlow()

    private val _hasTutorialBeenShown = MutableStateFlow(false)
    val hasTutorialBeenShown: StateFlow<Boolean> = _hasTutorialBeenShown.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _showYourInfo = MutableStateFlow(false)
    val showYourInfo: StateFlow<Boolean> = _showYourInfo.asStateFlow()

    private val _firstName = MutableStateFlow("")
    val firstName: StateFlow<String> = _firstName.asStateFlow()

    private val _lastName = MutableStateFlow("")
    val lastName: StateFlow<String> = _lastName.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _profileImageUri = MutableStateFlow<String?>(null)
    val profileImageUri: StateFlow<String?> = _profileImageUri.asStateFlow()

    fun updateCurrentRoute(route: String) {
        _currentRoute.value = route
    }

    fun updateDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
    }

    fun updateShowGrib(show: Boolean) {
        _showGrib.value = show
    }

    fun updateShowAlerts(show: Boolean) {
        _showAlerts.value = show
    }

    fun updateShowShips(show: Boolean) {
        _showShips.value = show
    }

    fun updateIsComingFromWelcome(coming: Boolean) {
        _isComingFromWelcome.value = coming
    }

    fun updateHasTutorialBeenShown(shown: Boolean) {
        _hasTutorialBeenShown.value = shown
    }

    fun updateShowSettings(show: Boolean) {
        _showSettings.value = show
    }

    fun updateShowYourInfo(show: Boolean) {
        _showYourInfo.value = show
    }

    fun updateFirstName(name: String) {
        _firstName.value = name
    }

    fun updateLastName(name: String) {
        _lastName.value = name
    }

    fun updatePhoneNumber(number: String) {
        _phoneNumber.value = number
    }

    fun updateEmail(email: String) {
        _email.value = email
    }

    fun updateProfileImageUri(uri: String?) {
        _profileImageUri.value = uri
    }
} 
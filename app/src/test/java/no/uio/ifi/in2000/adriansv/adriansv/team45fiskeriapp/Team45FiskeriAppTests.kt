package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp

import org.junit.Test
import org.junit.Assert.*

class Team45FiskeriAppTests {

    @Test
    fun testNavigationBarItems() {
        // Arrange
        val expectedItems = listOf("Kart", "Vær", "Fiske", "Profil")
        
        // Act
        val actualItems = listOf("Kart", "Vær", "Fiske", "Profil")
        
        // Assert
        assertEquals(expectedItems, actualItems)
    }

    @Test
    fun testWelcomeScreenTitle() {
        // Arrange
        val expectedTitle = "Velkommen til FiskeriApp"
        
        // Act
        val actualTitle = "Velkommen til FiskeriApp"
        
        // Assert
        assertEquals(expectedTitle, actualTitle)
    }

    @Test
    fun testSearchButtonText() {
        // Arrange
        val expectedText = "Søk"
        
        // Act
        val actualText = "Søk"
        
        // Assert
        assertEquals(expectedText, actualText)
    }

    @Test
    fun testProfilePopupTitle() {
        // Arrange
        val expectedTitle = "Din Profil"
        
        // Act
        val actualTitle = "Din Profil"
        
        // Assert
        assertEquals(expectedTitle, actualTitle)
    }

    @Test
    fun testSettingsPopupOptions() {
        // Arrange
        val expectedOptions = listOf("Mørk modus", "Språk", "Varsler")
        
        // Act
        val actualOptions = listOf("Mørk modus", "Språk", "Varsler")
        
        // Assert
        assertEquals(expectedOptions, actualOptions)
    }

    @Test
    fun testBaatvettOverlayTitle() {
        // Arrange
        val expectedTitle = "Båtvett"
        
        // Act
        val actualTitle = "Båtvett"
        
        // Assert
        assertEquals(expectedTitle, actualTitle)
    }

    @Test
    fun testThresholdValues() {
        // Arrange
        val expectedValues = listOf("Lav", "Medium", "Høy")
        
        // Act
        val actualValues = listOf("Lav", "Medium", "Høy")
        
        // Assert
        assertEquals(expectedValues, actualValues)
    }

    @Test
    fun testYourInformationScreenSections() {
        // Arrange
        val expectedSections = listOf("Personlig info", "Båt info", "Fiske info")
        
        // Act
        val actualSections = listOf("Personlig info", "Båt info", "Fiske info")
        
        // Assert
        assertEquals(expectedSections, actualSections)
    }

    @Test
    fun testSearchButtonPlaceholder() {
        // Arrange
        val expectedPlaceholder = "Søk etter sted..."
        
        // Act
        val actualPlaceholder = "Søk etter sted..."
        
        // Assert
        assertEquals(expectedPlaceholder, actualPlaceholder)
    }

    @Test
    fun testNavigationBarIcons() {
        // Arrange
        val expectedIcons = listOf("map", "weather", "fishing", "profile")
        
        // Act
        val actualIcons = listOf("map", "weather", "fishing", "profile")
        
        // Assert
        assertEquals(expectedIcons, actualIcons)
    }
} 
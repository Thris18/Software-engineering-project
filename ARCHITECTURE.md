# Arkitektur - Sjøspor

## Innledning
Dette dokumentet beskriver arkitekturen til Sjøspor, en Android-applikasjon utviklet for fiskere. Dokumentet er primært rettet mot utviklere som skal videreutvikle appen og sette seg inn i kodestrukturen.

## Teknisk Stack

### Android Versjon
- **Minimum SDK**: 28 (Android 9.0)
- **Target SDK**: 35 (Android 14)
- **Compile SDK**: 35

Valget av minimum SDK 28 gir oss tilgang til moderne Android-funksjoner samtidig som vi dekker 90% av aktive Android-enheter. Target SDK 35 sikrer at vi følger de nyeste Android-retningslinjene og har tilgang til de nyeste API-ene.

### Hovedteknologier
- **Kotlin**: Primært programmeringsspråk
- **Jetpack Compose**: UI-rammeverk
- **Material3**: Designsystem
- **MapLibre GL**: Kartløsning
- **OkHttp**: Nettverkskommunikasjon
- **Coil**: Bildehåndtering
- **Location Services**: Lokasjonstjenester
- **FileProvider**: Filhåndtering for bilder

## Arkitekturmønster

### MVVM (Model-View-ViewModel)
Appen følger MVVM-arkitekturmønsteret, som er anbefalt av Google for Android-utvikling, og anbefalt struktur for IN2000 prosjekter:

- **Model**: Data og forretningslogikk
  - Repositories: (GeoJsonRepository, GribRepository, WeatherRepository, ShipRepository)
  - Data klasser: (GeoJsonDataSource, GribDataSource, WeatherDataSource, ShipDataSource)
  - API-klienter: (Locationforecast 2.0, METalerts 2.0, GRIB files 1.1, Barentswatch AIS API/v1/latest/combined)

- **View**: UI-komponenter
  - Compose-baserte skjermer (MapScreen, WeatherScreen, ProfileScreen)
  - Gjenbrukbare UI-komponenter (NavigationBar, SettingsPopup)
  - Navigation (NavigationHandler)

- **ViewModel**: Tilstandshåndtering
  - AppViewModel for global tilstand
  - WeatherViewModel for værdata
  - FishLogViewModel for fiskelogger
  - ShipViewModel for skipsposisjoner

### Unidirectional Data Flow (UDF)
Appen implementerer UDF-prinsippet for å sikre forutsigbar tilstandshåndtering:

1. **State**: StateFlow for observabel tilstand
   - Global app-tilstand i AppViewModel
   - Skjermspesifikk tilstand i respektive ViewModels

2. **Event**: Brukerhandlinger og systemhendelser
   - UI-events håndteres i Composable-funksjoner
   - System-events (lokasjon, kamera) håndteres via ActivityResultLauncher

3. **Reducer**: Funksjoner som oppdaterer tilstand
   - ViewModel-funksjoner for tilstandsoppdateringer
   - Repository-funksjoner for datahåndtering

4. **Side Effects**: Asynkrone operasjoner
   - Coroutines for asynkron kjøring
   - Flow for reaktiv datastrøm

## Prosjektstruktur

```
app/
├── core/           # Kjernefunksjonalitet
│   └── utils/     # Hjelpefunksjoner
├── data/          # Data lag
│   ├── api/      # API-klienter
│   ├── models/   # Data modeller 
│   └── repositories/ # Data repositories
├── model/         # Domena modeller
├── ui/           # UI lag
│   ├── components/ # Gjenbrukbare komponenter
│   ├── fish/     # Fiskelogg-relaterte skjermer
│   ├── map/      # Kart-relaterte skjermer
│   ├── navigation/ # Navigasjonshåndtering
│   ├── theme/    # UI tema
│   ├── tutorial/ # Veiledningskomponenter
│   └── weather/  # Vær-relaterte skjermer
└── MainActivity.kt # App entry point
```

## Design Prinsipper

### Lav Kobling
- Bruk av dependency injection via ViewModelFactory
- Interface-basert design for repositories
- Modulær arkitektur med klare grenser
- Repository pattern for datahåndtering

### Høy Kohesjon
- Klasser har et enkelt, veldefinert ansvar
- Relatert funksjonalitet er gruppert sammen
- Tydelig separasjon av bekymringer

### Design Patterns
1. **Repository Pattern**
   - Abstraherer data kilder
   - Enkelt API for data tilgang
   - Håndterer caching og synkronisering

2. **Observer Pattern**
   - StateFlow for reaktiv tilstandshåndtering
   - Event-basert kommunikasjon

3. **Factory Pattern**
   - ViewModelFactory for ViewModel-opprettelse
   - Dependency injection

4. **Strategy Pattern**
   - Fleksibel håndtering av ulike kartlag
   - Konfigurerbar værdata-visning

## Vedlikehold og Videreutvikling

### Kodekvalitet
- "Kotlin coding conventions"
- Dokumentasjon av offentlige API-er 
- Unit testing av kritisk forretningslogikk
- UI testing av hovedflyter

### Skalerbarhet
- Modulær design for enkel utvidelse
- Klar separasjon av bekymringer
- Dokumenterte grensesnitt

### Ytelse
- Effektiv håndtering av minne
- Asynkron operasjoner med Coroutines
- Caching av data
- Rask bildehåndtering med Coil

## Drift og Vedlikehold

### Utviklingsmiljø
- Android Studio Arctic Fox eller nyere
- Gradle 8.0 eller nyere
- JDK 11 eller nyere
- Git for versjonskontroll

### Byggeprosess
1. Klon repositoriet
2. Åpne prosjektet i Android Studio
3. La Gradle synkronisere avhengigheter
4. Bygg prosjektet med `./gradlew build`

### Vedlikehold
- Regelmessig oppdatering av avhengigheter
- Følge Android Studio og Gradle oppdateringer
- Holde seg oppdatert på Android API-endringer

### Feilsøking
- Logging implementert for viktige operasjoner
- Crashlytics for produksjonsmiljø
- Debug-versjoner med ekstra logging

### API-integrasjoner
- Locationforecast 2.0: Værdata
- METalerts 2.0: Varsler
- GRIB files 1.1: Værprognoser
- Barentswatch AIS API: Skipsposisjoner

### Sikkerhet
- API-nøkler lagres i lokale properties
- Sensitive data håndteres via Android Keystore
- Filhåndtering følger Android best practices

### Ytelsesoptimalisering
- Bildekomprimering for fiskelogger
- Caching av værdata
- Effektiv håndtering av kartlag

## Konklusjon
Sjøspor er bygget med fokus på vedlikeholdbarhet, skalerbarhet og god brukeropplevelse. Ved å følge moderne Android-utviklingsprinsipper og etablere en solid arkitektur, er appen godt forberedt for videreutvikling og vedlikehold. Dokumentasjonen og kodebasen er strukturert for å gjøre det enkelt for nye utviklere å sette seg inn i prosjektet og bidra til videreutvikling. 
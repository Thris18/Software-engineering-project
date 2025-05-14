# Appnavn: Sjøspor 

En Android-applikasjon utviklet for fiskere som gir tilgang til værdata, skipsposisjoner, og mulighet for å logge fangster.

## Krav til systemet

- Android 7.0 (API level 24) eller nyere
- Internettilkobling
- GPS/lokasjonstjenester
- Kamera (valgfritt, for å ta bilder av fangster)

## Installasjon
1. Klon repositoriet: 
git clone https://github.com/Team45FiskeriApp/Team45FiskeriApp.git

2. Åpne prosjektet i Android Studio 

3. Bygg og kjør appen på en emulator eller fysisk enhet

## Funksjoner

- Værdata, farevarsler og prognoser for fiskere
- Skipsposisjoner i sanntid
- Fiskelogg for å registrere fangster
- Dark og light mode toggle > profil > innstillinger 
- Profilside med personlig informasjon
- Kart med ulike lag (vær, skip, farevarsler)

## Brukte biblioteker

### Kart og lokasjon
- **MapLibre GL**: Brukes for å vise interaktive kart med ulike lag
- **OSMDroid**: Alternativ kartløsning
- **Google Play Services Location**: For å håndtere lokasjonstjenester

### UI og design
- **Jetpack Compose**: Moderne UI-rammeverk for Android
- **Material3**: Designsystem for moderne Android-apps
- **Lottie**: For animasjoner og GIF-støtte
- **Coil**: For bildehåndtering og caching

### Data og nettverk
- **OkHttp**: For nettverkskommunikasjon
- **NetCDF/GRIB**: For å parse værdata og prognoser
- **JSON**: For datahåndtering

### Andre viktige biblioteker
- **ThreeTenABP**: For håndtering av datoer og tidssoner
- **Lifecycle Components**: For å håndtere app-tilstand og livssyklus

## Utviklere

1. Adrian Andersen Svindahl – adriansv@uio.no
2. Thrisanth Thambirajah – thrisant@uio.no
3. Aayan Ali – aayana@uio.no
4. Carl Orrall - carlorr@uio.no
5. Hedda Hinderlind Misje – heddahmi@uio.no 
6. Simen Brunvatne – simebru@uio.no 


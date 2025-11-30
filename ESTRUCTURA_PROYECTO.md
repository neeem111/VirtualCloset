# 📁 ESTRUCTURA DEL PROYECTO - VIRTUAL CLOSET

## 📊 Árbol de Archivos

```
VirtualCloset/                              ← Raíz del proyecto
│
├── 📋 DOCUMENTACIÓN (Generada para ti)
│   ├── INDICE_DOCUMENTACION.md             ← ⭐ EMPIEZA AQUÍ
│   ├── CAMBIOS_RESUMEN.md                  ← Qué cambió
│   ├── GUIA_PRUEBAS_COMPLETA.md            ← Cómo probar
│   ├── REFERENCIA_TECNICA.md               ← Detalles técnicos
│   ├── COMPILACION_COMANDOS.md             ← Cómo compilar
│   ├── FIXES_APPLIED.md                    ← Correcciones
│   └── [Este archivo]
│
├── 🔧 CONFIGURACIÓN DEL PROYECTO
│   ├── build.gradle.kts                    ← Compilación principal
│   ├── settings.gradle.kts                 ← Settings global
│   ├── gradle.properties                   ← Propiedades gradle
│   ├── local.properties                    ← Configuración local
│   ├── CHALLENGES.md                       ← Desafíos del proyecto
│   │
│   └── gradle/
│       ├── libs.versions.toml               ← Versiones de librerías
│       └── wrapper/
│           ├── gradle-wrapper.jar
│           └── gradle-wrapper.properties
│
├── 📦 CÓDIGO DE LA APLICACIÓN
│   └── app/
│       │
│       ├── build.gradle.kts                 ← Config compilación de app
│       ├── proguard-rules.pro               ← Obfuscación ProGuard
│       │
│       ├── 🎨 RECURSOS (res/)
│       │   ├── values/
│       │   │   ├── strings.xml              ← Textos en inglés
│       │   │   ├── colors.xml
│       │   │   ├── themes.xml
│       │   │   └── [otros]
│       │   │
│       │   ├── values-es/
│       │   │   ├── strings.xml              ← Textos en español
│       │   │   └── [traducción]
│       │   │
│       │   ├── drawable/
│       │   │   ├── leopard_background.png
│       │   │   ├── default_white_top.png
│       │   │   ├── default_jeans.png
│       │   │   └── [otros assets]
│       │   │
│       │   └── mipmap/
│       │       ├── ic_launcher.xml
│       │       └── [iconos]
│       │
│       ├── 📱 CÓDIGO FUENTE (src/)
│       │   ├── main/
│       │   │   ├── AndroidManifest.xml      ← Manifiesto de la app
│       │   │   │
│       │   │   ├── java/com/example/virtualcloset/
│       │   │   │   └── MainActivity.kt       ← ⭐ AQUÍ ESTÁN LOS CAMBIOS
│       │   │   │       ├── SharedViewModel (línea 68)
│       │   │   │       ├── MainActivity (línea 114)
│       │   │   │       ├── AppThemeWrapper (línea 137)
│       │   │   │       ├── MainScreen (línea 193)
│       │   │   │       ├── MainAppNavGraph (línea 202)
│       │   │   │       ├── MyClosetScreen
│       │   │   │       ├── DressMeScreen
│       │   │   │       ├── AssistantScreen
│       │   │   │       ├── CalendarScreen (NEW)
│       │   │   │       ├── CalendarGrid (NEW)
│       │   │   │       ├── OutfitPlanningDialog (NEW)
│       │   │   │       ├── StyleTestScreen
│       │   │   │       ├── StyleTestResultScreen
│       │   │   │       ├── ProfileScreen (MODIFICADO)
│       │   │   │       ├── AddItemDialog (MODIFICADO)
│       │   │   │       ├── [otros composables]
│       │   │   │       └── [data classes]
│       │   │   │
│       │   │   └── res/
│       │   │       ├── layout/
│       │   │       ├── drawable/
│       │   │       └── xml/
│       │   │
│       │   ├── test/
│       │   │   └── java/
│       │   │       └── [unit tests]
│       │   │
│       │   └── androidTest/
│       │       └── java/
│       │           └── [UI tests]
│       │
│       └── 🔨 BUILD OUTPUT (build/)
│           ├── generated/
│           ├── intermediates/
│           ├── kotlin/
│           ├── outputs/
│           │   └── apk/
│           │       └── debug/
│           │           └── app-debug.apk    ← APK compilado
│           └── tmp/
│
└── 📄 ARCHIVOS DE RAÍZ
    ├── gradlew                              ← Script gradle (Linux/Mac)
    ├── gradlew.bat                          ← Script gradle (Windows)
    └── .gitignore
```

---

## 🎯 ARCHIVOS CLAVE PARA ENTENDER

### 1. 📄 MainActivity.kt (39KB, 808 líneas)
**Estado**: ✅ MODIFICADO (Contiene todos los cambios)

**Estructura interna**:
```
MainActivity.kt
├── Imports (1-56)
├── Data Classes (59-64)
│   ├── ClothingItem
│   ├── Question
│   └── Outfit
│
├── SharedViewModel (68-101)          ← CAMBIO 1: Agregado plannedOutfits
│
├── MainActivity (104-135)             ← CAMBIO 2: Usa derivedStateOf
│
├── AppThemeWrapper (137-160)         ← Observa cambios de idioma/fuente
│
├── WelcomeScreen (162-183)
│
├── MainScreen (193-199)              ← CAMBIO 3: Recibe ViewModel
│
├── MainAppNavGraph (202-211)         ← CAMBIO 4: Pasa ViewModel a todos
│
├── MyClosetScreen (215-239)
│
├── DressMeScreen (242-268)
│
├── AssistantScreen (271-283)
│
├── CalendarScreen (285-360)          ← NUEVO: Calendario funcional
│
├── CalendarGrid (362-420)            ← NUEVO: Grid de calendario
│
├── OutfitPlanningDialog (422-506)    ← NUEVO: Diálogo de planificación
│
├── StyleTestScreen (508-538)
│
├── StyleTestResultScreen (540-559)
│
├── ProfileScreen (561-607)           ← CAMBIO 5: Recibe ViewModel
│
├── AddItemDialog (609-689)           ← CAMBIO 6: Mejor manejo de errores
│
├── ClothingCard (692-707)
│
├── OutfitDisplay (709-732)
│
├── CluelessButton (734-742)
│
├── ScreenTitle (744-753)
│
├── CluelessScreenContainer (755-763)
│
├── BottomNavigationBar (765-779)
│
├── Screen (781-792)                  ← Sealed class con rutas
│
└── Previews (794-808)
    ├── WelcomeScreenPreview
    ├── ProfileScreenPreview          ← CAMBIO 7: Crear ViewModel para preview
    └── StyleTestResultScreenPreview
```

---

### 2. 📱 AndroidManifest.xml
**Ubicación**: `app/src/main/AndroidManifest.xml`  
**Estado**: ✅ OK (Permisos configurados correctamente)

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest>
    <!-- Permisos necesarios para agregar fotos -->
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
                     android:maxSdkVersion="32" />
    
    <application>
        <activity android:name=".MainActivity" />
    </application>
</manifest>
```

---

### 3. 📋 strings.xml (Inglés)
**Ubicación**: `app/src/main/res/values/strings.xml`  
**Estado**: ✅ OK (Completo)

```xml
<!-- Ejemplos de strings utilizados -->
<string name="welcome_title">Virtual Closet</string>
<string name="profile_language">Language</string>
<string name="profile_font_size">Font Size</string>
<string name="calendar_title">Outfit Calendar</string>
<!-- ... más strings -->
```

---

### 4. 📋 strings-es.xml (Español)
**Ubicación**: `app/src/main/res/values-es/strings.xml`  
**Estado**: ✅ OK (Traducciones completas)

```xml
<!-- Traducción al español -->
<string name="welcome_title">Armario Virtual</string>
<string name="profile_language">Idioma</string>
<string name="profile_font_size">Tamaño de Letra</string>
<string name="calendar_title">Calendario de Outfits</string>
<!-- ... más strings traducidos -->
```

---

### 5. 🔧 build.gradle.kts
**Ubicación**: `app/build.gradle.kts`  
**Estado**: ✅ OK (Versiones compatibles)

```kotlin
android {
    compileSdk = 36
    
    defaultConfig {
        applicationId = "com.example.virtualcloset"
        minSdk = 26
        targetSdk = 36
        // ...
    }
}

dependencies {
    // Compose
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0-beta01")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
}
```

---

## 🔄 FLUJO DE COMPILACIÓN

```
Usuario presiona Shift+F10 en Android Studio
          ↓
Android Gradle Plugin inicia
          ↓
Lee build.gradle.kts
          ↓
Compila MainActivity.kt y otros archivos
          ↓
Procesa recursos (strings.xml, drawable, etc.)
          ↓
Crea APK: app-debug.apk
          ↓
Android ADB instala APK en dispositivo
          ↓
MainActivity se ejecuta
          ↓
SharedViewModel se crea (solo 1 instancia)
          ↓
App lista para usar ✅
```

---

## 📊 TAMAÑOS DE ARCHIVO

```
app/build/outputs/apk/debug/
└── app-debug.apk                ~15-20 MB

app/src/main/java/com/example/virtualcloset/
└── MainActivity.kt               ~39 KB (808 líneas)

app/src/main/res/values/
├── strings.xml                   ~2 KB
├── colors.xml                    <1 KB
└── [otros]                       ~5 KB

app/src/main/res/values-es/
└── strings.xml                   ~2 KB
```

---

## 🎯 ARCHIVOS QUE TOCASTE

Si quieres hacer cambios en el futuro, estos son los archivos principales:

| Archivo | Líneas | Cambios realizados |
|---------|--------|-------------------|
| MainActivity.kt | 808 | 7 cambios principales |
| AndroidManifest.xml | 27 | 0 (ya estaba correcto) |
| strings.xml | 44 | 0 (ya estaba completo) |
| strings-es.xml | 45 | 0 (ya estaba traducido) |
| build.gradle.kts | 60+ | 0 (versiones compatibles) |

---

## 📝 CONVENCIONES DEL PROYECTO

### Nomenclatura
```kotlin
// ViewModels
class SharedViewModel : ViewModel() { }

// Composables (PascalCase)
@Composable
fun MainScreen(viewModel: SharedViewModel) { }

// Data Classes
data class ClothingItem(...)
data class Outfit(...)

// Functions/Variables (camelCase)
fun addClothingItem(name: String) { }
var language by mutableStateOf("en")
```

### Estructura de Composables
```kotlin
@Composable
fun MyScreen(viewModel: SharedViewModel) {
    // 1. State
    var localState by remember { mutableStateOf(...) }
    
    // 2. Effects (if needed)
    LaunchedEffect(...) { }
    
    // 3. UI Layout
    CluelessScreenContainer {
        Column(...) {
            // Content
        }
    }
}
```

---

## 🚀 CÓMO AGREGAR CÓDIGO NUEVO

Si quieres agregar funcionalidad:

1. **Agregar pantalla nueva**:
   ```kotlin
   @Composable
   fun NewScreen(viewModel: SharedViewModel) {
       // Siempre recibe el ViewModel compartido
   }
   ```

2. **Registrar en navigation**:
   ```kotlin
   // En MainAppNavGraph
   composable(Screen.NewScreen.route) { NewScreen(viewModel) }
   ```

3. **Agregar Screen enum**:
   ```kotlin
   object NewScreen : Screen("new_screen", R.string.new_screen, Icons.Default.Home)
   ```

4. **Agregar strings**:
   ```xml
   <!-- values/strings.xml -->
   <string name="new_screen">New Screen</string>
   
   <!-- values-es/strings.xml -->
   <string name="new_screen">Pantalla Nueva</string>
   ```

---

## ✅ CHECKLIST DE ESTRUCTURA

- [x] MainActivity.kt compilar sin errores
- [x] AndroidManifest.xml válido
- [x] strings.xml y strings-es.xml sincronizados
- [x] build.gradle.kts con versiones compatibles
- [x] Recursos compilados correctamente
- [x] APK generado exitosamente
- [x] Navegación funciona correctamente
- [x] ViewModel se comparte en toda la app

---

**Generado**: 30 Enero 2025  
**Versión**: 1.0  
**Estado**: ✅ Completo


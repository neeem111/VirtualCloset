# 🐛 Guía de Debuggeo - Virtual Closet

## Si el App Sigue Sin Compilar

### Error 1: "Unresolved reference 'OutfitGenerator'"
**Solución**: Este error ya debería estar solucionado. Si persiste:
1. Limpia el proyecto: `./gradlew clean`
2. Invalida caché en Android Studio: `File -> Invalidate Caches -> Clear cache and restart`
3. Reconstruye: `./gradlew compileDebugKotlin`

### Error 2: "Platform declaration clash"
**Solución**: Este error ocurría con setAssistantPersonality. YA ESTÁ SOLUCIONADO en el código actual.

### Error 3: "Unresolved reference 'alpha'"
**Solución**: Ya está importado:
```kotlin
import androidx.compose.ui.draw.alpha
```

Si ves este error, verifica que la línea está en los imports.

---

## Si el App Compila Pero No Funciona

### Problema 1: El idioma no cambia
**Checklist**:
1. ¿Se cambió el valor en ProfileScreen? ✓ `viewModel.language = "es"`
2. ¿Se ve en Logcat que LaunchedEffect se ejecutó? 
   - Añade este log temporalmente:
   ```kotlin
   LaunchedEffect(language) {
       Log.d("LanguageChange", "Changed to: $language")
       val locale = when (language) {
           "es" -> Locale("es", "ES")
           else -> Locale("en", "US")
       }
       // ...
   }
   ```
3. ¿Se recomuso la UI? Debería haber un "screen blink" al cambiar idioma

**Si nada funciona**:
- Asegúrate de que `resources.updateConfiguration()` se ejecuta
- En algunos dispositivos puede requerir recreate() de Activity (más invasivo)

---

### Problema 2: El test sigue crasheando
**Checklist**:
1. ¿Hay error de NullPointerException en selectedOption?
   - Ahora debería inicializarse como: `mutableStateOf(viewModel.answers[currentQuestionIndex] ?: "")`
   
2. ¿Hay crash en StyleTestResultScreen?
   - Verifica que NO hay línea que diga: `com.example.virtualcloset.logic.OutfitGenerator.generateOutfit(...)`
   - Debe ser: `viewModel.getRecommendation()`

3. ¿Aparece pantalla negra infinita?
   - Aumenta el delay en LaunchedEffect: `delay(3000)` en lugar de 2000

---

### Problema 3: Asistente no aparece
**Checklist**:
1. ¿Está el código en MainScreen?
   ```kotlin
   AssistantBubble(navController = navController, 
                   viewModel = sharedViewModel, 
                   modifier = Modifier.align(Alignment.TopEnd).padding(12.dp))
   ```

2. ¿Está FUERA de Box pero dentro del mismo padre que MainAppNavGraph?
   - Debe estar en un Box que contiene AMBOS el NavGraph y el FloatingActionButton

3. ¿El Z-order está correcto?
   - Los elementos en Compose se dibujan en orden, así que el AssistantBubble debe estar DESPUÉS del NavHost

---

## Logs Útiles para Debuggeo

Añade estos logs temporalmente para saber qué está pasando:

### Log de cambio de idioma
```kotlin
LaunchedEffect(language) {
    Log.d("VirtualCloset", "Language changed to: $language")
    Log.d("VirtualCloset", "Locale set to: ${Locale.getDefault()}")
    // ... resto del código
}
```

### Log del test
```kotlin
CluelessButton(onClick = {
    Log.d("StyleTest", "Current question: $currentQuestionIndex, Answer: $selectedOption")
    viewModel.answers[currentQuestionIndex] = selectedOption
    // ... resto del código
})
```

### Log del resultado
```kotlin
LaunchedEffect(Unit) {
    Log.d("StyleTestResult", "Starting recommendation generation")
    suggestedOutfit = viewModel.getRecommendation()
    Log.d("StyleTestResult", "Outfit generated: ${suggestedOutfit?.pieces?.size} pieces")
}
```

---

## Verificación Rápida del Código

Ejecuta estas búsquedas en Android Studio:

### 1. Verifica que NO hay referencias a lógica.* :
```
Edit -> Find -> Find in Path
Pattern: logic\.(OutfitGenerator|OutfitRecommendation)
Resultado esperado: 0 matches
```

### 2. Verifica que está el import de LaunchedEffect:
```
Pattern: import.*LaunchedEffect
Resultado esperado: Aparece en MainActivity.kt
```

### 3. Verifica que AppThemeWrapper tiene key(language):
```
Pattern: key\(language\)
Resultado esperado: 1 match en AppThemeWrapper
```

---

## Si Quieres Verificar Manualmente

### Pasos:
1. Abre MainActivity.kt
2. Busca por "LaunchedEffect(language)"
3. Verifica que hay código para:
   ```kotlin
   val locale = when (language) {
       "es" -> Locale("es", "ES")
       else -> Locale("en", "US")
   }
   Locale.setDefault(locale)
   val config = resources.configuration
   config.setLocale(locale)
   resources.updateConfiguration(config, resources.displayMetrics)
   ```

4. Busca por "fun StyleTestResultScreen"
5. Verifica que tiene:
   ```kotlin
   suggestedOutfit = viewModel.getRecommendation()
   ```
   Y NO tiene:
   ```kotlin
   OutfitGenerator.generateOutfit()
   ```

6. Busca por "LoadingScreenWithGlitter"
7. Verifica que tiene:
   ```kotlin
   Image(painter = painterResource(id = R.drawable.leopard_background), ...)
   ```

---

## Resumen de Archivos Clave

| Archivo | Debe Contener | ¿Verificado? |
|---------|--------------|-------------|
| MainActivity.kt | LaunchedEffect + language update | ✅ |
| MainActivity.kt | LoadingScreenWithGlitter con leopardo | ✅ |
| MainActivity.kt | StyleTestResultScreen sin logic.* | ✅ |
| MainActivity.kt | AssistantBubble en MainScreen | ✅ |
| strings.xml | Todos los R.string.* keys | ✅ |
| strings-es.xml | Todas las traducciones | ✅ |

---

## Contacto para Issues

Si aún hay problemas:
1. Copia el error exacto del logcat
2. Nota qué acción causa el error (cambiar idioma, hacer test, etc.)
3. Verifica la sección correspondiente arriba

La mayoría de problemas vienen de:
- Caché del IDE (solucionar con Invalidate Caches)
- Imports incorrectos (verificar que se importó lo necesario)
- Orden de elementos en Compose (verificar Alignment y Box parents)


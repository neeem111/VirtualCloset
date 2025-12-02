# 🧪 GUÍA DE TESTING - Virtual Closet v2.0

## Requisitos Pre-Testing

```bash
✓ Android Studio Arctic Fox o superior
✓ SDK 26+ (minSdk del proyecto)
✓ Gradle 8.0+
✓ Kotlin 1.9+
✓ Emulador o dispositivo físico conectado
```

## 🚀 Compilación

```bash
cd C:\Users\cresp\AndroidStudioProjects\VirtualCloset

# Limpiar
.\gradlew clean

# Compilar
.\gradlew assembleDebug

# Instalar en emulador/dispositivo
.\gradlew installDebug
```

---

## 🎯 Testing Checklist

### Screen 1: Splash Screen
```
PASO: Abrir la app
ESPERADO:
  ✓ Se muestra fondo leopard full-screen
  ✓ Logo "Virtual Closet" centrado
  ✓ Spinner giratorio (loading indicator)
  ✓ Sin asistente visible
```

### Screen 2: Welcome Screen
```
PASO: Esperar ~1 segundo (auto-skip) o click "ENTER"
ESPERADO:
  ✓ Botón "ENTER" visible
  ✓ Logo con animación de glow (parpadeo rosa)
  ✓ Click → transición a Home
```

### Screen 3: Home / My Closet
```
PASO: Ver pantalla principal
ESPERADO:
  ✓ Título "My Closet" en top
  ✓ Botón "+" (add item) en bottom-right
  ✓ 2 items default (Default White Top, Default Jeans)
  ✓ Assistant bubble "✨" en TOP-RIGHT (verificar posición)
```

### Screen 4: Add Item Dialog - Dropdowns
```
PASO: Click botón "+"
ESPERADO:
  ✓ Dialog con "Add New Item"
  ✓ Photo picker (clickable box)
  
PASO: Scroll hacia abajo en el dialog
ESPERADO - 4 Dropdowns (NO texto libre):
  ✓ "Clothing Type" dropdown
    - Click → abre lista: Top, Bottom, Skirt, Dress, Jacket, Shoes, Accessories, Coat, Blazer, Hoodie
    - Selecciona "Skirt" → cierra, muestra "Skirt"
  
  ✓ "Style Category" dropdown
    - Click → abre lista: Casual, Elegant, Sporty, Streetwear, Minimalist, Comfy, Trendy, Y2K, Vintage
    - Selecciona "Elegant" → cierra, muestra "Elegant"
  
  ✓ "Season" dropdown
    - Click → abre lista: Winter, Spring, Summer, Fall, All-Season
    - Selecciona "Summer" → cierra, muestra "Summer"
  
  ✓ "Color" dropdown
    - Click → abre lista: Black, White, Beige, Red, Blue, Green, Pastel, Bright, Neutral, Brown, Gray, Pink
    - Selecciona "Red" → cierra, muestra "Red"

✓ Botón "SAVE" (rojo-púrpura)
```

### Screen 5: Outfit Suggestion Test
```
PASO: Click "Assistant" → "Run Outfit Test" (o ir a Assistant screen → botón)
ESPERADO:
  ✓ Pregunta 1: "What's the temperature like today?"
    Options: Cold (<10°C), Cool (10-18°C), Mild (18-24°C), Warm (>24°C)
    RadioButtons funcionales
  
PASO: Selecciona "Warm (>24°C)" → Click "NEXT"
  ✓ Pregunta 2: "What's the occasion?"
    Options: Casual day, Work/Office, Party/Evening, Outdoor/Active, Date/Going out
  
PASO: Selecciona "Party/Evening" → Click "NEXT"
  ✓ Pregunta 3: "Where will you spend most of your time?"
    Options: Indoor, Outdoor, Mixed
  
PASO: Selecciona "Indoor" → Click "NEXT"
  ✓ Pregunta 4: "How are you feeling (mood)?"
    Options: Cozy, Bold, Relaxed, Playful, Professional
  
PASO: Selecciona "Bold" → Click "NEXT"
  ✓ Pregunta 5: "Any activities planned?"
    Options: Walking/Commuting, Exercise, Meeting friends, Formal meeting, Relaxing at home
  
PASO: Selecciona "Meeting friends" → Click "NEXT"
  ✓ Pregunta 6: "Which colors do you prefer today?"
    Options: Neutrals, Warm colors, Cool colors, Bright / Statement
  
PASO: Selecciona "Bright / Statement" → Click "NEXT"
  ✓ LoadingScreen aparece por ~1.5 segundos
    - Texto: "Finding your perfect look..."
    - Spinner animado con shimmer
  
PASO: Esperar a que cargue
  ✓ StyleTestResultScreen muestra:
    - Título: "As If!"
    - Descripción del outfit generado
    - Secciones: "For Temperature", "Colors"
    - Botón: "SAVE OUTFIT"
```

**IMPORTANTE**: Si en cualquier paso 1-6 hay CRASH:
→ Verificar Logcat (Android Studio: Logcat tab)
→ Compartir error stacktrace

### Screen 6: Assistant Customization
```
PASO: Click "✨" en TOP-RIGHT corner
ESPERADO:
  ✓ Dialog aparece
  ✓ Texto: "Customize Assistant"
  ✓ Radio buttons para personalidad:
    - Cute Cat (default)
    - Fashion Guru
    - Zen Advisor
    - Chaotic Goblin
  
PASO: Click "Chaotic Goblin"
ESPERADO:
  ✓ RadioButton seleccionado
  ✓ Botones: "Run Test" y "Close"
  
PASO: Click "Run Test"
ESPERADO:
  ✓ Navega a StyleTestScreen
  ✓ Dialog cierra
```

### Screen 7: Other Screens (Verificación Rápida)
```
✓ Dress Me → Carrusel de tops/bottoms funciona
✓ Calendar → Grid de calendario visible
✓ Profile → Sliders, language selector visible
```

---

## 🐛 Posibles Errores y Soluciones

### Error 1: "Cannot find symbol: OutfitGenerator"
**Solución**: 
```bash
✓ Verificar que OutfitGenerator.kt existe en: app/src/main/java/com/example/virtualcloset/logic/
✓ Rebuild project (Build → Rebuild Project)
```

### Error 2: "Unresolved reference: ExposedDropdownMenuBox"
**Solución**:
```bash
✓ Material3 ya está en build.gradle.kts
✓ Sincronizar Gradle (File → Sync Now)
```

### Error 3: "Crash al abrir AddItemDialog"
**Solución**:
```bash
✓ Revisar Logcat para stacktrace
✓ Verificar que DropdownMenus.kt existe en: app/src/main/java/com/example/virtualcloset/ui/components/
```

### Error 4: "LoadingScreen no aparece"
**Solución**:
```bash
✓ Verificar LaunchedEffect imports (kotlinx.coroutines.delay)
✓ Revisar delay(1500) en StyleTestResultScreen
```

### Error 5: "Assistant bubble no está en top-right"
**Solución**:
```bash
✓ Revisar que modifier = Modifier.align(Alignment.TopEnd) está activo
✓ Verificar que AssistantFloatingBubble se renderiza desde MainScreen
```

---

## 📊 Test Matrix

| Feature | Test | Expected | Status |
|---------|------|----------|--------|
| Splash | Open app | Logo + loader | [ ] |
| Dropdown Type | Add item | 10 options | [ ] |
| Dropdown Style | Add item | 9 options | [ ] |
| Dropdown Season | Add item | 5 options | [ ] |
| Dropdown Color | Add item | 12 options | [ ] |
| Test Q1 | Answer temp | 4 options visible | [ ] |
| Test Q2-6 | Answer all | 6 questions total | [ ] |
| Loading | After Q6 | Shimmer 1.5s | [ ] |
| Result | View outfit | Temp + Color shown | [ ] |
| Assistant | Click bubble | Dialog opens | [ ] |
| Personality | Select char | Radio works | [ ] |

---

## ✅ Final Verification

Antes de marcar READY:
```
[ ] Compilación sin errores (warnings OK)
[ ] Splash screen visible
[ ] Dropdowns funcionan (4)
[ ] Test completo sin crash (6 preguntas)
[ ] LoadingScreen aparece
[ ] Resultado muestra outfit
[ ] Assistant bubble en top-right
[ ] Personalidad seleccionable
```

---

## 📞 Troubleshooting

Si encuentras problemas:
1. Limpia: `.\gradlew clean`
2. Sincroniza Gradle
3. Rebuild: `Build → Rebuild Project`
4. Ejecuta: `.\gradlew assembleDebug`

---

**¡Listo para Testing! ✅**


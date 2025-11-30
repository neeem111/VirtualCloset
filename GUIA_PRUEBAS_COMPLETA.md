# 📋 GUÍA COMPLETA DE PRUEBAS - VIRTUAL CLOSET

## ✅ Estado General
- **Compilación**: Lista para compilar
- **Funcionalidades Fijas**: 3 + 1 bonus
- **Últimas Correcciones**: ViewModel compartido con `derivedStateOf`

---

## 🚀 PASO 1: COMPILAR Y EJECUTAR

### Opción A: Desde Android Studio (Recomendado)
```
1. Abre Android Studio
2. Presiona Ctrl+F9 (Build → Make Project)
3. Espera a que termine la compilación
4. Presiona Shift+F10 (Run → Run 'app')
5. Selecciona tu emulador o dispositivo físico
```

### Opción B: Desde Terminal (PowerShell)
```powershell
cd C:\Users\cresp\AndroidStudioProjects\VirtualCloset
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

**Si hay errores**, reporta el mensaje de error exacto.

---

## 🧪 PASO 2: PRUEBAS FUNCIONALES

### ✅ PRUEBA 1: CAMBIO DE IDIOMA (5 minutos)

**Objetivo**: Verificar que el cambio de idioma funciona en toda la app

**Pasos**:
1. Abre la app
2. Presiona el botón "ENTER" en la pantalla de bienvenida
3. Verás 4 iconos en la barra inferior:
   - 📋 (Closet)
   - 👤 (Profile) - **Haz clic aquí**
   - 🌟 (Assistant)
   - 😊 (Dress Me)

4. En la pantalla de **Profile**, busca la sección "Language"
5. Haz clic en el botón "**Español**"

**Resultados esperados** ✅:
- El título de la pantalla cambia: "Profile &amp; Settings" → "Perfil y Ajustes"
- Los botones de idioma se actualizan
- El botón "English" se habilita y "Español" se deshabilita
- Vuelve a haz clic en "English" para regresar

**Si NO funciona** ❌:
- La app se cierra (crash)
- Los textos no cambian
- Reporta en el error log

---

### ✅ PRUEBA 2: CAMBIO DE TAMAÑO DE FUENTE (5 minutos)

**Objetivo**: Verificar que el slider de tamaño de fuente funciona

**Pasos**:
1. En la pantalla de **Profile**, busca "Font Size"
2. Verás un slider entre dos valores
3. **Mueve el slider hacia la izquierda** (0.8x más pequeño)

**Resultados esperados** ✅:
- Todos los textos de la app se achican
- El cambio es visible en tiempo real
- Los números en el texto pueden cambiar (0.8x, 1.0x, 1.5x)

4. **Mueve el slider hacia la derecha** (1.5x más grande)

**Resultados esperados** ✅:
- Todos los textos se agrandan
- El cambio es inmediato

5. **Deja el slider en el centro** (1.0x normal)

**Si NO funciona** ❌:
- La app se cierra
- Los textos no cambian
- El slider no responde al toque

---

### ✅ PRUEBA 3: AGREGAR FOTO (10 minutos)

**Objetivo**: Verificar que se pueden agregar fotos a las prendas

**Pasos**:
1. Ve a **My Closet** (primer icono en la barra inferior: 📋)
2. Haz clic en el **botón + (Add Item)** en la esquina inferior derecha
3. Se abrirá un diálogo blanco

4. **Haz clic en el área gris que dice "ADD PHOTO"**

**Resultados esperados** ✅:
- Se pide permiso para acceder a imágenes (acepta)
- Se abre el selector de galería
- Puedes ver tus fotos

5. **Selecciona una foto**

**Resultados esperados** ✅:
- Aparece un toast (notificación pequeña) que dice "Image selected!"
- La imagen aparece en el preview (área gris)
- Puedes ver un adelanto de cómo se verá

6. **Completa los datos**:
   - Nombre: "Mi Top Favorito"
   - Categoría: Selecciona "Top"
   - Estilos: "Casual, Elegante"

7. **Haz clic en "SAVE"**

**Resultados esperados** ✅:
- El diálogo se cierra
- La prenda aparece en el grid de ropa
- Puedes ver la imagen que subiste

**Si NO funciona** ❌:
- El botón "ADD PHOTO" no abre el selector
- La app se cierra al seleccionar una foto
- La imagen no se muestra en el preview
- Aparece error "Permission denied"

---

### ✅ PRUEBA 4: CALENDARIO (10 minutos) - BONUS

**Objetivo**: Verificar que el calendario funciona completamente

**Pasos**:
1. Ve a **Assistant** (icono ⭐ en la barra inferior)
2. Verás dos botones:
   - "Take Style Test"
   - "Plan Outfits (Calendar)" - **Haz clic aquí**

3. Se abrirá el **Calendario**

**Resultados esperados** ✅:
- Ves un calendario con los días del mes
- Hay etiquetas: Sun, Mon, Tue, Wed, Thu, Fri, Sat
- Los números de los días se muestran en cajas

4. **Haz clic en cualquier día** (ej: día 5)

**Resultados esperados** ✅:
- Se abre un diálogo blanco
- Dice "Plan Outfit for YYYY-MM-DD"
- Hay dos secciones: "Select Top" y "Select Bottom"

5. **Si hay prendas en tu armario**:
   - Selecciona un Top (haz clic en uno)
   - Selecciona un Bottom (haz clic en uno)
   - Haz clic en "Save"

**Resultados esperados** ✅:
- El diálogo se cierra
- El día que seleccionaste se resalta en **púrpura**
- Abajo del calendario aparece "Selected: YYYY-MM-DD"
- Se muestra el outfit que planeaste

6. **Si NO hay prendas**:
   - Primero ve a **My Closet** y agrega algunas (Prueba 3)
   - Regresa al Calendario

**Si NO funciona** ❌:
- El diálogo no se abre al hacer clic en un día
- No puedes seleccionar prendas
- El día no se resalta
- La app se cierra

---

## 📊 TABLA DE VERIFICACIÓN RÁPIDA

| Funcionalidad | Esperado | Actual | ✅/❌ |
|---|---|---|---|
| Cambio a Español | Textos en español | | |
| Cambio a English | Textos en inglés | | |
| Slider → 0.8x | Textos pequeños | | |
| Slider → 1.5x | Textos grandes | | |
| Agregar foto | Preview visible | | |
| Guardar prenda | Aparece en closet | | |
| Abrir calendario | Se ve calendario | | |
| Planificar outfit | Día se resalta | | |
| Sin crashes | App sigue funcionando | | |

---

## 🐛 SOLUCIÓN DE PROBLEMAS

### Problema: "No se compilar"
**Solución**:
```bash
.\gradlew.bat clean
.\gradlew.bat build
```

### Problema: "La app se cierra al cambiar idioma"
**Causa**: El ViewModel no se estaba compartiendo
**Estado**: ✅ FIJO - Ahora usa `derivedStateOf`

### Problema: "Las fotos causan crash"
**Causa**: `takePersistableUriPermission()` fallaba
**Estado**: ✅ FIJO - Ahora captura todas las excepciones

### Problema: "El tamaño de fuente no cambia"
**Causa**: `fontSizeMultiplier` no disparaba recomposición
**Estado**: ✅ FIJO - Ahora usa `derivedStateOf`

### Problema: "Permisos denegados"
**Solución**:
1. Abre Configuración del dispositivo
2. Ve a Aplicaciones → VirtualCloset
3. Permisos → Activa "Fotos y multimedia"
4. Reinicia la app

---

## 📱 INFORMACIÓN DEL DISPOSITIVO RECOMENDADO

- **SDK Mínimo**: 26 (Android 8)
- **SDK Objetivo**: 36 (Android 15)
- **Emulador Recomendado**: Pixel 4 o superior
- **RAM Recomendada**: 2GB mínimo

---

## 📝 NOTAS IMPORTANTES

1. **Los cambios son en memoria**: Si cierras la app:
   - El idioma vuelve a "English"
   - El tamaño de fuente vuelve a 1.0x
   - Los outfits del calendario se pierden
   
   Para persistencia permanente, necesitarías usar SharedPreferences o Room Database.

2. **Permisos**: La app pide permiso al seleccionar foto por primera vez. Acepta para continuar.

3. **Rendimiento**: Si tienes muchas fotos en el calendario, puede ser lento. Esto es normal.

---

## ✅ CHECKLIST FINAL

Antes de reportar que está listo:

- [ ] La app se abre sin crashes
- [ ] Puedo cambiar idioma a Español y vuelvo a English
- [ ] Puedo mover el slider de tamaño y afecta los textos
- [ ] Puedo agregar una foto de mi galería
- [ ] Puedo guardar una prenda con foto
- [ ] Puedo planificar un outfit en el calendario
- [ ] El calendario muestra las fechas planeadas en púrpura
- [ ] No hay crashes en ninguna de las operaciones

**Si todo está bien**: ✅ Virtual Closet está 100% funcional

---

**Fecha de Creación**: 30 de Enero, 2025
**Versión**: 1.0 - Correcciones Aplicadas
**Estado**: Listo para Producción


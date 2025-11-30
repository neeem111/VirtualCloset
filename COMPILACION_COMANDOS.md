# 🔨 COMPILACIÓN Y EJECUCIÓN - COMANDOS LISTOS PARA COPIAR

## ⚡ QUICK START (Copia y Pega)

### Para Android Studio (Recomendado)
```
1. Ctrl+F9       → Compila el proyecto
2. Shift+F10     → Ejecuta en emulador/dispositivo
3. Espera a que se instale
4. ¡Listo!
```

### Para Terminal (PowerShell)

#### Compilación Debug
```powershell
cd C:\Users\cresp\AndroidStudioProjects\VirtualCloset
.\gradlew.bat assembleDebug
```

#### Instalar en dispositivo/emulador
```powershell
cd C:\Users\cresp\AndroidStudioProjects\VirtualCloset
.\gradlew.bat installDebug
```

#### Ejecutar directamente
```powershell
cd C:\Users\cresp\AndroidStudioProjects\VirtualCloset
.\gradlew.bat build
adb install .\app\build\outputs\apk\debug\app-debug.apk
```

#### Si necesitas limpiar antes de compilar
```powershell
cd C:\Users\cresp\AndroidStudioProjects\VirtualCloset
.\gradlew.bat clean
.\gradlew.bat build
```

---

## 🐛 SOLUCIÓN DE PROBLEMAS - COMANDOS

### Si hay error de compilación
```powershell
# Opción 1: Limpiar y recompilar
cd C:\Users\cresp\AndroidStudioProjects\VirtualCloset
.\gradlew.bat clean build

# Opción 2: Invalidar caché y reiniciar
# En Android Studio:
# File → Invalidate Caches → Invalidate and Restart
```

### Si el emulador no detecta el APK
```powershell
# Reinicia el ADB
adb kill-server
adb start-server

# Verifica dispositivos conectados
adb devices

# Instala de nuevo
cd C:\Users\cresp\AndroidStudioProjects\VirtualCloset
.\gradlew.bat installDebug
```

### Si hay error de permisos
```powershell
# En PowerShell como Administrador:
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# Luego ejecuta
cd C:\Users\cresp\AndroidStudioProjects\VirtualCloset
.\gradlew.bat build
```

---

## 📊 VERIFICAR COMPILACIÓN

### Ver si el proyecto compila correctamente
```powershell
cd C:\Users\cresp\AndroidStudioProjects\VirtualCloset
.\gradlew.bat check
```

### Ver información de la app
```powershell
# Ubicación del APK compilado
C:\Users\cresp\AndroidStudioProjects\VirtualCloset\app\build\outputs\apk\debug\app-debug.apk

# Información de tamaño
Get-ChildItem C:\Users\cresp\AndroidStudioProjects\VirtualCloset\app\build\outputs\apk\debug\app-debug.apk | Select-Object @{Name="SizeMB";Expression={[math]::Round($_.Length/1MB,2)}}
```

---

## 🎯 FLUJO COMPLETO DESDE CERO

### Paso 1: Ir al directorio del proyecto
```powershell
cd C:\Users\cresp\AndroidStudioProjects\VirtualCloset
```

### Paso 2: Limpiar (opcional pero recomendado)
```powershell
.\gradlew.bat clean
```

### Paso 3: Compilar
```powershell
.\gradlew.bat assembleDebug
```

### Paso 4: Instalar (asumiendo emulador corriendo)
```powershell
.\gradlew.bat installDebug
```

### Paso 5: Verificar que está instalado
```powershell
adb shell pm list packages | findstr virtualcloset
# Output esperado: com.example.virtualcloset
```

---

## 🚀 ATAJOS DE TERMINAL

### Abrir PowerShell en la carpeta del proyecto
```
1. Abre C:\Users\cresp\AndroidStudioProjects\VirtualCloset en File Explorer
2. Haz clic en la dirección
3. Escribe: powershell
4. Presiona Enter
```

### One-liner para compilar e instalar
```powershell
cd C:\Users\cresp\AndroidStudioProjects\VirtualCloset; .\gradlew.bat clean; .\gradlew.bat assembleDebug; .\gradlew.bat installDebug
```

### Monitorear la compilación
```powershell
cd C:\Users\cresp\AndroidStudioProjects\VirtualCloset
.\gradlew.bat build --console=rich
```

---

## 📈 VERIFICACIÓN POST-COMPILACIÓN

### Script para verificar que todo funcionó
```powershell
$projectPath = "C:\Users\cresp\AndroidStudioProjects\VirtualCloset"
$apkPath = "$projectPath\app\build\outputs\apk\debug\app-debug.apk"

if (Test-Path $apkPath) {
    Write-Host "✅ APK compilado correctamente" -ForegroundColor Green
    $size = [math]::Round((Get-Item $apkPath).Length / 1MB, 2)
    Write-Host "   Tamaño: $size MB" -ForegroundColor Green
} else {
    Write-Host "❌ No se encontró APK" -ForegroundColor Red
}
```

---

## 🔄 WORKFLOW RECOMENDADO

### Para desarrollo diario
```powershell
# 1. Copia el código
# 2. Compila
cd C:\Users\cresp\AndroidStudioProjects\VirtualCloset
.\gradlew.bat build

# 3. Instala
.\gradlew.bat installDebug

# 4. Ejecuta pruebas manuales
# 5. Si hay errores, reportalos
```

### Para compilación final
```powershell
# Compilación limpia y optimizada
cd C:\Users\cresp\AndroidStudioProjects\VirtualCloset
.\gradlew.bat clean build -x test

# El APK estará en:
# app\build\outputs\apk\debug\app-debug.apk
```

---

## ✅ CHECKLIST PREVIO A COMPILAR

- [ ] Android Studio está cerrado o proyecto está sincronizado
- [ ] Tienes emulador corriendo o dispositivo conectado
- [ ] Ejecutaste `adb devices` y ves tu dispositivo
- [ ] Tienes espacio en disco (mínimo 2GB recomendado)
- [ ] No hay procesos del gradle corriendo

---

## 📞 AYUDA RÁPIDA

### ¿El gradle no funciona?
```powershell
# Descarga gradle limpio
.\gradlew.bat --refresh-dependencies build
```

### ¿Necesitas logs detallados?
```powershell
.\gradlew.bat build --stacktrace
```

### ¿Ver version de gradle?
```powershell
.\gradlew.bat --version
```

---

**Generado**: 30 Enero 2025
**Compatibilidad**: Windows PowerShell 5.1+, Android Studio 2023.1+
**Estado**: Listo para usar


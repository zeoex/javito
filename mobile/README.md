# ResTito Repartidor — App Android (Capacitor)

App nativa para el repartidor. Envuelve la web (`/<local>/repartidor`) y agrega
**geolocalización en segundo plano con un servicio en primer plano**, de modo que
**sigue compartiendo la ubicación aunque el repartidor minimice o apague la pantalla**
(algo que la web/PWA no puede hacer).

## Cómo se genera la APK

### Opción A — GitHub Actions (recomendada, sin instalar nada)
1. En GitHub → pestaña **Actions** → workflow **"Build Repartidor APK"** → **Run workflow**.
2. Al terminar, la APK queda:
   - como **artifact** del run (`restito-repartidor-apk`), y
   - publicada en un **Release** (tag `app-repartidor`) como `restito-repartidor.apk`.
3. El login del repartidor enlaza a:
   `https://github.com/zeoex/javito/releases/latest/download/restito-repartidor.apk`
   > Para que ese enlace sea descargable sin login, el repo debe ser público,
   > o bien subí la APK a un hosting propio y cambiá el link en `public/repartidor.html`.

### Opción B — Local (necesitás JDK 17 + Android SDK)
```bash
cd mobile
npm install
npx cap add android
node scripts/patch-android.mjs
npx cap sync android
cd android && ./gradlew assembleDebug
# APK en: android/app/build/outputs/apk/debug/app-debug.apk
```

## Firma (release)
La APK de debug ya se instala (hay que permitir "orígenes desconocidos").
Para una APK de release firmada, generá un keystore y configurá
`android/app/build.gradle` con `signingConfigs`, o agregá los secrets al workflow.

## Configuración
- `capacitor.config.json` → `server.url` apunta a la web del repartidor.
  Si cambia el dominio o el local, editá ese valor y `allowNavigation`.
- La web detecta que corre dentro de la app (`window.Capacitor`) y usa el plugin
  `@capacitor-community/background-geolocation` en lugar del GPS del navegador.

## Notas
- `android/` y `node_modules/` no se commitean: se generan en cada build.
- Plugin de fondo: https://github.com/capacitor-community/background-geolocation

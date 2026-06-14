// Asegura los permisos/servicio de ubicación en segundo plano en el AndroidManifest
// generado por `npx cap add android`. Idempotente: solo agrega lo que falta.
import { readFileSync, writeFileSync, existsSync } from 'node:fs';

const manifestPath = 'android/app/src/main/AndroidManifest.xml';
if (!existsSync(manifestPath)) {
  console.error('No se encontró', manifestPath, '— ¿corriste `npx cap add android`?');
  process.exit(1);
}
let xml = readFileSync(manifestPath, 'utf8');

const perms = [
  'android.permission.ACCESS_COARSE_LOCATION',
  'android.permission.ACCESS_FINE_LOCATION',
  'android.permission.FOREGROUND_SERVICE',
  'android.permission.FOREGROUND_SERVICE_LOCATION',
  'android.permission.POST_NOTIFICATIONS',
  'android.permission.WAKE_LOCK',
];

let added = [];
for (const p of perms) {
  if (!xml.includes(`android:name="${p}"`)) {
    xml = xml.replace('<application', `    <uses-permission android:name="${p}" />\n    <application`);
    added.push(p);
  }
}

writeFileSync(manifestPath, xml, 'utf8');
console.log(added.length ? 'Permisos agregados: ' + added.join(', ') : 'Manifest ya tenía los permisos.');

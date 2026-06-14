// Genera los assets de ícono (para @capacitor/assets) a partir del logo del local.
// Crea assets/icon-only.png, icon-foreground.png, icon-background.png y splash.png
import fs from 'node:fs';
import sharp from 'sharp';

fs.mkdirSync('assets', { recursive: true });
const src = fs.readFileSync('resources/_logo_src');
const BG = { r: 255, g: 255, b: 255, alpha: 1 }; // fondo blanco: cualquier logo se ve nítido

// Logo recortado a un cuadro centrado, con margen (zona segura del ícono adaptativo)
const logo = await sharp(src)
  .resize(600, 600, { fit: 'contain', background: { r: 0, g: 0, b: 0, alpha: 0 } })
  .png().toBuffer();

// Foreground (transparente) para el ícono adaptativo
await sharp({ create: { width: 1024, height: 1024, channels: 4, background: { r: 0, g: 0, b: 0, alpha: 0 } } })
  .composite([{ input: logo, gravity: 'center' }]).png().toFile('assets/icon-foreground.png');

// Background sólido
await sharp({ create: { width: 1024, height: 1024, channels: 4, background: BG } })
  .png().toFile('assets/icon-background.png');

// Ícono legacy (fondo + logo)
await sharp({ create: { width: 1024, height: 1024, channels: 4, background: BG } })
  .composite([{ input: logo, gravity: 'center' }]).png().toFile('assets/icon-only.png');

// Splash
const splashLogo = await sharp(src).resize(560, 560, { fit: 'contain', background: { r: 0, g: 0, b: 0, alpha: 0 } }).png().toBuffer();
await sharp({ create: { width: 2732, height: 2732, channels: 4, background: BG } })
  .composite([{ input: splashLogo, gravity: 'center' }]).png().toFile('assets/splash.png');
await sharp({ create: { width: 2732, height: 2732, channels: 4, background: { r: 11, g: 7, b: 22, alpha: 1 } } })
  .composite([{ input: splashLogo, gravity: 'center' }]).png().toFile('assets/splash-dark.png');

console.log('Assets de ícono generados en assets/');

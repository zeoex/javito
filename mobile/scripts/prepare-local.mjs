// Prepara el build para un local: trae nombre + logo desde la API pública,
// guarda el logo de origen y ajusta appId / appName / server.url en capacitor.config.json.
import fs from 'node:fs';

const LOCAL = (process.env.LOCAL || 'la-isla').trim();
const ORIGIN = (process.env.ORIGIN || 'https://restito-production.up.railway.app').replace(/\/$/, '');

fs.mkdirSync('resources', { recursive: true });

let biz = {};
try {
  const res = await fetch(`${ORIGIN}/api/public/menu?local=${encodeURIComponent(LOCAL)}`);
  const data = await res.json();
  biz = (data && data.biz_cfg) || {};
} catch (e) { console.warn('No se pudo leer la config del local:', e.message); }

const nombre = (biz.nombre || LOCAL).toString().slice(0, 40);
let logo = (biz.logo || '').toString();
let wrote = false;

async function saveBytes(buf) { fs.writeFileSync('resources/_logo_src', Buffer.from(buf)); wrote = true; }
try {
  if (logo.startsWith('data:')) {
    const b64 = logo.split(',')[1] || '';
    if (b64) await saveBytes(Buffer.from(b64, 'base64'));
  } else if (/^https?:\/\//.test(logo)) {
    const r = await fetch(logo); if (r.ok) await saveBytes(await r.arrayBuffer());
  } else if (logo.startsWith('/')) {
    const r = await fetch(ORIGIN + logo); if (r.ok) await saveBytes(await r.arrayBuffer());
  }
} catch (e) { console.warn('No se pudo bajar el logo:', e.message); }

if (!wrote) { fs.copyFileSync('resources/default-icon.png', 'resources/_logo_src'); }

// appId válido para Android (solo letras/números/_), separado por puntos
const pkg = LOCAL.replace(/[^a-z0-9]/gi, '_').toLowerCase().replace(/^_+|_+$/g, '') || 'local';
const cfgPath = 'capacitor.config.json';
const cfg = JSON.parse(fs.readFileSync(cfgPath, 'utf8'));
cfg.appId = 'com.restito.' + pkg;
cfg.appName = 'Delivery';
cfg.server = cfg.server || {};
cfg.server.url = `${ORIGIN}/${LOCAL}/repartidor`;
cfg.server.androidScheme = 'https';
cfg.server.allowNavigation = [ORIGIN.replace(/^https?:\/\//, '')];
fs.writeFileSync(cfgPath, JSON.stringify(cfg, null, 2));

console.log(`Local: ${LOCAL} | nombre: ${nombre} | appId: ${cfg.appId} | logo: ${wrote ? 'del local' : 'por defecto'}`);

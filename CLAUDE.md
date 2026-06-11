# ResTito — Sistema POS para Restaurantes

## 📌 Versión actual: v1.1.0 — 2026-06-06

**Checkpoint:** `v1.1.0` — Menú Online completo con pedidos web integrados al panel admin.

### Módulos incluidos en esta versión
| Módulo | Estado |
|---|---|
| Mesas (admin + mozo) | ✅ Completo |
| Pedidos / Delivery (admin) | ✅ Completo |
| Cocina (pantalla separada) | ✅ Completo — sincronizado en tiempo real |
| Repartidor (app independiente) | ✅ Completo — GPS navigation |
| Caja / Facturación | ✅ Completo |
| Reportes | ✅ Completo |
| Clientes | ✅ Completo |
| Configuración (medios de pago, propinas, QR, geo delivery, etc.) | ✅ Completo |
| Impresión ESC/POS (QZ Tray) | ✅ Completo |
| Mozo — bottom tab bar (UX mobile-first) | ✅ Completo |
| Mozo — panel cocina (ver estado de comandas) | ✅ Completo |
| Mozo — mis estadísticas con detalle por mesa | ✅ Completo |
| Comprobante X editable (nombre/CUIT cliente) | ✅ Completo |
| **Menú Online (`/carta`)** | ✅ Completo — dark theme, carrito persistente |
| **Pedidos Web → admin en tiempo real** | ✅ Completo — columna dedicada 🌐 WEB |
| **Geolocalización delivery** | ✅ Completo — distancia, cobertura, precio por km |
| **Asignación repartidor desde admin** | ✅ Completo — lista real de usuarios |

### Historial de cambios principales — v1.1.0 (2026-06-06)
- Menú Online (`/carta`) rediseñado con dark theme (fondo negro, acento ámbar)
- Carrito persistente en localStorage (sobrevive refresco de página)
- Botón flotante "Ver pedido" en parte superior con animación y resplandor
- Pago exclusivo por transferencia: Alias y CBU/CVU con botones "Copiar" (feedback visual)
- Geolocalización automática al seleccionar "Envío a domicilio":
  - Distancia calculada con Haversine vs ubicación del local
  - Costo de envío automático por km o tarifa fija
  - Cobertura máxima configurable — muestra error si excede
- "Retiro en Local" → link directo a Google Maps con coordenadas del negocio
- Auto-relleno de dirección vía reverse geocoding (Nominatim)
- Botón WhatsApp deshabilitado hasta: nombre + pago copiado + modo seleccionado + cobertura ok
- Pedidos web aparecen en panel admin columna **🌐 PEDIDOS WEB** (en tiempo real)
- Fix crítico: bypass de auth para `/api/web/` (antes fallaba con 401 silencioso)
- Pedidos web muestran ítems detallados con precio, distancia km, botón GPS
- Asignación de repartidor desde el pipeline de pedidos (lista real de usuarios)
- App repartidor: navegación GPS directa cuando el pedido tiene coordenadas
- Config: Teléfono y WhatsApp separados; lat/lon del local, radio y precio por km
- Módulo "Delivery" renombrado a "Pedidos"; columna "NUEVO" → "MOSTRADOR"
- Sidebar auto-colapsa en desktop al navegar
- Icono admin con cierre de sesión movido al topbar (junto al reloj)

### Historial de cambios principales — v1.0.0 (2026-06-05)
- Personas por mesa; timer eliminado de las cards
- Comprobante X editable para datos del cliente (facturas)
- Fix `[object Object]` en dashboard delivery
- Cocina: sincronización completa con mesas y delivery (Socket.io + polling 45s)
- Cocina: fix botones (UUIDs entre comillas en onclick)
- Sync delivery → cocina → admin en tiempo real
- Comanda mozo: solo se envía a cocina al presionar el botón "Comanda"
- Mozo: bottom tab bar nativo mobile (Mesas / Cocina / Stats)
- Mozo stats: 2 tarjetas alineadas + popup de detalle por mesa

---

## Railway

- **Token API**: `55ea6497-1857-4e12-a4ab-a31565de4d0c`
- **Workspace ID**: `8cfdea71-3014-4919-a9f2-ea178c15b881`
- **Proyecto**: `javito-portal` (ID: `ea348246-a9fb-4251-89a3-1f469cb26fbc`)
- **Servicio app**: `pizzeria-pro` (ID: `7d9be2a3-4f7c-4fdd-b93c-21f6d6796aa5`)
- **Servicio DB**: `Postgres` (ID: `1c3c173c-a230-4e8e-8f5a-f03bb96af1ff`)
- **Environment**: `production` (ID: `3b68f44b-d69f-4933-b8c1-063b68d23b3f`)

### Llamar la API de Railway
```bash
curl -s -X POST https://backboard.railway.app/graphql/v2 \
  -H "Authorization: Bearer 55ea6497-1857-4e12-a4ab-a31565de4d0c" \
  -H "Content-Type: application/json" \
  -d '{"query":"..."}'
```

## Git — Flujo obligatorio

**SIEMPRE** pushear a los 3 branches y luego deployar con SHA exacto:
```bash
git push origin master-sync
git push origin master-sync:master
git push origin master-sync:claude/portal-replica-multiagent-H53V3

SHA=$(git rev-parse HEAD)
curl -s -X POST https://backboard.railway.app/graphql/v2 \
  -H "Authorization: Bearer 55ea6497-1857-4e12-a4ab-a31565de4d0c" \
  -H "Content-Type: application/json" \
  -d "{\"query\":\"mutation { serviceInstanceDeploy(serviceId: \\\"7d9be2a3-4f7c-4fdd-b93c-21f6d6796aa5\\\", environmentId: \\\"3b68f44b-d69f-4933-b8c1-063b68d23b3f\\\", commitSha: \\\"$SHA\\\") }\"}"
```

**IMPORTANTE**: NO usar `serviceInstanceDeployV2` — usa un commit cacheado viejo.
Usar siempre `serviceInstanceDeploy` con `commitSha` explícito.

---

## Arquitectura del Sistema

### Stack
- **Backend**: `app.js` — Express + Socket.io + PostgreSQL (`pg`)
- **Frontend**: `public/index.html` — SPA monolítica (~5300 líneas)
- **Persistencia**: PostgreSQL Railway (tabla `app_state` JSONB) + `localStorage` para config del negocio
- **Impresión**: QZ Tray (ESC/POS) con fallback a window.print()
- **Deploy**: Railway desde branch `master`

### Archivos principales
| Archivo | Rol |
|---|---|
| `app.js` | Backend Express + WebSocket + API REST |
| `public/index.html` | SPA admin + mozo (mismo archivo, ruta define rol) |
| `public/cocina.html` | Pantalla cocina (solo lectura de comandas) |
| `public/repartidor.html` | App repartidor delivery |
| `public/carta.html` | Carta QR para clientes (sin botón volver al portal) |
| `public/menu.html` | Menú online para clientes (sin botón volver al portal) |
| `public/portal.html` | Portal de acceso principal |
| `public/cliente.html` | Portal clientes |

### Rutas de acceso
- `/admin` → `index.html` (rol admin)
- `/mozo` → `index.html` (rol mozo)
- `/cocina` → `cocina.html`
- `/repartidor` → `repartidor.html`
- `/carta` → `carta.html` (QR para clientes — sin navegación al portal)
- `/menu` → `menu.html` (menú online — sin navegación al portal)

---

## Persistencia de estado

### Backend (PostgreSQL)
`app_state` tabla con columna JSONB. El estado global se guarda/carga con `saveState()` / `loadStateFromAPI()`.

```javascript
// Estado en memoria (app.js)
let appState = {
  mesas: [],
  productos: [],
  delivery: [],
  facturas: [],
  clientes: [],
  cajaMoves: [],
  mozoHistorial: [],
  comandas: [],
  printQueue: [],
  biz_cfg: {}     // config del negocio
}
```

### Frontend (localStorage)
- `pz_biz_cfg` — configuración del negocio (nombre, logo, medios de pago, propinas, etc.)
- `pz_state` — estado completo (mesas, productos, delivery, facturas, clientes, caja)
- `pz_user` — usuario logueado actual

### `getBizConfig()` / `saveBizConfig()`
```javascript
// Lee siempre del localStorage
function getBizConfig() {
  return {...defaults, ...JSON.parse(localStorage.getItem('pz_biz_cfg') || '{}')};
}
// Para guardar: siempre hacer saveState() después para sincronizar con el backend
localStorage.setItem('pz_biz_cfg', JSON.stringify(biz));
saveState();
```

---

## Sistema de Autenticación y Roles

### Roles disponibles
- `admin` / `supervisor` — acceso completo
- `mozo` — solo mesas y delivery asignados
- `cajero` — caja y cobros

### Login y branding
```javascript
// setupLoginBranding() IIFE (~línea 2533):
// Determina el ícono de login ANTES de que loadBizConfig() corra
const roles = {
  '/mozo':  { emoji:'🤵', accent:'#059669', accentL:'#34d399' },
  '/admin': { emoji:'🛡️', accent:'#7c3aed', accentL:'#a78bfa' },
};
```

**REGLA CRÍTICA**: `loadBizConfig()` NUNCA reemplaza el emoji de la pantalla de login.
El logo del negocio va en el sidebar y en los recibos, NO en el login.

### Íconos de rol
| Rol | Login | Sidebar |
|---|---|---|
| admin | 🛡️ animado (`lgFloat`) | 🛡️ animado (`sFloat`) |
| mozo | 🤵 animado (`lgFloat`) | 🤵 animado (`sFloat`) |
| cajero | 💰 | 💰 |
| cocina | 👨‍🍳 | 👨‍🍳 |

---

## Sistema de Impresión (QZ Tray / ESC/POS)

### Funciones ESC/POS en index.html
| Función | Uso |
|---|---|
| `_ep(mesa, lines, header, footer)` | Builder base ESC/POS |
| `epComanda(mesa, items)` | Ticket de cocina |
| `epCuenta(mesa, items)` | Ticket de cuenta de mesa |
| `epComprobanteX(f)` | Comprobante X (cierre de mesa) |
| `epComandaDelivery(o)` | Ticket de comanda delivery |

### Formato de ítems en tickets
```
3x Coca Cola 500ml
$600 x un.    subt. $1800
```
Implementado con: `left = '$X x un.'`, `right = 'subt. $Y'`, `gap = cols - left.length - right.length`

### Print hub
`/api/print` (POST) → emite `print:job` via Socket.io → dispositivo admin con QZ Tray imprime.
Fallback: `window.open + window.print()` si QZ Tray no está disponible.

---

## Módulos de Configuración (Config)

Todas las configuraciones viven en `getBizConfig()` / `localStorage pz_biz_cfg` y se sincronizan al backend con `saveState()`.

### Medios de Pago (`biz.mediosPago`)
```javascript
// Estructura de cada medio
{ id: 'efectivo', nombre: 'Efectivo', icono: '💵', recargo: 0 }
// recargo: porcentaje que se suma al total al cobrar (mesas y delivery)
```
- `getMediosPago()` — devuelve array (con defaults si no hay config)
- `renderMediosPagoConfig()` — renderiza la lista editable en config
- `saveMedioPago(i)`, `deleteMedioPago(i)`, `addMedioPago()`
- El recargo se aplica solo al momento del cobro, no a los precios de los ítems

### Propinas (`biz.propinaPct`)
```javascript
biz.propinaPct = 10  // % de propina sugerida (0 = no mostrar)
```
- `getPropinaPct()` — devuelve el % configurado (0 si no hay)
- `savePropina()` — guarda desde el input `#propinaPct`
- Cuando `propinaPct > 0`, los tickets muestran:
  ```
  SUBTOTAL          $10000
  Con Propina 10%   $11000

  TOTAL sin Propina $10000
  ```
- Aparece en: `epCuenta`, `epComprobanteX`, `_buildComprobanteHtml`, `verComprobanteX`
- La propina es SUGERIDA — el total almacenado en la factura NO incluye propina

---

## Módulo de Mesas

### Estado de una mesa
```javascript
{ id, numero, capacidad, estado: 'libre'|'ocupada'|'cuenta', zona, mozo, pedido: [{productoId, nombre, size, precio, qty, categoria, nota}] }
```

### Flujo de cobro con recargo
1. Click "Cobrar" → `_mesaCobroMode = true` → muestra botones por medio de pago con % de recargo
2. Selección de medio → `_mesaCobroConfirm = mpId` → muestra confirmación con total final
3. Confirmación → `cerrarMesa(id, metodoPago, totalFinal)` — recibe el total ya calculado con recargo

### `cerrarMesa(id, metodoPago, totalOverride)`
- `totalOverride` es el total final ya calculado (con recargo incluido)
- Genera una factura en `facturasData` y un movimiento de caja

---

## Módulo Delivery

### Estado de un pedido delivery
```javascript
{ id, numero, cliente: {nombre, telefono, direccion}, items: [{nombre, qty, precio, size, nota}], estado: 'nuevo'|'cocina'|'en_camino'|'entregado', metodo_pago, nota, total }
```

### Estados y transiciones
- `nuevo` → se puede cancelar con `cancelarDelivery(id)`
- `nuevo` → `cocina` → `en_camino` → `entregado`
- Solo se puede cancelar en estado `nuevo` (antes de ir a cocina)

### Formulario de nuevo pedido
- Autocompletado de cliente desde `clientesData` (nombre, teléfono, dirección)
- Ítems en filas compactas: selector de producto + tamaño + cantidad + nota + ✕
- Total en vivo con recargo según medio de pago seleccionado
- Nota del pedido: campo inline compacto

---

## Módulo Cocina (`cocina.html`)

Al conectar vía Socket.io, fetchea comandas reales:
```javascript
socket.on('connect', () => {
  fetch('/api/cocina/comandas').then(r=>r.json()).then(data => {
    comandas = data.map(c => ({...c, createdAt: new Date(c.createdAt)}));
    renderAll(null);
  });
});
```

---

## Reglas de Código

### TDZ (Temporal Dead Zone)
`const`/`let` no están disponibles antes de su línea de declaración — ni siquiera para funciones declaradas antes pero llamadas después.
**Fix**: No usar constantes de módulo; inline los valores por defecto directamente en la función.

### Agregar una nueva feature de configuración
1. Agregar card HTML en `#sec-config` (después de la card de Medios de Pago / Propinas)
2. Agregar campo `id="miFeature"` en el HTML
3. Agregar `getMyFeature()` / `saveMyFeature()` en la sección `// ======= CONFIG =======`
4. En `loadBizConfig()`: leer `biz.miFeature` y poblar el campo HTML
5. En `saveBizConfig()` o en la función save dedicada: escribir a `biz.miFeature`, hacer `localStorage.setItem` y `saveState()`

### Agregar dato a tickets ESC/POS
- Para `epCuenta`: modificar la función en `~línea 1787`
- Para `epComprobanteX`: modificar en `~línea 1820`
- Para HTML impreso: modificar `_buildComprobanteHtml` en `~línea 1860`
- Para modal de vista: modificar `verComprobanteX` en `~línea 1930`
- Para delivery: modificar `epComandaDelivery` en `~línea 3531`

---

## API Endpoints principales (app.js)

### Mesas
- `GET /api/mesas` — lista todas
- `POST /api/mesas` — crear mesa (auth requerida)
- `POST /api/mesas/:id/abrir` — asignar mozo
- `POST /api/mesas/:id/cerrar` — cerrar/liberar mesa
- `POST /api/mesas/:id/pedido` — agregar ítem al pedido
- `DELETE /api/mesas/:id/pedido/:itemId` — quitar ítem

### Delivery
- `GET /api/delivery` — todos los pedidos
- `GET /api/delivery/activos` — pedidos activos (estado != entregado/cancelado)
- `POST /api/delivery` — crear pedido
- `PUT /api/delivery/:id/estado` — cambiar estado

### Cocina
- `GET /api/cocina/comandas` — comandas activas
- `POST /api/cocina/comanda` — nueva comanda
- `PUT /api/cocina/comandas/:id/estado` — cambiar estado comanda

### Impresión
- `POST /api/print` — encolar trabajo de impresión (broadcast via Socket.io)
- `GET /api/print/queue` — cola actual

### Estado global
- `GET /api/state` — estado completo del sistema
- `POST /api/state` — guardar estado completo

---

## Verificación con Playwright

```javascript
const { chromium } = require('playwright'); // disponible en /home/user/javito/node_modules/playwright
// Iniciar servidor local: node app.js &
// Navegar a http://localhost:3000/admin o /mozo
// Capturar errores: page.on('pageerror', e => errors.push(e.message))
// Inyectar localStorage antes de reload para simular configuraciones
```

---

## Notas de Deploy

1. `node app.js` corre en puerto 3000 (o `PORT` env var)
2. Railway inyecta `DATABASE_URL` para PostgreSQL
3. El `PORT` en Railway se asigna automáticamente
4. Si el deploy no refleja cambios: verificar que se usó `serviceInstanceDeploy` con `commitSha`

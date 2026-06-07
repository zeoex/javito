# ResTito — Guía de Uso por Módulo

**Versión:** 1.1.0  
**URL de producción:** https://restito-production.up.railway.app

Esta guía explica cómo usar cada módulo del sistema ResTito, paso a paso, para cada tipo de usuario.

---

## MÓDULO ADMIN

---

### 1. Acceso y Login

**URL de acceso:** `https://restito-production.up.railway.app/admin`

1. Ingresar el email de la cuenta en el campo "Correo electrónico".
2. Ingresar la contraseña.
3. Presionar el botón "Ingresar" o la tecla Enter.

**Qué ve cada rol:**

| Rol | Secciones disponibles |
|---|---|
| admin / supervisor | Dashboard, Mesas, Pedidos, Cocina, Caja, Facturación, Reportes, Productos, Clientes, Usuarios, Configuración |
| cajero | Dashboard, Mesas, Caja, Facturación |
| mozo | Solo el módulo de mesas asignadas (via `/mozo`) |

**Cierre de sesión:** Hacer clic en el avatar del usuario en la esquina superior derecha del topbar y seleccionar "Cerrar sesión".

**Credenciales de demo por defecto:**

| Usuario | Contraseña | Rol |
|---|---|---|
| admin@restito.com | admin123 | Admin completo |
| cajero01@restito.com | cajero123 | Cajero |
| carlos@restito.com | mozo123 | Mozo |
| repartidor@restito.com | delivery123 | Repartidor |

---

### 2. Dashboard

El dashboard es la pantalla de inicio que muestra el estado actual del negocio en tiempo real.

**Métricas del panel:**

- **Ventas del día:** Suma de todos los pedidos cobrados en el día.
- **Mesas ocupadas:** Cantidad de mesas en estado "ocupada" o "cuenta".
- **Pedidos activos:** Pedidos en proceso (no entregados ni cancelados).
- **Delivery activos:** Pedidos delivery en tránsito.
- **Saldo en caja:** Balance actual de la caja abierta.
- **Comandas pendientes:** Comandas que aún no comenzaron a prepararse.

Los datos se actualizan automáticamente cada 10 segundos via Socket.io.

---

### 3. Módulo Mesas

**Acceso:** Clic en "Mesas" en el menú lateral izquierdo.

#### Ver mesas

Las mesas aparecen como tarjetas con código de colores:
- **Verde** — Mesa libre
- **Amarillo/Naranja** — Mesa ocupada
- **Rojo** — Mesa pidiendo la cuenta

#### Crear una mesa nueva (solo admin)

1. Hacer clic en el botón "Nueva Mesa" (parte superior derecha del módulo).
2. Completar el número de mesa, zona (salón, vereda, terraza, etc.) y capacidad.
3. Hacer clic en "Guardar".

#### Abrir una mesa

1. Hacer clic sobre una mesa en estado **libre**.
2. En el modal que aparece, seleccionar el mozo asignado.
3. Ingresar la cantidad de personas (opcional).
4. Hacer clic en "Abrir Mesa".

La mesa cambia a estado **ocupada**.

#### Agregar ítems al pedido

1. Hacer clic sobre una mesa **ocupada**.
2. En el modal de la mesa, ir a la sección "Agregar ítem".
3. Seleccionar la categoría del producto.
4. Seleccionar el producto del listado.
5. Si el producto tiene variantes (tamaño o presentación), seleccionar la opción.
6. Ajustar la cantidad con los botones +/-.
7. Agregar una nota o aclaración si es necesario (sin cebolla, bien cocido, etc.).
8. Hacer clic en "Agregar al pedido".

El ítem aparece en la lista del pedido y el total se actualiza automáticamente.

#### Enviar comanda a cocina

1. Con ítems en el pedido, hacer clic en "Comanda" o "Enviar a cocina".
2. La comanda llega en tiempo real a la pantalla `/cocina`.
3. El mozo recibe una notificación cuando la comanda está lista.

**Nota:** Solo se envía a cocina al presionar el botón "Comanda", no automáticamente al agregar ítems.

#### Cobrar una mesa

1. Hacer clic sobre la mesa ocupada → botón "Cobrar".
2. El sistema muestra los medios de pago disponibles con su % de recargo si corresponde.
3. Seleccionar el medio de pago (por ejemplo, "Tarjeta +5%").
4. Confirmar el total final que incluye el recargo.
5. Hacer clic en "Confirmar cobro".

La mesa queda libre, se genera un comprobante en el módulo de Facturación, y el movimiento se registra en Caja.

#### Transferir una mesa

1. Con una mesa **ocupada**, buscar la opción "Transferir".
2. Seleccionar la mesa destino (debe estar libre).
3. Confirmar. El pedido completo se mueve a la mesa destino.

#### Unir mesas

1. Desde el módulo de mesas, buscar la opción "Unir mesas".
2. Seleccionar la mesa principal y las mesas a unir (deben estar ocupadas).
3. Los pedidos se consolidan en la mesa principal.

---

### 4. Módulo Pedidos (Delivery)

**Acceso:** Clic en "Pedidos" en el menú lateral.

El módulo de pedidos usa un sistema de pipeline (tablero Kanban horizontal) con las siguientes columnas:

```
🌐 PEDIDOS WEB → MOSTRADOR → EN COCINA → LISTO → EN CAMINO → ENTREGADO
```

La columna **PEDIDOS WEB** recibe automáticamente los pedidos enviados desde `/carta` (menú online).

#### Crear un nuevo pedido desde admin

1. Hacer clic en "Nuevo Pedido".
2. Completar los datos del cliente:
   - Nombre (el sistema sugiere clientes existentes mientras se escribe)
   - Teléfono
   - Dirección de envío (o "Retiro en local" si corresponde)
3. Agregar ítems:
   - Seleccionar producto desde el selector
   - Elegir tamaño o presentación si aplica
   - Ajustar cantidad
   - Agregar nota si es necesario
   - El total se actualiza automáticamente
4. Seleccionar medio de pago (los recargos se aplican automáticamente).
5. Seleccionar repartidor (opcional en este paso).
6. Hacer clic en "Crear Pedido".

#### Avanzar un pedido por el pipeline

1. En la tarjeta del pedido, hacer clic en el botón de acción correspondiente al estado siguiente.
2. El pedido se mueve a la columna siguiente en tiempo real para todos los usuarios conectados.

#### Asignar un repartidor

1. En la tarjeta del pedido, buscar el selector de repartidor.
2. La lista muestra los usuarios con rol `repartidor` activos.
3. Seleccionar el repartidor deseado.
4. El repartidor verá el pedido asignado en su app (`/repartidor`).

#### Editar el costo de envío

1. En la tarjeta del pedido web, buscar el campo de costo de envío.
2. Modificar el monto.
3. El cambio se guarda y sincroniza automáticamente.

#### Ver pedidos anteriores

1. Los pedidos entregados se muestran al final de la columna "ENTREGADO" o en el historial.
2. Usar el filtro de fecha para buscar por rango.
3. Hacer clic en un pedido para expandir el detalle (ítems, cliente, distancia, GPS).

---

### 5. Módulo Cocina (pantalla separada)

**URL:** `https://restito-production.up.railway.app/cocina`

No requiere login. Se usa en una pantalla o tablet en la cocina.

#### Estructura de la pantalla

La pantalla muestra tres columnas:
- **PENDIENTE** (rojo) — Comandas recibidas, aún no comenzadas
- **EN PREPARACIÓN** (naranja) — Comandas que están siendo preparadas
- **LISTO** (verde) — Comandas terminadas, esperando ser retiradas

Cada comanda muestra:
- Número de comanda
- Tipo (MESA o DELIVERY) con número de mesa o nombre del cliente
- Mozo o cliente asignado
- Lista de ítems con cantidad y variante
- Timer de tiempo transcurrido (verde < 10min, naranja 10-20min, rojo > 20min)

#### Acciones sobre una comanda

1. **Iniciar preparación:** Hacer clic en el botón naranja "INICIAR PREPARACIÓN". La comanda pasa a la columna del medio.
2. **Marcar como listo:** Hacer clic en el botón verde "MARCAR COMO LISTO". La comanda pasa a la columna derecha.
   - Se emite automáticamente una notificación al mozo (para mesas) o al admin (para delivery).
3. **Marcar entregado:** Hacer clic en el botón violeta "MARCAR ENTREGADO". La comanda desaparece del tablero.

También es posible arrastrar y soltar (drag & drop) las tarjetas entre columnas.

#### Sincronización en tiempo real

- Al conectarse, la pantalla carga todas las comandas activas vía `GET /api/cocina/comandas`.
- Las nuevas comandas aparecen automáticamente con animación y sonido (beep).
- Se hace polling de respaldo cada 45 segundos para recuperar eventos perdidos.
- El indicador en la esquina superior muestra "EN VIVO" cuando hay conexión Socket.io activa.

#### Botón F1

Presionar F1 en el teclado genera una comanda de prueba simulada (útil para demostración).

---

### 6. Módulo Caja

**Acceso:** Clic en "Caja" en el menú lateral.

#### Abrir la caja

1. Si no hay caja abierta, el módulo mostrará un botón "Abrir Caja".
2. Ingresar el saldo inicial (el dinero físico disponible al inicio del turno).
3. Hacer clic en "Abrir". Se registra un movimiento de ingreso por el saldo inicial.

#### Ver el estado actual

La pantalla muestra:
- Saldo actual (suma de ingresos menos egresos)
- Lista de todos los movimientos del día con hora, concepto y monto

#### Registrar un movimiento manual

1. Hacer clic en "Agregar Movimiento".
2. Seleccionar tipo: **Ingreso** (entrada de dinero) o **Egreso** (salida de dinero).
3. Completar el concepto (descripción del movimiento).
4. Ingresar el monto.
5. Hacer clic en "Guardar".

Ejemplos de uso: compra de insumos (egreso), propinas retiradas (egreso), cambio de fondo (ingreso).

#### Cerrar la caja

1. Hacer clic en "Cerrar Caja".
2. El sistema muestra el saldo final calculado automáticamente.
3. Confirmar el cierre.

El cierre queda registrado con hora, saldo final y un resumen de movimientos.

---

### 7. Módulo Facturación

**Acceso:** Clic en "Facturación" en el menú lateral.

#### Ver comprobantes generados

La pantalla muestra una tabla con todos los comprobantes, ordenados por fecha. Cada fila muestra:
- Número de comprobante (formato `F-000001`)
- Tipo (B)
- Total
- Método de pago
- Fecha

#### Buscar comprobantes

- Usar el filtro de fecha para acotar el rango.
- Usar el buscador por nombre de cliente o número de comprobante.

#### Ver detalle de un comprobante

1. Hacer clic en el comprobante de la lista.
2. Se abre el modal con el detalle completo:
   - Ítems del pedido
   - Subtotal, IVA (21%), total
   - Datos del cliente (nombre y CUIT si se completaron)
   - Método de pago

#### Comprobante X editable

El Comprobante X permite ingresar nombre y CUIT del cliente para facturación. Estos datos son editables directamente en el modal antes de imprimir.

---

### 8. Módulo Productos

**Acceso:** Clic en "Productos" en el menú lateral.

#### Ver el catálogo

Los productos se muestran agrupados por categoría. Cada producto indica si está activo o inactivo.

#### Crear un nuevo producto

1. Hacer clic en "Nuevo Producto".
2. Completar los campos:
   - **Nombre** (obligatorio)
   - **Descripción** — texto que aparece en el menú online
   - **Emoji** — ícono que se muestra cuando no hay foto
   - **Foto** — imagen del producto (base64, máx 2MB)
   - **Categoría** — seleccionar de la lista existente
3. Seleccionar el **tipo de precio**:
   - **Precio único:** Completar solo el campo "Precio"
   - **Por tamaño (Chica/Mediana/Grande):** Completar precio para cada tamaño disponible
   - **Por presentación:** Agregar filas dinámicas con nombre de presentación y precio (Ej: "Entero - $2000", "Mitad - $1200")
4. Hacer clic en "Guardar".

El producto queda disponible de inmediato en el menú de mesas/pedidos y en el menú online.

#### Editar un producto

1. En la lista de productos, hacer clic en el ícono de edición del producto.
2. Modificar los campos deseados.
3. Hacer clic en "Guardar".

#### Activar / Desactivar un producto

Usar el toggle de activo/inactivo en la tarjeta del producto. Un producto inactivo no aparece en el menú de mesas ni en el menú online.

#### Gestión de categorías

En la sección de categorías (dentro del módulo Productos):
1. Hacer clic en "Nueva Categoría".
2. Ingresar ID (texto, ej: `pizzas`), nombre, emoji, y si los ítems de esta categoría se envían automáticamente a cocina.
3. Guardar.

Las categorías se ordenan por el campo "Orden" (número entero). Para reorganizar el orden, editar cada categoría y cambiar el número.

---

### 9. Módulo Clientes

**Acceso:** Clic en "Clientes" en el menú lateral.

#### Ver el listado

Tabla con todos los clientes registrados. Columnas: nombre, teléfono, email, cantidad de pedidos.

#### Crear un cliente manualmente

1. Hacer clic en "Nuevo Cliente".
2. Completar:
   - Nombre (obligatorio)
   - Email
   - Teléfono
   - Direcciones (se pueden agregar múltiples con calle, barrio y referencia)
3. Hacer clic en "Guardar".

#### Autocompletado en pedidos delivery

Al crear un pedido de delivery, al escribir el nombre del cliente en el formulario, el sistema sugiere clientes existentes. Al seleccionar uno, los campos de teléfono y dirección se completan automáticamente.

#### Ver historial de un cliente

Hacer clic en un cliente de la lista para ver su perfil con el historial de pedidos anteriores.

---

### 10. Módulo Reportes

**Acceso:** Clic en "Reportes" en el menú lateral.

#### Ventas por período

Seleccionar el período con el selector:
- **Hoy** — ventas del día actual
- **Semana** — últimos 7 días
- **Mes** — mes calendario actual

La pantalla muestra:
- Total recaudado en el período
- Cantidad de pedidos
- Desglose por método de pago (efectivo, tarjeta, transferencia, etc.)

#### Productos más vendidos

Lista del top 10 de productos por cantidad vendida en el período seleccionado. Muestra nombre, cantidad de unidades y total recaudado.

---

### 11. Módulo Usuarios

**Acceso:** Clic en "Usuarios" en el menú lateral (solo rol admin/supervisor).

#### Ver usuarios

Tabla con todos los usuarios del sistema con nombre, email, rol y estado (activo/inactivo).

#### Crear un usuario

1. Hacer clic en "Nuevo Usuario".
2. Completar:
   - Nombre completo
   - Email (es el nombre de usuario para el login)
   - Contraseña
   - Rol (ver tabla más abajo)
   - Teléfono (útil para repartidores)
3. Hacer clic en "Guardar".

**Roles disponibles:**

| Rol | Descripción |
|---|---|
| `admin` | Acceso completo a todas las secciones |
| `supervisor` | Igual que admin |
| `cajero` | Solo caja, cobros y facturación |
| `mozo` | Solo mesas asignadas y comandas (via `/mozo`) |
| `cocinero` | Solo pantalla cocina (via `/cocina`) |
| `repartidor` | Solo app de repartidor (via `/repartidor`) |

#### Desactivar un usuario

Usar el toggle de activo/inactivo. Un usuario desactivado no puede hacer login.

---

### 12. Configuración del Negocio

**Acceso:** Clic en "Configuración" en el menú lateral.

#### Datos del negocio

- **Nombre del negocio** — aparece en el header del menú online, tickets y recibos
- **Dirección** — aparece en el menú online y en tickets
- **Teléfono** — número de contacto general
- **WhatsApp** — número para pedidos por WhatsApp (solo dígitos, sin + ni espacios; ej: `5491144445555`)
- **Mensaje de pie de recibo** — texto que aparece al final de los tickets impresos

#### Logo del negocio

1. Hacer clic en "Subir Logo" o en la imagen actual.
2. Seleccionar un archivo PNG o JPG (máximo 2 MB).
3. El logo se guarda como base64 en `localStorage` y se sincroniza con el servidor.
4. Aparece en el menú online (`/carta`) y en el sidebar del sistema admin.

#### Configuración de transferencia (para menú online)

Estos datos aparecen en el formulario de confirmación de pedidos en `/carta`:
- **Alias** — alias de MercadoPago o banco (ej: `RESTITO.MP`)
- **CBU/CVU** — número de cuenta bancaria para transferencias

Los clientes verán botones "Copiar" para cada dato.

#### Geolocalización delivery

- **Latitud y Longitud del local** — coordenadas del negocio. Hacer clic en "Usar mi ubicación actual" para obtenerlas automáticamente.
- **Radio máximo de delivery (km)** — distancia máxima a la que se hace delivery. Si el cliente está más lejos, se muestra un error y no puede enviar el pedido.
- **Costo de envío base ($)** — tarifa fija de envío (si `Precio por km` es 0, se usa este valor siempre).
- **Precio por km ($)** — si es mayor a 0, el costo de envío se calcula multiplicando por la distancia Haversine al cliente.

#### Medios de pago con recargo

1. En la sección "Medios de Pago", hacer clic en "Agregar Medio de Pago".
2. Completar:
   - Nombre (ej: "Tarjeta de crédito")
   - Ícono (emoji)
   - Recargo (%) — porcentaje que se suma al total al cobrar (0 para sin recargo)
3. Guardar.

Los medios de pago con recargo muestran el porcentaje al momento de cobrar mesas o pedidos. El precio de los ítems no se modifica; el recargo se aplica solo al total final.

#### Propina sugerida

Ingresar el porcentaje de propina sugerida (0 para no mostrar propina). Cuando el valor es mayor a 0, los tickets impresos muestran dos totales:
```
SUBTOTAL                $10.000
Con Propina 10%         $11.000

TOTAL sin Propina       $10.000
```
La propina es únicamente sugerida; el cobro registrado en la factura siempre usa el total sin propina.

#### Configuración de comandera (impresora)

- **Ancho de papel:** Seleccionar 58mm (32 columnas) o 80mm (48 columnas).
- **Impresora QZ Tray:** Seleccionar la impresora instalada en el sistema desde el selector desplegable.
- **Estado:** El badge indica si QZ Tray está activo en la PC actual.
- **Prueba de impresión:** Hacer clic en "Imprimir prueba" para verificar la conexión.

---

## MÓDULO MOZO

---

### 13. Módulo Mozo (`/mozo`)

**URL:** `https://restito-production.up.railway.app/mozo`

El módulo mozo está diseñado para uso en dispositivos móviles. Usa un layout oscuro optimizado para touch.

#### Login

1. Ir a `/mozo` e ingresar email y contraseña del usuario con rol `mozo`.
2. El ícono de login es el traje de mozo (🤵).

#### Ver mesas asignadas

La pantalla principal muestra todas las mesas. El mozo solo puede interactuar con sus mesas asignadas (las que tienen su ID de mozo).

Las mesas tienen tres estados visuales:
- **Verde** — Libre
- **Naranja** — Ocupada (con pedido en curso)
- **Rojo/Pulsante** — Pidiendo la cuenta

Cada tarjeta muestra el número de mesa (grande), el estado, y la cantidad de ítems en el pedido.

#### Agregar ítems a una mesa

1. Hacer tap en una mesa ocupada.
2. El modal de la mesa abre con la lista actual del pedido.
3. Navegar por categorías → productos.
4. Seleccionar el producto, variante (si aplica) y cantidad.
5. Agregar nota si es necesario.
6. Presionar "Agregar".

#### Enviar comanda a cocina

Dentro del modal de la mesa, presionar el botón "Comanda". La comanda llega a la pantalla de cocina y el total del pedido queda actualizado.

#### Tab de Cocina — ver estado de comandas

En la barra inferior del módulo mozo, presionar el tab "Cocina" (o el ícono de cocina). Muestra las comandas activas con su estado (pendiente / en preparación / listo).

Cuando una comanda queda lista, el sistema muestra una notificación en pantalla completa con campana animada indicando el número de mesa y los ítems listos.

#### Mis Estadísticas

Presionar el tab "Stats" en la barra inferior. Muestra:
- Dos tarjetas con resumen de ventas del turno
- Al hacer tap en una tarjeta, aparece un popup con el detalle de ventas por mesa

---

## MÓDULO CLIENTE (Menú Online)

---

### 14. Carta QR (`/carta`) y Menú Online (`/menu`)

**URL:** `https://restito-production.up.railway.app/carta`

Diseñada para que los clientes hagan pedidos directamente desde su celular escaneando el QR o entrando a la URL.

#### Navegación del cliente

1. El cliente accede a la URL o escanea el QR.
2. Ve el hero con el nombre del negocio, logo y subtítulo.
3. Debajo, un menú horizontal de categorías (sticky, se queda fijo al hacer scroll).
4. Al hacer scroll, la categoría activa se resalta automáticamente.

#### Agregar productos al carrito

1. Hacer tap en cualquier tarjeta de producto.
2. Si el producto tiene múltiples variantes (tamaño o presentación), se abre un bottom sheet para seleccionar.
3. Si tiene precio único, el sheet permite ingresar una nota/aclaración.
4. Hacer tap en "Agregar al pedido".
5. El producto se agrega al carrito.

**Botón flotante del carrito:** Una vez agregado el primer producto, aparece un botón flotante en la parte superior de la pantalla con animación de brillo. Muestra la cantidad de ítems y el total. Al hacer tap, se abre el resumen del carrito.

#### Carrito persistente

El carrito se guarda en `localStorage`. Si el cliente recarga la página o cierra y vuelve a abrir el navegador, el carrito se restaura automáticamente.

#### Modificar cantidades en el carrito

1. Hacer tap en el botón del carrito flotante.
2. En el modal del carrito, usar los botones `+` y `-` junto a cada ítem.
3. Bajar a 0 elimina el ítem del carrito.
4. El total se actualiza en tiempo real.

#### Confirmar el pedido

1. En el carrito, hacer tap en "Confirmar pedido".
2. Se abre el panel de confirmación con:
   - Resumen de ítems y totales
   - Subtotal + costo de envío (si aplica) + total final
   - Datos de transferencia bancaria (Alias y CBU/CVU) con botones "Copiar"
   - Formulario de nombre y apellido
   - Selector de modo de envío

#### Elegir modo de envío

**Envío a domicilio (geolocalización automática):**
1. Seleccionar "Envío a domicilio".
2. El navegador solicita permiso de geolocalización.
3. Al conceder, el sistema calcula la distancia al local usando la fórmula Haversine.
4. Si la distancia supera el radio máximo configurado, aparece un mensaje de error y el botón de WhatsApp queda desactivado.
5. El costo de envío se calcula automáticamente (tarifa base o precio por km).
6. Un campo de dirección se completa automáticamente via reverse geocoding (Nominatim/OpenStreetMap).

**Retiro en local:**
1. Seleccionar "Retiro en local".
2. Aparece un link "Ver en Google Maps" con las coordenadas del negocio.
3. No hay costo de envío.

#### Proceso de pago y envío por WhatsApp

El botón "Enviar pedido por WhatsApp" se habilita SOLO cuando se cumplen todas estas condiciones:
- Se completó nombre y apellido
- Se copió el Alias O el CBU/CVU (feedback visual en el botón Copiar)
- Se seleccionó el modo de envío (retiro o delivery)
- Si es delivery: la geolocalización fue exitosa y no excede el radio de cobertura

Al presionar el botón:
1. Se abre WhatsApp con un mensaje pre-armado que incluye el detalle del pedido, total, modo de envío, dirección si corresponde, y nombre del cliente.
2. El pedido se registra simultáneamente en el backend via `POST /api/web/pedido`.
3. El pedido aparece en el panel admin en la columna "PEDIDOS WEB" en tiempo real.

---

## MÓDULO REPARTIDOR

---

### 15. App Repartidor (`/repartidor`)

**URL:** `https://restito-production.up.railway.app/repartidor`

Diseñada como PWA para instalar en el celular del repartidor. Interfaz completamente mobile-first.

#### Login

1. Ingresar el email del usuario con rol `repartidor`.
2. Ingresar la contraseña.
3. Presionar "Iniciar turno".

La sesión se guarda en `localStorage`. Si el repartidor cierra el navegador y vuelve a entrar, la sesión se restaura automáticamente sin necesidad de volver a loguearse.

#### Toggle de disponibilidad

En el topbar hay un toggle "DISPONIBLE / NO DISPONIBLE". Al cambiar el estado, el admin puede saber si el repartidor está en condiciones de tomar pedidos. El estado se transmite via Socket.io.

#### Ver pedidos asignados

La tab "Pedidos" (activa por defecto) muestra todos los pedidos activos, ordenados por urgencia:
- Primero los pedidos en estado **LISTO P/ RETIRAR** o **ASIGNADO** (prioridad máxima)
- Luego los pedidos **EN CAMINO**
- Finalmente los pedidos en preparación

Cada tarjeta de pedido muestra:
- Número de pedido y hora
- Estado (badge con color)
- Nombre del cliente
- Dirección de entrega
- Teléfono (con link para llamar directamente)
- Lista de ítems
- Total y método de pago

#### Navegación GPS al destino

En cada tarjeta hay un botón de navegación:
- Si el pedido tiene coordenadas GPS (`lat_cliente` y `lon_cliente`): el botón dice "Navegar GPS" y abre Google Maps con la ruta directa usando las coordenadas exactas.
- Si el pedido solo tiene dirección textual: el botón dice "Ver en Maps" y abre una búsqueda en Google Maps.

#### Iniciar viaje

1. Cuando el pedido está en estado "LISTO" o "ASIGNADO", aparece el botón "Iniciar viaje".
2. Presionar el botón.
3. El estado del pedido cambia a "EN CAMINO" y se actualiza en el panel admin en tiempo real.
4. Aparecen los botones de GPS y "Marcar entregado".

#### Marcar como entregado

1. Una vez en destino y entregado el pedido, presionar "Marcar Entregado".
2. El estado cambia a "ENTREGADO" y se registra la hora de entrega.
3. El pedido pasa al historial.

#### Notificación de pedido listo (llamado)

Cuando un pedido pasa al estado "LISTO" desde la pantalla de cocina, el sistema envía una notificación visual al repartidor:
- Aparece un overlay pantalla completa con el mensaje "¡Pedido listo para retirar!"
- Suena una melodía de alerta.
- Presionar "Entendido — voy" cierra la notificación y confirma el llamado al servidor.

#### Historial de entregas

La tab "Historial" muestra todas las entregas anteriores, agrupadas por fecha (Hoy, Ayer, fechas anteriores). Para cada entrega:
- Número de pedido, cliente, dirección y hora de entrega
- Total y método de pago
- Botón para agregar un comentario (ej: "Cliente no estaba, dejé con portero")

Los comentarios y el historial se guardan localmente en `localStorage` (hasta 300 entregas).

#### Perfil

La tab "Perfil" muestra los datos del repartidor: nombre, email y teléfono. También incluye el botón "Cerrar sesión".

---

## Notas Generales de Uso

### Sincronización y tiempo real

Todos los módulos conectados al mismo servidor se sincronizan en tiempo real via Socket.io. Por ejemplo:
- Al agregar un ítem en una mesa desde la pantalla mozo, el admin lo ve actualizado al instante.
- Al marcar una comanda como lista en cocina, el mozo recibe la notificación inmediatamente.
- Al llegar un pedido del menú online, aparece en el pipeline del admin sin necesidad de recargar.

### Modo offline / sin servidor

Si el servidor no está disponible:
- La pantalla de cocina (`/cocina`) opera en "modo demo" con datos simulados (indicador "DEMO" en lugar de "EN VIVO").
- La app repartidor usa datos de demo hardcodeados.
- El menú online no puede enviar pedidos al servidor, pero el link de WhatsApp sigue funcionando.

### Sidebar en desktop

En pantallas grandes, el sidebar se puede colapsar haciendo clic en el botón de hamburguesa. Al navegar a otra sección, el sidebar se colapsa automáticamente para dar más espacio al contenido.

### PWA — Instalar como app

Los siguientes módulos se pueden instalar como app en el dispositivo:
- `/admin` — ícono de escudo violeta
- `/mozo` — ícono de mozo azul
- `/repartidor` — ícono rojo

En iOS: Safari → Compartir → "Agregar a pantalla de inicio".  
En Android: Chrome → menú (tres puntos) → "Instalar app".

'use strict';

// ─────────────────────────────────────────────
//  PIZZERÍA PRO – Backend completo (single file)
// ─────────────────────────────────────────────

const express    = require('express');
const http       = require('http');
const path       = require('path');
const cors       = require('cors');
const bcrypt     = require('bcryptjs');
const jwt        = require('jsonwebtoken');
const { Server } = require('socket.io');
const { v4: uuidv4 } = require('uuid');

const PORT       = process.env.PORT || 3000;
const JWT_SECRET = 'pizzeria-pro-secret-2024';
const JWT_EXPIRY = '8h';

// ─────────────────────────────────────────────
//  APP & SERVER
// ─────────────────────────────────────────────
const app    = express();
const server = http.createServer(app);
const io     = new Server(server, {
  cors: { origin: '*', methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'] }
});

// ─────────────────────────────────────────────
//  IN-MEMORY STORE
// ─────────────────────────────────────────────
const db = {
  users:      [],
  mesas:      [],
  productos:  [],
  categorias: [],
  pedidos:    [],
  comandas:   [],
  delivery:   [],
  clientes:   [],
  caja:       [],
  facturas:   [],
  stock:      []
};

// ─────────────────────────────────────────────
//  SEED DATA
// ─────────────────────────────────────────────
(async () => {
  // ---------- CATEGORÍAS ----------
  db.categorias = [
    { id: uuidv4(), nombre: 'Pizzas',        icono: '🍕', orden: 1 },
    { id: uuidv4(), nombre: 'Empanadas',     icono: '🥟', orden: 2 },
    { id: uuidv4(), nombre: 'Bebidas',       icono: '🥤', orden: 3 },
    { id: uuidv4(), nombre: 'Postres',       icono: '🍰', orden: 4 }
  ];

  const catPizzas    = db.categorias[0].id;
  const catEmpanadas = db.categorias[1].id;
  const catBebidas   = db.categorias[2].id;
  const catPostres   = db.categorias[3].id;

  // ---------- PRODUCTOS ----------
  db.productos = [
    {
      id: uuidv4(), codigo: 'PIZ001', nombre: 'Muzzarella',
      descripcion: 'Clásica pizza de muzzarella con salsa de tomate casera',
      categoria: catPizzas, precio: 1200, precioMediano: 1600, precioGrande: 2100,
      stock: 100, stockMinimo: 5, imagen: '', activo: true,
      extras: [
        { id: 'e1', nombre: 'Aceitunas', precio: 150 },
        { id: 'e2', nombre: 'Jamón',     precio: 250 }
      ]
    },
    {
      id: uuidv4(), codigo: 'PIZ002', nombre: 'Napolitana',
      descripcion: 'Tomate, muzzarella, tomates frescos, ajo y albahaca',
      categoria: catPizzas, precio: 1400, precioMediano: 1850, precioGrande: 2400,
      stock: 100, stockMinimo: 5, imagen: '', activo: true,
      extras: [
        { id: 'e1', nombre: 'Aceitunas', precio: 150 },
        { id: 'e3', nombre: 'Anchoas',   precio: 300 }
      ]
    },
    {
      id: uuidv4(), codigo: 'PIZ003', nombre: 'Fugazzeta',
      descripcion: 'Pizza rellena de muzzarella con cebolla y aceitunas',
      categoria: catPizzas, precio: 1600, precioMediano: 2100, precioGrande: 2700,
      stock: 100, stockMinimo: 5, imagen: '', activo: true,
      extras: [{ id: 'e2', nombre: 'Jamón', precio: 250 }]
    },
    {
      id: uuidv4(), codigo: 'PIZ004', nombre: 'Cuatro Quesos',
      descripcion: 'Muzzarella, provolone, gorgonzola y parmesano',
      categoria: catPizzas, precio: 1800, precioMediano: 2400, precioGrande: 3100,
      stock: 100, stockMinimo: 5, imagen: '', activo: true,
      extras: []
    },
    {
      id: uuidv4(), codigo: 'PIZ005', nombre: 'Especial de la Casa',
      descripcion: 'Jamón, morrón, aceitunas, huevo y salsa golf',
      categoria: catPizzas, precio: 2000, precioMediano: 2700, precioGrande: 3500,
      stock: 100, stockMinimo: 5, imagen: '', activo: true,
      extras: [{ id: 'e4', nombre: 'Extra queso', precio: 200 }]
    },
    {
      id: uuidv4(), codigo: 'EMP001', nombre: 'Empanadas de Carne',
      descripcion: 'Empanadas de carne cortada a cuchillo, jugosas y condimentadas',
      categoria: catEmpanadas, precio: 450, precioMediano: null, precioGrande: null,
      stock: 60, stockMinimo: 10, imagen: '', activo: true,
      extras: [{ id: 'e5', nombre: 'Picante', precio: 0 }]
    },
    {
      id: uuidv4(), codigo: 'EMP002', nombre: 'Empanadas de Jamón y Queso',
      descripcion: 'Jamón cocido y queso muzzarella derretido',
      categoria: catEmpanadas, precio: 420, precioMediano: null, precioGrande: null,
      stock: 60, stockMinimo: 10, imagen: '', activo: true,
      extras: []
    },
    {
      id: uuidv4(), codigo: 'BEB001', nombre: 'Coca-Cola',
      descripcion: 'Gaseosa Coca-Cola 500ml / 1.5L',
      categoria: catBebidas, precio: 600, precioMediano: 900, precioGrande: null,
      stock: 80, stockMinimo: 15, imagen: '', activo: true,
      extras: [{ id: 'e6', nombre: 'Con hielo', precio: 0 }]
    },
    {
      id: uuidv4(), codigo: 'BEB002', nombre: 'Agua Mineral',
      descripcion: 'Agua mineral sin gas 500ml',
      categoria: catBebidas, precio: 400, precioMediano: null, precioGrande: null,
      stock: 80, stockMinimo: 15, imagen: '', activo: true,
      extras: []
    },
    {
      id: uuidv4(), codigo: 'BEB003', nombre: 'Cerveza Quilmes',
      descripcion: 'Cerveza Quilmes botella 340ml',
      categoria: catBebidas, precio: 800, precioMediano: null, precioGrande: null,
      stock: 50, stockMinimo: 10, imagen: '', activo: true,
      extras: []
    },
    {
      id: uuidv4(), codigo: 'POS001', nombre: 'Tiramisú',
      descripcion: 'Tiramisú casero con mascarpone y café',
      categoria: catPostres, precio: 850, precioMediano: null, precioGrande: null,
      stock: 20, stockMinimo: 3, imagen: '', activo: true,
      extras: []
    },
    {
      id: uuidv4(), codigo: 'POS002', nombre: 'Promo Familiar',
      descripcion: '2 pizzas grandes + 2 bebidas 1.5L',
      categoria: catPostres, precio: 5800, precioMediano: null, precioGrande: null,
      stock: 999, stockMinimo: 0, imagen: '', activo: true,
      extras: []
    }
  ];

  // ---------- STOCK ----------
  db.stock = db.productos.map(p => ({
    productoId:  p.id,
    cantidad:    p.stock,
    movimientos: [
      { tipo: 'inicial', cantidad: p.stock, motivo: 'Stock inicial', fecha: new Date().toISOString() }
    ]
  }));

  // ---------- USUARIOS ----------
  const hash = pwd => bcrypt.hashSync(pwd, 10);
  db.users = [
    { id: uuidv4(), nombre: 'Administrador',  email: 'admin@pizzeriapro.com',      password: hash('admin123'),    rol: 'admin',      activo: true, createdAt: new Date().toISOString() },
    { id: uuidv4(), nombre: 'Supervisor',     email: 'supervisor@pizzeriapro.com', password: hash('super123'),    rol: 'supervisor', activo: true, createdAt: new Date().toISOString() },
    { id: uuidv4(), nombre: 'Cajero 01',      email: 'cajero01@pizzeriapro.com',   password: hash('cajero123'),   rol: 'cajero',     activo: true, createdAt: new Date().toISOString() },
    { id: uuidv4(), nombre: 'Mozo Martín',    email: 'mozo01@pizzeriapro.com',     password: hash('mozo123'),     rol: 'mozo',       activo: true, createdAt: new Date().toISOString() },
    { id: uuidv4(), nombre: 'Mozo Laura',     email: 'mozo02@pizzeriapro.com',     password: hash('mozo456'),     rol: 'mozo',       activo: true, createdAt: new Date().toISOString() },
    { id: uuidv4(), nombre: 'Cocinero Pedro', email: 'cocinero@pizzeriapro.com',   password: hash('cocina123'),   rol: 'cocinero',   activo: true, createdAt: new Date().toISOString() },
    { id: uuidv4(), nombre: 'Repartidor 01',  email: 'repartidor@pizzeriapro.com', password: hash('delivery123'), rol: 'repartidor', activo: true, createdAt: new Date().toISOString() }
  ];

  // ---------- MESAS ----------
  const mozoA = db.users.find(u => u.email === 'mozo01@pizzeriapro.com').id;
  const mozoB = db.users.find(u => u.email === 'mozo02@pizzeriapro.com').id;
  db.mesas = [
    { id: uuidv4(), numero: 1,  zona: 'Salón',   estado: 'libre',    mozoid: null,  apertura: null, consumo: 0, pedidos: [] },
    { id: uuidv4(), numero: 2,  zona: 'Salón',   estado: 'ocupada',  mozoid: mozoA, apertura: new Date(Date.now() - 45 * 60000).toISOString(), consumo: 3200, pedidos: [
      { id: uuidv4(), productoId: db.productos[0].id, nombre: 'Muzzarella', variante: 'grande', cantidad: 1, precio: 2100, extras: [], observacion: '' },
      { id: uuidv4(), productoId: db.productos[7].id, nombre: 'Coca-Cola',  variante: '1.5L',   cantidad: 2, precio: 900,  extras: [], observacion: '' }
    ]},
    { id: uuidv4(), numero: 3,  zona: 'Salón',   estado: 'libre',    mozoid: null,  apertura: null, consumo: 0, pedidos: [] },
    { id: uuidv4(), numero: 4,  zona: 'Salón',   estado: 'reservada',mozoid: mozoB, apertura: null, consumo: 0, pedidos: [] },
    { id: uuidv4(), numero: 5,  zona: 'Salón',   estado: 'libre',    mozoid: null,  apertura: null, consumo: 0, pedidos: [] },
    { id: uuidv4(), numero: 6,  zona: 'Terraza', estado: 'ocupada',  mozoid: mozoB, apertura: new Date(Date.now() - 20 * 60000).toISOString(), consumo: 1600, pedidos: [
      { id: uuidv4(), productoId: db.productos[1].id, nombre: 'Napolitana', variante: 'mediana', cantidad: 1, precio: 1850, extras: [], observacion: 'Sin ajo' }
    ]},
    { id: uuidv4(), numero: 7,  zona: 'Terraza', estado: 'libre',    mozoid: null,  apertura: null, consumo: 0, pedidos: [] },
    { id: uuidv4(), numero: 8,  zona: 'Terraza', estado: 'libre',    mozoid: null,  apertura: null, consumo: 0, pedidos: [] }
  ];

  // ---------- CLIENTES ----------
  db.clientes = [
    {
      id: uuidv4(), nombre: 'Juan García', email: 'juan@ejemplo.com', telefono: '11-4444-5555',
      direcciones: [{ id: uuidv4(), calle: 'Av. Corrientes 1234', barrio: 'Centro', referencia: 'Piso 3 dpto B' }],
      historial: [], createdAt: new Date().toISOString()
    },
    {
      id: uuidv4(), nombre: 'María López', email: 'maria@ejemplo.com', telefono: '11-6666-7777',
      direcciones: [{ id: uuidv4(), calle: 'Lavalle 567', barrio: 'Palermo', referencia: '' }],
      historial: [], createdAt: new Date().toISOString()
    },
    {
      id: uuidv4(), nombre: 'Carlos Fernández', email: 'carlos@ejemplo.com', telefono: '11-8888-9999',
      direcciones: [
        { id: uuidv4(), calle: 'Santa Fe 890',  barrio: 'Recoleta', referencia: '' },
        { id: uuidv4(), calle: 'Callao 321',    barrio: 'Balvanera', referencia: 'Casa con reja verde' }
      ],
      historial: [], createdAt: new Date().toISOString()
    }
  ];

  // ---------- CAJA DEL DÍA ----------
  const cajeroId = db.users.find(u => u.rol === 'cajero').id;
  db.caja = [
    {
      id: uuidv4(),
      fecha:        new Date().toISOString().split('T')[0],
      apertura:     new Date(Date.now() - 6 * 3600000).toISOString(),
      cierre:       null,
      saldoInicial: 5000,
      saldoFinal:   null,
      cajeroId,
      movimientos: [
        { id: uuidv4(), tipo: 'ingreso',  concepto: 'Apertura de caja',  monto: 5000, fecha: new Date(Date.now() - 6 * 3600000).toISOString() },
        { id: uuidv4(), tipo: 'ingreso',  concepto: 'Venta mesa 2',       monto: 3200, fecha: new Date(Date.now() - 3 * 3600000).toISOString() },
        { id: uuidv4(), tipo: 'egreso',   concepto: 'Compra ingredientes',monto: 1500, fecha: new Date(Date.now() - 2 * 3600000).toISOString() }
      ],
      estado: 'abierta'
    }
  ];

  console.log('[SEED] Base de datos inicializada correctamente');
  console.log(`[SEED] Usuarios: ${db.users.length} | Productos: ${db.productos.length} | Mesas: ${db.mesas.length}`);
})();

// ─────────────────────────────────────────────
//  HELPERS
// ─────────────────────────────────────────────
function calcularTotal(items) {
  return items.reduce((sum, it) => {
    const extrasTotal = (it.extras || []).reduce((s, e) => s + (e.precio || 0), 0);
    return sum + (it.precio + extrasTotal) * it.cantidad;
  }, 0);
}

function cajaActual() {
  return db.caja.find(c => c.estado === 'abierta') || null;
}

function emitDashboardStats() {
  const hoy = new Date().toISOString().split('T')[0];
  const pedidosHoy = db.pedidos.filter(p => p.createdAt.startsWith(hoy));
  const ventaHoy   = pedidosHoy.filter(p => p.estado === 'pagado').reduce((s, p) => s + p.total, 0);
  const caja       = cajaActual();
  const saldoCaja  = caja
    ? caja.movimientos.reduce((s, m) => m.tipo === 'ingreso' ? s + m.monto : s - m.monto, 0)
    : 0;

  const stats = {
    mesasOcupadas:  db.mesas.filter(m => m.estado === 'ocupada').length,
    mesasLibres:    db.mesas.filter(m => m.estado === 'libre').length,
    pedidosActivos: db.pedidos.filter(p => !['pagado','cancelado'].includes(p.estado)).length,
    deliveryActivos:db.delivery.filter(d => !['entregado','cancelado'].includes(d.estado)).length,
    ventaHoy,
    saldoCaja,
    comandasPendientes: db.comandas.filter(c => c.estado === 'pendiente').length,
    timestamp: new Date().toISOString()
  };
  io.emit('dashboard:stats', stats);
  return stats;
}

// ─────────────────────────────────────────────
//  MIDDLEWARE GLOBAL
// ─────────────────────────────────────────────
app.use(cors({ origin: '*' }));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, 'public')));

// Logger
app.use((req, _res, next) => {
  const ts = new Date().toISOString();
  console.log(`[${ts}] ${req.method} ${req.originalUrl}`);
  next();
});

// ─────────────────────────────────────────────
//  AUTH MIDDLEWARE
// ─────────────────────────────────────────────
function authMiddleware(req, res, next) {
  const header = req.headers['authorization'] || '';
  const token  = header.startsWith('Bearer ') ? header.slice(7) : null;
  if (!token) return res.status(401).json({ error: 'Token requerido' });
  try {
    req.user = jwt.verify(token, JWT_SECRET);
    next();
  } catch {
    return res.status(401).json({ error: 'Token inválido o expirado' });
  }
}

// Aplicar auth a /api/* excepto /api/auth/*
app.use('/api', (req, res, next) => {
  if (req.path.startsWith('/auth')) return next();
  authMiddleware(req, res, next);
});

// ─────────────────────────────────────────────
//  AUTH ROUTES
// ─────────────────────────────────────────────
app.post('/api/auth/login', async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) return res.status(400).json({ error: 'Email y password requeridos' });

  const user = db.users.find(u => u.email === email && u.activo);
  if (!user) return res.status(401).json({ error: 'Credenciales inválidas' });

  const ok = await bcrypt.compare(password, user.password);
  if (!ok) return res.status(401).json({ error: 'Credenciales inválidas' });

  const payload = { id: user.id, email: user.email, nombre: user.nombre, rol: user.rol };
  const token   = jwt.sign(payload, JWT_SECRET, { expiresIn: JWT_EXPIRY });

  res.json({
    token,
    usuario: { id: user.id, nombre: user.nombre, email: user.email, rol: user.rol }
  });
});

app.post('/api/auth/logout', (_req, res) => {
  // Stateless JWT — sólo confirmamos en cliente
  res.json({ message: 'Sesión cerrada correctamente' });
});

// ─────────────────────────────────────────────
//  MESAS ROUTES
// ─────────────────────────────────────────────
app.get('/api/mesas', (_req, res) => {
  res.json(db.mesas);
});

app.post('/api/mesas/:id/abrir', (req, res) => {
  const mesa = db.mesas.find(m => m.id === req.params.id);
  if (!mesa) return res.status(404).json({ error: 'Mesa no encontrada' });
  if (mesa.estado === 'ocupada') return res.status(400).json({ error: 'Mesa ya está ocupada' });

  mesa.estado  = 'ocupada';
  mesa.mozoid  = req.body.mozoid || req.user.id;
  mesa.apertura = new Date().toISOString();
  mesa.consumo = 0;
  mesa.pedidos = [];

  io.emit('mesa:update', mesa);
  emitDashboardStats();
  res.json(mesa);
});

app.post('/api/mesas/:id/cerrar', (req, res) => {
  const mesa = db.mesas.find(m => m.id === req.params.id);
  if (!mesa) return res.status(404).json({ error: 'Mesa no encontrada' });

  mesa.estado  = 'libre';
  mesa.mozoid  = null;
  mesa.apertura = null;
  mesa.consumo = 0;
  mesa.pedidos = [];

  io.emit('mesa:update', mesa);
  emitDashboardStats();
  res.json({ message: 'Mesa cerrada', mesa });
});

app.post('/api/mesas/:id/pedido', (req, res) => {
  const mesa = db.mesas.find(m => m.id === req.params.id);
  if (!mesa) return res.status(404).json({ error: 'Mesa no encontrada' });
  if (mesa.estado !== 'ocupada') return res.status(400).json({ error: 'Mesa no está ocupada' });

  const { productoId, nombre, variante, cantidad, precio, extras, observacion } = req.body;
  if (!productoId || !precio || !cantidad) return res.status(400).json({ error: 'Faltan campos requeridos' });

  const item = {
    id: uuidv4(),
    productoId,
    nombre:      nombre || '',
    variante:    variante || 'unica',
    cantidad:    parseInt(cantidad),
    precio:      parseFloat(precio),
    extras:      extras || [],
    observacion: observacion || ''
  };

  mesa.pedidos.push(item);
  mesa.consumo = calcularTotal(mesa.pedidos);

  // Generar comanda para cocina
  const comanda = {
    id:        uuidv4(),
    pedidoId:  null,
    numero:    db.comandas.length + 1,
    mesa:      mesa.numero,
    mozo:      db.users.find(u => u.id === mesa.mozoid)?.nombre || 'Desconocido',
    items:     [item],
    estado:    'pendiente',
    createdAt: new Date().toISOString()
  };
  db.comandas.push(comanda);

  io.emit('mesa:update', mesa);
  io.emit('cocina:comanda', comanda);
  emitDashboardStats();
  res.json({ mesa, item, comanda });
});

app.delete('/api/mesas/:id/pedido/:itemId', (req, res) => {
  const mesa = db.mesas.find(m => m.id === req.params.id);
  if (!mesa) return res.status(404).json({ error: 'Mesa no encontrada' });

  const idx = mesa.pedidos.findIndex(p => p.id === req.params.itemId);
  if (idx === -1) return res.status(404).json({ error: 'Ítem no encontrado' });

  mesa.pedidos.splice(idx, 1);
  mesa.consumo = calcularTotal(mesa.pedidos);

  io.emit('mesa:update', mesa);
  res.json({ message: 'Ítem eliminado', mesa });
});

app.post('/api/mesas/:id/transferir', (req, res) => {
  const origen  = db.mesas.find(m => m.id === req.params.id);
  const destino = db.mesas.find(m => m.id === req.body.destinoId);

  if (!origen)  return res.status(404).json({ error: 'Mesa origen no encontrada' });
  if (!destino) return res.status(404).json({ error: 'Mesa destino no encontrada' });
  if (destino.estado !== 'libre') return res.status(400).json({ error: 'Mesa destino no está libre' });

  destino.estado  = 'ocupada';
  destino.mozoid  = origen.mozoid;
  destino.apertura = origen.apertura;
  destino.consumo = origen.consumo;
  destino.pedidos = [...origen.pedidos];

  origen.estado  = 'libre';
  origen.mozoid  = null;
  origen.apertura = null;
  origen.consumo = 0;
  origen.pedidos = [];

  io.emit('mesa:update', origen);
  io.emit('mesa:update', destino);
  emitDashboardStats();
  res.json({ origen, destino });
});

app.post('/api/mesas/unir', (req, res) => {
  const { mesaIds } = req.body;
  if (!Array.isArray(mesaIds) || mesaIds.length < 2) return res.status(400).json({ error: 'Se necesitan al menos 2 mesas' });

  const mesas = mesaIds.map(id => db.mesas.find(m => m.id === id)).filter(Boolean);
  if (mesas.length !== mesaIds.length) return res.status(404).json({ error: 'Alguna mesa no fue encontrada' });

  const principal = mesas[0];
  for (let i = 1; i < mesas.length; i++) {
    principal.pedidos = principal.pedidos.concat(mesas[i].pedidos);
    mesas[i].estado  = 'libre';
    mesas[i].mozoid  = null;
    mesas[i].apertura = null;
    mesas[i].consumo = 0;
    mesas[i].pedidos = [];
    io.emit('mesa:update', mesas[i]);
  }
  principal.consumo = calcularTotal(principal.pedidos);

  io.emit('mesa:update', principal);
  emitDashboardStats();
  res.json({ message: 'Mesas unidas', principal, liberadas: mesas.slice(1) });
});

app.get('/api/mesas/:id/cuenta', (req, res) => {
  const mesa = db.mesas.find(m => m.id === req.params.id);
  if (!mesa) return res.status(404).json({ error: 'Mesa no encontrada' });

  const subtotal = mesa.consumo;
  const iva      = parseFloat((subtotal * 0.21).toFixed(2));
  const total    = parseFloat((subtotal + iva).toFixed(2));

  res.json({ mesa: mesa.numero, zona: mesa.zona, items: mesa.pedidos, subtotal, iva, total, apertura: mesa.apertura });
});

// ─────────────────────────────────────────────
//  PRODUCTOS ROUTES
// ─────────────────────────────────────────────
app.get('/api/productos', (_req, res) => {
  res.json(db.productos.filter(p => p.activo));
});

app.get('/api/productos/categorias', (_req, res) => {
  res.json(db.categorias.sort((a, b) => a.orden - b.orden));
});

app.post('/api/productos', (req, res) => {
  const { codigo, nombre, descripcion, categoria, precio, precioMediano, precioGrande, stock, stockMinimo, imagen, extras } = req.body;
  if (!nombre || !categoria || precio == null) return res.status(400).json({ error: 'Faltan campos requeridos' });

  const producto = {
    id: uuidv4(),
    codigo:        codigo || `PROD${String(db.productos.length + 1).padStart(3, '0')}`,
    nombre,
    descripcion:   descripcion || '',
    categoria,
    precio:        parseFloat(precio),
    precioMediano: precioMediano ? parseFloat(precioMediano) : null,
    precioGrande:  precioGrande  ? parseFloat(precioGrande)  : null,
    stock:         parseInt(stock || 0),
    stockMinimo:   parseInt(stockMinimo || 5),
    imagen:        imagen || '',
    activo:        true,
    extras:        extras || []
  };

  db.productos.push(producto);
  db.stock.push({
    productoId: producto.id,
    cantidad:   producto.stock,
    movimientos: [{ tipo: 'inicial', cantidad: producto.stock, motivo: 'Stock inicial', fecha: new Date().toISOString() }]
  });

  res.status(201).json(producto);
});

app.put('/api/productos/:id', (req, res) => {
  const idx = db.productos.findIndex(p => p.id === req.params.id);
  if (idx === -1) return res.status(404).json({ error: 'Producto no encontrado' });

  db.productos[idx] = { ...db.productos[idx], ...req.body, id: req.params.id };
  res.json(db.productos[idx]);
});

app.delete('/api/productos/:id', (req, res) => {
  const producto = db.productos.find(p => p.id === req.params.id);
  if (!producto) return res.status(404).json({ error: 'Producto no encontrado' });
  producto.activo = false;
  res.json({ message: 'Producto desactivado' });
});

// ─────────────────────────────────────────────
//  PEDIDOS ROUTES
// ─────────────────────────────────────────────
app.get('/api/pedidos', (req, res) => {
  const { estado, tipo } = req.query;
  let lista = db.pedidos;
  if (estado) lista = lista.filter(p => p.estado === estado);
  if (tipo)   lista = lista.filter(p => p.tipo   === tipo);
  res.json(lista.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)));
});

app.get('/api/pedidos/:id', (req, res) => {
  const pedido = db.pedidos.find(p => p.id === req.params.id);
  if (!pedido) return res.status(404).json({ error: 'Pedido no encontrado' });
  res.json(pedido);
});

app.post('/api/pedidos', (req, res) => {
  const { tipo, mesaId, clienteId, mozoid, items, metodoPago, observaciones } = req.body;
  if (!tipo || !items || !items.length) return res.status(400).json({ error: 'Faltan campos requeridos' });

  const total  = calcularTotal(items);
  const pedido = {
    id:           uuidv4(),
    tipo:         tipo,              // 'mesa' | 'delivery' | 'mostrador'
    mesaId:       mesaId   || null,
    clienteId:    clienteId|| null,
    mozoid:       mozoid   || req.user.id,
    estado:       'pendiente',
    items:        items.map(i => ({ id: uuidv4(), ...i })),
    total,
    metodoPago:   metodoPago   || null,
    observaciones:observaciones|| '',
    createdAt:    new Date().toISOString(),
    updatedAt:    new Date().toISOString()
  };

  db.pedidos.push(pedido);

  // Generar comanda automáticamente
  const mesa   = mesaId ? db.mesas.find(m => m.id === mesaId) : null;
  const comanda = {
    id:        uuidv4(),
    pedidoId:  pedido.id,
    numero:    db.comandas.length + 1,
    mesa:      mesa ? mesa.numero : (tipo === 'delivery' ? 'DELIVERY' : 'MOSTRADOR'),
    mozo:      db.users.find(u => u.id === pedido.mozoid)?.nombre || 'Sistema',
    items:     pedido.items,
    estado:    'pendiente',
    createdAt: new Date().toISOString()
  };
  db.comandas.push(comanda);

  io.emit('pedido:nuevo',   pedido);
  io.emit('cocina:comanda', comanda);
  emitDashboardStats();
  res.status(201).json({ pedido, comanda });
});

app.put('/api/pedidos/:id/estado', (req, res) => {
  const pedido = db.pedidos.find(p => p.id === req.params.id);
  if (!pedido) return res.status(404).json({ error: 'Pedido no encontrado' });

  pedido.estado    = req.body.estado || pedido.estado;
  pedido.updatedAt = new Date().toISOString();

  io.emit('pedido:update', pedido);
  emitDashboardStats();
  res.json(pedido);
});

app.post('/api/pedidos/:id/pagar', (req, res) => {
  const pedido = db.pedidos.find(p => p.id === req.params.id);
  if (!pedido) return res.status(404).json({ error: 'Pedido no encontrado' });
  if (pedido.estado === 'pagado') return res.status(400).json({ error: 'Pedido ya pagado' });

  const { metodoPago } = req.body;
  pedido.metodoPago = metodoPago || pedido.metodoPago || 'efectivo';
  pedido.estado     = 'pagado';
  pedido.updatedAt  = new Date().toISOString();

  // Actualizar caja
  const caja = cajaActual();
  if (caja) {
    caja.movimientos.push({
      id:       uuidv4(),
      tipo:     'ingreso',
      concepto: `Pago pedido #${pedido.id.slice(-6)}`,
      monto:    pedido.total,
      fecha:    new Date().toISOString()
    });
    io.emit('caja:update', caja);
  }

  // Liberar mesa si corresponde
  if (pedido.mesaId) {
    const mesa = db.mesas.find(m => m.id === pedido.mesaId);
    if (mesa) {
      mesa.estado  = 'libre';
      mesa.mozoid  = null;
      mesa.apertura = null;
      mesa.consumo = 0;
      mesa.pedidos = [];
      io.emit('mesa:update', mesa);
    }
  }

  // Generar factura
  const subtotal = parseFloat((pedido.total / 1.21).toFixed(2));
  const iva      = parseFloat((pedido.total - subtotal).toFixed(2));
  const factura  = {
    id:          uuidv4(),
    numero:      `F-${String(db.facturas.length + 1).padStart(6, '0')}`,
    tipo:        'B',
    pedidoId:    pedido.id,
    total:       pedido.total,
    subtotal,
    iva,
    metodoPago:  pedido.metodoPago,
    createdAt:   new Date().toISOString()
  };
  db.facturas.push(factura);

  io.emit('pedido:update', pedido);
  emitDashboardStats();
  res.json({ pedido, factura });
});

// ─────────────────────────────────────────────
//  DELIVERY ROUTES
// ─────────────────────────────────────────────
app.get('/api/delivery', (_req, res) => {
  res.json(db.delivery.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)));
});

app.get('/api/delivery/activos', (_req, res) => {
  res.json(db.delivery.filter(d => !['entregado', 'cancelado'].includes(d.estado)));
});

app.post('/api/delivery', (req, res) => {
  const { pedidoId, clienteNombre, clienteTelefono, direccion, barrio, referencia, latitud, longitud, repartidorId, estimacion } = req.body;
  if (!clienteNombre || !direccion) return res.status(400).json({ error: 'Faltan campos requeridos' });

  const envio = {
    id:              uuidv4(),
    pedidoId:        pedidoId || null,
    clienteNombre,
    clienteTelefono: clienteTelefono || '',
    direccion,
    barrio:          barrio     || '',
    referencia:      referencia || '',
    latitud:         latitud    || null,
    longitud:        longitud   || null,
    repartidorId:    repartidorId || null,
    estado:          'pendiente',
    distancia:       null,
    estimacion:      estimacion || 30,
    createdAt:       new Date().toISOString()
  };

  db.delivery.push(envio);
  io.emit('delivery:update', envio);
  emitDashboardStats();
  res.status(201).json(envio);
});

app.put('/api/delivery/:id/estado', (req, res) => {
  const envio = db.delivery.find(d => d.id === req.params.id);
  if (!envio) return res.status(404).json({ error: 'Envío no encontrado' });

  envio.estado = req.body.estado || envio.estado;
  if (req.body.repartidorId) envio.repartidorId = req.body.repartidorId;

  io.emit('delivery:update', envio);
  emitDashboardStats();
  res.json(envio);
});

// ─────────────────────────────────────────────
//  COCINA ROUTES
// ─────────────────────────────────────────────
app.get('/api/cocina/comandas', (req, res) => {
  const { estado } = req.query;
  let lista = db.comandas;
  if (estado) lista = lista.filter(c => c.estado === estado);
  res.json(lista.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt)));
});

app.put('/api/cocina/comandas/:id/estado', (req, res) => {
  const comanda = db.comandas.find(c => c.id === req.params.id);
  if (!comanda) return res.status(404).json({ error: 'Comanda no encontrada' });

  comanda.estado = req.body.estado || comanda.estado;

  // Si la comanda es de un pedido, actualizar el pedido también
  if (comanda.pedidoId) {
    const pedido = db.pedidos.find(p => p.id === comanda.pedidoId);
    if (pedido && comanda.estado === 'lista') {
      pedido.estado    = 'listo';
      pedido.updatedAt = new Date().toISOString();
      io.emit('pedido:update', pedido);
    }
  }

  io.emit('cocina:update', comanda);
  emitDashboardStats();
  res.json(comanda);
});

// ─────────────────────────────────────────────
//  CAJA ROUTES
// ─────────────────────────────────────────────
app.get('/api/caja/actual', (_req, res) => {
  const caja = cajaActual();
  if (!caja) return res.status(404).json({ error: 'No hay caja abierta' });

  const saldo = caja.movimientos.reduce((s, m) => m.tipo === 'ingreso' ? s + m.monto : s - m.monto, 0);
  res.json({ ...caja, saldoActual: saldo });
});

app.post('/api/caja/abrir', (req, res) => {
  if (cajaActual()) return res.status(400).json({ error: 'Ya hay una caja abierta' });

  const { saldoInicial } = req.body;
  const caja = {
    id:           uuidv4(),
    fecha:        new Date().toISOString().split('T')[0],
    apertura:     new Date().toISOString(),
    cierre:       null,
    saldoInicial: parseFloat(saldoInicial || 0),
    saldoFinal:   null,
    cajeroId:     req.user.id,
    movimientos:  [{
      id:       uuidv4(),
      tipo:     'ingreso',
      concepto: 'Apertura de caja',
      monto:    parseFloat(saldoInicial || 0),
      fecha:    new Date().toISOString()
    }],
    estado: 'abierta'
  };

  db.caja.push(caja);
  io.emit('caja:update', caja);
  res.status(201).json(caja);
});

app.post('/api/caja/cerrar', (req, res) => {
  const caja = cajaActual();
  if (!caja) return res.status(404).json({ error: 'No hay caja abierta' });

  const saldoFinal = caja.movimientos.reduce((s, m) => m.tipo === 'ingreso' ? s + m.monto : s - m.monto, 0);
  caja.cierre     = new Date().toISOString();
  caja.saldoFinal = saldoFinal;
  caja.estado     = 'cerrada';

  io.emit('caja:update', caja);
  res.json(caja);
});

app.post('/api/caja/movimiento', (req, res) => {
  const caja = cajaActual();
  if (!caja) return res.status(404).json({ error: 'No hay caja abierta' });

  const { tipo, concepto, monto } = req.body;
  if (!tipo || !monto) return res.status(400).json({ error: 'Faltan campos requeridos' });

  const movimiento = {
    id:       uuidv4(),
    tipo:     tipo,         // 'ingreso' | 'egreso'
    concepto: concepto || '',
    monto:    parseFloat(monto),
    fecha:    new Date().toISOString()
  };

  caja.movimientos.push(movimiento);
  io.emit('caja:update', caja);
  res.json(movimiento);
});

app.get('/api/caja/resumen', (_req, res) => {
  const hoy  = new Date().toISOString().split('T')[0];
  const cajas = db.caja.filter(c => c.fecha === hoy);

  const ventasEfectivo = db.pedidos
    .filter(p => p.estado === 'pagado' && p.metodoPago === 'efectivo' && p.updatedAt.startsWith(hoy))
    .reduce((s, p) => s + p.total, 0);

  const ventasTarjeta = db.pedidos
    .filter(p => p.estado === 'pagado' && p.metodoPago === 'tarjeta' && p.updatedAt.startsWith(hoy))
    .reduce((s, p) => s + p.total, 0);

  const totalVentas = ventasEfectivo + ventasTarjeta;

  res.json({ fecha: hoy, cajas, ventasEfectivo, ventasTarjeta, totalVentas });
});

// ─────────────────────────────────────────────
//  CLIENTES ROUTES
// ─────────────────────────────────────────────
app.get('/api/clientes', (_req, res) => {
  res.json(db.clientes);
});

app.post('/api/clientes', (req, res) => {
  const { nombre, email, telefono, direcciones } = req.body;
  if (!nombre) return res.status(400).json({ error: 'El nombre es requerido' });

  const cliente = {
    id:          uuidv4(),
    nombre,
    email:       email     || '',
    telefono:    telefono  || '',
    direcciones: (direcciones || []).map(d => ({ id: uuidv4(), ...d })),
    historial:   [],
    createdAt:   new Date().toISOString()
  };

  db.clientes.push(cliente);
  res.status(201).json(cliente);
});

app.get('/api/clientes/:id', (req, res) => {
  const cliente = db.clientes.find(c => c.id === req.params.id);
  if (!cliente) return res.status(404).json({ error: 'Cliente no encontrado' });

  const historial = db.pedidos.filter(p => p.clienteId === cliente.id);
  res.json({ ...cliente, historial });
});

app.put('/api/clientes/:id', (req, res) => {
  const idx = db.clientes.findIndex(c => c.id === req.params.id);
  if (idx === -1) return res.status(404).json({ error: 'Cliente no encontrado' });

  db.clientes[idx] = { ...db.clientes[idx], ...req.body, id: req.params.id };
  res.json(db.clientes[idx]);
});

// ─────────────────────────────────────────────
//  REPORTES ROUTES
// ─────────────────────────────────────────────
function pedidosPorPeriodo(periodo) {
  const ahora = new Date();
  return db.pedidos.filter(p => {
    if (p.estado !== 'pagado') return false;
    const fecha = new Date(p.updatedAt);
    if (periodo === 'hoy') {
      return fecha.toDateString() === ahora.toDateString();
    } else if (periodo === 'semana') {
      const hace7 = new Date(ahora); hace7.setDate(ahora.getDate() - 7);
      return fecha >= hace7;
    } else if (periodo === 'mes') {
      return fecha.getMonth() === ahora.getMonth() && fecha.getFullYear() === ahora.getFullYear();
    }
    return true;
  });
}

app.get('/api/reportes/ventas', (req, res) => {
  const periodo  = req.query.periodo || 'hoy';
  const pedidos  = pedidosPorPeriodo(periodo);
  const total    = pedidos.reduce((s, p) => s + p.total, 0);
  const cantidad = pedidos.length;

  const porMetodo = pedidos.reduce((acc, p) => {
    const m = p.metodoPago || 'otros';
    acc[m] = (acc[m] || 0) + p.total;
    return acc;
  }, {});

  res.json({ periodo, total, cantidad, porMetodo, pedidos });
});

app.get('/api/reportes/productos-mas-vendidos', (req, res) => {
  const periodo = req.query.periodo || 'hoy';
  const pedidos = pedidosPorPeriodo(periodo);

  const conteo = {};
  pedidos.forEach(p => {
    (p.items || []).forEach(item => {
      const k = item.nombre || item.productoId;
      if (!conteo[k]) conteo[k] = { nombre: item.nombre, productoId: item.productoId, cantidad: 0, total: 0 };
      conteo[k].cantidad += item.cantidad;
      conteo[k].total    += item.precio * item.cantidad;
    });
  });

  const ranking = Object.values(conteo).sort((a, b) => b.cantidad - a.cantidad).slice(0, 10);
  res.json(ranking);
});

app.get('/api/reportes/dashboard', (_req, res) => {
  const stats = emitDashboardStats();
  res.json(stats);
});

// ─────────────────────────────────────────────
//  FACTURAS ROUTES
// ─────────────────────────────────────────────
app.post('/api/facturas', (req, res) => {
  const { pedidoId, tipo, metodoPago } = req.body;
  const pedido = pedidoId ? db.pedidos.find(p => p.id === pedidoId) : null;

  const total    = pedido ? pedido.total : (parseFloat(req.body.total) || 0);
  const subtotal = parseFloat((total / 1.21).toFixed(2));
  const iva      = parseFloat((total - subtotal).toFixed(2));

  const factura = {
    id:         uuidv4(),
    numero:     `F-${String(db.facturas.length + 1).padStart(6, '0')}`,
    tipo:       tipo      || 'B',
    pedidoId:   pedidoId  || null,
    total,
    subtotal,
    iva,
    metodoPago: metodoPago || (pedido ? pedido.metodoPago : 'efectivo'),
    createdAt:  new Date().toISOString()
  };

  db.facturas.push(factura);
  res.status(201).json(factura);
});

app.get('/api/facturas/:id', (req, res) => {
  const factura = db.facturas.find(f => f.id === req.params.id);
  if (!factura) return res.status(404).json({ error: 'Factura no encontrada' });

  const pedido = factura.pedidoId ? db.pedidos.find(p => p.id === factura.pedidoId) : null;
  res.json({ ...factura, pedido });
});

// ─────────────────────────────────────────────
//  WEBSOCKET
// ─────────────────────────────────────────────
io.on('connection', (socket) => {
  console.log(`[WS] Cliente conectado: ${socket.id}`);

  socket.on('join:room', (room) => {
    socket.join(room);
    console.log(`[WS] ${socket.id} se unió a sala: ${room}`);

    // Enviar estado actual según la sala
    if (room === 'cocina') {
      socket.emit('cocina:init', db.comandas.filter(c => c.estado !== 'entregada'));
    } else if (room === 'dashboard') {
      socket.emit('dashboard:stats', emitDashboardStats());
    } else if (room === 'delivery') {
      socket.emit('delivery:init', db.delivery.filter(d => !['entregado', 'cancelado'].includes(d.estado)));
    }
  });

  socket.on('disconnect', () => {
    console.log(`[WS] Cliente desconectado: ${socket.id}`);
  });
});

// ─────────────────────────────────────────────
//  DASHBOARD STATS – broadcast cada 10s
// ─────────────────────────────────────────────
setInterval(() => {
  if (io.engine.clientsCount > 0) {
    emitDashboardStats();
  }
}, 10000);

// ─────────────────────────────────────────────
//  CATCH-ALL – SPA fallback
// ─────────────────────────────────────────────
app.get('*', (_req, res) => {
  const indexPath = path.join(__dirname, 'public', 'index.html');
  res.sendFile(indexPath, (err) => {
    if (err) res.status(404).json({ error: 'Not found' });
  });
});

// ─────────────────────────────────────────────
//  ERROR HANDLER
// ─────────────────────────────────────────────
// eslint-disable-next-line no-unused-vars
app.use((err, _req, res, _next) => {
  console.error('[ERROR]', err.message);
  res.status(500).json({ error: 'Error interno del servidor', detalle: err.message });
});

// ─────────────────────────────────────────────
//  START SERVER
// ─────────────────────────────────────────────
server.listen(PORT, '0.0.0.0', () => {
  console.log(`\n🍕 PIZZERÍA PRO – Backend corriendo en http://0.0.0.0:${PORT}`);
  console.log(`   JWT Secret: ${JWT_SECRET}`);
  console.log(`   Tokens expiran en: ${JWT_EXPIRY}\n`);
});

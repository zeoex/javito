const express = require('express');
const sql = require('mssql');
const path = require('path');

const app = express();
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

const dbConfig = {
  user: 'sa',
  password: 'Binaria676',
  server: 'localhost',
  database: 'javier',
  options: {
    encrypt: false,
    trustServerCertificate: true,
  }
};

let pool;

async function getPool() {
  if (!pool) {
    pool = await sql.connect(dbConfig);
  }
  return pool;
}

app.get('/api/articulo', async (req, res) => {
  const codigo = req.query.codigo;
  try {
    const db = await getPool();
    const result = await db.request()
      .input('codigo', sql.VarChar, codigo)
      .query(`
        SELECT
          ARTICULO, DESCRIPCION, DESCRIPCIONEXTRA1, DESCRIPCIONEXTRA2,
          PRECIO, PRECIO_DE_COSTO, PRECIO_LISTA1, PRECIO_LISTA2, PRECIO_LISTA3,
          PRECIOOFERTA, UTILIZAPRECIOOFERTA, PORCENTAJEOFERTA,
          TIPOARTICULO, UNIDAD, UNIDADPROVEEDOR, CANTPRESENTACION,
          STOCK_ACTUAL, PUNTO_PEDIDO, TOPE_REPOSIC,
          MARCA, RUBRO, SUB_RUBRO, PLU,
          TASA_IVA, IMPUESTO_INT, BONIFICACION,
          ESTADO, FECHAALTA, FECHAACTPRECIOS, FECHAVIGENCIA,
          COSTOPROVEEDOR, ULTIMO_PROVEEDOR, FABRICACION, ESTANTERIA, GAVETA
        FROM fact0007
        WHERE ARTICULO = @codigo
      `);
    if (result.recordset.length === 0) return res.status(404).json({ error: 'No encontrado' });
    res.json(result.recordset[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/filtros', async (req, res) => {
  try {
    const db = await getPool();
    const [tipos, marcas, estados] = await Promise.all([
      db.request().query(`SELECT DISTINCT TIPOARTICULO AS v FROM fact0007 WHERE TIPOARTICULO IS NOT NULL AND TIPOARTICULO != '' ORDER BY TIPOARTICULO`),
      db.request().query(`SELECT DISTINCT MARCA AS v FROM fact0007 WHERE MARCA IS NOT NULL AND MARCA != '' ORDER BY MARCA`),
      db.request().query(`SELECT DISTINCT ESTADO AS v FROM fact0007 WHERE ESTADO IS NOT NULL AND ESTADO != '' ORDER BY ESTADO`),
    ]);
    res.json({
      tipos:   tipos.recordset.map(r => r.v),
      marcas:  marcas.recordset.map(r => r.v),
      estados: estados.recordset.map(r => r.v),
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/buscar', async (req, res) => {
  const { q = '', pagina = 1, tipo = '', marca = '', estado = '' } = req.query;
  const porPagina = 50;
  const offset = (parseInt(pagina) - 1) * porPagina;
  const busqueda = `%${q}%`;

  let where = `WHERE (ARTICULO LIKE @busqueda OR DESCRIPCION LIKE @busqueda)`;
  if (tipo)   where += ` AND TIPOARTICULO = @tipo`;
  if (marca)  where += ` AND MARCA = @marca`;
  if (estado) where += ` AND ESTADO = @estado`;

  try {
    const db = await getPool();

    const buildReq = () => {
      const r = db.request()
        .input('busqueda', sql.VarChar, busqueda)
        .input('offset',   sql.Int, offset)
        .input('porPagina',sql.Int, porPagina);
      if (tipo)   r.input('tipo',   sql.VarChar, tipo);
      if (marca)  r.input('marca',  sql.VarChar, marca);
      if (estado) r.input('estado', sql.VarChar, estado);
      return r;
    };

    const [result, countResult] = await Promise.all([
      buildReq().query(`
        SELECT ARTICULO, DESCRIPCION, PRECIO, TIPOARTICULO,
               UNIDAD, STOCK_ACTUAL, ESTADO, MARCA
        FROM fact0007
        ${where}
        ORDER BY ARTICULO
        OFFSET @offset ROWS FETCH NEXT @porPagina ROWS ONLY
      `),
      buildReq().query(`
        SELECT COUNT(*) AS total FROM fact0007 ${where}
      `)
    ]);

    res.json({
      datos: result.recordset,
      total: countResult.recordset[0].total,
      pagina: parseInt(pagina),
      porPagina
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

const PORT = 3000;
app.listen(PORT, () => {
  console.log(`Servidor corriendo en http://localhost:${PORT}`);
});

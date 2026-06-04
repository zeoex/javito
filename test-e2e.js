/**
 * E2E Test: Comanda flow and delivery sync
 * Target: https://restito-production.up.railway.app
 */

const { chromium } = require('playwright');
const https = require('https');
const http = require('http');
const fs = require('fs');
const path = require('path');

const BASE_URL = 'https://restito-production.up.railway.app';
const SCREENSHOTS_DIR = '/home/user/javito/e2e-screenshots';

// Ensure screenshots dir exists
if (!fs.existsSync(SCREENSHOTS_DIR)) {
  fs.mkdirSync(SCREENSHOTS_DIR, { recursive: true });
}

function log(msg) {
  console.log(`[${new Date().toISOString()}] ${msg}`);
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function apiRequest(method, urlPath, body, token) {
  return new Promise((resolve, reject) => {
    const url = new URL(BASE_URL + urlPath);
    const options = {
      hostname: url.hostname,
      port: url.port || 443,
      path: url.pathname + url.search,
      method: method,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      },
      rejectUnauthorized: false
    };

    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', chunk => { data += chunk; });
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, body: JSON.parse(data), raw: data });
        } catch (e) {
          resolve({ status: res.statusCode, body: null, raw: data });
        }
      });
    });

    req.on('error', reject);

    if (body) {
      req.write(JSON.stringify(body));
    }
    req.end();
  });
}

async function main() {
  log('=== Starting E2E Test ===');

  const browser = await chromium.launch({
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--ignore-certificate-errors']
  });

  // ─── Create 3 contexts ────────────────────────────────────────────────────
  const ctxAdmin = await browser.newContext({ ignoreHTTPSErrors: true });
  const ctxMozo  = await browser.newContext({ ignoreHTTPSErrors: true });
  const ctxCocina = await browser.newContext({ ignoreHTTPSErrors: true });

  const adminPage  = await ctxAdmin.newPage();
  const mozoPage   = await ctxMozo.newPage();
  const cocinaPage = await ctxCocina.newPage();

  // ─── Step 1: Open all 3 tabs ──────────────────────────────────────────────
  log('Step 1: Opening admin, mozo, cocina tabs...');
  await Promise.all([
    adminPage.goto(BASE_URL, { waitUntil: 'domcontentloaded', timeout: 30000 }),
    mozoPage.goto(BASE_URL, { waitUntil: 'domcontentloaded', timeout: 30000 }),
    cocinaPage.goto(BASE_URL, { waitUntil: 'domcontentloaded', timeout: 30000 }),
  ]);
  log('All tabs opened');

  await sleep(2000);

  // Screenshot initial state
  await adminPage.screenshot({ path: `${SCREENSHOTS_DIR}/01-admin-initial.png`, fullPage: true });
  await mozoPage.screenshot({ path: `${SCREENSHOTS_DIR}/01-mozo-initial.png`, fullPage: true });
  await cocinaPage.screenshot({ path: `${SCREENSHOTS_DIR}/01-cocina-initial.png`, fullPage: true });
  log(`Screenshots saved to ${SCREENSHOTS_DIR}/`);

  // ─── Step 2: Log in mozo as carlos@restito.com / mozo123 ─────────────────
  log('Step 2: Logging in as mozo (carlos@restito.com)...');
  try {
    // Look for login form on mozo page
    const mozoLoginPage = mozoPage;

    // Try to find email/password fields
    const emailInput = await mozoPage.$('input[type="email"], input[name="email"], input[placeholder*="email" i], input[placeholder*="correo" i]');
    const passInput  = await mozoPage.$('input[type="password"]');

    if (emailInput && passInput) {
      await emailInput.fill('carlos@restito.com');
      await passInput.fill('mozo123');
      await mozoPage.screenshot({ path: `${SCREENSHOTS_DIR}/02-mozo-login-filled.png`, fullPage: true });

      // Click login button
      const loginBtn = await mozoPage.$('button[type="submit"], button:has-text("Ingresar"), button:has-text("Login"), button:has-text("Entrar"), button:has-text("Acceder")');
      if (loginBtn) {
        await loginBtn.click();
        await sleep(2000);
        log('Mozo login submitted');
      } else {
        // Try pressing Enter
        await passInput.press('Enter');
        await sleep(2000);
        log('Mozo login submitted via Enter');
      }
    } else {
      log('WARN: No login form found on mozo page — checking page content...');
      const bodyText = await mozoPage.textContent('body');
      log(`Mozo page body (first 500 chars): ${bodyText.substring(0, 500)}`);
    }
  } catch (err) {
    log(`ERROR logging in mozo: ${err.message}`);
  }

  await mozoPage.screenshot({ path: `${SCREENSHOTS_DIR}/02-mozo-after-login.png`, fullPage: true });

  // ─── Step 3: Log in admin as admin@restito.com / admin123 ────────────────
  log('Step 3: Logging in as admin (admin@restito.com)...');
  try {
    const adminEmailInput = await adminPage.$('input[type="email"], input[name="email"], input[placeholder*="email" i], input[placeholder*="correo" i]');
    const adminPassInput  = await adminPage.$('input[type="password"]');

    if (adminEmailInput && adminPassInput) {
      await adminEmailInput.fill('admin@restito.com');
      await adminPassInput.fill('admin123');

      const loginBtn = await adminPage.$('button[type="submit"], button:has-text("Ingresar"), button:has-text("Login"), button:has-text("Entrar"), button:has-text("Acceder")');
      if (loginBtn) {
        await loginBtn.click();
        await sleep(2000);
        log('Admin login submitted');
      } else {
        await adminPassInput.press('Enter');
        await sleep(2000);
        log('Admin login submitted via Enter');
      }
    } else {
      log('WARN: No login form found on admin page');
      const bodyText = await adminPage.textContent('body');
      log(`Admin page body (first 500 chars): ${bodyText.substring(0, 500)}`);
    }
  } catch (err) {
    log(`ERROR logging in admin: ${err.message}`);
  }

  await adminPage.screenshot({ path: `${SCREENSHOTS_DIR}/03-admin-after-login.png`, fullPage: true });

  // Navigate cocina to cocina view
  log('Navigating cocina tab to cocina view...');
  try {
    // Try clicking a cocina/kitchen link or navigating to cocina route
    const cocinaLink = await cocinaPage.$('a[href*="cocina"], button:has-text("Cocina"), [data-view="cocina"], #nav-cocina');
    if (cocinaLink) {
      await cocinaLink.click();
      await sleep(2000);
      log('Clicked cocina link');
    } else {
      // Try navigating directly
      await cocinaPage.goto(`${BASE_URL}/#cocina`, { waitUntil: 'domcontentloaded', timeout: 15000 });
      await sleep(2000);
      log('Navigated to /#cocina');
    }
  } catch (err) {
    log(`WARN cocina nav: ${err.message}`);
  }
  await cocinaPage.screenshot({ path: `${SCREENSHOTS_DIR}/03-cocina-view.png`, fullPage: true });

  // ─── Step 4: In mozo — open a mesa (any available) ───────────────────────
  log('Step 4: In mozo — opening a mesa...');
  await sleep(1000);

  try {
    // Look for mesa cards/buttons
    const mesaSelectors = [
      '.mesa-card', '.mesa', '[class*="mesa"]',
      '.table-card', '.table-btn',
      'button:has-text("Mesa")', 'button:has-text("Mesa 1")',
      '[data-mesa]', '.floor-plan button'
    ];

    let mesaClicked = false;
    for (const sel of mesaSelectors) {
      const elements = await mozoPage.$$(sel);
      if (elements.length > 0) {
        log(`Found ${elements.length} elements with selector: ${sel}`);
        // Click the first available mesa
        for (const el of elements) {
          try {
            const isVisible = await el.isVisible();
            if (isVisible) {
              await el.click();
              log(`Clicked mesa element with selector: ${sel}`);
              mesaClicked = true;
              await sleep(1500);
              break;
            }
          } catch (e) { /* continue */ }
        }
        if (mesaClicked) break;
      }
    }

    if (!mesaClicked) {
      log('WARN: No mesa found — checking page structure...');
      const pageContent = await mozoPage.evaluate(() => document.body.innerHTML.substring(0, 2000));
      log(`Mozo page HTML (first 2000): ${pageContent}`);
    }
  } catch (err) {
    log(`ERROR opening mesa: ${err.message}`);
  }

  await mozoPage.screenshot({ path: `${SCREENSHOTS_DIR}/04-mozo-mesa-selected.png`, fullPage: true });

  // ─── Step 4b: Add mesa as abierta ────────────────────────────────────────
  log('Step 4b: Adding mesa as abierta...');
  try {
    const abrirBtn = await mozoPage.$('button:has-text("Abrir"), button:has-text("Abrir Mesa"), button:has-text("Nueva Comanda"), [class*="abrir"]');
    if (abrirBtn) {
      await abrirBtn.click();
      await sleep(1500);
      log('Clicked abrir/nueva comanda button');
    } else {
      log('WARN: No abrir button found');
    }
  } catch (err) {
    log(`WARN abrir mesa: ${err.message}`);
  }

  await mozoPage.screenshot({ path: `${SCREENSHOTS_DIR}/04b-mozo-mesa-abierta.png`, fullPage: true });

  // ─── Step 5: In mozo — add a product to the order ────────────────────────
  log('Step 5: In mozo — adding a product to the order...');
  try {
    const productSelectors = [
      '.menu-item', '.producto-card', '.product-card',
      '[class*="producto"]', '[class*="product"]',
      '.item-menu', '.menu-card',
      'button[data-producto]', 'button[data-product]',
      '.categoria-producto button', '.menu button'
    ];

    let productClicked = false;
    for (const sel of productSelectors) {
      const elements = await mozoPage.$$(sel);
      if (elements.length > 0) {
        log(`Found ${elements.length} product elements with selector: ${sel}`);
        for (const el of elements) {
          try {
            const isVisible = await el.isVisible();
            if (isVisible) {
              await el.click();
              log(`Clicked product with selector: ${sel}`);
              productClicked = true;
              await sleep(1500);
              break;
            }
          } catch (e) { /* continue */ }
        }
        if (productClicked) break;
      }
    }

    if (!productClicked) {
      log('WARN: No product found — dumping current page structure...');
      const pageContent = await mozoPage.evaluate(() => {
        const els = document.querySelectorAll('button, .card, [class*="item"], [class*="product"]');
        return Array.from(els).slice(0, 20).map(el => ({
          tag: el.tagName,
          class: el.className,
          text: el.textContent.trim().substring(0, 50)
        }));
      });
      log(`Mozo elements: ${JSON.stringify(pageContent, null, 2)}`);
    }
  } catch (err) {
    log(`ERROR adding product: ${err.message}`);
  }

  await mozoPage.screenshot({ path: `${SCREENSHOTS_DIR}/05-mozo-product-added.png`, fullPage: true });

  // ─── Step 6: Click "Imprimir Comanda" if visible ─────────────────────────
  log('Step 6: Looking for "Imprimir Comanda" button...');
  try {
    const comandaBtn = await mozoPage.$('button:has-text("Imprimir Comanda"), button:has-text("Enviar Comanda"), button:has-text("Comanda"), button:has-text("Confirmar"), [class*="imprimir"], [class*="comanda"]');
    if (comandaBtn) {
      const isVisible = await comandaBtn.isVisible();
      if (isVisible) {
        log('Found and clicking "Imprimir Comanda" button');
        await comandaBtn.click();
        await sleep(2000);
      } else {
        log('Imprimir Comanda button found but not visible');
      }
    } else {
      log('No "Imprimir Comanda" button found');

      // Check all buttons
      const allBtns = await mozoPage.evaluate(() => {
        return Array.from(document.querySelectorAll('button')).map(b => ({
          text: b.textContent.trim().substring(0, 60),
          class: b.className,
          visible: b.offsetParent !== null
        }));
      });
      log(`All mozo buttons: ${JSON.stringify(allBtns, null, 2)}`);
    }
  } catch (err) {
    log(`ERROR with imprimir comanda: ${err.message}`);
  }

  await mozoPage.screenshot({ path: `${SCREENSHOTS_DIR}/06-mozo-after-comanda.png`, fullPage: true });

  // ─── Step 7: Wait 3s, screenshot cocina ──────────────────────────────────
  log('Step 7: Waiting 3 seconds then checking cocina...');
  await sleep(3000);
  await cocinaPage.screenshot({ path: `${SCREENSHOTS_DIR}/07-cocina-after-comanda.png`, fullPage: true });

  // Check for comanda cards
  try {
    const comandaCards = await cocinaPage.$$('.comanda, .comanda.new-arrival, [class*="comanda"]');
    log(`Cocina comanda cards found: ${comandaCards.length}`);

    const enVivo = await cocinaPage.$('text=EN VIVO, [class*="en-vivo"], [class*="live"]');
    log(`Cocina "EN VIVO" indicator: ${enVivo ? 'FOUND' : 'not found'}`);

    // Get cocina page text
    const cocinaText = await cocinaPage.evaluate(() => document.body.innerText.substring(0, 1000));
    log(`Cocina page text: ${cocinaText}`);
  } catch (err) {
    log(`ERROR checking cocina: ${err.message}`);
  }

  // ─── Step 8: Admin — open delivery section ────────────────────────────────
  log('Step 8: Admin — opening delivery section...');
  try {
    const deliverySelectors = [
      '#sec-delivery', '[data-section="delivery"]',
      'button:has-text("Delivery")', 'a:has-text("Delivery")',
      '.nav-delivery', '[href*="delivery"]',
      'li:has-text("Delivery")'
    ];

    let deliveryClicked = false;
    for (const sel of deliverySelectors) {
      const el = await adminPage.$(sel);
      if (el) {
        const isVisible = await el.isVisible();
        if (isVisible) {
          await el.click();
          log(`Clicked delivery section with: ${sel}`);
          deliveryClicked = true;
          await sleep(2000);
          break;
        }
      }
    }

    if (!deliveryClicked) {
      log('WARN: No delivery section found — checking admin nav...');
      const navItems = await adminPage.evaluate(() => {
        return Array.from(document.querySelectorAll('nav a, nav button, .sidebar a, .sidebar button, .menu a, .menu button')).map(el => ({
          tag: el.tagName,
          text: el.textContent.trim().substring(0, 50),
          href: el.href || '',
          class: el.className
        }));
      });
      log(`Admin nav items: ${JSON.stringify(navItems, null, 2)}`);
    }
  } catch (err) {
    log(`ERROR opening delivery section: ${err.message}`);
  }

  await adminPage.screenshot({ path: `${SCREENSHOTS_DIR}/08-admin-delivery.png`, fullPage: true });

  // ─── Step 9: Direct API calls ─────────────────────────────────────────────
  log('Step 9: Making direct API calls...');

  // 9a: Get admin token via POST /api/auth/login
  let adminToken = null;
  try {
    log('POST /api/auth/login (admin)...');
    const loginRes = await apiRequest('POST', '/api/auth/login', { email: 'admin@restito.com', password: 'admin123' });
    log(`Login response status: ${loginRes.status}`);
    log(`Login response body: ${JSON.stringify(loginRes.body, null, 2)}`);

    if (loginRes.body && loginRes.body.token) {
      adminToken = loginRes.body.token;
      log(`Admin token obtained: ${adminToken.substring(0, 50)}...`);
    } else if (loginRes.body && loginRes.body.accessToken) {
      adminToken = loginRes.body.accessToken;
      log(`Admin token (accessToken): ${adminToken.substring(0, 50)}...`);
    } else {
      log(`WARN: No token in login response. Full response: ${loginRes.raw}`);
    }
  } catch (err) {
    log(`ERROR getting admin token: ${err.message}`);
  }

  // 9b: GET /api/delivery/activos
  try {
    log('GET /api/delivery/activos...');
    const deliveryRes = await apiRequest('GET', '/api/delivery/activos', null, adminToken);
    log(`Delivery activos status: ${deliveryRes.status}`);
    log(`Delivery activos body: ${JSON.stringify(deliveryRes.body, null, 2)}`);
  } catch (err) {
    log(`ERROR GET /api/delivery/activos: ${err.message}`);
  }

  // 9c: GET /api/state
  try {
    log('GET /api/state...');
    const stateRes = await apiRequest('GET', '/api/state', null, adminToken);
    log(`State status: ${stateRes.status}`);
    // State might be large, summarize
    if (stateRes.body) {
      const keys = Object.keys(stateRes.body);
      log(`State keys: ${JSON.stringify(keys)}`);
      // Log summary of state
      if (stateRes.body.mesas) {
        log(`Mesas count: ${Array.isArray(stateRes.body.mesas) ? stateRes.body.mesas.length : Object.keys(stateRes.body.mesas).length}`);
      }
      if (stateRes.body.comandas) {
        log(`Comandas count: ${Array.isArray(stateRes.body.comandas) ? stateRes.body.comandas.length : Object.keys(stateRes.body.comandas).length}`);
      }
      if (stateRes.body.delivery) {
        log(`Delivery orders: ${JSON.stringify(stateRes.body.delivery)}`);
      }
      log(`Full state: ${JSON.stringify(stateRes.body, null, 2).substring(0, 3000)}`);
    } else {
      log(`State raw: ${stateRes.raw.substring(0, 1000)}`);
    }
  } catch (err) {
    log(`ERROR GET /api/state: ${err.message}`);
  }

  // Also try mozo login to get mozo token
  try {
    log('POST /api/auth/login (mozo)...');
    const mozoLoginRes = await apiRequest('POST', '/api/auth/login', { email: 'carlos@restito.com', password: 'mozo123' });
    log(`Mozo login status: ${mozoLoginRes.status}`);
    log(`Mozo login body: ${JSON.stringify(mozoLoginRes.body, null, 2)}`);
  } catch (err) {
    log(`ERROR mozo API login: ${err.message}`);
  }

  // ─── Final screenshots ────────────────────────────────────────────────────
  log('Taking final screenshots of all tabs...');
  await adminPage.screenshot({ path: `${SCREENSHOTS_DIR}/09-admin-final.png`, fullPage: true });
  await mozoPage.screenshot({ path: `${SCREENSHOTS_DIR}/09-mozo-final.png`, fullPage: true });
  await cocinaPage.screenshot({ path: `${SCREENSHOTS_DIR}/09-cocina-final.png`, fullPage: true });

  // ─── List all screenshots ─────────────────────────────────────────────────
  const screenshots = fs.readdirSync(SCREENSHOTS_DIR).filter(f => f.endsWith('.png'));
  log('\n=== SCREENSHOTS TAKEN ===');
  screenshots.forEach(f => log(`  ${SCREENSHOTS_DIR}/${f}`));

  await browser.close();
  log('\n=== E2E Test Complete ===');
}

main().catch(err => {
  console.error('FATAL ERROR:', err);
  process.exit(1);
});

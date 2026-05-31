# Pizzería Pro — Contexto del Proyecto

## Railway

- **Token API**: `55ea6497-1857-4e12-a4ab-a31565de4d0c`
- **Workspace ID**: `8cfdea71-3014-4919-a9f2-ea178c15b881`
- **Proyecto**: `javito-portal` (ID: `ea348246-a9fb-4251-89a3-1f469cb26fbc`)
- **Servicio app**: `pizzeria-pro` (ID: `7d9be2a3-4f7c-4fdd-b93c-21f6d6796aa5`)
- **Servicio DB**: `Postgres` (ID: `1c3c173c-a230-4e8e-8f5a-f03bb96af1ff`)
- **Environment**: `production` (ID: `3b68f44b-d69f-4933-b8c1-063b68d23b3f`)

Para llamar a la API de Railway usar:
```bash
curl -s -X POST https://backboard.railway.app/graphql/v2 \
  -H "Authorization: Bearer 55ea6497-1857-4e12-a4ab-a31565de4d0c" \
  -H "Content-Type: application/json" \
  -d '{"query":"..."}'
```

## Git

Siempre pushear a los 3 branches:
```bash
git push origin master-sync
git push origin master-sync:master
git push origin master-sync:claude/portal-replica-multiagent-H53V3
```

## Arquitectura

- **Backend**: `app.js` — Express + Socket.io + PostgreSQL (`pg`)
- **Frontend**: `public/index.html` — SPA single file
- **Persistencia**: PostgreSQL en Railway (tabla `app_state` con JSONB)
- **Deploy**: Railway desde branch `master`

# Cómo se trabaja en este repo

Estas reglas valen para todos los cambios, incluidos los míos.

## Idioma

La documentación, los issues, las ramas, los commits y los títulos de los pull requests van en español. Hay dos excepciones en inglés:

- Los nombres de las carpetas de los servicios (`users`, `catalog`, `orders`...).
- Los tipos de Conventional Commits (`feat`, `fix`, `docs`...), porque el chequeo del título y las herramientas de versionado los reconocen así.

## Flujo

1. Todo cambio sale de una rama nueva desde `main`. Nunca se sube directo a `main`.
2. Una rama, un tema. Los pull requests chicos se revisan mejor y rompen menos.
3. Se abre un pull request. Tiene que pasar el CI antes de integrarlo.
4. Se integra con **squash merge**: el título del pull request queda como único commit en `main`.

### Nombre de las ramas

`tipo/descripcion-corta`, en minúsculas, con guiones y sin tildes ni eñes: `feat/paginacion-catalogo`, `fix/redondeo-total-ordenes`, `docs/contratos-api`.

### Títulos y commits

Siguen [Conventional Commits](https://www.conventionalcommits.org/): `tipo(servicio): descripción`, con la descripción en español, en tercera persona y en minúscula (`agrega`, `corrige`, `elimina`).

| Tipo | Para qué |
|---|---|
| `feat` | Una funcionalidad nueva |
| `fix` | Un bug corregido |
| `docs` | Solo documentación |
| `refactor` | Cambiar el código sin cambiar lo que hace |
| `test` | Agregar o corregir tests |
| `build` / `ci` | Dependencias, Docker, workflows |
| `chore` | Todo lo demás |

Ejemplos: `feat(catalog): agrega paginación a GET /products`, `fix(orders): rechaza carritos vacíos`, `ci: cachea las dependencias de Maven`.

## Secretos

Nunca se commitean contraseñas, keys, certificados ni archivos `.env`. La configuración que cambia entre entornos va en variables de entorno, y cada servicio documenta las suyas en un `.env.example` sin valores reales. Si algo sensible llega a subirse, se considera comprometido y se cambia, aunque se borre después: queda en el historial.

## Servicios

Cada servicio vive en `services/<nombre>/`, con su propio `README.md` que explica cómo correrlo y qué endpoints expone. Cuando se agrega uno nuevo, también se suman su job en `.github/workflows/ci.yml` y su entrada en `.github/dependabot.yml`.

# Cómo se trabaja en este repo

Estas reglas valen para todos los cambios, incluidos los míos.

## Idioma

La documentación (README, issues, plantillas) está en español. El código, los nombres de carpetas, las ramas, los commits y los títulos de los pull requests van en inglés.

## Flujo

1. Todo cambio sale de una rama nueva desde `main`. Nunca se sube directo a `main`.
2. Una rama, un tema. Los pull requests chicos se revisan mejor y rompen menos.
3. Se abre un pull request. Tiene que pasar el CI antes de integrarlo.
4. Se integra con **squash merge**: el título del pull request queda como único commit en `main`.

### Nombre de las ramas

`tipo/descripcion-corta`, en minúsculas y con guiones: `feat/catalog-pagination`, `fix/orders-total-rounding`, `docs/api-contracts`.

### Títulos y commits

Siguen [Conventional Commits](https://www.conventionalcommits.org/): `tipo(servicio): descripción en imperativo`.

| Tipo | Para qué |
|---|---|
| `feat` | Una funcionalidad nueva |
| `fix` | Un bug corregido |
| `docs` | Solo documentación |
| `refactor` | Cambiar el código sin cambiar lo que hace |
| `test` | Agregar o corregir tests |
| `build` / `ci` | Dependencias, Docker, workflows |
| `chore` | Todo lo demás |

Ejemplos: `feat(catalog): add pagination to GET /products`, `fix(orders): reject empty carts`, `ci: cache Maven dependencies`.

## Secretos

Nunca se commitean contraseñas, keys, certificados ni archivos `.env`. La configuración que cambia entre entornos va en variables de entorno, y cada servicio documenta las suyas en un `.env.example` sin valores reales. Si algo sensible llega a subirse, se considera comprometido y se cambia, aunque se borre después: queda en el historial.

## Servicios

Cada servicio vive en `services/<nombre>/`, con su propio `README.md` que explica cómo correrlo y qué endpoints expone. Cuando se agrega uno nuevo, también se suman su job en `.github/workflows/ci.yml` y su entrada en `.github/dependabot.yml`.

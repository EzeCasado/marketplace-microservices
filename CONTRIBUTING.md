# Cómo se trabaja en este repo

Estas reglas valen para todos los cambios, incluidos los míos.

## Idioma

La documentación, los issues, las ramas, los commits y los títulos de los pull requests van en español. Hay dos excepciones en inglés:

- Los nombres de las carpetas de los servicios (`users`, `catalog`, `orders`...).
- Los tipos de Conventional Commits (`feat`, `fix`, `docs`...), porque el chequeo del título y las herramientas de versionado los reconocen así.

## Flujo (gitflow)

Hay dos ramas permanentes:

- `develop`: donde se integra el trabajo del día a día. Es la rama por defecto del repo.
- `main`: solo versiones estables. Cada integración a `main` es una versión nueva.

Y tres tipos de ramas temporales:

| Rama | Sale de | Se integra en | Cómo |
|---|---|---|---|
| De trabajo (`feat/...`, `fix/...`, `docs/...`) | `develop` | `develop` | Squash merge: queda un solo commit con el título del pull request |
| `release/vX.Y.Z` | `develop` | `main` | Merge commit |
| `hotfix/descripcion` | `main` | `main` | Merge commit |

Después de integrar una `release` o un `hotfix` en `main`, se abre un pull request de `main` a `develop` (también con merge commit), para que `develop` tenga lo mismo que salió.

Reglas:

1. Nunca se sube directo a `develop` ni a `main`: todo entra por pull request y con el CI en verde.
2. Una rama, un tema. Los pull requests chicos se revisan mejor y rompen menos.
3. Entre `develop` y `main` nunca se usa squash. Si se aplasta, las dos ramas dejan de compartir historial y cada versión nueva arrastra conflictos con cambios que ya estaban integrados.

### Nombre de las ramas

`tipo/descripcion-corta`, en minúsculas, con guiones y sin tildes ni eñes: `feat/paginacion-catalogo`, `fix/redondeo-total-ordenes`, `docs/contratos-api`. Las versiones van como `release/v0.1.0` y las correcciones urgentes como `hotfix/stock-negativo`.

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

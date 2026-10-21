# Cómo se trabaja en este repo

Estas reglas valen para todos los cambios, incluidos los míos.

## Idioma

La documentación, los issues, las ramas, los commits y los títulos de los pull requests van en español. Hay dos excepciones en inglés:

- Los nombres de las carpetas de los servicios (`users`, `catalog`, `orders`...).
- Los tipos de Conventional Commits (`feat`, `fix`, `docs`...), porque el chequeo del título y las herramientas de versionado los reconocen así.

## Flujo (gitflow)

Hay dos ramas permanentes:

- `develop`: donde se integra el trabajo del día a día. Es la rama por defecto del repo.
- `main`: solo lo que se publica. Cada integración a `main` termina en versiones nuevas (ver [Versiones](#versiones)).

Y dos tipos de ramas temporales:

| Rama | Sale de | Se integra en | Cómo |
|---|---|---|---|
| De trabajo (`feat/...`, `fix/...`, `docs/...`) | `develop` | `develop` | Squash merge: queda un solo commit con el título del pull request |
| `hotfix/descripcion` | `main` | `main` | Merge commit |

Para publicar, `develop` entra a `main` con un pull request directo, sin rama `release/*`: como cada servicio tiene su propia versión, un número de versión en el nombre de la rama no representaría a ninguno.

Después de cualquier integración en `main`, se abre un pull request de `main` a `develop` (también con merge commit), para que `develop` tenga lo mismo que salió.

Reglas:

1. Nunca se sube directo a `develop` ni a `main`: todo entra por pull request y con el CI en verde.
2. Una rama, un tema. Los pull requests chicos se revisan mejor y rompen menos.
3. Entre `develop` y `main` nunca se usa squash. Si se aplasta, las dos ramas dejan de compartir historial y cada versión nueva arrastra conflictos con cambios que ya estaban integrados.

### Nombre de las ramas

`tipo/descripcion-corta`, en minúsculas, con guiones y sin tildes ni eñes: `feat/paginacion-catalogo`, `fix/redondeo-total-ordenes`, `docs/contratos-api`. Las correcciones urgentes van como `hotfix/stock-negativo`.

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

## Versiones

Cada servicio tiene su propia versión y su propio `CHANGELOG.md`, con [Semantic Versioning](https://semver.org/lang/es/). Las calcula [release-please](https://github.com/googleapis/release-please) a partir de los títulos de los commits: `fix` sube el parche, `feat` sube la versión menor y un cambio incompatible (`feat!:`) sube la mayor. Mientras un servicio esté en `0.x`, un cambio incompatible sube la menor.

Para publicar:

1. Se abre un pull request de `develop` a `main` y se integra con merge commit.
2. release-please abre (o actualiza) un pull request de versión contra `main`, con los números nuevos y los changelogs de los servicios que cambiaron. Se revisa y se integra con merge commit.
3. Al integrarlo, se crean los tags (`catalog-v0.1.0`) y los releases en GitHub.
4. Se abre un pull request de `main` a `develop` para traer las versiones y los changelogs.

Los milestones del repo son las versiones planificadas (por ejemplo, `catalog v0.1.0`): una versión se publica cuando su milestone queda sin issues abiertos.

## Secretos

Nunca se commitean contraseñas, keys, certificados ni archivos `.env`. La configuración que cambia entre entornos va en variables de entorno, y cada servicio documenta las suyas en un `.env.example` sin valores reales. Si algo sensible llega a subirse, se considera comprometido y se cambia, aunque se borre después: queda en el historial.

## Servicios

Cada servicio vive en `services/<nombre>/`, con su propio `README.md` que explica cómo correrlo y qué endpoints expone. Cuando se agrega uno nuevo, también se suman:

- su job en `.github/workflows/ci.yml`;
- su entrada en `.github/dependabot.yml` y en `.github/labeler.yml`;
- su paquete en `release-please-config.json`, con versión `0.0.0` en `.release-please-manifest.json`.

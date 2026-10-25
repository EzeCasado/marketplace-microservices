# catalog

Servicio de catálogo de productos: productos, categorías y búsqueda. Es el módulo 2 de la consigna.

Viene del trabajo práctico de POO ([repo original](https://github.com/EzeCasado/POO_Catalogo_Productos_TPI)), copiado sin su historial, y desde ahí se va alineando con la consigna. Lo que falta y lo que ya se hizo está en la [épica #8](https://github.com/EzeCasado/marketplace-microservices/issues/8) y, desde la primera versión, en el `CHANGELOG.md` del servicio. El frontend del TPI no se trajo.

## Tecnología

Java 21, Spring Boot 4.1 (Web MVC, Data JPA, Validation), MySQL, Flyway, MapStruct y Lombok.

## Cómo correrlo

### Con Docker

El catálogo y su MySQL se levantan juntos con el Compose de la raíz. Los pasos están en el [README de la raíz](../../README.md#cómo-levantarlo).

Si la base del Compose se creó antes de Flyway, hay que borrarla una vez con `docker compose down -v` antes de levantarlo: la armó Hibernate, y Flyway no arranca contra una base con tablas y sin su historial. Desde ahí, los cambios de esquema se aplican solos al arrancar (ver [Migraciones de la base](#migraciones-de-la-base)).

En el CI, el job `Catalog (Docker Compose)` levanta lo mismo y le pega a `GET /categories` y a dos productos de los [datos de prueba](#datos-de-prueba) (`BK-001` y `EB-001`) en cada pull request que toca `services/catalog` o el Compose.

### Con Maven

Hace falta Java 21 o más nuevo y un MySQL accesible.

1. Copiá `.env.example` como `.env` y completá `DB_PASSWORD`. Las demás variables tienen valores por defecto.
2. Definí las variables en tu terminal o en tu IDE y levantalo:

```bash
./mvnw spring-boot:run
```

Si no tenés un MySQL instalado, usá el del Compose. Desde la raíz del repo, con el `.env` de la raíz completo:

```bash
docker compose up -d catalog-db
```

En el `.env` del catálogo, poné `DB_URL=jdbc:mysql://localhost:3307/catalog?serverTimezone=UTC` y en `DB_PASSWORD` la misma `CATALOG_DB_PASSWORD` del `.env` de la raíz.

### Con las dos formas

Queda escuchando en el puerto `8082`. Si Usuarios, Inventario o Reseñas no están corriendo, el catálogo funciona igual, pero `cantidad` y `ratingPromedio` vienen en `null`. En Docker vienen siempre en `null`: el Compose todavía no tiene esos servicios.

La excepción son las escrituras (`POST`, `PATCH` y `DELETE`): validan la `X-API-Key` contra Usuarios y, sin una respuesta 2xx, la rechazan. Si Usuarios rechaza la key, responden 401; si la key no tiene permiso, 403, y si Usuarios no responde (está caído, tarda más de 3 segundos o contesta otro código), 503. Para escribir en local hace falta Usuarios corriendo o cualquier servicio que responda 200 en `USERS_VALIDATE_URL` (por ejemplo, un WireMock suelto). En Docker, por ahora, sale siempre 503: el Compose no tiene Usuarios.

## Datos de prueba

En el Compose, el catálogo arranca con datos cargados, para poder mirar la API sin Usuarios. Están en `src/main/resources/db/seed/R__datos_de_prueba.sql`.

Son cuatro categorías: `cat-libros` (Libros), `cat-electronica` (Electrónica), `cat-software` (Software) y `cat-hogar` (Hogar). Y diez productos, cada uno con un caso para ver:

| SKU | Nombre | Tipo | Categoría | Qué muestra |
|---|---|---|---|---|
| `BK-001` | Libro POO | `FISICO` | `cat-libros` | El ejemplo del contrato, con descripción |
| `EB-001` | Libro POO (e-book) | `DIGITAL` | `cat-libros` | El ejemplo del contrato, sin descripción |
| `BK-002` | Pequeño manual de programación | `FISICO` | `cat-libros` | Tildes y eñe, para ver el charset |
| `BK-003` | Estructuras de datos | `FISICO` | `cat-libros` | Otro libro, para la búsqueda y la paginación |
| `EL-001` | Televisor LED 65 pulgadas | `FISICO` | `cat-electronica` | Pesado y grande |
| `EL-002` | Auriculares inalámbricos | `FISICO` | `cat-electronica` | Un `FISICO` sin descripción |
| `EL-003` | Tarjeta microSD 128 GB | `FISICO` | `cat-electronica` | El volumen mínimo, `0.000001` |
| `SW-001` | Antivirus anual | `DIGITAL` | `cat-software` | Un `DIGITAL` con descripción y centavos en el precio |
| `SW-002` | Editor de código Pro | `DIGITAL` | `cat-software` | Un `DIGITAL` sin descripción |
| `HG-001` | Lámpara de escritorio | `FISICO` | `cat-hogar` | Dado de baja, el único de su categoría |

Los precios, pesos y volúmenes están en el archivo.

Se cargan solo en el Compose: el `docker-compose.yml` suma `db/seed` a las ubicaciones de Flyway con `SPRING_FLYWAY_LOCATIONS`. Con Maven y en los tests no se cargan, y los tests arrancan con la base vacía. El archivo es una migración repetible: corre después de las versionadas y vuelve a correr cuando cambia, sin duplicar filas.

Con el Compose levantado, desde PowerShell (`curl.exe`, no el alias `curl`):

```powershell
curl.exe -i http://localhost:8082/categories
curl.exe -i http://localhost:8082/products
curl.exe -i http://localhost:8082/products/BK-001
```

- Sirven para leer, no para escribir: los `POST`, `PATCH` y `DELETE` siguen dando 503 sin Usuarios.
- `GET /products` devuelve, por ahora, un array con el detalle completo de los nueve activos, sin paginar. Cambia con el [#13](https://github.com/EzeCasado/marketplace-microservices/issues/13).
- `GET /products/HG-001` da 200 aunque el producto está dado de baja; el contrato pide 404 ([#20](https://github.com/EzeCasado/marketplace-microservices/issues/20)).
- Los datos quedan en el volumen de MySQL. Si se saca una fila del archivo, sigue en la base. Para empezar de cero, `docker compose down -v` y otro `up`.
- Si una migración nueva cambia `categorias` o `productos`, el archivo se actualiza en el mismo pull request. Si no, el catálogo no arranca en el Compose, y lo ve el job `Catalog (Docker Compose)` del CI.

## Migraciones de la base

El esquema lo arma Flyway con las migraciones de `src/main/resources/db/migration`, que corren solas al arrancar la app, en orden y una sola vez cada una. La `V1` es el esquema que generaba Hibernate antes de Flyway. La `V2` suma `tipo` y `volumen` a los productos, deja `peso` opcional y saca `dimensiones`.

- Un cambio de esquema es un archivo nuevo, `V<n>__<que_hace>.sql`, con el número siguiente y dos guiones bajos. La próxima es `V3__<que_hace>.sql`.
- Una migración que ya se aplicó no se edita: Flyway guarda su checksum y, si el archivo cambia, la app no arranca. Para corregirla, va otra migración.
- Los [datos de prueba](#datos-de-prueba) (`db/seed/R__datos_de_prueba.sql`) no son una `V<n>`: son una migración repetible, no cuentan para la numeración y solo los lee el Compose.
- Hibernate no toca el esquema (`ddl-auto=validate`): al arrancar comprueba que las entidades coincidan con la base y, si falta una tabla o una columna o un tipo no coincide, la app no arranca. Por eso el cambio de una entidad y su migración van juntos.

## Tests

Son tests de integración: levantan la aplicación completa en un puerto libre, contra un MySQL 8.4 en un contenedor (Testcontainers), con Usuarios, Inventario y Reseñas simulados con WireMock. No hace falta tener un MySQL propio ni los otros servicios corriendo.

- `CatalogoDeProductosTpiApplicationTests`: la app arranca contra MySQL y `GET /categories` lista lo que hay en la base.
- `ProductoPorSkuTests`: `/products/{sku}`. Un SKU que no existe, el detalle con los campos del contrato (un FISICO con la `cantidad` de Inventario aunque Inventario mande campos de más, y un DIGITAL sin `peso`, `volumen` ni `descripcion`), el detalle con Inventario caído o lento, el `PATCH` (el precio, los campos que no acepta y los decimales de más), la baja con y sin `X-API-Key`, y que ninguna key pasa sin Usuarios, ni siquiera `TEST-KEY`, la que aceptaba el TPI. También los códigos de la baja según lo que responde Usuarios: 401, 403 y 503 (caído, lento, con un 500 o con un 404). Sirve de molde para los tests nuevos.
- `CategoriasTests`: las categorías con id de texto. El alta con el id que manda el cliente, el 409 por un id o un nombre repetido (sin pisar la categoría que estaba), la validación del id, `GET /categories/{categoryId}` con un id que no existe o mal formado, y `GET /categories` con la lista vacía y ordenada por nombre.
- `AltaDeProductoTests`: el `categoriaId` de texto en los productos. El alta con una categoría que existe, el 400 con una que no existe o mal formada, el 422 si falta, el 409 del SKU repetido antes que la categoría y el filtro `categoriaId` de `GET /products`.
- `CamposDelProductoTests`: el alta según el `tipo`. `peso` y `volumen` obligatorios en un FISICO y prohibidos en un DIGITAL, un `tipo` inválido, los decimales de más, el 400 por un campo que el body no acepta (también en el alta de categorías) y la respuesta del alta, con `cantidad` en `null`.
- `CodigosDeErrorTests`: los códigos de error del contrato que no dependen de Usuarios. JSON roto, parámetro inválido, ruta inexistente, 405, 406, 415, las reglas del 422 y del 400, y un 500 que no muestra detalles internos.
- `MigracionesTests`: las migraciones de Flyway sobre la base vacía. La base queda en la última versión, sin migraciones pendientes ni fallidas, y la tabla `productos` queda con las columnas de la `V2`. Cada migración nueva sube la versión que espera.
- `TestDeIntegracion`: la clase base que arma la app, el MySQL y los WireMock. Los tests nuevos la extienden.

Hace falta:

- **Docker andando.** En Windows, Docker Desktop abierto. La primera corrida baja la imagen `mysql:8.4` y tarda más.
- **Java 21 o más nuevo.** El wrapper usa el JDK de `JAVA_HOME` o, si no está definida, el `java` del PATH.

Se corren desde `services/catalog`. En Windows:

```powershell
.\mvnw.cmd verify
```

En Linux o Mac:

```bash
./mvnw verify
```

En el CI corren solos: el job `Catalog (build and test)` hace `mvn -B verify` en cada pull request que toca `services/catalog`.

## Endpoints

Así están hoy. Todavía no coinciden del todo con la consigna (ver más abajo).

| Método | Ruta | Qué hace | Pide `X-API-Key` |
|---|---|---|---|
| `GET` | `/products` | Lista los productos activos. Filtros opcionales: `nombre`, `categoriaId` | No |
| `GET` | `/products/{sku}` | Detalle de un producto, con `cantidad` de Inventario y rating | No |
| `POST` | `/products` | Crea un producto. El `tipo` es `FISICO`, con `peso` y `volumen`, o `DIGITAL`, sin ellos. No acepta `cantidad`: el stock se carga en Inventario | Sí |
| `PATCH` | `/products/{sku}` | Modifica `nombre`, `descripcion` o `precio`. Valida `peso` y `volumen`, pero todavía no los guarda. No acepta `stock`, `cantidad`, `tipo` ni `sku` | Sí |
| `DELETE` | `/products/{sku}` | Baja lógica | Sí |
| `GET` | `/categories` | Lista las categorías dentro de `items`, ordenadas por nombre | No |
| `GET` | `/categories/{categoryId}` | Detalle de una categoría | No |
| `POST` | `/categories` | Crea una categoría | Sí |

El id de una categoría lo elige quien la crea: minúsculas, números y guiones entre medio, de 3 a 50 caracteres (por ejemplo, `cat-libros`). Un id o un nombre que ya existen dan 409.

Los errores salen en JSON con la forma `{ "error": "...", "message": "..." }`. Los códigos y cuándo sale cada uno están en el contrato, [`docs/api/catalog.yaml`](../../docs/api/catalog.yaml).

Los bodies son cerrados, como en el contrato: un campo que la operación no acepta da 400 (por ejemplo, `El campo cantidad no se acepta en esta operación`), también en el alta de categorías.

## Diferencias conocidas con la consigna

Se revisaron leyendo el código y van a ir como issues. Las principales:

- `GET /products` filtra por `nombre` y devuelve un array; la consigna pide `q`, rango de precio, paginación y la forma `{items, page, pageSize, total}`.
- El cliente de Inventario espera el campo `disponibilidad`; la consigna devuelve `disponible`.
- `PATCH /products/{sku}` todavía no guarda `peso` ni `volumen`, rechaza `categoriaId` con un 400 y responde 200 a un body vacío; el contrato los aplica y pide 422 para `{}` ([#19](https://github.com/EzeCasado/marketplace-microservices/issues/19)).
- El detalle de un producto trae `ratingPromedio` y `cantidadResenas`, que el contrato no tiene ([#29](https://github.com/EzeCasado/marketplace-microservices/issues/29)).

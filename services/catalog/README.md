# catalog

Servicio de catálogo de productos: productos, categorías y búsqueda. Es el módulo 2 de la consigna.

Viene del trabajo práctico de POO ([repo original](https://github.com/EzeCasado/POO_Catalogo_Productos_TPI)), copiado sin su historial, y desde ahí se va alineando con la consigna. Lo que falta y lo que ya se hizo está en la [épica #8](https://github.com/EzeCasado/marketplace-microservices/issues/8) y, desde la primera versión, en el `CHANGELOG.md` del servicio. El frontend del TPI no se trajo.

## Tecnología

Java 21, Spring Boot 4.1 (Web MVC, Data JPA, Validation), MySQL, MapStruct y Lombok.

## Cómo correrlo

### Con Docker

El catálogo y su MySQL se levantan juntos con el Compose de la raíz. Los pasos están en el [README de la raíz](../../README.md#cómo-levantarlo).

Si la base del Compose se creó antes de que las categorías pasaran a ids de texto, hay que borrarla con `docker compose down -v` antes de levantarlo: el id de las categorías cambió de tipo y la base no se actualiza sola.

En el CI, el job `Catalog (Docker Compose)` levanta lo mismo y le pega a `GET /categories` en cada pull request que toca `services/catalog` o el Compose.

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

Queda escuchando en el puerto `8082`. Si Usuarios, Inventario o Reseñas no están corriendo, el catálogo funciona igual, pero `stock` y `ratingPromedio` vienen en `null`. En Docker vienen siempre en `null`: el Compose todavía no tiene esos servicios.

La excepción son las escrituras (`POST`, `PATCH` y `DELETE`): validan la `X-API-Key` contra Usuarios y, sin una respuesta 2xx, la rechazan. Si Usuarios rechaza la key, responden 401; si la key no tiene permiso, 403, y si Usuarios no responde (está caído, tarda más de 3 segundos o contesta otro código), 503. Para escribir en local hace falta Usuarios corriendo o cualquier servicio que responda 200 en `USERS_VALIDATE_URL` (por ejemplo, un WireMock suelto). En Docker, por ahora, sale siempre 503: el Compose no tiene Usuarios.

## Tests

Son tests de integración: levantan la aplicación completa en un puerto libre, contra un MySQL 8.4 en un contenedor (Testcontainers), con Usuarios, Inventario y Reseñas simulados con WireMock. No hace falta tener un MySQL propio ni los otros servicios corriendo.

- `CatalogoDeProductosTpiApplicationTests`: la app arranca contra MySQL y `GET /categories` lista lo que hay en la base.
- `ProductoPorSkuTests`: `/products/{sku}`. Un SKU que no existe, el detalle con Inventario caído o lento, la baja con y sin `X-API-Key`, y que ninguna key pasa sin Usuarios, ni siquiera `TEST-KEY`, la que aceptaba el TPI. También los códigos de la baja según lo que responde Usuarios: 401, 403 y 503 (caído, lento, con un 500 o con un 404). Sirve de molde para los tests nuevos.
- `CategoriasTests`: las categorías con id de texto. El alta con el id que manda el cliente, el 409 por un id o un nombre repetido (sin pisar la categoría que estaba), la validación del id y `GET /categories/{categoryId}` con un id que no existe o mal formado.
- `AltaDeProductoTests`: el `categoriaId` de texto en los productos. El alta con una categoría que existe, el 400 con una que no existe o mal formada, el 422 si falta, el 409 del SKU repetido antes que la categoría y el filtro `categoriaId` de `GET /products`.
- `CodigosDeErrorTests`: los códigos de error del contrato que no dependen de Usuarios. JSON roto, parámetro inválido, ruta inexistente, 405, 406, 415, las reglas del 422 y del 400, y un 500 que no muestra detalles internos.
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
| `GET` | `/products/{sku}` | Detalle de un producto, con stock y rating | No |
| `POST` | `/products` | Crea un producto | Sí |
| `PATCH` | `/products/{sku}` | Modifica los campos enviados | Sí |
| `DELETE` | `/products/{sku}` | Baja lógica | Sí |
| `GET` | `/categories` | Lista las categorías | No |
| `GET` | `/categories/{categoryId}` | Detalle de una categoría | No |
| `POST` | `/categories` | Crea una categoría | Sí |

El id de una categoría lo elige quien la crea: minúsculas, números y guiones entre medio, de 3 a 50 caracteres (por ejemplo, `cat-libros`). Un id o un nombre que ya existen dan 409.

Los errores salen en JSON con la forma `{ "error": "...", "message": "..." }`. Los códigos y cuándo sale cada uno están en el contrato, [`docs/api/catalog.yaml`](../../docs/api/catalog.yaml).

## Diferencias conocidas con la consigna

Se revisaron leyendo el código y van a ir como issues. Las principales:

- `GET /products` filtra por `nombre` y devuelve un array; la consigna pide `q`, rango de precio, paginación y la forma `{items, page, pageSize, total}`.
- El cliente de Inventario espera el campo `disponibilidad`; la consigna devuelve `disponible`.

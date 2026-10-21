# catalog

Servicio de catálogo de productos: productos, categorías y búsqueda. Es el módulo 2 de la consigna.

Viene del trabajo práctico de POO ([repo original](https://github.com/EzeCasado/POO_Catalogo_Productos_TPI)), copiado sin su historial. El código está tal cual se entregó; lo único que cambió es la configuración, que ahora sale de variables de entorno. El frontend del TPI no se trajo.

## Tecnología

Java 21, Spring Boot 3.3 (Web, Data JPA, Validation), MySQL, MapStruct y Lombok.

## Cómo correrlo

Hace falta Java 21 y un MySQL accesible.

1. Copiá `.env.example` como `.env` y completá `DB_PASSWORD`. Las demás variables tienen valores por defecto.
2. Definí las variables en tu terminal o en tu IDE y levantalo:

```bash
./mvnw spring-boot:run
```

Queda escuchando en el puerto `8082`. Si Usuarios, Inventario o Reseñas no están corriendo, el catálogo funciona igual, pero `stock` y `ratingPromedio` vienen en `null`.

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
| `GET` | `/categories/{id}` | Detalle de una categoría | No |
| `POST` | `/categories` | Crea una categoría | Sí |

Los errores salen en JSON con la forma `{ "error": "...", "message": "..." }`.

## Diferencias conocidas con la consigna

Se revisaron leyendo el código y van a ir como issues. Las principales:

- Las categorías usan un id numérico autogenerado; la consigna usa ids de texto (`cat-libros`).
- `GET /products` filtra por `nombre` y devuelve un array; la consigna pide `q`, rango de precio, paginación y la forma `{items, page, pageSize, total}`.
- El cliente de Inventario espera el campo `disponibilidad`; la consigna devuelve `disponible`.
- El manejador de errores devuelve 500 para cualquier error de Spring que no maneje (JSON roto, ruta inexistente).
- El interceptor acepta la clave `TEST-KEY` sin validarla contra Usuarios. Se dejó para la demo del TPI y hay que sacarla.

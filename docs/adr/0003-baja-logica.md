# 0003. Baja lógica de productos

- **Estado:** Aceptado, 2026-10-21.
- **Origen:** decidido por Eze el 2026-10-21, en un comentario del issue #9.

## Contexto

La consigna describe el catálogo como "alta, baja, modificación y búsqueda", pero no lista ningún endpoint de baja: solo `POST`, `GET` y `PATCH`.

El catálogo del TPI ya tenía `DELETE /products/{sku}`, con baja lógica: marca el producto como inactivo y no lo borra.

## Decisión

- Se mantiene `DELETE /products/{sku}`, aunque no esté en la consigna.
- Es una baja lógica. El producto dado de baja deja de existir para la API: `GET` y `PATCH` sobre su `sku` devuelven 404, y no aparece en el listado.
- Órdenes guarda una copia del nombre y el precio de cada producto al momento de la compra.

La decisión no deja escrito por qué la baja es lógica y no física.

Lo que el contrato agrega sobre esto:

- `DELETE` responde 204 sin body. *Propuesto por Claude Code* en la tarea 1: es lo que hace hoy el servicio.
- Un segundo `DELETE` sobre el mismo `sku` devuelve 404. *Deducción*, a partir de "deja de existir para la API".
- La respuesta no trae el campo `activo`. *Propuesto por Claude Code* y aceptado por Eze el 2026-10-21: como los productos dados de baja no se devuelven, siempre valdría `true`.

## Consecuencias

**Se gana:**

- Se puede sacar un producto de la venta sin perder sus datos en la base. *Deducción.*
- Con la copia que guarda Órdenes, una orden vieja sigue mostrando qué se compró y a qué precio, aunque el producto ya no exista para la API ni haya cambiado de precio. *Deducción.*

**Se paga:**

- La API no ofrece forma de ver ni de reactivar un producto dado de baja. Para recuperarlo hay que ir a la base. *Deducción.*
- Órdenes no puede pedirle al catálogo los datos de un producto dado de baja: depende de su copia. *Deducción.*
- No está decidido si el `sku` de un producto dado de baja se puede volver a usar. El contrato responde 409 cuando el `sku` ya existe, y no trata aparte este caso. Sigue abierto.
- No está decidido qué pasa con el stock en Inventario cuando un producto se da de baja.

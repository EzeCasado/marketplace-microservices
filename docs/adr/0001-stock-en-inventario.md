# 0001. El stock es de Inventario

- **Estado:** Aceptado, 2026-10-21.
- **Origen:** decidido por Eze el 2026-10-21, en un comentario del issue #9.

## Contexto

La consigna mezcla el stock con el catálogo. El body de ejemplo de `POST /products` (2.1) trae `cantidad`, y la respuesta de `GET /products/{sku}` (2.3) también. Pero el stock por SKU es lo que hace Inventario (módulo 3), con sus propias reservas y confirmaciones.

Si el catálogo aceptara `cantidad` en el alta, habría dos lugares donde cargar el stock.

## Decisión

- El catálogo no guarda stock.
- `POST /products` no acepta `cantidad`. El stock inicial se carga en Inventario.
- `GET /products/{sku}` completa `cantidad` consultándole a Inventario. Si Inventario no responde, `cantidad` viene en `null` y el resto del detalle se devuelve igual.

Lo que el contrato agrega sobre esto:

- `PATCH /products/{sku}` tampoco acepta `cantidad`. Mandarla da 400. *Propuesto por Claude Code* en la tarea 1 y aceptado por Eze el 2026-10-21.
- El listado no trae `cantidad`, porque devuelve solo un resumen (`sku`, `nombre`, `precio`) y no le consulta a Inventario. *Decidido* por Eze el 2026-10-21.

## Consecuencias

**Se gana:**

- El stock se modifica en un solo servicio. *Deducción:* las reservas de Inventario y el número que muestra el catálogo no pueden desincronizarse por una carga en el lugar equivocado.

**Se paga:**

- Dar de alta un producto con stock lleva dos pasos, uno en el catálogo y otro en Inventario. *Deducción.*
- En la respuesta del alta, `cantidad` viene en `null` hasta que Inventario tenga el producto. *Deducción.*
- El detalle depende de Inventario para un campo. Si Inventario está caído, el cliente recibe `null` y tiene que distinguirlo de un stock 0.
- Se aparta del ejemplo de 2.1. Un cliente escrito contra la consigna, que mande `cantidad` en el alta, recibe 400.

# 0002. `tipo`, `peso` y `volumen`

- **Estado:** Aceptado, 2026-10-21.
- **Origen:** decidido por Eze el 2026-10-21, en un comentario del issue #9.

## Contexto

La respuesta de `GET /products/{sku}` en la consigna (2.3) trae `"tipo": "FISICO"`, pero la consigna no dice qué otros valores hay ni qué cambia según el tipo. Tampoco pide `peso` ni `volumen` en el producto.

Envíos (módulo 6) cotiza con el peso y el volumen (consigna 6.1), así que esos datos tienen que estar en algún lado. El catálogo del TPI pedía `peso` siempre y guardaba `dimensiones` como texto libre.

## Decisión

- `tipo` es `FISICO` o `DIGITAL`.
- Para `FISICO`, `peso` (en kg) y `volumen` (en m³) son obligatorios, porque Envíos cotiza con ellos.
- Para `DIGITAL`, `peso` y `volumen` no van.

En el contrato, la regla está en el esquema `PesoYVolumenSegunTipo`, con `if`/`then`/`else` de OpenAPI 3.1. La usan el alta (`ProductoAlta`) y el detalle (`Producto`).

Lo que el contrato agrega sobre esto (*propuesto por Claude Code* en la tarea 1 y aceptado por Eze el 2026-10-21):

- `dimensiones` sale; lo reemplaza `volumen`.
- `tipo` no se cambia con `PATCH`: mandarlo da 400.
- Mandarle `peso` o `volumen` a un producto `DIGITAL` con `PATCH` da 400. El esquema no puede expresarlo, porque no conoce el `tipo` guardado, así que la regla está escrita en la descripción de la operación.

Si en el alta de un `FISICO` falta `peso` o `volumen`, da 422, porque es un campo obligatorio que falta. Sale de los códigos de error que se decidieron el mismo día.

## Consecuencias

**Se gana:**

- Envíos puede cotizar cualquier producto físico sin datos faltantes.
- Un producto digital no arrastra un peso inventado. *Deducción:* hoy el código lo exige siempre.

**Se paga:**

- El alta de un producto físico pide dos datos más.
- Un producto que se cargó con el `tipo` equivocado no se corrige con `PATCH`: hay que darlo de baja y crearlo de nuevo. *Deducción*, a partir de que `tipo` no se cambia. Choca con lo que sigue abierto en la nota de la tarea: no está decidido si se puede reusar el `sku` de un producto dado de baja.
- Con `volumen` se pierde la forma. *Deducción:* un paquete largo y fino tiene el mismo volumen que un cubo. Alcanza mientras Envíos cotice solo por peso y volumen.
- La regla del `PATCH` para `DIGITAL` no la valida el esquema, así que la tiene que validar el servicio.

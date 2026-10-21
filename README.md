# marketplace-microservices

Un marketplace armado con microservicios, en un monorepo. **En construcción: todavía no hay código.**

## Origen

Nace del trabajo práctico integrador de Programación Orientada a Objetos (UNSAdA, 2025). En ese trabajo cada grupo hizo un módulo de un marketplace y todos se comunicaban por API REST. Mi grupo hizo el catálogo de productos: [POO_Catalogo_Productos_TPI](https://github.com/EzeCasado/POO_Catalogo_Productos_TPI). Este repo retoma la idea con todos los módulos hechos por una sola persona.

## Qué va a tener

| Módulo | Qué hace | Tecnología |
|---|---|---|
| Usuarios y Autenticación | Registro, login, roles y API keys | Spring |
| Catálogo de Productos | Productos, categorías y búsqueda | Spring |
| Inventario | Stock, reservas y confirmaciones | Spring |
| Carrito y Órdenes | Carritos y órdenes de compra | Spring |
| Pagos | Simulación de pagos | Spring |
| Envíos y Logística | Cotización y tracking de envíos | Spring |
| Reseñas y Reputación | Reseñas y rating promedio | Spring |
| Notificaciones | Mensajes por email e in-app | Spring |
| API Gateway | Punto de entrada único | Spring |
| Asistente con IA | Un chat que usa las APIs de los demás servicios | Python |

Todo se va a levantar junto con Docker Compose.

## Cómo se trabaja

El repo se va a desarrollar con un flujo parecido al de un equipo: ramas cortas, pull requests, integración continua con GitHub Actions y dependencias actualizadas con Dependabot. Esas piezas se suman de a una; este README se actualiza a medida que existan. Las reglas están en [CONTRIBUTING.md](CONTRIBUTING.md).

## Licencia

Por ahora el repo no tiene licencia. El código se puede leer, pero no copiar, modificar ni redistribuir.

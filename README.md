# marketplace-microservices

Un marketplace armado con microservicios, en un monorepo. **En construcción.**

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

Hoy está el catálogo, en [`services/catalog/`](services/catalog/), traído del TPI.

## Cómo levantarlo

Todo se levanta con Docker Compose, desde la raíz del repo. Por ahora, el catálogo y su MySQL. Hace falta Docker; en Windows, Docker Desktop abierto.

1. Copiá `.env.example` como `.env` y completá `CATALOG_DB_PASSWORD`. El `.env` no se commitea.
2. Levantalo:

```bash
docker compose up --build
```

El catálogo queda en `http://localhost:8082` y su MySQL en `127.0.0.1:3307`. Si el 3307 ya está tomado, cambialo con `CATALOG_DB_PORT` en el `.env`. La primera vez tarda más: baja las imágenes y compila el catálogo.

El catálogo arranca con datos de prueba (cuatro categorías y diez productos), para poder mirar la API sin Usuarios. Qué trae y cómo se usa está en el [README del catálogo](services/catalog/README.md#datos-de-prueba).

Para bajarlo, `down` guarda los datos de MySQL y `down -v` los borra:

```bash
docker compose down
docker compose down -v
```

## Cómo se trabaja

El repo se va a desarrollar con un flujo parecido al de un equipo: gitflow (el trabajo se integra en `develop` y `main` recibe solo versiones estables), pull requests, integración continua con GitHub Actions y dependencias actualizadas con Dependabot. Esas piezas se suman de a una; este README se actualiza a medida que existan. Las reglas están en [CONTRIBUTING.md](CONTRIBUTING.md).

## Licencia

Por ahora el repo no tiene licencia. El código se puede leer, pero no copiar, modificar ni redistribuir.

# Decisiones de arquitectura (ADR)

Un ADR (Architecture Decision Record) es una nota corta que registra una decisión: el problema, lo que se decidió y lo que se gana y se paga. Sirve para que, dentro de unos meses, se pueda saber por qué algo es como es sin tener que buscar la charla donde se decidió.

Acá van las decisiones que se apartan de la consigna del TPI o la amplían.

## Cómo se escribe uno

- Un archivo por decisión: `NNNN-titulo-corto.md`, con el número siguiente al último.
- Secciones: **Estado**, **Contexto** (qué dice la consigna y qué problema hay), **Decisión** y **Consecuencias**.
- Cada decisión dice de dónde sale: quién la decidió y cuándo. Lo que es una deducción se marca como deducción.
- Un ADR aceptado no se reescribe. Si la decisión cambia, se escribe uno nuevo y el viejo pasa a **Reemplazado por NNNN**.

## Los que hay

| # | Decisión | Estado |
|---|---|---|
| [0001](0001-stock-en-inventario.md) | El stock es de Inventario: el catálogo no lo guarda y el alta no acepta `cantidad` | Aceptado, 2026-10-21 |
| [0002](0002-tipo-peso-y-volumen.md) | `tipo` es `FISICO` o `DIGITAL`; un `FISICO` lleva `peso` y `volumen` | Aceptado, 2026-10-21 |
| [0003](0003-baja-logica.md) | `DELETE /products/{sku}` hace una baja lógica, y el producto deja de existir para la API | Aceptado, 2026-10-21 |

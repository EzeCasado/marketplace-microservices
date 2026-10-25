-- Datos de prueba del catálogo: cuatro categorías y diez productos de los dos tipos, cada uno con
-- un caso para ver a mano (ver el README del servicio).
--
-- Solo lo carga el Compose, que suma db/seed a las ubicaciones de Flyway con SPRING_FLYWAY_LOCATIONS.
-- Con Maven y en los tests no se carga.
--
-- Es una migración repetible: Flyway la corre después de las versionadas y la vuelve a correr cada
-- vez que cambia el archivo. Se puede correr más de una vez sin duplicar filas: las categorías se
-- pisan por id y los productos por sku. fecha_creacion se pone al insertar y no se toca después.
--
-- El SQL se saltea las validaciones del alta, así que las reglas del contrato se cuidan acá:
-- FISICO con peso y volumen, DIGITAL sin ninguno de los dos, y los decimales de cada columna.
--
-- Si una migración nueva cambia categorias o productos, este archivo se actualiza en el mismo
-- pull request. Si no, el catálogo no arranca en el Compose.

-- Primero las categorías, por la clave foránea de productos.
insert into categorias (id, nombre) values
    ('cat-libros', 'Libros'),
    ('cat-electronica', 'Electrónica'),
    ('cat-software', 'Software'),
    ('cat-hogar', 'Hogar')
as nuevo
on duplicate key update
    nombre = nuevo.nombre;

insert into productos (sku, nombre, descripcion, tipo, precio, peso, volumen, categoria_id, activo, fecha_creacion) values
    ('BK-001', 'Libro POO', 'Programación orientada a objetos, tapa blanda.', 'FISICO', 15000.00, 0.450, 0.001200, 'cat-libros', true, now(6)),
    ('BK-002', 'Pequeño manual de programación', 'Ejercicios resueltos con explicación paso a paso.', 'FISICO', 18750.00, 0.380, 0.000950, 'cat-libros', true, now(6)),
    ('BK-003', 'Estructuras de datos', 'Listas, pilas, colas y árboles, con ejemplos en Java.', 'FISICO', 21000.00, 0.620, 0.001500, 'cat-libros', true, now(6)),
    ('EL-001', 'Televisor LED 65 pulgadas', 'Pantalla 4K, con control remoto y soporte de pared.', 'FISICO', 899999.00, 24.375, 0.187425, 'cat-electronica', true, now(6)),
    ('EL-002', 'Auriculares inalámbricos', null, 'FISICO', 45000.00, 0.250, 0.000980, 'cat-electronica', true, now(6)),
    -- El menor volumen que entra en decimal(10,6): 1 cm3.
    ('EL-003', 'Tarjeta microSD 128 GB', 'Con adaptador a SD.', 'FISICO', 12500.00, 0.002, 0.000001, 'cat-electronica', true, now(6)),
    -- Dado de baja, el único de cat-hogar.
    ('HG-001', 'Lámpara de escritorio', 'Brazo articulado y luz cálida.', 'FISICO', 27000.00, 1.200, 0.008500, 'cat-hogar', false, now(6))
as nuevo
on duplicate key update
    nombre = nuevo.nombre,
    descripcion = nuevo.descripcion,
    tipo = nuevo.tipo,
    precio = nuevo.precio,
    peso = nuevo.peso,
    volumen = nuevo.volumen,
    categoria_id = nuevo.categoria_id,
    activo = nuevo.activo;

-- Los DIGITAL no listan peso ni volumen: quedan en null. Al pisarlos también se ponen en null,
-- por si el SKU era FISICO en una versión anterior del archivo.
insert into productos (sku, nombre, descripcion, tipo, precio, categoria_id, activo, fecha_creacion) values
    ('EB-001', 'Libro POO (e-book)', null, 'DIGITAL', 9000.00, 'cat-libros', true, now(6)),
    ('SW-001', 'Antivirus anual', 'Licencia por un año para un equipo.', 'DIGITAL', 3500.50, 'cat-software', true, now(6)),
    ('SW-002', 'Editor de código Pro', null, 'DIGITAL', 22000.00, 'cat-software', true, now(6))
as nuevo
on duplicate key update
    nombre = nuevo.nombre,
    descripcion = nuevo.descripcion,
    tipo = nuevo.tipo,
    precio = nuevo.precio,
    peso = null,
    volumen = null,
    categoria_id = nuevo.categoria_id,
    activo = nuevo.activo;

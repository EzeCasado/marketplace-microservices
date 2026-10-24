-- Alinea las columnas de productos con el contrato: suma tipo y volumen, deja peso opcional
-- (un producto DIGITAL no lo lleva) y saca dimensiones, que reemplaza volumen.
--
-- Se escribió con productos vacía en todas las bases: los tests arrancan vacíos y en el Compose
-- no se pueden crear productos sin Usuarios. El DEFAULT 'FISICO' es solo para que el ADD COLUMN
-- no falle, y se saca enseguida. Una fila que ya existiera quedaría FISICO y sin volumen.
-- Cuando una migración corra sobre datos de verdad, tiene que proteger ese caso.

alter table productos
    add column tipo enum ('DIGITAL','FISICO') not null default 'FISICO';

alter table productos
    alter column tipo drop default;

alter table productos
    add column volumen decimal(10,6);

alter table productos
    modify column peso decimal(10,3) null;

alter table productos
    drop column dimensiones;

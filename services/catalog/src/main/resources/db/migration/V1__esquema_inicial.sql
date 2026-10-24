-- Esquema que generaba Hibernate antes de Flyway, con nombres legibles en las restricciones.
-- No se edita: una migración aplicada no se cambia, el cambio va en una nueva.

create table categorias (
    id varchar(50) not null,
    nombre varchar(100) not null,
    primary key (id)
) engine=InnoDB;

create table productos (
    activo bit not null,
    peso decimal(10,3) not null,
    precio decimal(10,2) not null,
    fecha_creacion datetime(6) not null,
    id bigint not null auto_increment,
    categoria_id varchar(50) not null,
    dimensiones varchar(50),
    sku varchar(50) not null,
    nombre varchar(200) not null,
    descripcion TEXT,
    primary key (id)
) engine=InnoDB;

alter table categorias
    add constraint uk_categorias_nombre unique (nombre);

alter table productos
    add constraint idx_sku unique (sku);

alter table productos
    add constraint fk_productos_categoria
    foreign key (categoria_id)
    references categorias (id);

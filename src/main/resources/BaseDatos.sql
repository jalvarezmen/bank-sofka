-- BaseDatos.sql — Sistema Bancario Sofka (Prueba Técnica)
-- Datos alineados con los casos de uso del enunciado (Jose Lema, Marianela Montalvo, Juan Osorio)
-- Ejecutar conectado al motor SQL Server (usuario sa o equivalente)

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'banking_db')
BEGIN
    CREATE DATABASE banking_db;
END
GO

USE banking_db;
GO

IF OBJECT_ID('movimientos', 'U') IS NOT NULL DROP TABLE movimientos;
IF OBJECT_ID('cuentas', 'U') IS NOT NULL DROP TABLE cuentas;
IF OBJECT_ID('clientes', 'U') IS NOT NULL DROP TABLE clientes;
GO

CREATE TABLE clientes (
    cliente_id      BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    genero          VARCHAR(20) NULL,
    edad            INT NULL,
    identificacion  VARCHAR(20) NOT NULL UNIQUE,
    direccion       VARCHAR(200) NULL,
    telefono        VARCHAR(20) NULL,
    contrasena      VARCHAR(100) NOT NULL,
    estado          BIT NOT NULL DEFAULT 1
);
GO

CREATE TABLE cuentas (
    id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    numero_cuenta    VARCHAR(20) NOT NULL UNIQUE,
    tipo_cuenta      VARCHAR(20) NOT NULL,
    saldo_inicial    DECIMAL(18,2) NOT NULL DEFAULT 0,
    saldo_disponible DECIMAL(18,2) NOT NULL DEFAULT 0,
    estado           BIT NOT NULL DEFAULT 1,
    cliente_id       BIGINT NOT NULL,
    CONSTRAINT fk_cuentas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(cliente_id)
);
GO

CREATE TABLE movimientos (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    fecha           DATETIME NOT NULL DEFAULT GETDATE(),
    tipo_movimiento VARCHAR(20) NOT NULL,
    valor           DECIMAL(18,2) NOT NULL,
    saldo           DECIMAL(18,2) NOT NULL,
    cuenta_id       BIGINT NOT NULL,
    CONSTRAINT fk_movimientos_cuenta FOREIGN KEY (cuenta_id) REFERENCES cuentas(id)
);
GO

-- Caso de uso 1: Creación de usuarios
SET IDENTITY_INSERT clientes ON;

INSERT INTO clientes (cliente_id, nombre, genero, edad, identificacion, direccion, telefono, contrasena, estado)
VALUES
    (1, N'Jose Lema', NULL, NULL, N'ID-JOSE-001', N'Otavalo sn y principal', N'098254785', N'1234', 1),
    (2, N'Marianela Montalvo', NULL, NULL, N'ID-MARIANELA-002', N'Amazonas y NNUU', N'097548965', N'5678', 1),
    (3, N'Juan Osorio', NULL, NULL, N'ID-JUAN-003', N'13 junio y Equinoccial', N'098874587', N'1245', 1);

SET IDENTITY_INSERT clientes OFF;
GO

-- Caso de uso 2: Creación de cuentas (saldos iniciales antes de movimientos)
SET IDENTITY_INSERT cuentas ON;

INSERT INTO cuentas (id, numero_cuenta, tipo_cuenta, saldo_inicial, saldo_disponible, estado, cliente_id)
VALUES
    (1, N'478758', N'Ahorro', 2000.00, 1425.00, 1, 1),
    (2, N'225487', N'Corriente', 100.00, 700.00, 1, 2),
    (3, N'495878', N'Ahorros', 0.00, 150.00, 1, 3),
    (4, N'496825', N'Ahorros', 540.00, 0.00, 1, 2);

SET IDENTITY_INSERT cuentas OFF;
GO

-- Caso de uso 4: Movimientos (saldos disponibles resultantes según el PDF)
-- Caso 5: fechas 2022-02-10 y 2022-02-08 para reporte de Marianela Montalvo
SET IDENTITY_INSERT movimientos ON;

INSERT INTO movimientos (id, fecha, tipo_movimiento, valor, saldo, cuenta_id)
VALUES
    (1, '2022-02-01 10:00:00', N'Retiro', -575.00, 1425.00, 1),
    (2, '2022-02-10 10:00:00', N'Deposito', 600.00, 700.00, 2),
    (3, '2022-02-09 10:00:00', N'Deposito', 150.00, 150.00, 3),
    (4, '2022-02-08 10:00:00', N'Retiro', -540.00, 0.00, 4);

SET IDENTITY_INSERT movimientos OFF;
GO

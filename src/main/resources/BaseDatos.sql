-- Script de base de datos — Sistema Bancario Sofka
-- Ejecutar conectado al motor SQL Server (como sa o usuario con permisos)

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

-- Datos de ejemplo (Jose Lema, Marianela Montalvo, Juan Osorio)
SET IDENTITY_INSERT clientes ON;

INSERT INTO clientes (cliente_id, nombre, genero, edad, identificacion, direccion, telefono, contrasena, estado)
VALUES
    (1, N'Jose Lema', N'M', 35, N'1234567890', N'Otavalo', N'0987654321', N'1234', 1),
    (2, N'Marianela Montalvo', N'F', 32, N'0987654321', N'Quito', N'0991234567', N'5678', 1),
    (3, N'Juan Osorio', N'M', 40, N'1122334455', N'Guayaquil', N'0981122334', N'9012', 1);

SET IDENTITY_INSERT clientes OFF;
GO

SET IDENTITY_INSERT cuentas ON;

INSERT INTO cuentas (id, numero_cuenta, tipo_cuenta, saldo_inicial, saldo_disponible, estado, cliente_id)
VALUES
    (1, N'478758', N'Ahorro', 2000.00, 1425.00, 1, 1),
    (2, N'225487', N'Corriente', 950.00, 700.00, 1, 2),
    (3, N'495878', N'Ahorro', 2000.00, 0.00, 1, 3),
    (4, N'496825', N'Ahorro', 1500.00, 1500.00, 1, 3);

SET IDENTITY_INSERT cuentas OFF;
GO

SET IDENTITY_INSERT movimientos ON;

INSERT INTO movimientos (id, fecha, tipo_movimiento, valor, saldo, cuenta_id)
VALUES
    (1, '2024-01-10 08:00:00', N'Retiro', -575.00, 1425.00, 1),
    (2, '2024-01-11 09:00:00', N'Deposito', 600.00, 700.00, 2),
    (3, '2024-01-12 10:00:00', N'Retiro', -250.00, 700.00, 2),
    (4, '2024-01-13 11:00:00', N'Retiro', -2000.00, 0.00, 3);

SET IDENTITY_INSERT movimientos OFF;
GO

-- ================================================
-- BaseDatos.sql – Esquema de base de datos
-- Sistema Bancario – Prueba Técnica TCS
-- ================================================

CREATE DATABASE IF NOT EXISTS banco_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE banco_db;

SET FOREIGN_KEY_CHECKS = 0;

-- ─────────────────────────────────────────────────────
-- Tabla base: persona
-- ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS persona (
  id             BIGINT       NOT NULL AUTO_INCREMENT,
  nombre         VARCHAR(100) NOT NULL,
  genero         VARCHAR(20)  NOT NULL,
  edad           INT          NOT NULL,
  identificacion VARCHAR(20)  NOT NULL,
  direccion      VARCHAR(200)          DEFAULT NULL,
  telefono       VARCHAR(20)           DEFAULT NULL,
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_persona_identificacion (identificacion)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- ─────────────────────────────────────────────────────
-- Tabla derivada: cliente (hereda de persona – JOINED)
-- ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cliente (
  id         BIGINT       NOT NULL,
  password   VARCHAR(255) NOT NULL,
  estado     TINYINT(1)   NOT NULL DEFAULT 1,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_cliente_persona
    FOREIGN KEY (id) REFERENCES persona (id)
    ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- ─────────────────────────────────────────────────────
-- Tabla: cuenta  (muchas cuentas por cliente)
-- ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cuenta (
  id            BIGINT         NOT NULL AUTO_INCREMENT,
  numero_cuenta VARCHAR(30)    NOT NULL,
  tipo_cuenta   VARCHAR(50)    NOT NULL,
  saldo_inicial DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
  saldo_actual  DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
  estado        TINYINT(1)     NOT NULL DEFAULT 1,
  cliente_id    BIGINT         NOT NULL,
  created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_cuenta_numero (numero_cuenta),
  KEY idx_cuenta_cliente (cliente_id),
  CONSTRAINT fk_cuenta_cliente
    FOREIGN KEY (cliente_id) REFERENCES cliente (id)
    ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- ─────────────────────────────────────────────────────
-- Tabla: movimiento  (transacciones por cuenta)
-- ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS movimiento (
  id               BIGINT         NOT NULL AUTO_INCREMENT,
  cuenta_id        BIGINT         NOT NULL,
  fecha_movimiento DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tipo_movimiento  VARCHAR(20)    NOT NULL,
  valor            DECIMAL(15, 2) NOT NULL,
  saldo            DECIMAL(15, 2) NOT NULL,
  created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_movimiento_cuenta (cuenta_id),
  KEY idx_movimiento_fecha  (fecha_movimiento),
  CONSTRAINT fk_movimiento_cuenta
    FOREIGN KEY (cuenta_id) REFERENCES cuenta (id)
    ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ─────────────────────────────────────────────────────
-- Datos de ejemplo (casos de uso del enunciado)
-- ─────────────────────────────────────────────────────

-- Personas / Clientes
INSERT INTO persona (nombre, genero, edad, identificacion, direccion, telefono)
VALUES
  ('Jose Lema',          'Masculino', 30, '1001000001', 'Otavalo sn y principal',  '098254785'),
  ('Marianela Montalvo', 'Femenino',  28, '1001000002', 'Amazonas y NNUU',         '097548965'),
  ('Juan Osorio',        'Masculino', 25, '1001000003', '13 junio y Equinoccial',  '098874587');

INSERT INTO cliente (id, password, estado)
VALUES
  (1, '1234', 1),
  (2, '5678', 1),
  (3, '1245', 1);

-- Cuentas
INSERT INTO cuenta (numero_cuenta, tipo_cuenta, saldo_inicial, saldo_actual, estado, cliente_id)
VALUES
  ('478758', 'Ahorro',    2000.00, 1425.00, 1, 1),  -- Jose Lema (retiro 575)
  ('225487', 'Corriente',  100.00,  700.00, 1, 2),  -- Marianela (depósito 600)
  ('495878', 'Ahorro',       0.00,  150.00, 1, 3),  -- Juan Osorio (depósito 150)
  ('496825', 'Ahorro',     540.00,    0.00, 1, 2),  -- Marianela (retiro 540)
  ('585545', 'Corriente', 1000.00, 1000.00, 1, 1);  -- Jose Lema nueva cuenta

-- Movimientos
INSERT INTO movimiento (cuenta_id, tipo_movimiento, valor,  saldo)
VALUES
  (1, 'RETIRO',   -575.00, 1425.00),
  (2, 'DEPOSITO',  600.00,  700.00),
  (3, 'DEPOSITO',  150.00,  150.00),
  (4, 'RETIRO',   -540.00,    0.00);

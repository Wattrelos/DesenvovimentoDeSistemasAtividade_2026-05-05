CREATE DATABASE IF NOT EXISTS `locadora`;
USE `locadora`;

-- Atenção: O programa não aceita usuário sem senha (como no PC da Fatec)
-- é necessário ter um usuário e senha:
-- MySQL 8.0+: O comando GRANT ... IDENTIFIED BY foi removido. Primeiro você deve criar o usuário e depois dar as permissões:
-- 1. Cria o usuário primeiro
CREATE USER IF NOT EXISTS 'desenvolvedor'@'%' IDENTIFIED BY 'b2#FbXPQTu4FYw';
-- 2. Garante privilégios totais apenas no banco gwj2
GRANT ALL PRIVILEGES ON `locadora`.* TO 'desenvolvedor'@'%';
-- 6. Aplica as mudanças
FLUSH PRIVILEGES;
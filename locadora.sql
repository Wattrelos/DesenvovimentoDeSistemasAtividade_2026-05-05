-- phpMyAdmin SQL Dump
-- version 5.2.2deb1+deb13u1
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Tempo de geração: 05/05/2026 às 20:53
-- Versão do servidor: 11.8.3-MariaDB-0+deb13u1 from Debian
-- Versão do PHP: 8.4.16

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Banco de dados: `locadora`
--

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_locacao`
--

CREATE TABLE IF NOT EXISTS `table_locacao` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint(20) NOT NULL,
  `data_registro` timestamp NOT NULL,
  `status` enum('Ativo','Finalizado','Cancelado') DEFAULT 'Ativo',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Despejando dados para a tabela `table_locacao`
--

INSERT IGNORE INTO `table_locacao` (`id`, `usuario_id`, `data_registro`, `status`) VALUES
(1, 1, '2023-10-01 15:00:00', 'Finalizado'),
(2, 1, '2023-10-15 20:30:00', 'Ativo'),
(3, 2, '2023-10-10 16:15:00', 'Finalizado'),
(4, 2, '2023-11-02 14:00:00', 'Ativo'),
(5, 3, '2023-11-05 22:45:00', 'Ativo'),
(6, 3, '2023-09-20 17:20:00', 'Cancelado');

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_locacao_veiculo`
--

CREATE TABLE IF NOT EXISTS `table_locacao_veiculo` (
  `locacao_id` bigint(20) NOT NULL,
  `veiculo_id` bigint(20) NOT NULL,
  KEY `fk_locacao` (`locacao_id`),
  KEY `fk_veiculo` (`veiculo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Despejando dados para a tabela `table_locacao_veiculo`
--

INSERT IGNORE INTO `table_locacao_veiculo` (`locacao_id`, `veiculo_id`) VALUES
(1, 5),
(2, 7),
(3, 8),
(4, 10),
(5, 9),
(6, 4);

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_principal`
--

CREATE TABLE IF NOT EXISTS `table_principal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nome` varchar(80) DEFAULT NULL COMMENT 'Nome completo',
  `descricao` text DEFAULT NULL COMMENT 'Descrição',
  `data_cadastro` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Data de Cadastro',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

--
-- Despejando dados para a tabela `table_principal`
--

INSERT IGNORE INTO `table_principal` (`id`, `nome`, `descricao`, `data_cadastro`) VALUES
(1, 'João Batista', 'Cliente preferencial', '2026-05-05 13:43:51'),
(2, 'Dr. Matheus', 'Dar desconteos de até 10%', '2026-05-05 13:46:33'),
(3, 'Lucar Escritor', 'Revisão semestral.', '2026-05-05 13:46:33');

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_registro`
--

CREATE TABLE IF NOT EXISTS `table_registro` (
  `id` bigint(20) NOT NULL COMMENT 'Chave primária',
  `id_usuario` bigint(20) NOT NULL COMMENT 'Chave estrangeira',
  `nome` varchar(80) DEFAULT NULL COMMENT 'Nome completo',
  `status` int(11) NOT NULL COMMENT 'Status',
  `data_registro` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Data de Cadastro',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_usuario`
--

CREATE TABLE IF NOT EXISTS `table_usuario` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nome_usuario` varchar(80) DEFAULT NULL COMMENT 'Nome completo',
  `email` varchar(80) DEFAULT NULL COMMENT 'Endereço de email',
  `telefone` varchar(80) DEFAULT NULL COMMENT 'Telefone',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

--
-- Despejando dados para a tabela `table_usuario`
--

INSERT IGNORE INTO `table_usuario` (`id`, `nome_usuario`, `email`, `telefone`) VALUES
(1, 'JBatista', 'joao.batista@yahoo.com.br', '(11) 1234 5678'),
(2, 'DrMatheus', 'dr.matheus@gmail.com', '(21) 98765 4321'),
(3, 'Lucar', 'lucas.evangelista@gmail.com', '(14) 1234 4321');

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_veiculo`
--

CREATE TABLE IF NOT EXISTS `table_veiculo` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `modelo` varchar(100) NOT NULL,
  `descricao` text DEFAULT NULL,
  `placa` varchar(10) NOT NULL,
  `data_cadastro` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `placa` (`placa`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Despejando dados para a tabela `table_veiculo`
--

INSERT IGNORE INTO `table_veiculo` (`id`, `modelo`, `descricao`, `placa`, `data_cadastro`) VALUES
(1, 'Volkswagen Gol', 'Motor 1.0, 4 portas, Ar-condicionado', 'NCG3C50', '2026-05-05 14:00:48'),
(2, 'Fiat Uno', 'Econômico, 2 portas, Direção Hidráulica', 'QJV9K76', '2026-05-05 14:00:48'),
(3, 'Chevrolet Onix', 'Motor Turbo, Central Multimídia, Câmera de ré', 'TIX1A74', '2026-05-05 14:00:48'),
(4, 'Toyota Corolla', 'Sedan de luxo, Automático, Bancos de couro', 'RZL8C45', '2026-05-05 14:00:48'),
(5, 'Jeep Compass', 'SUV, Diesel, 4x4, Teto solar', 'ARD8L70', '2026-05-05 14:00:48'),
(6, 'Hyundai HB20', 'Compacto, Bluetooth, Comandos no volante', 'KLY1H69', '2026-05-05 14:00:48'),
(7, 'Ford Ka', 'Motor 1.5, Direção elétrica, Airbag duplo', 'KTA4V75', '2026-05-05 14:00:48'),
(8, 'Renault Sandero', 'Espaçoso, Porta-malas amplo, Flex', 'NNB0L90', '2026-05-05 14:00:48'),
(9, 'Honda Civic', 'Design esportivo, Automático, Controle de tração', 'VLU7J92', '2026-05-05 14:00:48'),
(10, 'Nissan Kicks', 'SUV compacto, Sensor de estacionamento, Econômico', 'FLY1M83', '2026-05-05 14:00:48');

--
-- Restrições para tabelas despejadas
--

--
-- Restrições para tabelas `table_locacao_veiculo`
--
ALTER TABLE `table_locacao_veiculo`
  ADD CONSTRAINT `fk_locacao` FOREIGN KEY (`locacao_id`) REFERENCES `table_locacao` (`id`),
  ADD CONSTRAINT `fk_veiculo` FOREIGN KEY (`veiculo_id`) REFERENCES `table_veiculo` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
